package com.example.nearmekotlindemo.network

import com.example.nearmekotlindemo.models.googlePlaceModel.GoogleResponseModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface RetrofitApi {

    @GET
    suspend fun getNearByPlaces(@Url url: String): Response<GoogleResponseModel>
}