package dev.kelocen.reminders.locationreminders

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import dev.kelocen.reminders.R
import dev.kelocen.reminders.authentication.AuthenticationActivity
import dev.kelocen.reminders.authentication.AuthenticationViewModel
import dev.kelocen.reminders.databinding.ActivityRemindersBinding
import dev.kelocen.reminders.locationreminders.reminderslist.RemindersListFragment

/**
 * The RemindersActivity that holds the reminders fragments.
 */
class RemindersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemindersBinding
    private val authViewModel: AuthenticationViewModel by viewModels()
    private val pass: Unit = Unit // Placeholder for empty blocks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)
        observeAuthenticationState()
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration: AppBarConfiguration =
            AppBarConfiguration.Builder(R.id.reminderListFragment).build()
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /**
     * Configures an observer for [authenticationState][AuthenticationViewModel.authenticationState].
     */
    private fun observeAuthenticationState() {
        authViewModel.authenticationState.observe(this) { authenticationState ->
            when (authenticationState) {
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                    pass
                }
                AuthenticationViewModel.AuthenticationState.UNAUTHENTICATED -> {
                    startActivity(Intent(this, AuthenticationActivity::class.java))
                    finish()
                }
                AuthenticationViewModel.AuthenticationState.INVALID_AUTHENTICATION -> {
                    startActivity(Intent(this, AuthenticationActivity::class.java))
                    finish()
                }
                else -> {
                    pass
                }
            }
        }
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
