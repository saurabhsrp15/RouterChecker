package com.example.routerchecker.model

sealed class SpeedTestStatus {
    object Idle : SpeedTestStatus()
    data class Testing(
        val stage: String, // "Measuring Ping", "Testing Download", etc.
        val progress: Float, // 0f to 1f
        val currentMbps: Double
    ) : SpeedTestStatus()
    data class Completed(
        val pingMs: Long,
        val jitterMs: Long,
        val downloadMbps: Double,
        val rating: String // "EXCELLENT", "GOOD", "FAIR", "POOR"
    ) : SpeedTestStatus()
    data class Failed(val error: String) : SpeedTestStatus()
}
