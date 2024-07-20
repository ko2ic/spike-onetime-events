package com.ko2ic.sample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val navigationChannel = Channel<TransitionEvent>()
    val navigationChannelFlow = navigationChannel.receiveAsFlow()

    private val _navigationSharedFlow = MutableSharedFlow<TransitionEvent>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navigationSharedFlow = _navigationSharedFlow.asSharedFlow()

    private val _uiStateFlow = MutableStateFlow(MainState())
    val uiStateFlow = _uiStateFlow.asStateFlow()

    var uiState by mutableStateOf(MainState())
        private set

    fun onClickNextPageByChannel() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            delay(3_000)
            navigationChannel.send(TransitionEvent.OnClickNextPage)
            uiState = uiState.copy(isLoading = false)
        }
    }

    fun onClickNextPageBySharedFlow() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            delay(3_000)
            _navigationSharedFlow.emit(TransitionEvent.OnClickNextPage)
            uiState = uiState.copy(isLoading = false)
        }
    }

    fun onClickNextPageByStateFlow() {
        viewModelScope.launch {
            _uiStateFlow.value = _uiStateFlow.value.copy(isLoading = true)
            delay(3_000)
            _uiStateFlow.value = _uiStateFlow.value.copy(isLoading = false, shouldGoNextPageForStateFlow = true)
        }
    }

    fun onClickNextPageByState() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            delay(3_000)
            uiState = uiState.copy(isLoading = false, shouldGoNextPageForState = true)
        }
    }

    // StateFlowで画面遷移をする場合だけ利用
    fun finishTransitionForStateFlow() {
        _uiStateFlow.value = uiState.copy(shouldGoNextPageForStateFlow = false)
    }

    // Stateで画面遷移をする場合だけ利用
    fun finishTransitionForState() {
        uiState = uiState.copy(shouldGoNextPageForState = false)
    }

    // Channel or SharedFlowで画面遷移をする場合に利用
    sealed interface TransitionEvent {

        data object OnClickNextPage : TransitionEvent
    }
}

data class MainState(
    val isLoading: Boolean = false,
    val shouldGoNextPageForStateFlow: Boolean = false, // StateFlowで画面遷移をする場合だけ利用
    val shouldGoNextPageForState: Boolean = false, // Stateで画面遷移をする場合だけ利用
)