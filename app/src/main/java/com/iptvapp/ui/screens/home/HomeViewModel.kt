package com.iptvapp.ui.screens.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvapp.data.model.Channel
import com.iptvapp.data.repository.ChannelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val channels: List<Channel> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "Todos",
    val isLoading: Boolean = false,
    val snackbar: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: ChannelRepository,
    private val prefs: DataStore<Preferences>
) : ViewModel() {

    companion object {
        val KEY_DEFAULTS_LOADED = booleanPreferencesKey("defaults_loaded")
        val DEFAULT_SOURCES = listOf(
            "https://iptv-org.github.io/iptv/categories/movies.m3u",
            "https://iptv-org.github.io/iptv/categories/series.m3u",
            "https://iptv-org.github.io/iptv/categories/sports.m3u",
            "https://iptv-org.github.io/iptv/index.m3u"
        )
    }

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _category = MutableStateFlow("Todos")

    init {
        viewModelScope.launch {
            _category.flatMapLatest { cat ->
                if (cat == "Todos") repo.getAllChannels()
                else repo.getChannelsByCategory(cat)
            }.collect { ch -> _state.update { it.copy(channels = ch) } }
        }
        viewModelScope.launch {
            repo.getAllCategories().collect { cats ->
                _state.update { it.copy(categories = listOf("Todos") + cats) }
            }
        }
        viewModelScope.launch {
            val alreadyLoaded = prefs.data.map { it[KEY_DEFAULTS_LOADED] ?: false }.first()
            if (!alreadyLoaded) loadDefaultSources()
        }
    }

    private fun loadDefaultSources() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, snackbar = "Cargando canales...") }
            repo.loadFromUrls(DEFAULT_SOURCES).fold(
                onSuccess = { count ->
                    prefs.edit { it[KEY_DEFAULTS_LOADED] = true }
                    _state.update { it.copy(isLoading = false, snackbar = "$count canales cargados") }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, snackbar = "Error: ${e.message}") }
                }
            )
        }
    }

    fun selectCategory(cat: String) {
        _category.value = cat
        _state.update { it.copy(selectedCategory = cat) }
    }

    fun loadM3u(url: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repo.loadFromUrl(url).fold(
                onSuccess = { count -> _state.update { it.copy(isLoading = false, snackbar = "$count canales cargados") } },
                onFailure = { e  -> _state.update { it.copy(isLoading = false, snackbar = "Error: ${e.message}") } }
            )
        }
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch { repo.toggleFavorite(channel.id, !channel.isFavorite) }
    }

    fun clearSnackbar() = _state.update { it.copy(snackbar = null) }
}
