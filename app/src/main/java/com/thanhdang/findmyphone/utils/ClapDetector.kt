package com.thanhdang.findmyphone.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.app.ActivityCompat
import org.jtransforms.fft.DoubleFFT_1D
import kotlin.math.sqrt

object ClapDetector {
    private const val SAMPLE_RATE = 44100
    private const val BUFFER_SIZE = 2048 // Reduced buffer size
    private const val BASE_CLAP_THRESHOLD = 1500
    private const val DOUBLE_CLAP_INTERVAL_MS = 1000
    private const val MAX_CLAP_DURATION_MS = 100
    private const val CHECK_INTERVAL_MS = 50 // Adjusted for balance

    private var audioRecord: AudioRecord? = null
    private var isListening = false
    private var lastClapTime: Long = 0
    private var clapStartTime: Long = 0
    private var dynamicThreshold = BASE_CLAP_THRESHOLD



    fun startListening(context: Context, onDoubleClapDetected: () -> Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            return
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            BUFFER_SIZE
        )

        audioRecord?.startRecording()
        isListening = true
        lastClapTime = System.currentTimeMillis()

        Thread {
            val buffer = ShortArray(BUFFER_SIZE)
            val fft = DoubleFFT_1D(BUFFER_SIZE.toLong())
            val movingAverageWindow = mutableListOf<Double>() // For moving average threshold

            while (isListening) {
                val read = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: 0
                if (read > 0) {
                    val doubleBuffer = buffer.map { it.toDouble() }.toDoubleArray()
                    fft.realForward(doubleBuffer)

                    // Analyze frequency components
                    val magnitudes = DoubleArray(BUFFER_SIZE / 2)
                    for (i in magnitudes.indices) {
                        val real = doubleBuffer[2 * i]
                        val imag = doubleBuffer[2 * i + 1]
                        magnitudes[i] = sqrt(real * real + imag * imag)
                    }

                    // Focus on the frequency range typical for claps (e.g., 1kHz to 4kHz)
                    val startFrequencyIndex = 1000 * BUFFER_SIZE / SAMPLE_RATE
                    val endFrequencyIndex = 4000 * BUFFER_SIZE / SAMPLE_RATE
                    val filteredMagnitudes = magnitudes.sliceArray(startFrequencyIndex until endFrequencyIndex)
                    val maxMagnitude = filteredMagnitudes.maxOrNull() ?: 0.0

                    // Use a moving average for ambient noise adaptation
                    if (movingAverageWindow.size > 50) { // Keep a window size of 50 readings
                        movingAverageWindow.removeAt(0)
                    }
                    movingAverageWindow.add(magnitudes.average())

                    // Adjust the threshold dynamically based on ambient noise
//                    dynamicThreshold = BASE_CLAP_THRESHOLD + (magnitudes.average() * 2).toInt()
                    dynamicThreshold = BASE_CLAP_THRESHOLD + (movingAverageWindow.average() * 1.5).toInt()

                    Log.d(TAG.ClapDetector, "Avg Noise: ${movingAverageWindow.average() * 1.5}, Dynamic Threshold: $dynamicThreshold, Max Magnitude: $maxMagnitude")

                    if (maxMagnitude > dynamicThreshold) {
                        val currentTime = System.currentTimeMillis()
                        if (clapStartTime == 0L) {
                            clapStartTime = currentTime
                        }
                        val clapDuration = currentTime - clapStartTime
                        if (clapDuration <= MAX_CLAP_DURATION_MS) {
                            if (currentTime - lastClapTime <= DOUBLE_CLAP_INTERVAL_MS) {
                                onDoubleClapDetected()
//                                handler.post { onDoubleClapDetected() }
                                Log.d(TAG.ClapDetector, "Double Clap Detected")

                            }
                            lastClapTime = currentTime
                        }
                    } else {
                        clapStartTime = 0L
                    }
                }
                Thread.sleep(CHECK_INTERVAL_MS.toLong())
            }
        }.start()
    }

    fun stopListening() {
        isListening = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}