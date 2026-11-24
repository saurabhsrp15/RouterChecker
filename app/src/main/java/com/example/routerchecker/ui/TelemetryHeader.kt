package com.example.routerchecker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routerchecker.model.WifiTelemetry

@Composable
fun TelemetryHeader(
    telemetry: WifiTelemetry,
    onlineNodesCount: Int,
    totalNodesCount: Int,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(TechSurface)
            .border(1.dp, TechBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: SSID + Status beacon + Refresh
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (telemetry.isConnected) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                        contentDescription = "Wi-Fi",
                        tint = if (telemetry.isConnected) TechCyan else TechRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = telemetry.ssid.uppercase(),
                            color = TechTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (telemetry.bssid != "--:--:--:--:--:--") "BSSID: ${telemetry.bssid}" else "NETWORK LINK ACTIVE",
                            color = TechTextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // System online badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (onlineNodesCount == totalNodesCount && totalNodesCount > 0) TechGreenMuted else TechSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (onlineNodesCount > 0) TechGreen else TechRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$onlineNodesCount/$totalNodesCount NODES",
                                color = if (onlineNodesCount > 0) TechGreen else TechRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh Telemetry",
                            tint = TechCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tech Telemetry Matrix (Link Speed, Signal dBm, Gateway, Local IP)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TechSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryMetricItem(
                    label = "GATEWAY",
                    value = telemetry.gatewayIp,
                    highlight = true
                )
                TelemetryMetricItem(
                    label = "LOCAL IP",
                    value = telemetry.localIp,
                    highlight = false
                )
                TelemetryMetricItem(
                    label = "PHY RATE",
                    value = if (telemetry.linkSpeedMbps > 0) "${telemetry.linkSpeedMbps}M" else "--",
                    highlight = false
                )
                TelemetryMetricItem(
                    label = "RSSI",
                    value = if (telemetry.signalDbm != 0) "${telemetry.signalDbm}dBm" else "--",
                    highlight = false
                )
            }
        }
    }
}

@Composable
private fun TelemetryMetricItem(label: String, value: String, highlight: Boolean) {
    Column {
        Text(
            text = label,
            fontSize = 9.sp,
            color = TechTextMuted,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 11.sp,
            color = if (highlight) TechCyan else TechTextPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
