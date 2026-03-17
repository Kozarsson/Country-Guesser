package org.kth.countryguesser.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kth.countryguesser.Application
import org.kth.countryguesser.model.entity.UserEntity
import org.kth.countryguesser.model.repository.FirebaseAuthRepository
import org.kth.countryguesser.model.service.MyFirebaseMessagingService
import org.kth.countryguesser.util.NetworkUtils


interface IAuthViewModel {
    val userEntity: StateFlow<UserEntity?>
    fun signInWithEmailPassword(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    )

    fun registerWithEmailPassword(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    )

    fun authenticated(): Boolean
    fun signOut()
    fun signInAnonymously()
}

class AuthViewModel(
    private val authRepository: FirebaseAuthRepository
) : ViewModel(), IAuthViewModel {

    companion object {
        const val TAG: String = "[Authentication]"

        fun Factory(authRepository: FirebaseAuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                        return AuthViewModel(authRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

    private val _userEntity = MutableStateFlow<UserEntity?>(null)
    override val userEntity: StateFlow<UserEntity?> get() = _userEntity

    init {
        _userEntity.value = authRepository.getCurrentUser()
        if (_userEntity.value != null) {
            Log.d(TAG, "User signed in! UserId: " + _userEntity.value?.uid)
        } else {
            signInAnonymously()
        }
    }

    override fun authenticated(): Boolean {
        return _userEntity.value != null && _userEntity.value?.isAnonymous == false
    }

    override fun signInWithEmailPassword(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (!NetworkUtils.isNetworkAvailable(Application.APPLICATION.applicationContext)) {
                    throw Exception("No internet connection!")
                }
                if (authRepository.login(email, password)) {
                    Log.d(TAG, "Signed in successfully!")
                    _userEntity.value = authRepository.getCurrentUser()
                    onResult(true, null)
                } else {
                    Log.d(TAG, "Failed to sign in with email and password!")
                    onResult(false, "Unknown error occurred.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Authentication error: ${e.localizedMessage}", e)
                onResult(false, e.localizedMessage ?: "An unknown error occurred.")
            }
        }
    }

    override fun registerWithEmailPassword(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (!NetworkUtils.isNetworkAvailable(Application.APPLICATION.applicationContext)) {
                    throw Exception("No internet connection!")
                }
                if (authRepository.register(email, password)) {
                    Log.d(TAG, "Registered successfully!")
                    MyFirebaseMessagingService.enableTokenAutoInit()
                    _userEntity.value = authRepository.getCurrentUser()
                    onResult(true, null)
                } else {
                    Log.d(TAG, "Failed to register with email and password!!")
                    onResult(false, "Registration failed!")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Authentication error: ${e.localizedMessage}", e)
                onResult(false, e.localizedMessage ?: "An unknown error occurred.")
            }
        }
    }

    override fun signOut() {
        authRepository.signOut()
        _userEntity.value = null
        Log.d(TAG, "User signed out!")
    }

    override fun signInAnonymously() {
        viewModelScope.launch {
            if (authRepository.signInAnonymously()) {
                Log.d(TAG, "Signed in anonymously")
                _userEntity.value = authRepository.getCurrentUser()
            } else {
                Log.d(TAG, "Failed to sign in anonymously")
            }
        }
    }
}