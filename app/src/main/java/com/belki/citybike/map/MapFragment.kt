package com.belki.citybike.map


import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.res.Resources
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import com.belki.citybike.*
import com.belki.citybike.databinding.FragmentMapBinding
import com.belki.citybike.map.Constants.MY_LOCATION_TAG
import com.belki.citybike.utils.awaitCurrentLocation
import com.belki.citybike.utils.bindImage
import com.belki.citybike.utils.foregroundLocationPermissionApproved
import com.belki.citybike.utils.getBitmap
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel


object Constants {
    const val MY_LOCATION_TAG = "My location"
}

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    /** This solution is taken from https://stackoverflow.com/questions/29657948/get-the-current-location-fast-and-once-in-android/66051728#66051728 */

    private val permissionRequester = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        when {
            isGranted -> {
                viewModel.hideBanner()
                onPermissionGranted()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                ACCESS_FINE_LOCATION
            ) -> {
                Log.d("MapFragment", "Should show request rationale")
                //viewModel.disableMyLocation()
                viewModel.showBanner("Application has no location permission", "Permit")
                {
                    Log.d("MapFragment", "Banner button with requirePermission() clicked")
                    requirePermission()
                }
            }
            else -> {
                Log.d("MapFragment", "Don't ask again -> Banner with startActivity")
                //viewModel.disableMyLocation()
                viewModel.showBanner("Application has no location permission", "Permit")
                {
                    Log.d("MapFragment", "Banner button with startActivity() clicked")
                    startActivity(
                        Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                }
            }
        }
    }

    private val locationServiceRequester = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
        onPermissionGranted(false)
    }

    private val viewModel by activityViewModel<CityBikeViewModel>()
    private lateinit var binding: FragmentMapBinding


    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        /**         Navigation drawer             */
        val navController = findNavController()

        NavigationUI.setupWithNavController(binding.navigationView, findNavController())

        binding.menuImage.setOnClickListener {
            binding.drawerLayout.open()
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            // Handle menu item selected
            menuItem.onNavDestinationSelected(navController)
            binding.drawerLayout.close()
            true
        }

        val profileImage =
            binding.navigationView.getHeaderView(0).findViewById<ImageView>(R.id.profile_image)
        bindImage(profileImage, getString(R.string.profilePhotoUrl))

        /** Bottom sheet behavior and it's Callback */

        bottomSheetBehavior = from(binding.bottomSheet)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                binding.startButton.visibility =
                    if (newState == STATE_HIDDEN || newState == STATE_COLLAPSED) View.VISIBLE
                    else View.GONE
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

        })

        viewModel.bottomSheetBehavior.observe(viewLifecycleOwner) {
            bottomSheetBehavior.state = it
        }

        /** Setting observer for navigating event */

        viewModel.startARideEvent.observe(viewLifecycleOwner) { isStarted ->
            if (isStarted) {
                findNavController().navigate(R.id.action_mapFragment_to_qrFragment)
                viewModel.finishStartARideEvent()
            }
        }

        /** Connecting GoogleMap object with MapView in our UI */

        mapView = binding.map

        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync(this)

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        setMapStyle(map)
        addStationMarkers(map)
        setOnMarkerClick(map)

        binding.customMyLocation.setOnClickListener {
            requirePermission()
        }

        viewModel.location.observe(viewLifecycleOwner) { latLng ->
//            moveToLocation(map, latLng)
            if (viewModel.myLocationEnabled) {
                showMyLocation(map, latLng)
                moveToLocation(map, latLng)
                viewModel.updateStationsDistance()
                viewModel.sortStationsByDistance()
            }
        }

        checkSettingsAndShowBanners()
    }

    private fun checkSettingsAndShowBanners() {
        if (!foregroundLocationPermissionApproved()) {
            // viewModel.disableMyLocation()
            viewModel.showBanner("Application has no location permission", "Permit") {
                requirePermission()
            }
        } else if (!locationIsSettingsEnabled()) {
            //viewModel.disableMyLocation()
            viewModel.showBanner("Device location is turned off", "Turn on") {
                onPermissionGranted()
            }
        } else {
            viewModel.enableMyLocation()
            updateLocation()
        }
        moveToLocation(map, viewModel.location.value, zoom = 10f)
    }


    private fun updateLocation() {
        lifecycleScope.launch {
            val location = LocationServices
                .getFusedLocationProviderClient(requireContext())
                .awaitCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY)
            viewModel.updateLocation(location)
        }
    }

    private fun locationIsSettingsEnabled(): Boolean {
        val lm = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(lm)
    }

    private fun addStationMarkers(googleMap: GoogleMap) {
        viewModel.stationMarkerList.observe(viewLifecycleOwner) { stationMarkers ->

            val markerBitMapFactory = BitmapDescriptorFactory.fromBitmap(
                getBitmap(requireContext(), R.drawable.ic_station_active)
            )

            stationMarkers.forEach { stationMarker ->
                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(stationMarker.latLng)
                        .icon(markerBitMapFactory)
                        .anchor(0.5f, 0.5f)
                )
                marker?.tag = stationMarker.id
                Log.d("Stations", "Station \"${stationMarker.id}\" marker added to the map")
            }
        }

    }

    private fun onPermissionGranted(resolve: Boolean = true) {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100000L).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            //viewModel.disableMyLocation()
            if (exception is ResolvableApiException && resolve) {
                try {
                    locationServiceRequester.launch(
                        IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("", "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                //viewModel.disableMyLocation()
                viewModel.showBanner("Device location is turned off", "Turn on") {
                    onPermissionGranted()
                }
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                // viewModel.enableMyLocation()
                updateLocation()
            }
        }
    }


    private fun requirePermission() {
        permissionRequester.launch(ACCESS_FINE_LOCATION)
    }

    /**       Extension functions for GoogleMap         */

    private fun showMyLocation(googleMap: GoogleMap, latLng: LatLng?) {
        latLng?.let {
            viewModel.myLocationMarker.value?.let {
                it.remove()
            }
            viewModel.updateMyLocationMarker(
                googleMap.addMarker(
                    MarkerOptions()
                        .icon(
                            BitmapDescriptorFactory.fromBitmap(
                                getBitmap(
                                    this.requireContext(),
                                    R.drawable.ic_my_location
                                )
                            )
                        )
                        .position(latLng)
                )
            )
            viewModel.myLocationMarker.value?.tag = MY_LOCATION_TAG
        }
    }

    private fun moveToLocation(googleMap: GoogleMap, latLng: LatLng?, zoom: Float = 15f) {
        latLng?.let {
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(latLng, zoom)
            )
        }
    }

    private fun setMapStyle(googleMap: GoogleMap) {
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this.requireContext(), R.raw.map_style
                )
            )
            if (!success) {
                Log.e("MapFragment", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MapFragment", "Can't find style. Error: ", e)
        }
    }

    private fun setOnMarkerClick(googleMap: GoogleMap) {
        googleMap.setOnMarkerClickListener { clickedMarker ->
            if (clickedMarker.tag != MY_LOCATION_TAG) {
                if (viewModel.selectedMarker.value != clickedMarker) {

                    /**         SWITCH MARKERS ICONS        */

                    viewModel.selectedMarker.value?.setIcon(
                        BitmapDescriptorFactory.fromBitmap(
                            getBitmap(
                                requireContext(),
                                R.drawable.ic_station_active
                            )
                        )
                    )

                    clickedMarker.setIcon(
                        BitmapDescriptorFactory.fromBitmap(
                            getBitmap(
                                requireContext(),
                                R.drawable.ic_station_focused
                            )
                        )
                    )

                    viewModel.updateSelectedMarker(clickedMarker)
                    viewModel.updateSelectedStation(clickedMarker.tag.toString())
                }

                if (bottomSheetBehavior.state != STATE_EXPANDED)
                    viewModel.expandBottomSheet()

            }
            true
        }
    }

    /**     MapView's lifecycle should repeat fragment's lifecycle    */

    override fun onResume() {
        Log.d("MapFragment", "is resumed")
        if (::mapView.isInitialized) mapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        Log.d("MapFragment", "is paused")
        if (::mapView.isInitialized) mapView.onPause()
        super.onPause()
    }

    override fun onStart() {
        if (::mapView.isInitialized) mapView.onStart()
        Log.d("MapFragment", "is started")
        super.onStart()
    }

    override fun onStop() {
        Log.d("MapFragment", "is stopped")
        if (::mapView.isInitialized) mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        Log.d("MapFragment", "is destroyed")
        if (::mapView.isInitialized) mapView.onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::mapView.isInitialized) mapView.onSaveInstanceState(outState)
    }
}



