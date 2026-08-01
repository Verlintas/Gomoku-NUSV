package com.gomoku.nusv

import java.time.LocalDate

actual fun todayStr(): String = LocalDate.now().toString()
