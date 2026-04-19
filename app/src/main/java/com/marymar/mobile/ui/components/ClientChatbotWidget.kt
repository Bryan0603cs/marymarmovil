package com.marymar.mobile.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marymar.mobile.presentation.viewmodel.ChatbotMessageUi
import com.marymar.mobile.presentation.viewmodel.ChatbotUiState

private val ChatHeader = Color(0xFF0F4A60)
private val ChatBg = Color(0xFFF4F6F8)
private val ChatBubbleBot = Color.White
private val ChatBubbleUser = Color(0xFFE8F2F5)
private val ChatInputBg = Color.White
private val ChatSend = Color(0xFF0F4A60)
private val ChatFab = Color(0xFF0F4A60)
private val ChatText = Color(0xFF24323D)
private val ChatMuted = Color(0xFF6B7280)
private val ChatBorder = Color(0xFFD8DEE6)

@Composable
fun ClientChatbotWidget(
    expanded: Boolean,
    ui: ChatbotUiState,
    onToggleExpanded: () -> Unit,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var input by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AnimatedVisibility(visible = expanded) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.93f)
                    .heightIn(min = 420.dp, max = 560.dp),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 12.dp,
                color = ChatBg
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ChatHeader)
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Asistente Mar y Mar",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Respuestas automáticas",
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            text = "✕",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable { onToggleExpanded() }
                                .padding(top = 2.dp)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true)
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        contentPadding = PaddingValues(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(ui.messages, key = { it.id }) { message ->
                            ChatMessageBubble(message)
                        }

                        if (ui.sending) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = ChatBubbleBot,
                                    tonalElevation = 0.dp,
                                    shadowElevation = 1.dp
                                ) {
                                    Text(
                                        text = "Escribiendo...",
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        color = ChatMuted,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ChatBg)
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = input,
                            onValueChange = { input = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = ChatText,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    color = ChatInputBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ChatBorder)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (input.isBlank()) {
                                            Text(
                                                text = "Escribe tu mensaje...",
                                                color = ChatMuted,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            }
                        )

                        Surface(
                            onClick = {
                                val toSend = input.trim()
                                if (toSend.isNotBlank() && !ui.sending) {
                                    onSendMessage(toSend)
                                    input = ""
                                }
                            },
                            shape = RoundedCornerShape(18.dp),
                            color = ChatSend,
                            contentColor = Color.White,
                            modifier = Modifier.height(54.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Enviar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onToggleExpanded,
            containerColor = ChatFab,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 10.dp)
        ) {
            Text(
                text = "🤖",
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun ChatMessageBubble(message: ChatbotMessageUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (message.fromUser) ChatBubbleUser else ChatBubbleBot,
            shadowElevation = if (message.fromUser) 0.dp else 1.dp,
            tonalElevation = 0.dp,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                color = ChatText,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}