package com.example.routerchecker.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routerchecker.model.DeviceType
import com.example.routerchecker.model.DiscoveredDevice

@Composable
fun SubnetRadarView(
    devices: List<DiscoveredDevice>,
    isScanning: Boolean,
    progress: Float,
    currentScanningIp: String,
    subnetPrefix: String,
    onStartScan: () -> Unit,
    onAddDeviceAsNode: (ip: String, label: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Radar Control Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(TechSurface)
                .border(1.dp, TechBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadarPulseIcon(isScanning = isScanning)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SUBNET DEVICE RADAR",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TechCyan
                            )
                            Text(
                                text = "Scanning scope: ${subnetPrefix}1 - ${subnetPrefix}254",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TechTextMuted
                            )
                        }
                    }

                    Button(
                        onClick = onStartScan,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isScanning) TechRed else TechCyan,
                            contentColor = TechSurface
                        )
                    ) {
                        Text(
                            text = if (isScanning) "STOP" else "SCAN SUBNET",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }

                if (isScanning) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SWEEPING: $currentScanningIp",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TechAmber
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TechCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = TechCyan,
                        trackColor = TechSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Device counter header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${devices.size} ACTIVE DEVICES DETECTED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TechTextSecondary
            )

            if (devices.isNotEmpty()) {
                Text(
                    text = "TAP + TO MONITOR NODE",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TechTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (devices.isEmpty() && !isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Radar,
                        contentDescription = "Radar",
                        tint = TechTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NO DEVICES SCANNED YET",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TechTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap 'SCAN SUBNET' above to discover all live routers, computers, NAS and phones.",
                        fontSize = 11.sp,
                        color = TechTextMuted,
                        modifier = Modifier.padding(horizontal = 30.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(devices, key = { it.ip }) { device ->
                    DiscoveredDeviceItem(
                        device = device,
                        onAddAsNode = { onAddDeviceAsNode(device.ip, device.hostname) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun DiscoveredDeviceItem(
    device: DiscoveredDevice,
    onAddAsNode: () -> Unit
) {
    val (typeIcon, typeColor, typeLabel) = when (device.deviceType) {
        DeviceType.ROUTER_GATEWAY -> Triple(Icons.Filled.Router, TechCyan, "GATEWAY / ROUTER")
        DeviceType.SERVER_NAS -> Triple(Icons.Filled.Storage, TechPurple, "SERVER / NAS")
        DeviceType.COMPUTER -> Triple(Icons.Filled.Computer, Color(0xFF42A5F5), "WORKSTATION")
        DeviceType.MOBILE -> Triple(Icons.Filled.PhoneAndroid, TechGreen, "MOBILE")
        DeviceType.IOT_DEVICE -> Triple(Icons.Filled.DevicesOther, TechAmber, "IOT / EMBEDDED")
        DeviceType.GENERIC -> Triple(Icons.Filled.DevicesOther, TechTextSecondary, "HOST")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TechSurface)
            .border(1.dp, TechBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(typeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = typeLabel,
                        tint = typeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = device.ip,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TechTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(typeColor.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = typeLabel,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = typeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = if (device.hostname != device.ip) device.hostname else "Unresolved Hostname",
                        fontSize = 11.sp,
                        color = TechTextSecondary,
                        maxLines = 1
                    )

                    if (device.openPorts.isNotEmpty()) {
                        Text(
                            text = "Ports: " + device.openPorts.joinToString(", ") { "$it" },
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TechCyan
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${device.latencyMs}ms",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TechGreen
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onAddAsNode,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(TechSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add as Monitored Node",
                        tint = TechCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RadarPulseIcon(isScanning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarRotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarSpin"
    )

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (isScanning) TechCyan.copy(alpha = 0.15f) else TechSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Radar,
            contentDescription = "Radar",
            tint = if (isScanning) TechCyan else TechTextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
