package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TestPhase
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricSkyBlue
import com.example.ui.theme.PulseEmerald

enum class StepperPhase(val label: String, val stepNumber: Int) {
    CONNECTING("Connecting", 1),
    PING("Ping", 2),
    DOWNLOAD("Download", 3),
    UPLOAD("Upload", 4),
    COMPLETE("Complete", 5)
}

@Composable
fun PhaseStepper(
    currentPhase: TestPhase,
    modifier: Modifier = Modifier
) {
    val activeStep = when (currentPhase) {
        TestPhase.IDLE -> 0
        TestPhase.PREPARING -> 1
        TestPhase.PING_TEST -> 2
        TestPhase.DOWNLOAD_TEST -> 3
        TestPhase.UPLOAD_TEST -> 4
        TestPhase.COMPLETED -> 5
        TestPhase.ERROR -> 0
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val steps = StepperPhase.values()
        steps.forEachIndexed { index, step ->
            val isCurrent = step.stepNumber == activeStep
            val isDone = step.stepNumber < activeStep || activeStep == 5

            val stepColor by animateColorAsState(
                targetValue = when {
                    isCurrent -> ElectricCyan
                    isDone -> PulseEmerald
                    else -> Color(0xFF334155)
                },
                animationSpec = tween(durationMillis = 300),
                label = "stepColor_${step.name}"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> ElectricCyan.copy(alpha = 0.2f)
                                isDone -> PulseEmerald.copy(alpha = 0.2f)
                                else -> Color(0xFF1E293B)
                            }
                        )
                        .border(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = stepColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = PulseEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Text(
                            text = "${step.stepNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isCurrent) ElectricCyan else Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = step.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = when {
                        isCurrent -> Color.White
                        isDone -> Color(0xFFE2E8F0)
                        else -> Color(0xFF64748B)
                    }
                )
            }

            // Divider line between steps
            if (index < steps.size - 1) {
                val lineDone = step.stepNumber < activeStep
                val lineColor by animateColorAsState(
                    targetValue = if (lineDone) PulseEmerald.copy(alpha = 0.8f) else Color(0xFF1E293B),
                    animationSpec = tween(durationMillis = 300),
                    label = "lineColor_$index"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 4.dp, vertical = 0.dp)
                        .padding(bottom = 16.dp)
                        .background(lineColor)
                )
            }
        }
    }
}
