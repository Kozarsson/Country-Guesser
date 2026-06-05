package org.kth.countryguesser.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kth.countryguesser.Application
import org.kth.countryguesser.model.repository.CountryRepository
import org.kth.countryguesser.ui.model.CountryUiModel
import org.kth.countryguesser.ui.model.toUiModel
import org.kth.countryguesser.util.GamePopupState
import org.kth.countryguesser.util.NetworkUtils
import org.kth.countryguesser.util.PopupState
import javax.inject.Inject

interface StudyVM {
    val searchResults: StateFlow<List<Pair<String, String?>>>
    val countryInfo: StateFlow<CountryUiModel?>

    fun searchCountries(searchQuery: String)
    fun fetchCountry(countryName: String)
}

@HiltViewModel
class StudyVMImpl @Inject constructor(
    private val countryRepository: CountryRepository,
) : BaseVM(), StudyVM {

    private val _searchResults = MutableStateFlow<List<Pair<String, String?>>>(listOf())
    override val searchResults: StateFlow<List<Pair<String, String?>>>
        get() = _searchResults

    private val _countryInfo = MutableStateFlow<CountryUiModel?>(null)
    override val countryInfo: StateFlow<CountryUiModel?>
        get() = _countryInfo

    init {
        viewModelScope.launch {
            _searchResults.value = countryRepository.getAllCountrySearchResults()
        }
    }

    override fun searchCountries(searchQuery: String) {
        viewModelScope.launch {
            if (!NetworkUtils.isNetworkAvailable(Application.APPLICATION.applicationContext)) {
                setPopupState(PopupState.NO_INTERNET)
            } else {
                val countries = countryRepository.getAllCountrySearchResults()

                val sortedResult = countries.sortedWith(
                    compareBy<Pair<String, String?>> {
                        !it.first.startsWith(
                            searchQuery,
                            ignoreCase = true
                        )
                    }
                        .thenBy { it.first.lowercase() }
                )
                _searchResults.value = sortedResult
            }
        }
    }

    override fun fetchCountry(countryName: String) {
        //TODO: Refetch after internet establishment
        viewModelScope.launch {
            if (!NetworkUtils.isNetworkAvailable(Application.APPLICATION.applicationContext)) {
                setPopupState(PopupState.NO_INTERNET)
            } else {
                setPopupState(PopupState.LOADING)
                startTimerUntilLoadingPopup(1000)

                val result = countryRepository.getCountryByName(countryName)
                isFetching = false
                setPopupState(PopupState.NONE)

                _countryInfo.value = result?.toUiModel()
            }
        }
    }

    fun clearCountryInfo() {
        _countryInfo.value = null
    }
}