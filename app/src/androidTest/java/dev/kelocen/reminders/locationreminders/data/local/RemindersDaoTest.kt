package dev.kelocen.reminders.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import dev.kelocen.reminders.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.core.IsNull.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Config(manifest = "src/main/AndroidManifest.xml")
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var reminderDao: RemindersDao
    private lateinit var reminders: MutableList<ReminderDTO>
    private val nothing: Unit = Unit

    @Before
    fun setupRemindersDaoTest() {
        database =
            Room.inMemoryDatabaseBuilder(
                getApplicationContext(),
                RemindersDatabase::class.java
            ).allowMainThreadQueries().build()
        reminderDao = database.reminderDao()
        reminders = fakeReminderData.asReminderDTO()
    }

    /**
     * A [test][Test] function for [getSize][RemindersDao.getCount].
     */
    @Test
    fun nothing_getCount_confirmCountIsAccurate() = runBlockingTest {
        // Given
        nothing
        // When
        val remindersCount = reminderDao.getCount()
        // Then
        assertThat(remindersCount, `is`(0))
    }

    /**
     * A [test][Test] function for [saveReminder][RemindersDao.saveReminder].
     */
    @Test
    fun reminder_saveReminder_confirmCountIncrease() = runBlockingTest {
        // Given
        val reminder = reminders[0]
        val oldCount = reminderDao.getCount()
        // When
        reminderDao.saveReminder(reminder)
        // Then
        val newCount = reminderDao.getCount()
        assertThat(oldCount, `is`(0))
        assertThat(newCount, `is`(1))
    }

    /**
     * A [test][Test] function for [deleteReminder][RemindersDao.deleteReminder].
     */
    @Test
    fun reminder_deleteReminder_confirmCountDecrease() = runBlockingTest {
        // Given
        val reminder = reminders[0]
        reminderDao.saveReminder(reminder)
        val oldCount = reminderDao.getCount()
        // When
        reminderDao.deleteReminder(reminder.id)
        // Then
        val newCount = reminderDao.getCount()
        assertThat(oldCount, `is`(1))
        assertThat(newCount, `is`(0))
    }

    /**
     * A [test][Test] function for [deleteAllReminders][RemindersDao.deleteAllReminders].
     */
    @Test
    fun reminders_deleteAlReminders_confirmCountZero() = runBlockingTest {
        // Given
        for (reminder in reminders) {
            reminderDao.saveReminder(reminder)
        }
        val oldCount = reminderDao.getCount()
        // When
        reminderDao.deleteAllReminders()
        // Then
        val newCount = reminderDao.getCount()
        assertThat(oldCount, `is`(7))
        assertThat(newCount, `is`(0))
    }

    /**
     * A [test][Test] function for [getReminderById][RemindersDao.getReminderById].
     */
    @Test
    fun reminder_getReminderById_validateReminder() = runBlockingTest {
        // Given
        val savedReminder = reminders[0]
        reminderDao.saveReminder(savedReminder)
        // When
        val retrievedReminder = reminderDao.getReminderById(savedReminder.id)
        // Then
        assertThat(retrievedReminder as ReminderDTO, notNullValue())
        assertThat(retrievedReminder.id, `is`(savedReminder.id))
        assertThat(retrievedReminder.title, `is`(savedReminder.title))
        assertThat(retrievedReminder.description, `is`(savedReminder.description))
        assertThat(retrievedReminder.location, `is`(savedReminder.location))
        assertThat(retrievedReminder.latitude, `is`(savedReminder.latitude))
        assertThat(retrievedReminder.longitude, `is`(savedReminder.longitude))
    }

    /**
     * A [test][Test] function for [getReminders][RemindersDao.getReminders].
     */
    @Test
    fun reminders_getReminders_validateReminders() = runBlockingTest {
        // Given
        for (reminder in reminders) {
            reminderDao.saveReminder(reminder)
        }
        // When
        val retrievedReminders = reminderDao.getReminders()
        // Then
        val listValidated = retrievedReminders.containsAll(reminders)
        assertThat(listValidated, `is`(true))
    }

    /**
     * A [test][Test] function for [saveReminder][RemindersDao.saveReminder].
     */
    @Test
    fun reminder_saveReminder_confirmReminderUpdated() = runBlockingTest {
        // Given
        for (reminder in reminders) {
            reminderDao.saveReminder(reminder)
        }
        val reminderToEdit = reminderDao.getReminders()[1]
        val newDescription = "Postcard from Golden Gate Bridge."
        // When
        reminderToEdit.description = newDescription
        reminderDao.saveReminder(reminderToEdit)
        // Then
        val updatedReminder = reminderDao.getReminderById(reminderToEdit.id)
        assertThat(updatedReminder?.description, `is`(newDescription))
    }

    @After
    fun resetRemindersDaoTest() {
        database.close()
    }
}