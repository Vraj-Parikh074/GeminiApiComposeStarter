package com.example.n074_vraj_assignment1

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

@Composable
fun ChatRoute(
    viewModel: ChatViewModel,
    initialInputText: String = "",
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChatScreen(
        uiState = uiState,
        initialInputText = initialInputText,
        onSendMessage = viewModel::sendMessage,
        onSelectPersona = viewModel::selectPersona,
        onCreatePersona = viewModel::addCustomPersona,
        onUpdateTemp = viewModel::updateTemperature,
        onRegenerate = viewModel::regenerateLastMessage,
        onClearHistory = viewModel::clearHistory,
        onDismissError = viewModel::clearError,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    uiState: ChatUiState,
    initialInputText: String = "",
    onSendMessage: (String) -> Unit,
    onSelectPersona: (AiPersona) -> Unit,
    onCreatePersona: (AiPersona) -> Unit,
    onUpdateTemp: (Float) -> Unit,
    onRegenerate: () -> Unit,
    onClearHistory: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var inputText by remember { mutableStateOf(initialInputText) }
    var showPersonaMenu by remember { mutableStateOf(false) }
    var showCreatePersonaDialog by remember { mutableStateOf(false) }
    var showTempDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Text To Speech Engine
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                ttsEngine = tts
            }
        }
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                inputText = spokenText
            }
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onDismissError()
        }
    }

    val promptSuggestions = listOf(
        "💡 Explain quantum computing simply",
        "🔍 Summarize our chat takeaways",
        "💻 Write a Kotlin Flow example",
        "🌐 Translate 'Hello World' into 5 languages",
        "📝 Give me an app idea outline",
    )

    if (showCreatePersonaDialog) {
        CreatePersonaDialog(
            onDismiss = { showCreatePersonaDialog = false },
            onCreate = onCreatePersona,
        )
    }

    if (showTempDialog) {
        TemperatureDialog(
            currentTemp = uiState.temperature,
            onDismiss = { showTempDialog = false },
            onTempSelected = onUpdateTemp,
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${uiState.activePersona.icon} ${uiState.activePersona.name}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showTempDialog = true }) {
                        Icon(Icons.Default.Thermostat, contentDescription = "AI Temperature")
                    }

                    Box {
                        IconButton(onClick = { showPersonaMenu = true }) {
                            Icon(Icons.Default.Psychology, contentDescription = "Change Persona")
                        }
                        DropdownMenu(
                            expanded = showPersonaMenu,
                            onDismissRequest = { showPersonaMenu = false },
                        ) {
                            Text(
                                text = "Select AI Persona",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            (defaultPersonas + uiState.customPersonas).forEach { persona ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("${persona.icon} ${persona.name}", fontWeight = FontWeight.Bold)
                                            Text(persona.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        onSelectPersona(persona)
                                        showPersonaMenu = false
                                        Toast.makeText(context, "Persona set to ${persona.name}", Toast.LENGTH_SHORT).show()
                                    },
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("+ Create Custom Persona", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                onClick = {
                                    showPersonaMenu = false
                                    showCreatePersonaDialog = true
                                },
                            )
                        }
                    }

                    IconButton(onClick = {
                        onClearHistory()
                        Toast.makeText(context, "Chat history cleared", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear History")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = uiState.messages,
                    key = { it.id },
                ) { message ->
                    ChatBubble(
                        message = message,
                        onSpeak = { text ->
                            ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "message_tts")
                        },
                        onCopy = { text ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Gemini Message", text))
                            Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        onShare = { text ->
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share AI Response"))
                        },
                        onRegenerate = onRegenerate,
                    )
                }
            }

            AnimatedVisibility(visible = uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            // Quick Prompt Suggestion Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(promptSuggestions) { chipText ->
                    AssistChip(
                        onClick = { inputText = chipText.substringAfter(" ") },
                        label = { Text(chipText, fontSize = 12.sp) },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                )

                IconButton(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                            )
                        }
                        speechLauncher.launch(intent)
                    },
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Input")
                }

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Message")
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessageEntity,
    onSpeak: (String) -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onRegenerate: () -> Unit,
) {
    val isUser = message.sender == "USER"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 16.dp,
                    ),
                )
                .background(bgColor)
                .padding(12.dp),
        ) {
            Column {
                FormattedMessageContent(
                    text = message.text,
                    textColor = textColor,
                    onCopyCode = onCopy,
                )

                if (!isUser) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Read Aloud",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onSpeak(message.text) }
                                .padding(2.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Text",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onCopy(message.text) }
                                .padding(2.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Response",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onShare(message.text) }
                                .padding(2.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry / Regenerate",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onRegenerate() }
                                .padding(2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FormattedMessageContent(
    text: String,
    textColor: Color,
    onCopyCode: (String) -> Unit,
) {
    val parts = remember(text) { parseMarkdownCodeBlocks(text) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        parts.forEach { part ->
            when (part) {
                is TextPart.Plain -> {
                    Text(text = part.text, color = textColor)
                }
                is TextPart.CodeBlock -> {
                    CodeBlockCard(
                        language = part.language,
                        code = part.code,
                        onCopyCode = onCopyCode,
                    )
                }
            }
        }
    }
}

sealed class TextPart {
    data class Plain(val text: String) : TextPart()
    data class CodeBlock(val language: String, val code: String) : TextPart()
}

fun parseMarkdownCodeBlocks(text: String): List<TextPart> {
    val result = mutableListOf<TextPart>()
    val regex = Regex("```(\\w*)\\n?([\\s\\S]*?)```")
    var lastIndex = 0

    for (match in regex.findAll(text)) {
        val start = match.range.first
        if (start > lastIndex) {
            val plainText = text.substring(lastIndex, start)
            if (plainText.isNotBlank()) {
                result.add(TextPart.Plain(plainText))
            }
        }
        val lang = match.groupValues[1].ifBlank { "code" }
        val code = match.groupValues[2].trimEnd()
        result.add(TextPart.CodeBlock(lang, code))
        lastIndex = match.range.last + 1
    }

    if (lastIndex < text.length) {
        val remaining = text.substring(lastIndex)
        if (remaining.isNotBlank()) {
            result.add(TextPart.Plain(remaining))
        }
    }

    if (result.isEmpty()) {
        result.add(TextPart.Plain(text))
    }

    return result
}

@Composable
fun CodeBlockCard(
    language: String,
    code: String,
    onCopyCode: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E1E))
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = language.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF9CDCFE),
                fontWeight = FontWeight.Bold,
            )
            IconButton(
                onClick = { onCopyCode(code) },
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Code",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = Color(0xFFD4D4D4),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
fun CreatePersonaDialog(
    onDismiss: () -> Unit,
    onCreate: (AiPersona) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("⚡") }
    var description by remember { mutableStateOf("") }
    var systemPrompt by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Custom Persona") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Persona Name (e.g. Fitness Coach)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = { Text("Emoji Icon (e.g. 🏋️)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    label = { Text("System Instructions") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && systemPrompt.isNotBlank()) {
                        onCreate(
                            AiPersona(
                                name = name,
                                icon = icon.ifBlank { "⚡" },
                                description = description.ifBlank { "Custom Assistant" },
                                systemPrompt = systemPrompt,
                            ),
                        )
                        onDismiss()
                    }
                },
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun TemperatureDialog(
    currentTemp: Float,
    onDismiss: () -> Unit,
    onTempSelected: (Float) -> Unit,
) {
    var sliderValue by remember { mutableFloatStateOf(currentTemp) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI Creativity (Temperature)") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Temperature: ${String.format(Locale.US, "%.1f", sliderValue)}")
                Text(
                    text = when {
                        sliderValue < 0.4f -> "Strict & Factual (Best for code & math)"
                        sliderValue < 0.8f -> "Balanced (Standard responses)"
                        else -> "Highly Creative (Best for writing & ideas)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0.0f..1.0f,
                    steps = 9,
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onTempSelected(sliderValue)
                onDismiss()
            }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
