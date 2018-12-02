package com.pixelart.week4weekendgoogleplacesapi

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationListener

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.pixelart.week4weekendgoogleplacesapi.adapter.RecyclerViewAdapter
import com.pixelart.week4weekendgoogleplacesapi.model.Place
import com.pixelart.week4weekendgoogleplacesapi.model.PlaceData
import com.pixelart.week4weekendgoogleplacesapi.remote.RemoteHelper
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_places.*
import kotlinx.android.synthetic.main.place_type_menu.*


class PlacesActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener, RecyclerViewAdapter.OnItemClickedListener {
    private val TAG = "PlacesActivity"

    private val USER_REQUEST_CODE = 99

    private lateinit var mMap: GoogleMap
    private lateinit var locationRequest: LocationRequest
    private lateinit var googleApiClient : GoogleApiClient
    private lateinit var lastLocation: Location

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0

    private var mapConnected = false

    //RecyclerView
    private lateinit var layoutManager:LinearLayoutManager
    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var placeList: List<PlaceData>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        layoutManager = LinearLayoutManager(this)
        placeList = ArrayList()

        buildGoogleApiClient()

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission()
        }

        tvInfo.text = "Select a place from the menu"
        rvProgressBar.visibility = View.GONE
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)  {
            buildGoogleApiClient()
            mMap.isMyLocationEnabled = true

        }

        if (mMap.isMyLocationEnabled) {
            ibRestaurant.setOnClickListener {
                getPlacesResponse("restaurant")
                rvProgressBar.visibility = View.VISIBLE
                tvInfo.visibility = View.GONE
            }
            ibHotel.setOnClickListener {
                getPlacesResponse("lodging")
                rvProgressBar.visibility = View.VISIBLE
                tvInfo.visibility = View.GONE
            }
            ibBank.setOnClickListener {
                getPlacesResponse("bank")
                rvProgressBar.visibility = View.VISIBLE
                tvInfo.visibility = View.GONE
            }
            ibHospital.setOnClickListener {
                getPlacesResponse("hospital")
                rvProgressBar.visibility = View.VISIBLE
                tvInfo.visibility = View.GONE
            }
            ibSuperMarket.setOnClickListener {
                getPlacesResponse("supermarket")
                rvProgressBar.visibility = View.VISIBLE
                tvInfo.visibility = View.GONE
            }
        }

    }

    fun CheckGooglePlayServices(): Boolean{
        val googleAPi = GoogleApiAvailability.getInstance()
        val result: Int = googleAPi.isGooglePlayServicesAvailable(this)
        if (result != ConnectionResult.SUCCESS){
            if (googleAPi.isUserResolvableError(result))
            {
                googleAPi.getErrorDialog(this, result, 0).show()
            }
            return false
        }
        return true
    }

    fun checkPermission(): Boolean{

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), USER_REQUEST_CODE)
            }
            else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), USER_REQUEST_CODE)
            }
            return false
        }
        else{
            return true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode)
        {
            USER_REQUEST_CODE ->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if (googleApiClient == null)
                        {
                            buildGoogleApiClient()
                        }
                        mMap.isMyLocationEnabled = true
                    }
                }
                else{
                    Toast.makeText(this, "Permission Require", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    @Synchronized
    protected fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        googleApiClient.connect()
    }

    fun getPlacesResponse(type: String){
        RemoteHelper.getPlaces("${lastLocation.latitude},${lastLocation.longitude}", type).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Place>{
                override fun onSuccess(t: Place) {

                    mMap.clear()
                    (placeList as ArrayList<PlaceData>).clear()
                    for (i in 0 until t.results.size)
                    {
                        val lat = t.results[i].geometry.location.lat
                        val lng = t.results[i].geometry.location.lng
                        val placeName = t.results[i].name
                        val vicinity = t.results[i].vicinity

                        val markerOptions = MarkerOptions()
                        val latLng = LatLng(lat, lng)

                        markerOptions.position(latLng)
                        markerOptions.title("$placeName: $vicinity")
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

                        mMap.addMarker(markerOptions)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                        mMap.animateCamera(CameraUpdateFactory.zoomBy(0F))

                        /*var photoUrl = ""
                        if (t.results[i].photos.size != null){
                            for (j in 0 until t.results[i].photos.size)
                            {
                                val photoReference =  t.results[i].photos[j].photoReference
                                val maxHeight = 100
                                val maxWidth = 100

                                photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxheight=$maxHeight&maxwidth=$maxWidth&photoreference=$photoReference&key=$API_KEY"
                                Log.d(TAG, photoUrl)


                            }
                        }*/
                            //Set up recycler view data
                        (placeList as ArrayList<PlaceData>).add(
                            PlaceData(placeName, vicinity, t.results[i].rating,
                                t.results[i].priceLevel, t.results[i].icon, /*t.results[i].openingHours.openNow*/true)
                        )

                    }

                    adapter = RecyclerViewAdapter(placeList, this@PlacesActivity)
                    recyclerView.layoutManager = layoutManager
                    recyclerView.adapter = adapter

                    val status = t.status
                    if (status == "OK") {
                        rvProgressBar.visibility = View.INVISIBLE
                        tvInfo.visibility = View.INVISIBLE
                        initItemTouchHelper()
                        adapter.notifyDataSetChanged()
                    }
                    else if (status == "OVER_QUERY_LIMIT")
                    {
                        adapter.notifyDataSetChanged()
                        tvInfo.visibility = View.VISIBLE
                        tvInfo.text = "Query limit exceeded, try again later"
                        rvProgressBar.visibility = View.GONE
                        Toast.makeText(this@PlacesActivity, "$status, Try again", Toast.LENGTH_SHORT).show()
                    }

                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }

            })
    }

    override fun onConnected(bundle: Bundle?) {
        locationRequest = LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (googleApiClient.isConnected)
            {
                mapConnected = true
                lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this)
            }
        }
    }

    override fun onConnectionSuspended(i: Int) {
        Toast.makeText(this, "Connection Suspended", Toast.LENGTH_SHORT).show()

    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show()
    }

    override fun onLocationChanged(location: Location?) {
        lastLocation = location!!
        longitude = location.longitude
        latitude = location.latitude

        val latLng = LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("Current Location")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

        val userLocationMarker: Marker = mMap.addMarker(markerOptions)

        if (userLocationMarker != null)
        {
            userLocationMarker.remove()
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.zoomBy(15F))

        if (googleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
        }
    }

    override fun onItemClicked(position: Int) {
        val places = PlaceData(
            placeList[position].placeName,
            placeList[position].vicinity,
            placeList[position].rating,
            placeList[position].priceLevel,
            placeList[position].icon,
            placeList[position].openNow
        )

       startActivity(Intent(this, DetailActivity::class.java).also {
           it.putExtra("places", places)
       } )

    }

    private fun initItemTouchHelper(){
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
            override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
                val position = holder.adapterPosition

                val places = PlaceData(
                    placeList[position].placeName,
                    placeList[position].vicinity,
                    placeList[position].rating,
                    placeList[position].priceLevel,
                    placeList[position].icon,
                    placeList[position].openNow
                )

                val destination = ("${places.placeName}${places.vicinity}").replace(" ", "+")
                val mapIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destination")
                val mapIntent = Intent(Intent.ACTION_VIEW, mapIntentUri)
                mapIntent.`package` = "com.google.android.apps.maps"


                if (direction == ItemTouchHelper.LEFT)
                {
                    startActivity(mapIntent)
                    adapter.notifyDataSetChanged()
                }
                else if (direction == ItemTouchHelper.RIGHT)
                {
                    startActivity(mapIntent)
                    adapter.notifyDataSetChanged()
                }
            }

        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}
