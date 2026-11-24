package com.example.routerchecker.ui

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.routerchecker.model.RouterStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: RouterViewModel) {
    val states by viewModel.states.collectAsStateWithLifecycle()
    val isCheckingAll by viewModel.isCheckingAll.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val discoveredDevices by viewModel.discoveredDevices.collectAsStateWithLifecycle()
    val isScanningSubnet by viewModel.isScanningSubnet.collectAsStateWithLifecycle()
    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()
    val scanCurrentIp by viewModel.scanCurrentIp.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val showAddDialog by viewModel.showAddDialog.collectAsStateWithLifecycle()

    val onlineCount = states.count { it.status is RouterStatus.Online }
    val totalCount = states.size

    if (showAddDialog) {
        AddRouterDialog(
            onDismiss = { viewModel.setShowAddDialog(false) },
            onAdd = { label, ip -> viewModel.addRouter(label, ip) },
            suggestedSubnet = telemetry.subnetPrefix
        )
    }

    Scaffold(
        containerColor = TechBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ROUTER SENTINEL",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = TechCyan
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TechSurfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "NOC V2",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = TechTextSecondary
                                )
                            }
                        }
                        Text(
                            text = "NETWORK OPERATIONS & TELEMETRY",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TechTextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TechBackground,
                    titleContentColor = TechCyan
                )
            )
        },
        floatingActionButton = {
            if (activeTab == 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Add Router Button
                    FloatingActionButton(
                        onClick = { viewModel.setShowAddDialog(true) },
                        containerColor = TechSurfaceVariant,
                        contentColor = TechCyan,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Router IP")
                    }

                    // Check All Button
                    ExtendedFloatingActionButton(
                        onClick = { if (!isCheckingAll) viewModel.checkAll() },
                        containerColor = TechCyan,
                        contentColor = TechSurface,
                        shape = RoundedCornerShape(12.dp),
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Check All"
                            )
                        },
                        text = {
                            Text(
                                text = if (isCheckingAll) "PROBING..." else "PING ALL",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live Wi-Fi & Gateway Telemetry Banner
            TelemetryHeader(
                telemetry = telemetry,
                onlineNodesCount = onlineCount,
                totalNodesCount = totalCount,
                onRefresh = { viewModel.refreshTelemetry() }
            )

            // Technical Navigation Tabs
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = TechBackground,
                contentColor = TechCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = TechCyan,
                        height = 2.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    text = {
                        Text(
                            text = "ROUTERS (${states.size})",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    },
                    icon = { Icon(Icons.Filled.Router, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )

                Tab(
                    selected = activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    text = {
                        Text(
                            text = "DEVICE RADAR (${discoveredDevices.size})",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    },
                    icon = { Icon(Icons.Filled.Radar, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )

                Tab(
                    selected = activeTab == 2,
                    onClick = { viewModel.setActiveTab(2) },
                    text = {
                        Text(
                            text = "DIAGNOSTICS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    },
                    icon = { Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Pages
            when (activeTab) {
                0 -> {
                    // Routers List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        itemsIndexed(states, key = { _, item -> item.entry.id }) { index, routerState ->
                            RouterCard(
                                state = routerState,
                                nodeIndex = index,
                                onCheck = { viewModel.checkSingle(routerState.entry) },
                                onSpeedTest = { viewModel.runSpeedTest(routerState.entry) },
                                onDelete = { viewModel.deleteRouter(routerState.entry.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(90.dp)) } // FAB clearance
                    }
                }

                1 -> {
                    // Subnet Radar Scan (Show all connected devices on network)
                    SubnetRadarView(
                        devices = discoveredDevices,
                        isScanning = isScanningSubnet,
                        progress = scanProgress,
                        currentScanningIp = scanCurrentIp,
                        subnetPrefix = telemetry.subnetPrefix,
                        onStartScan = { viewModel.startSubnetScan() },
                        onAddDeviceAsNode = { ip, label ->
                            viewModel.addRouter(label, ip)
                            viewModel.setActiveTab(0)
                        }
                    )
                }

                2 -> {
                    // Network Diagnostics View
                    DiagnosticsView(telemetry = telemetry, onRefresh = { viewModel.refreshTelemetry() })
                }
            }
        }
    }
}

@Composable
fun DiagnosticsView(
    telemetry: com.example.routerchecker.model.WifiTelemetry,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TechSurface)
                    .border(1.dp, TechBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "NETWORK ADAPTER TELEMETRY",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TechCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DiagnosticRow("CONNECTION STATUS", if (telemetry.isConnected) "ACTIVE / ASSOCIATED" else "OFFLINE", TechGreen)
                    DiagnosticRow("SSID NETWORK", telemetry.ssid, TechTextPrimary)
                    DiagnosticRow("BSSID MAC", telemetry.bssid, TechCyan)
                    DiagnosticRow("LOCAL IP (DEVICE)", telemetry.localIp, TechCyan)
                    DiagnosticRow("DEFAULT GATEWAY (ROUTER)", telemetry.gatewayIp, TechCyan)
                    DiagnosticRow("RADIO FREQUENCY", if (telemetry.frequencyMhz > 0) "${telemetry.frequencyMhz} MHz" else "N/A", TechTextSecondary)
                    DiagnosticRow("PHYSICAL LINK SPEED", if (telemetry.linkSpeedMbps > 0) "${telemetry.linkSpeedMbps} Mbps" else "N/A", TechTextSecondary)
                    DiagnosticRow("SIGNAL STRENGTH (RSSI)", if (telemetry.signalDbm != 0) "${telemetry.signalDbm} dBm" else "N/A", TechAmber)
                    DiagnosticRow("SUBNET BASE PREFIX", telemetry.subnetPrefix + "0/24", TechTextSecondary)
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TechSurface)
                    .border(1.dp, TechBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ICMP & TCP PROBE MATRIX",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TechCyan
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• Primary Check: Direct ICMP ping via InetAddress with 1200ms timeout\n" +
                               "• Secondary Fallback: TCP Socket connect to port 80 & 443\n" +
                               "• Port Profiling: Scans ports 80 (HTTP), 443 (HTTPS), 8080 (MGMT), 53 (DNS)\n" +
                               "• Bandwidth Measurement: Real-time throughput stream calculation in Mbps",
                        fontSize = 11.sp,
                        color = TechTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = TechTextMuted,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = valueColor
        )
    }
}
