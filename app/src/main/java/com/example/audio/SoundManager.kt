package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Centralized arcade sound and haptic manager.
 * Generates custom synthesized SFX using AudioTrack for zero latency and zero external asset dependencies.
 */
class SoundManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("fruit_catcher_audio", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isSoundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", true))
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun toggleSound(): Boolean {
        val next = !_isSoundEnabled.value
        _isSoundEnabled.value = next
        prefs.edit().putBoolean("sound_enabled", next).apply()
        if (next) playButtonClick()
        return next
    }

    fun playButtonClick() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playTone(frequencyHz = 880f, durationMs = 45, volume = 0.5f)
        }
        vibrate(20)
    }

    fun playFruitSpawn() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playTone(frequencyHz = 440f, durationMs = 30, volume = 0.15f)
        }
    }

    fun playCatch(points: Int) {
        if (!_isSoundEnabled.value) return
        scope.launch {
            when {
                points == 100 -> {
                    playArpeggio(listOf(523f, 659f), durationPerToneMs = 40, volume = 0.6f)
                    vibrate(30)
                }
                points == 200 -> {
                    playArpeggio(listOf(587f, 740f), durationPerToneMs = 40, volume = 0.65f)
                    vibrate(35)
                }
                points == 300 -> {
                    playArpeggio(listOf(659f, 830f, 988f), durationPerToneMs = 35, volume = 0.7f)
                    vibrate(40)
                }
                points == 400 -> {
                    playArpeggio(listOf(698f, 880f, 1046f), durationPerToneMs = 35, volume = 0.75f)
                    vibrate(45)
                }
                points == 500 -> { // Golden fruit fanfare!
                    playArpeggio(listOf(523f, 659f, 784f, 1046f, 1318f), durationPerToneMs = 50, volume = 0.9f)
                    vibrate(70)
                }
                else -> { // ZONK penalty!
                    playTone(frequencyHz = 160f, durationMs = 180, volume = 0.8f)
                    vibrate(100)
                }
            }
        }
    }

    fun playCountdownBeep(isFinal: Boolean = false) {
        if (!_isSoundEnabled.value) return
        scope.launch {
            if (isFinal) {
                playTone(frequencyHz = 1046f, durationMs = 250, volume = 0.85f)
                vibrate(60)
            } else {
                playTone(frequencyHz = 587f, durationMs = 120, volume = 0.65f)
                vibrate(30)
            }
        }
    }

    fun playGameStart() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playArpeggio(listOf(440f, 554f, 659f, 880f), durationPerToneMs = 60, volume = 0.8f)
        }
        vibrate(50)
    }

    fun playGameOver() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playArpeggio(listOf(523f, 493f, 440f, 349f), durationPerToneMs = 100, volume = 0.8f)
        }
        vibrate(80)
    }

    fun playWinner() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playArpeggio(listOf(523f, 659f, 784f, 1046f, 880f, 1046f), durationPerToneMs = 70, volume = 0.85f)
        }
        vibrate(100)
    }

    fun playCameraShutter() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playTone(frequencyHz = 1800f, durationMs = 20, volume = 0.7f)
            kotlinx.coroutines.delay(35)
            playTone(frequencyHz = 1400f, durationMs = 60, volume = 0.8f)
        }
        vibrate(40)
    }

    private fun vibrate(durationMs: Long) {
        try {
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }

    private fun playTone(frequencyHz: Float, durationMs: Int, volume: Float) {
        val sampleRate = 44100
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt().coerceAtLeast(1)
        val generatedSnd = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Sine wave with soft attack & decay envelope to avoid audio clicks
            val envelope = when {
                i < numSamples * 0.1f -> i / (numSamples * 0.1f)
                i > numSamples * 0.85f -> (numSamples - i) / (numSamples * 0.15f)
                else -> 1f
            }
            val sample = sin(2.0 * Math.PI * frequencyHz * t) * envelope * volume * Short.MAX_VALUE
            generatedSnd[i] = sample.toInt().toShort()
        }

        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(generatedSnd, 0, numSamples)
            audioTrack.play()
            // release after playing
            scope.launch {
                kotlinx.coroutines.delay(durationMs.toLong() + 50)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    private fun playArpeggio(frequencies: List<Float>, durationPerToneMs: Int, volume: Float) {
        frequencies.forEach { freq ->
            playTone(freq, durationPerToneMs, volume)
            Thread.sleep((durationPerToneMs * 0.85f).toLong())
        }
    }
}
