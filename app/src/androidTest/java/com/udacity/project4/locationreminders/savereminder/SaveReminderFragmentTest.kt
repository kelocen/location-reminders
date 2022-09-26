package com.udacity.project4.locationreminders.savereminder

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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
import org.mockito.Mockito
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class SaveReminderFragmentTest : KoinTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminders: MutableList<ReminderDTO>
    private lateinit var dataSource: ReminderDataSource

    @Before
    fun setupSaveReminderFragmentTest() {
        stopKoin()
        val myModule = module {
            viewModel {
                SaveReminderViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
            single {
                val reminderDataSource = RemindersLocalRepository(get()) as ReminderDataSource
                reminderDataSource
            }
            single {
                LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext())
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
     * A [test][Test] function for [navigateToSelectLocation][SaveReminderFragment.setupSelectLocationButton].
     */
    @Test
    fun saveReminderScreen_navigateToSelectLocation_verifySelectLocationNavigation() = runBlocking {
        // Given
        val reminder = reminders[0]
        val mockNavController = Mockito.mock(NavController::class.java)
        val scenario =
            launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.Reminders_Theme)
        scenario.onFragment { saveReminderFragment ->
            Navigation.setViewNavController(saveReminderFragment.view!!, mockNavController)
        }
        onView(withId(R.id.reminderTitle))
            .perform(typeText(reminder.title))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.reminderDescription))
            .perform(typeText(reminder.description))
            .perform(closeSoftKeyboard())
        // When
        onView(withId(R.id.selectLocation)).perform(click())
        // Then
        verify(mockNavController).navigate(SaveReminderFragmentDirections.toSelectLocation())
    }

    /**
     * A [test][Test] function for [saveReminder][SaveReminderFragment.setupSaveReminderButton].
     */
    @Test
    fun saveReminderScreen_saveReminder_displayTitleSnackBarMessage(): Unit = runBlocking {
        // Given
        val mockNavController = Mockito.mock(NavController::class.java)
        val scenario =
            launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.Reminders_Theme)
        scenario.onFragment { saveReminderFragment ->
            Navigation.setViewNavController(saveReminderFragment.view!!, mockNavController)
        }
        // When
        onView(withId(R.id.saveReminder)).perform(click())
        // Then
        onView(withText(R.string.err_enter_title)).check(matches(isDisplayed()))
    }

    /**
     * A [test][Test] function for [saveReminder][SaveReminderFragment.setupSaveReminderButton].
     */
    @Test
    fun saveReminderScreen_saveReminder_displayLocationSnackBarMessage(): Unit = runBlocking {
        // Given
        val mockNavController = Mockito.mock(NavController::class.java)
        val scenario =
            launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.Reminders_Theme)
        scenario.onFragment { saveReminderFragment ->
            Navigation.setViewNavController(saveReminderFragment.view!!, mockNavController)
        }
        val reminder = reminders[0]
        onView(withId(R.id.reminderTitle))
            .perform(typeText(reminder.title))
            .perform(closeSoftKeyboard())
        // When
        onView(withId(R.id.saveReminder)).perform(click())
        // Then
        onView(withText(R.string.err_select_location)).check(matches(isDisplayed()))
    }
}