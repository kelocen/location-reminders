package dev.kelocen.reminders.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dev.kelocen.reminders.R
import dev.kelocen.reminders.databinding.ActivityReminderDescriptionBinding
import dev.kelocen.reminders.locationreminders.reminderslist.ReminderDataItem
import dev.kelocen.reminders.locationreminders.reminderslist.RemindersListViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReminderDescriptionBinding
    private val listViewModel: RemindersListViewModel by viewModel()
    private lateinit var reminderDataItem: ReminderDataItem
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_description)
        reminderDataItem =
            intent?.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem
        binding.reminderDataItem = reminderDataItem
        setupReminderDeleteButton()
    }

    /**
     * Configures the delete button for the reminder description screen.
     */
    private fun setupReminderDeleteButton() {
        binding.buttonDelete.setOnClickListener {
            if (binding.reminderDataItem != null) {
                listViewModel.deleteReminder(reminderDataItem.id)
                finish()
            }
        }
    }

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }
}
