package com.example.calculator.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkActionText
import com.example.ui.theme.DarkEqualsKey
import com.example.ui.theme.DarkNumberKey
import com.example.ui.theme.DarkOperatorKey

enum class KeyType {
    NUMBER,
    OPERATOR,
    ACTION,
    EQUALS,
    SCIENTIFIC
}

@Composable
fun CalculatorKeypad(
    isScientificMode: Boolean,
    onToggleScientific: () -> Unit,
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (String) -> Unit,
    onParenthesis: () -> Unit,
    onPercentage: () -> Unit,
    onToggleSign: () -> Unit,
    onClearAll: () -> Unit,
    onDelete: () -> Unit,
    onEqual: () -> Unit,
    onScientificFunc: (String) -> Unit,
    onMemoryClear: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryStore: () -> Unit,
    hasMemoryValue: Boolean,
    hapticEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val playHaptic = {
        if (hapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Memory & Quick Toggle Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MemoryButton(label = "MC", enabled = hasMemoryValue, onClick = { playHaptic(); onMemoryClear() })
            MemoryButton(label = "MR", enabled = hasMemoryValue, onClick = { playHaptic(); onMemoryRecall() })
            MemoryButton(label = "M+", enabled = true, onClick = { playHaptic(); onMemoryAdd() })
            MemoryButton(label = "M−", enabled = true, onClick = { playHaptic(); onMemorySubtract() })
            MemoryButton(label = "MS", enabled = true, onClick = { playHaptic(); onMemoryStore() })

            // Scientific Mode Toggle Button
            Surface(
                onClick = { playHaptic(); onToggleScientific() },
                shape = RoundedCornerShape(16.dp),
                color = if (isScientificMode) MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(
                    1.dp,
                    if (isScientificMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .weight(1.3f)
                    .height(44.dp)
                    .testTag("toggle_scientific_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isScientificMode) Icons.Default.Calculate else Icons.Default.Functions,
                        contentDescription = "Toggle Scientific Keypad",
                        tint = if (isScientificMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isScientificMode) "Basic" else "Sci",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isScientificMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Scientific keypad expansion (Animated)
        AnimatedVisibility(
            visible = isScientificMode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            ScientificKeyGrid(
                onScientificFunc = { func -> playHaptic(); onScientificFunc(func) }
            )
        }

        // Standard 4x5 Keypad Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: AC, (), %, ÷
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcKey(
                    label = "AC",
                    type = KeyType.ACTION,
                    onClick = { playHaptic(); onClearAll() },
                    modifier = Modifier.weight(1f)
                )
                CalcKey(
                    label = "( )",
                    type = KeyType.ACTION,
                    onClick = { playHaptic(); onParenthesis() },
                    modifier = Modifier.weight(1f)
                )
                CalcKey(
                    label = "%",
                    type = KeyType.ACTION,
                    onClick = { playHaptic(); onPercentage() },
                    modifier = Modifier.weight(1f)
                )
                CalcKey(
                    label = "÷",
                    type = KeyType.OPERATOR,
                    onClick = { playHaptic(); onOperator("÷") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: 7, 8, 9, ×
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcKey(label = "7", type = KeyType.NUMBER, onClick = { playHaptic(); onDigit("7") }, modifier = Modifier.weight(1f))
                CalcKey(label = "8", type = KeyType.NUMBER, onClick = { playHaptic(); onDigit("8") }, modifier = Modifier.weight(1f))
                CalcKey(label = "9", type = KeyType.NUMBER, onClick = { playHaptic(); onDigit("9") }, modifier = Modifier.weight(1f))
                CalcKey(label = "×", type = KeyType.OPERATOR, onClick = { playHaptic(); onOperator("×") }, modifier = Modifier.weight(1f))
            }

            // Row 3: 4, 5, 6, −
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcKey(label = "4", type = KeyType.NUMBER, onClick = { playHaptic(); onDigit("4") }, modifier = Modifier.weight(1f))
                CalcKey(label = "5", type = KeyType.NUMBER, onClick = { playHaptic(); onDigit("5") }, modifier = Modifier.weight(1f))
                CalcKey(label = "6", type = KeyType.NUMBER, onClick = { playHaptic(); onDigit("6") }, modifier = Modifier.weight(1f))
                CalcKey(label = "−", type = KeyType.OPERATOR, onClick = { playHaptic(); onOperator("−") }, modifier = Modifier.weight(1f))
            }

            // Row 4: 1, 2, 3, +
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcKey(label = "1", type = KeyType.NUMBER, onClick = { playHaptic(); onDigit("1") }, modifier = Modifier.weight(1f))
                CalcKey(label = "2", type = KeyType.NUMBER, onClick = { playHaptic(); onDigit("2") }, modifier = Modifier.weight(1f))
                CalcKey(label = "3", type = KeyType.NUMBER, onClick = { playHaptic(); onDigit("3") }, modifier = Modifier.weight(1f))
                CalcKey(label = "+", type = KeyType.OPERATOR, onClick = { playHaptic(); onOperator("+") }, modifier = Modifier.weight(1f))
            }

            // Row 5: +/−, 0, ., =, and Backspace integrated
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcKey(
                    label = "+/−",
                    type = KeyType.NUMBER,
                    onClick = { playHaptic(); onToggleSign() },
                    modifier = Modifier.weight(0.9f)
                )
                CalcKey(
                    label = "0",
                    type = KeyType.NUMBER,
                    onClick = { playHaptic(); onDigit("0") },
                    modifier = Modifier.weight(1.1f)
                )
                CalcKey(
                    label = ".",
                    type = KeyType.NUMBER,
                    onClick = { playHaptic(); onDecimal() },
                    modifier = Modifier.weight(0.9f)
                )
                // Backspace icon key
                Surface(
                    onClick = { playHaptic(); onDelete() },
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(64.dp)
                        .testTag("btn_backspace")
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Delete last character",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                // Equals key
                CalcKey(
                    label = "=",
                    type = KeyType.EQUALS,
                    onClick = { playHaptic(); onEqual() },
                    modifier = Modifier.weight(1.2f)
                )
            }
        }
    }
}

@Composable
fun ScientificKeyGrid(
    onScientificFunc: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: sin, cos, tan, π, e
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SciKey(label = "sin", onClick = { onScientificFunc("sin") }, modifier = Modifier.weight(1f))
            SciKey(label = "cos", onClick = { onScientificFunc("cos") }, modifier = Modifier.weight(1f))
            SciKey(label = "tan", onClick = { onScientificFunc("tan") }, modifier = Modifier.weight(1f))
            SciKey(label = "π", onClick = { onScientificFunc("pi") }, modifier = Modifier.weight(1f))
            SciKey(label = "e", onClick = { onScientificFunc("e") }, modifier = Modifier.weight(1f))
        }

        // Row 2: sin⁻¹, cos⁻¹, tan⁻¹, x², x³
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SciKey(label = "sin⁻¹", onClick = { onScientificFunc("asin") }, modifier = Modifier.weight(1f))
            SciKey(label = "cos⁻¹", onClick = { onScientificFunc("acos") }, modifier = Modifier.weight(1f))
            SciKey(label = "tan⁻¹", onClick = { onScientificFunc("atan") }, modifier = Modifier.weight(1f))
            SciKey(label = "x²", onClick = { onScientificFunc("sqr") }, modifier = Modifier.weight(1f))
            SciKey(label = "x³", onClick = { onScientificFunc("cube") }, modifier = Modifier.weight(1f))
        }

        // Row 3: √, ∛, xʸ, 10ˣ, eˣ
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SciKey(label = "√", onClick = { onScientificFunc("sqrt") }, modifier = Modifier.weight(1f))
            SciKey(label = "∛", onClick = { onScientificFunc("cbrt") }, modifier = Modifier.weight(1f))
            SciKey(label = "xʸ", onClick = { onScientificFunc("pow") }, modifier = Modifier.weight(1f))
            SciKey(label = "10ˣ", onClick = { onScientificFunc("10pow") }, modifier = Modifier.weight(1f))
            SciKey(label = "eˣ", onClick = { onScientificFunc("epow") }, modifier = Modifier.weight(1f))
        }

        // Row 4: ln, log, n!, 1/x, |x|
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SciKey(label = "ln", onClick = { onScientificFunc("ln") }, modifier = Modifier.weight(1f))
            SciKey(label = "log", onClick = { onScientificFunc("log") }, modifier = Modifier.weight(1f))
            SciKey(label = "n!", onClick = { onScientificFunc("fact") }, modifier = Modifier.weight(1f))
            SciKey(label = "1/x", onClick = { onScientificFunc("inv") }, modifier = Modifier.weight(1f))
            SciKey(label = "|x|", onClick = { onScientificFunc("abs") }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun RowScope.MemoryButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        color = if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .testTag("btn_mem_${label.lowercase()}")
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            )
        }
    }
}

@Composable
fun SciKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = modifier
            .height(44.dp)
            .testTag("btn_sci_${label.replace(" ", "_")}")
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CalcKey(
    label: String,
    type: KeyType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp
) {
    val containerColor = when (type) {
        KeyType.NUMBER -> MaterialTheme.colorScheme.surfaceVariant
        KeyType.OPERATOR -> MaterialTheme.colorScheme.primaryContainer
        KeyType.ACTION -> MaterialTheme.colorScheme.surface
        KeyType.EQUALS -> MaterialTheme.colorScheme.primary
        KeyType.SCIENTIFIC -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (type) {
        KeyType.NUMBER -> MaterialTheme.colorScheme.onSurface
        KeyType.OPERATOR -> MaterialTheme.colorScheme.onPrimaryContainer
        KeyType.ACTION -> MaterialTheme.colorScheme.secondary
        KeyType.EQUALS -> MaterialTheme.colorScheme.onPrimary
        KeyType.SCIENTIFIC -> MaterialTheme.colorScheme.tertiary
    }

    val fontSize = when {
        type == KeyType.ACTION && label.length > 2 -> 18.sp
        type == KeyType.OPERATOR || type == KeyType.EQUALS -> 28.sp
        else -> 24.sp
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        border = BorderStroke(
            1.dp,
            if (type == KeyType.EQUALS) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ),
        tonalElevation = if (type == KeyType.EQUALS) 4.dp else 1.dp,
        modifier = modifier
            .height(height)
            .testTag("btn_${label.replace(" ", "_")}")
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = fontSize,
                fontWeight = if (type == KeyType.NUMBER) FontWeight.Normal else FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
