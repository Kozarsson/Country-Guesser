package org.kth.countryguesser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kth.countryguesser.util.PopupState

/**
 * The superclass viewmodel that all other viewmodels inherit.
 */
abstract class BaseVM : ViewModel() {

    private val _popupState = MutableStateFlow(PopupState.NONE)
    val popupState: StateFlow<PopupState>
        get() = _popupState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?>
        get() = _errorMessage

    internal var isFetching: Boolean = false
    internal var timerJob: Job? = null

    fun setPopupState(state: PopupState) {
        _popupState.value = state
    }

    /**
     * Sets the error message and sets the popup state to [PopupState.ERROR].
     *
     * @property message The error message to be displayed in the popup.
     */
    protected fun setError(message: String) {
        _errorMessage.value = message
        setPopupState(PopupState.ERROR)
    }

    fun resetPopupState() {
        _errorMessage.value = null
        setPopupState(PopupState.NONE)
    }

    internal fun startTimerUntilLoadingPopup(timeMillis: Long) {
        timerJob?.cancel()
        isFetching = true
        timerJob = viewModelScope.launch {
            delay(timeMillis)
            while (isFetching) {
                setPopupState(PopupState.LOADING)
                delay(100)
            }
        }
    }
}