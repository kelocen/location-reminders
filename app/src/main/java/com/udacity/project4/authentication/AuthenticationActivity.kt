package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.view.View.OnClickListener
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationViewModel.AuthenticationState
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import timber.log.Timber

/**
 * A subclass of [AppCompatActivity] that redirects the user to the login or reminders screens based
 * on their authentication state.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    private val authViewModel: AuthenticationViewModel by viewModels()
    private val pass: Unit = Unit // Placeholder for empty block

    /**
     * An [ActivityResultLauncher] that registers a callback for the [FirebaseAuthUIActivityResultContract].
     */
    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
            this.onSignInResult(result)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)
        observeAuthenticationState()
        configureLoginButton()
    }

    /**
     * Configures an observer for the [AuthenticationState] of the user.
     */
    private fun observeAuthenticationState() {
        authViewModel.authenticationState.observe(this) { authenticationState ->
            when (authenticationState) {
                AuthenticationState.AUTHENTICATED -> {
                    startActivity(
                        Intent(
                            this,
                            RemindersActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                    finish()
                }
                AuthenticationState.UNAUTHENTICATED -> {
                    pass
                }
                AuthenticationState.INVALID_AUTHENTICATION -> {
                    launchSignInFlow()
                }
                else -> {
                    pass
                }
            }
        }
    }

    /**
     * Configures the [OnClickListener] for the login button.
     */
    private fun configureLoginButton() {
        binding.buttonLoginRegister.setOnClickListener {
            launchSignInFlow()
        }
    }

    /**
     * Launches the [AuthUI] to sign into the app using an email address or Google account.
     */
    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.reminders_logo)
            .setTheme(R.style.LoginStyle)
            .setLockOrientation(true)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            Timber.i("Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
        } else {
            Timber.i("Sign in unsuccessful ${response?.error?.errorCode}")
        }
    }
}
