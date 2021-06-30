package com.example.nearmekotlindemo.interfaces

import com.example.nearmekotlindemo.SavedPlaceModel

interface SaveLocationInterface {

    fun onLocationClick(savedPlaceModel: SavedPlaceModel)
}