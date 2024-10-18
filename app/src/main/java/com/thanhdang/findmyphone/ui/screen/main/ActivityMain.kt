package com.thanhdang.findmyphone.ui.screen.main

import android.content.Intent
import android.view.LayoutInflater
import com.thanhdang.findmyphone.R
import com.thanhdang.findmyphone.databinding.ActivityMainBinding
import com.thanhdang.findmyphone.helper.notification.NotificationHelper
import com.thanhdang.findmyphone.service.ClapDetectionService
import com.thanhdang.findmyphone.ui.base.BaseActivity
import com.thanhdang.findmyphone.utils.ClapDetector

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
                isListening = false
                binding.btnPower.setImageResource(R.drawable.btn_pause)

                val serviceIntent = Intent(this, ClapDetectionService::class.java)
                startForegroundService(serviceIntent)
            }
            else {
                ClapDetector.startListening(this) {
                    NotificationHelper.sendNotification(this)
                }
                isListening = true
                binding.btnPower.setImageResource(R.drawable.btn_power)

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