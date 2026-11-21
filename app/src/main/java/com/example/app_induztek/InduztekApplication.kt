package com.example.app_induztek

import android.app.Application
import com.example.app_induztek.di.AppContainer

class InduztekApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
