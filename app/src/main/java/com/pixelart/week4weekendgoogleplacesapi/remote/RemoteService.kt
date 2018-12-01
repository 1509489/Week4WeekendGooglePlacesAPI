package com.pixelart.week4weekendgoogleplacesapi.remote

import com.pixelart.week4weekendgoogleplacesapi.model.Place
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface RemoteService{

    @GET("maps/api/place/nearbysearch/json")
    fun getPlaces(
        @Query("location") location: String,
        @Query("type") type: String,
        @Query("radius") radius: Int,
        @Query("sensor") sensor: Boolean,
        @Query("key") key: String
    ): Single<Place>
}