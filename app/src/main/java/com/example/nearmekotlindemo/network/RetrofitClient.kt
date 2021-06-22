package com.example.nearmekotlindemo.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://maps.googleapis.com/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
    }

    val retrofitApi: RetrofitApi by lazy {
        retrofit.create(RetrofitApi::class.java)
    }


}