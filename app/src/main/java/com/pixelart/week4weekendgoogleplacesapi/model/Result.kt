package com.pixelart.week4weekendgoogleplacesapi.model

import com.google.gson.annotations.SerializedName

data class Result(
    val geometry: Geometry,
    val icon: String,
    val id: String,
    val name: String,

@SerializedName("opening_hours")
    val openingHours: OpeningHours,
    val photos: List<Photo>,

@SerializedName("place_id")
    val placeId: String,

@SerializedName("plus_code")
    val plusCode: PlusCode,
@SerializedName("price_level")
    val priceLevel: Int,
    val rating: Double,
    val reference: String,
    val scope: String,
    val type: List<String>,
    val vicinity: String
)