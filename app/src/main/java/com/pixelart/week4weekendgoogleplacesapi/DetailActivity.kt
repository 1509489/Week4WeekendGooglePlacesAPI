package com.pixelart.week4weekendgoogleplacesapi

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.pixelart.week4weekendgoogleplacesapi.model.PlaceData
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        val places = intent.getParcelableExtra<PlaceData>("places")
        tvName.text = places.placeName
        tvAddress.text = places.vicinity
        tvRatings.text = places.rating.toString()

        if (places.openNow)
            tvOpenHours.text = "Open now"
        else
            tvOpenHours.text = "Closed"

        //tvPriceLevel.text = "${places.priceLevel}"

        when(places.priceLevel){
            0 ->{
                tvPriceLevel.text = "Price Level: Not available"
            }
            1 ->{
                tvPriceLevel.text = "Price Level: £"
            }
            2 ->{
                tvPriceLevel.text = "Price Level: ££"
            }
            3 ->{
                tvPriceLevel.text = "Price Level: £££"
            }
            4 ->{
                tvPriceLevel.text = "Price Level: ££££"
            }
            5 ->{
                tvPriceLevel.text = "Price Level: £££££"
            }
        }

        ratingBar.rating = places.rating.toFloat()


        GlideApp.with(this)
            .load(places.icon)
            .placeholder(R.drawable.placeholder)
            .override(385,250)
            .fitCenter()
            .into(ivPhoto)

        btnDirection.setOnClickListener {
            val destination = ("${places.placeName}${places.vicinity}").replace(" ", "+")
            val mapIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destination")
            val mapIntent = Intent(Intent.ACTION_VIEW, mapIntentUri)
            mapIntent.`package` = "com.google.android.apps.maps"
            startActivity(mapIntent)
        }
    }
}
