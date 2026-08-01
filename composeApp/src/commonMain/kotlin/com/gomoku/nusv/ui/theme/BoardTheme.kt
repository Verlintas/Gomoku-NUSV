package com.gomoku.nusv.ui.theme

import androidx.compose.ui.graphics.Color

data class BoardTheme(
    val id: String,
    val name: String,
    val boardColor: Color,
    val gridColor: Color,
    val starColor: Color,
    val blackStone: Color,
    val whiteStone: Color,
    val lastMoveMark: Color,
    val uiBackground: Color,
    val uiSurface: Color,
    val uiSurfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val isDark: Boolean
)

object ThemeRegistry {

    private val WoodTheme = BoardTheme(
        id = "wood",
        name = "木色经典",
        boardColor = Color(0xFFE8C97E),
        gridColor = Color(0xFF6B4F2A),
        starColor = Color(0xFF6B4F2A),
        blackStone = Color(0xFF1A1A1A),
        whiteStone = Color(0xFFF5F2EA),
        lastMoveMark = Color(0xFFD32F2F),
        uiBackground = Color(0xFFF7EEDD),
        uiSurface = Color(0xFFFFFFFF),
        uiSurfaceVariant = Color(0xFFF0E4CC),
        textPrimary = Color(0xFF3E2F1B),
        textSecondary = Color(0xFF7A6A4E),
        accent = Color(0xFFB8860B),
        isDark = false
    )

    val DarkTheme = BoardTheme(
        id = "dark",
        name = "深空幽蓝",
        boardColor = Color(0xFF2B3A4A),
        gridColor = Color(0xFF9FB6C9),
        starColor = Color(0xFF9FB6C9),
        blackStone = Color(0xFF10151C),
        whiteStone = Color(0xFFE8EDF2),
        lastMoveMark = Color(0xFFFF5252),
        uiBackground = Color(0xFF141A22),
        uiSurface = Color(0xFF1E2833),
        uiSurfaceVariant = Color(0xFF263340),
        textPrimary = Color(0xFFE3EAF2),
        textSecondary = Color(0xFF8FA3B8),
        accent = Color(0xFF5C9CE6),
        isDark = true
    )

    val BlueOcean = BoardTheme(
        id = "ocean",
        name = "碧海青天",
        boardColor = Color(0xFF8FD3E8),
        gridColor = Color(0xFF1E5B6E),
        starColor = Color(0xFF1E5B6E),
        blackStone = Color(0xFF14212B),
        whiteStone = Color(0xFFFDFEFE),
        lastMoveMark = Color(0xFFFF3D00),
        uiBackground = Color(0xFFE4F4F9),
        uiSurface = Color(0xFFFFFFFF),
        uiSurfaceVariant = Color(0xFFD2ECF4),
        textPrimary = Color(0xFF0F3642),
        textSecondary = Color(0xFF4C7A8A),
        accent = Color(0xFF00A8C8),
        isDark = false
    )

    val themes: List<BoardTheme> = listOf(WoodTheme, DarkTheme, BlueOcean)

    fun byId(id: String): BoardTheme = themes.find { it.id == id } ?: WoodTheme
}
