package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.data.local.asReminderDTO
import com.udacity.project4.locationreminders.data.local.fakeReminderData
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class RemindersListFragmentTest : KoinTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminders: MutableList<ReminderDTO>
    private lateinit var dataSource: ReminderDataSource
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private val nothing: Unit = Unit


    @Before
    fun setupReminderListFragmentTest() {
        stopKoin()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
            single {
                val reminderDataSource = RemindersLocalRepository(get()) as ReminderDataSource
                reminderDataSource
            }
            single {
                LocalDB.createRemindersDao(getApplicationContext())
            }
        }
        startKoin {
            modules(listOf(myModule))
        }
        dataSource = get()
        runBlocking {
            dataSource.deleteAllReminders()
        }
        reminders = fakeReminderData.asReminderDTO()
    }

    /**
     * A [test][Test] function for [navigateToSaveReminder][RemindersListFragment.navigateToSaveReminder].
     */
    @Test
    fun remindersScreen_navigateToSaveReminder_verifySaveReminderNavigation() = runBlocking {
        // Given
        val mockNavController = mock(NavController::class.java)
        val scenario =
            launchFragmentInContainer<RemindersListFragment>(Bundle(), R.style.Reminders_Theme)
        scenario.onFragment { reminderListFragment ->
            Navigation.setViewNavController(reminderListFragment.view!!, mockNavController)
        }
        dataBindingIdlingResource.monitorFragment(scenario)
        // When
        onView(withId(R.id.addReminderFAB)).perform(click())
        // Then
        verify(mockNavController).navigate(RemindersListFragmentDirections.toSaveReminder())
    }

    /**
     * A [test][Test] function to display the [noDataTextView][R.id.noDataTextView].
     */
    @Test
    fun nothing_launchRemindersListFragment_displayNoDataTextView(): Unit = runBlocking {
        // Given
        nothing
        // When
        launchFragmentInContainer<RemindersListFragment>(Bundle(), R.style.Reminders_Theme)
        // Then
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    /**
     * A [test][Test] function for [setupRecyclerView][RemindersListFragment.setupRecyclerView] to display [Reminder][ReminderDTO] data.
     */
    @Test
    fun remindersList_setupRecyclerView_displayReminderData(): Unit = runBlocking {
        // Given
        for (reminder in reminders) {
            dataSource.saveReminder(reminder)
        }
        // When
        val mockNavController = mock(NavController::class.java)
        val scenario =
            launchFragmentInContainer<RemindersListFragment>(Bundle(), R.style.Reminders_Theme)
        scenario.onFragment { reminderListFragment ->
            Navigation.setViewNavController(reminderListFragment.view!!, mockNavController)
        }
        dataBindingIdlingResource.monitorFragment(scenario)
        // Then
        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))
        for (reminder in reminders) {
            onView(withText(reminder.title)).check(matches(withText(reminder.title)))
            onView(withText(reminder.description)).check(matches(withText(reminder.description)))
            onView(withText(reminder.location)).check(matches(withText(reminder.location)))
        }
    }
}