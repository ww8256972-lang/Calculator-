package com.example.calculator.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculator.data.AppThemeMode
import com.example.ui.theme.DarkAccentCyan
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkOperatorKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val historyList by viewModel.historyList.collectAsState()

    BackHandler(enabled = uiState.isHistoryOpen || uiState.isSettingsOpen) {
        if (uiState.isHistoryOpen) viewModel.setHistoryOpen(false)
        if (uiState.isSettingsOpen) viewModel.setSettingsOpen(false)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("calculator_screen"),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Waqar Calculator",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            val newMode = if (uiState.userSettings.themeMode == AppThemeMode.DARK) {
                                AppThemeMode.LIGHT
                            } else {
                                AppThemeMode.DARK
                            }
                            viewModel.setThemeMode(newMode)
                        },
                        modifier = Modifier.testTag("quick_theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.userSettings.themeMode == AppThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme mode",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // History Button with Badge
                    IconButton(
                        onClick = { viewModel.setHistoryOpen(true) },
                        modifier = Modifier.testTag("open_history_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (historyList.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ) {
                                        Text(text = if (historyList.size > 99) "99+" else historyList.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Calculation history",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Settings Button
                    IconButton(
                        onClick = { viewModel.setSettingsOpen(true) },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Calculator settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isTabletOrLandscape = maxWidth > 640.dp

            if (isTabletOrLandscape) {
                // Wide / Tablet Layout: Dual-column layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Left Column: Display and Scientific functions
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CalculatorDisplay(
                            expression = uiState.expression,
                            result = uiState.result,
                            livePreview = uiState.livePreview,
                            errorMessage = uiState.errorMessage,
                            isEvaluated = uiState.isEvaluated,
                            memoryValue = uiState.memoryValue,
                            angleMode = uiState.userSettings.angleMode,
                            onAngleToggle = { viewModel.toggleAngleMode() },
                            onMemoryClick = { viewModel.onMemoryRecall() },
                            onCopyFeedback = { viewModel.triggerCopyFeedback() },
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "Scientific Functions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        ScientificKeyGrid(
                            onScientificFunc = { func -> viewModel.onScientificFunction(func) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Right Column: Keypad
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        CalculatorKeypad(
                            isScientificMode = false, // already rendered side-by-side
                            onToggleScientific = { viewModel.toggleScientificMode() },
                            onDigit = { viewModel.onDigit(it) },
                            onDecimal = { viewModel.onDecimal() },
                            onOperator = { viewModel.onOperator(it) },
                            onParenthesis = { viewModel.onParenthesis() },
                            onPercentage = { viewModel.onPercentage() },
                            onToggleSign = { viewModel.onToggleSign() },
                            onClearAll = { viewModel.onClearAll() },
                            onDelete = { viewModel.onDelete() },
                            onEqual = { viewModel.onEqual() },
                            onScientificFunc = { viewModel.onScientificFunction(it) },
                            onMemoryClear = { viewModel.onMemoryClear() },
                            onMemoryRecall = { viewModel.onMemoryRecall() },
                            onMemoryAdd = { viewModel.onMemoryAdd() },
                            onMemorySubtract = { viewModel.onMemorySubtract() },
                            onMemoryStore = { viewModel.onMemoryStore() },
                            hasMemoryValue = uiState.memoryValue != null,
                            hapticEnabled = uiState.userSettings.hapticFeedback,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // Mobile Portrait Layout: Stacked display and adaptive keypad
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CalculatorDisplay(
                        expression = uiState.expression,
                        result = uiState.result,
                        livePreview = uiState.livePreview,
                        errorMessage = uiState.errorMessage,
                        isEvaluated = uiState.isEvaluated,
                        memoryValue = uiState.memoryValue,
                        angleMode = uiState.userSettings.angleMode,
                        onAngleToggle = { viewModel.toggleAngleMode() },
                        onMemoryClick = { viewModel.onMemoryRecall() },
                        onCopyFeedback = { viewModel.triggerCopyFeedback() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CalculatorKeypad(
                        isScientificMode = uiState.isScientificMode,
                        onToggleScientific = { viewModel.toggleScientificMode() },
                        onDigit = { viewModel.onDigit(it) },
                        onDecimal = { viewModel.onDecimal() },
                        onOperator = { viewModel.onOperator(it) },
                        onParenthesis = { viewModel.onParenthesis() },
                        onPercentage = { viewModel.onPercentage() },
                        onToggleSign = { viewModel.onToggleSign() },
                        onClearAll = { viewModel.onClearAll() },
                        onDelete = { viewModel.onDelete() },
                        onEqual = { viewModel.onEqual() },
                        onScientificFunc = { viewModel.onScientificFunction(it) },
                        onMemoryClear = { viewModel.onMemoryClear() },
                        onMemoryRecall = { viewModel.onMemoryRecall() },
                        onMemoryAdd = { viewModel.onMemoryAdd() },
                        onMemorySubtract = { viewModel.onMemorySubtract() },
                        onMemoryStore = { viewModel.onMemoryStore() },
                        hasMemoryValue = uiState.memoryValue != null,
                        hapticEnabled = uiState.userSettings.hapticFeedback,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // History Sheet
    if (uiState.isHistoryOpen) {
        HistorySheet(
            historyList = historyList,
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = { viewModel.setSearchQuery(it) },
            onUseExpression = { viewModel.useHistoryExpression(it) },
            onUseResult = { viewModel.useHistoryResult(it) },
            onDeleteItem = { viewModel.deleteHistoryItem(it) },
            onClearAll = { viewModel.clearAllHistory() },
            onToggleBookmark = { id, current -> viewModel.toggleHistoryBookmark(id, current) },
            onDismiss = { viewModel.setHistoryOpen(false) }
        )
    }

    // Settings Dialog
    if (uiState.isSettingsOpen) {
        SettingsDialog(
            settings = uiState.userSettings,
            onThemeChange = { viewModel.setThemeMode(it) },
            onAngleChange = { viewModel.toggleAngleMode() },
            onPrecisionChange = { viewModel.setDecimalPrecision(it) },
            onGroupingToggle = { viewModel.toggleGrouping() },
            onHapticsToggle = { viewModel.toggleHaptics() },
            onDismiss = { viewModel.setSettingsOpen(false) }
        )
    }
}
