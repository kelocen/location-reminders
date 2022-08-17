@file:Suppress("unused", "KotlinDeprecation")

package dev.kelocen.reminders

import android.app.Application
import dev.kelocen.reminders.locationreminders.data.ReminderDataSource
import dev.kelocen.reminders.locationreminders.data.local.LocalDB
import dev.kelocen.reminders.locationreminders.data.local.RemindersLocalRepository
import dev.kelocen.reminders.locationreminders.reminderslist.RemindersListViewModel
import dev.kelocen.reminders.locationreminders.savereminder.SaveReminderViewModel
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
}