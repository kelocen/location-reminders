package com.udacity.project4.authentication

import android.app.Application
import androidx.lifecycle.map
import com.udacity.project4.base.BaseViewModel

/**
 * A subclass of [BaseViewModel] for user authentication.
 */
class AuthenticationViewModel(val app: Application) : BaseViewModel(app) {

    /**
     * Uses [FirebaseUserLiveData] to check if the user is signed in and [AuthenticationState] to
     * assign authenticated and unauthenticated states.
     */
    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    /**
     * An [Enum] for the state of authentication.
     */
    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }
}