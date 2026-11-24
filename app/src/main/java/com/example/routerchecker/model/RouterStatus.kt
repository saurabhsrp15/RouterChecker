package com.example.routerchecker.model

sealed class RouterStatus {
    object Idle : RouterStatus()
    object Checking : RouterStatus()
    data class Online(
        val latencyMs: Long,
        val jitterMs: Long = 0,
        val openPorts: List<Int> = emptyList(),
        val protocol: String = "ICMP/TCP"
    ) : RouterStatus()
    object Offline : RouterStatus()
    data class Error(val message: String) : RouterStatus()
}
