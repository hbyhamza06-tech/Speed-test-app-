package com.example.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TestPhase
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricSkyBlue
import com.example.ui.theme.GaugeInnerRingColor
import com.example.ui.theme.GaugeTrackInactive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.DarkTextSecondary
import java.util.Locale

@Composable
fun SpeedometerGauge(
    speedMbps: Float,
    maxScaleMbps: Float = 500f,
    phase: TestPhase,
    progress: Float = 0f,
    pingMs: Int = 0,
    showInMBps: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 270.dp
) {
    // Scale latching: resets when idle/preparing, only latches upwards during testing to prevent scale oscillation
    var currentScale by remember { mutableFloatStateOf(500f) }

    LaunchedEffect(phase) {
        if (phase == TestPhase.IDLE || phase == TestPhase.PREPARING) {
            currentScale = 500f
        }
    }

    if (speedMbps > 1050f && currentScale < 2000f) {
        currentScale = 2000f
    } else if (speedMbps > 520f && currentScale < 1000f) {
        currentScale = 1000f
    }

    val effectiveMaxScale = currentScale
    val normalizedFraction = if (phase == TestPhase.PING_TEST) {
        if (pingMs > 0) (pingMs.toFloat() / 150f).coerceIn(0.08f, 0.90f) else (progress * 0.35f).coerceIn(0.05f, 0.4f)
    } else {
        calculateDynamicGaugeFraction(speedMbps, effectiveMaxScale)
    }

    // Smooth physical spring physics for needle motion - realistic, fluid, analog momentum
    val animatedProgress by animateFloatAsState(
        targetValue = normalizedFraction.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = 0.82f, // Eliminates oscillation while preserving responsive needle swing
            stiffness = 160f      // Swift, continuous follow-through without stutter
        ),
        label = "gaugeProgress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val center = Offset(canvasW / 2f, canvasH / 2f)

            val strokeWidth = 18.dp.toPx()
            val outerMargin = strokeWidth / 2f + 8.dp.toPx()
            val arcSize = Size(canvasW - outerMargin * 2, canvasH - outerMargin * 2)
            val arcTopLeft = Offset(outerMargin, outerMargin)

            val startAngle = 145f
            val sweepTotal = 250f

            // 1. Inactive Dark Track Arc
            drawArc(
                color = GaugeTrackInactive,
                startAngle = startAngle,
                sweepAngle = sweepTotal,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Active Glowing Gradient Track Arc (Ookla Standard: Cyan for Download, Purple for Upload)
            val activeSweep = (animatedProgress * sweepTotal).coerceAtLeast(1.5f)
            val arcGradient = if (phase == TestPhase.UPLOAD_TEST) {
                Brush.sweepGradient(
                    0.35f to Color(0xFF5B21B6),
                    0.55f to Color(0xFF7C3AED),
                    0.75f to Color(0xFF8B5CF6),
                    0.90f to Color(0xFFA855F7),
                    1.00f to Color(0xFFC084FC),
                    center = center
                )
            } else {
                Brush.sweepGradient(
                    0.35f to Color(0xFF0052D4),
                    0.55f to Color(0xFF0075FF),
                    0.75f to Color(0xFF00A3FF),
                    0.90f to Color(0xFF00D2FF),
                    1.00f to Color(0xFF00F0FF),
                    center = center
                )
            }

            drawArc(
                brush = arcGradient,
                startAngle = startAngle,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 3. Thin Concentric Inner Circle Ring
            val innerRingRadius = (canvasW / 2f) * 0.62f
            drawCircle(
                color = GaugeInnerRingColor,
                radius = innerRingRadius,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )

            // 4. Dynamic Tick Scale Labels: adapts smoothly to 500, 1000, or 2000 Mbps
            val ticks = when {
                effectiveMaxScale >= 2000f -> listOf(
                    0f to "0",
                    100f to "100",
                    250f to "250",
                    500f to "500",
                    1000f to "1k",
                    1500f to "1.5k",
                    2000f to "2k"
                )
                effectiveMaxScale >= 1000f -> listOf(
                    0f to "0",
                    50f to "50",
                    100f to "100",
                    250f to "250",
                    500f to "500",
                    750f to "750",
                    1000f to "1k"
                )
                else -> listOf(
                    0f to "0",
                    15f to "15",
                    45f to "45",
                    75f to "75",
                    100f to "100",
                    250f to "250",
                    400f to "400",
                    500f to "500"
                )
            }

            val textRadius = (canvasW / 2f) * 0.73f
            val textPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#8E9BB5")
                textSize = 12.sp.toPx()
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
            }

            for ((speedVal, label) in ticks) {
                val fraction = calculateDynamicGaugeFraction(speedVal, effectiveMaxScale)
                val angleDeg = startAngle + (fraction * sweepTotal)
                val angleRad = (angleDeg * PI / 180.0)

                val x = center.x + (textRadius * cos(angleRad)).toFloat()
                val y = center.y + (textRadius * sin(angleRad)).toFloat() + 4.dp.toPx()

                drawContext.canvas.nativeCanvas.drawText(label, x, y, textPaint)
            }

            // 5. Needle Pointer with glowing indicator tip
            val needleAngleDeg = startAngle + (animatedProgress * sweepTotal)
            val needleAngleRad = (needleAngleDeg * PI / 180.0)
            val needleLength = innerRingRadius * 0.95f

            val tipX = center.x + (needleLength * cos(needleAngleRad)).toFloat()
            val tipY = center.y + (needleLength * sin(needleAngleRad)).toFloat()

            // Perpendicular vector for base of needle
            val perpAngle = needleAngleRad + (PI / 2.0)
            val baseWidth = 5.dp.toPx()
            val base1 = Offset(
                center.x + (baseWidth * cos(perpAngle)).toFloat(),
                center.y + (baseWidth * sin(perpAngle)).toFloat()
            )
            val base2 = Offset(
                center.x - (baseWidth * cos(perpAngle)).toFloat(),
                center.y - (baseWidth * sin(perpAngle)).toFloat()
            )

            // Draw sleek tapered needle triangle
            val needlePath = Path().apply {
                moveTo(base1.x, base1.y)
                lineTo(tipX, tipY)
                lineTo(base2.x, base2.y)
                close()
            }

            val needleColor = when (phase) {
                TestPhase.UPLOAD_TEST -> Color(0xFFC084FC)
                TestPhase.PING_TEST -> Color(0xFF34D399)
                else -> ElectricSkyBlue
            }

            val needleAccentColor = when (phase) {
                TestPhase.UPLOAD_TEST -> Color(0xFFA855F7)
                TestPhase.PING_TEST -> Color(0xFF10B981)
                else -> ElectricCyan
            }

            drawPath(
                path = needlePath,
                color = needleColor
            )

            // Needle tip highlight dot with subtle glow
            drawCircle(
                color = needleAccentColor.copy(alpha = 0.4f),
                radius = 5.dp.toPx(),
                center = Offset(tipX, tipY)
            )
            drawCircle(
                color = Color.White,
                radius = 2.5.dp.toPx(),
                center = Offset(tipX, tipY)
            )

            // Needle Center Hub
            drawCircle(
                color = Color(0xFF0C1024),
                radius = 12.dp.toPx(),
                center = center
            )
            drawCircle(
                color = needleColor,
                radius = 8.dp.toPx(),
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )
            drawCircle(
                color = needleAccentColor,
                radius = 4.dp.toPx(),
                center = center
            )
        }

        // Real-time speed numeric display positioned neatly beneath the center hub (Ookla Standard)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 74.dp)
        ) {
            val phaseAccentColor = when (phase) {
                TestPhase.DOWNLOAD_TEST -> ElectricCyan
                TestPhase.UPLOAD_TEST -> Color(0xFFA855F7)
                TestPhase.COMPLETED -> Color(0xFF10B981)
                TestPhase.ERROR -> Color(0xFFF43F5E)
                TestPhase.PREPARING, TestPhase.PING_TEST -> Color(0xFF38BDF8)
                else -> DarkTextSecondary
            }

            // Phase indicator with directional icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                when (phase) {
                    TestPhase.DOWNLOAD_TEST -> {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Download",
                            tint = phaseAccentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    TestPhase.UPLOAD_TEST -> {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Upload",
                            tint = phaseAccentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    TestPhase.PING_TEST -> {
                        Icon(
                            imageVector = Icons.Default.NetworkPing,
                            contentDescription = "Ping",
                            tint = phaseAccentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    TestPhase.COMPLETED -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Complete",
                            tint = phaseAccentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    else -> {}
                }

                Text(
                    text = when (phase) {
                        TestPhase.PREPARING -> "CONNECTING"
                        TestPhase.PING_TEST -> "PING"
                        TestPhase.DOWNLOAD_TEST -> "DOWNLOAD"
                        TestPhase.UPLOAD_TEST -> "UPLOAD"
                        TestPhase.COMPLETED -> "COMPLETE"
                        TestPhase.ERROR -> "ERROR"
                        TestPhase.IDLE -> "READY"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = phaseAccentColor
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (phase == TestPhase.PING_TEST) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (pingMs > 0) "$pingMs" else "--",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = " ms",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = phaseAccentColor,
                        modifier = Modifier.padding(bottom = 5.dp, start = 3.dp)
                    )
                }

                Text(
                    text = "Measuring Latency & Jitter",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF94A3B8)
                )
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (showInMBps) {
                            String.format(Locale.US, "%.2f", speedMbps / 8f)
                        } else {
                            String.format(Locale.US, "%.1f", speedMbps)
                        },
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = if (showInMBps) " MB/s" else " Mbps",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = phaseAccentColor,
                        modifier = Modifier.padding(bottom = 5.dp, start = 3.dp)
                    )
                }

                // Real speed secondary converted value so user always knows both Mbps and MB/s
                if (speedMbps > 0.05f) {
                    Text(
                        text = if (showInMBps) {
                            "≈ ${String.format(Locale.US, "%.1f", speedMbps)} Mbps"
                        } else {
                            "≈ ${String.format(Locale.US, "%.2f", speedMbps / 8f)} MB/s"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

/**
 * Maps speed to 0..1 matching dynamic scales (500, 1000, 2000 Mbps)
 */
private fun calculateDynamicGaugeFraction(speedMbps: Float, maxScale: Float): Float {
    if (speedMbps <= 0f) return 0f
    if (speedMbps >= maxScale) return 1f

    val checkpoints = when {
        maxScale >= 2000f -> listOf(
            0f to 0.0f,
            100f to 0.15f,
            250f to 0.30f,
            500f to 0.50f,
            1000f to 0.70f,
            1500f to 0.85f,
            2000f to 1.0f
        )
        maxScale >= 1000f -> listOf(
            0f to 0.0f,
            50f to 0.15f,
            100f to 0.30f,
            250f to 0.50f,
            500f to 0.70f,
            750f to 0.85f,
            1000f to 1.0f
        )
        else -> listOf(
            0f to 0.0f,
            15f to 0.14f,
            45f to 0.28f,
            75f to 0.42f,
            100f to 0.55f,
            250f to 0.72f,
            400f to 0.88f,
            500f to 1.0f
        )
    }

    for (i in 0 until checkpoints.size - 1) {
        val (s1, f1) = checkpoints[i]
        val (s2, f2) = checkpoints[i + 1]
        if (speedMbps in s1..s2) {
            val ratio = (speedMbps - s1) / (s2 - s1)
            return f1 + ratio * (f2 - f1)
        }
    }

    return 1f
}
