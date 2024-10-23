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
    private const val CLAP_THRESHOLD_MAX = 30000
    private const val CLAP_TIME_MIN = 50 // milliseconds
    private const val CLAP_TIME_MAX = 500 // milliseconds
    private const val DOUBLE_CLAP_TIME = 700 // milliseconds

    private var isListening = false
    private var firstClapTime = 0L
    private var lastClapTime = 0L
    private var clapCount = 0

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
        val buffer = ShortArray(BUFFER_SIZE)
        val fft = DoubleFFT_1D(BUFFER_SIZE.toLong())

        Thread {
            while (isListening) {
                val read = audioRecord.read(buffer, 0, BUFFER_SIZE)
                if (read > 0) {
                    val currentTime = System.currentTimeMillis()
                    if (detectClap(buffer, fft)) {
                        if (currentTime - lastClapTime in CLAP_TIME_MIN..CLAP_TIME_MAX) {
                            clapCount++
                            if (clapCount == 2 && currentTime - firstClapTime <= DOUBLE_CLAP_TIME) {
                                onDoubleClapDetected()
                                clapCount = 0
                                Log.d(TAG.ClapDetector, "Double clap detected")
                            }
                        } else {
                            clapCount = 1
                            firstClapTime = currentTime
                        }
                        Log.d(TAG.ClapDetector, "Clap count: $clapCount")
                        lastClapTime = currentTime
                    }
                }
            }
            audioRecord.stop()
            audioRecord.release()
        }.start()
    }

    private fun detectClap(buffer: ShortArray, fft: DoubleFFT_1D): Boolean {
        val audioData = buffer.map { it.toDouble() }.toDoubleArray()
        fft.realForward(audioData)
        val magnitude = sqrt(audioData[0] * audioData[0] + audioData[1] * audioData[1])
//        Log.d(TAG.ClapDetector, "Magnitude: $magnitude")
        return magnitude > CLAP_THRESHOLD_MAX
    }

    fun stopListening() {
        isListening = false
    }
}