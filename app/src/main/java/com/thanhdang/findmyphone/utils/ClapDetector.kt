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
import kotlin.math.abs

object ClapDetector {
    private const val SAMPLE_RATE = 44100
    private const val BUFFER_SIZE = 2048
    private const val CLAP_THRESHOLD = 2000
    private const val CHECK_INTERVAL_MS = 250
    private const val DOUBLE_CLAP_INTERVAL_MS = 1000

    private var audioRecord: AudioRecord? = null
    private var isListening = false
    private var lastClapTime: Long = 0

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
            var previousAmplitude = 0
            while (isListening) {
                val read = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: 0
                if (read > 0) {
                    val maxAmplitude = buffer.maxOrNull()?.let { abs(it.toInt()) } ?: 0
                    Log.d(TAG.ClapDetector, "Max amplitude: $maxAmplitude")
                    if (maxAmplitude > CLAP_THRESHOLD && previousAmplitude < CLAP_THRESHOLD) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastClapTime <= DOUBLE_CLAP_INTERVAL_MS) {
                            Log.d(TAG.ClapDetector, "Double clap detected!")
                            onDoubleClapDetected()
                        }
                        lastClapTime = currentTime
                    }
                    previousAmplitude = maxAmplitude
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