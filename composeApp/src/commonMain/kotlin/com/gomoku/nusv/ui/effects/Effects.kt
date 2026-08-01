package com.gomoku.nusv.ui.effects

data class BoardEffect(
    val id: String,
    val nameKey: String,
    val descKey: String,
    val price: Int
)

object EffectRegistry {

    val all: List<BoardEffect> = listOf(
        BoardEffect(
            id = "ripple",
            nameKey = "fx_ripple",
            descKey = "fx_ripple_desc",
            price = 0
        ),
        BoardEffect(
            id = "starfield",
            nameKey = "fx_starfield",
            descKey = "fx_starfield_desc",
            price = 150
        ),
        BoardEffect(
            id = "hologram",
            nameKey = "fx_hologram",
            descKey = "fx_hologram_desc",
            price = 250
        ),
        BoardEffect(
            id = "neon",
            nameKey = "fx_neon",
            descKey = "fx_neon_desc",
            price = 350
        )
    )

    fun byId(id: String): BoardEffect? = all.find { it.id == id }

    val freeEffects: List<String> = all.filter { it.price == 0 }.map { it.id }

    fun isOwned(effect: BoardEffect, purchased: List<String>): Boolean =
        effect.price == 0 || effect.id in purchased
}
