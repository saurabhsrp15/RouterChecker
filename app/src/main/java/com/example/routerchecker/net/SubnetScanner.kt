package com.example.routerchecker.net

import com.example.routerchecker.model.DeviceType
import com.example.routerchecker.model.DiscoveredDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger

object SubnetScanner {

    private val SCAN_PORTS = listOf(80, 443, 22, 53, 445, 8080)

    suspend fun scanSubnet(
        subnetPrefix: String = "192.168.1.",
        onDeviceFound: (DiscoveredDevice) -> Unit,
        onProgress: (progress: Float, currentIp: String) -> Unit
    ): List<DiscoveredDevice> = withContext(Dispatchers.IO) {
        val discoveredList = mutableListOf<DiscoveredDevice>()
        val semaphore = Semaphore(25) // 25 concurrent probes for high speed without network congestion
        val completedCount = AtomicInteger(0)
        val totalIps = 254

        val tasks = (1..totalIps).map { hostNum ->
            async {
                val targetIp = "$subnetPrefix$hostNum"
                semaphore.withPermit {
                    val progress = completedCount.incrementAndGet().toFloat() / totalIps
                    onProgress(progress, targetIp)

                    val device = probeHost(targetIp)
                    if (device != null) {
                        synchronized(discoveredList) {
                            discoveredList.add(device)
                        }
                        onDeviceFound(device)
                    }
                }
            }
        }

        tasks.awaitAll()
        discoveredList.sortedBy { device ->
            val lastOctet = device.ip.substringAfterLast(".").toIntOrNull() ?: 999
            lastOctet
        }
    }

    private fun probeHost(ip: String): DiscoveredDevice? {
        try {
            val address = InetAddress.getByName(ip)
            val startTime = System.currentTimeMillis()

            var isAlive = false
            try {
                isAlive = address.isReachable(450)
            } catch (e: Exception) {
                // Ignore and proceed to port probe
            }

            val openPorts = mutableListOf<Int>()
            // Test primary web & DNS ports
            for (port in SCAN_PORTS) {
                if (testPort(ip, port, 300)) {
                    openPorts.add(port)
                    isAlive = true
                }
            }

            if (!isAlive) return null

            val latency = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
            var hostname = try {
                address.canonicalHostName
            } catch (e: Exception) {
                ip
            }
            if (hostname == ip || hostname.isBlank()) {
                hostname = address.hostName ?: ip
            }

            val deviceType = inferDeviceType(ip, hostname, openPorts)

            return DiscoveredDevice(
                ip = ip,
                hostname = hostname,
                latencyMs = latency,
                openPorts = openPorts,
                deviceType = deviceType
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun testPort(ip: String, port: Int, timeoutMs: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeoutMs)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun inferDeviceType(ip: String, hostname: String, openPorts: List<Int>): DeviceType {
        val lowerHost = hostname.lowercase()
        return when {
            ip.endsWith(".1") || openPorts.contains(53) && openPorts.contains(80) ||
            lowerHost.contains("router") || lowerHost.contains("gateway") || lowerHost.contains("openwrt") ->
                DeviceType.ROUTER_GATEWAY

            openPorts.contains(22) || openPorts.contains(445) ||
            lowerHost.contains("nas") || lowerHost.contains("server") || lowerHost.contains("synology") ->
                DeviceType.SERVER_NAS

            lowerHost.contains("desktop") || lowerHost.contains("laptop") || lowerHost.contains("macbook") || lowerHost.contains("pc") ->
                DeviceType.COMPUTER

            lowerHost.contains("iphone") || lowerHost.contains("android") || lowerHost.contains("galaxy") || lowerHost.contains("pixel") ->
                DeviceType.MOBILE

            openPorts.contains(8080) || lowerHost.contains("tv") || lowerHost.contains("cast") || lowerHost.contains("echo") || lowerHost.contains("cam") ->
                DeviceType.IOT_DEVICE

            else -> DeviceType.GENERIC
        }
    }
}
