package com.example.nearmekotlindemo.models.googlePlaceModel


import com.squareup.moshi.Json

data class GooglePlaceModel(
    @field:Json(name = "business_status")
    val businessStatus: String?,

    @field:Json(name = "geometry")

    val geometry: GeometryModel?,

    @field:Json(name = "icon")

    val icon: String?,

    @field:Json(name = "name")

    val name: String?,

    @field:Json(name = "obfuscated_type")

    val obfuscatedType: List<Any>?,

    @field:Json(name = "photos")

    val photos: List<PhotoModel>?,

    @field:Json(name = "place_id")

    val placeId: String?,

    @field:Json(name = "rating")

    val rating: Double?,

    @field:Json(name = "reference")

    val reference: String?,

    @field:Json(name = "scope")
    val scope: String?,

    @field:Json(name = "types")
    val types: List<String>?,

    @field:Json(name = "user_ratings_total")
    val userRatingsTotal: Int?,

    @field:Json(name = "vicinity")
    val vicinity: String?,

    @Transient
    var saved: Boolean?
)