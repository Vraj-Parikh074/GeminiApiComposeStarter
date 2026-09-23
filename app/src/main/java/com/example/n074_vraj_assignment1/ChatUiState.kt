package com.example.n074_vraj_assignment1

data class AiPersona(
    val name: String,
    val icon: String,
    val description: String,
    val systemPrompt: String,
)

val defaultPersonas = listOf(
    AiPersona("Standard AI", "🤖", "General Purpose Assistant", "You are a helpful AI assistant."),
    AiPersona("Code Architect", "💻", "Expert Kotlin & Android Developer", "You are an expert Android & Kotlin developer. Provide clean, production-ready code with concise explanations."),
    AiPersona("Creative Writer", "🎨", "Storyteller & Poet", "You are a creative writer and storyteller. Craft engaging, imaginative responses."),
    AiPersona("Academic Tutor", "🎓", "Step-by-Step Teacher", "You are an expert tutor. Explain complex concepts clearly step by step."),
    AiPersona("ELI5 Explainer", "🧠", "Simple Analogies", "Explain concepts like I am 5 years old using simple, relatable analogies."),
)

data class ChatUiState(
    val messages: List<ChatMessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val activePersona: AiPersona = defaultPersonas[0],
    val customPersonas: List<AiPersona> = emptyList(),
    val temperature: Float = 0.7f,
)
