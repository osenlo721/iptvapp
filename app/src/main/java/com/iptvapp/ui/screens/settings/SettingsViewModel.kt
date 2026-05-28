package com.iptvapp.ui.screens.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvapp.data.repository.ChannelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val savedUrl: String = "",
    val isLoading: Boolean = false,
    val snackbar: String? = null,
    val channelCount: Int = 0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: ChannelRepository,
    private val prefs: DataStore<Preferences>
) : ViewModel() {

    companion object {
        val KEY_M3U_URL = stringPreferencesKey("m3u_url")
    }

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.data.map { it[KEY_M3U_URL] ?: "" }
                .collect { url -> _state.update { it.copy(savedUrl = url) } }
        }
        viewModelScope.launch {
            repo.getAllChannels().collect { list ->
                _state.update { it.copy(channelCount = list.size) }
            }
        }
    }

    fun loadM3u(url: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            prefs.edit { it[KEY_M3U_URL] = url }
            repo.loadFromUrl(url).fold(
                onSuccess = { count ->
                    _state.update { it.copy(isLoading = false, snackbar = "$count canales cargados correctamente") }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, snackbar = "Error: ${e.message}") }
                }
            )
        }
    }

    fun clearData() {
        viewModelScope.launch {
            repo.clearAll()
            prefs.edit { it.remove(KEY_M3U_URL) }
            _state.update { it.copy(snackbar = "Datos borrados") }
        }
    }

    fun clearSnackbar() = _state.update { it.copy(snackbar = null) }
}
