package com.gomoku.nusv.data

/** 道具类型与定价（积分购买，对局内消耗库存） */
enum class PowerupType(val key: String, val price: Int) {
    HINT("hint", 100),
    TIMEBOOST("timeboost", 80)
}

object PowerupSystem {

    const val INITIAL_HINT = 3
    const val INITIAL_TIMEBOOST = 5

    fun initialPowerups(): Map<String, Int> = mapOf(
        PowerupType.HINT.key to INITIAL_HINT,
        PowerupType.TIMEBOOST.key to INITIAL_TIMEBOOST
    )

    fun count(profile: PlayerProfile, type: PowerupType): Int =
        profile.powerups[type.key] ?: 0

    fun consume(profile: PlayerProfile, type: PowerupType): PlayerProfile {
        val current = count(profile, type)
        if (current <= 0) return profile
        val map = profile.powerups.toMutableMap()
        map[type.key] = current - 1
        return profile.copy(powerups = map)
    }

    /**
     * 购买道具。
     * @return 是否购买成功（积分足够）
     */
    fun purchase(profile: PlayerProfile, type: PowerupType, amount: Int): PlayerProfile? {
        if (amount <= 0) return null
        val cost = type.price * amount
        if (profile.score < cost) return null
        val map = profile.powerups.toMutableMap()
        map[type.key] = (map[type.key] ?: 0) + amount
        return profile.copy(
            score = profile.score - cost,
            powerups = map
        )
    }
}
