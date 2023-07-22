package com.wororn.storyapp.interfaces.stories

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.wororn.storyapp.R
import com.wororn.storyapp.databinding.ActivityDataStoriesBinding
import com.wororn.storyapp.factory.StoriesViewModelFactory
import com.wororn.storyapp.interfaces.camera.CameraActivity
import com.wororn.storyapp.interfaces.login.LoginActivity
import com.wororn.storyapp.interfaces.main.MainActivity
import com.wororn.storyapp.interfaces.main.MainViewModel
import com.wororn.storyapp.tools.Status
import com.wororn.storyapp.tools.reduceFileImage
import com.wororn.storyapp.tools.rotateBitmap
import com.wororn.storyapp.tools.uriToFile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class DataStoriesActivity : AppCompatActivity() {
    private lateinit var adddatabunching: ActivityDataStoriesBinding
    private lateinit var storiesViewModel: StoriesViewModel
    private lateinit var mainViewModel: MainViewModel

    private var getFile: File? = null
    private var location: Location? = null

    private var lat: RequestBody? = null
    private var lon: RequestBody? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this@DataStoriesActivity,
                    resources.getString(R.string.no_permission),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
       ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adddatabunching = ActivityDataStoriesBinding.inflate(layoutInflater)
        setContentView(adddatabunching.root)

        this.title = resources.getString(R.string.data_stories)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getTheLocation()
        settingViewModel()

        adddatabunching.btnCamera.setOnClickListener { startCamera()}
        adddatabunching.btnGallery.setOnClickListener { startGallery() }
        adddatabunching.btnUpload.setOnClickListener { uploadImage() }

    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    // Precise location access granted.
                    getTheLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    // Only approximate location access granted.
                    getTheLocation()
                }
                else -> {
                    // No location access granted.
                    Toast.makeText(
                        this@DataStoriesActivity,
                        "No location access granted.",
                        Toast.LENGTH_SHORT
                    ).show()
                    lat = Vertical.toRequestBody("text/plain".toMediaType())
                    lon = Horisontal.toRequestBody("text/plain".toMediaType())
                }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this@DataStoriesActivity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

     private fun getTheLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    this.location = location
                    Log.e("TagLocationStories","$location")
                    lat = location.latitude.toString().toRequestBody("text/plain".toMediaType())
                    lon = location.longitude.toString().toRequestBody("text/plain".toMediaType())
                } else {
                    Toast.makeText(
                        this@DataStoriesActivity,
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                    lat = Vertical.toRequestBody("text/plain".toMediaType())
                    lon = Horisontal.toRequestBody("text/plain".toMediaType())
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun settingViewModel() {
        val factoryMain: StoriesViewModelFactory = StoriesViewModelFactory.getInstance(this)
        mainViewModel = ViewModelProvider(this@DataStoriesActivity, factoryMain)[MainViewModel::class.java]

        val factory: StoriesViewModelFactory = StoriesViewModelFactory.getInstance(this)
        storiesViewModel = ViewModelProvider(this@DataStoriesActivity, factory)[StoriesViewModel::class.java]

        storiesViewModel.getToken().observe(this@DataStoriesActivity) { token ->
            if (token.isEmpty()) {
                Toast.makeText(
                    this@DataStoriesActivity,
                    "Watch Out: The Token is Empty,You should login again",
                    Toast.LENGTH_SHORT
                ).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(
                        this@DataStoriesActivity,
                        LoginActivity::class.java
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }, 2000)
            }
        }
        adddatabunching.btnUpload.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    noEmptyDesc()

                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)

        val addMenu = menu.findItem(R.id.data_stories)

        val aboutMenu = menu.findItem(R.id.about)
        val themeMenu = menu.findItem(R.id.theme_mode)

        addMenu.isVisible = false
        aboutMenu.isVisible = false
        themeMenu.isVisible = false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.main -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            R.id.settings -> {
                val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(intent)
            }
            R.id.logout -> {
                mainViewModel.logout()
                finish()
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    private fun startCamera() {
        val intent = Intent(this@DataStoriesActivity, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, resources.getString(R.string.choose_image))
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this@DataStoriesActivity)
            getFile = myFile
            adddatabunching.previewImage.setImageURI(selectedImg)
        }
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            getFile = myFile

            val result = rotateBitmap(
                BitmapFactory.decodeFile(myFile.path),
                isBackCamera
            )
            adddatabunching.previewImage.setImageBitmap(result)
        }
    }
    private fun noEmptyDesc():String {
        val desc = adddatabunching.ceDesc.text.toString()
        if(desc =="") {
            adddatabunching.ceDesc.error =  resources.getString(R.string.input_message, "Description")
        }
        return desc
    }

    private fun uploadImage() = if (getFile != null) {
        storiesViewModel.getToken().observe(this@DataStoriesActivity) { token ->
            if (token.isNotEmpty()) {
                val ceDescText = adddatabunching.ceDesc.text.toString()
                val description = ceDescText.toRequestBody("text/plain".toMediaType())
                val file = reduceFileImage(getFile as File)
                val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    file.name,
                    requestImageFile
                )

                storiesViewModel.addFieldStories(token, imageMultipart, description, lat, lon)
                    .observe(this) {
                        if (it != null) {
                            when (it) {
                                is Status.Process -> {
                                    showLoading(true)
                                }
                                is Status.Fail -> {
                                    Toast.makeText(
                                        this@DataStoriesActivity,
                                        resources.getString(R.string.dataStories_error),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                is Status.Done -> {
                                    showLoading(false)
                                    val data = it.data
                                    if (data.error) {
                                        Toast.makeText(
                                            this@DataStoriesActivity,
                                            data.message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        val alertBuilder = AlertDialog.Builder(this).create()
                                        alertBuilder.apply {
                                            setTitle("Information")
                                            setMessage(resources.getString(R.string.store_message))
                                            setIcon(R.drawable.ic_baseline_check_green_24dp)
                                            setCancelable(false)
                                        }.show()
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            alertBuilder.dismiss()
                                            val intent = Intent(
                                                this@DataStoriesActivity,
                                                MainActivity::class.java
                                            )
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            startActivity(intent)

                                        }, 2000)
                                    }
                                }
                            }
                        }//it=null
                        else {
                            showLoading(false)
                            Toast.makeText(
                                this@DataStoriesActivity,
                                "Watch Out: The Connection might in Trouble",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }//viewmodeladd
            }//token
        }//viewmodel
            showLoading(false)

        } else {
            Toast.makeText(
                this@DataStoriesActivity,
                resources.getString(R.string.no_image),
                Toast.LENGTH_SHORT
            ).show()
              showLoading(false)
        }//getfile

        private fun showLoading(isLoading: Boolean) {
            adddatabunching.progressBarLayout.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }

        companion object {
            private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
            private const val REQUEST_CODE_PERMISSIONS = 10
            const val CAMERA_X_RESULT = 200
            var Vertical= "0.00"
            var Horisontal= "0.00"
        }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        finish()
        return true
    }
    }
