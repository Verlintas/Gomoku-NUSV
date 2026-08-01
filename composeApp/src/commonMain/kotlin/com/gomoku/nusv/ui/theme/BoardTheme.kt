package com.gomoku.nusv.ui.theme

import androidx.compose.ui.graphics.Color

data class BoardTheme(
    val id: String,
    val nameKey: String,
    val descKey: String,
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
        nameKey = "theme_wood",
        descKey = "theme_wood_desc",
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
        nameKey = "theme_dark",
        descKey = "theme_dark_desc",
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
        nameKey = "theme_ocean",
        descKey = "theme_ocean_desc",
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

    private val BambooTheme = BoardTheme(
        id = "bamboo",
        nameKey = "theme_bamboo",
        descKey = "theme_bamboo_desc",
        boardColor = Color(0xFFC8E6A0),
        gridColor = Color(0xFF3E6B2F),
        starColor = Color(0xFF3E6B2F),
        blackStone = Color(0xFF1B2A18),
        whiteStone = Color(0xFFFBFDF4),
        lastMoveMark = Color(0xFFE53935),
        uiBackground = Color(0xFFF0F7E4),
        uiSurface = Color(0xFFFFFFFF),
        uiSurfaceVariant = Color(0xFFE2EFD2),
        textPrimary = Color(0xFF2C4721),
        textSecondary = Color(0xFF64855A),
        accent = Color(0xFF558B2F),
        isDark = false
    )

    private val MintTheme = BoardTheme(
        id = "mint",
        nameKey = "theme_mint",
        descKey = "theme_mint_desc",
        boardColor = Color(0xFF2E4B45),
        gridColor = Color(0xFFA8D8CB),
        starColor = Color(0xFFA8D8CB),
        blackStone = Color(0xFF0F1815),
        whiteStone = Color(0xFFEAF5F0),
        lastMoveMark = Color(0xFFFF8A65),
        uiBackground = Color(0xFF16241F),
        uiSurface = Color(0xFF21342E),
        uiSurfaceVariant = Color(0xFF2B443C),
        textPrimary = Color(0xFFD8EFE6),
        textSecondary = Color(0xFF87B3A5),
        accent = Color(0xFF4DB6AC),
        isDark = true
    )

    private val RoseTheme = BoardTheme(
        id = "rose",
        nameKey = "theme_rose",
        descKey = "theme_rose_desc",
        boardColor = Color(0xFFF2C6B4),
        gridColor = Color(0xFF8C3B2E),
        starColor = Color(0xFF8C3B2E),
        blackStone = Color(0xFF2B1613),
        whiteStone = Color(0xFFFFF9F5),
        lastMoveMark = Color(0xFFD81B60),
        uiBackground = Color(0xFFFBF0EC),
        uiSurface = Color(0xFFFFFFFF),
        uiSurfaceVariant = Color(0xFFF6E2DA),
        textPrimary = Color(0xFF5C2620),
        textSecondary = Color(0xFF9C6459),
        accent = Color(0xFFE91E63),
        isDark = false
    )

    private val VioletTheme = BoardTheme(
        id = "violet",
        nameKey = "theme_violet",
        descKey = "theme_violet_desc",
        boardColor = Color(0xFFC9B8E8),
        gridColor = Color(0xFF4A3478),
        starColor = Color(0xFF4A3478),
        blackStone = Color(0xFF1D152E),
        whiteStone = Color(0xFFF8F5FD),
        lastMoveMark = Color(0xFFE040FB),
        uiBackground = Color(0xFFF3EEFA),
        uiSurface = Color(0xFFFFFFFF),
        uiSurfaceVariant = Color(0xFFE6DCF5),
        textPrimary = Color(0xFF34235C),
        textSecondary = Color(0xFF7A6AA3),
        accent = Color(0xFF9C27B0),
        isDark = false
    )

    private val MapleTheme = BoardTheme(
        id = "maple",
        nameKey = "theme_maple",
        descKey = "theme_maple_desc",
        boardColor = Color(0xFFF0C896),
        gridColor = Color(0xFF7A3B1E),
        starColor = Color(0xFF7A3B1E),
        blackStone = Color(0xFF2B150C),
        whiteStone = Color(0xFFFDF6EE),
        lastMoveMark = Color(0xFFC62828),
        uiBackground = Color(0xFFFAF0E2),
        uiSurface = Color(0xFFFFFFFF),
        uiSurfaceVariant = Color(0xFFF3E0C4),
        textPrimary = Color(0xFF5C2E14),
        textSecondary = Color(0xFF9C6B44),
        accent = Color(0xFFE65100),
        isDark = false
    )

    private val MidnightTheme = BoardTheme(
        id = "midnight",
        nameKey = "theme_midnight",
        descKey = "theme_midnight_desc",
        boardColor = Color(0xFF1A1F35),
        gridColor = Color(0xFF7C86B8),
        starColor = Color(0xFF7C86B8),
        blackStone = Color(0xFF0B0D18),
        whiteStone = Color(0xFFE6E9F7),
        lastMoveMark = Color(0xFFFF5252),
        uiBackground = Color(0xFF0E1120),
        uiSurface = Color(0xFF1A1F35),
        uiSurfaceVariant = Color(0xFF252C4D),
        textPrimary = Color(0xFFDDE2F8),
        textSecondary = Color(0xFF8E96C4),
        accent = Color(0xFF7986CB),
        isDark = true
    )

    private val InkTheme = BoardTheme(
        id = "ink",
        nameKey = "theme_ink",
        descKey = "theme_ink_desc",
        boardColor = Color(0xFFEFEDE6),
        gridColor = Color(0xFF2C2C2C),
        starColor = Color(0xFF2C2C2C),
        blackStone = Color(0xFF111111),
        whiteStone = Color(0xFFFDFDFB),
        lastMoveMark = Color(0xFFB71C1C),
        uiBackground = Color(0xFFF7F5F0),
        uiSurface = Color(0xFFFFFFFF),
        uiSurfaceVariant = Color(0xFFE8E5DC),
        textPrimary = Color(0xFF262626),
        textSecondary = Color(0xFF737373),
        accent = Color(0xFF424242),
        isDark = false
    )

    val themes: List<BoardTheme> = listOf(
        WoodTheme, DarkTheme, BlueOcean, BambooTheme, MintTheme, RoseTheme,
        VioletTheme, MapleTheme, MidnightTheme, InkTheme
    )


    fun byId(id: String): BoardTheme = themes.find { it.id == id } ?: WoodTheme
}
