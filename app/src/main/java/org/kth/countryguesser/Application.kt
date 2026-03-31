package org.kth.countryguesser

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Application : Application() {

    companion object {
        lateinit var APPLICATION: Application
    }

    override fun onCreate() {
        super.onCreate()
        APPLICATION = this
    }

}