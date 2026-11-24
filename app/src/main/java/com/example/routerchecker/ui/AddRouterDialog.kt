package com.example.routerchecker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddRouterDialog(
    onDismiss: () -> Unit,
    onAdd: (label: String, ip: String) -> Unit,
    suggestedSubnet: String = "192.168.1."
) {
    var label by remember { mutableStateOf("") }
    var ip by remember { mutableStateOf(suggestedSubnet) }
    var isIpError by remember { mutableStateOf(false) }

    val quickLabels = listOf("AP Living Room", "Mesh Node", "Office AP", "Extender", "CCTV Router")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TechSurface,
        titleContentColor = TechCyan,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.border(1.dp, TechBorder, RoundedCornerShape(16.dp)),
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "ADD ROUTER NODE",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TechCyan
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Configure a target router or access point to monitor.",
                    fontSize = 12.sp,
                    color = TechTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Node Name / Label", color = TechTextSecondary) },
                    placeholder = { Text("e.g. Mesh Satellite 02", color = TechTextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TechCyan,
                        unfocusedBorderColor = TechBorder,
                        focusedTextColor = TechTextPrimary,
                        unfocusedTextColor = TechTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick label suggestion chips
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickLabels.take(3).forEach { suggestion ->
                        SuggestionChip(
                            onClick = { label = suggestion },
                            label = { Text(suggestion, fontSize = 10.sp, color = TechCyan) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = TechSurfaceVariant
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = TechBorder
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = ip,
                    onValueChange = {
                        ip = it.trim()
                        isIpError = !isValidIpv4(ip)
                    },
                    label = { Text("IPv4 Address", color = TechTextSecondary) },
                    placeholder = { Text("192.168.1.xxx", color = TechTextMuted) },
                    singleLine = true,
                    isError = isIpError,
                    supportingText = {
                        if (isIpError) {
                            Text("Invalid IPv4 address format", color = TechRed, fontSize = 10.sp)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TechCyan,
                        unfocusedBorderColor = TechBorder,
                        errorBorderColor = TechRed,
                        focusedTextColor = TechCyan,
                        unfocusedTextColor = TechCyan
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValidIpv4(ip)) {
                        val finalLabel = if (label.isBlank()) "Router $ip" else label
                        onAdd(finalLabel, ip)
                    } else {
                        isIpError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = TechCyan,
                    contentColor = TechSurface
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "DEPLOY NODE",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TechBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TechTextSecondary)
            ) {
                Text("CANCEL", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
        }
    )
}

private fun isValidIpv4(ip: String): Boolean {
    val parts = ip.split(".")
    if (parts.size != 4) return false
    return parts.all { part ->
        val num = part.toIntOrNull() ?: return false
        num in 0..255
    }
}
