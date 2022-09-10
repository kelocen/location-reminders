package dev.kelocen.reminders

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
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiSelector
import dev.kelocen.reminders.locationreminders.RemindersActivity
import dev.kelocen.reminders.locationreminders.data.ReminderDataSource
import dev.kelocen.reminders.locationreminders.data.dto.ReminderDTO
import dev.kelocen.reminders.locationreminders.data.local.LocalDB
import dev.kelocen.reminders.locationreminders.data.local.RemindersLocalRepository
import dev.kelocen.reminders.locationreminders.data.local.asReminderDTO
import dev.kelocen.reminders.locationreminders.data.local.fakeReminderData
import dev.kelocen.reminders.locationreminders.reminderslist.ReminderDataItem
import dev.kelocen.reminders.locationreminders.reminderslist.RemindersListViewModel
import dev.kelocen.reminders.locationreminders.savereminder.SaveReminderViewModel
import dev.kelocen.reminders.util.DataBindingIdlingResource
import dev.kelocen.reminders.util.monitorActivity
import kotlinx.coroutines.runBlocking
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
        // When
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        Thread.sleep(4000)
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))
        Thread.sleep(4000)
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).check(matches(isDisplayed()))
        Thread.sleep(4000)
        // Then
        val position = getCenterPoint(device)
        if (position != null) {
            device.swipe(position.x, position.y, position.x, position.y, 100)
        }
        device.findObject(UiSelector().descriptionContains(droppedPin)).click()
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
        Thread.sleep(3700)
        onView(withId(android.R.id.button1)).perform(click())
        Thread.sleep(3700)
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderTitle)).perform(typeText(droppedPin))
        Thread.sleep(2700)
        onView(withId(R.id.reminderDescription)).perform(typeText(takePicture))
        Thread.sleep(4000)
        closeSoftKeyboard()
        onView(withId(R.id.saveReminder)).perform(click())
        Thread.sleep(4000)
        onView(withId(R.id.noDataTextView)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        Thread.sleep(4000)
        // Use the map to wake location services and trigger the notification.
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))
        Thread.sleep(4000)
        onView(withId(R.id.selectLocation)).perform(click())
        Thread.sleep(6000)
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
