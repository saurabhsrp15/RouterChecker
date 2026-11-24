package com.example.routerchecker.model

enum class DeviceType {
    ROUTER_GATEWAY,
    SERVER_NAS,
    COMPUTER,
    MOBILE,
    IOT_DEVICE,
    GENERIC
}

data class DiscoveredDevice(
    val ip: String,
    val hostname: String,
    val latencyMs: Long,
    val openPorts: List<Int>,
    val deviceType: DeviceType
)
