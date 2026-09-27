package com.example.data.model

enum class TestPhase {
    IDLE,
    PREPARING,
    PING_TEST,
    DOWNLOAD_TEST,
    UPLOAD_TEST,
    COMPLETED,
    ERROR
}

enum class ConnectionType {
    WIFI,
    CELLULAR_5G,
    CELLULAR_4G,
    CELLULAR_3G,
    ETHERNET,
    VPN,
    UNKNOWN
}

data class ServerLocation(
    val id: String,
    val name: String,
    val sponsor: String,
    val city: String,
    val country: String,
    val countryCode: String,
    val host: String,
    var pingUrl: String,
    var downloadUrl: String,
    var uploadUrl: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var distanceKm: Double = 0.0,
    var pingMs: Int = -1,
    var isCustom: Boolean = false,
    var ip: String = ""
) {
    val displayIp: String
        get() = ip.ifBlank {
            val hostPart = host.substringBefore(":")
            if (hostPart.matches(Regex("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))) hostPart else ""
        }
}

data class LiveMetrics(
    val currentSpeedMbps: Float = 0f,
    val peakSpeedMbps: Float = 0f,
    val downloadMbps: Float = 0f,
    val uploadMbps: Float = 0f,
    val pingMs: Int = 0,
    val jitterMs: Int = 0,
    val packetLossPercent: Float = 0f,
    val loadedPingMs: Int = 0,
    val progress: Float = 0f, // 0.0 to 1.0
    val phase: TestPhase = TestPhase.IDLE,
    val speedHistory: List<Float> = emptyList(),
    val errorMessage: String? = null
) {
    val currentSpeedMBps: Float
        get() = currentSpeedMbps / 8f
    val downloadMBps: Float
        get() = downloadMbps / 8f
    val uploadMBps: Float
        get() = uploadMbps / 8f
}

data class ConnectionQuality(
    val grade: String, // A+, A, B, C, D
    val description: String,
    val streaming4k: Boolean,
    val onlineGaming: Boolean,
    val videoCalls: Boolean,
    val largeDownloads: Boolean
)

data class NetworkTelemetry(
    val connectionType: ConnectionType = ConnectionType.UNKNOWN,
    val connectionName: String = "Connecting...",
    val wifiSsid: String = "Unknown SSID",
    val wifiBssid: String = "Unknown",
    val linkSpeedMbps: Int = 0,
    val frequencyMhz: Int = 0,
    val signalStrengthDbm: Int = -65,
    val signalLevel: Int = 3, // 0 to 4
    val channel: Int = 0,
    val ipv4Address: String = "192.168.1.100",
    val ipv6Address: String = "fe80::1",
    val publicIpv4: String = "Checking...",
    val publicIpv6: String = "Not detected",
    val ispName: String = "Detecting ISP...",
    val gateway: String = "192.168.1.1",
    val dnsServers: String = "8.8.8.8, 1.1.1.1",
    val isVpnActive: Boolean = false
)
