package com.jussicodes.easyapex.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// 🌟 1. 定义主题枚举
enum class AppTheme(val displayName: String) {
    DYNAMIC("动态取色"),
    PURPLE("紫"),
    BLUE("蓝")
}

// 🌟 2. 加上 AppColor 前缀，防止冲突
private val AppColorPurple80 = Color(0xFFD0BCFF)
private val AppColorPurpleGrey80 = Color(0xFFCCC2DC)
private val AppColorPink80 = Color(0xFFEFB8C8)
private val AppColorPurple40 = Color(0xFF6650a4)
private val AppColorPurpleGrey40 = Color(0xFF625b71)
private val AppColorPink40 = Color(0xFF7D5260)

// --- 默认的暗夜紫配色 ---
private val CustomDarkColorScheme = darkColorScheme(
    primary = AppColorPurple80,
    secondary = AppColorPurpleGrey80,
    tertiary = AppColorPink80,
    primaryContainer = Color(0xFF4F378B), // 显式声明紫色的容器底色
    onPrimaryContainer = Color(0xFFEADDFF)
)

private val CustomLightColorScheme = lightColorScheme(
    primary = AppColorPurple40,
    secondary = AppColorPurpleGrey40,
    tertiary = AppColorPink40,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D)
)

// --- 🌟 修复后的深海蓝配色 ---
private val BlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    secondary = Color(0xFFBAC8E0),
    tertiary = Color(0xFFD6BEE4),
    // 增加深色模式下的蓝色容器底色和文字色
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF)
)

private val BlueLightColorScheme = lightColorScheme(
    primary = Color(0xFF0061A4),
    secondary = Color(0xFF535F70),
    tertiary = Color(0xFF6B5778),
    // 增加浅色模式下的蓝色容器底色和文字色
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36)
)

@Composable
fun EasyApexTheme(
    appTheme: AppTheme = AppTheme.DYNAMIC,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.DYNAMIC -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) CustomDarkColorScheme else CustomLightColorScheme
            }
        }
        AppTheme.PURPLE -> if (darkTheme) CustomDarkColorScheme else CustomLightColorScheme
        AppTheme.BLUE -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}