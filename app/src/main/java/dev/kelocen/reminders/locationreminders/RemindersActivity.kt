package dev.kelocen.reminders.locationreminders

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import dev.kelocen.reminders.R

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
    }

    /**
     * [Override] of [onSupportNavigateUp] to [RemindersListFragment] to avoid duplicating the
     * behavior of the back button.
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        if (navController.currentDestination?.id == R.id.saveReminderFragment) {
            navController.navigate(R.id.save_to_reminder_list)
        } else if (navController.currentDestination?.id == R.id.selectLocationFragment) {
            navController.navigate(R.id.action_SelectReminderFragment_to_RemindersListFragment)
        }
        return true
    }
}