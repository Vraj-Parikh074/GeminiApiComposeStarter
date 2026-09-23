package com.example.n074_vraj_assignment1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: GeminiRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getChatHistory().collect { history ->
                _uiState.update { it.copy(messages = history) }
            }
        }
    }

    fun selectPersona(persona: AiPersona) {
        _uiState.update { it.copy(activePersona = persona) }
    }

    fun addCustomPersona(persona: AiPersona) {
        _uiState.update { state ->
            val updatedCustoms = state.customPersonas + persona
            state.copy(
                customPersonas = updatedCustoms,
                activePersona = persona,
            )
        }
    }

    fun updateTemperature(temp: Float) {
        _uiState.update { it.copy(temperature = temp) }
    }

    fun sendMessage(prompt: String) {
        if (prompt.isBlank()) return

        val currentState = _uiState.value
        val activePersona = currentState.activePersona
        val currentTemp = currentState.temperature

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.sendMessage(prompt, activePersona.systemPrompt, currentTemp)
            _uiState.update { state ->
                if (result.isSuccess) {
                    state.copy(isLoading = false)
                } else {
                    state.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "An error occurred.",
                    )
                }
            }
        }
    }

    fun regenerateLastMessage() {
        val lastUserMessage = _uiState.value.messages.lastOrNull { it.sender == "USER" }
        lastUserMessage?.let {
            sendMessage(it.text)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
