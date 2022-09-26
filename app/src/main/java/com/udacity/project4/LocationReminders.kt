@file:Suppress("unused", "KotlinDeprecation")

package com.udacity.project4

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

class LocationReminders : Application() {

    /**
     * use Koin Library as a service locator
     */
    private val myModule = module {
        viewModel {
            RemindersListViewModel(get(), get() as ReminderDataSource)
        }
        single {
            SaveReminderViewModel(get(), get() as ReminderDataSource)
        }
        single {
            val reminderDataSource = RemindersLocalRepository(get()) as ReminderDataSource
            reminderDataSource
        }
        single {
            LocalDB.createRemindersDao(this@LocationReminders)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LocationReminders)
            modules(listOf(myModule))
        }
        Timber.plant(Timber.DebugTree())
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        try {
            MultiDex.install(this)
        } catch (multiDexException: RuntimeException) {
            try {
                Class.forName("org.robolectric.Robolectric")
            } catch (ex: ClassNotFoundException) {
                throw multiDexException
            }
        }
        /* Attribution
         * Content: Workaround for multidex installation failure with Robolectric tests.
         * Author: DimaSkopiuk
         * Date: 10/9/2018
         * Location: https://github.com/robolectric/robolectric/issues/3946
         */
    }
}