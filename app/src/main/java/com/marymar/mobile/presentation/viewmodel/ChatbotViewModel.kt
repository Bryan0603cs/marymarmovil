package com.marymar.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.domain.repository.ChatbotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatbotMessageUi(
    val id: Long,
    val text: String,
    val fromUser: Boolean
)

data class ChatbotUiState(
    val messages: List<ChatbotMessageUi> = listOf(
        ChatbotMessageUi(
            id = 1L,
            text = "Hola, soy el asistente de Mar y Mar. ¿En qué puedo ayudarte?",
            fromUser = false
        )
    ),
    val sending: Boolean = false
)

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val repository: ChatbotRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ChatbotUiState())
    val ui: StateFlow<ChatbotUiState> = _ui.asStateFlow()

    fun sendMessage(rawMessage: String) {
        val message = rawMessage.trim()
        if (message.isBlank() || _ui.value.sending) return

        val nextIdBase = System.currentTimeMillis()
        val userMessage = ChatbotMessageUi(
            id = nextIdBase,
            text = message,
            fromUser = true
        )

        _ui.value = _ui.value.copy(
            messages = _ui.value.messages + userMessage,
            sending = true
        )

        viewModelScope.launch {
            when (val result = repository.ask(message)) {
                is ApiResult.Success -> {
                    _ui.value = _ui.value.copy(
                        sending = false,
                        messages = _ui.value.messages + ChatbotMessageUi(
                            id = nextIdBase + 1,
                            text = result.data.ifBlank {
                                "Ahora mismo no tengo una respuesta disponible."
                            },
                            fromUser = false
                        )
                    )
                }

                is ApiResult.Error -> {
                    _ui.value = _ui.value.copy(
                        sending = false,
                        messages = _ui.value.messages + ChatbotMessageUi(
                            id = nextIdBase + 1,
                            text = result.message.ifBlank {
                                "No pude responder en este momento. Intenta nuevamente."
                            },
                            fromUser = false
                        )
                    )
                }
            }
        }
    }
}