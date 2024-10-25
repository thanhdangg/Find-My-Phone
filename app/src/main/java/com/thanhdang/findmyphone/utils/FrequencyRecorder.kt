package com.thanhdang.findmyphone.utils

import android.Manifest
import android.app.Activity
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

object FrequencyRecorder {
    private const val SAMPLE_RATE = 44100
    private const val BUFFER_SIZE = 2048

    private var isRecording = false

    fun startRecording(context: Context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            Toast.makeText(context, "Microphone permission required", Toast.LENGTH_SHORT).show()
            return
        }
        else{
            // request permission microphone
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }

        isRecording = true
        val audioBuffer = ShortArray(BUFFER_SIZE)
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            BUFFER_SIZE
        )

        audioRecord.startRecording()

        Thread {
            while (isRecording) {
                val readSize = audioRecord.read(audioBuffer, 0, BUFFER_SIZE)
                if (readSize > 0) {
                    val fft = DoubleFFT_1D(BUFFER_SIZE.toLong())
                    val fftData = DoubleArray(BUFFER_SIZE * 2)
                    for (i in audioBuffer.indices) {
                        fftData[i] = audioBuffer[i].toDouble()
                    }
                    fft.realForward(fftData)

                    val magnitudes = DoubleArray(BUFFER_SIZE / 2)
                    for (i in magnitudes.indices) {
                        val real = fftData[2 * i]
                        val imaginary = fftData[2 * i + 1]
                        magnitudes[i] = sqrt(real * real + imaginary * imaginary)
                    }

                    val maxMagnitudeIndex = magnitudes.indices.maxByOrNull { magnitudes[it] } ?: -1
                    val frequency = maxMagnitudeIndex * SAMPLE_RATE / BUFFER_SIZE
                    Log.d(TAG.FrequencyRecorder, "Detected frequency: $frequency Hz")
                }
            }
            audioRecord.stop()
            audioRecord.release()
        }.start()
    }

    fun stopRecording() {
        isRecording = false
    }
}