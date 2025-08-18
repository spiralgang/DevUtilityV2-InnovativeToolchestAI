package com.spiralgang.srirachaarmy.devutility.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// SrirachaArmy color palette
val SrirachaRed = Color(0xFFD32F2F)
val SrirachaOrange = Color(0xFFFF9800)
val SrirachaMild = Color(0xFF4CAF50)
val SrirachaMedium = Color(0xFFFF9800)
val SrirachaSpicy = Color(0xFFFF5722)
val SrirachaGhostPepper = Color(0xFFB71C1C)

// Bot status colors
val BotActive = Color(0xFF4CAF50)
val BotThinking = Color(0xFFFF9800)
val BotError = Color(0xFFF44336)

// Material Design 3 color scheme
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val DarkColorScheme = darkColorScheme(
    primary = SrirachaRed,
    secondary = SrirachaOrange,
    tertiary = SrirachaSpicy,
    background = Color(0xFF121212),
    surface = Color(0xFF1F1F1F),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

val LightColorScheme = lightColorScheme(
    primary = SrirachaRed,
    secondary = SrirachaOrange,
    tertiary = SrirachaSpicy,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)