package com.example.routerchecker.data

import android.content.Context
import android.content.SharedPreferences
import com.example.routerchecker.model.RouterEntry
import org.json.JSONArray
import org.json.JSONObject

class RouterRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("router_prefs", Context.MODE_PRIVATE)

    fun getRouters(): List<RouterEntry> {
        val jsonString = prefs.getString("saved_routers", null) ?: return RouterEntry.DEFAULT_ROUTERS
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<RouterEntry>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    RouterEntry(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        label = obj.getString("label"),
                        ip = obj.getString("ip"),
                        isCustom = obj.optBoolean("isCustom", false)
                    )
                )
            }
            if (list.isEmpty()) RouterEntry.DEFAULT_ROUTERS else list
        } catch (e: Exception) {
            RouterEntry.DEFAULT_ROUTERS
        }
    }

    fun saveRouters(routers: List<RouterEntry>) {
        val jsonArray = JSONArray()
        routers.forEach { entry ->
            val obj = JSONObject().apply {
                put("id", entry.id)
                put("label", entry.label)
                put("ip", entry.ip)
                put("isCustom", entry.isCustom)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("saved_routers", jsonArray.toString()).apply()
    }

    fun addRouter(label: String, ip: String): List<RouterEntry> {
        val current = getRouters().toMutableList()
        val newEntry = RouterEntry(
            label = label.trim(),
            ip = ip.trim(),
            isCustom = true
        )
        current.add(newEntry)
        saveRouters(current)
        return current
    }

    fun removeRouter(id: String): List<RouterEntry> {
        val current = getRouters().filterNot { it.id == id }
        val updated = if (current.isEmpty()) RouterEntry.DEFAULT_ROUTERS else current
        saveRouters(updated)
        return updated
    }

    fun resetToDefaults(): List<RouterEntry> {
        saveRouters(RouterEntry.DEFAULT_ROUTERS)
        return RouterEntry.DEFAULT_ROUTERS
    }
}
