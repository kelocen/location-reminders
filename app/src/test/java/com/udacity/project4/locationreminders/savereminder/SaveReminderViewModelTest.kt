package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.asReminderDataItem
import com.udacity.project4.locationreminders.data.fakeReminderData
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.util.MainCoroutineRule
import com.udacity.project4.locationreminders.util.getOrAwaitValue
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

@Config(manifest = "AndroidManifest.xml")
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class SaveReminderViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var dataSource: FakeDataSource
    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var reminders: MutableList<ReminderDataItem>
    private val nothing: Unit = Unit

    @Before
    fun setupViewModelTest() {
        stopKoin()
        dataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
        reminders = fakeReminderData.asReminderDataItem()
    }

    /**
     * A [test][Test] function for [validateEnteredData][SaveReminderViewModel.validateEnteredData] and [showSnackBarInt][SaveReminderViewModel.showSnackBarInt].
     */
    @Test
    fun nullReminderTitle_validateEnteredData_showSnackBarIntErrorTitle() = runBlockingTest {
        // Given
        val reminder = reminders[0]
        // When
        reminder.title = null
        // Then
        assertThat(viewModel.validateEnteredData(reminder), `is`(false))
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    /**
     * A [test][Test] function for [validateEnteredData][SaveReminderViewModel.validateEnteredData] and [showSnackBarInt][SaveReminderViewModel.showSnackBarInt].
     */
    @Test
    fun emptyReminderTitle_validateEnteredData_showSnackBarIntErrorTitle() = runBlockingTest {
        // Given
        val reminder = reminders[1]
        // When
        reminder.title = ""
        // Then
        assertThat(viewModel.validateEnteredData(reminder), `is`(false))
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    /**
     * A [test][Test] function for [validateEnteredData][SaveReminderViewModel.validateEnteredData] and [showSnackBarInt][SaveReminderViewModel.showSnackBarInt].
     */
    @Test
    fun nullReminderLocation_validateEnteredData_showSnackBarIntErrorLocation() = runBlockingTest {
        // Given
        val reminder = reminders[2]
        // When
        reminder.location = null
        // Then
        assertThat(viewModel.validateEnteredData(reminder), `is`(false))
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }

    /**
     * A [test][Test] function for [validateEnteredData][SaveReminderViewModel.validateEnteredData] and [showSnackBarInt][SaveReminderViewModel.showSnackBarInt].
     */
    @Test
    fun emptyReminderLocation_validateEnteredData_showSnackBarIntErrorLocation() = runBlockingTest {
        // Given
        val reminder = reminders[3]
        // When
        reminder.location = ""
        // Then
        assertThat(viewModel.validateEnteredData(reminder), `is`(false))
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }

    /**
     * A [test][Test] function for [saveReminder][SaveReminderViewModel.saveReminder] and [showLoading][SaveReminderViewModel.showLoading].
     */
    @Test
    fun reminder_saveReminder_showLoading() = mainCoroutineRule.runBlockingTest {
        // Given
        val reminder = reminders[4]
        pauseDispatcher()
        // When
        viewModel.saveReminder(reminder)
        // Then
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))
        resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    /**
     * A [test][Test] function for [saveReminder][SaveReminderViewModel.saveReminder] and [showToast][SaveReminderViewModel.showToast].
     */
    @Test
    fun reminder_saveReminder_showToast() = mainCoroutineRule.runBlockingTest {
        // Given
        val reminder = reminders[5]
        // When
        viewModel.saveReminder(reminder)
        // Then
        assertThat(viewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved!"))
    }

    /**
     * A [test][Test] function for [saveReminder][SaveReminderViewModel.saveReminder] and [navigationCommand][com.udacity.project4.base.NavigationCommand.Back].
     */
    @Test
    fun reminder_saveReminder_navigateCommandBack() = runBlockingTest {
        // Given
        val reminder = reminders[6]
        // When
        viewModel.saveReminder(reminder)
        // Then
        assertThat(viewModel.navigationCommand.getOrAwaitValue(), `is`(NavigationCommand.Back))
    }

    /**
     * A [test][Test] function for [saveReminder][SaveReminderViewModel.onClear].
     */
    @Test
    fun nothing_onClear_checkValuesCleared() = runBlockingTest {
        // Given
        nothing
        // When
        viewModel.onClear()
        // Then
        assert(viewModel.reminderTitle.getOrAwaitValue() == null)
        assert(viewModel.reminderDescription.getOrAwaitValue() == null)
        assert(viewModel.reminderSelectedLocationStr.getOrAwaitValue() == null)
        assert(viewModel.selectedPOI.getOrAwaitValue() == null)
        assert(viewModel.latitude.getOrAwaitValue() == null)
        assert(viewModel.longitude.getOrAwaitValue() == null)
    }
}