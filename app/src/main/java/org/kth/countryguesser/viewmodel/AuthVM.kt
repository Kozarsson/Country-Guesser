package org.kth.countryguesser.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kth.countryguesser.Application
import org.kth.countryguesser.data.repository.FirebaseAuthRepository
import org.kth.countryguesser.data.repository.RegistrationResult
import org.kth.countryguesser.model.entity.UserEntity
import org.kth.countryguesser.util.NetworkUtils
import org.kth.countryguesser.util.PopupState
import javax.inject.Inject


interface AuthVM {
    val userEntity: StateFlow<UserEntity?>
    fun signInWithEmailPassword(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    )

    fun registerWithEmailPassword(
        nickname: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    )

    fun changePassword(
        oldPassword: String,
        newPassword: String,
        onResult: (Boolean, String?) -> Unit
    )

    fun authenticated(): Boolean
    fun signOut()
    fun signInAnonymously()
}

@HiltViewModel
class AuthVMImpl @Inject constructor(
    private val authRepository: FirebaseAuthRepository
) : BaseVM(), AuthVM {

    companion object {
        const val TAG: String = "[Authentication]"

        fun Factory(authRepository: FirebaseAuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AuthVMImpl::class.java)) {
                        return AuthVMImpl(authRepository) as T
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
                    setPopupState(PopupState.NO_INTERNET)
                }
                if (authRepository.login(email, password)) {
                    Log.d(TAG, "Signed in successfully!")
                    _userEntity.value = authRepository.getCurrentUser()
                    onResult(true, null)
                } else {
                    Log.d(TAG, "Failed to sign in with email and password!")
                    onResult(false, "Incorrect email or password.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Authentication error: ${e.localizedMessage}", e)
                onResult(false, e.localizedMessage ?: "An unknown error occurred.")
            }
        }
    }

    override fun registerWithEmailPassword(
        nickname: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (!NetworkUtils.isNetworkAvailable(Application.APPLICATION.applicationContext)) {
                    setPopupState(PopupState.NO_INTERNET)
                }
                when (val result = authRepository.register(nickname, email, password)) {
                    RegistrationResult.Success -> {
                        Log.d(TAG, "Registered successfully!")
                        _userEntity.value = authRepository.getCurrentUser()
                        onResult(true, null)
                    }

                    RegistrationResult.EmailTaken -> {
                        onResult(false, "This email is already in use.")
                    }

                    RegistrationResult.NicknameTaken -> {
                        onResult(false, "This nickname is already taken.")
                    }

                    is RegistrationResult.Error -> {
                        onResult(false, result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Authentication error: ${e.localizedMessage}", e)
                onResult(false, e.localizedMessage ?: "An unknown error occurred.")
            }
        }
    }

    override fun changePassword(
        oldPassword: String,
        newPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (!NetworkUtils.isNetworkAvailable(Application.APPLICATION.applicationContext)) {
                    setPopupState(PopupState.NO_INTERNET)
                }
                if (authRepository.updatePassword(oldPassword, newPassword)) {
                    Log.d(TAG, "Password updated successfully!")
                    onResult(true, null)
                } else {
                    Log.d(TAG, "Failed to update password!")
                    onResult(false, "Incorrect current password.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Password update error: ${e.localizedMessage}", e)
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