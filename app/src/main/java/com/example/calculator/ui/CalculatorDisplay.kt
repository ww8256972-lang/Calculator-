package com.example.calculator.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.engine.AngleMode
import kotlinx.coroutines.delay

@Composable
fun CalculatorDisplay(
    expression: String,
    result: String,
    livePreview: String?,
    errorMessage: String?,
    isEvaluated: Boolean,
    memoryValue: Double?,
    angleMode: AngleMode,
    onAngleToggle: () -> Unit,
    onMemoryClick: () -> Unit,
    onCopyFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var showCopiedBadge by searchCopiedState()
    val scrollState = rememberScrollState()

    // Auto-scroll to end when expression changes
    LaunchedEffect(expression) {
        if (expression.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calculator_display_surface"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        tonalElevation = 6.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Status bar row: Angle mode, Memory tag, and Copy button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Angle Mode Chip
                    AssistChip(
                        onClick = onAngleToggle,
                        label = {
                            Text(
                                text = if (angleMode == AngleMode.DEGREE) "DEG" else "RAD",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            labelColor = MaterialTheme.colorScheme.secondary
                        ),
                        border = null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("angle_mode_chip")
                    )

                    // Memory status indicator
                    if (memoryValue != null) {
                        AssistChip(
                            onClick = onMemoryClick,
                            label = {
                                Text(
                                    text = "M",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                labelColor = MaterialTheme.colorScheme.primary
                            ),
                            border = null,
                            shape = CircleShape,
                            modifier = Modifier.testTag("memory_indicator_chip")
                        )
                    }
                }

                // Copy Action & Copied Toast pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AnimatedVisibility(
                        visible = showCopiedBadge,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Copied",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            val textToCopy = if (isEvaluated && errorMessage == null) result else (livePreview ?: expression)
                            if (textToCopy.isNotBlank()) {
                                clipboardManager.setText(AnnotatedString(textToCopy.replace(",", "")))
                                showCopiedBadge = true
                                onCopyFeedback()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("copy_result_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy result to clipboard",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expression input display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expression.isEmpty()) "0" else expression,
                    color = if (isEvaluated) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurface,
                    fontSize = if (isEvaluated) 20.sp else 32.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.testTag("calculator_expression_text")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main result or live preview
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                when {
                    errorMessage != null -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.testTag("calculator_error_text")
                            )
                        }
                    }
                    isEvaluated -> {
                        Text(
                            text = result,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            modifier = Modifier.testTag("calculator_result_text")
                        )
                    }
                    livePreview != null -> {
                        Text(
                            text = "= $livePreview",
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            modifier = Modifier.testTag("calculator_live_preview_text")
                        )
                    }
                    else -> {
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun searchCopiedState(): androidx.compose.runtime.MutableState<Boolean> {
    val state = remember { mutableStateOf(false) }
    LaunchedEffect(state.value) {
        if (state.value) {
            delay(1800)
            state.value = false
        }
    }
    return state
}
