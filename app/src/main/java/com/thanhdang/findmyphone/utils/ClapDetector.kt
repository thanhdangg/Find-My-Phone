package com.thanhdang.findmyphone.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import org.jtransforms.fft.DoubleFFT_1D
import kotlin.math.sqrt

object ClapDetector {
    private const val SAMPLE_RATE = 44100
    private const val BUFFER_SIZE = 2048

    private const val FREQUENCY_MIN = 2200 // Hz
    private const val FREQUENCY_MAX = 2800 // Hz
    private const val LOW_FREQ_THRESHOLD = 2000 // Hz
    private const val MIN_CLAP_INTERVAL = 200 // ms
    private const val MAX_CLAP_INTERVAL = 400 // ms
    private const val ENERGY_THRESHOLD = 15.0
    private const val ENERGY_RATIO_THRESHOLD = 0.1

    private var isListening = false
    private var lastClapTime: Long = 0
    private var clapCount = 0 // Biến đếm số lần vỗ tay

    fun startListening(context: Context, onDoubleClapDetected: () -> Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Audio permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        isListening = true
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            BUFFER_SIZE
        )

        audioRecord.startRecording()

        Thread {
            val audioData = ShortArray(BUFFER_SIZE)

            while (isListening) {
                val readSize = audioRecord.read(audioData, 0, BUFFER_SIZE)
                if (readSize > 0) {
                    val energyRatio = analyzeFrequency(audioData)
//                    Log.d(TAG.ClapDetector, "Energy ratio: $energyRatio")
                    if (isClap(energyRatio)) {
                        val currentTime = System.currentTimeMillis()
                        if (isDoubleClap(currentTime)) {
                            clapCount++
                            if (clapCount == 2) {
                                onDoubleClapDetected()
                                clapCount = 0 // Reset sau khi phát hiện double clap
                            }
                        }
                        Log.d(CONSTANT.ClapDetector, "${energyRatio} clap counts: $clapCount")
                        lastClapTime = currentTime
                    }
                }
            }
            audioRecord.stop()
            audioRecord.release()
        }.start()
    }

    private fun analyzeFrequency(audioData: ShortArray): Double {
        val fft = DoubleFFT_1D(audioData.size.toLong())
        val fftData = DoubleArray(audioData.size * 2)

        for (i in audioData.indices) {
            fftData[i] = audioData[i].toDouble()
        }

        fft.realForward(fftData)

        val startClapFreqIndex = (FREQUENCY_MIN * audioData.size / SAMPLE_RATE).toInt()
        val endClapFreqIndex = (FREQUENCY_MAX * audioData.size / SAMPLE_RATE).toInt()

        var clapEnergy = 0.0
        for (i in startClapFreqIndex..endClapFreqIndex) {
            val real = fftData[2 * i]
            val imaginary = fftData[2 * i + 1]
            clapEnergy += sqrt(real * real + imaginary * imaginary)
        }

        val lowFreqIndex = (LOW_FREQ_THRESHOLD * audioData.size / SAMPLE_RATE).toInt()
        var lowFreqEnergy = 0.0
        for (i in 0 until lowFreqIndex) {
            val real = fftData[2 * i]
            val imaginary = fftData[2 * i + 1]
            lowFreqEnergy += sqrt(real * real + imaginary * imaginary)
        }

        return if (lowFreqEnergy > 0) {
            clapEnergy / lowFreqEnergy
        } else {
            0.0
        }
    }

    private fun isClap(energyRatio: Double): Boolean {
        return energyRatio > ENERGY_RATIO_THRESHOLD
    }

    private fun isDoubleClap(currentTime: Long): Boolean {
        val interval = currentTime - lastClapTime
        return interval in MIN_CLAP_INTERVAL..MAX_CLAP_INTERVAL
    }

    fun stopListening() {
        isListening = false
    }
}
