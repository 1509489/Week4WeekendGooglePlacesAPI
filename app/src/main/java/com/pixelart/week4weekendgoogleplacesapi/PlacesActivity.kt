package com.pixelart.week4weekendgoogleplacesapi

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
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
import com.pixelart.week4weekendgoogleplacesapi.model.Place
import com.pixelart.week4weekendgoogleplacesapi.remote.RemoteHelper
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.place_type_menu.*


class PlacesActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private val USER_REQUEST_CODE = 99

    private lateinit var mMap: GoogleMap
    private lateinit var locationRequest: LocationRequest
    private lateinit var googleApiClient : GoogleApiClient
    private lateinit var lastLocation: Location

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        buildGoogleApiClient()

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)  {
            buildGoogleApiClient()
            mMap.isMyLocationEnabled = true

        }

        ibRestaurant.setOnClickListener {
            getPlacesResponse("restaurant")
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
                    Toast.makeText(this@PlacesActivity, "${lastLocation.latitude},${lastLocation.longitude} $type", Toast.LENGTH_SHORT).show()

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
                lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this)
            }
        }
    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnectionFailed(result: ConnectionResult) {

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


}
