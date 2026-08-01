package com.gomoku.nusv.sound

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

actual class SoundPlayer actual constructor() {

    private val synth = SoundSynth()

    actual fun play(type: SoundType) {
        val samples = synth.synthesize(synth.notesFor(type))
        Thread {
            try {
                val format = AudioFormat(44100f, 16, 1, true, false)
                val line = AudioSystem.getSourceDataLine(format)
                line.open(format)
                line.start()
                line.write(samples, 0, samples.size)
                line.drain()
                line.close()
            } catch (_: Exception) {
            }
        }.start()
    }

    actual fun dispose() {}
}
