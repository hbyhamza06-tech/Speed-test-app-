package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SpeedTestEntity
import com.example.data.model.ConnectionQuality
import com.example.data.model.LiveMetrics
import com.example.data.model.NetworkTelemetry
import com.example.data.model.ServerLocation
import com.example.data.model.TestPhase
import com.example.data.repository.ServerRepository
import com.example.engine.SpeedTestEngine
import com.example.network.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SpeedTestViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.speedTestDao()
    private val serverRepo = ServerRepository()
    private val networkMonitor = NetworkMonitor(application)
    private val speedTestEngine = SpeedTestEngine(viewModelScope)

    val metrics: StateFlow<LiveMetrics> = speedTestEngine.metrics

    private val _telemetry = MutableStateFlow(NetworkTelemetry())
    val telemetry: StateFlow<NetworkTelemetry> = _telemetry.asStateFlow()

    private val _servers = MutableStateFlow(serverRepo.getAllServers())
    val servers: StateFlow<List<ServerLocation>> = _servers.asStateFlow()

    private val _activeServer = MutableStateFlow(serverRepo.getActiveServer())
    val activeServer: StateFlow<ServerLocation> = _activeServer.asStateFlow()

    private val _lastResult = MutableStateFlow<SpeedTestEntity?>(null)
    val lastResult: StateFlow<SpeedTestEntity?> = _lastResult.asStateFlow()

    private val _isPingingServers = MutableStateFlow(false)
    val isPingingServers: StateFlow<Boolean> = _isPingingServers.asStateFlow()

    private val _showInMBps = MutableStateFlow(false)
    val showInMBps: StateFlow<Boolean> = _showInMBps.asStateFlow()

    private val _testDurationSec = MutableStateFlow(20)
    val testDurationSec: StateFlow<Int> = _testDurationSec.asStateFlow()

    fun toggleUnit() {
        _showInMBps.value = !_showInMBps.value
    }

    fun setTestDuration(seconds: Int) {
        _testDurationSec.value = seconds.coerceIn(10, 60)
    }

    val history: StateFlow<List<SpeedTestEntity>> = dao.getAllResults()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshNetworkInfo()
        loadOoklaServers()
        viewModelScope.launch(Dispatchers.IO) {
            val srv = _activeServer.value
            val ip = serverRepo.resolveServerIp(srv)
            if (ip.isNotBlank()) {
                _activeServer.value = srv.copy(ip = ip)
            }
        }
    }

    fun loadOoklaServers() {
        viewModelScope.launch(Dispatchers.IO) {
            val config = serverRepo.fetchOoklaConfig()
            if (config != null) {
                _telemetry.value = _telemetry.value.copy(
                    publicIpv4 = config.ip,
                    ispName = config.isp
                )
            }
            val liveServers = serverRepo.fetchLiveOoklaServers()
            _servers.value = liveServers
            val best = serverRepo.findLowestLatencyServer()
            serverRepo.resolveServerIp(best)
            _activeServer.value = best
            _servers.value = serverRepo.getAllServers()
        }
    }

    fun refreshNetworkInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val localTelemetry = networkMonitor.getActiveNetworkTelemetry()
            _telemetry.value = localTelemetry

            // Query public IP & ISP asynchronously
            try {
                val (publicIp, isp) = networkMonitor.fetchPublicIpAndIsp()
                _telemetry.value = _telemetry.value.copy(
                    publicIpv4 = publicIp,
                    ispName = isp
                )
            } catch (_: Exception) {}
        }
    }

    fun findNearestServer() {
        viewModelScope.launch(Dispatchers.IO) {
            val best = serverRepo.findLowestLatencyServer()
            serverRepo.resolveServerIp(best)
            _activeServer.value = best
            _servers.value = serverRepo.getAllServers()
        }
    }

    fun pingAllServers() {
        viewModelScope.launch(Dispatchers.IO) {
            _isPingingServers.value = true
            val currentServers = _servers.value
            currentServers.chunked(6).forEach { chunk ->
                chunk.map { srv ->
                    async { serverRepo.pingServer(srv) }
                }.awaitAll()
                _servers.value = serverRepo.getAllServers().sortedBy { if (it.pingMs in 1..998) it.pingMs else 9999 }
            }
            _isPingingServers.value = false
        }
    }

    fun selectServer(server: ServerLocation) {
        serverRepo.setActiveServer(server)
        _activeServer.value = server
        viewModelScope.launch(Dispatchers.IO) {
            val ip = serverRepo.resolveServerIp(server)
            if (ip.isNotBlank()) {
                server.ip = ip
                _activeServer.value = server.copy(ip = ip)
            }
        }
    }

    fun addCustomServer(name: String, hostOrUrl: String) {
        val custom = serverRepo.addCustomServer(name, hostOrUrl)
        _servers.value = serverRepo.getAllServers()
        _activeServer.value = custom
        viewModelScope.launch(Dispatchers.IO) {
            val ip = serverRepo.resolveServerIp(custom)
            if (ip.isNotBlank()) {
                custom.ip = ip
                _activeServer.value = custom.copy(ip = ip)
            }
        }
    }

    private var testStartTime: Long = 0L

    fun startSpeedTest() {
        if (speedTestEngine.isRunning()) return

        testStartTime = System.currentTimeMillis()
        refreshNetworkInfo()

        speedTestEngine.startTest(_activeServer.value, _testDurationSec.value) { finalMetrics ->
            val actualDuration = (((System.currentTimeMillis() - testStartTime) / 1000L).coerceAtLeast(1L)).toInt()
            val quality = SpeedTestEngine.evaluateQuality(
                finalMetrics.downloadMbps,
                finalMetrics.uploadMbps,
                finalMetrics.pingMs,
                finalMetrics.jitterMs
            )

            val currentTel = _telemetry.value
            val currentSrv = _activeServer.value

            val entity = SpeedTestEntity(
                downloadMbps = finalMetrics.downloadMbps,
                uploadMbps = finalMetrics.uploadMbps,
                pingMs = finalMetrics.pingMs,
                jitterMs = finalMetrics.jitterMs,
                packetLossPercent = finalMetrics.packetLossPercent,
                loadedPingMs = finalMetrics.loadedPingMs,
                serverName = currentSrv.name,
                serverLocation = "${currentSrv.city}, ${currentSrv.country}",
                serverHost = if (currentSrv.ip.isNotBlank()) "${currentSrv.host} (${currentSrv.ip})" else currentSrv.host,
                networkType = currentTel.connectionName,
                wifiSsid = currentTel.wifiSsid,
                ispName = currentTel.ispName,
                publicIp = currentTel.publicIpv4,
                testDurationSec = actualDuration,
                ratingGrade = quality.grade
            )

            _lastResult.value = entity

            viewModelScope.launch(Dispatchers.IO) {
                dao.insertResult(entity)
            }
        }
    }

    fun stopSpeedTest() {
        speedTestEngine.stopTest()
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteResult(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearAll()
        }
    }

    fun getQualityRating(download: Float, upload: Float, ping: Int, jitter: Int): ConnectionQuality {
        return SpeedTestEngine.evaluateQuality(download, upload, ping, jitter)
    }

    fun exportHistoryCsv(): String {
        val list = history.value
        val sb = StringBuilder()
        sb.append("Timestamp,Date,Download(Mbps),Upload(Mbps),Ping(ms),Jitter(ms),PacketLoss(%),Server,Network,ISP\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        for (item in list) {
            sb.append("${item.timestamp},")
            sb.append("\"${sdf.format(Date(item.timestamp))}\",")
            sb.append(String.format(Locale.US, "%.2f,", item.downloadMbps))
            sb.append(String.format(Locale.US, "%.2f,", item.uploadMbps))
            sb.append("${item.pingMs},")
            sb.append("${item.jitterMs},")
            sb.append(String.format(Locale.US, "%.1f,", item.packetLossPercent))
            sb.append("\"${item.serverName}\",")
            sb.append("\"${item.networkType}\",")
            sb.append("\"${item.ispName}\"\n")
        }
        return sb.toString()
    }
}
