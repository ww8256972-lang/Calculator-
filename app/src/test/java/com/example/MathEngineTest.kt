package com.example

import com.example.calculator.engine.AngleMode
import com.example.calculator.engine.EvalResult
import com.example.calculator.engine.MathEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MathEngineTest {

    private lateinit var engine: MathEngine

    @Before
    fun setUp() {
        engine = MathEngine()
    }

    @Test
    fun testBasicArithmeticOperations() {
        // 25 × 4 = 100
        val res1 = engine.evaluate("25 × 4")
        assertTrue(res1 is EvalResult.Success)
        assertEquals("100", (res1 as EvalResult.Success).formatted)
        assertEquals(100.0, res1.value, 1e-9)

        // 10 ÷ 4 = 2.5
        val res2 = engine.evaluate("10 ÷ 4")
        assertTrue(res2 is EvalResult.Success)
        assertEquals("2.5", (res2 as EvalResult.Success).formatted)
        assertEquals(2.5, res2.value, 1e-9)

        // 2 + 3 × 4 = 14 (precedence: multiplication before addition)
        val res3 = engine.evaluate("2 + 3 × 4")
        assertTrue(res3 is EvalResult.Success)
        assertEquals("14", (res3 as EvalResult.Success).formatted)
        assertEquals(14.0, res3.value, 1e-9)

        // 50 − 18.5 = 31.5
        val res4 = engine.evaluate("50 − 18.5")
        assertTrue(res4 is EvalResult.Success)
        assertEquals("31.5", (res4 as EvalResult.Success).formatted)
    }

    @Test
    fun testParenthesesAndPrecedence() {
        // (2 + 3) × 4 = 20
        val res1 = engine.evaluate("(2 + 3) × 4")
        assertTrue(res1 is EvalResult.Success)
        assertEquals("20", (res1 as EvalResult.Success).formatted)

        // (10 - 2) / (2 + 2) = 2
        val res2 = engine.evaluate("(10 - 2) / (2 + 2)")
        assertTrue(res2 is EvalResult.Success)
        assertEquals("2", (res2 as EvalResult.Success).formatted)

        // Nested parentheses: 2 * (3 + (4 * 2)) = 22
        val res3 = engine.evaluate("2 * (3 + (4 * 2))")
        assertTrue(res3 is EvalResult.Success)
        assertEquals("22", (res3 as EvalResult.Success).formatted)
    }

    @Test
    fun testPercentageCalculations() {
        // Simple percentage: 200 * 15% = 30
        val res1 = engine.evaluate("200 * 15%")
        assertTrue(res1 is EvalResult.Success)
        assertEquals("30", (res1 as EvalResult.Success).formatted)

        // Additive percentage: 100 + 20% = 120
        val res2 = engine.evaluate("100 + 20%")
        assertTrue(res2 is EvalResult.Success)
        assertEquals("120", (res2 as EvalResult.Success).formatted)

        // Subtractive percentage: 100 - 15% = 85
        val res3 = engine.evaluate("100 - 15%")
        assertTrue(res3 is EvalResult.Success)
        assertEquals("85", (res3 as EvalResult.Success).formatted)
    }

    @Test
    fun testScientificPowersAndRoots() {
        // Square: 5² = 25
        val res1 = engine.evaluate("5²")
        assertTrue(res1 is EvalResult.Success)
        assertEquals("25", (res1 as EvalResult.Success).formatted)

        // Cube: 3³ = 27
        val res2 = engine.evaluate("3³")
        assertTrue(res2 is EvalResult.Success)
        assertEquals("27", (res2 as EvalResult.Success).formatted)

        // Power: 2^10 = 1024
        val res3 = engine.evaluate("2^10")
        assertTrue(res3 is EvalResult.Success)
        assertEquals("1,024", (res3 as EvalResult.Success).formatted)

        // Square root: √(144) = 12
        val res4 = engine.evaluate("√(144)")
        assertTrue(res4 is EvalResult.Success)
        assertEquals("12", (res4 as EvalResult.Success).formatted)

        // Cube root: ∛(27) = 3
        val res5 = engine.evaluate("∛(27)")
        assertTrue(res5 is EvalResult.Success)
        assertEquals("3", (res5 as EvalResult.Success).formatted)
    }

    @Test
    fun testTrigonometricFunctions() {
        // Degrees: sin(30) = 0.5
        val res1 = engine.evaluate("sin(30)", AngleMode.DEGREE)
        assertTrue(res1 is EvalResult.Success)
        assertEquals(0.5, (res1 as EvalResult.Success).value, 1e-7)

        // Degrees: cos(0) = 1
        val res2 = engine.evaluate("cos(0)", AngleMode.DEGREE)
        assertTrue(res2 is EvalResult.Success)
        assertEquals(1.0, (res2 as EvalResult.Success).value, 1e-7)

        // Degrees: tan(45) = 1
        val res3 = engine.evaluate("tan(45)", AngleMode.DEGREE)
        assertTrue(res3 is EvalResult.Success)
        assertEquals(1.0, (res3 as EvalResult.Success).value, 1e-7)

        // Radians: sin(π / 2) = 1
        val res4 = engine.evaluate("sin(π / 2)", AngleMode.RADIAN)
        assertTrue(res4 is EvalResult.Success)
        assertEquals(1.0, (res4 as EvalResult.Success).value, 1e-7)
    }

    @Test
    fun testFactorialAndLogarithms() {
        // 5! = 120
        val res1 = engine.evaluate("5!")
        assertTrue(res1 is EvalResult.Success)
        assertEquals("120", (res1 as EvalResult.Success).formatted)

        // 0! = 1
        val res2 = engine.evaluate("0!")
        assertTrue(res2 is EvalResult.Success)
        assertEquals("1", (res2 as EvalResult.Success).formatted)

        // log(100) = 2
        val res3 = engine.evaluate("log(100)")
        assertTrue(res3 is EvalResult.Success)
        assertEquals("2", (res3 as EvalResult.Success).formatted)

        // ln(e) = 1
        val res4 = engine.evaluate("ln(e)")
        assertTrue(res4 is EvalResult.Success)
        assertEquals("1", (res4 as EvalResult.Success).formatted)
    }

    @Test
    fun testErrorHandling() {
        // Division by zero
        val res1 = engine.evaluate("10 ÷ 0")
        assertTrue(res1 is EvalResult.Error)
        assertEquals("Cannot divide by zero", (res1 as EvalResult.Error).message)

        // Square root of negative number
        val res2 = engine.evaluate("√(-9)")
        assertTrue(res2 is EvalResult.Error)

        // Malformed expression
        val res3 = engine.evaluate("2 + * 3")
        assertTrue(res3 is EvalResult.Error)
    }

    @Test
    fun testLivePreview() {
        // Incomplete expression preview with unclosed bracket
        val preview = engine.evaluatePreview("2 * (3 + 4")
        assertNotNull(preview)
        assertEquals("14", preview)
    }
}
