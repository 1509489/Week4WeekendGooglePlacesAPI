package com.pixelart.week4weekendgoogleplacesapi.remote

import com.pixelart.week4weekendgoogleplacesapi.API_KEY
import com.pixelart.week4weekendgoogleplacesapi.BASE_URL
import com.pixelart.week4weekendgoogleplacesapi.RADIUS
import com.pixelart.week4weekendgoogleplacesapi.model.Place
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RemoteHelper{
    fun getPlaces(location: String, type: String): Single<Place>{
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(RemoteService::class.java)

        return service.getPlaces(location, type, RADIUS, true, API_KEY)
    }
}