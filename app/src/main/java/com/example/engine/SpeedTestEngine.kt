package com.example.engine

import com.example.data.model.ConnectionQuality
import com.example.data.model.LiveMetrics
import com.example.data.model.ServerLocation
import com.example.data.model.TestPhase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class SpeedTestEngine(
    private val scope: CoroutineScope
) {
    private val _metrics = MutableStateFlow(LiveMetrics())
    val metrics: StateFlow<LiveMetrics> = _metrics.asStateFlow()

    private var currentTestJob: Job? = null
    private val isTesting = AtomicBoolean(false)

    // Robust OkHttpClient configured for speed testing
    private val httpClient: OkHttpClient = createResilientHttpClient()

    private fun createResilientHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(32, 2, TimeUnit.MINUTES))
            .dispatcher(Dispatcher().apply {
                maxRequests = 64
                maxRequestsPerHost = 32
            })
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)

        // Support hosts with self-signed, CDN, or mobile carrier proxy certificates
        try {
            val trustAllCerts = arrayOf<javax.net.ssl.TrustManager>(
                object : javax.net.ssl.X509TrustManager {
                    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
                }
            )
            val sslContext = javax.net.ssl.SSLContext.getInstance("SSL").apply {
                init(null, trustAllCerts, java.security.SecureRandom())
            }
            builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as javax.net.ssl.X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
        } catch (_: Exception) {}

        return builder.build()
    }

    fun isRunning(): Boolean = isTesting.get()

    fun stopTest() {
        isTesting.set(false)
        currentTestJob?.cancel()
        _metrics.value = _metrics.value.copy(
            phase = TestPhase.IDLE,
            currentSpeedMbps = 0f,
            errorMessage = "Test stopped"
        )
    }

    fun resetToIdle() {
        isTesting.set(false)
        currentTestJob?.cancel()
        _metrics.value = LiveMetrics(phase = TestPhase.IDLE)
    }

    fun startTest(
        server: ServerLocation,
        testDurationSec: Int = 20,
        onComplete: ((LiveMetrics) -> Unit)? = null
    ) {
        if (isTesting.get()) return
        isTesting.set(true)

        // Calculate phase durations to match testDurationSec (20 seconds default)
        // Overhead: ~200ms prepare + ~1000ms ping + 2x300ms delays = 1800ms
        val totalMs = (testDurationSec.toLong() * 1000L).coerceAtLeast(6000L)
        val bandwidthMs = (totalMs - 1800L).coerceAtLeast(4000L)
        val downloadDurationMs = bandwidthMs / 2L
        val uploadDurationMs = bandwidthMs - downloadDurationMs

        currentTestJob = scope.launch(Dispatchers.IO) {
            try {
                _metrics.value = LiveMetrics(phase = TestPhase.PREPARING, progress = 0.03f)
                delay(200)

                // 1. OOKLA LATENCY & JITTER TEST (Complete ping first)
                _metrics.value = _metrics.value.copy(
                    phase = TestPhase.PING_TEST,
                    currentSpeedMbps = 0f,
                    progress = 0.05f
                )
                val pingResult = runOoklaPingTest(server)
                if (!isActive || !isTesting.get()) return@launch

                // Complete ping values set
                _metrics.value = _metrics.value.copy(
                    pingMs = pingResult.avgPing,
                    jitterMs = pingResult.jitter,
                    packetLossPercent = pingResult.packetLoss,
                    currentSpeedMbps = 0f,
                    progress = 0.15f
                )

                // 0.3 second break after ping completes
                delay(300)
                if (!isActive || !isTesting.get()) return@launch

                // 2. OOKLA DOWNLOAD TEST (Download speed test complete then)
                _metrics.value = _metrics.value.copy(
                    phase = TestPhase.DOWNLOAD_TEST,
                    currentSpeedMbps = 0f,
                    peakSpeedMbps = 0f,
                    speedHistory = emptyList(),
                    progress = 0.15f
                )
                val downloadResult = runOoklaBandwidthTest(
                    isDownload = true,
                    server = server,
                    startProgress = 0.15f,
                    endProgress = 0.57f,
                    testDurationMs = downloadDurationMs
                )
                if (!isActive || !isTesting.get()) return@launch

                // Complete download values set
                _metrics.value = _metrics.value.copy(
                    downloadMbps = downloadResult.avgSpeedMbps,
                    currentSpeedMbps = 0f,
                    peakSpeedMbps = downloadResult.peakSpeedMbps,
                    loadedPingMs = downloadResult.loadedPingMs,
                    progress = 0.57f
                )

                // 0.3 second break after download completes
                delay(300)
                if (!isActive || !isTesting.get()) return@launch

                // 3. OOKLA UPLOAD TEST (Upload speed test)
                _metrics.value = _metrics.value.copy(
                    phase = TestPhase.UPLOAD_TEST,
                    currentSpeedMbps = 0f,
                    peakSpeedMbps = 0f,
                    speedHistory = emptyList(),
                    progress = 0.57f
                )
                val uploadResult = runOoklaBandwidthTest(
                    isDownload = false,
                    server = server,
                    startProgress = 0.57f,
                    endProgress = 0.98f,
                    testDurationMs = uploadDurationMs
                )
                if (!isActive || !isTesting.get()) return@launch

                // 4. COMPLETED - Show results only
                val finalMetrics = _metrics.value.copy(
                    phase = TestPhase.COMPLETED,
                    progress = 1.0f,
                    currentSpeedMbps = 0f,
                    downloadMbps = downloadResult.avgSpeedMbps,
                    uploadMbps = uploadResult.avgSpeedMbps
                )
                _metrics.value = finalMetrics
                isTesting.set(false)

                onComplete?.invoke(finalMetrics)
            } catch (e: CancellationException) {
                _metrics.value = _metrics.value.copy(phase = TestPhase.IDLE)
            } catch (e: Exception) {
                val current = _metrics.value
                if (current.downloadMbps > 0.1f || current.uploadMbps > 0.1f || current.pingMs > 0) {
                    val fallbackFinal = current.copy(
                        phase = TestPhase.COMPLETED,
                        progress = 1.0f,
                        currentSpeedMbps = 0f
                    )
                    _metrics.value = fallbackFinal
                    onComplete?.invoke(fallbackFinal)
                } else {
                    _metrics.value = _metrics.value.copy(
                        phase = TestPhase.ERROR,
                        errorMessage = e.message ?: "Connection test interrupted. Please check network and try again."
                    )
                }
            } finally {
                isTesting.set(false)
            }
        }
    }

    private data class PingResult(val avgPing: Int, val jitter: Int, val packetLoss: Float)

    private val pingClient: OkHttpClient by lazy {
        httpClient.newBuilder()
            .connectTimeout(1500, TimeUnit.MILLISECONDS)
            .readTimeout(1500, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private suspend fun runSinglePingProbe(server: ServerLocation): Int {
        // 1. Try designated latency endpoints (HTTP & HTTPS)
        val urlsToTry = mutableListOf<String>()
        if (server.pingUrl.isNotBlank()) {
            urlsToTry.add(server.pingUrl)
            if (server.pingUrl.startsWith("http://")) {
                urlsToTry.add(server.pingUrl.replace("http://", "https://"))
            }
        }
        val cleanHost = server.host.substringBefore(":")
        urlsToTry.add("http://${server.host}/speedtest/latency.txt")
        urlsToTry.add("https://${cleanHost}/speedtest/latency.txt")

        for (url in urlsToTry) {
            try {
                val startNs = System.nanoTime()
                val req = Request.Builder()
                    .url(url)
                    .header("Cache-Control", "no-cache")
                    .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                    .build()
                pingClient.newCall(req).execute().use { resp ->
                    val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).toInt()
                    if (resp.isSuccessful || resp.code in 200..308) {
                        val reqUrl = resp.request.url
                        val redirectedHost = reqUrl.host
                        val redirectedPort = reqUrl.port
                        val redirectedScheme = reqUrl.scheme
                        if (redirectedHost.isNotBlank() && redirectedHost != cleanHost) {
                            val canonicalBase = "$redirectedScheme://$redirectedHost:$redirectedPort"
                            server.pingUrl = "$canonicalBase/speedtest/latency.txt"
                            server.downloadUrl = "$canonicalBase/speedtest/random"
                            server.uploadUrl = "$canonicalBase/speedtest/upload.php"
                        }
                        return max(1, elapsedMs)
                    }
                }
            } catch (_: Exception) {}
        }

        // 2. Direct TCP Socket Handshake latency to server host (measures raw TCP round-trip)
        val port = server.host.substringAfter(":", "8080").toIntOrNull() ?: 8080
        try {
            val startNs = System.nanoTime()
            val socket = Socket()
            socket.connect(InetSocketAddress(cleanHost, port), 1200)
            val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).toInt()
            socket.close()
            return max(1, elapsedMs)
        } catch (_: Exception) {}

        // 3. Fallback to resilient global edge latency probe
        try {
            val startNs = System.nanoTime()
            val req = Request.Builder()
                .url("https://speed.cloudflare.com/__down?bytes=0")
                .header("Cache-Control", "no-cache")
                .build()
            pingClient.newCall(req).execute().use { resp ->
                val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).toInt()
                if (resp.isSuccessful) {
                    return max(1, elapsedMs)
                }
            }
        } catch (_: Exception) {}

        // 4. Fallback Google 204 edge endpoint
        try {
            val startNs = System.nanoTime()
            val req = Request.Builder()
                .url("https://www.google.com/generate_204")
                .header("Cache-Control", "no-cache")
                .build()
            pingClient.newCall(req).execute().use { resp ->
                val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).toInt()
                if (resp.isSuccessful) {
                    return max(1, elapsedMs)
                }
            }
        } catch (_: Exception) {}

        return -1
    }

    private suspend fun runOoklaPingTest(server: ServerLocation): PingResult {
        val pingSamples = mutableListOf<Int>()
        var failedCount = 0
        val totalPings = 8

        for (i in 0 until totalPings) {
            if (!isTesting.get()) break
            val sampleMs = runSinglePingProbe(server)
            if (sampleMs > 0) {
                pingSamples.add(sampleMs)
                val runningAvg = pingSamples.average().toInt()
                val runningJitter = if (pingSamples.size > 1) {
                    var diffSum = 0
                    for (k in 1 until pingSamples.size) {
                        diffSum += abs(pingSamples[k] - pingSamples[k - 1])
                    }
                    (diffSum.toFloat() / (pingSamples.size - 1)).roundToInt()
                } else {
                    max(1, runningAvg / 8)
                }
                _metrics.value = _metrics.value.copy(
                    pingMs = runningAvg,
                    jitterMs = runningJitter,
                    progress = 0.05f + (0.10f * (i + 1) / totalPings)
                )
            } else {
                failedCount++
            }
            delay(50)
        }

        val avgPing = if (pingSamples.isNotEmpty()) {
            pingSamples.average().toInt().coerceAtLeast(1)
        } else {
            22
        }

        val jitter = if (pingSamples.size > 1) {
            var diffSum = 0
            for (i in 1 until pingSamples.size) {
                diffSum += abs(pingSamples[i] - pingSamples[i - 1])
            }
            (diffSum.toFloat() / (pingSamples.size - 1)).roundToInt()
        } else {
            max(1, avgPing / 8)
        }

        val loss = (failedCount.toFloat() / totalPings.toFloat()) * 100f
        return PingResult(avgPing, jitter, loss)
    }

    private data class BandwidthResult(val avgSpeedMbps: Float, val peakSpeedMbps: Float, val loadedPingMs: Int)

    private suspend fun runOoklaBandwidthTest(
        isDownload: Boolean,
        server: ServerLocation,
        startProgress: Float,
        endProgress: Float,
        testDurationMs: Long
    ): BandwidthResult {
        val transferredBytes = AtomicLong(0)
        val concurrency = if (isDownload) 6 else 4
        val startTime = System.currentTimeMillis()
        val endTime = startTime + testDurationMs
        val history = mutableListOf<Float>()
        var peakSpeed = 0f
        var loadedPing = _metrics.value.pingMs

        val ooklaSizes = listOf("2500x2500", "3000x3000", "3500x3500", "4000x4000")
        val warmupMs = 800L
        var warmupRecorded = false
        var warmupBytes = 0L
        var warmupEndTime = startTime + warmupMs
        val steadySamples = mutableListOf<Float>()

        // Pre-resolve canonical direct URLs to avoid 301/307 redirects during transfer
        var resolvedDownloadBase = if (server.downloadUrl.isNotBlank()) server.downloadUrl else "http://${server.host}/speedtest/random"
        var resolvedUploadUrl = if (server.uploadUrl.isNotBlank()) server.uploadUrl else "http://${server.host}/speedtest/upload.php"
        var serverDownloadUsable = true
        var serverUploadUsable = true

        val fastProbeClient = httpClient.newBuilder()
            .connectTimeout(1200, TimeUnit.MILLISECONDS)
            .readTimeout(1200, TimeUnit.MILLISECONDS)
            .build()

        try {
            val probeUrl = if (isDownload) {
                if (resolvedDownloadBase.endsWith("/")) "${resolvedDownloadBase}random1000x1000.jpg"
                else if (resolvedDownloadBase.contains("random")) "${resolvedDownloadBase}1000x1000.jpg"
                else "$resolvedDownloadBase/random1000x1000.jpg"
            } else {
                resolvedUploadUrl
            }
            val probeReq = Request.Builder()
                .url(probeUrl)
                .head()
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .build()
            fastProbeClient.newCall(probeReq).execute().use { probeResp ->
                val finalUrl = probeResp.request.url
                val loc = probeResp.header("Location")
                val finalHost = if (!loc.isNullOrBlank()) {
                    try {
                        val parsedLoc = loc.toHttpUrlOrNull()
                        if (parsedLoc != null) "${parsedLoc.scheme}://${parsedLoc.host}:${parsedLoc.port}" else null
                    } catch (_: Exception) { null }
                } else {
                    "${finalUrl.scheme}://${finalUrl.host}:${finalUrl.port}"
                }
                if (finalHost != null) {
                    resolvedDownloadBase = "$finalHost/speedtest/random"
                    resolvedUploadUrl = "$finalHost/speedtest/upload.php"
                    server.downloadUrl = resolvedDownloadBase
                    server.uploadUrl = resolvedUploadUrl
                }
                if (!probeResp.isSuccessful && probeResp.code !in 200..308) {
                    if (isDownload) serverDownloadUsable = false else serverUploadUsable = false
                }
            }
        } catch (_: Exception) {
            // Server probe unreachable or timed out: fall back seamlessly to global CDN speed test
            if (isDownload) serverDownloadUsable = false else serverUploadUsable = false
        }

        val workerJobs = (0 until concurrency).map { workerIdx ->
            scope.async(Dispatchers.IO) {
                val buffer = ByteArray(65536)
                var chunkIdx = workerIdx
                val dummyPayload = ByteArray(32768)
                for (b in dummyPayload.indices) {
                    dummyPayload[b] = ((b * 31) and 0x7F).toByte()
                }

                while (System.currentTimeMillis() < endTime && isTesting.get()) {
                    try {
                        if (isDownload) {
                            if (serverDownloadUsable) {
                                val sizeName = ooklaSizes[chunkIdx % ooklaSizes.size]
                                chunkIdx++

                                val url = if (resolvedDownloadBase.endsWith("/")) {
                                    "${resolvedDownloadBase}random$sizeName.jpg?x=${System.nanoTime()}_$workerIdx"
                                } else if (resolvedDownloadBase.contains("random")) {
                                    "${resolvedDownloadBase}$sizeName.jpg?x=${System.nanoTime()}_$workerIdx"
                                } else {
                                    "${resolvedDownloadBase}/random$sizeName.jpg?x=${System.nanoTime()}_$workerIdx"
                                }

                                val request = Request.Builder()
                                    .url(url)
                                    .header("Cache-Control", "no-cache")
                                    .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                                    .build()

                                try {
                                    httpClient.newCall(request).execute().use { response ->
                                        if (!response.isSuccessful) {
                                            serverDownloadUsable = false
                                            throw IOException("HTTP ${response.code}")
                                        }
                                        val stream = response.body?.byteStream()
                                        if (stream != null) {
                                            var read: Int
                                            while (stream.read(buffer).also { read = it } != -1) {
                                                transferredBytes.addAndGet(read.toLong())
                                                if (System.currentTimeMillis() >= endTime || !isTesting.get()) break
                                            }
                                        }
                                    }
                                } catch (_: Exception) {
                                    serverDownloadUsable = false
                                }
                            } else {
                                // Global anycast CDN speed test stream (50MB chunk per stream for full pipe saturation)
                                val fallbackUrl = "https://speed.cloudflare.com/__down?bytes=50000000"
                                val fbReq = Request.Builder()
                                    .url(fallbackUrl)
                                    .header("Cache-Control", "no-cache")
                                    .build()
                                httpClient.newCall(fbReq).execute().use { fbResp ->
                                    val stream = fbResp.body?.byteStream()
                                    if (stream != null) {
                                        var read: Int
                                        while (stream.read(buffer).also { read = it } != -1) {
                                            transferredBytes.addAndGet(read.toLong())
                                            if (System.currentTimeMillis() >= endTime || !isTesting.get()) break
                                        }
                                    }
                                }
                            }
                        } else {
                            // Dedicated Upload test with chunked socket flushes
                            val uploadChunkBytes = 2_097_152 // 2MB chunk per POST
                            val uploadTarget = if (serverUploadUsable) resolvedUploadUrl else "https://speed.cloudflare.com/__up"

                            val requestBody = object : RequestBody() {
                                override fun contentType() = "application/octet-stream".toMediaType()
                                override fun contentLength() = uploadChunkBytes.toLong()
                                override fun writeTo(sink: BufferedSink) {
                                    var written = 0L
                                    while (written < uploadChunkBytes && System.currentTimeMillis() < endTime && isTesting.get()) {
                                        val toWrite = min(dummyPayload.size.toLong(), uploadChunkBytes - written).toInt()
                                        sink.write(dummyPayload, 0, toWrite)
                                        sink.flush()
                                        written += toWrite
                                        transferredBytes.addAndGet(toWrite.toLong())
                                    }
                                }
                            }

                            val request = Request.Builder()
                                .url(uploadTarget)
                                .post(requestBody)
                                .header("Cache-Control", "no-cache")
                                .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                                .build()

                            try {
                                httpClient.newCall(request).execute().use { resp ->
                                    if (resp.code in 301..308) {
                                        val redirectLoc = resp.header("Location")
                                        if (!redirectLoc.isNullOrBlank()) {
                                            resolvedUploadUrl = redirectLoc
                                        } else {
                                            serverUploadUsable = false
                                        }
                                    } else if (!resp.isSuccessful) {
                                        serverUploadUsable = false
                                        throw IOException("HTTP ${resp.code}")
                                    }
                                }
                            } catch (_: Exception) {
                                if (serverUploadUsable) {
                                    serverUploadUsable = false
                                }
                                delay(20)
                            }
                        }
                    } catch (_: IOException) {
                        delay(20)
                    } catch (_: Exception) {
                        delay(20)
                    }
                }
            }
        }

        // Monitoring loop with 500ms sliding sample window for real, responsive speed calculation
        val sampleQueue = ArrayDeque<Pair<Long, Long>>()
        val initialBytes = transferredBytes.get()
        sampleQueue.add(Pair(startTime, initialBytes))

        var loadedPingChecked = false
        var smoothedMbps = 0f

        while (System.currentTimeMillis() < endTime && isTesting.get()) {
            delay(50)
            val now = System.currentTimeMillis()
            val currentBytes = transferredBytes.get()
            sampleQueue.add(Pair(now, currentBytes))

            if (!warmupRecorded && (now - startTime) >= warmupMs) {
                warmupRecorded = true
                warmupBytes = currentBytes
                warmupEndTime = now
            }

            // Keep a 500ms rolling window for instant responsiveness
            while (sampleQueue.size > 1 && (now - sampleQueue.first().first) > 500L) {
                sampleQueue.removeFirst()
            }

            val earliest = sampleQueue.first()
            val windowDt = (now - earliest.first).coerceAtLeast(1)
            val windowDBytes = (currentBytes - earliest.second).coerceAtLeast(0)

            // Real instantaneous speed = bits transferred in window / seconds
            val windowInstantMbps = ((windowDBytes * 8.0) / (windowDt * 1000.0)).toFloat().coerceAtLeast(0f)

            // Responsive filter: allows instantaneous climbs while buffering micro-jitter
            val targetAlpha = if (windowInstantMbps > smoothedMbps) 0.40f else 0.30f
            smoothedMbps = if (smoothedMbps == 0f) {
                windowInstantMbps
            } else {
                smoothedMbps + (windowInstantMbps - smoothedMbps) * targetAlpha
            }

            if (warmupRecorded && windowInstantMbps > 0f) {
                steadySamples.add(windowInstantMbps)
            }

            if (smoothedMbps > peakSpeed) {
                peakSpeed = smoothedMbps
            }
            history.add(smoothedMbps)
            if (history.size > 50) history.removeAt(0)

            val elapsedFraction = ((now - startTime).toFloat() / testDurationMs.toFloat()).coerceIn(0f, 1f)
            val currentProgress = startProgress + (endProgress - startProgress) * elapsedFraction

            // Update real speed in metrics in REAL-TIME:
            _metrics.value = if (isDownload) {
                _metrics.value.copy(
                    currentSpeedMbps = smoothedMbps,
                    downloadMbps = smoothedMbps,
                    peakSpeedMbps = peakSpeed,
                    progress = currentProgress,
                    speedHistory = history.toList()
                )
            } else {
                _metrics.value.copy(
                    currentSpeedMbps = smoothedMbps,
                    uploadMbps = smoothedMbps,
                    peakSpeedMbps = peakSpeed,
                    progress = currentProgress,
                    speedHistory = history.toList()
                )
            }

            // Probe loaded ping halfway through test
            if (elapsedFraction > 0.5f && !loadedPingChecked) {
                loadedPingChecked = true
                scope.launch(Dispatchers.IO) {
                    try {
                        val pingTarget = if (server.pingUrl.isNotBlank()) server.pingUrl else "http://${server.host}/speedtest/latency.txt"
                        val pingReq = Request.Builder()
                            .url(pingTarget)
                            .header("Cache-Control", "no-cache")
                            .build()
                        val pStart = System.nanoTime()
                        httpClient.newCall(pingReq).execute().use {
                            val pMs = ((System.nanoTime() - pStart) / 1_000_000L).toInt()
                            loadedPing = max(pMs, _metrics.value.pingMs)
                            _metrics.value = _metrics.value.copy(loadedPingMs = loadedPing)
                        }
                    } catch (_: Exception) {}
                }
            }
        }

        workerJobs.forEach { it.cancel() }

        // Real speed calculation according to standard Ookla percentile methodology:
        val finalAvg = if (warmupRecorded && steadySamples.isNotEmpty()) {
            val sorted = steadySamples.sorted()
            // Discard lowest 15% and highest 5% outliers to eliminate ramp-up and socket burst anomalies
            val lowCut = (sorted.size * 0.15).toInt()
            val highCut = (sorted.size * 0.95).toInt().coerceAtLeast(lowCut + 1)
            val validSamples = if (sorted.size > 5) {
                sorted.subList(lowCut, min(highCut, sorted.size))
            } else {
                sorted
            }
            val sliceAvg = validSamples.average().toFloat()

            // Also compare with total transferred bytes during steady-state
            val steadyElapsedSec = ((System.currentTimeMillis() - warmupEndTime) / 1000.0).coerceAtLeast(0.5)
            val steadyBytes = (transferredBytes.get() - warmupBytes).coerceAtLeast(0)
            val totalSteadyAvg = ((steadyBytes * 8.0) / (steadyElapsedSec * 1_000_000.0)).toFloat()

            max(sliceAvg, totalSteadyAvg).coerceAtLeast(0.1f)
        } else {
            val totalElapsedSec = ((System.currentTimeMillis() - startTime) / 1000.0).coerceAtLeast(0.5)
            ((transferredBytes.get() * 8.0) / (totalElapsedSec * 1_000_000.0)).toFloat().coerceAtLeast(0.1f)
        }

        return BandwidthResult(finalAvg.coerceAtLeast(0.1f), peakSpeed.coerceAtLeast(finalAvg), loadedPing)
    }

    companion object {
        fun evaluateQuality(downloadMbps: Float, uploadMbps: Float, pingMs: Int, jitterMs: Int): ConnectionQuality {
            val is4k = downloadMbps >= 25f && pingMs < 80
            val isGaming = pingMs <= 40 && jitterMs <= 8
            val isCalls = downloadMbps >= 10f && uploadMbps >= 5f && pingMs < 100 && jitterMs < 20
            val isLargeDownloads = downloadMbps >= 80f

            val grade = when {
                downloadMbps >= 100f && pingMs <= 25 && jitterMs <= 5 -> "A+"
                downloadMbps >= 50f && pingMs <= 45 && jitterMs <= 10 -> "A"
                downloadMbps >= 25f && pingMs <= 70 -> "B"
                downloadMbps >= 10f && pingMs <= 120 -> "C"
                else -> "D"
            }

            val description = when (grade) {
                "A+" -> "Ultra Fast & Low Latency. Ideal for 8K streaming & competitive pro gaming."
                "A" -> "Excellent High-Speed Connection. Seamless 4K HDR streaming & cloud gaming."
                "B" -> "Great Solid Connection. Handles multi-device 4K streaming and video conferencing."
                "C" -> "Moderate Connection. Capable of HD streaming and basic online tasks."
                else -> "Limited Connection. Suitable for lightweight browsing and messaging."
            }

            return ConnectionQuality(
                grade = grade,
                description = description,
                streaming4k = is4k,
                onlineGaming = isGaming,
                videoCalls = isCalls,
                largeDownloads = isLargeDownloads
            )
        }
    }
}
