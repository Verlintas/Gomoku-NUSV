package com.gomoku.nusv.sound

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

internal class SoundSynth(private val sampleRate: Int = 44100) {

    data class Note(val freq: Double, val duration: Double, val gain: Double = 0.8)

    fun notesFor(type: SoundType): List<Note> = when (type) {
        SoundType.PLACE -> listOf(Note(880.0, 0.12, 0.9))
        SoundType.WIN -> listOf(Note(523.25, 0.14), Note(659.25, 0.14), Note(783.99, 0.24))
        SoundType.DRAW -> listOf(Note(392.0, 0.14), Note(329.63, 0.22))
        SoundType.TIMEOUT -> listOf(Note(220.0, 0.4, 0.7))
    }

    fun synthesize(notes: List<Note>): ByteArray {
        val total = notes.sumOf { it.duration }
        val n = (total * sampleRate).toInt()
        val pcm = ByteArray(n * 2)
        var offset = 0
        for (note in notes) {
            val len = (note.duration * sampleRate).toInt()
            for (i in 0 until len) {
                val t = i.toDouble() / sampleRate
                val env = exp(-4.0 * t / note.duration)
                val value = 0.7 * note.gain * env * sin(2 * PI * note.freq * t)
                val sample = (value * 32767).toInt().coerceIn(-32768, 32767)
                pcm[offset * 2] = (sample and 0xFF).toByte()
                pcm[offset * 2 + 1] = ((sample shr 8) and 0xFF).toByte()
                offset++
            }
        }
        return pcm
    }
}
