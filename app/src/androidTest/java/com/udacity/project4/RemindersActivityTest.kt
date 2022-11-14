package com.udacity.project4

import android.app.Activity
import android.app.Application
import android.graphics.Point
import android.os.Build
import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiSelector
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.data.local.asReminderDTO
import com.udacity.project4.locationreminders.data.local.fakeReminderData
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private lateinit var reminders: MutableList<ReminderDTO>
    private lateinit var device: UiDevice
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin() //stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single<ReminderDataSource> { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
        // initialize fake data list
        reminders = fakeReminderData.asReminderDTO()
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
        device = UiDevice.getInstance(getInstrumentation())
    }

    /**
     * An end-to-end test to save a [Reminder][ReminderDataItem].
     */
    @Test
    fun remindersScreen_saveReminder_confirmSavedReminders(): Unit = runBlocking {
        // Given
        val width = getInstrumentation().context.resources.displayMetrics.widthPixels
        val height = getInstrumentation().context.resources.displayMetrics.heightPixels
        val droppedPin = appContext.getString(R.string.marker_title_dropped_pin)
        val takePicture = appContext.getString(R.string.take_a_picture)
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        // When: No reminders are present
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        withContext(Dispatchers.IO) {
            Thread.sleep(4000)
        }
        // Then: Click the 'Add Reminder' button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // When: The 'Save Reminder' button is visible.
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))
        withContext(Dispatchers.IO) {
            Thread.sleep(4000)
        }
        // Then: Click the 'Select Location' button.
        onView(withId(R.id.selectLocation)).perform(click())

        // When: The map is visible on the screen.
        onView(withId(R.id.map)).check(matches(isDisplayed()))
        // Then: Test 'Select Location' toast message.
        reminderScreen_saveReminder_confirmToastMessage(R.string.toast_select_location)
        withContext(Dispatchers.IO) {
            Thread.sleep(4000)
        }
        // Then: Get the center of the screen.
        val position = getCenterPoint(device)
        // Then: Drop a map marker on the center of the screen.
        if (position != null) {
            device.swipe(position.x, position.y, position.x, position.y, 100)
        }
        // Then: Click the map marker.
        device.findObject(UiSelector().descriptionContains(droppedPin)).click()
        // Then: Test the 'Select Window' toast message.
        reminderScreen_saveReminder_confirmToastMessage(R.string.toast_select_window)
        // Then: Click the map marker's info window.
        if (Build.FINGERPRINT.contains("generic")) {          // Click position for emulators
            device.click(width / 2, (height * 0.43).toInt())
            /* Attribution
             * Simulate a click on a marker info window.
             * Author: Mr-IDE
             * Location: https://stackoverflow.com/questions/42505274/android-testing-google-map-info-window-click
             */
        } else {                                                    // Click position for devices
            device.click(width / 2, (height * 0.49).toInt())
        }
        withContext(Dispatchers.IO) {
            Thread.sleep(3700)
        }
        // Then: Click the 'Add' button in the map marker info window.
        onView(withId(android.R.id.button1)).perform(click())
        withContext(Dispatchers.IO) {
            Thread.sleep(3700)
        }

        // When: The 'Save Reminder' button is visible
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))
        // Then: Enter the reminder title.
        onView(withId(R.id.reminderTitle)).perform(typeText(droppedPin))
        withContext(Dispatchers.IO) {
            Thread.sleep(2700)
        }
        closeSoftKeyboard()
        // Then: Enter the reminder description.
        onView(withId(R.id.reminderDescription)).perform(typeText(takePicture))
        withContext(Dispatchers.IO) {
            Thread.sleep(4000)
        }
        closeSoftKeyboard()
        // Then: Click the 'Save Reminder' button.
        onView(withId(R.id.saveReminder)).perform(click())
        // Then: Test the 'Geofence Added' toast message.
        reminderScreen_saveReminder_confirmToastMessage(R.string.toast_geofence_added)
        withContext(Dispatchers.IO) {
            Thread.sleep(4000)
        }
        // Then: Test the 'Reminder Saved' toast message.
        reminderScreen_saveReminder_confirmToastMessage(R.string.toast_reminder_saved)
        // Then: Confirm the reminder list is no longer empty.
        onView(withId(R.id.noDataTextView)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        withContext(Dispatchers.IO) {
            Thread.sleep(4000)
        }

        // Use the map to wake location services and trigger the notification.
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))
        withContext(Dispatchers.IO) {
            Thread.sleep(4000)
        }
        onView(withId(R.id.selectLocation)).perform(click())
        withContext(Dispatchers.IO) {
            Thread.sleep(6000)
        }
    }

    /**
     * A [test][Test] function to confirm toast messages are visible.
     */
    private fun reminderScreen_saveReminder_confirmToastMessage(message: Int) {
        /* Attribution
        * Content: Testing toast message with Espresso
        * Author: kowalcj0
        * Date: 11/13/2022
        * Location: https://stackoverflow.com/questions/28390574/checking-toast-message-in-android-espresso/28606603#28606603
         */
        onView(withText(message)).inRoot(
            withDecorView(
                not(
                    `is`(
                        getActivity(appContext)?.window?.decorView
                    )
                )
            )
        ).check(matches(isDisplayed()))
    }

    /**
     * A helper method to simulate a long click to add a map marker.
     */
    private fun getCenterPoint(device: UiDevice): Point? {
        val uiMapObject: UiObject2 =
            device.findObject(By.descContains(appContext.getString(R.string.google_map)))
        return uiMapObject.visibleCenter
        /* Attribution
         * Simulate a long click on a map using UiAutomator 2
         * Author: Huang Jianon
         * Location: https://stackoverflow.com/questions/21432561/how-to-achieve-long-click-in-uiautomator
         */
    }

    @After
    fun resetResources() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }
}

fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription()
        : String {
    var description = ""
    onActivity {
        description =
            it.findViewById<Toolbar>(R.id.toolbar).navigationContentDescription as String
    }
    return description
}
