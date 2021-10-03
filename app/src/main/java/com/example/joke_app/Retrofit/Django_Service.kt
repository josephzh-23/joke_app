package com.smartherd.globofly.services

import com.example.joke_app.Joke
import com.google.gson.JsonObject

import retrofit2.Call
import retrofit2.http.*


interface Django_Service {


    @Headers("Accept: application/json")
	@GET(".")
    fun get_random_jokes(): Call<Joke>



}