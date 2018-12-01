package com.pixelart.week4weekendgoogleplacesapi.model

import com.google.gson.annotations.SerializedName

data class OpeningHours(
    @SerializedName("open_now")
    val openNow: Boolean
)