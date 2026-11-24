package com.example.routerchecker.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routerchecker.model.RouterState
import com.example.routerchecker.model.RouterStatus
import com.example.routerchecker.model.SpeedTestStatus

@Composable
fun RouterCard(
    state: RouterState,
    nodeIndex: Int,
    onCheck: () -> Unit,
    onSpeedTest: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(TechSurface)
            .border(
                1.dp,
                when (state.status) {
                    is RouterStatus.Online -> TechBorder
                    is RouterStatus.Checking -> TechAmber.copy(alpha = 0.5f)
                    is RouterStatus.Offline -> TechRed.copy(alpha = 0.4f)
                    else -> TechBorder
                },
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Node index + Status badge + Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TechSurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NODE [0${nodeIndex + 1}]",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TechCyan
                        )
                    }

                    if (state.entry.isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TechPurple.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CUSTOM",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TechPurple
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusIndicatorBadge(status = state.status)

                    if (state.entry.isCustom) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = TechTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Label and Monospace IP address
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = state.entry.label,
                        color = TechTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.entry.ip,
                        color = TechCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Quick Ping latency display
                when (val s = state.status) {
                    is RouterStatus.Online -> {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${s.latencyMs} ms",
                                color = TechGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            if (s.jitterMs > 0) {
                                Text(
                                    text = "±${s.jitterMs}ms jitter",
                                    color = TechTextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                    is RouterStatus.Checking -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = TechAmber,
                            strokeWidth = 2.dp
                        )
                    }
                    is RouterStatus.Offline -> {
                        Text(
                            text = "NO REPLY",
                            color = TechRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    else -> {}
                }
            }

            // Open ports tag if online
            if (state.status is RouterStatus.Online) {
                val onlineStatus = state.status
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ACTIVE SERVICES:",
                        fontSize = 9.sp,
                        color = TechTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    if (onlineStatus.openPorts.isNotEmpty()) {
                        onlineStatus.openPorts.forEach { port ->
                            val serviceName = when (port) {
                                80 -> "HTTP(80)"
                                443 -> "HTTPS(443)"
                                8080 -> "MGMT(8080)"
                                53 -> "DNS(53)"
                                else -> "$port"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TechSurfaceVariant)
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = serviceName,
                                    fontSize = 9.sp,
                                    color = TechCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "ICMP ONLY",
                            fontSize = 9.sp,
                            color = TechTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Speed Test Status Card / Actions
            SpeedTestSection(
                speedStatus = state.speedStatus,
                isOnline = state.status is RouterStatus.Online,
                onRunSpeedTest = onSpeedTest
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions Bar (Open Web Admin + Quick Recheck)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Open Web Admin button
                OutlinedButton(
                    onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://${state.entry.ip}"))
                        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try {
                            context.startActivity(browserIntent)
                        } catch (e: Exception) {
                            // Handled if no browser
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TechBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TechCyan
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Language,
                        contentDescription = "Web Admin",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WEB ADMIN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Recheck ping button
                OutlinedButton(
                    onClick = onCheck,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TechBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TechTextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Ping",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RE-PING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusIndicatorBadge(status: RouterStatus) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val (badgeText, badgeColor) = when (status) {
        is RouterStatus.Online -> "ONLINE" to TechGreen
        is RouterStatus.Checking -> "PROBING..." to TechAmber
        is RouterStatus.Offline -> "OFFLINE" to TechRed
        is RouterStatus.Error -> "ERROR" to TechAmber
        is RouterStatus.Idle -> "IDLE" to TechTextMuted
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(badgeColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(
                    if (status is RouterStatus.Checking) badgeColor.copy(alpha = alpha) else badgeColor
                )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = badgeText,
            color = badgeColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun SpeedTestSection(
    speedStatus: SpeedTestStatus,
    isOnline: Boolean,
    onRunSpeedTest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TechSurfaceVariant)
            .padding(10.dp)
    ) {
        when (speedStatus) {
            is SpeedTestStatus.Idle -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Speed,
                            contentDescription = "Speed Test",
                            tint = TechPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "BANDWIDTH TEST",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TechTextPrimary
                            )
                            Text(
                                text = "Measure real throughput & jitter",
                                fontSize = 9.sp,
                                color = TechTextMuted
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onRunSpeedTest,
                        enabled = isOnline,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TechPurple),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TechPurple
                        ),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = "⚡ TEST SPEED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            is SpeedTestStatus.Testing -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = speedStatus.stage.uppercase(),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TechAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (speedStatus.currentMbps > 0) "${speedStatus.currentMbps} Mbps" else "--",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TechCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { speedStatus.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = TechCyan,
                        trackColor = TechSurface
                    )
                }
            }

            is SpeedTestStatus.Completed -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${speedStatus.downloadMbps} Mbps",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TechCyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TechGreenMuted)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = speedStatus.rating,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TechGreen
                                )
                            }
                        }
                        Text(
                            text = "Ping: ${speedStatus.pingMs}ms | Jitter: ±${speedStatus.jitterMs}ms",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TechTextMuted
                        )
                    }

                    OutlinedButton(
                        onClick = onRunSpeedTest,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TechBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TechTextSecondary),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("RETEST", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            is SpeedTestStatus.Failed -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SPEED TEST FAILED: ${speedStatus.error}",
                        fontSize = 10.sp,
                        color = TechRed,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = onRunSpeedTest,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("RETRY", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
