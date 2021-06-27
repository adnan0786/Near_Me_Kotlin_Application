package com.example.nearmekotlindemo.models.googlePlaceModel.directionPlaceModel

import com.example.nearmekotlindemo.models.googlePlaceModel.directionPlaceModel.DirectionStepModel
import com.squareup.moshi.Json

data class DirectionLegModel(
    @field:Json(name = "distance")

    val distance: DirectionDistanceModel? = null,

    @field:Json(name = "duration")

    val duration: DirectionDurationModel? = null,

    @field:Json(name = "end_address")

    val endAddress: String? = null,

    @field:Json(name = "end_location")

    val endLocation: EndLocationModel? = null,

    @field:Json(name = "start_address")

    val startAddress: String? = null,

    @field:Json(name = "start_location")

    val startLocation: StartLocationModel? = null,

    @field:Json(name = "steps")

    val steps: List<DirectionStepModel>? = null
)