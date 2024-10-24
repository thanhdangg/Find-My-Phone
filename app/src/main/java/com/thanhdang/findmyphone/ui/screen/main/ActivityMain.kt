package com.thanhdang.findmyphone.ui.screen.main

import android.content.Intent
import android.view.LayoutInflater
import android.widget.Toast
import com.thanhdang.findmyphone.R
import com.thanhdang.findmyphone.databinding.ActivityMainBinding
import com.thanhdang.findmyphone.helper.notification.NotificationHelper
import com.thanhdang.findmyphone.service.ClapDetectionService
import com.thanhdang.findmyphone.ui.base.BaseActivity
import com.thanhdang.findmyphone.utils.ClapDetector
import com.thanhdang.findmyphone.utils.FrequencyRecorder

class ActivityMain : BaseActivity<ActivityMainBinding>() {
    private var isListening = false
    override fun getViewBinding(layoutInflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initArguments() {
    }

    override fun setup() {
        NotificationHelper.createNotificationChannel(this)
    }

    override fun initViews() {
    }

    override fun initData() {
    }

    override fun initActions() {

        binding.btnPower.setOnClickListener {
            if (isListening) {
                ClapDetector.stopListening()
//                FrequencyRecorder.startRecording(this)
                isListening = false
                binding.btnPower.setImageResource(R.drawable.btn_pause)

//                val serviceIntent = Intent(this, ClapDetectionService::class.java)
//                startForegroundService(serviceIntent)
            }
            else {
//                FrequencyRecorder.stopRecording()
                ClapDetector.startListening(this) {
                    NotificationHelper.sendNotification(this)
                    runOnUiThread {
                        Toast.makeText(this, "Double clap detected", Toast.LENGTH_SHORT).show()
                    }
                }
                isListening = true
                binding.btnPower.setImageResource(R.drawable.btn_power)

//                stopService(Intent(this, ClapDetectionService::class.java))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val serviceIntent = Intent(this, ClapDetectionService::class.java)
//        ClapDetector.stopListening()
        stopService(serviceIntent)
    }
}