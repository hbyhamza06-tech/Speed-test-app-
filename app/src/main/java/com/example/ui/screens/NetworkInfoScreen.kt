package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionType
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBackgroundSecondary
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricSkyBlue
import com.example.ui.theme.PulseEmerald
import com.example.ui.theme.PulsePurple
import com.example.ui.theme.PulseRose
import com.example.ui.viewmodel.SpeedTestViewModel
import kotlinx.coroutines.launch

@Composable
fun NetworkInfoScreen(
    viewModel: SpeedTestViewModel,
    onNavigateToServerSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val telemetry by viewModel.telemetry.collectAsState()
    val activeServer by viewModel.activeServer.collectAsState()
    val isPingingServers by viewModel.isPingingServers.collectAsState()
    val scrollState = rememberScrollState()

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Network & Server",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp
                        ),
                        color = DarkTextPrimary
                    )
                    Text(
                        text = "Active Connection & Host Diagnostics",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = DarkTextSecondary
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.refreshNetworkInfo()
                        Toast.makeText(context, "Network telemetry refreshed", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Telemetry",
                        tint = ElectricSkyBlue
                    )
                }
            }

            // 1. Connection Status Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(ElectricCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (telemetry.connectionType) {
                                        ConnectionType.WIFI -> Icons.Default.Wifi
                                        ConnectionType.CELLULAR_5G,
                                        ConnectionType.CELLULAR_4G,
                                        ConnectionType.CELLULAR_3G -> Icons.Default.SignalCellularAlt
                                        else -> Icons.Default.Router
                                    },
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (telemetry.connectionType == ConnectionType.WIFI && telemetry.wifiSsid.isNotBlank() && telemetry.wifiSsid != "Unknown SSID") {
                                        telemetry.wifiSsid
                                    } else {
                                        telemetry.connectionName
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = when (telemetry.connectionType) {
                                        ConnectionType.WIFI -> "Wi-Fi Network"
                                        ConnectionType.CELLULAR_5G -> "5G Cellular Network"
                                        ConnectionType.CELLULAR_4G -> "4G LTE Network"
                                        ConnectionType.CELLULAR_3G -> "3G Cellular Network"
                                        ConnectionType.ETHERNET -> "Ethernet Connection"
                                        ConnectionType.VPN -> "VPN Active"
                                        ConnectionType.UNKNOWN -> "Cellular / Broadband"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = DarkTextSecondary
                                )
                            }
                        }

                        // Connected Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = PulseEmerald.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, PulseEmerald.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(PulseEmerald)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Connected",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = PulseEmerald
                                )
                            }
                        }
                    }

                    if (telemetry.linkSpeedMbps > 0 || telemetry.signalStrengthDbm != 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (telemetry.linkSpeedMbps > 0) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF070D1D)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "LINK SPEED",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color(0xFF64748B)
                                        )
                                        Text(
                                            text = "${telemetry.linkSpeedMbps} Mbps",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF070D1D)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "SIGNAL STRENGTH",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = "${telemetry.signalStrengthDbm} dBm (${telemetry.signalLevel}/4)",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Active Benchmark Server Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = ElectricSkyBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACTIVE TEST SERVER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = ElectricSkyBlue
                            )
                        }

                        if (activeServer.pingMs > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF070D1D),
                                border = BorderStroke(1.dp, Color(0xFF1E293B))
                            ) {
                                Text(
                                    text = "${activeServer.pingMs} ms",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = PulseEmerald,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    NetworkDetailItem(
                        label = "Server Name",
                        value = activeServer.name
                    )

                    val ispInfo = if (telemetry.ispName.isNotBlank() && telemetry.ispName != "Detecting ISP...") telemetry.ispName else "Ookla Network"
                    NetworkDetailItem(
                        label = "Location",
                        value = "${activeServer.city}, ${activeServer.country} • $ispInfo"
                    )

                    NetworkDetailItem(
                        label = "Host Domain",
                        value = activeServer.host
                    )

                    NetworkDetailItem(
                        label = "Estimated Distance",
                        value = if (activeServer.distanceKm > 0) "${activeServer.distanceKm.toInt()} km" else "Optimal / Local"
                    )

                    if (activeServer.sponsor.isNotBlank() && activeServer.sponsor != activeServer.name) {
                        NetworkDetailItem(
                            label = "Sponsor",
                            value = activeServer.sponsor
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = onNavigateToServerSelect,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("switch_server_from_network_tab"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "Select Different Benchmark Server",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 3. IP Address & ISP Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = PulsePurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "IP & ISP CONFIGURATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = PulsePurple
                        )
                    }

                    NetworkDetailItem(
                        label = "Internet Service Provider",
                        value = if (telemetry.ispName.isNotBlank() && telemetry.ispName != "Detecting ISP...") {
                            telemetry.ispName
                        } else {
                            "Broadband Provider / Ookla Network"
                        }
                    )

                    NetworkDetailItemWithCopy(
                        context = context,
                        label = "Public IPv4",
                        value = if (telemetry.publicIpv4.isNotBlank()) telemetry.publicIpv4 else "Detecting..."
                    )

                    NetworkDetailItem(
                        label = "Local IPv4",
                        value = telemetry.ipv4Address
                    )

                    NetworkDetailItem(
                        label = "Default Gateway",
                        value = telemetry.gateway
                    )

                    NetworkDetailItem(
                        label = "DNS Resolvers",
                        value = telemetry.dnsServers
                    )

                    if (telemetry.isVpnActive) {
                        NetworkDetailItem(
                            label = "VPN Status",
                            value = "Active VPN Detected"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun NetworkDetailItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
            color = Color(0xFF94A3B8)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = Color.White
        )
    }
}

@Composable
private fun NetworkDetailItemWithCopy(
    context: Context,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
            color = Color(0xFF94A3B8)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                ),
                color = ElectricCyan
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
                    Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy $label",
                    tint = DarkTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
