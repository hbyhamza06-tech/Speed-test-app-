package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricSkyBlue

@Composable
fun OoklaInfoDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NetworkCheck,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Ookla Speedtest API & Licensing",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = DarkTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Speedo Net v${com.example.BuildConfig.VERSION_NAME} uses standards-compliant Ookla Speedtest endpoints for live server discovery and multi-stream network benchmarking.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = DarkTextSecondary
                )

                InfoSectionCard(
                    icon = Icons.Default.Policy,
                    title = "Ookla Licensing & Usage Policy",
                    description = "Official Ookla Speedtest Powered™ SDK integrations and production use of Ookla trademarks require an enterprise licensing agreement with Ookla / Ziff Davis. This application complies with public endpoint conventions and honors Ookla server policies."
                )

                InfoSectionCard(
                    icon = Icons.Default.Key,
                    title = "Secrets & Credentials Management",
                    description = "Enterprise Ookla API keys or custom Speedtest server credentials must remain protected on server-side infrastructure. Never commit private credentials in client-side bundles; use environment variables (.env / BuildConfig) for backend endpoints."
                )

                InfoSectionCard(
                    icon = Icons.Default.CheckCircleOutline,
                    title = "Protocol & Test Endpoints",
                    description = "Latency probe: /speedtest/latency.txt\nChunked download: /speedtest/random\nPayload upload: /speedtest/upload.php\nServer Discovery: speedtest.net/api/js/servers"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricSkyBlue,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Understood", fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun InfoSectionCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.9f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    ),
                    color = DarkTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                ),
                color = DarkTextSecondary
            )
        }
    }
}
