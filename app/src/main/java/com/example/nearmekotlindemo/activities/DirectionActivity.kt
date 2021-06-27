package com.example.nearmekotlindemo.activities

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.adapter.DirectionStepAdapter
import com.example.nearmekotlindemo.databinding.ActivityDirectionBinding
import com.example.nearmekotlindemo.databinding.BottomSheetLayoutBinding
import com.example.nearmekotlindemo.models.googlePlaceModel.GoogleResponseModel
import com.example.nearmekotlindemo.models.googlePlaceModel.directionPlaceModel.DirectionLegModel
import com.example.nearmekotlindemo.models.googlePlaceModel.directionPlaceModel.DirectionResponseModel
import com.example.nearmekotlindemo.models.googlePlaceModel.directionPlaceModel.DirectionRouteModel
import com.example.nearmekotlindemo.permissions.AppPermissions
import com.example.nearmekotlindemo.utility.LoadingDialog
import com.example.nearmekotlindemo.utility.State
import com.example.nearmekotlindemo.viewModels.LocationViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect

class DirectionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityDirectionBinding
    private var mGoogleMap: GoogleMap? = null
    private lateinit var appPermissions: AppPermissions
    private var isLocationPermissionOk = false
    private var isTrafficEnable = false
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
    private lateinit var bottomSheetLayoutBinding: BottomSheetLayoutBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var currentLocation: Location
    private var endLat: Double? = null
    private var endLng: Double? = null
    private lateinit var placeId: String
    private lateinit var adapterStep: DirectionStepAdapter
    private val locationViewModel: LocationViewModel by viewModels()
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var permissionToRequest = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDirectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.apply {
            endLat = getDoubleExtra("lat", 0.0)
            endLng = getDoubleExtra("lng", 0.0)
            placeId = getStringExtra("placeId")!!
        }


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        appPermissions = AppPermissions()
        loadingDialog = LoadingDialog(this)

        bottomSheetLayoutBinding = binding.bottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayoutBinding.root)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        adapterStep = DirectionStepAdapter()

        bottomSheetLayoutBinding.stepRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DirectionActivity)
            setHasFixedSize(false)
            adapter = adapterStep
        }


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.directionMap) as SupportMapFragment?

        mapFragment?.getMapAsync(this)

        binding.enableTraffic.setOnClickListener {
            if (isTrafficEnable) {
                mGoogleMap?.isTrafficEnabled = false
                isTrafficEnable = false
            } else {
                mGoogleMap?.isTrafficEnabled = true
                isTrafficEnable = true
            }
        }


        binding.travelMode.setOnCheckedChangeListener { _, checked ->
            if (checked != -1) {
                when (checked) {
                    R.id.btnChipDriving -> getDirection("driving")
                    R.id.btnChipWalking -> getDirection("walking")
                    R.id.btnChipBike -> getDirection("bicycling")
                    R.id.btnChipTrain -> getDirection("transit")
                }
            }
        }

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isLocationPermissionOk =
                    permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
                            && permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true

                if (isLocationPermissionOk)
                    setUpGoogleMap()
                else
                    Snackbar.make(binding.root, "Location permission denied", Snackbar.LENGTH_LONG)
                        .show()

            }


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        else
            super.onBackPressed()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        when {
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                isLocationPermissionOk = true
                setUpGoogleMap()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission")
                    .setMessage("Near me required location permission to access your location")
                    .setPositiveButton("Ok") { _, _ ->
                        requestLocation()
                    }.create().show()
            }

            else -> {
                requestLocation()
            }
        }
    }

    private fun setUpGoogleMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionOk = false
            return
        }
        mGoogleMap?.isMyLocationEnabled = true
        mGoogleMap?.uiSettings?.isTiltGesturesEnabled = true
        mGoogleMap?.uiSettings?.isMyLocationButtonEnabled = false
        mGoogleMap?.uiSettings?.isCompassEnabled = false

        getCurrentLocation()

    }

    private fun getCurrentLocation() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionOk = false
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {

            if (it != null) {
                currentLocation = it
                getDirection("driving")
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestLocation() {
        permissionToRequest.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissionToRequest.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)

        permissionLauncher.launch(permissionToRequest.toTypedArray())
    }

    private fun getDirection(mode: String) {

        if (isLocationPermissionOk) {
            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + currentLocation.latitude + "," + currentLocation.longitude +
                    "&destination=" + endLat + "," + endLng +
                    "&mode=" + mode +
                    "&key=" + resources.getString(R.string.API_KEY)

            lifecycleScope.launchWhenStarted {
                locationViewModel.getDirection(url).collect {
                    when (it) {
                        is State.Loading -> {
                            if (it.flag == true) {
                                loadingDialog.startLoading()
                            }
                        }

                        is State.Success -> {
                            loadingDialog.stopLoading()
                            clearUI()

                            val directionResponseModel: DirectionResponseModel =
                                it.data as DirectionResponseModel
                            val routeModel: DirectionRouteModel =
                                directionResponseModel.directionRouteModels!![0]

                            supportActionBar!!.title = routeModel.summary
                            val legModel: DirectionLegModel = routeModel.legs?.get(0)!!
                            binding.apply {
                                txtStartLocation.text = legModel.startAddress
                                txtEndLocation.text = legModel.endAddress
                            }

                            bottomSheetLayoutBinding.apply {
                                txtSheetTime.text = legModel.duration?.text
                                txtSheetDistance.text = legModel.distance?.text
                            }

                            adapterStep.setDirectionStepModels(legModel.steps!!)

                            val stepList: MutableList<LatLng> = ArrayList()

                            val options = PolylineOptions().apply {
                                width(25f)
                                color(Color.BLUE)
                                geodesic(true)
                                clickable(true)
                                visible(true)
                            }

                            val pattern: List<PatternItem>

                            if (mode == "walking") {
                                pattern = listOf(
                                    Dot(), Gap(10f)
                                )

                                options.jointType(JointType.ROUND)
                            } else {

                                pattern = listOf(
                                    Dash(30f)
                                )

                            }

                            options.pattern(pattern)
                            for (stepModel in legModel.steps) {
                                val decodedList = decode(stepModel.polyline?.points!!)
                                for (latLng in decodedList) {
                                    stepList.add(
                                        LatLng(
                                            latLng.lat,
                                            latLng.lng
                                        )
                                    )
                                }
                            }

                            options.addAll(stepList)
                            mGoogleMap?.addPolyline(options)
                            val startLocation = com.google.android.gms.maps.model.LatLng(
                                legModel.startLocation?.lat!!,
                                legModel.startLocation.lng!!
                            )

                            val endLocation = com.google.android.gms.maps.model.LatLng(
                                legModel.endLocation?.lat!!,
                                legModel.endLocation.lng!!
                            )

                            mGoogleMap?.addMarker(
                                MarkerOptions()
                                    .position(endLocation)
                                    .title("End Location")
                            )

                            mGoogleMap?.addMarker(
                                MarkerOptions()
                                    .position(startLocation)
                                    .title("Start Location")
                            )

                            val builder = LatLngBounds.builder()
                            builder.include(endLocation).include(startLocation)
                            val latLngBounds = builder.build()


                            mGoogleMap?.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(
                                    latLngBounds, 0
                                )
                            )


                        }
                        is State.Failed -> {
                            loadingDialog.stopLoading()
                            Snackbar.make(
                                binding.root, it.error,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun clearUI() {
        mGoogleMap?.clear()
        binding.txtStartLocation.text = ""
        binding.txtEndLocation.text = ""
        supportActionBar!!.title = ""
        bottomSheetLayoutBinding.txtSheetDistance.text = ""
        bottomSheetLayoutBinding.txtSheetTime.text = ""
    }

    private fun decode(points: String): List<com.google.maps.model.LatLng> {
        val len = points.length
        val path: MutableList<com.google.maps.model.LatLng> = java.util.ArrayList(len / 2)
        var index = 0
        var lat = 0
        var lng = 0
        while (index < len) {
            var result = 1
            var shift = 0
            var b: Int
            do {
                b = points[index++].toInt() - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            result = 1
            shift = 0
            do {
                b = points[index++].toInt() - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            path.add(com.google.maps.model.LatLng(lat * 1e-5, lng * 1e-5))
        }
        return path
    }
}