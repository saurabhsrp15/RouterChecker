package com.example.routerchecker.model

import java.util.UUID

data class RouterEntry(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val ip: String,
    val isCustom: Boolean = false
) {
    companion object {
        val DEFAULT_ROUTERS: List<RouterEntry> = listOf(
            RouterEntry(id = "router-main", label = "Main Router", ip = "192.168.1.1", isCustom = false),
            RouterEntry(id = "router-2", label = "Router 2", ip = "192.168.1.247", isCustom = false),
            RouterEntry(id = "router-3", label = "Router 3", ip = "192.168.1.248", isCustom = false),
            RouterEntry(id = "router-4", label = "Router 4", ip = "192.168.1.249", isCustom = false),
            RouterEntry(id = "router-5", label = "Router 5", ip = "192.168.1.250", isCustom = false)
        )
    }
}
