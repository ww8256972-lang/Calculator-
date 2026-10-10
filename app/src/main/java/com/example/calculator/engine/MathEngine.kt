package com.example.calculator.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class AngleMode {
    DEGREE,
    RADIAN
}

sealed class EvalResult {
    data class Success(val value: Double, val formatted: String) : EvalResult()
    data class Error(val message: String) : EvalResult()
}

/**
 * Robust mathematical expression parser and evaluator.
 * Safe, zero-eval, supports arithmetic, percentage, brackets, scientific functions,
 * degree/radian trigonometry, and continuous calculations.
 */
class MathEngine {

    companion object {
        const val PI_VALUE = PI
        const val E_VALUE = Math.E
        private const val EPSILON = 1e-12
    }

    /**
     * Evaluates a mathematical expression string.
     * @param expression The mathematical expression (e.g. "25 × 4", "2 + 3 × 4", "sin(30)")
     * @param angleMode DEGREE or RADIAN for trigonometric calculations
     * @param decimalPlaces Number of decimal places to format to (-1 for automatic clean formatting)
     * @param useGrouping Whether to use thousand separators in formatted output
     */
    fun evaluate(
        expression: String,
        angleMode: AngleMode = AngleMode.DEGREE,
        decimalPlaces: Int = -1,
        useGrouping: Boolean = true
    ): EvalResult {
        val sanitized = sanitize(expression)
        if (sanitized.isBlank()) {
            return EvalResult.Success(0.0, "0")
        }

        return try {
            val parser = ExpressionParser(sanitized, angleMode)
            val value = parser.parse()

            if (value.isNaN()) {
                EvalResult.Error("Result is undefined")
            } else if (value.isInfinite()) {
                if (value > 0) EvalResult.Error("Value too large (Infinity)")
                else EvalResult.Error("Value too small (-Infinity)")
            } else {
                val formatted = formatResult(value, decimalPlaces, useGrouping)
                EvalResult.Success(value, formatted)
            }
        } catch (e: ArithmeticException) {
            EvalResult.Error(e.message ?: "Math error")
        } catch (e: IllegalArgumentException) {
            EvalResult.Error(e.message ?: "Invalid expression")
        } catch (e: Exception) {
            EvalResult.Error("Invalid expression")
        }
    }

    /**
     * Attempts a live evaluation of the expression as the user types.
     * Returns null if incomplete or invalid, so preview won't display annoying red errors mid-typing.
     */
    fun evaluatePreview(
        expression: String,
        angleMode: AngleMode = AngleMode.DEGREE,
        decimalPlaces: Int = -1,
        useGrouping: Boolean = true
    ): String? {
        val clean = sanitize(expression)
        if (clean.isBlank()) return null

        // Try auto-closing open parentheses for preview
        val openCount = clean.count { it == '(' }
        val closeCount = clean.count { it == ')' }
        val closed = if (openCount > closeCount) {
            clean + ")".repeat(openCount - closeCount)
        } else {
            clean
        }

        return when (val res = evaluate(closed, angleMode, decimalPlaces, useGrouping)) {
            is EvalResult.Success -> res.formatted
            is EvalResult.Error -> null
        }
    }

    /**
     * Sanitizes user expression replacing UI symbols with standard parser tokens.
     */
    fun sanitize(input: String): String {
        return input
            .replace("×", "*")
            .replace("·", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("–", "-")
            .replace("π", "PI")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .trim()
    }

    /**
     * Formats double result with specified precision and grouping,
     * stripping unnecessary trailing zeroes, handling scientific notation for extreme numbers.
     */
    fun formatResult(value: Double, decimalPlaces: Int = -1, useGrouping: Boolean = true): String {
        // Zero check
        if (abs(value) < EPSILON) return "0"

        // For extremely large or small numbers, use scientific notation
        if (abs(value) >= 1e14 || (abs(value) < 1e-6 && abs(value) > 0)) {
            val df = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
            return df.format(value)
        }

        val symbols = DecimalFormatSymbols(Locale.US)
        if (!useGrouping) {
            symbols.groupingSeparator = '\u0000'
        }

        val pattern = when {
            decimalPlaces >= 0 -> {
                val decimals = "0".repeat(decimalPlaces)
                if (useGrouping) "#,##0.$decimals" else "0.$decimals"
            }
            else -> {
                // Auto precision: up to 10 decimal digits, clean trailing zeros
                if (useGrouping) "#,##0.##########" else "0.##########"
            }
        }

        return try {
            val bd = BigDecimal.valueOf(value).setScale(
                if (decimalPlaces >= 0) decimalPlaces else 10,
                RoundingMode.HALF_UP
            )
            val df = DecimalFormat(pattern, symbols)
            df.isGroupingUsed = useGrouping
            var str = df.format(bd)
            if (decimalPlaces < 0 && str.contains(".")) {
                str = str.trimEnd('0').trimEnd('.')
            }
            str
        } catch (e: Exception) {
            val df = DecimalFormat(pattern, symbols)
            df.format(value)
        }
    }

    /**
     * Safe factorial computation
     */
    fun factorial(n: Double): Double {
        if (n < 0 || n != kotlin.math.floor(n)) {
            throw ArithmeticException("Factorial is only defined for non-negative integers")
        }
        if (n > 170) {
            throw ArithmeticException("Value too large for factorial")
        }
        val intN = n.roundToLong()
        var result = 1.0
        for (i in 2..intN) {
            result *= i
        }
        return result
    }
}

/**
 * Tokenizer & Recursive Descent Parser for expressions.
 * Handles operator precedence, functions, parentheses, percentages, constants, and implicit multiplication.
 */
private class ExpressionParser(
    private val text: String,
    private val angleMode: AngleMode
) {
    private var pos = 0
    private var ch: Char = ' '

    private fun nextChar() {
        ch = if (++pos < text.length) text[pos] else '\u0000'
    }

    private fun peekChar(): Char {
        val nextPos = pos + 1
        return if (nextPos < text.length) text[nextPos] else '\u0000'
    }

    private fun eat(charToEat: Char): Boolean {
        while (ch == ' ') nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    fun parse(): Double {
        pos = -1
        nextChar()
        val x = parseExpression()
        while (ch == ' ') nextChar()
        if (pos < text.length && ch != '\u0000') {
            throw IllegalArgumentException("Unexpected character: '$ch'")
        }
        return x
    }

    // Grammar:
    // Expression = Term | Expression `+` Term | Expression `-` Term (with percentage support)
    // Term = Factor | Term `*` Factor | Term `/` Factor
    // Factor = `+` Factor | `-` Factor | Power
    // Power = Base `^` Factor
    // Postfix = Primary (`!` | `%` | `²` | `³`)*
    // Primary = Number | Constant | `(` Expression `)` | Function `(` Expression `)`

    private var lastWasPercentage = false

    private fun parseExpression(): Double {
        var x = parseTerm()
        while (true) {
            while (ch == ' ') nextChar()
            when {
                eat('+') -> {
                    lastWasPercentage = false
                    val nextTerm = parseTerm()
                    if (lastWasPercentage) {
                        x += x * nextTerm
                    } else {
                        x += nextTerm
                    }
                }
                eat('-') -> {
                    lastWasPercentage = false
                    val nextTerm = parseTerm()
                    if (lastWasPercentage) {
                        x -= x * nextTerm
                    } else {
                        x -= nextTerm
                    }
                }
                else -> return x
            }
        }
    }

    private fun parseTerm(): Double {
        var x = parsePower()
        while (true) {
            while (ch == ' ') nextChar()
            when {
                eat('*') -> {
                    val nextTerm = parsePower()
                    x *= nextTerm
                }
                eat('/') -> {
                    val nextTerm = parsePower()
                    if (abs(nextTerm) < 1e-15) {
                        throw ArithmeticException("Cannot divide by zero")
                    }
                    x /= nextTerm
                }
                // Implicit multiplication: e.g. 2(3), 5sin(30), 2PI
                ch == '(' || ch.isLetter() -> {
                    val nextTerm = parsePower()
                    x *= nextTerm
                }
                else -> return x
            }
        }
    }

    private fun parsePower(): Double {
        var x = parseUnary()
        while (true) {
            while (ch == ' ') nextChar()
            if (eat('^')) {
                val exponent = parseUnary()
                x = x.pow(exponent)
            } else {
                return x
            }
        }
    }

    private fun parseUnary(): Double {
        while (ch == ' ') nextChar()
        if (eat('+')) return parseUnary()
        if (eat('-')) return -parseUnary()
        return parsePostfix()
    }

    private fun parsePostfix(): Double {
        var x = parsePrimary()
        while (true) {
            while (ch == ' ') nextChar()
            when {
                eat('!') -> {
                    x = MathEngine().factorial(x)
                }
                eat('%') -> {
                    lastWasPercentage = true
                    x /= 100.0
                }
                eat('²') -> {
                    x = x * x
                }
                eat('³') -> {
                    x = x * x * x
                }
                else -> return x
            }
        }
    }

    private fun parsePrimary(): Double {
        while (ch == ' ') nextChar()

        val startPos = pos
        if (eat('(')) {
            val x = parseExpression()
            if (!eat(')')) {
                throw IllegalArgumentException("Missing closing parenthesis")
            }
            return x
        }

        // Numbers
        if ((ch in '0'..'9') || ch == '.') {
            while ((ch in '0'..'9') || ch == '.') nextChar()
            // Optional scientific notation within number like 1.5e3
            if (ch == 'E' || ch == 'e') {
                val peek = peekChar()
                if (peek == '+' || peek == '-' || (peek in '0'..'9')) {
                    nextChar()
                    if (ch == '+' || ch == '-') nextChar()
                    while (ch in '0'..'9') nextChar()
                }
            }
            val numStr = text.substring(startPos, pos)
            return numStr.toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid number: $numStr")
        }

        // Identifiers: functions & constants
        if (ch.isLetter()) {
            while (ch.isLetter() || ch in '0'..'9') nextChar()
            val func = text.substring(startPos, pos).lowercase(Locale.US)

            return when (func) {
                "pi" -> MathEngine.PI_VALUE
                "e" -> MathEngine.E_VALUE
                "sqrt" -> {
                    val arg = parseArg()
                    if (arg < 0) throw ArithmeticException("Cannot take square root of negative number")
                    sqrt(arg)
                }
                "cbrt" -> {
                    cbrt(parseArg())
                }
                "sin" -> {
                    val arg = parseArg()
                    val rad = toRadians(arg)
                    // Check near 180 degrees
                    if (angleMode == AngleMode.DEGREE && abs(arg % 180.0) < 1e-10) 0.0 else sin(rad)
                }
                "cos" -> {
                    val arg = parseArg()
                    val rad = toRadians(arg)
                    // Check near 90 or 270 degrees
                    if (angleMode == AngleMode.DEGREE && abs((arg - 90.0) % 180.0) < 1e-10) 0.0 else cos(rad)
                }
                "tan" -> {
                    val arg = parseArg()
                    if (angleMode == AngleMode.DEGREE && abs((arg - 90.0) % 180.0) < 1e-10) {
                        throw ArithmeticException("tan(${arg}°) is undefined")
                    }
                    val rad = toRadians(arg)
                    tan(rad)
                }
                "asin" -> {
                    val arg = parseArg()
                    if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error: asin argument must be between -1 and 1")
                    fromRadians(asin(arg))
                }
                "acos" -> {
                    val arg = parseArg()
                    if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error: acos argument must be between -1 and 1")
                    fromRadians(acos(arg))
                }
                "atan" -> {
                    fromRadians(atan(parseArg()))
                }
                "log", "log10" -> {
                    val arg = parseArg()
                    if (arg <= 0) throw ArithmeticException("Domain error: log of non-positive number")
                    log10(arg)
                }
                "ln" -> {
                    val arg = parseArg()
                    if (arg <= 0) throw ArithmeticException("Domain error: ln of non-positive number")
                    ln(arg)
                }
                "exp" -> {
                    exp(parseArg())
                }
                "abs" -> {
                    abs(parseArg())
                }
                else -> throw IllegalArgumentException("Unknown function: $func")
            }
        }

        throw IllegalArgumentException("Unexpected token: '$ch'")
    }

    private fun parseArg(): Double {
        while (ch == ' ') nextChar()
        if (eat('(')) {
            val x = parseExpression()
            if (!eat(')')) throw IllegalArgumentException("Missing closing parenthesis")
            return x
        }
        return parseUnary()
    }

    private fun toRadians(angle: Double): Double =
        if (angleMode == AngleMode.DEGREE) Math.toRadians(angle) else angle

    private fun fromRadians(rad: Double): Double =
        if (angleMode == AngleMode.DEGREE) Math.toDegrees(rad) else rad
}
