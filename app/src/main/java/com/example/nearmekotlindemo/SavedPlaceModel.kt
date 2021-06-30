package com.example.nearmekotlindemo

data class SavedPlaceModel(
    var name:String="",
    var address:String="",
    var placeId:String="",
    var totalRating:Int=0,
    var rating:Double=0.0,
    var lat:Double=0.0,
    var lng:Double=0.0,
)
