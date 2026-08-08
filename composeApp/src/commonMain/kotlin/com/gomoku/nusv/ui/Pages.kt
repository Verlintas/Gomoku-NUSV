package com.gomoku.nusv.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gomoku.nusv.data.Achievements
import com.gomoku.nusv.data.Titles
import com.gomoku.nusv.i18n.I18n
import com.gomoku.nusv.model.Difficulty
import com.gomoku.nusv.ui.nav.NavController
import com.gomoku.nusv.ui.theme.BoardTheme
import com.gomoku.nusv.ui.theme.ThemeRegistry

@Composable
private fun PageHeader(title: String, nav: NavController, theme: BoardTheme) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(onClick = { nav.back() }) {
            Text("←", fontSize = 14.sp)
        }
        Spacer(Modifier.width(12.dp))
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
    }
}

@Composable
fun AchievementsPage(controller: GameController, theme: BoardTheme, nav: NavController) {
    Surface(color = theme.uiBackground, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PageHeader(I18n.t("nav_achievements"), nav, theme)
            Text(
                "${I18n.t("achievements_unlocked")}: ${controller.profile.achievements.size} / ${Achievements.all.size}",
                fontSize = 13.sp,
                color = theme.textSecondary
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Achievements.all.forEach { a ->
                    val unlocked = a.id in controller.profile.achievements
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (unlocked) Color(0xFFFFF8E1) else theme.uiBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                if (unlocked) "★" else "☆",
                                fontSize = 18.sp,
                                color = if (unlocked) Color(0xFFF0C94C) else theme.textSecondary
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    I18n.t(a.nameKey),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (unlocked) theme.textPrimary else theme.textSecondary
                                )
                                Text(
                                    I18n.t(a.descKey),
                                    fontSize = 12.sp,
                                    color = if (unlocked) theme.textSecondary else theme.textSecondary.copy(alpha = 0.6f)
                                )
                            }
                            Text(
                                if (unlocked) I18n.t("unlocked_short") else I18n.t("locked_short"),
                                fontSize = 12.sp,
                                color = if (unlocked) Color(0xFF8D6E00) else theme.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TitlesPage(controller: GameController, theme: BoardTheme, nav: NavController) {
    Surface(color = theme.uiBackground, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PageHeader(I18n.t("nav_titles"), nav, theme)
            val current = Titles.current(controller.profile)
            Text(
                I18n.t("title_current") + ": " + I18n.t(current.nameKey),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = theme.accent
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Titles.all.forEach { t ->
                    val achieved = Titles.current(controller.profile).let { cur ->
                        t.id == cur.id || (controller.profile.wins >= t.minWins &&
                            controller.profile.bestWinStreak >= t.minBestStreak &&
                            controller.profile.gamesPlayed >= t.minGames &&
                            (controller.profile.winsByDifficulty["HARD"] ?: 0) >= t.minHardWins)
                    }
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (t.id == current.id) theme.uiSurfaceVariant else theme.uiBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (t.id == current.id) theme.accent else theme.uiSurfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                if (achieved) "★" else "☆",
                                fontSize = 18.sp,
                                color = if (achieved) theme.accent else theme.textSecondary
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    I18n.t(t.nameKey),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (achieved) theme.textPrimary else theme.textSecondary
                                )
                                Text(
                                    I18n.t(t.descKey),
                                    fontSize = 12.sp,
                                    color = theme.textSecondary
                                )
                            }
                            if (t.id == current.id) {
                                Text(
                                    I18n.t("title_in_use"),
                                    fontSize = 12.sp,
                                    color = theme.accent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsPage(controller: GameController, theme: BoardTheme, nav: NavController) {
    val p = controller.profile
    val played = p.gamesPlayed.coerceAtLeast(1)
    val winRate = p.wins * 100 / played
    Surface(color = theme.uiBackground, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PageHeader(I18n.t("nav_stats"), nav, theme)
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.uiSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatsRow(I18n.t("total_games"), "${p.gamesPlayed}", theme)
                    StatsRow(I18n.t("victory"), "${p.wins}", theme)
                    StatsRow(I18n.t("defeat"), "${p.losses}", theme)
                    StatsRow(I18n.t("draws"), "${p.draws}", theme)
                    StatsRow(I18n.t("win_rate"), "$winRate%", theme)
                    StatsRow(I18n.t("current_streak"), "${p.winStreak}", theme)
                    StatsRow(I18n.t("best_streak"), "${p.bestWinStreak}", theme)
                    StatsRow(I18n.t("total_time"), formatDuration(p.totalTimeSec), theme)
                    StatsRow(I18n.t("fastest_win"), if (p.fastestWinSec > 0) formatClockLocal(p.fastestWinSec) else "—", theme)
                    StatsRow(I18n.t("longest_game"), if (p.longestGameMoves > 0) "${p.longestGameMoves} ${I18n.t("move_count")}" else "—", theme)
                    StatsRow(I18n.t("minigame_wins"), "${p.minigameWins}", theme)
                    StatsRow(I18n.t("score"), "${p.score}", theme)
                    StatsRow(I18n.t("signin_total"), "${p.totalSignIns}", theme)
                    StatsRow(I18n.t("signin_streak"), "${p.signInStreak}", theme)
                    StatsRow(I18n.t("daily_tasks_done"), "${p.dailyTaskCompletions}", theme)
                }
            }
            HorizontalDivider(color = theme.uiSurfaceVariant)
            Text(I18n.t("difficulty_wins"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
            Difficulty.entries.forEach { d ->
                val wins = p.winsByDifficulty[d.name] ?: 0
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        difficultyNameLocal(d),
                        fontSize = 13.sp,
                        color = theme.textPrimary,
                        modifier = Modifier.width(64.dp)
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(theme.uiSurfaceVariant)
                    ) {
                        if (wins > 0) {
                            Box(
                                Modifier
                                    .fillMaxWidth((wins / 20f).coerceAtMost(1f))
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(theme.accent)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("$wins", fontSize = 13.sp, color = theme.textSecondary)
                }
            }
            HorizontalDivider(color = theme.uiSurfaceVariant)
            Text(I18n.t("theme_usage"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
            ThemeRegistry.themes.forEach { t ->
                val uses = p.themeUses[t.id] ?: 0
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        I18n.t(t.nameKey),
                        fontSize = 13.sp,
                        color = theme.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text("$uses", fontSize = 13.sp, color = theme.textSecondary)
                }
            }
        }
    }
}

@Composable
private fun StatsRow(label: String, value: String, theme: BoardTheme) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, color = theme.textSecondary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, color = theme.textPrimary, fontWeight = FontWeight.Medium)
    }
}

private fun difficultyNameLocal(d: Difficulty): String = I18n.t(
    when (d) {
        Difficulty.EASY -> "diff_easy"
        Difficulty.MEDIUM -> "diff_medium"
        Difficulty.HARD -> "diff_hard"
    }
)

private fun formatClockLocal(seconds: Int): String {
    val s = seconds.coerceAtLeast(0)
    val m = s / 60
    val sec = s % 60
    return "$m:${if (sec < 10) "0" else ""}$sec"
}

private fun formatDuration(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val sec = totalSeconds % 60
    return if (h > 0) {
        "$h:${if (m < 10) "0" else ""}$m:${if (sec < 10) "0" else ""}$sec"
    } else {
        val total = m.toInt() * 60 + sec.toInt()
        "$m:${if (sec < 10) "0" else ""}$sec" + if (total == 0) "" else ""
    }
}

@Composable
fun SettingsPage(
    controller: GameController,
    theme: BoardTheme,
    nav: NavController,
    onThemeChange: (BoardTheme) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var importText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var resetStep by remember { mutableStateOf(0) }

    Surface(color = theme.uiBackground, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PageHeader(I18n.t("nav_settings"), nav, theme)

            Card(
                colors = CardDefaults.cardColors(containerColor = theme.uiSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(I18n.t("settings_language"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        I18n.Language.entries.forEach { lang ->
                            Card(
                                modifier = Modifier.clickable { controller.setLanguage(lang) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (I18n.currentLanguage == lang) theme.uiSurfaceVariant else theme.uiBackground
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (I18n.currentLanguage == lang) theme.accent else theme.uiSurfaceVariant
                                )
                            ) {
                                Text(
                                    lang.displayName,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    fontSize = 13.sp,
                                    color = theme.textPrimary
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = theme.uiSurfaceVariant)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(I18n.t("effects_master"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                            Text(I18n.t("effects_master_desc"), fontSize = 11.sp, color = theme.textSecondary)
                        }
                        androidx.compose.material3.Switch(
                            checked = controller.effectsEnabled,
                            onCheckedChange = { controller.setEffectsEnabledFlag(it) }
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(I18n.t("sound_master"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                            Text(I18n.t("sound_master_desc"), fontSize = 11.sp, color = theme.textSecondary)
                        }
                        androidx.compose.material3.Switch(
                            checked = controller.soundEnabled,
                            onCheckedChange = { controller.setSoundEnabledFlag(it) }
                        )
                    }

                    OutlinedButton(
                        onClick = { nav.navigate(com.gomoku.nusv.ui.nav.Page.STORE) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(I18n.t("nav_store"), fontSize = 13.sp)
                    }

                    HorizontalDivider(color = theme.uiSurfaceVariant)

                    Text(I18n.t("settings_theme"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                    @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ThemeRegistry.themes.forEach { t ->
                            Card(
                                modifier = Modifier
                                    .width(190.dp)
                                    .clickable { onThemeChange(t) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (t.id == theme.id) theme.uiSurfaceVariant else theme.uiBackground
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (t.id == theme.id) theme.accent else theme.uiSurfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .width(16.dp)
                                                .height(16.dp)
                                                .clip(CircleShape)
                                                .background(t.boardColor)
                                                .border(1.dp, t.gridColor, CircleShape)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            I18n.t(t.nameKey),
                                            fontSize = 13.sp,
                                            fontWeight = if (t.id == theme.id) FontWeight.Bold else FontWeight.Normal,
                                            color = theme.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = theme.uiSurfaceVariant)

                    Text(I18n.t("settings_save"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                    Text(
                        I18n.t("settings_save_desc"),
                        fontSize = 12.sp,
                        color = theme.textSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                clipboard.setText(AnnotatedString(controller.exportSave()))
                                message = I18n.t("export_done")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(I18n.t("export_save"), fontSize = 13.sp)
                        }
                        Button(
                            onClick = {
                                val text = clipboard.getText()?.text
                                if (text == null) {
                                    message = I18n.t("import_empty")
                                } else {
                                    message = if (controller.importSave(text)) {
                                        I18n.t("import_done")
                                    } else {
                                        I18n.t("import_failed")
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(I18n.t("import_save"), fontSize = 13.sp)
                        }
                    }

                    Text(
                        I18n.t("import_paste_hint"),
                        fontSize = 12.sp,
                        color = theme.textSecondary
                    )
                    TextField(
                        value = importText,
                        onValueChange = { importText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 110.dp),
                        placeholder = {
                            Text(
                                I18n.t("import_paste_placeholder"),
                                fontSize = 12.sp,
                                color = theme.textSecondary
                            )
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                    )
                    Button(
                        onClick = {
                            message = if (controller.importSave(importText)) {
                                importText = ""
                                I18n.t("import_done")
                            } else {
                                I18n.t("import_failed")
                            }
                        },
                        enabled = importText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(I18n.t("import_paste_confirm"), fontSize = 13.sp)
                    }
                    message?.let {
                        Text(it, fontSize = 12.sp, color = theme.accent)
                    }

                    HorizontalDivider(color = theme.uiSurfaceVariant)

                    Text(I18n.t("settings_reset"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                    Text(
                        I18n.t("settings_reset_desc"),
                        fontSize = 12.sp,
                        color = theme.textSecondary
                    )
                    Button(
                        onClick = { resetStep = 1 },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color(0xFFC62828)
                        )
                    ) {
                        Text(I18n.t("settings_reset_btn"), fontSize = 13.sp)
                    }

                    HorizontalDivider(color = theme.uiSurfaceVariant)

                    Text(I18n.t("settings_about"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                    Text(
                        "Gomoku-NUSV ${com.gomoku.nusv.APP_VERSION}",
                        fontSize = 12.sp,
                        color = theme.textSecondary
                    )
                }
            }
        }
    }

    if (resetStep == 1) {
        AlertDialog(
            onDismissRequest = { resetStep = 0 },
            containerColor = theme.uiSurface,
            title = { Text(I18n.t("reset_confirm_title"), color = theme.textPrimary) },
            text = { Text(I18n.t("reset_confirm_body"), color = theme.textSecondary) },
            confirmButton = {
                Button(
                    onClick = { resetStep = 2 },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFFC62828)
                    )
                ) {
                    Text(I18n.t("settings_reset_btn"), fontSize = 13.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { resetStep = 0 }) { Text(I18n.t("cancel")) }
            }
        )
    }
    if (resetStep == 2) {
        AlertDialog(
            onDismissRequest = { resetStep = 0 },
            containerColor = theme.uiSurface,
            title = { Text(I18n.t("reset_confirm2_title"), color = androidx.compose.ui.graphics.Color(0xFFC62828)) },
            text = { Text(I18n.t("reset_confirm2_body"), color = theme.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        controller.resetProfile()
                        resetStep = 0
                        message = I18n.t("reset_done")
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFFC62828)
                    )
                ) {
                    Text(I18n.t("reset_confirm_final"), fontSize = 13.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { resetStep = 0 }) { Text(I18n.t("cancel")) }
            }
        )
    }
}
