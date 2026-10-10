package com.example.calculator.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.data.AppThemeMode
import com.example.calculator.data.CalculationHistory
import com.example.calculator.data.CalculatorDatabase
import com.example.calculator.data.CalculatorPreferences
import com.example.calculator.data.HistoryRepository
import com.example.calculator.data.UserSettings
import com.example.calculator.engine.AngleMode
import com.example.calculator.engine.EvalResult
import com.example.calculator.engine.MathEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val expression: String = "",
    val result: String = "0",
    val livePreview: String? = null,
    val isEvaluated: Boolean = false,
    val errorMessage: String? = null,
    val isScientificMode: Boolean = false,
    val memoryValue: Double? = null,
    val searchQuery: String = "",
    val isHistoryOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val copyFeedbackTrigger: Long = 0L,
    val userSettings: UserSettings = UserSettings()
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val mathEngine = MathEngine()
    private val repository: HistoryRepository
    private val preferences = CalculatorPreferences(application)

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        val database = CalculatorDatabase.getDatabase(application)
        repository = HistoryRepository(database.calculationDao())

        // Collect preferences
        viewModelScope.launch {
            preferences.settings.collect { settings ->
                _uiState.value = _uiState.value.copy(userSettings = settings)
                updateLivePreview()
            }
        }
    }

    val historyList: StateFlow<List<CalculationHistory>> = combine(
        repository.allHistory,
        _searchQuery
    ) { all, query ->
        if (query.isBlank()) {
            all
        } else {
            all.filter {
                it.expression.contains(query, ignoreCase = true) ||
                        it.result.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Calculator Actions ---

    fun onDigit(digit: String) {
        val current = _uiState.value
        val newExpr = if (current.isEvaluated) {
            digit
        } else {
            current.expression + digit
        }
        _uiState.value = current.copy(
            expression = newExpr,
            isEvaluated = false,
            errorMessage = null
        )
        updateLivePreview()
    }

    fun onDecimal() {
        val current = _uiState.value
        val expr = if (current.isEvaluated) "0." else current.expression

        if (expr.isEmpty()) {
            _uiState.value = current.copy(expression = "0.", isEvaluated = false, errorMessage = null)
            updateLivePreview()
            return
        }

        // Check last token
        val lastNumber = expr.takeLastWhile { it.isDigit() || it == '.' }
        if (!lastNumber.contains('.')) {
            val toAppend = if (lastNumber.isEmpty() && (expr.lastOrNull()?.isDigit() == false && expr.lastOrNull() != ')')) {
                "0."
            } else {
                "."
            }
            _uiState.value = current.copy(
                expression = expr + toAppend,
                isEvaluated = false,
                errorMessage = null
            )
            updateLivePreview()
        }
    }

    fun onOperator(op: String) {
        val current = _uiState.value
        var expr = current.expression

        if (current.isEvaluated) {
            // Continuous calculation: use previous result
            expr = if (current.errorMessage == null && current.result.isNotBlank()) {
                current.result.replace(",", "")
            } else {
                "0"
            }
        }

        if (expr.isEmpty()) {
            if (op == "−" || op == "-") {
                _uiState.value = current.copy(expression = "−", isEvaluated = false, errorMessage = null)
            }
            return
        }

        val lastChar = expr.last()
        // If last char is an operator, replace it (unless minus for negative)
        val isLastOp = lastChar in listOf('+', '−', '-', '×', '*', '÷', '/')
        val newExpr = if (isLastOp) {
            expr.dropLast(1) + op
        } else {
            expr + op
        }

        _uiState.value = current.copy(
            expression = newExpr,
            isEvaluated = false,
            errorMessage = null
        )
        updateLivePreview()
    }

    fun onParenthesis() {
        val current = _uiState.value
        val expr = if (current.isEvaluated) "" else current.expression

        val openCount = expr.count { it == '(' }
        val closeCount = expr.count { it == ')' }

        val lastChar = expr.lastOrNull()
        val toAppend = when {
            expr.isEmpty() || lastChar in listOf('+', '−', '-', '×', '*', '÷', '/', '(', '^') -> "("
            openCount > closeCount && (lastChar?.isDigit() == true || lastChar == ')') -> ")"
            else -> "×("
        }

        _uiState.value = current.copy(
            expression = expr + toAppend,
            isEvaluated = false,
            errorMessage = null
        )
        updateLivePreview()
    }

    fun onPercentage() {
        val current = _uiState.value
        val expr = if (current.isEvaluated) current.result.replace(",", "") else current.expression
        if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')')) {
            _uiState.value = current.copy(
                expression = "$expr%",
                isEvaluated = false,
                errorMessage = null
            )
            updateLivePreview()
        }
    }

    fun onToggleSign() {
        val current = _uiState.value
        var expr = if (current.isEvaluated) current.result.replace(",", "") else current.expression
        if (expr.isEmpty()) return

        // Toggle sign of last number segment
        var i = expr.length - 1
        while (i >= 0 && (expr[i].isDigit() || expr[i] == '.')) {
            i--
        }

        val prefix = expr.substring(0, i + 1)
        val num = expr.substring(i + 1)

        val newExpr = when {
            num.isNotEmpty() -> {
                if (prefix.endsWith("−") || prefix.endsWith("-")) {
                    prefix.dropLast(1) + num
                } else if (prefix.endsWith("+")) {
                    prefix.dropLast(1) + "−" + num
                } else {
                    prefix + "(-" + num + ")"
                }
            }
            expr.startsWith("(-") && expr.endsWith(")") -> {
                expr.substring(2, expr.length - 1)
            }
            else -> {
                "(-$expr)"
            }
        }

        _uiState.value = current.copy(
            expression = newExpr,
            isEvaluated = false,
            errorMessage = null
        )
        updateLivePreview()
    }

    fun onClearAll() {
        _uiState.value = _uiState.value.copy(
            expression = "",
            result = "0",
            livePreview = null,
            isEvaluated = false,
            errorMessage = null
        )
    }

    fun onDelete() {
        val current = _uiState.value
        if (current.isEvaluated) {
            onClearAll()
            return
        }
        if (current.expression.isNotEmpty()) {
            val newExpr = current.expression.dropLast(1)
            _uiState.value = current.copy(
                expression = newExpr,
                errorMessage = null
            )
            updateLivePreview()
        }
    }

    fun onScientificFunction(func: String) {
        val current = _uiState.value
        val expr = if (current.isEvaluated) "" else current.expression

        val toAppend = when (func) {
            "sin" -> "sin("
            "cos" -> "cos("
            "tan" -> "tan("
            "asin" -> "asin("
            "acos" -> "acos("
            "atan" -> "atan("
            "ln" -> "ln("
            "log" -> "log("
            "sqrt" -> "√("
            "cbrt" -> "∛("
            "abs" -> "abs("
            "sqr" -> "²"
            "cube" -> "³"
            "pow" -> "^"
            "fact" -> "!"
            "pi" -> "π"
            "e" -> "e"
            "inv" -> "^(-1)"
            "10pow" -> "10^("
            "epow" -> "e^("
            else -> "$func("
        }

        // Implicit multiply if appending a function or constant after a digit or ')'
        val finalAppend = if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')') &&
            (toAppend.startsWith("s") || toAppend.startsWith("c") || toAppend.startsWith("t") ||
                    toAppend.startsWith("a") || toAppend.startsWith("l") || toAppend.startsWith("√") ||
                    toAppend.startsWith("∛") || toAppend.startsWith("π") || toAppend.startsWith("e"))
        ) {
            "×$toAppend"
        } else {
            toAppend
        }

        _uiState.value = current.copy(
            expression = expr + finalAppend,
            isEvaluated = false,
            errorMessage = null
        )
        updateLivePreview()
    }

    fun onEqual() {
        val current = _uiState.value
        val expr = current.expression.trim()
        if (expr.isEmpty()) return

        val settings = current.userSettings
        val eval = mathEngine.evaluate(
            expression = expr,
            angleMode = settings.angleMode,
            decimalPlaces = settings.decimalPrecision,
            useGrouping = settings.useGrouping
        )

        when (eval) {
            is EvalResult.Success -> {
                _uiState.value = current.copy(
                    result = eval.formatted,
                    livePreview = null,
                    isEvaluated = true,
                    errorMessage = null
                )
                // Persist calculation in Room database
                viewModelScope.launch {
                    repository.insertCalculation(expr, eval.formatted)
                }
            }
            is EvalResult.Error -> {
                _uiState.value = current.copy(
                    errorMessage = eval.message,
                    livePreview = null,
                    isEvaluated = true
                )
            }
        }
    }

    // --- Memory Functions ---

    fun onMemoryClear() {
        _uiState.value = _uiState.value.copy(memoryValue = null)
    }

    fun onMemoryRecall() {
        val memory = _uiState.value.memoryValue ?: return
        val formattedMem = mathEngine.formatResult(
            memory,
            _uiState.value.userSettings.decimalPrecision,
            false
        )
        val current = _uiState.value
        val expr = if (current.isEvaluated) formattedMem else current.expression + formattedMem
        _uiState.value = current.copy(expression = expr, isEvaluated = false, errorMessage = null)
        updateLivePreview()
    }

    fun onMemoryAdd() {
        getCurrentNumericValue()?.let { currentVal ->
            val prev = _uiState.value.memoryValue ?: 0.0
            _uiState.value = _uiState.value.copy(memoryValue = prev + currentVal)
        }
    }

    fun onMemorySubtract() {
        getCurrentNumericValue()?.let { currentVal ->
            val prev = _uiState.value.memoryValue ?: 0.0
            _uiState.value = _uiState.value.copy(memoryValue = prev - currentVal)
        }
    }

    fun onMemoryStore() {
        getCurrentNumericValue()?.let { currentVal ->
            _uiState.value = _uiState.value.copy(memoryValue = currentVal)
        }
    }

    private fun getCurrentNumericValue(): Double? {
        val current = _uiState.value
        val target = if (current.isEvaluated && current.errorMessage == null) {
            current.result.replace(",", "")
        } else if (current.livePreview != null) {
            current.livePreview.replace(",", "")
        } else {
            current.expression
        }

        return when (val eval = mathEngine.evaluate(target, _uiState.value.userSettings.angleMode)) {
            is EvalResult.Success -> eval.value
            is EvalResult.Error -> null
        }
    }

    // --- UI Controls & Navigation ---

    fun toggleScientificMode() {
        _uiState.value = _uiState.value.copy(isScientificMode = !_uiState.value.isScientificMode)
    }

    fun toggleAngleMode() {
        val currentAngle = _uiState.value.userSettings.angleMode
        val newAngle = if (currentAngle == AngleMode.DEGREE) AngleMode.RADIAN else AngleMode.DEGREE
        preferences.setAngleMode(newAngle)
    }

    fun setDecimalPrecision(precision: Int) {
        preferences.setDecimalPrecision(precision)
    }

    fun toggleGrouping() {
        preferences.setUseGrouping(!_uiState.value.userSettings.useGrouping)
    }

    fun toggleHaptics() {
        preferences.setHapticFeedback(!_uiState.value.userSettings.hapticFeedback)
    }

    fun setThemeMode(mode: AppThemeMode) {
        preferences.setThemeMode(mode)
    }

    fun setHistoryOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isHistoryOpen = open)
    }

    fun setSettingsOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isSettingsOpen = open)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun useHistoryExpression(expr: String) {
        _uiState.value = _uiState.value.copy(
            expression = expr,
            isEvaluated = false,
            errorMessage = null,
            isHistoryOpen = false
        )
        updateLivePreview()
    }

    fun useHistoryResult(result: String) {
        val current = _uiState.value
        val cleanResult = result.replace(",", "")
        val newExpr = if (current.isEvaluated || current.expression.isEmpty()) {
            cleanResult
        } else {
            current.expression + cleanResult
        }
        _uiState.value = current.copy(
            expression = newExpr,
            isEvaluated = false,
            errorMessage = null,
            isHistoryOpen = false
        )
        updateLivePreview()
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun toggleHistoryBookmark(id: Long, currentBookmark: Boolean) {
        viewModelScope.launch {
            repository.toggleBookmark(id, currentBookmark)
        }
    }

    fun triggerCopyFeedback() {
        _uiState.value = _uiState.value.copy(copyFeedbackTrigger = System.currentTimeMillis())
    }

    private fun updateLivePreview() {
        val expr = _uiState.value.expression
        if (expr.isBlank() || _uiState.value.isEvaluated) {
            _uiState.value = _uiState.value.copy(livePreview = null)
            return
        }

        val settings = _uiState.value.userSettings
        val preview = mathEngine.evaluatePreview(
            expression = expr,
            angleMode = settings.angleMode,
            decimalPlaces = settings.decimalPrecision,
            useGrouping = settings.useGrouping
        )
        _uiState.value = _uiState.value.copy(livePreview = preview)
    }
}
