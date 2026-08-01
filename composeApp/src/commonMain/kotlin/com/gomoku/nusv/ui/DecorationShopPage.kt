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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gomoku.nusv.data.Decoration
import com.gomoku.nusv.data.DecorationRegistry
import com.gomoku.nusv.data.DecorationType
import com.gomoku.nusv.i18n.I18n
import com.gomoku.nusv.ui.nav.NavController
import com.gomoku.nusv.ui.theme.BoardTheme

@Composable
fun DecorationShopPage(controller: GameController, theme: BoardTheme, nav: NavController) {
    Surface(color = theme.uiBackground, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { nav.back() }) {
                    Text("←", fontSize = 14.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        I18n.t("nav_store"),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                    Text(
                        I18n.t("store_subtitle", "n" to "${controller.profile.score}"),
                        fontSize = 12.sp,
                        color = theme.textSecondary
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DecorationSection(
                    title = I18n.t("dec_section_effect"),
                    items = DecorationRegistry.all.filter { it.type == DecorationType.EFFECT_COLOR },
                    selectedId = controller.profile.selectedEffectColor,
                    onSelect = { controller.selectDecoration(it.id, DecorationType.EFFECT_COLOR) },
                    controller = controller,
                    theme = theme
                )
                DecorationSection(
                    title = I18n.t("dec_section_glow"),
                    items = DecorationRegistry.all.filter { it.type == DecorationType.GLOW },
                    selectedId = controller.profile.selectedGlow,
                    onSelect = { controller.selectDecoration(it.id, DecorationType.GLOW) },
                    controller = controller,
                    theme = theme
                )
                DecorationSection(
                    title = I18n.t("dec_section_winline"),
                    items = DecorationRegistry.all.filter { it.type == DecorationType.WIN_LINE },
                    selectedId = controller.profile.selectedWinLine,
                    onSelect = { controller.selectDecoration(it.id, DecorationType.WIN_LINE) },
                    controller = controller,
                    theme = theme
                )
            }
        }
    }
}

@Composable
private fun DecorationSection(
    title: String,
    items: List<Decoration>,
    selectedId: String,
    onSelect: (Decoration) -> Unit,
    controller: GameController,
    theme: BoardTheme
) {
    if (items.isEmpty()) return
    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
    items.forEach { d ->
        val owned = DecorationRegistry.isOwned(controller.profile, d.id)
        val selected = d.id == selectedId
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (selected) theme.uiSurfaceVariant else theme.uiSurface
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (selected) theme.accent else theme.uiSurfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    decorationColor(d.id).copy(alpha = 0.8f),
                                    decorationColor(d.id).copy(alpha = 0.3f)
                                )
                            )
                        )
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        I18n.t(d.nameKey) + if (selected) "（${I18n.t("in_use_short")}）" else "",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.textPrimary
                    )
                    Text(I18n.t(d.descKey), fontSize = 12.sp, color = theme.textSecondary)
                }
                when {
                    selected -> Text(
                        I18n.t("in_use_short"),
                        fontSize = 13.sp,
                        color = theme.accent
                    )
                    owned -> Button(onClick = { onSelect(d) }) {
                        Text(I18n.t("use"), fontSize = 13.sp)
                    }
                    else -> Button(
                        enabled = controller.profile.score >= d.price,
                        onClick = {
                            if (controller.purchaseDecoration(d)) {
                                onSelect(d)
                            }
                        }
                    ) {
                        Text("${d.price}", fontSize = 13.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(I18n.t("score_unit"), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

private fun decorationColor(id: String): Color = when (id) {
    "color_gold", "glow_gold" -> Color(0xFFFFD54F)
    "color_violet" -> Color(0xFFCE93D8)
    "glow_blue" -> Color(0xFF64B5F6)
    "winline_neon" -> Color(0xFFFF4081)
    else -> Color(0xFFBDBDBD)
}
