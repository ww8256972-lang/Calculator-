package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculator.ui.CalculatorScreen
import com.example.calculator.ui.CalculatorViewModel
import com.example.ui.theme.WaqarCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val calcViewModel: CalculatorViewModel = viewModel()
            val uiState by calcViewModel.uiState.collectAsState()

            WaqarCalculatorTheme(themeMode = uiState.userSettings.themeMode) {
                CalculatorScreen(viewModel = calcViewModel)
            }
        }
    }
}
