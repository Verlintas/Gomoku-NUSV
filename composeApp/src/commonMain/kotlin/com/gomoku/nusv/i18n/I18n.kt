package com.gomoku.nusv.i18n

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Compose 客户端（Android / Desktop / iOS）的语言状态桥接。
 * 翻译数据与纯函数在 [Translations]（鸿蒙 ArkUI 客户端可直接复用）。
 */
object I18n {

    typealias Language = Translations.Language

    var currentLanguage by mutableStateOf(Language.ZH)
        private set

    fun setLanguage(language: Language) {
        currentLanguage = language
    }

    fun t(key: String): String = Translations.t(key, currentLanguage)

    fun t(key: String, vararg args: Pair<String, String>): String =
        Translations.t(key, currentLanguage, *args)
}
