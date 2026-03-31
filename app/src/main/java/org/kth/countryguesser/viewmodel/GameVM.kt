package org.kth.countryguesser.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.kth.countryguesser.model.CountryModel
import org.kth.countryguesser.model.repository.CountryRepository
import org.kth.countryguesser.model.repository.GameRepository
import org.kth.countryguesser.ui.model.CountryUiModel
import org.kth.countryguesser.ui.model.toUiModel
import javax.inject.Inject


interface GameVM {
    val guessedCountries: StateFlow<List<CountryUiModel>>
    val searchResults: StateFlow<List<CountryUiModel>>

    fun searchCountries(searchQuery: String)
    fun guessCountry(country: String)
}

@HiltViewModel
class GameVMImpl @Inject constructor(
    private val countryRepository: CountryRepository,
    private val gameRepository: GameRepository
) : ViewModel(), GameVM {
    private val _guessedCountries = MutableStateFlow<List<CountryUiModel>>(listOf())
    override val guessedCountries: StateFlow<List<CountryUiModel>>
        get() = _guessedCountries

    private val _searchResults = MutableStateFlow<List<CountryUiModel>>(listOf())
    override val searchResults: StateFlow<List<CountryUiModel>>
        get() = _searchResults

    override fun searchCountries(searchQuery: String) {
        viewModelScope.launch {
            val results = countryRepository.searchCountries(searchQuery)
            _searchResults.value = results
        }
    }

    override fun guessCountry(country: String) {
        viewModelScope.launch {
            val result = countryRepository.getCountryByName(country)
            if (result != null) {
                //if guessed country correct win game
                _guessedCountries.value += result.toUiModel()
                Log.d("GameVM", _guessedCountries.value.toString())
            } else {
                Log.e("GameVM", "No country found with name $country")
            }
        }
    }
}


enum class PopupState {
    NONE,
    SEARCH,
    LOADING,
    NO_RESULT
}