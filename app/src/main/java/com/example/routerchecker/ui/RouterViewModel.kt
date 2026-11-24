package com.example.routerchecker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.routerchecker.data.RouterRepository
import com.example.routerchecker.model.DiscoveredDevice
import com.example.routerchecker.model.RouterEntry
import com.example.routerchecker.model.RouterState
import com.example.routerchecker.model.RouterStatus
import com.example.routerchecker.model.SpeedTestStatus
import com.example.routerchecker.model.WifiTelemetry
import com.example.routerchecker.net.ConnectivityChecker
import com.example.routerchecker.net.NetworkTelemetryHelper
import com.example.routerchecker.net.SpeedTestEngine
import com.example.routerchecker.net.SubnetScanner
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RouterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RouterRepository(application)

    private val _states = MutableStateFlow<List<RouterState>>(emptyList())
    val states: StateFlow<List<RouterState>> = _states.asStateFlow()

    private val _isCheckingAll = MutableStateFlow(false)
    val isCheckingAll: StateFlow<Boolean> = _isCheckingAll.asStateFlow()

    private val _telemetry = MutableStateFlow(WifiTelemetry())
    val telemetry: StateFlow<WifiTelemetry> = _telemetry.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    private val _isScanningSubnet = MutableStateFlow(false)
    val isScanningSubnet: StateFlow<Boolean> = _isScanningSubnet.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _scanCurrentIp = MutableStateFlow("")
    val scanCurrentIp: StateFlow<String> = _scanCurrentIp.asStateFlow()

    private val _activeTab = MutableStateFlow(0) // 0: Routers, 1: Subnet Radar, 2: Diagnostics
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private var scanJob: Job? = null

    init {
        loadRouters()
        refreshTelemetry()
        checkAll()
    }

    fun setActiveTab(tab: Int) {
        _activeTab.value = tab
    }

    fun setShowAddDialog(show: Boolean) {
        _showAddDialog.value = show
    }

    fun refreshTelemetry() {
        viewModelScope.launch {
            _telemetry.value = NetworkTelemetryHelper.getTelemetry(getApplication())
        }
    }

    fun loadRouters() {
        val routers = repository.getRouters()
        _states.value = routers.map { RouterState(entry = it) }
    }

    fun addRouter(label: String, ip: String) {
        val updated = repository.addRouter(label, ip)
        _states.update { oldList ->
            updated.map { entry ->
                oldList.find { it.entry.id == entry.id } ?: RouterState(entry = entry)
            }
        }
        _showAddDialog.value = false
        // Automatically check the newly added router
        val newEntry = updated.lastOrNull()
        if (newEntry != null) {
            checkSingle(newEntry)
        }
    }

    fun deleteRouter(id: String) {
        val updated = repository.removeRouter(id)
        _states.update { oldList ->
            oldList.filter { it.entry.id != id }
        }
    }

    fun checkAll() {
        if (_isCheckingAll.value) return
        viewModelScope.launch {
            _isCheckingAll.value = true
            refreshTelemetry()

            _states.update { list ->
                list.map { it.copy(status = RouterStatus.Checking) }
            }

            val deferredChecks = _states.value.map { routerState ->
                async {
                    val status = ConnectivityChecker.checkRouter(routerState.entry.ip)
                    routerState.entry.id to status
                }
            }

            val results = deferredChecks.awaitAll().toMap()

            _states.update { list ->
                list.map { routerState ->
                    val status = results[routerState.entry.id] ?: routerState.status
                    routerState.copy(
                        status = status,
                        lastCheckedTimestamp = System.currentTimeMillis()
                    )
                }
            }
            _isCheckingAll.value = false
        }
    }

    fun checkSingle(entry: RouterEntry) {
        viewModelScope.launch {
            _states.update { list ->
                list.map {
                    if (it.entry.id == entry.id) it.copy(status = RouterStatus.Checking) else it
                }
            }

            val newStatus = ConnectivityChecker.checkRouter(entry.ip)

            _states.update { list ->
                list.map {
                    if (it.entry.id == entry.id) it.copy(
                        status = newStatus,
                        lastCheckedTimestamp = System.currentTimeMillis()
                    ) else it
                }
            }
        }
    }

    fun runSpeedTest(entry: RouterEntry) {
        viewModelScope.launch {
            _states.update { list ->
                list.map {
                    if (it.entry.id == entry.id) it.copy(
                        speedStatus = SpeedTestStatus.Testing("Initializing...", 0f, 0.0)
                    ) else it
                }
            }

            val finalResult = SpeedTestEngine.executeSpeedTest(entry.ip) { stage, progress, currentMbps ->
                _states.update { list ->
                    list.map {
                        if (it.entry.id == entry.id) it.copy(
                            speedStatus = SpeedTestStatus.Testing(stage, progress, currentMbps)
                        ) else it
                    }
                }
            }

            _states.update { list ->
                list.map {
                    if (it.entry.id == entry.id) it.copy(speedStatus = finalResult) else it
                }
            }
        }
    }

    fun startSubnetScan() {
        if (_isScanningSubnet.value) {
            scanJob?.cancel()
            _isScanningSubnet.value = false
            return
        }

        scanJob = viewModelScope.launch {
            _isScanningSubnet.value = true
            _discoveredDevices.value = emptyList()
            _scanProgress.value = 0f
            val prefix = _telemetry.value.subnetPrefix

            val results = SubnetScanner.scanSubnet(
                subnetPrefix = prefix,
                onDeviceFound = { device ->
                    _discoveredDevices.update { current ->
                        if (current.none { it.ip == device.ip }) {
                            (current + device).sortedBy {
                                it.ip.substringAfterLast(".").toIntOrNull() ?: 999
                            }
                        } else current
                    }
                },
                onProgress = { progress, currentIp ->
                    _scanProgress.value = progress
                    _scanCurrentIp.value = currentIp
                }
            )

            _discoveredDevices.value = results
            _isScanningSubnet.value = false
        }
    }
}
