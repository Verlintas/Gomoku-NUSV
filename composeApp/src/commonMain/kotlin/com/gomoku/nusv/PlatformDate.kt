package com.gomoku.nusv

/**
 * 平台日期（commonMain 无 java.time，用 expect/actual 提供本地日期字符串）。
 */
expect fun todayStr(): String
