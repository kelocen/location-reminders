@file:Suppress("unused", "KotlinDeprecation")

package dev.kelocen.reminders

import android.app.Application
import timber.log.Timber

class LocationReminders : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}