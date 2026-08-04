package com.gomoku.nusv

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
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
import androidx.compose.ui.Modifier
import com.gomoku.nusv.data.ProfileStore
import com.gomoku.nusv.i18n.I18n
import com.gomoku.nusv.sound.SoundPlayer
import com.gomoku.nusv.ui.GameController
import com.gomoku.nusv.ui.HomePage
import com.gomoku.nusv.ui.GamePage
import com.gomoku.nusv.ui.TicTacToePage
import com.gomoku.nusv.ui.AchievementsPage
import com.gomoku.nusv.ui.StatsPage
import com.gomoku.nusv.ui.TitlesPage
import com.gomoku.nusv.ui.ShopPage
import com.gomoku.nusv.ui.SettingsPage
import com.gomoku.nusv.ui.LanPage
import com.gomoku.nusv.ui.nav.NavController
import com.gomoku.nusv.ui.nav.Page
import com.gomoku.nusv.ui.theme.BoardTheme
import com.gomoku.nusv.ui.theme.ThemeRegistry
import com.gomoku.nusv.data.createSettings

@Composable
fun App() {
    val store = remember { ProfileStore(createSettings()) }
    val sound = remember { SoundPlayer() }
    val controller = remember { GameController(store, sound) }
    val nav = remember { NavController() }
    var theme by remember { mutableStateOf<BoardTheme>(ThemeRegistry.byId(store.loadThemeId())) }
    val onThemeChange: (BoardTheme) -> Unit = { newTheme ->
        theme = newTheme
        store.saveThemeId(newTheme.id)
        controller.onThemeSelected(newTheme.id)
    }

    I18n.setLanguage(I18n.Language.fromCode(store.loadLanguage()))

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
        Crossfade(
            targetState = theme,
            animationSpec = tween(300),
            label = "theme"
        ) { activeTheme ->
            AnimatedContent(
                targetState = nav.currentPage,
                transitionSpec = {
                    fadeIn(tween(220)) togetherWith fadeOut(tween(160))
                },
                label = "page"
            ) { page ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    when (page) {
                        Page.HOME -> HomePage(controller, activeTheme, nav, onThemeChange)
                        Page.GAME -> GamePage(controller, activeTheme, nav, onThemeChange)
                        Page.TICTACTOE -> TicTacToePage(controller, activeTheme, nav)
                        Page.ACHIEVEMENTS -> AchievementsPage(controller, activeTheme, nav)
                        Page.STATS -> StatsPage(controller, activeTheme, nav)
                        Page.STORE -> ShopPage(controller, activeTheme, nav)
                        Page.LAN -> LanPage(controller, activeTheme, nav)
                        Page.TITLES -> TitlesPage(controller, activeTheme, nav)
                        Page.SETTINGS -> SettingsPage(controller, activeTheme, nav, onThemeChange)
                    }
                }
            }
        }
    }
}
