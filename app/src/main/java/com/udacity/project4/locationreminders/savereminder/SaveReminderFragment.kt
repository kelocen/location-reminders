package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber


@SuppressLint("UnspecifiedImmutableFlag")
class SaveReminderFragment : BaseFragment() {
    override val viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var reminderDataItem: ReminderDataItem
    private var snackbar: Snackbar? = null
    private val pass: Unit = Unit // Placeholder for empty blocks

    /**
     * Configures a [PendingIntent] for [GeofenceBroadcastReceiver].
     */
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupSelectLocationButton()
        setupSaveReminderButton()
    }

    /**
     * Configures the [onClickListener][View.setOnClickListener] for the **Select Location** button.
     */
    private fun setupSelectLocationButton() {
        binding.selectLocation.setOnClickListener {
            viewModel.navigationCommand.value =
                NavigationCommand.To(
                    SaveReminderFragmentDirections.toSelectLocation()
                )
        }
    }

    /**
     * Configures the [onClickListener][View.setOnClickListener] for the **Save Reminder** button.
     */
    private fun setupSaveReminderButton() {
        binding.saveReminder.setOnClickListener {
            reminderDataItem = ReminderDataItem(
                viewModel.reminderTitle.value,
                viewModel.reminderDescription.value, viewModel.reminderSelectedLocationStr.value,
                viewModel.latitude.value, viewModel.longitude.value
            )
            if (viewModel.validateEnteredData(reminderDataItem)) {
                checkPermissionsAndStartGeofencing()
            }
        }
    }

    /**
     * Checks the location permissions and begins geofencing.
     */
    private fun checkPermissionsAndStartGeofencing() {
        var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (!isForegroundGranted()) {
            requestForegroundLocationPermissions(permissions)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions += arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            if (!isBackgroundGranted()) {
                requestBackgroundLocationPermissions(permissions)
            } else {
                checkSettingsAndStartGeoFence()
            }
        } else {
            checkSettingsAndStartGeoFence()
        }
    }

    /**
     * Returns **true** if permission has been granted to access fine location.
     */
    private fun isForegroundGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    /**
     * Requests permissions for fine location.
     */
    private fun requestForegroundLocationPermissions(permissions: Array<String>) {
        requestPermissions(
            permissions,
            FOREGROUND_RESULT_CODE
        )
    }

    /**
     * Returns **true** if permission has been granted to access background location.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isBackgroundGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    /**
     * Requests permissions for background location.
     */
    private fun requestBackgroundLocationPermissions(permissions: Array<String>) {
        val builder = AlertDialog.Builder(requireActivity())
            .setMessage(getString(R.string.alert_background_access_explanation))
            .setPositiveButton(getString(android.R.string.ok), null)
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            builder.setOnDismissListener {
                requestPermissions(
                    permissions,
                    FOREGROUND_AND_BACKGROUND_RESULT_CODE
                )
            }
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            builder.setOnDismissListener {
                openApplicationSettings()
            }
        }
        builder.show()
    }

    /**
     * Shows a snackbar with an action using the given string and identifier.
     */
    private fun makeSnack(snackMessage: Int, snackId: Int) {
        when (snackId) {
            SNACK_PERMISSIONS -> {
                snackbar = Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    snackMessage,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.snack_settings) {
                    openApplicationSettings()
                }
            }
            SNACK_CHECK_SETTINGS -> {
                snackbar = Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    snackMessage,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkSettingsAndStartGeoFence()
                }
            }
            else -> {
                pass
            }
        }
    }

    /**
     * Opens the settings of the application.
     */
    private fun openApplicationSettings() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts(
                getString(R.string.uri_package_scheme),
                BuildConfig.APPLICATION_ID,
                null
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (grantResults.isEmpty() || isLocationDenied(requestCode, grantResults)) {
            makeSnack(R.string.snack_permission_denied_explanation, SNACK_PERMISSIONS)
            snackbar?.show()
        } else if (isBackgroundLocationDenied(requestCode, grantResults)) {
            makeSnack(R.string.snack_background_access_explanation, SNACK_PERMISSIONS)
            snackbar?.show()
        }
    }

    /**
     * Returns a [Boolean] to indicate if location permission was denied using the given
     * request code and results array.
     */
    private fun isLocationDenied(requestCode: Int, grantResults: IntArray): Boolean {
        return requestCode == FOREGROUND_RESULT_CODE &&
                grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED
    }

    /**
     * Returns a [Boolean] to indicate if background location permission was denied using the given
     * request code and results array.
     */
    private fun isBackgroundLocationDenied(requestCode: Int, grantResults: IntArray): Boolean {
        return requestCode == FOREGROUND_AND_BACKGROUND_RESULT_CODE &&
                grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED
    }

    /**
     * Checks location settings and adds a [Geofence] for reminders.
     */
    private fun checkSettingsAndStartGeoFence(resolve: Boolean = true) {
        val locationSettingsResponseTask = getLocationSettingsResponseTask()
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Timber.d("Error getting location settings resolution$sendEx")
                }
            } else {
                makeSnack(android.R.string.ok, SNACK_CHECK_SETTINGS)
                snackbar?.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful && !isDetached) {
                addReminderGeofence()
            }
        }
    }

    /**
     * Returns a [LocationSettingsResponse] task.
     */
    private fun getLocationSettingsResponseTask(): Task<LocationSettingsResponse> {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        return settingsClient.checkLocationSettings(builder.build())
    }

    /**
     * Adds a [Geofence] for a [ReminderDataItem].
     */
    @SuppressLint("MissingPermission")
    private fun addReminderGeofence() {
        val geofence = getGeofence()
        val geofenceRequest = getGeofencingRequest(geofence)
        geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                viewModel.showToast.value = getString(R.string.toast_geofence_added)
                viewModel.validateAndSaveReminder(reminderDataItem)
            }
            addOnFailureListener {
                viewModel.showToast.value = getString(R.string.error_adding_geofence)
                if (it.message != null) {
                    Timber.w(it.message!!)
                }
            }
        }
    }

    /**
     * Returns a [Geofence] using the given [ReminderDataItem].
     */
    private fun getGeofence(): Geofence {
        return Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(
                reminderDataItem.latitude!!,
                reminderDataItem.longitude!!,
                GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
    }

    /**
     * Returns a [GeofencingRequest] object.
     */
    private fun getGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onClear()
        if (snackbar != null && snackbar?.isShown == true) {
            snackbar?.dismiss()
        }
    }

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "SaveReminderFragment.locationReminders.reminders.action.ACTION_GEOFENCE_EVENT"
        private const val GEOFENCE_RADIUS_IN_METERS = 100f
        private const val SNACK_PERMISSIONS = 10
        private const val SNACK_CHECK_SETTINGS = 11
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
        private const val FOREGROUND_AND_BACKGROUND_RESULT_CODE = 33
        private const val FOREGROUND_RESULT_CODE = 34
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
    }
}