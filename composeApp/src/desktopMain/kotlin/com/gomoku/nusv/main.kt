package com.gomoku.nusv

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val osName = System.getProperty("os.name").lowercase()
    println("Gomoku-NUSV desktop starting on $osName")
    Window(
        onCloseRequest = ::exitApplication,
        title = "Gomoku-NUSV",
        state = rememberWindowState(width = 1080.dp, height = 800.dp)
    ) {
        App()
    }
}
