package com.whip

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import java.awt.Toolkit
import kotlin.math.pow

class WhipSound {
    private val sampleRate = 44100
    private val audioFormat = AudioFormat(sampleRate.toFloat(), 16, 1, true, false)

    fun playCrack(intensity: Double) {
        try {
            val line = AudioSystem.getSourceDataLine(audioFormat)
            line.open(audioFormat, sampleRate / 5)
            line.start()

            // Noise burst (crack)
            val duration = 0.2
            val samples = (sampleRate * duration).toInt()
            val data = ByteArray(samples * 2)

            for (i in 0 until samples) {
                val env = Math.exp(-i.toDouble() / samples * 8)
                val noise = (Math.random() * 2 - 1) * env
                val sampleInt = (noise * 0.78 * intensity * Short.MAX_VALUE)
                    .toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                data[i * 2] = (sampleInt and 0xFF).toByte()
                data[i * 2 + 1] = ((sampleInt ushr 8) and 0xFF).toByte()
            }

            line.write(data, 0, data.size)
            line.drain()
            line.close()

            // Low-frequency thwack (sine sweep)
            val thwackLine = AudioSystem.getSourceDataLine(audioFormat)
            thwackLine.open(audioFormat, sampleRate / 7)
            thwackLine.start()

            val thwackSamples = (sampleRate * 0.12).toInt()
            val thwackData = ByteArray(thwackSamples * 2)
            val startFreq = 160.0
            val endFreq = 42.0

            for (i in 0 until thwackSamples) {
                val t = i.toDouble() / thwackSamples
                val freq = startFreq * (endFreq / startFreq).pow(t)
                val phase = 2 * Math.PI * freq * i / sampleRate
                val env = Math.exp(-t * 8)
                val sampleInt = (Math.sin(phase) * 0.54 * intensity * env * Short.MAX_VALUE)
                    .toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                thwackData[i * 2] = (sampleInt and 0xFF).toByte()
                thwackData[i * 2 + 1] = ((sampleInt ushr 8) and 0xFF).toByte()
            }

            thwackLine.write(thwackData, 0, thwackData.size)
            thwackLine.drain()
            thwackLine.close()
        } catch (e: Exception) {
            Toolkit.getDefaultToolkit().beep()
        }
    }
}

