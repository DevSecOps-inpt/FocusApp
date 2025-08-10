package com.devsecopsinpt.focusapp

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FocusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FocusAppRef.appContext = applicationContext
    }
}

object FocusAppRef {
    @Volatile
    var appContext: Context? = null
}
