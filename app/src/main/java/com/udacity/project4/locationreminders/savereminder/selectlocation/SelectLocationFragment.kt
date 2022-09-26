@file:Suppress("RedundantOverride")

package com.udacity.project4.locationreminders.savereminder.selectlocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    override val viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var layout: View
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var snackbar: Snackbar
    private lateinit var locationCallback: LocationCallback
    private lateinit var mapOptionsMenu: Menu
    private var pointOfInterest: PointOfInterest? = null
    private var nightMap = false
    private var showToastTips = true
    private var isRequestingLocationUpdates = false

    @RequiresApi(Build.VERSION_CODES.N)
    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
            ) {
                enableMyLocation()
                setupLastLocationListenerAndZoom()
                setMapStyle()
                setMapLongClick()
                setOnPoiClick()
                setOnInfoClick()
            } else {
                snackbar.show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        layout = binding.root
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        updateValuesFromBundle(savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationCallback = getLocationCallback()
        snackbar = getPermissionSnack()
        return binding.root
    }

    /**
     * Retrieves the values of [isRequestingLocationUpdates] and [showToastTips] from a
     * [Bundle].
     */
    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        savedInstanceState ?: return
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES)) {
            isRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES)
            showToastTips = savedInstanceState.getBoolean(SHOWING_TOAST_TIPS)
        }
    }

    /**
     * Returns a [LocationCallback] object for [startLocationUpdates].
     */
    private fun getLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
            }
        }
    }

    /**
     * Returns a snackbar to explain the necessity of location permissions.
     */
    private fun getPermissionSnack(): Snackbar {
        return Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            getString(R.string.snack_permission_denied_explanation),
            Snackbar.LENGTH_INDEFINITE
        )
    }

    override fun onResume() {
        super.onResume()
        if (isRequestingLocationUpdates) {
            startLocationUpdates(createLocationRequest())
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        if (!isPermissionGranted()) {
            requestLocationPermissions()
        } else {
            enableMyLocation()
            setupLastLocationListenerAndZoom()
            setMapStyle()
            setMapLongClick()
            setOnPoiClick()
            setOnInfoClick()
        }
    }

    /**
     * Returns **true** if permissions to access fine location have been granted.
     */
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Requests location permissions.
     */
    private fun requestLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else { // For APIs < N
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    /**
     * Sets the map style to standard or night based on the device theme.
     */
    private fun setMapStyle() {
        try {
            nightMap = if (isDarkModeEnabled() == true) {
                map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.map_style_normal_night
                    )
                )
                true
            } else {
                map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_normal)
                )
                false
            }
        } catch (e: Resources.NotFoundException) {
            Timber.e(e.message)
        }
    }

    /**
     * Returns **true** if dark mode is enabled on devices >= API 30.
     */
    private fun isDarkModeEnabled(): Boolean? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity?.resources?.configuration?.isNightModeActive
        } else {
            return false
        }
    }

    /**
     * Enables the My Location layer if the permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        map.isMyLocationEnabled = true
        startLocationUpdates(createLocationRequest())
        map.setOnMyLocationButtonClickListener {
            if (showToastTips) {
                viewModel.showToast.postValue(getString(R.string.toast_select_location))
            }
            false
        }
    }

    /**
     * Uses a [FusedLocationProviderClient] to request location updates.
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(locationRequest: LocationRequest) {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        isRequestingLocationUpdates = true
    }

    /**
     * Returns a [LocationRequest] object for [startLocationUpdates].
     */
    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        return locationRequest
    }

    /**
     * Configures a [fusedLocationClient] last location listener and moves the camera to the
     * given coordinates.
     */
    @SuppressLint("MissingPermission")
    private fun setupLastLocationListenerAndZoom() {
        fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation: Location? ->
            if (lastLocation != null) {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(lastLocation.latitude, lastLocation.longitude), 15f
                    )
                )
                if (showToastTips) {
                    showInstructionToast(getString(R.string.toast_select_location))
                }
            } else {
                val builder = AlertDialog.Builder(requireActivity())
                    .setMessage(getString(R.string.error_last_location_null))
                    .setPositiveButton(getString(android.R.string.ok), null)
                builder.show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (snackbar.isShown) {
                    snackbar.dismiss()
                }
                enableMyLocation()
                setupLastLocationListenerAndZoom()
            }
        }
    }

    /**
     * Shows an instructional toast using the given message.
     */
    private fun showInstructionToast(message: String) {
        if (showToastTips) {
            viewModel.showToast.value = message
        }
    }

    /**
     * Configures the long click listener for the map.
     */
    private fun setMapLongClick() {
        map.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                getString(R.string.lat_long_snippet),
                latLng.latitude,
                latLng.longitude
            )
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.marker_title_dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .draggable(true)
            )
            showInstructionToast(getString(R.string.toast_select_window))
        }
    }

    /**
     * Configures the click listener for points of interest.
     */
    private fun setOnPoiClick() {
        map.setOnPoiClickListener { poi ->
            pointOfInterest = poi
            val snippet = String.format(
                Locale.getDefault(),
                getString(R.string.lat_long_snippet),
                poi.latLng.latitude,
                poi.latLng.longitude
            )
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .draggable(true)
            )
            poiMarker?.showInfoWindow()
            showInstructionToast(getString(R.string.toast_select_window))
        }
    }

    /**
     * Displays an alert dialog for location marker options.
     */
    private fun setOnInfoClick() {
        map.setOnInfoWindowClickListener { marker ->
            val builder = AlertDialog.Builder(requireActivity())
                .setMessage(getString(R.string.alert_prompt_for_option))
                .setPositiveButton(getString(R.string.alert_add_location)) { _, _ ->
                    onLocationSelected(marker)
                }
                .setNegativeButton(getString(R.string.alert_delete_location)) { _, _ -> marker.remove() }
                .setNeutralButton(getString(R.string.alert_cancel_dialog), null)
            builder.show()
        }
    }

    /**
     * Saves the location data to the [SaveReminderViewModel].
     */
    private fun onLocationSelected(marker: Marker) {
        if (pointOfInterest != null) {
            viewModel.selectedPOI.value = pointOfInterest
        }
        viewModel.reminderSelectedLocationStr.value = marker.title
        viewModel.latitude.value = marker.position.latitude
        viewModel.longitude.value = marker.position.longitude
        viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
        mapOptionsMenu = menu
        toggleToastTipIcon()
    }

    /**
     * Toggles the toast tip icon in the action bar to indicate enabled and disabled toast
     * popups.
     */
    private fun toggleToastTipIcon() {
        val toastOptions = mapOptionsMenu.getItem(TOAST_TIPS_ITEM_INDEX)
        if (showToastTips) {
            toastOptions.icon =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_enabled_toast_tips)
        } else {
            toastOptions.icon =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_disabled_toast_tips)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            toggleMapStyle(getString(R.string.map_style_normal))
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        R.id.toggle_night_map -> {
            toggleMapStyle(getString(R.string.map_style_normal_night))
            true
        }
        R.id.toggle_toast_tips -> {
            toggleToastTips()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /**
     * Toggles the toast tips icon in the action bar between enabled and disabled states.
     */
    private fun toggleToastTips() {
        if (showToastTips) {
            viewModel.showToast.postValue(getString(R.string.toast_tips_off))
            showToastTips = false
            toggleToastTipIcon()
        } else {
            viewModel.showToast.postValue(getString(R.string.toast_tips_on))
            showToastTips = true
            toggleToastTipIcon()
        }
    }

    /**
     * Toggles the map for normal and night styles.
     */
    private fun toggleMapStyle(mapName: String) {
        val mapId = resources.getIdentifier(
            mapName,
            getString(R.string.resource_def_type),
            activity?.packageName
        )
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        try {
            nightMap = if (nightMap) {
                map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(requireContext(), mapId)
                )
                false
            } else {
                map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(requireContext(), mapId)
                )
                true
            }
        } catch (e: Resources.NotFoundException) {
            Timber.e(e.message)
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    /**
     * Stops the location updates with the given [LocationCallback].
     */
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isRequestingLocationUpdates = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES, isRequestingLocationUpdates)
        outState.putBoolean(SHOWING_TOAST_TIPS, showToastTips)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (snackbar.isShown) {
            snackbar.dismiss()
        }
        stopLocationUpdates()
    }

    companion object {
        private const val TOAST_TIPS_ITEM_INDEX = 5
        private const val SHOWING_TOAST_TIPS = "SHOWING_TOAST_TIPS"
        private const val REQUESTING_LOCATION_UPDATES = "REQUESTING_LOCATION_UPDATES"
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}