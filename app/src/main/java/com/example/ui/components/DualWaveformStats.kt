package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricViolet
import java.util.Locale
import kotlin.math.sin

@Composable
fun DualWaveformStats(
    downloadMbps: Float,
    uploadMbps: Float,
    isTesting: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveAnim")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isTesting) (2f * Math.PI.toFloat()) else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    // Smooth numerical animation for download and upload speeds
    val animDownloadMbps by animateFloatAsState(
        targetValue = downloadMbps,
        animationSpec = tween(durationMillis = 200),
        label = "animDownload"
    )
    val animUploadMbps by animateFloatAsState(
        targetValue = uploadMbps,
        animationSpec = tween(durationMillis = 200),
        label = "animUpload"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Download Section (Left)
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            MiniWaveformCanvas(
                color = ElectricCyan,
                offset = waveOffset,
                frequency = 3.5f,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(34.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Download",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = DarkTextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format(Locale.US, "%.2f", animDownloadMbps),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Text(
                    text = " Mbps",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = DarkTextSecondary,
                    modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                )
            }
        }

        // Upload Section (Right)
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            MiniWaveformCanvas(
                color = ElectricViolet,
                offset = waveOffset + 1.2f,
                frequency = 4.0f,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(34.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Upload",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = DarkTextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format(Locale.US, "%.1f", animUploadMbps),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Text(
                    text = " Mbps",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = DarkTextSecondary,
                    modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MiniWaveformCanvas(
    color: Color,
    offset: Float,
    frequency: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val midY = height / 2f

        val path = Path()
        val points = 60
        val dx = width / points

        for (i in 0..points) {
            val x = i * dx
            val normX = i.toFloat() / points
            // Undulating wave pattern with varying harmonics matching reference screenshot
            val y = midY + (height * 0.35f) * sin(normX * frequency * Math.PI.toFloat() + offset).toFloat() +
                    (height * 0.15f) * sin(normX * frequency * 2f * Math.PI.toFloat() + offset * 1.5f).toFloat()

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}
