package com.gomoku.nusv.ui.nav

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class Page(val titleKey: String) {
    HOME("nav_home"),
    GAME("nav_game"),
    TICTACTOE("nav_minigame"),
    ACHIEVEMENTS("nav_achievements"),
    STATS("nav_stats"),
    TITLES("nav_titles"),
    SETTINGS("nav_settings")
}

class NavController(initial: Page = Page.HOME) {

    var currentPage by mutableStateOf(initial)
        private set

    private val backStack = ArrayDeque<Page>()

    fun navigate(page: Page) {
        if (page == currentPage) return
        backStack.addLast(currentPage)
        currentPage = page
    }

    fun back(): Boolean {
        if (backStack.isEmpty()) return false
        currentPage = backStack.removeLast()
        return true
    }

    fun navigateHome() {
        backStack.clear()
        currentPage = Page.HOME
    }
}
