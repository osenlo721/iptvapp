package com.iptvapp.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvapp.data.model.Channel
import com.iptvapp.data.repository.ChannelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerState(
    val channel: Channel? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repo: ChannelRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    fun loadChannel(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val channel = repo.getChannelById(id)
            if (channel != null) {
                _state.update { it.copy(channel = channel, isLoading = false) }
            } else {
                _state.update { it.copy(isLoading = false, error = "Canal no encontrado") }
            }
        }
    }

    fun toggleFavorite() {
        val ch = _state.value.channel ?: return
        viewModelScope.launch {
            repo.toggleFavorite(ch.id, !ch.isFavorite)
            _state.update { it.copy(channel = ch.copy(isFavorite = !ch.isFavorite)) }
        }
    }
}
