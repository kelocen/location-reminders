package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListFragment
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.locationreminders.savereminder.selectlocation.SelectLocationFragment
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
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
@LargeTest
class RemindersActivityNavigationTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
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
    }

    /**
     * A test for the back button to navigate from the [Save Reminder Screen][SaveReminderFragment]
     * to the [Reminder List Screen][RemindersListFragment].
     */
    @Test
    fun saveReminderScreen_pressBack_confirmReminderListNavigation(): Unit = runBlocking {
        // Given
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        onView(withId(R.id.saveReminder))
            .check(matches(isDisplayed()))
        // When
        pressBack()
        // Then
        onView(withId(R.id.addReminderFAB))
            .check(matches(isDisplayed()))
        scenario.close()
    }

    /**
     * A test for the back button to navigate from the [Select Location Screen][SelectLocationFragment]
     * to the [Reminder List Screen][RemindersListFragment].
     */
    @Test
    fun saveReminderScreen_pressBackTwice_confirmReminderListNavigation(): Unit = runBlocking {
        // Given
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        onView(withId(R.id.saveReminder))
            .check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        onView(withId(R.id.map))
            .check(matches(isDisplayed()))
        // When
        pressBack()
        onView(withId(R.id.saveReminder))
            .check(matches(isDisplayed()))
        // Then
        pressBack()
        onView(withId(R.id.remindersRecyclerView))
            .check(matches(isDisplayed()))
        scenario.close()
    }

    /**
     * A test for the up arrow to navigate from the [Save Reminder Screen][SaveReminderFragment]
     * to the [Reminder List Screen][RemindersListFragment].
     */
    @Test
    fun saveReminderScreen_tapUpArrow_confirmReminderListNavigation(): Unit = runBlocking {
        // Given
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        onView(withId(R.id.saveReminder))
            .check(matches(isDisplayed()))
        // When
        onView(withContentDescription(scenario.getToolbarNavigationContentDescription()))
            .perform(
                ViewActions.click()
            )
        // Then
        onView(withId(R.id.addReminderFAB))
            .check(matches(isDisplayed()))
        scenario.close()
    }

    /**
     * A test for the up arrow to navigate from the [Select Location Screen][SelectLocationFragment]
     * to the [Reminder List Screen][RemindersListFragment].
     */
    @Test
    fun selectLocationScreen_tapUpArrow_confirmReminderListNavigation(): Unit = runBlocking {
        // Given
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        onView(withId(R.id.saveReminder))
            .check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        onView(withId(R.id.map))
            .check(matches(isDisplayed()))
        // When
        onView(withContentDescription(scenario.getToolbarNavigationContentDescription()))
            .perform(
                ViewActions.click()
            )
        // Then
        onView(withId(R.id.addReminderFAB))
            .check(matches(isDisplayed()))
        scenario.close()
    }
}