package com.example.joke_app

import android.app.Application

class App: Application() {
    companion object {
        lateinit var instance: App
    }
    init {

        instance = this
    }
    override fun onCreate() {
        super.onCreate()

    }
}