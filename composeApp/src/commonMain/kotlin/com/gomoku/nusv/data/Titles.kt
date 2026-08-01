package com.gomoku.nusv.data

data class Title(
    val id: String,
    val nameKey: String,
    val descKey: String,
    val minWins: Int = 0,
    val minBestStreak: Int = 0,
    val minGames: Int = 0,
    val minHardWins: Int = 0
)

object Titles {

    val all: List<Title> = listOf(
        Title("novice", "title_novice", "title_novice_desc"),
        Title("starter", "title_starter", "title_starter_desc", minWins = 1),
        Title("streak3", "title_streak3", "title_streak3_desc", minBestStreak = 3),
        Title("wins10", "title_wins10", "title_wins10_desc", minWins = 10),
        Title("general", "title_general", "title_general_desc", minWins = 25),
        Title("games100", "title_games100", "title_games100_desc", minGames = 100),
        Title("hard10", "title_hard10", "title_hard10_desc", minHardWins = 10),
        Title("master", "title_master", "title_master_desc", minWins = 50, minBestStreak = 8)
    )

    fun current(profile: PlayerProfile): Title {
        val hardWins = profile.winsByDifficulty["HARD"] ?: 0
        var best = all[0]
        for (t in all) {
            if (profile.wins >= t.minWins &&
                profile.bestWinStreak >= t.minBestStreak &&
                profile.gamesPlayed >= t.minGames &&
                hardWins >= t.minHardWins
            ) {
                best = t
            }
        }
        return best
    }
}
