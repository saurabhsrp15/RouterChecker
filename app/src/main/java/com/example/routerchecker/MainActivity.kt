package com.example.routerchecker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routerchecker.ui.MainScreen
import com.example.routerchecker.ui.TechBackground
import com.example.routerchecker.ui.TechnicalRouterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TechnicalRouterTheme {
                Surface(color = TechBackground) {
                    MainScreen(viewModel = viewModel())
                }
            }
        }
    }
}
