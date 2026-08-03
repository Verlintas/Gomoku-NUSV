package com.gomoku.nusv.data

import com.russhwolf.settings.Settings

/** 平台存档存储（桌面/安卓用系统偏好，Web 用 localStorage）。 */
expect fun createSettings(): Settings
