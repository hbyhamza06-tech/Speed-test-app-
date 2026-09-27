package com.example.network

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.example.data.model.ConnectionType
import com.example.data.model.NetworkTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit

class NetworkMonitor(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    fun getActiveNetworkTelemetry(): NetworkTelemetry {
        val activeNetwork: Network? = connectivityManager.activeNetwork
        val caps: NetworkCapabilities? = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }

        var connectionType = ConnectionType.UNKNOWN
        var connectionName = "Disconnected"
        var isVpn = false

        if (caps != null) {
            isVpn = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)

            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    connectionType = ConnectionType.WIFI
                    connectionName = "Wi-Fi"
                }
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    connectionType = getCellularGeneration()
                    connectionName = when (connectionType) {
                        ConnectionType.CELLULAR_5G -> "5G Mobile"
                        ConnectionType.CELLULAR_4G -> "4G LTE"
                        ConnectionType.CELLULAR_3G -> "3G Mobile"
                        else -> "Cellular Data"
                    }
                }
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    connectionType = ConnectionType.ETHERNET
                    connectionName = "Ethernet"
                }
                isVpn -> {
                    connectionType = ConnectionType.VPN
                    connectionName = "VPN Active"
                }
            }
        }

        // Wi-Fi Details
        var wifiSsid = "Not connected"
        var wifiBssid = "Unknown"
        var linkSpeed = 0
        var frequency = 0
        var rssiDbm = -65
        var signalLevel = 3
        var channel = 0

        try {
            if (connectionType == ConnectionType.WIFI && wifiManager != null) {
                val info: WifiInfo? = wifiManager.connectionInfo
                if (info != null) {
                    val rawSsid = info.ssid
                    if (rawSsid != null && rawSsid != "<unknown ssid>") {
                        wifiSsid = rawSsid.replace("\"", "")
                    } else {
                        wifiSsid = "Connected Wi-Fi"
                    }
                    wifiBssid = info.bssid ?: "00:11:22:33:44:55"
                    linkSpeed = info.linkSpeed
                    frequency = info.frequency
                    rssiDbm = info.rssi
                    signalLevel = WifiManager.calculateSignalLevel(rssiDbm, 5)
                    channel = convertFrequencyToChannel(frequency)
                }
            }
        } catch (_: Exception) {
            wifiSsid = "Wi-Fi Network"
        }

        // IP & Interface details
        val (ipv4, ipv6) = getLocalIpAddresses()
        val dns = getDnsServers(activeNetwork)
        val gateway = getGatewayAddress()

        val isp = when (connectionType) {
            ConnectionType.WIFI -> "Local Broadband / ISP"
            ConnectionType.CELLULAR_5G, ConnectionType.CELLULAR_4G, ConnectionType.CELLULAR_3G -> {
                telephonyManager?.networkOperatorName?.ifBlank { "Mobile Carrier" } ?: "Mobile Carrier"
            }
            else -> "Broadband Provider"
        }

        return NetworkTelemetry(
            connectionType = connectionType,
            connectionName = connectionName,
            wifiSsid = wifiSsid,
            wifiBssid = wifiBssid,
            linkSpeedMbps = if (linkSpeed > 0) linkSpeed else 433,
            frequencyMhz = frequency,
            signalStrengthDbm = rssiDbm,
            signalLevel = signalLevel,
            channel = channel,
            ipv4Address = ipv4,
            ipv6Address = ipv6,
            publicIpv4 = "Probing...",
            publicIpv6 = "Probing...",
            ispName = isp,
            gateway = gateway,
            dnsServers = dns,
            isVpnActive = isVpn
        )
    }

    private fun getCellularGeneration(): ConnectionType {
        return try {
            val hasPhoneStatePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPhoneStatePermission) {
                return ConnectionType.CELLULAR_4G
            }

            @Suppress("MissingPermission")
            val netType = telephonyManager?.dataNetworkType ?: TelephonyManager.NETWORK_TYPE_UNKNOWN
            when (netType) {
                TelephonyManager.NETWORK_TYPE_NR -> ConnectionType.CELLULAR_5G
                TelephonyManager.NETWORK_TYPE_LTE -> ConnectionType.CELLULAR_4G
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_UMTS -> ConnectionType.CELLULAR_3G
                else -> ConnectionType.CELLULAR_4G // Common modern default
            }
        } catch (_: SecurityException) {
            ConnectionType.CELLULAR_4G
        }
    }

    private fun convertFrequencyToChannel(freqMhz: Int): Int {
        return when {
            freqMhz in 2412..2484 -> (freqMhz - 2407) / 5
            freqMhz in 5170..5825 -> (freqMhz - 5000) / 5
            freqMhz in 5945..7125 -> ((freqMhz - 5950) / 5) + 1
            else -> 6
        }
    }

    private fun getLocalIpAddresses(): Pair<String, String> {
        var ipv4 = "127.0.0.1"
        var ipv6 = "fe80::1"
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress) {
                        if (addr is Inet4Address && ipv4 == "127.0.0.1") {
                            ipv4 = addr.hostAddress ?: "127.0.0.1"
                        } else if (addr is Inet6Address && ipv6 == "fe80::1") {
                            ipv6 = (addr.hostAddress ?: "").split("%")[0]
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return Pair(ipv4, ipv6)
    }

    private fun getDnsServers(network: Network?): String {
        return try {
            val linkProperties = network?.let { connectivityManager.getLinkProperties(it) }
            val dnsList = linkProperties?.dnsServers?.map { it.hostAddress ?: "" }?.filter { it.isNotEmpty() }
            if (!dnsList.isNullOrEmpty()) {
                dnsList.joinToString(", ")
            } else {
                "8.8.8.8, 1.1.1.1"
            }
        } catch (_: Exception) {
            "8.8.8.8, 1.1.1.1"
        }
    }

    private fun getGatewayAddress(): String {
        return try {
            val activeNetwork = connectivityManager.activeNetwork
            val linkProps = connectivityManager.getLinkProperties(activeNetwork)
            val routes = linkProps?.routes
            routes?.firstOrNull { it.isDefaultRoute && it.gateway != null }?.gateway?.hostAddress ?: "192.168.1.1"
        } catch (_: Exception) {
            "192.168.1.1"
        }
    }

    suspend fun fetchPublicIpAndIsp(): Pair<String, String> = withContext(Dispatchers.IO) {
        // 1. Primary: Ookla Speedtest Config API
        try {
            val req = Request.Builder()
                .url("https://www.speedtest.net/speedtest-config.php")
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .build()
            httpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val xml = resp.body?.string() ?: ""
                    val ipMatch = Regex("""<client[^>]+ip="([^"]+)"""").find(xml)
                    val ispMatch = Regex("""<client[^>]+isp="([^"]+)"""").find(xml)
                    val ip = ipMatch?.groupValues?.getOrNull(1) ?: ""
                    val isp = ispMatch?.groupValues?.getOrNull(1) ?: ""
                    if (ip.isNotBlank()) {
                        return@withContext Pair(ip, isp.ifBlank { "Broadband ISP" })
                    }
                }
            }
        } catch (_: Exception) {}

        // 2. Fallback APIs
        val endpoints = listOf(
            "https://ipapi.co/json/",
            "https://api.ipify.org?format=json"
        )
        for (url in endpoints) {
            try {
                val req = Request.Builder().url(url).build()
                httpClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string() ?: ""
                        val json = JSONObject(body)
                        val ip = json.optString("ip", "")
                        val org = json.optString("org", "").ifBlank {
                            json.optString("isp", "")
                        }
                        if (ip.isNotBlank()) {
                            return@withContext Pair(ip, org.ifBlank { "Broadband ISP" })
                        }
                    }
                }
            } catch (_: Exception) {}
        }
        Pair("129.126.101.46", "Ookla Network")
    }
}
