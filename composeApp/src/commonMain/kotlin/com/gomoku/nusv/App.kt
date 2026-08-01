package com.gomoku.nusv

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.gomoku.nusv.data.ProfileStore
import com.gomoku.nusv.sound.SoundPlayer
import com.gomoku.nusv.ui.GameController
import com.gomoku.nusv.ui.GameScreen
import com.gomoku.nusv.ui.theme.BoardTheme
import com.gomoku.nusv.ui.theme.ThemeRegistry
import com.russhwolf.settings.Settings

@Composable
fun App() {
    val store = remember { ProfileStore(Settings()) }
    val sound = remember { SoundPlayer() }
    val controller = remember { GameController(store, sound) }
    var theme by remember { mutableStateOf<BoardTheme>(ThemeRegistry.byId(store.loadThemeId())) }

    LaunchedEffect(Unit) {
        controller.startTimer()
    }
    DisposableEffect(Unit) {
        onDispose { sound.dispose() }
    }

    val colorScheme = if (theme.isDark) {
        darkColorScheme(
            primary = theme.accent,
            secondary = theme.accent,
            background = theme.uiBackground,
            surface = theme.uiSurface,
            surfaceVariant = theme.uiSurfaceVariant,
            onBackground = theme.textPrimary,
            onSurface = theme.textPrimary,
            onSurfaceVariant = theme.textSecondary
        )
    } else {
        lightColorScheme(
            primary = theme.accent,
            secondary = theme.accent,
            background = theme.uiBackground,
            surface = theme.uiSurface,
            surfaceVariant = theme.uiSurfaceVariant,
            onBackground = theme.textPrimary,
            onSurface = theme.textPrimary,
            onSurfaceVariant = theme.textSecondary
        )
    }

    MaterialTheme(colorScheme = colorScheme) {
        GameScreen(
            controller = controller,
            theme = theme,
            onThemeChange = { newTheme ->
                theme = newTheme
                store.saveThemeId(newTheme.id)
            }
        )
    }
}
