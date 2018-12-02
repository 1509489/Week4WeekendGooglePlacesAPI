package com.pixelart.week4weekendgoogleplacesapi.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.pixelart.week4weekendgoogleplacesapi.R
import com.pixelart.week4weekendgoogleplacesapi.model.PlaceData
import com.pixelart.week4weekendgoogleplacesapi.GlideApp


class RecyclerViewAdapter(var placeList: List<PlaceData>, val listener: OnItemClickedListener): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    lateinit var context: Context

    override fun onCreateViewHolder(viewGroup: ViewGroup, type: Int): RecyclerViewAdapter.ViewHolder {
        context = viewGroup.context
        val view = LayoutInflater.from(context).inflate(R.layout.recyclerview_layout, viewGroup, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return placeList.size
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        val place = placeList[position]

        holder.placeName.text = place.placeName
        holder.rating.text = "${place.rating}"

        when(place.priceLevel){
            0 ->{
                holder.priceLevel.text = ""
            }
            1 ->{
                holder.priceLevel.text = "£"
            }
            2 ->{
                holder.priceLevel.text = "££"
            }
            3 ->{
                holder.priceLevel.text = "£££"
            }
            4 ->{
                holder.priceLevel.text = "££££"
            }
            5 ->{
                holder.priceLevel.text = "£££££"
            }
        }

        if (place.openNow)
            holder.openNow.text = "Open now"
        else
            holder.openNow.text = "Closed"

        holder.ratingBar.rating = place.rating.toFloat()
            GlideApp.with(context)
                .load(place.icon)
                .placeholder(R.drawable.placeholder)
                .override(100,100)
                .into(holder.icon)


        holder.itemView.setOnClickListener {
            listener.onItemClicked(position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val placeName : TextView = itemView.findViewById(R.id.tvName)
        val rating : TextView = itemView.findViewById(R.id.tvRating)
        val priceLevel: TextView = itemView.findViewById(R.id.tvPriceLevel)
        val openNow : TextView = itemView.findViewById(R.id.tvOpenNow)
        val icon: ImageView = itemView.findViewById(R.id.ivIcon)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
    }


    interface OnItemClickedListener{
        fun onItemClicked(position: Int)
    }

}