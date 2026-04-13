package com.alertastock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.alertastock.ui.navigation.AlertaStockNavigation
import com.alertastock.ui.theme.AlertaStockTheme
import com.alertastock.ui.theme.BgScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlertaStockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgScreen
                ) {
                    AlertaStockNavigation()
                }
            }
        }
    }
}