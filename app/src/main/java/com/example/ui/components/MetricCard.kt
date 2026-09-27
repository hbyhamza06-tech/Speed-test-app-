package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PulseAmber
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.PulseEmerald
import com.example.ui.theme.PulsePurple
import com.example.ui.theme.PulseRose
import java.util.Locale

@Composable
fun BandwidthSummaryCard(
    downloadMbps: Float,
    uploadMbps: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Download Block
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PulseCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Download",
                        tint = PulseCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "DOWNLOAD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f", downloadMbps),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Mbps",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Divider line
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )

            // Upload Block
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PulsePurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Upload",
                        tint = PulsePurple,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "UPLOAD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f", uploadMbps),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Mbps",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun LatencyGridCards(
    pingMs: Int,
    jitterMs: Int,
    packetLossPercent: Float,
    loadedPingMs: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricItemCard(
            title = "PING",
            value = if (pingMs > 0) "$pingMs" else "--",
            unit = "ms",
            icon = Icons.Default.NetworkPing,
            tint = PulseEmerald,
            modifier = Modifier.weight(1f)
        )
        MetricItemCard(
            title = "JITTER",
            value = if (jitterMs > 0) "$jitterMs" else "--",
            unit = "ms",
            icon = Icons.Default.Speed,
            tint = PulseAmber,
            modifier = Modifier.weight(1f)
        )
        MetricItemCard(
            title = "LOSS",
            value = String.format(Locale.US, "%.1f", packetLossPercent),
            unit = "%",
            icon = Icons.Default.Shield,
            tint = if (packetLossPercent > 0.5f) PulseRose else PulseEmerald,
            modifier = Modifier.weight(1f)
        )
        MetricItemCard(
            title = "LOADED",
            value = if (loadedPingMs > 0) "$loadedPingMs" else "--",
            unit = "ms",
            icon = Icons.Default.ElectricBolt,
            tint = PulsePurple,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MetricItemCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    SingleMetricCard(
        title = title,
        value = value,
        unit = unit,
        icon = icon,
        tint = tint,
        isActive = false,
        modifier = modifier
    )
}

@Composable
fun SpeedTestMetricsPanel(
    downloadMbps: Float,
    uploadMbps: Float,
    pingMs: Int,
    activePhase: com.example.data.model.TestPhase,
    modifier: Modifier = Modifier,
    jitterMs: Int = 0,
    packetLossPercent: Float = 0f,
    showInMBps: Boolean = false
) {
    val isPinging = activePhase == com.example.data.model.TestPhase.PING_TEST
    val isDownloading = activePhase == com.example.data.model.TestPhase.DOWNLOAD_TEST
    val isUploading = activePhase == com.example.data.model.TestPhase.UPLOAD_TEST

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Ookla Standard Download & Upload Twin Hero Speed Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // DOWNLOAD Card
            OoklaSpeedCard(
                title = "DOWNLOAD",
                speedMbps = downloadMbps,
                icon = Icons.Default.ArrowDownward,
                accentColor = PulseCyan,
                isActive = isDownloading,
                isCompleted = !isDownloading && downloadMbps > 0f,
                showInMBps = showInMBps,
                modifier = Modifier.weight(1f)
            )

            // UPLOAD Card
            OoklaSpeedCard(
                title = "UPLOAD",
                speedMbps = uploadMbps,
                icon = Icons.Default.ArrowUpward,
                accentColor = PulsePurple,
                isActive = isUploading,
                isCompleted = !isUploading && uploadMbps > 0f,
                showInMBps = showInMBps,
                modifier = Modifier.weight(1f)
            )
        }

        // 2. Ookla Standard Latency Bar (Ping, Jitter, Loss)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(
                width = if (isPinging) 1.5.dp else 1.dp,
                color = if (isPinging) PulseEmerald else Color(0xFF1E293B)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ping
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NetworkPing,
                        contentDescription = "Ping",
                        tint = PulseEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "PING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = if (pingMs > 0) "$pingMs ms" else "--",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }

                // Jitter
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "JITTER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = if (jitterMs > 0) "$jitterMs ms" else "--",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (jitterMs > 0) PulseAmber else Color(0xFF94A3B8)
                    )
                }

                // Loss
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "LOSS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = if (packetLossPercent >= 0f && (activePhase != com.example.data.model.TestPhase.IDLE && activePhase != com.example.data.model.TestPhase.PREPARING)) {
                            String.format(Locale.US, "%.0f%%", packetLossPercent)
                        } else {
                            "0%"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }
    }
}

@Composable
fun OoklaSpeedCard(
    title: String,
    speedMbps: Float,
    icon: ImageVector,
    accentColor: Color,
    isActive: Boolean,
    isCompleted: Boolean,
    showInMBps: Boolean = false,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isActive -> accentColor
        isCompleted -> accentColor.copy(alpha = 0.45f)
        else -> Color(0xFF1E293B)
    }

    val backgroundColor = when {
        isActive -> accentColor.copy(alpha = 0.08f)
        else -> Color(0xFF0F172A)
    }

    val displaySpeed = speedMbps

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor,
        border = BorderStroke(if (isActive) 1.8.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Icon, Title, and Active Pulse
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = if (isActive) 0.25f else 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = if (isActive) accentColor else Color(0xFF94A3B8)
                )

                if (isActive) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Speed Value in Ookla Standard Display
            Text(
                text = if (displaySpeed > 0f) {
                    if (showInMBps) String.format(Locale.US, "%.2f", displaySpeed / 8f)
                    else String.format(Locale.US, "%.1f", displaySpeed)
                } else "--",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                ),
                color = Color.White,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Unit in Ookla Standard Style
            Text(
                text = if (showInMBps) "MB/s" else "Mbps",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = accentColor
            )

            // Secondary real speed readout (dual unit)
            if (displaySpeed > 0.05f) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (showInMBps) "≈ ${String.format(Locale.US, "%.1f", displaySpeed)} Mbps"
                    else "≈ ${String.format(Locale.US, "%.2f", displaySpeed / 8f)} MB/s",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
fun OoklaMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    tint: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) tint else Color(0xFF1E293B)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = if (isActive) 0.22f else 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = tint,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = if (isActive) tint else Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                ),
                color = Color.White,
                maxLines = 1
            )

            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun BandwidthMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    accentColor: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) accentColor else Color(0xFF1E293B)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = Color(0xFF94A3B8)
                    )
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

@Composable
fun SingleMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    tint: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) tint else Color(0xFF1E293B)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFF94A3B8)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

