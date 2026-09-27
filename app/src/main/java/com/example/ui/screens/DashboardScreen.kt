package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LiveMetrics
import com.example.data.model.NetworkTelemetry
import com.example.data.model.ServerLocation
import com.example.data.model.TestPhase
import com.example.engine.SpeedTestEngine
import com.example.ui.components.OoklaInfoDialog
import com.example.ui.components.ResultShareHelper
import com.example.ui.components.SpeedTestMetricsPanel
import com.example.ui.components.SpeedometerGauge
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBackgroundSecondary
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricSkyBlue
import com.example.ui.theme.PulseEmerald
import com.example.ui.theme.PulsePurple
import com.example.ui.viewmodel.SpeedTestViewModel
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: SpeedTestViewModel,
    onNavigateToServerSelect: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.metrics.collectAsState()
    val activeServer by viewModel.activeServer.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val showInMBps by viewModel.showInMBps.collectAsState()
    val testDurationSec by viewModel.testDurationSec.collectAsState()

    val isTesting = metrics.phase != TestPhase.IDLE &&
            metrics.phase != TestPhase.COMPLETED &&
            metrics.phase != TestPhase.ERROR

    var showInfoDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    if (showInfoDialog) {
        OoklaInfoDialog(onDismiss = { showInfoDialog = false })
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DarkBackground, DarkBackgroundSecondary)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Speedo Net",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp
                        ),
                        color = DarkTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ElectricCyan.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "v${com.example.BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ElectricCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Duration Selector Chip (Default 20s)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .clickable(enabled = !isTesting) {
                                val next = when (testDurationSec) {
                                    20 -> 30
                                    30 -> 10
                                    else -> 20
                                }
                                viewModel.setTestDuration(next)
                            }
                            .padding(end = 8.dp)
                            .testTag("duration_toggle_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏱️ ${testDurationSec}s",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = ElectricCyan
                            )
                        }
                    }

                    // Unit Toggle Chip (Mbps vs MB/s)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (showInMBps) PulseEmerald.copy(alpha = 0.6f) else ElectricSkyBlue.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .clickable { viewModel.toggleUnit() }
                            .padding(end = 8.dp)
                            .testTag("unit_toggle_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (showInMBps) "MB/s" else "Mbps",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = if (showInMBps) PulseEmerald else ElectricSkyBlue
                            )
                        }
                    }

                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Ookla API & Licensing Info",
                            tint = ElectricSkyBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Error banner if any error occurs with direct Retry action
            if (metrics.phase == TestPhase.ERROR && !metrics.errorMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x33E11D48),
                    border = BorderStroke(1.dp, Color(0xFFE11D48)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = metrics.errorMessage ?: "Network test error occurred",
                            color = Color(0xFFFF6B81),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { viewModel.startSpeedTest() },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF6B81))
                        ) {
                            Text("Retry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // If speed test completed: Show results only!
            if (metrics.phase == TestPhase.COMPLETED) {
                ResultsOnlyView(
                    metrics = metrics,
                    telemetry = telemetry,
                    activeServer = activeServer,
                    showInMBps = showInMBps,
                    testDurationSec = testDurationSec,
                    onTestAgain = { viewModel.startSpeedTest() },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Active Speed Testing or Ready state: Show Real Time Speed & Gauge
                SpeedometerGauge(
                    speedMbps = if (isTesting) metrics.currentSpeedMbps else 0f,
                    maxScaleMbps = 500f,
                    phase = metrics.phase,
                    progress = metrics.progress,
                    pingMs = metrics.pingMs,
                    showInMBps = showInMBps,
                    size = 270.dp
                )

                // Real-time progress bar while testing
                if (isTesting) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { metrics.progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ElectricCyan,
                            trackColor = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(metrics.progress * 100).toInt()}% • ${testDurationSec}s Speed Test",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = ElectricSkyBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Real-time speed in metric cards: Download and Upload update in real time (Ookla Standard)
                SpeedTestMetricsPanel(
                    downloadMbps = if (isTesting && metrics.phase == TestPhase.DOWNLOAD_TEST) {
                        metrics.currentSpeedMbps
                    } else {
                        metrics.downloadMbps
                    },
                    uploadMbps = if (isTesting && metrics.phase == TestPhase.UPLOAD_TEST) {
                        metrics.currentSpeedMbps
                    } else {
                        metrics.uploadMbps
                    },
                    pingMs = metrics.pingMs,
                    jitterMs = metrics.jitterMs,
                    packetLossPercent = metrics.packetLossPercent,
                    activePhase = metrics.phase,
                    showInMBps = showInMBps
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Standard Ookla ISP & Server Information Bar (Name and IP)
                StandardOoklaInfoBar(
                    ispName = if (telemetry.ispName.isNotBlank() && telemetry.ispName != "Detecting ISP...") telemetry.ispName else "Ookla Network",
                    connectionName = telemetry.connectionName,
                    publicIp = telemetry.publicIpv4,
                    serverName = activeServer.name,
                    serverSponsor = activeServer.sponsor,
                    serverLocation = "${activeServer.city}, ${activeServer.countryCode.ifBlank { activeServer.country }}",
                    serverIp = activeServer.ip.ifBlank { activeServer.displayIp.ifBlank { activeServer.host.substringBefore(":") } },
                    onServerClick = onNavigateToServerSelect,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Button: Start Test / Stop Testing
                val buttonBorderColor by animateColorAsState(
                    targetValue = if (isTesting) Color(0xFFE11D48) else ElectricSkyBlue,
                    animationSpec = tween(durationMillis = 350),
                    label = "btnBorderColor"
                )

                Button(
                    onClick = {
                        if (isTesting) {
                            viewModel.stopSpeedTest()
                        } else {
                            viewModel.startSpeedTest()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_test_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTesting) Color(0xFF2B0A12) else Color(0xFF0F152B),
                        contentColor = DarkTextPrimary
                    ),
                    border = BorderStroke(1.5.dp, buttonBorderColor)
                ) {
                    Icon(
                        imageVector = if (isTesting) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = buttonBorderColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTesting) "Stop Testing" else "Start ${testDurationSec}s Test",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp
                        ),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ResultsOnlyView(
    metrics: LiveMetrics,
    telemetry: NetworkTelemetry,
    activeServer: ServerLocation,
    showInMBps: Boolean = false,
    testDurationSec: Int = 20,
    onTestAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val quality = remember(metrics.downloadMbps, metrics.uploadMbps, metrics.pingMs, metrics.jitterMs) {
        SpeedTestEngine.evaluateQuality(
            downloadMbps = metrics.downloadMbps,
            uploadMbps = metrics.uploadMbps,
            pingMs = metrics.pingMs,
            jitterMs = metrics.jitterMs
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Pill
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PulseEmerald.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, PulseEmerald.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PulseEmerald,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SPEED TEST COMPLETE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = PulseEmerald
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Connection Rating / Quality Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(ElectricCyan, PulseEmerald))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(ElectricCyan, PulseEmerald))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = quality.grade,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        ),
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Connection Grade ${quality.grade}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = quality.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Primary Speed Results (Download & Upload)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Download Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "DOWNLOAD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (showInMBps) {
                            String.format(Locale.US, "%.2f", metrics.downloadMbps / 8f)
                        } else {
                            String.format(Locale.US, "%.1f", metrics.downloadMbps)
                        },
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.White
                    )
                    Text(
                        text = if (showInMBps) "MB/s" else "Mbps",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = ElectricCyan
                    )
                    if (metrics.downloadMbps > 0.05f) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (showInMBps) {
                                "≈ ${String.format(Locale.US, "%.1f", metrics.downloadMbps)} Mbps"
                            } else {
                                "≈ ${String.format(Locale.US, "%.2f", metrics.downloadMbps / 8f)} MB/s"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Upload Card (Ookla Standard Purple Theme)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, PulsePurple.copy(alpha = 0.6f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = PulsePurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "UPLOAD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (showInMBps) {
                            String.format(Locale.US, "%.2f", metrics.uploadMbps / 8f)
                        } else {
                            String.format(Locale.US, "%.1f", metrics.uploadMbps)
                        },
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.White
                    )
                    Text(
                        text = if (showInMBps) "MB/s" else "Mbps",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = PulsePurple
                    )
                    if (metrics.uploadMbps > 0.05f) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (showInMBps) {
                                "≈ ${String.format(Locale.US, "%.1f", metrics.uploadMbps)} Mbps"
                            } else {
                                "≈ ${String.format(Locale.US, "%.2f", metrics.uploadMbps / 8f)} MB/s"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Latency details (Ping, Jitter, Loaded Ping, Packet Loss)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PING",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${metrics.pingMs} ms",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PulseEmerald
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "JITTER",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${metrics.jitterMs} ms",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFF59E0B)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LOADED",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val loadedVal = if (metrics.loadedPingMs > 0) metrics.loadedPingMs else metrics.pingMs
                    Text(
                        text = "$loadedVal ms",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ElectricSkyBlue
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LOSS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(Locale.US, "%.0f%%", metrics.packetLossPercent),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Network and Server Summary
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Connection",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = telemetry.connectionName,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ISP Provider",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = if (telemetry.ispName.isNotBlank() && telemetry.ispName != "Detecting ISP...") telemetry.ispName else "Ookla Network",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Test Duration",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "${testDurationSec}s",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ElectricCyan
                    )
                }

                if (telemetry.publicIpv4.isNotBlank() && telemetry.publicIpv4 != "Checking...") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Your IP",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = telemetry.publicIpv4,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = ElectricSkyBlue
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Server Name",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                    val serverDisplayName = if (activeServer.sponsor.isNotBlank() && activeServer.sponsor != activeServer.name) {
                        "${activeServer.name} (${activeServer.sponsor})"
                    } else {
                        activeServer.name
                    }
                    Text(
                        text = serverDisplayName,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val activeServerIp = activeServer.ip.ifBlank { activeServer.displayIp.ifBlank { activeServer.host.substringBefore(":") } }
                if (activeServerIp.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Server IP",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = activeServerIp,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = PulseEmerald
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "${activeServer.city}, ${activeServer.country}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons: "Test Again"
        Button(
            onClick = onTestAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_test_button"),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF063321),
                contentColor = DarkTextPrimary
            ),
            border = BorderStroke(1.5.dp, PulseEmerald)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = PulseEmerald,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Test Again",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                ),
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Share & Copy Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    val effectiveIspName = if (telemetry.ispName.isNotBlank() && telemetry.ispName != "Detecting ISP...") telemetry.ispName else "Ookla Network"
                    val summary = """
Speedo Net Results
Download: ${String.format(Locale.US, "%.1f", metrics.downloadMbps)} Mbps
Upload: ${String.format(Locale.US, "%.1f", metrics.uploadMbps)} Mbps
Ping: ${metrics.pingMs} ms | Jitter: ${metrics.jitterMs} ms
Grade: ${quality.grade}
Location: ${activeServer.city}, ${activeServer.country} • $effectiveIspName
Server: ${activeServer.name}
ISP: $effectiveIspName
                    """.trimIndent()
                    ResultShareHelper.shareSummary(context, summary)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("share_summary_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F172A),
                    contentColor = DarkTextPrimary
                ),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Share",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }

            Button(
                onClick = {
                    val summary = """
Speedo Net Results
Download: ${String.format(Locale.US, "%.1f", metrics.downloadMbps)} Mbps
Upload: ${String.format(Locale.US, "%.1f", metrics.uploadMbps)} Mbps
Ping: ${metrics.pingMs} ms
                    """.trimIndent()
                    ResultShareHelper.copyToClipboard(context, summary)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("copy_summary_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F172A),
                    contentColor = DarkTextPrimary
                ),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = ElectricSkyBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Copy",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StandardOoklaInfoBar(
    ispName: String,
    connectionName: String,
    publicIp: String,
    serverName: String,
    serverSponsor: String = "",
    serverLocation: String,
    serverIp: String = "",
    onServerClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: CONNECTIONS (ISP Provider & Client Public IP)
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Connections",
                        tint = ElectricSkyBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CONNECTIONS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = ispName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (publicIp.isNotBlank() && publicIp != "Checking...") publicIp else connectionName,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Vertical separator
            Box(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .width(1.dp)
                    .height(38.dp)
                    .background(Color(0xFF1E293B))
            )

            // Right: SERVER (Server Name, Sponsor, IP, and Location)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (onServerClick != null) {
                            Modifier
                                .clickable { onServerClick() }
                                .testTag("server_select_pill")
                        } else Modifier
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = "Server",
                        tint = PulseEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SERVER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color(0xFF64748B)
                        )
                        if (onServerClick != null) {
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Change Server",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    val displayServerTitle = if (serverSponsor.isNotBlank() && serverSponsor != serverName) {
                        serverSponsor
                    } else {
                        serverName
                    }
                    Text(
                        text = displayServerTitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val serverSubtext = if (serverIp.isNotBlank()) {
                        "$serverLocation • $serverIp"
                    } else {
                        serverLocation
                    }
                    Text(
                        text = serverSubtext,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}


