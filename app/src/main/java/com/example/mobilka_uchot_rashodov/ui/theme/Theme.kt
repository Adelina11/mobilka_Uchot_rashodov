package com.example.mobilka_uchot_rashodov.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Цвета из вашей палитры (Светлая тема)
val LemonCream = Color(0xFFFFF6D6)
val CoralReef = Color(0xFFFFB5A7)
val OceanMist = Color(0xFFB7DCD6)
val MorningSky = Color(0xFFD6E4FA)
val GoldenDune = Color(0xFFF4D9A6)
val SunsetClay = Color(0xFFE07A5F)

private val LightColorScheme = lightColorScheme(
    primary = SunsetClay,                     // Главные кнопки (#E07A5F)
    onPrimary = Color.White,
    primaryContainer = GoldenDune,            // Верхняя шапка (#F4D9A6)
    onPrimaryContainer = Color(0xFF3E2C14),
    secondary = Color(0xFF2B5B54),
    onSecondary = Color.White,
    secondaryContainer = OceanMist,           // Карточка "Итог за период" (#B7DCD6)
    onSecondaryContainer = Color(0xFF103833),
    tertiaryContainer = MorningSky,
    onTertiaryContainer = Color(0xFF1B3A6B),
    background = Color(0xFFFFFBF0),           // Мягкий кремовый фон
    onBackground = Color(0xFF2C2520),
    surface = Color(0xFFFFFFFF),              // Карточки расходов
    onSurface = Color(0xFF2C2520),
    surfaceVariant = LemonCream,
    onSurfaceVariant = Color(0xFF6B5E52),
    error = Color(0xFFC94A4A)
)

// Парная Тёмная тема (уютный вечерний стиль в тех же тонах)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFEB8A70),              // Мягкий терракотовый
    onPrimary = Color(0xFF2B1109),
    primaryContainer = Color(0xFF3D3122),     // Тёмное золото для шапки
    onPrimaryContainer = GoldenDune,
    secondary = OceanMist,
    onSecondary = Color(0xFF103833),
    secondaryContainer = Color(0xFF253D39),   // Тёмный океан для карточки итога
    onSecondaryContainer = OceanMist,
    tertiaryContainer = Color(0xFF263347),
    onTertiaryContainer = MorningSky,
    background = Color(0xFF181614),           // Тёплый тёмный графит/эспрессо
    onBackground = Color(0xFFEDE5DC),
    surface = Color(0xFF24201D),              // Тёмные карточки
    onSurface = Color(0xFFEDE5DC),
    surfaceVariant = Color(0xFF302B26),
    onSurfaceVariant = Color(0xFFB8ACA0),
    error = Color(0xFFFF8989)
)

// Цвета кружочков для 5 категорий из вашей палитры
fun categoryColor(category: String, isDark: Boolean): Color = when (category) {
    "продукты" -> if (isDark) Color(0xFF2E544E) else OceanMist
    "транспорт" -> if (isDark) Color(0xFF2E4361) else MorningSky
    "жильё" -> if (isDark) Color(0xFF594626) else GoldenDune
    "развлечения" -> if (isDark) Color(0xFF5E362F) else CoralReef
    else -> if (isDark) Color(0xFF5C3428) else SunsetClay.copy(alpha = 0.45f)
}

@Composable
fun Mobilka_Uchot_rashodovTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}