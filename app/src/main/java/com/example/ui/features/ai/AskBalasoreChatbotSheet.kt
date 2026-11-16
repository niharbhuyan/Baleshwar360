package com.example.ui.features.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.remote.ChatMessage
import com.example.data.remote.ChatRolePersona
import com.example.data.remote.GeminiChatModel

@Composable
fun AskBalasoreChatbotSheet(
    chatMessages: List<ChatMessage>,
    isLoading: Boolean,
    selectedModel: GeminiChatModel,
    selectedRole: ChatRolePersona,
    isSearchGroundingEnabled: Boolean,
    isMapsGroundingEnabled: Boolean,
    language: AppLanguage,
    onSendMessage: (String) -> Unit,
    onSelectModel: (GeminiChatModel) -> Unit,
    onSelectRole: (ChatRolePersona) -> Unit,
    onToggleSearchGrounding: () -> Unit,
    onToggleMapsGrounding: () -> Unit,
    onClearChat: () -> Unit,
    onGenerateMusic: (String) -> Unit = {},
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱର ୩୬୦ ଗାଇଡ୍" else "Ask Baleshwar 360 Guide",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Row {
                IconButton(onClick = onClearChat) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear")
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatMessages) { msg ->
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = msg.text,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.5.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask about Chandipur tides, temples, trains...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                enabled = !isLoading && inputText.isNotBlank(),
                onClick = {
                    onSendMessage(inputText)
                    inputText = ""
                }
            ) {
                Text("Send")
            }
        }
    }
}
