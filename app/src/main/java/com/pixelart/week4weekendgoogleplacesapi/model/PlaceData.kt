package com.pixelart.week4weekendgoogleplacesapi.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlaceData(
    val placeName: String,
    val vicinity: String,
    val rating: Double,
    val priceLevel: Int,
    val icon: String,
    val openNow: Boolean
): Parcelable