package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var reminders: MutableList<ReminderDTO>
    private lateinit var repository: RemindersLocalRepository
    private val nothing: Unit = Unit

    @Before
    fun setupRemindersLocalRepositoryTest() {
        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
            ).allowMainThreadQueries().build()
        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
        reminders = fakeReminderData.asReminderDTO()
    }

    /**
     * A [test][Test] function for [getSize][RemindersLocalRepository.getCount].
     */
    @Test
    fun nothing_getCount_confirmCountIsAccurate() = runBlocking {
        // Given
        nothing
        // When
        val remindersCount = repository.getCount()
        // Then
        assertThat(remindersCount, `is`(0))
    }

    /**
     * A [test][Test] function for [saveReminder][RemindersLocalRepository.saveReminder].
     */
    @Test
    fun reminder_saveReminder_confirmCountIncrease() = runBlocking {
        // Given
        val reminder = reminders[0]
        val oldCount = repository.getCount()
        // When
        repository.saveReminder(reminder)
        // Then
        val newCount = repository.getCount()
        assertThat(oldCount, `is`(0))
        assertThat(newCount, `is`(1))
    }

    /**
     * A [test][Test] function for [deleteReminder][RemindersLocalRepository.deleteReminder].
     */
    @Test
    fun reminder_deleteReminder_confirmCountDecrease() = runBlocking {
        // Given
        val reminder = reminders[0]
        repository.saveReminder(reminder)
        val oldCount = repository.getCount()
        // When
        repository.deleteReminder(reminder.id)
        // Then
        val newCount = repository.getCount()
        assertThat(oldCount, `is`(1))
        assertThat(newCount, `is`(0))
    }

    /**
     * A [test][Test] function for [deleteAllReminders][RemindersLocalRepository.deleteAllReminders].
     */
    @Test
    fun reminders_deleteAlReminders_confirmCountZero() = runBlocking {
        // Given
        for (reminder in reminders) {
            repository.saveReminder(reminder)
        }
        val oldCount = repository.getCount()
        // When
        repository.deleteAllReminders()
        // Then
        val newCount = repository.getCount()
        assertThat(oldCount, `is`(7))
        assertThat(newCount, `is`(0))
    }

    /**
     * A [test][Test] function for [getReminderById][RemindersLocalRepository.getReminder].
     */
    @Test
    fun reminder_getReminder_validateReminder() = runBlocking {
        // Given
        val savedReminder = reminders[0]
        repository.saveReminder(savedReminder)
        // When
        val retrievedResult = repository.getReminder(savedReminder.id)
        // Then
        assertThat(retrievedResult.succeeded, `is`(true))
        val retrievedReminder = (retrievedResult as Result.Success<ReminderDTO>).data
        assertThat(retrievedReminder.id, `is`(savedReminder.id))
        assertThat(retrievedReminder.title, `is`(savedReminder.title))
        assertThat(retrievedReminder.description, `is`(savedReminder.description))
        assertThat(retrievedReminder.location, `is`(savedReminder.location))
        assertThat(retrievedReminder.latitude, `is`(savedReminder.latitude))
        assertThat(retrievedReminder.longitude, `is`(savedReminder.longitude))
    }

    /**
     * A [test][Test] function for [getReminderById][RemindersLocalRepository.getReminder].
     */
    @Test
    fun nothing_getReminder_returnError() = runBlocking {
        // Given
        nothing
        // When
        val retrievedResult = repository.getReminder(reminders[0].id)
        // Then
        assertThat(retrievedResult.error, `is`(true))
        val errorMessage = (retrievedResult as Result.Error).message
        assertThat(errorMessage, `is`("Reminder not found!"))
    }

    /**
     * A [test][Test] function for [getReminders][RemindersLocalRepository.getReminders].
     */
    @Test
    fun reminders_getReminders_validateReminders() = runBlocking {
        // Given
        for (reminder in reminders) {
            repository.saveReminder(reminder)
        }
        // When
        val retrievedResult = repository.getReminders()
        // Then
        assertThat(retrievedResult.succeeded, `is`(true))
        val retrievedReminders = (retrievedResult as Result.Success<List<ReminderDTO>>).data
        val listValidated = retrievedReminders.containsAll(reminders)
        assertThat(listValidated, `is`(true))
    }

    /**
     * A [test][Test] function for [getReminders][RemindersLocalRepository.getReminders].
     */
    @Test
    fun reminders_getReminders_confirmEmptyList() = runBlocking {
        // Given
        nothing
        // When
        val retrievedResult = repository.getReminders()
        // Then
        assertThat(retrievedResult.succeeded, `is`(true))
        val retrievedReminders = (retrievedResult as Result.Success<List<ReminderDTO>>).data
        val listEmpty = retrievedReminders.isEmpty()
        assertThat(listEmpty, `is`(true))
    }

    @After
    fun resetRemindersLocalRepositoryTest() {
        database.close()
    }
}