package com.example.routerchecker.model

data class RouterState(
    val entry: RouterEntry,
    val status: RouterStatus = RouterStatus.Idle,
    val speedStatus: SpeedTestStatus = SpeedTestStatus.Idle,
    val lastCheckedTimestamp: Long = 0L
)
