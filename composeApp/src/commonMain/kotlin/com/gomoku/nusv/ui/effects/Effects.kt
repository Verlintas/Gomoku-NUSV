package com.gomoku.nusv.ui.effects

data class BoardEffect(
    val id: String,
    val nameKey: String,
    val descKey: String
)

object EffectRegistry {

    val all: List<BoardEffect> = listOf(
        BoardEffect(
            id = "ripple",
            nameKey = "fx_ripple",
            descKey = "fx_ripple_desc",
        ),
        BoardEffect(
            id = "starfield",
            nameKey = "fx_starfield",
            descKey = "fx_starfield_desc",
        ),
        BoardEffect(
            id = "hologram",
            nameKey = "fx_hologram",
            descKey = "fx_hologram_desc",
        ),
        BoardEffect(
            id = "neon",
            nameKey = "fx_neon",
            descKey = "fx_neon_desc",
        )
    )

    fun byId(id: String): BoardEffect? = all.find { it.id == id }

    val allEffectIds: List<String> = all.map { it.id }
}
