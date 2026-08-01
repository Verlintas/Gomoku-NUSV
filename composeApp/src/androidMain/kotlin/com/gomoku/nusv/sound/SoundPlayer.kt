package com.gomoku.nusv.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

actual class SoundPlayer actual constructor() {

    private val synth = SoundSynth()
    private var track: AudioTrack? = null

    actual fun play(type: SoundType) {
        val samples = synth.synthesize(synth.notesFor(type))
        try {
            val minSize = AudioTrack.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val newTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(44100)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(minSize, samples.size))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            newTrack.write(samples, 0, samples.size)
            track?.release()
            track = newTrack
            newTrack.play()
        } catch (_: Exception) {
        }
    }

    actual fun dispose() {
        track?.release()
        track = null
    }
}
