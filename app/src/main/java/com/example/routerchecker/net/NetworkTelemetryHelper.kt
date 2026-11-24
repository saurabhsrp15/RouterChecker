package com.example.routerchecker.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import com.example.routerchecker.model.WifiTelemetry
import java.net.Inet4Address
import java.net.NetworkInterface

object NetworkTelemetryHelper {

    fun getTelemetry(context: Context): WifiTelemetry {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

            val activeNetwork = connManager?.activeNetwork
            val capabilities = connManager?.getNetworkCapabilities(activeNetwork)
            val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

            if (!isWifi || wifiManager == null) {
                val cellularOrEthernetIp = getLocalIpv4Address()
                return WifiTelemetry(
                    isConnected = activeNetwork != null,
                    ssid = if (activeNetwork != null) "Cellular / Ethernet" else "Offline",
                    localIp = cellularOrEthernetIp,
                    gatewayIp = "192.168.1.1",
                    subnetPrefix = deriveSubnetPrefix(cellularOrEthernetIp)
                )
            }

            val connectionInfo = wifiManager.connectionInfo
            val rawSsid = connectionInfo.ssid ?: "Wi-Fi Connected"
            val cleanSsid = rawSsid.replace("\"", "")

            val linkSpeed = connectionInfo.linkSpeed.coerceAtLeast(0)
            val rssi = connectionInfo.rssi
            val bssid = connectionInfo.bssid ?: "--:--:--:--:--:--"
            val freq = connectionInfo.frequency

            val dhcpInfo = wifiManager.dhcpInfo
            val gatewayIp = formatIp(dhcpInfo?.gateway ?: 0).ifBlank { "192.168.1.1" }
            val localIp = getLocalIpv4Address().ifBlank { formatIp(dhcpInfo?.ipAddress ?: 0) }
            val subnetPrefix = deriveSubnetPrefix(localIp.ifBlank { gatewayIp })

            WifiTelemetry(
                isConnected = true,
                ssid = if (cleanSsid.contains("unknown", ignoreCase = true)) "Local Wi-Fi" else cleanSsid,
                bssid = bssid,
                localIp = localIp,
                gatewayIp = if (gatewayIp == "0.0.0.0") "192.168.1.1" else gatewayIp,
                linkSpeedMbps = linkSpeed,
                signalDbm = rssi,
                frequencyMhz = freq,
                subnetPrefix = subnetPrefix
            )
        } catch (e: Exception) {
            WifiTelemetry(
                isConnected = true,
                ssid = "Local Wi-Fi",
                localIp = getLocalIpv4Address(),
                gatewayIp = "192.168.1.1",
                subnetPrefix = "192.168.1."
            )
        }
    }

    private fun formatIp(ipInt: Int): String {
        return if (ipInt == 0) "" else "${ipInt and 0xFF}.${ipInt shr 8 and 0xFF}.${ipInt shr 16 and 0xFF}.${ipInt shr 24 and 0xFF}"
    }

    private fun getLocalIpv4Address(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val hostAddress = addr.hostAddress ?: ""
                        if (!hostAddress.startsWith("127.")) {
                            return hostAddress
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        return "192.168.1.100"
    }

    private fun deriveSubnetPrefix(ip: String): String {
        val parts = ip.split(".")
        return if (parts.size >= 3) {
            "${parts[0]}.${parts[1]}.${parts[2]}."
        } else {
            "192.168.1."
        }
    }
}
