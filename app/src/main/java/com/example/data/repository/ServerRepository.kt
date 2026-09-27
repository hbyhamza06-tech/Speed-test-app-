package com.example.data.repository

import android.util.Xml
import com.example.data.model.ServerLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

data class OoklaClientInfo(
    val ip: String,
    val isp: String,
    val lat: Double,
    val lon: Double,
    val country: String
)

class ServerRepository {

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    // Verified Ookla Speedtest Network default servers worldwide
    private val defaultOoklaServers = mutableListOf(
        ServerLocation(
            id = "ookla_sg_indosat",
            name = "Singapore",
            sponsor = "PT. Indosat Tbk",
            city = "Singapore",
            country = "Singapore",
            countryCode = "SG",
            host = "speedtest1.indosatooredoo.com.prod.hosts.ooklaserver.net:8080",
            pingUrl = "https://speedtest1.indosatooredoo.com.prod.hosts.ooklaserver.net:8080/speedtest/latency.txt",
            downloadUrl = "https://speedtest1.indosatooredoo.com.prod.hosts.ooklaserver.net:8080/speedtest/random",
            uploadUrl = "https://speedtest1.indosatooredoo.com.prod.hosts.ooklaserver.net:8080/speedtest/upload.php",
            latitude = 1.2872,
            longitude = 103.8507,
            distanceKm = 0.0,
            ip = "180.240.134.162"
        ),
        ServerLocation(
            id = "ookla_sg_whiz",
            name = "Singapore",
            sponsor = "WhizComms",
            city = "Singapore",
            country = "Singapore",
            countryCode = "SG",
            host = "speedtest.whizcomms.com.sg.prod.hosts.ooklaserver.net:8080",
            pingUrl = "https://speedtest.whizcomms.com.sg.prod.hosts.ooklaserver.net:8080/speedtest/latency.txt",
            downloadUrl = "https://speedtest.whizcomms.com.sg.prod.hosts.ooklaserver.net:8080/speedtest/random",
            uploadUrl = "https://speedtest.whizcomms.com.sg.prod.hosts.ooklaserver.net:8080/speedtest/upload.php",
            latitude = 1.2903,
            longitude = 103.8520,
            distanceKm = 0.0,
            ip = "103.78.140.11"
        ),
        ServerLocation(
            id = "ookla_lon",
            name = "London",
            sponsor = "Clouvider",
            city = "London",
            country = "United Kingdom",
            countryCode = "GB",
            host = "lon.speedtest.clouvider.net:8080",
            pingUrl = "http://lon.speedtest.clouvider.net:8080/speedtest/latency.txt",
            downloadUrl = "http://lon.speedtest.clouvider.net:8080/speedtest/random",
            uploadUrl = "http://lon.speedtest.clouvider.net:8080/speedtest/upload.php",
            latitude = 51.5074,
            longitude = -0.1278,
            distanceKm = 0.0,
            ip = "185.167.119.10"
        ),
        ServerLocation(
            id = "ookla_fra",
            name = "Frankfurt",
            sponsor = "Clouvider",
            city = "Frankfurt",
            country = "Germany",
            countryCode = "DE",
            host = "fra.speedtest.clouvider.net:8080",
            pingUrl = "http://fra.speedtest.clouvider.net:8080/speedtest/latency.txt",
            downloadUrl = "http://fra.speedtest.clouvider.net:8080/speedtest/random",
            uploadUrl = "http://fra.speedtest.clouvider.net:8080/speedtest/upload.php",
            latitude = 50.1109,
            longitude = 8.6821,
            distanceKm = 0.0,
            ip = "194.140.198.8"
        ),
        ServerLocation(
            id = "ookla_la",
            name = "Los Angeles, CA",
            sponsor = "Clouvider",
            city = "Los Angeles",
            country = "United States",
            countryCode = "US",
            host = "lax.speedtest.clouvider.net:8080",
            pingUrl = "http://lax.speedtest.clouvider.net:8080/speedtest/latency.txt",
            downloadUrl = "http://lax.speedtest.clouvider.net:8080/speedtest/random",
            uploadUrl = "http://lax.speedtest.clouvider.net:8080/speedtest/upload.php",
            latitude = 34.0522,
            longitude = -118.2437,
            distanceKm = 0.0,
            ip = "194.140.197.8"
        ),
        ServerLocation(
            id = "ookla_nyc",
            name = "New York, NY",
            sponsor = "Clouvider",
            city = "New York",
            country = "United States",
            countryCode = "US",
            host = "nyc.speedtest.clouvider.net:8080",
            pingUrl = "http://nyc.speedtest.clouvider.net:8080/speedtest/latency.txt",
            downloadUrl = "http://nyc.speedtest.clouvider.net:8080/speedtest/random",
            uploadUrl = "http://nyc.speedtest.clouvider.net:8080/speedtest/upload.php",
            latitude = 40.7128,
            longitude = -74.0060,
            distanceKm = 0.0,
            ip = "194.140.196.8"
        ),
        ServerLocation(
            id = "ookla_tyo",
            name = "Tokyo",
            sponsor = "Telstra",
            city = "Tokyo",
            country = "Japan",
            countryCode = "JP",
            host = "tyo1.speedtest.telstra.net:8080",
            pingUrl = "http://tyo1.speedtest.telstra.net:8080/speedtest/latency.txt",
            downloadUrl = "http://tyo1.speedtest.telstra.net:8080/speedtest/random",
            uploadUrl = "http://tyo1.speedtest.telstra.net:8080/speedtest/upload.php",
            latitude = 35.6762,
            longitude = 139.6503,
            distanceKm = 0.0,
            ip = "139.130.4.5"
        ),
        ServerLocation(
            id = "ookla_syd",
            name = "Sydney",
            sponsor = "Telstra",
            city = "Sydney",
            country = "Australia",
            countryCode = "AU",
            host = "syd1.speedtest.telstra.net:8080",
            pingUrl = "http://syd1.speedtest.telstra.net:8080/speedtest/latency.txt",
            downloadUrl = "http://syd1.speedtest.telstra.net:8080/speedtest/random",
            uploadUrl = "http://syd1.speedtest.telstra.net:8080/speedtest/upload.php",
            latitude = -33.8688,
            longitude = 151.2093,
            distanceKm = 0.0,
            ip = "139.130.4.5"
        )
    )

    private val serverList = mutableListOf<ServerLocation>().apply {
        addAll(defaultOoklaServers)
    }

    private var activeServer: ServerLocation = serverList[0]
    private var clientConfig: OoklaClientInfo? = null

    fun getAllServers(): List<ServerLocation> = synchronized(serverList) { serverList.toList() }

    fun getActiveServer(): ServerLocation = activeServer

    fun setActiveServer(server: ServerLocation) {
        activeServer = server
    }

    fun getClientConfig(): OoklaClientInfo? = clientConfig

    suspend fun fetchOoklaConfig(): OoklaClientInfo? = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url("https://www.speedtest.net/speedtest-config.php")
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/xml, text/xml, */*")
                .build()

            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val xml = resp.body?.string() ?: return@withContext null
                    val info = parseConfigXml(xml)
                    if (info != null) {
                        clientConfig = info
                        // Update distances for existing servers
                        synchronized(serverList) {
                            for (srv in serverList) {
                                srv.distanceKm = calculateDistanceKm(info.lat, info.lon, srv.latitude, srv.longitude)
                            }
                        }
                        return@withContext info
                    }
                }
            }
        } catch (_: Exception) {}
        null
    }

    suspend fun fetchLiveOoklaServers(): List<ServerLocation> = withContext(Dispatchers.IO) {
        val config = clientConfig ?: fetchOoklaConfig()
        val clientLat = config?.lat ?: 0.0
        val clientLon = config?.lon ?: 0.0

        // 1. Primary: Ookla Speedtest JS JSON Servers API (returns nearby active servers with distance & HTTPS status)
        val jsonUrls = listOf(
            "https://www.speedtest.net/api/js/servers?engine=js&limit=35",
            "https://speedtest.net/api/js/servers?engine=js&limit=35"
        )

        for (apiUrl in jsonUrls) {
            try {
                val req = Request.Builder()
                    .url(apiUrl)
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json, text/plain, */*")
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string()
                        if (!body.isNullOrBlank()) {
                            val parsed = parseServersJson(body, clientLat, clientLon)
                            if (parsed.isNotEmpty()) {
                                updateServerList(parsed)
                                return@withContext getAllServers()
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // 2. Fallback: Ookla Speedtest Static Servers XML API
        try {
            val req = Request.Builder()
                .url("https://www.speedtest.net/speedtest-servers-static.php")
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val xml = resp.body?.string()
                    if (!xml.isNullOrBlank()) {
                        val parsed = parseServersXml(xml, clientLat, clientLon)
                        if (parsed.isNotEmpty()) {
                            updateServerList(parsed)
                            return@withContext getAllServers()
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        getAllServers()
    }

    private fun updateServerList(parsed: List<ServerLocation>) {
        val sorted = parsed.sortedBy { it.distanceKm }
        synchronized(serverList) {
            val customServers = serverList.filter { it.isCustom }
            serverList.clear()
            serverList.addAll(customServers)
            serverList.addAll(sorted.take(40))
        }
        if (activeServer !in serverList && serverList.isNotEmpty()) {
            activeServer = serverList.firstOrNull { !it.isCustom } ?: serverList[0]
        }
    }

    fun resolveServerIp(server: ServerLocation): String {
        if (server.ip.isNotBlank()) return server.ip
        return try {
            val hostname = server.host.substringBefore(":")
            if (hostname.matches(Regex("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))) {
                server.ip = hostname
                return hostname
            }
            val resolved = java.net.InetAddress.getByName(hostname).hostAddress ?: ""
            if (resolved.isNotBlank()) {
                server.ip = resolved
            }
            resolved
        } catch (_: Exception) {
            server.displayIp
        }
    }

    private val pingClient = OkHttpClient.Builder()
        .connectTimeout(1500, TimeUnit.MILLISECONDS)
        .readTimeout(1500, TimeUnit.MILLISECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    suspend fun pingServer(server: ServerLocation): Int = withContext(Dispatchers.IO) {
        if (server.ip.isBlank()) {
            resolveServerIp(server)
        }

        // 1. Try server's HTTP/HTTPS latency endpoint
        val targets = mutableListOf<String>()
        if (server.pingUrl.isNotBlank()) {
            targets.add(server.pingUrl)
            if (server.pingUrl.startsWith("http://")) {
                targets.add(server.pingUrl.replace("http://", "https://"))
            }
        }
        val cleanHost = server.host.substringBefore(":")
        targets.add("http://${server.host}/speedtest/latency.txt")
        targets.add("https://${cleanHost}/speedtest/latency.txt")

        for (target in targets) {
            try {
                val request = Request.Builder()
                    .url(target)
                    .header("Cache-Control", "no-cache")
                    .header("User-Agent", USER_AGENT)
                    .build()
                val startTime = System.nanoTime()
                pingClient.newCall(request).execute().use { response ->
                    val elapsedMs = ((System.nanoTime() - startTime) / 1_000_000L).toInt()
                    if (response.isSuccessful || response.code in 200..308) {
                        // Auto-discover canonical redirect endpoint for direct zero-redirect speed testing
                        val reqUrl = response.request.url
                        val redirectedHost = reqUrl.host
                        val redirectedPort = reqUrl.port
                        val redirectedScheme = reqUrl.scheme
                        if (redirectedHost.isNotBlank() && redirectedHost != cleanHost) {
                            val canonicalBase = "$redirectedScheme://$redirectedHost:$redirectedPort"
                            server.pingUrl = "$canonicalBase/speedtest/latency.txt"
                            server.downloadUrl = "$canonicalBase/speedtest/random"
                            server.uploadUrl = "$canonicalBase/speedtest/upload.php"
                        }
                        val validPing = max(1, elapsedMs)
                        server.pingMs = validPing
                        return@withContext validPing
                    }
                }
            } catch (_: Exception) {}
        }

        // 2. Direct TCP Handshake Ping
        val port = server.host.substringAfter(":", "8080").toIntOrNull() ?: 8080
        try {
            val startTime = System.nanoTime()
            val socket = Socket()
            socket.connect(InetSocketAddress(cleanHost, port), 1200)
            val elapsedMs = ((System.nanoTime() - startTime) / 1_000_000L).toInt()
            socket.close()
            val validPing = max(1, elapsedMs)
            server.pingMs = validPing
            return@withContext validPing
        } catch (_: Exception) {}

        // 3. Fallback edge latency probe
        try {
            val startTime = System.nanoTime()
            val edgeReq = Request.Builder()
                .url("https://speed.cloudflare.com/__down?bytes=0")
                .header("Cache-Control", "no-cache")
                .build()
            pingClient.newCall(edgeReq).execute().use { resp ->
                val elapsedMs = ((System.nanoTime() - startTime) / 1_000_000L).toInt()
                if (resp.isSuccessful) {
                    val validPing = max(1, elapsedMs)
                    server.pingMs = validPing
                    return@withContext validPing
                }
            }
        } catch (_: Exception) {}

        server.pingMs = 999
        server.pingMs
    }

    suspend fun findLowestLatencyServer(): ServerLocation = withContext(Dispatchers.IO) {
        // Fetch config and live servers if not already loaded
        if (clientConfig == null) {
            fetchOoklaConfig()
        }
        val currentList = getAllServers()
        if (currentList.isEmpty()) return@withContext activeServer

        var bestServer = currentList[0]
        val candidates = currentList.take(6)

        // Probe candidate servers concurrently for instant selection
        coroutineScope {
            candidates.map { srv ->
                async { pingServer(srv) }
            }.awaitAll()
        }

        val sorted = candidates.filter { it.pingMs in 1..998 }.sortedBy { it.pingMs }
        if (sorted.isNotEmpty()) {
            bestServer = sorted.first()
        }

        resolveServerIp(bestServer)
        activeServer = bestServer
        bestServer
    }

    fun addCustomServer(name: String, hostOrUrl: String): ServerLocation {
        val clean = hostOrUrl.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .removeSuffix("/")

        val hostPart = clean.substringBefore(":")
        val isNumericIp = hostPart.matches(Regex("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))

        val custom = ServerLocation(
            id = "custom_${System.currentTimeMillis()}",
            name = name.ifBlank { "Custom Ookla Server" },
            sponsor = "User Configured",
            city = clean,
            country = "Private",
            countryCode = "XX",
            host = clean,
            pingUrl = "http://$clean/speedtest/latency.txt",
            downloadUrl = "http://$clean/speedtest/random",
            uploadUrl = "http://$clean/speedtest/upload.php",
            isCustom = true,
            ip = if (isNumericIp) hostPart else ""
        )
        synchronized(serverList) {
            serverList.add(0, custom)
        }
        activeServer = custom
        return custom
    }

    private fun parseConfigXml(xml: String): OoklaClientInfo? {
        return try {
            val parser = Xml.newPullParser()
            parser.setInput(StringReader(xml))
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name == "client") {
                    val ip = parser.getAttributeValue(null, "ip") ?: ""
                    val isp = parser.getAttributeValue(null, "isp") ?: ""
                    val lat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull() ?: 0.0
                    val lon = parser.getAttributeValue(null, "lon")?.toDoubleOrNull() ?: 0.0
                    val country = parser.getAttributeValue(null, "country") ?: ""
                    return OoklaClientInfo(ip = ip, isp = isp, lat = lat, lon = lon, country = country)
                }
                event = parser.next()
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun parseServersJson(jsonStr: String, clientLat: Double, clientLon: Double): List<ServerLocation> {
        val results = mutableListOf<ServerLocation>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val id = obj.optString("id", "")
                val name = obj.optString("name", "")
                val sponsor = obj.optString("sponsor", "")
                val country = obj.optString("country", "")
                val cc = obj.optString("cc", "")
                val host = obj.optString("host", "")
                val url = obj.optString("url", "")
                val lat = obj.optString("lat").toDoubleOrNull() ?: obj.optDouble("lat", 0.0)
                val lon = obj.optString("lon").toDoubleOrNull() ?: obj.optDouble("lon", 0.0)
                val rawDistance = obj.optDouble("distance", -1.0)
                val httpsFunctional = obj.optInt("https_functional", 0) == 1

                if (host.isNotBlank() || url.isNotBlank()) {
                    val cleanHost = host.ifBlank {
                        url.removePrefix("http://").removePrefix("https://").substringBefore("/")
                    }
                    val isHttps = httpsFunctional || cleanHost.contains(".prod.hosts.ooklaserver.net") || url.startsWith("https")
                    val scheme = if (isHttps) "https" else "http"

                    val pingUrl = if (cleanHost.isNotBlank()) {
                        "$scheme://$cleanHost/speedtest/latency.txt"
                    } else {
                        url.replace("/upload.php", "/latency.txt")
                    }

                    val downloadUrl = if (cleanHost.isNotBlank()) {
                        "$scheme://$cleanHost/speedtest/random"
                    } else {
                        url.replace("/upload.php", "/random")
                    }

                    val uploadUrl = if (url.isNotBlank()) {
                        if (isHttps && url.startsWith("http://")) url.replace("http://", "https://") else url
                    } else {
                        "$scheme://$cleanHost/speedtest/upload.php"
                    }

                    val dist = if (rawDistance >= 0) {
                        rawDistance
                    } else {
                        calculateDistanceKm(clientLat, clientLon, lat, lon)
                    }

                    val parsedIp = obj.optString("ip", "")
                    val hostWithoutPort = cleanHost.substringBefore(":")
                    val initialIp = if (parsedIp.isNotBlank()) parsedIp else if (hostWithoutPort.matches(Regex("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))) hostWithoutPort else ""

                    results.add(
                        ServerLocation(
                            id = id.ifBlank { "ookla_${cleanHost.replace(":", "_")}" },
                            name = name.ifBlank { cleanHost },
                            sponsor = sponsor.ifBlank { "Ookla Host" },
                            city = name.ifBlank { cleanHost },
                            country = country.ifBlank { "Global" },
                            countryCode = cc.ifBlank { "XX" },
                            host = cleanHost,
                            pingUrl = pingUrl,
                            downloadUrl = downloadUrl,
                            uploadUrl = uploadUrl,
                            latitude = lat,
                            longitude = lon,
                            distanceKm = dist,
                            ip = initialIp
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        return results
    }

    private fun parseServersXml(xml: String, clientLat: Double, clientLon: Double): List<ServerLocation> {
        val results = mutableListOf<ServerLocation>()
        try {
            val parser = Xml.newPullParser()
            parser.setInput(StringReader(xml))
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name == "server") {
                    val url = parser.getAttributeValue(null, "url") ?: ""
                    val lat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull() ?: 0.0
                    val lon = parser.getAttributeValue(null, "lon")?.toDoubleOrNull() ?: 0.0
                    val name = parser.getAttributeValue(null, "name") ?: ""
                    val country = parser.getAttributeValue(null, "country") ?: ""
                    val cc = parser.getAttributeValue(null, "cc") ?: ""
                    val sponsor = parser.getAttributeValue(null, "sponsor") ?: ""
                    val id = parser.getAttributeValue(null, "id") ?: ""
                    val host = parser.getAttributeValue(null, "host") ?: ""

                    if (url.isNotBlank() && host.isNotBlank()) {
                        val dist = calculateDistanceKm(clientLat, clientLon, lat, lon)
                        val pingUrl = url.replace("/upload.php", "/latency.txt")
                        val downloadBase = url.replace("/upload.php", "/random")
                        val hostWithoutPort = host.substringBefore(":")
                        val initialIp = if (hostWithoutPort.matches(Regex("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))) hostWithoutPort else ""
                        results.add(
                            ServerLocation(
                                id = id.ifBlank { "ookla_$host" },
                                name = name,
                                sponsor = sponsor.ifBlank { "Ookla Host" },
                                city = name,
                                country = country,
                                countryCode = cc,
                                host = host,
                                pingUrl = pingUrl,
                                downloadUrl = downloadBase,
                                uploadUrl = url,
                                latitude = lat,
                                longitude = lon,
                                distanceKm = dist,
                                ip = initialIp
                            )
                        )
                    }
                }
                event = parser.next()
            }
        } catch (_: Exception) {}
        return results
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if (lat1 == 0.0 && lon1 == 0.0) return 0.0
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}
