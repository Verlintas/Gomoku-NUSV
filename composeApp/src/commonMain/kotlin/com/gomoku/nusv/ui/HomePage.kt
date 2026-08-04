package com.gomoku.nusv.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gomoku.nusv.data.DailyTaskSystem
import com.gomoku.nusv.data.Titles
import com.gomoku.nusv.data.TaskType
import com.gomoku.nusv.i18n.I18n
import com.gomoku.nusv.model.GameMode
import com.gomoku.nusv.ui.nav.NavController
import com.gomoku.nusv.ui.nav.Page
import com.gomoku.nusv.ui.theme.BoardTheme

@Composable
fun HomePage(
    controller: GameController,
    theme: BoardTheme,
    nav: NavController,
    onThemeChange: (BoardTheme) -> Unit
) {
    Surface(color = theme.uiBackground, modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
        val cardWidth = if (maxWidth > 560.dp) 0.72f else 0.95f
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Gomoku-NUSV",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
            Text(
                I18n.t("home_tagline"),
                fontSize = 14.sp,
                color = theme.textSecondary
            )

            val title = Titles.current(controller.profile)
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.uiSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, theme.accent.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(theme.accent.copy(alpha = 0.7f), theme.uiSurfaceVariant)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("★", fontSize = 20.sp, color = theme.textPrimary)
                    }
                    Column {
                        Text(
                            I18n.t("title_label"),
                            fontSize = 11.sp,
                            color = theme.textSecondary
                        )
                        Text(
                            I18n.t(title.nameKey),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.accent
                        )
                    }
                }
            }

            Text(
                "${controller.profile.wins}${I18n.t("wins")} · ${controller.profile.losses}${I18n.t("losses")} · " +
                    "${controller.profile.draws}${I18n.t("draws")} · ${I18n.t("best_streak")} ${controller.profile.bestWinStreak}",
                fontSize = 13.sp,
                color = theme.textSecondary
            )

            ScoreAndSignInCard(controller, theme)
            DailyTaskCard(controller, theme)

            Spacer(Modifier.height(4.dp))

            ModeCard(
                title = I18n.t("mode_ai"),
                subtitle = I18n.t("home_vs_ai_desc"),
                onClick = {
                    controller.setMode(GameMode.VS_AI)
                    nav.navigate(Page.GAME)
                },
                theme = theme,
                widthFactor = cardWidth
            )
            ModeCard(
                title = I18n.t("mode_pvp"),
                subtitle = I18n.t("home_pvp_desc"),
                onClick = {
                    controller.setMode(GameMode.PVP)
                    nav.navigate(Page.GAME)
                },
                theme = theme,
                widthFactor = cardWidth
            )
            ModeCard(
                title = I18n.t("nav_lan"),
                subtitle = I18n.t("home_lan_desc"),
                onClick = { nav.navigate(Page.LAN) },
                theme = theme,
                widthFactor = cardWidth
            )
            ModeCard(
                title = I18n.t("nav_minigame"),
                subtitle = I18n.t("home_minigame_desc"),
                onClick = { nav.navigate(Page.TICTACTOE) },
                theme = theme,
                widthFactor = cardWidth
            )

            Spacer(Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { nav.navigate(Page.STORE) }) {
                    Text(I18n.t("nav_store"), fontSize = 14.sp)
                }
                OutlinedButton(onClick = { nav.navigate(Page.ACHIEVEMENTS) }) {
                    Text(I18n.t("nav_achievements"), fontSize = 14.sp)
                }
                OutlinedButton(onClick = { nav.navigate(Page.TITLES) }) {
                    Text(I18n.t("nav_titles"), fontSize = 14.sp)
                }
                OutlinedButton(onClick = { nav.navigate(Page.STATS) }) {
                    Text(I18n.t("nav_stats"), fontSize = 14.sp)
                }
            }
            OutlinedButton(
                onClick = { nav.navigate(Page.SETTINGS) },
                modifier = Modifier.fillMaxWidth(cardWidth)
            ) {
                Text(I18n.t("nav_settings"), fontSize = 14.sp)
            }
        }
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    theme: BoardTheme,
    widthFactor: Float = 0.72f
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(widthFactor)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = theme.uiSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, theme.uiSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
            Text(subtitle, fontSize = 13.sp, color = theme.textSecondary)
        }
    }
}

@Composable
private fun ScoreAndSignInCard(controller: GameController, theme: BoardTheme) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.uiSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, theme.accent.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    I18n.t("score"),
                    fontSize = 11.sp,
                    color = theme.textSecondary
                )
                Text(
                    "${controller.profile.score}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.accent
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    I18n.t("signin_streak") + " ${controller.profile.signInStreak}",
                    fontSize = 12.sp,
                    color = theme.textSecondary
                )
                Text(
                    I18n.t("signin_total") + " ${controller.profile.totalSignIns}",
                    fontSize = 11.sp,
                    color = theme.textSecondary
                )
            }
            Spacer(Modifier.width(14.dp))
            val signedToday = controller.profile.signInDate == com.gomoku.nusv.todayStr()
            Button(
                onClick = { controller.signIn() },
                enabled = !signedToday
            ) {
                Text(
                    if (signedToday) I18n.t("signin_done") else I18n.t("signin"),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun DailyTaskCard(controller: GameController, theme: BoardTheme) {
    val tasks = DailyTaskSystem.tasksForToday(controller.profile)
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.uiSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, theme.uiSurfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                I18n.t("daily_tasks"),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
            tasks.forEach { task ->
                val progress = DailyTaskSystem.progressOf(task.type, controller.profile)
                val done = progress >= task.target
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (done) "★" else "☆",
                        fontSize = 14.sp,
                        color = if (done) theme.accent else theme.textSecondary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        taskName(task.type) + " ${progress}/${task.target}",
                        fontSize = 13.sp,
                        color = if (done) theme.textSecondary else theme.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "+${task.reward}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (done) theme.accent else theme.textSecondary
                    )
                }
            }
            if (tasks.isEmpty()) {
                Text(
                    I18n.t("daily_tasks_empty"),
                    fontSize = 12.sp,
                    color = theme.textSecondary
                )
            }
        }
    }
}

private fun taskName(type: TaskType): String = I18n.t(type.key)
