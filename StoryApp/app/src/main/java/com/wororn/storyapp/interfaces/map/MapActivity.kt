package com.wororn.storyapp.interfaces.map

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.wororn.storyapp.R
import com.wororn.storyapp.databinding.ActivityMapBinding
import com.wororn.storyapp.factory.StoriesViewModelFactory
import com.wororn.storyapp.interfaces.main.MainActivity
import com.wororn.storyapp.interfaces.main.MainViewModel
import com.wororn.storyapp.tools.Status
import java.io.IOException
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var mapbunching: ActivityMapBinding
    private val boundsBuilder = LatLngBounds.Builder()
    private lateinit var mapViewModel: MapViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var location: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapbunching = ActivityMapBinding.inflate(layoutInflater)
        setContentView(mapbunching.root)
        supportActionBar?.show()
        this.title="MAP"

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val factory: StoriesViewModelFactory = StoriesViewModelFactory.getInstance(this)
        mapViewModel = ViewModelProvider(this@MapActivity, factory)[MapViewModel::class.java]

        addmoreMarker()

    }

    @SuppressLint("NewApi")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        setMapStyle()
        getTheLocation()
        addmoreMarker()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_map, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.main -> {
                startActivity(Intent(this@MapActivity, MainActivity::class.java))
                finish()
                true
            }
            R.id.menu_map -> {
                true
            }
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }

            R.id.logout -> {
                mainViewModel.logout()
                finish()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun vectorToBitmap(@DrawableRes id: Int, @ColorInt color: Int): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
        if (vectorDrawable == null) {
            Log.e("BitmapHelper", "Resource not found")
            return BitmapDescriptorFactory.defaultMarker()
        }
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, color)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[permission.ACCESS_FINE_LOCATION] ?: false -> {
                    // Precise location access granted.
                    getTheLocation()
                    addmoreMarker()
                }
                permissions[permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    // Only approximate location access granted.
                    getTheLocation()
                    addmoreMarker()
                }
                else -> {
                    // No location access granted.
                    Toast.makeText(
                        this@MapActivity,
                        "No location access granted.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this@MapActivity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getTheLocation() {
        if (checkPermission(permission.ACCESS_FINE_LOCATION) &&
            checkPermission(permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    this.location = location
                    checkpointMarker(location)
                    Log.e("TagLocationMap","$location")
                } else {
                    Toast.makeText(
                        this@MapActivity,
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    permission.ACCESS_FINE_LOCATION,
                    permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("NewApi")
    private fun checkpointMarker(location: Location) {
                val startLocation = LatLng(location.latitude, location.longitude)
                val addressName = theAddressName(location.latitude, location.longitude)
                val titleName = theTitleName(location.latitude, location.longitude)
                mMap.addMarker(
                    MarkerOptions()
                        .position(startLocation)
                        .title(titleName)
                        .snippet(addressName)
                        .icon(
                            vectorToBitmap(
                                R.drawable.ic_baseline_house_24dp,
                                Color.parseColor("#3DDC84")
                            )
                        )
                )
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 8f))
            }

    private fun theAddressName(lat: Double, lon: Double): String? {
        var addressName: String? = null
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val list = geocoder.getFromLocation(lat, lon, 1)
            if (list != null && list.size != 0) {
                addressName = list[0].getAddressLine(0)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addressName
    }

    private fun theTitleName(lat: Double, lon: Double): String? {
        var titleName: String? = null
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val list = geocoder.getFromLocation(lat, lon, 1)
            if (list != null && list.size != 0) {
                titleName = list[0].featureName
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return titleName
    }

    private fun addmoreMarker() {
        val factory: StoriesViewModelFactory = StoriesViewModelFactory.getInstance(this)
        mainViewModel = ViewModelProvider(this@MapActivity, factory)[MainViewModel::class.java]

        mainViewModel.getToken().observe(this@MapActivity) { token ->
            if (token.isNotEmpty()) {
                mapViewModel.getAllMapData(token,1).observe(this) { map ->
                    if (map != null) {
                        when (map) {
                            is Status.Process -> {
                                val alertBuilder = AlertDialog.Builder(this).create()
                                alertBuilder.apply {
                                    setTitle("Information")
                                    setMessage("Let's get The Story Map")
                                    setIcon(R.drawable.ic_baseline_check_green_24dp)
                                    setCancelable(false)
                                }.show()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    alertBuilder.dismiss()
                                }, 2000)
                            }
                            is Status.Done -> {
                                val data = map.data
                                if (data.error) {
                                    Toast.makeText(
                                        this@MapActivity,
                                        data.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    map.data.listStory.forEach { mapping ->
                                        when {
                                            mapping.lat != null && mapping.lon != null -> {
                                                val latLng = LatLng(mapping.lat, mapping.lon)
                                                mMap.addMarker(
                                                    MarkerOptions()
                                                        .position(LatLng(mapping.lat, mapping.lon))
                                                        .title(mapping.name)
                                                        .snippet(mapping.description)
                                                        .icon(
                                                            BitmapDescriptorFactory.defaultMarker(
                                                                BitmapDescriptorFactory.HUE_RED
                                                            )
                                                        )
                                                )
                                                boundsBuilder.include(latLng)

                                            }//when
                                        }
                                    }//mapping
                                    val bounds: LatLngBounds = boundsBuilder.build()
                                    mMap.animateCamera(
                                        CameraUpdateFactory.newLatLngBounds(
                                            bounds,
                                            resources.displayMetrics.widthPixels,
                                            resources.displayMetrics.heightPixels,
                                            300
                                        )
                                    )
                                }//else
                            }//done
                            is Status.Fail -> {
                                val alertBuilder = AlertDialog.Builder(this).create()
                                alertBuilder.apply {
                                    setTitle("Fail")
                                    setMessage("Could not get The Story Map")
                                    setIcon(R.drawable.ic_baseline_error_outline_24dp)
                                    setCancelable(false)
                                }.show()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    alertBuilder.dismiss()
                                }, 2000)
                            }
                        }
                    }
                }
            }
        }}
    companion object {
        private const val TAG = "MapActivity"
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        finish()
        return true
    }
}




