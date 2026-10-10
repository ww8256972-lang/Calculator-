# Waqar Smart Calculator

A premium, modern Android calculator app designed with Jetpack Compose and Material Design 3. Built for phones and tablets with basic arithmetic, comprehensive scientific functions, calculation history, memory register, dynamic live preview, and dark/light themes.

---

## ✨ Features

### 1. Basic Calculator
- Standard operations: Addition (+), Subtraction (−), Multiplication (×), Division (÷)
- Percentage calculation with additive/subtractive smart syntax (`100 + 20% = 120`, `200 * 15% = 30`)
- Decimal numbers with configurable precision and auto-trimming
- Sign toggle (`+/−`), All Clear (`AC`), Delete backspace (`⌫`)
- Continuous calculations (chaining operators directly on results)
- Parentheses `(` and `)` with auto-balancing and implicit multiplication
- Division-by-zero and domain error protection

### 2. Scientific Calculator
- Powers & exponents: Square ($x^2$), Cube ($x^3$), Power ($x^y$), $10^x$, $e^x$
- Roots: Square root ($\sqrt{x}$), Cube root ($\sqrt[3]{x}$)
- Trigonometric: $\sin$, $\cos$, $\tan$ (with DEG / RAD mode toggle)
- Inverse trigonometric: $\sin^{-1}$, $\cos^{-1}$, $\tan^{-1}$
- Logarithms: Common logarithm ($\log_{10}$), Natural logarithm ($\ln$)
- Special functions: Factorial ($n!$), Reciprocal ($1/x$), Absolute value ($|x|$)
- Mathematical constants: $\pi$ ($3.14159...$), $e$ ($2.71828...$)

### 3. Smart Features & Storage
- **Memory Operations**: MC (Clear), MR (Recall), M+ (Add), M− (Subtract), MS (Store) with visual badge indicator
- **Live Expression Preview**: Instant calculation preview displayed below input while typing
- **Room Database History**: Automatic, persistent calculation logging on the local device
- **History Actions**: Search through past records, tap to reuse expression or insert result, bookmark favorites, or clear history
- **Clipboard Integration**: One-tap copy with animated feedback badge

### 4. Customization & Settings
- **Themes**: Elegant Dark Slate, Clean Light, or System default
- **Angle Unit**: Degrees (° DEG) or Radians (RAD)
- **Decimal Precision**: Auto, 2, 4, 6, or 8 decimal places
- **Grouping**: Thousands separators toggle (e.g. `1,250,000`)
- **Haptic Feedback**: Tactile vibration on button presses

---

## 🛠 Project Architecture

- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Local Persistence**: Android Room Database (`CalculationHistory`, `CalculationDao`, `CalculatorDatabase`)
- **Expression Engine**: Pure Kotlin recursive-descent parser (`MathEngine`) with zero `eval()` vulnerabilities
- **Testing**: Comprehensive JUnit 4 and Robolectric test suite

---

## 🚀 Building & Testing

### Run Tests
```bash
gradle :app:testDebugUnitTest
```

### Build APK
```bash
gradle :app:assembleDebug
```
The generated APK will be in `app/build/outputs/apk/debug/app-debug.apk`.
