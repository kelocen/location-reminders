package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    override fun onHandleWork(intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            Timber.e(getErrorMessage(this, geofencingEvent.errorCode))
            return
        }
        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            if (geofencingEvent.triggeringGeofences.isNullOrEmpty()) {
                Timber.e(getErrorMessage(this, TRIGGERING_GEOFENCES_NULL_OR_EMPTY))
            } else {
                val triggeringGeofences: MutableList<Geofence> = geofencingEvent.triggeringGeofences
                sendNotification(triggeringGeofences)
            }
        }
    }

    /**
     * Returns an error [String] using the given [Context] and [Int].
     */
    private fun getErrorMessage(context: Context, errorCode: Int): String {
        val resources = context.resources
        return when (errorCode) {
            TRIGGERING_GEOFENCES_NULL_OR_EMPTY ->
                resources.getString(R.string.geofences_null_or_empty)
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE ->
                resources.getString(R.string.geofence_not_available)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES ->
                resources.getString(R.string.geofence_too_many_geofences)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS ->
                resources.getString(R.string.geofence_too_many_pending_intents)
            else -> resources.getString(R.string.geofence_unknown_error)
        }
    }

    /**
     * Retrieves a [ReminderDataItem] using the given [List] of [geofences][Geofence] and sends
     * a location-based notification.
     */
    private fun sendNotification(geofences: MutableList<Geofence>) {
        val remindersLocalRepository: ReminderDataSource by inject()
        for (geofence in geofences) {
            val requestId = geofence.requestId
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                val result = remindersLocalRepository.getReminder(requestId)
                if (result is Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    com.udacity.project4.utils.sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                        )
                    )
                }
            }
        }
    }

    companion object {
        private const val TRIGGERING_GEOFENCES_NULL_OR_EMPTY = 0
        private const val JOB_ID = 573
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }
}