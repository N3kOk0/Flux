package com.flux.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.data.model.ProgressBoardModel
import com.flux.data.repository.ProgressBoardRepository
import com.flux.ui.events.ProgressBoardEvents
import com.flux.ui.state.ProgressBoardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProgressBoardViewModel @Inject constructor(
    val repository: ProgressBoardRepository
) : ViewModel() {
    private val _state: MutableStateFlow<ProgressBoardState> = MutableStateFlow(ProgressBoardState())
    val state: StateFlow<ProgressBoardState> = _state.asStateFlow()

    fun onEvent(event: ProgressBoardEvents) {
        viewModelScope.launch { reduce(event = event) }
    }

    private fun updateState(reducer: (ProgressBoardState) -> ProgressBoardState) {
        _state.value = reducer(_state.value)
    }

    init {
        viewModelScope.launch {
            state
                .map { it.workspaceId }
                .distinctUntilChanged()
                .filterNotNull()
                .flatMapLatest { workspaceId: String ->
                    repository.getBoardItemsByWorkspace(workspaceId)
                }
                .collect { items ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            allItems  = items
                        )
                    }
                }
        }
    }

    private fun reduce(event: ProgressBoardEvents) {
        when (event) {
            is ProgressBoardEvents.DeleteBoardItemsByWorkspace -> deleteBoardItemsByWorkspace(event.workspaceId)
            is ProgressBoardEvents.EnterWorkspace -> { updateState { if (it.workspaceId == event.workspaceId) { it } else {it.copy(workspaceId = event.workspaceId, isLoading = true) }} }
            is ProgressBoardEvents.DeleteProgressItem -> deleteProgressItem(event.data)
            is ProgressBoardEvents.UpsertProgressItem -> upsertProgressItem(event.data)
        }
    }

    private fun deleteBoardItemsByWorkspace(workspaceId: String){
        viewModelScope.launch(Dispatchers.IO) { repository.deleteBoardItemsByWorkspace(workspaceId) }
    }

    private fun deleteProgressItem(data: ProgressBoardModel){
        viewModelScope.launch(Dispatchers.IO) { repository.deleteBoardItem(data) }
    }

    private fun upsertProgressItem(data: ProgressBoardModel){
        viewModelScope.launch(Dispatchers.IO) { repository.upsertBoardItem(data) }
    }
}