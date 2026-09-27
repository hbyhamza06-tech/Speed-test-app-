package com.example.ui.components

import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SpeedTestEntity
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.PulseEmerald
import com.example.ui.theme.PulsePurple
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ShareableResultCard(
    result: SpeedTestEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(PulseCyan, PulsePurple)))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Brand & Grade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(PulseCyan, PulsePurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Speedo Net",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Verified Network Benchmark",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Grade Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PulseCyan.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, PulseCyan)
                ) {
                    Text(
                        text = "GRADE ${result.ratingGrade}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = PulseCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Speeds (Download / Upload)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Download
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = PulseCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "DOWNLOAD",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", result.downloadMbps),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 36.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Mbps",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = PulseCyan
                    )
                }

                // Upload
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", result.uploadMbps),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 36.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Mbps",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = PulsePurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Secondary metrics row (Ping, Jitter, Loss)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "PING", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        Text(
                            text = "${result.pingMs} ms",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = PulseEmerald
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "JITTER", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        Text(
                            text = "${result.jitterMs} ms",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF59E0B)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "LOSS", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        Text(
                            text = String.format(Locale.US, "%.1f%%", result.packetLossPercent),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "LOADED", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        Text(
                            text = "${result.loadedPingMs} ms",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFC084FC)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer info
            val dateStr = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(result.timestamp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val clientInfo = if (result.publicIp.isNotBlank()) "${result.ispName} • ${result.publicIp}" else result.ispName
                Text(
                    text = clientInfo,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }
            if (result.serverName.isNotBlank() || result.serverHost.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                val serverInfo = if (result.serverHost.isNotBlank()) "${result.serverName} • ${result.serverHost}" else result.serverName
                Text(
                    text = "Server: $serverInfo",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

object ResultShareHelper {
    fun formatSummary(
        downloadMbps: Float,
        uploadMbps: Float,
        pingMs: Int,
        jitterMs: Int,
        packetLossPercent: Float,
        serverName: String,
        ispName: String,
        serverIp: String = "",
        publicIp: String = ""
    ): String {
        val serverLine = if (serverIp.isNotBlank()) "Server: $serverName ($serverIp)" else "Server: $serverName"
        val ispLine = if (publicIp.isNotBlank()) "ISP: $ispName ($publicIp)" else "ISP: $ispName"
        return """
Download: ${String.format(Locale.US, "%.1f", downloadMbps)} Mbps
Upload: ${String.format(Locale.US, "%.1f", uploadMbps)} Mbps
Ping: $pingMs ms
Jitter: $jitterMs ms
Packet Loss: ${String.format(Locale.US, "%.0f", packetLossPercent)}%
$serverLine
$ispLine
""".trimIndent()
    }

    fun copyToClipboard(context: Context, summaryText: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Speedo Net Results", summaryText)
        clipboard.setPrimaryClip(clip)
        android.widget.Toast.makeText(context, "Results copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
    }

    fun shareSummary(context: Context, summaryText: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Speedo Net Results")
            putExtra(Intent.EXTRA_TEXT, summaryText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Speedo Net Results"))
    }

    fun shareResult(context: Context, result: SpeedTestEntity) {
        val summary = formatSummary(
            downloadMbps = result.downloadMbps,
            uploadMbps = result.uploadMbps,
            pingMs = result.pingMs,
            jitterMs = result.jitterMs,
            packetLossPercent = result.packetLossPercent,
            serverName = result.serverName,
            ispName = result.ispName
        )
        shareSummary(context, summary)
    }
}
