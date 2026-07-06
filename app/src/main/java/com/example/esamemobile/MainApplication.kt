package com.example.esamemobile

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

//Nome momentaneo, lo voglio cambiare una volta che abbiamo il nome dell'app
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(appModule)
        }
    }
}