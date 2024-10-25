package com.thanhdang.findmyphone.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.thanhdang.findmyphone.R
import org.jtransforms.fft.DoubleFFT_1D
import kotlin.math.sqrt

object ClapDetector {
    private const val SAMPLE_RATE = 44100
    private const val BUFFER_SIZE = 2048
    private const val CLAP_FREQUENCY_MIN = 1800
    private const val CLAP_FREQUENCY_MAX = 2800
    private const val CLAP_INTERVAL_MIN = 50 // milliseconds
    private const val CLAP_INTERVAL_MAX = 400 // milliseconds
    private const val AMPLITUDE_THRESHOLD = 1000000 // Threshold to detect clap

    private var isRecording = false
    private var lastClapTime: Long = 0
    private var clapCount = 0

    private var mediaPlayer: MediaPlayer? = null


    fun startListening(context: Context, onDoubleClapDetected: () -> Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            Toast.makeText(context, "Microphone permission required", Toast.LENGTH_SHORT).show()
            return
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
                    val maxAmplitude = magnitudes[maxMagnitudeIndex]

//                    Log.d(TAG.ClapDetector, "Detected frequency: $frequency Hz, Amplitude: ${maxAmplitude.toInt()}")

                    // Detect clap here: The sound frequency of an hand clap is typically within the 2200 to 2800 hertz range
                    // Duration: A clap is typically 0.2 – 0.3 seconds long
                    // Time interval: The time interval between claps is usually 0.2 – 0.5 seconds
                    if (frequency in CLAP_FREQUENCY_MIN..CLAP_FREQUENCY_MAX && maxAmplitude > AMPLITUDE_THRESHOLD) {
                        val currentTime = System.currentTimeMillis()

                        if (lastClapTime != 0L && (currentTime - lastClapTime) in CLAP_INTERVAL_MIN..CLAP_INTERVAL_MAX) {
                            clapCount++
                            if (clapCount == 2) {
                                Log.d(TAG.ClapDetector, "Double clap detected")
                                onDoubleClapDetected()
                                playAlarmSound(context)

                                clapCount = 0 // Reset the count after detection
                            }
                        } else {
                            clapCount = 1 // Reset the count if the interval is too long
                        }
//                        Log.d(TAG.ClapDetector, "clapCount: $clapCount")
                        lastClapTime = currentTime
                    }
                }
            }
            audioRecord.stop()
            audioRecord.release()
        }.start()
    }

    fun stopListening() {
        isRecording = false
    }
    private fun playAlarmSound(context: Context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound)
            mediaPlayer?.isLooping = true
        }
        mediaPlayer?.start()
    }
    fun stopAlarmSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}