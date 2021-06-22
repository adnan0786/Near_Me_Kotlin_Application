package com.example.nearmekotlindemo.interfaces

import com.example.nearmekotlindemo.models.googlePlaceModel.GooglePlaceModel

interface NearLocationInterface {

    fun onSaveClick(googlePlaceModel: GooglePlaceModel)

    fun onDirectionClick(googlePlaceModel: GooglePlaceModel)
}