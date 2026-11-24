package com.example.routerchecker.net

import com.example.routerchecker.model.RouterStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.abs

object ConnectivityChecker {

    private val PROBE_PORTS = listOf(80, 443, 8080, 53)

    /**
     * Checks router reachability, average latency, jitter, and open ports.
     */
    suspend fun checkRouter(ip: String): RouterStatus = withContext(Dispatchers.IO) {
        try {
            val address = InetAddress.getByName(ip)
            val latencies = mutableListOf<Long>()

            // Probe 3 times for reliable RTT and jitter
            for (i in 0 until 3) {
                val start = System.currentTimeMillis()
                val reachable = try {
                    address.isReachable(1200)
                } catch (e: Exception) {
                    false
                }
                val rtt = System.currentTimeMillis() - start

                if (reachable) {
                    latencies.add(rtt)
                } else {
                    // Fallback to TCP socket on port 80 / 443
                    val tcpStart = System.currentTimeMillis()
                    val tcpReachable = tryTcpConnect(ip, 80, 1000) || tryTcpConnect(ip, 443, 1000)
                    if (tcpReachable) {
                        latencies.add(System.currentTimeMillis() - tcpStart)
                    }
                }
            }

            if (latencies.isEmpty()) {
                return@withContext RouterStatus.Offline
            }

            val avgLatency = latencies.average().toLong()
            val jitter = if (latencies.size > 1) {
                var sumDiff = 0L
                for (i in 0 until latencies.size - 1) {
                    sumDiff += abs(latencies[i + 1] - latencies[i])
                }
                sumDiff / (latencies.size - 1)
            } else {
                0L
            }

            // Quick check for open admin/services ports
            val openPorts = mutableListOf<Int>()
            for (port in PROBE_PORTS) {
                if (tryTcpConnect(ip, port, 400)) {
                    openPorts.add(port)
                }
            }

            RouterStatus.Online(
                latencyMs = avgLatency,
                jitterMs = jitter,
                openPorts = openPorts,
                protocol = if (openPorts.isNotEmpty()) "ICMP/TCP" else "ICMP"
            )
        } catch (e: Exception) {
            RouterStatus.Error(e.message ?: "Connection failed")
        }
    }

    fun tryTcpConnect(ip: String, port: Int, timeoutMs: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeoutMs)
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
