package com.jussicodes.easyapex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState // 🌟 修复 collectAsState 报错
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel // 🌟 修复 viewModel() 报错
import com.jussicodes.easyapex.ui.theme.EasyApexTheme // 🌟 修复主题导入报错

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // 实例化全局 ViewModel
            val viewModel: ApexViewModel = viewModel()
            // 收集当前主题状态
            val currentTheme by viewModel.currentTheme.collectAsState()

            EasyApexTheme(appTheme = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ApexMainScreen(viewModel = viewModel)
                }
            }
        }
    }
}