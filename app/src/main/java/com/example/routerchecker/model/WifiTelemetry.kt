package com.example.routerchecker.model

data class WifiTelemetry(
    val isConnected: Boolean = false,
    val ssid: String = "Disconnected",
    val bssid: String = "--:--:--:--:--:--",
    val localIp: String = "0.0.0.0",
    val gatewayIp: String = "192.168.1.1",
    val linkSpeedMbps: Int = 0,
    val signalDbm: Int = 0,
    val frequencyMhz: Int = 0,
    val subnetPrefix: String = "192.168.1."
)
