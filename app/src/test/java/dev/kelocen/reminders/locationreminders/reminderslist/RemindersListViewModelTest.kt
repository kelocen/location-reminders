package dev.kelocen.reminders.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import dev.kelocen.reminders.locationreminders.data.FakeDataSource
import dev.kelocen.reminders.locationreminders.data.asReminderDTO
import dev.kelocen.reminders.locationreminders.data.dto.ReminderDTO
import dev.kelocen.reminders.locationreminders.data.fakeReminderData
import dev.kelocen.reminders.locationreminders.util.MainCoroutineRule
import dev.kelocen.reminders.locationreminders.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(manifest = "src/main/AndroidManifest.xml")
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class RemindersListViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var dataSource: FakeDataSource
    private lateinit var viewModel: RemindersListViewModel
    private lateinit var reminders: MutableList<ReminderDTO>
    private val nothing: Unit = Unit // Placeholder for empty test parameters.

    @Before
    fun setupViewModelTest() {
        stopKoin()
        dataSource = FakeDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
        reminders = fakeReminderData.asReminderDTO()
    }

    /**
     * A [test][Test] function for [loadReminders][RemindersListViewModel.loadReminders] and [showLoading][RemindersListViewModel.showLoading].
     */
    @Test
    fun nothing_loadReminders_checkLoading() = mainCoroutineRule.runBlockingTest {
        // Given
        nothing
        pauseDispatcher()
        // When
        viewModel.loadReminders()
        // Then
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))
        resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    /**
     * A [test][Test] function for [loadReminders][RemindersListViewModel.loadReminders] and [remindersList][RemindersListViewModel.remindersList].
     */
    @Test
    fun remindersList_loadReminders_checkNotEmpty() = runBlockingTest {
        // Given
        for (reminder in reminders) {
            dataSource.saveReminder(reminder)
        }
        // When
        viewModel.loadReminders()
        // Then
        val hasOneOrMore = dataSource.getCount() >= 1
        assertThat(hasOneOrMore, `is`(true))
    }

    /**
     * A [test][Test] function for [loadReminders][RemindersListViewModel.loadReminders] and [showNoData][RemindersListViewModel.showNoData].
     */
    @Test
    fun emptyRemindersList_loadReminders_showNoData() = runBlockingTest {
        // Given
        dataSource.deleteAllReminders()
        // When
        viewModel.loadReminders()
        // Then
        assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    /**
     * A [test][Test] function for [deleteReminder][RemindersListViewModel.deleteReminder].
     */
    @Test
    fun remindersList_deleteReminder_checkEmpty() = runBlockingTest {
        // Given
        dataSource.saveReminder(reminders[0])
        viewModel.loadReminders()
        // When
        viewModel.deleteReminder(reminders[0].id)
        // Then
        viewModel.loadReminders()
        val hasNone = dataSource.getCount() == 0
        assertThat(hasNone, `is`(true))
    }

    /**
     * A [test][Test] function for [loadReminders][RemindersListViewModel.loadReminders] and [showSnackBar][RemindersListViewModel.showSnackBar].
     */
    @Test
    fun reminderError_loadReminders_showSnackBar() {
        // Given
        dataSource.setShouldReturnError(true)
        // When
        viewModel.loadReminders()
        // Then
        val reminderException = Exception("Reminder Exception!").toString()
        assertThat(viewModel.showSnackBar.getOrAwaitValue(), `is`(reminderException))
    }
}