package com.thanhdang.findmyphone.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    protected abstract fun getViewBinding(layoutInflater: LayoutInflater): VB
    protected abstract fun initArguments()
    protected abstract fun setup()

    protected abstract fun initViews()
    protected abstract fun initData()
    protected abstract fun initActions()

    lateinit var binding: VB

    var isIntentToActivity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)


        binding = getViewBinding(layoutInflater)
        setContentView(binding.root)


        initArguments()     //get arguments from previous
        setup()             // setup ....

        initViews()
        initActions()
        initData()

    }

    override fun onResume() {
        super.onResume()

        isIntentToActivity = false
    }

    fun setIntentToActivity() {
        isIntentToActivity = true
    }
}
