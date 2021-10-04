package com.smartherd.globofly.services

import android.os.Build
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

// Used to build retrofit service
object ServiceBuilder {

    private const val URL = "https://icanhazdadjoke.com/"

    // Create OkHttp Client with a timeout
    val okHttp = OkHttpClient.Builder()
                                        .callTimeout(5, TimeUnit.SECONDS)
    // Create Retrofit Builder
    private val builder = Retrofit.Builder().baseUrl(URL)
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .client(okHttp.build())

    // Create Retrofit Instance
    private val retrofit = builder.build()

    fun <T> buildService(serviceType: Class<T>): T {
        return retrofit.create(serviceType)
    }
}