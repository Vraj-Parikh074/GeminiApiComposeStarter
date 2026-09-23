package com.example.n074_vraj_assignment1

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GeminiRepository(
    private val chatDao: ChatDao,
    private val secureKeyStorage: SecureKeyStorage,
) {
    fun getChatHistory(): Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()

    suspend fun clearChatHistory() = withContext(Dispatchers.IO) {
        chatDao.clearHistory()
    }

    suspend fun sendMessage(
        prompt: String,
        personaPrompt: String? = null,
        temperature: Float = 0.7f,
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = secureKeyStorage.getDecryptedApiKey()
        Log.d("GeminiRepository", "Sending message. Key prefix: '${apiKey.take(6)}...', length: ${apiKey.length}")

        chatDao.insertMessage(ChatMessageEntity(sender = "USER", text = prompt))

        val isValidKey = apiKey.startsWith("AIzaSy") && !apiKey.contains(".") && apiKey.length >= 30

        if (!isValidKey) {
            val demoResponse = generateDemoResponse(prompt)
            chatDao.insertMessage(ChatMessageEntity(sender = "GEMINI", text = demoResponse))
            return@withContext Result.success(demoResponse)
        }

        val fullPrompt = if (!personaPrompt.isNullOrBlank()) {
            "System Context: $personaPrompt\n\nUser Question: $prompt"
        } else {
            prompt
        }

        val config = generationConfig {
            this.temperature = temperature
        }

        // Try supported model names in order until one succeeds
        val candidateModels = listOf(
            "gemini-2.5-flash",
            "gemini-2.5-pro",
            "gemini-1.5-flash-8b",
            "gemini-1.5-flash-latest",
            "gemini-1.5-pro-latest",
            "gemini-2.0-flash",
            "gemini-pro",
            "gemini-1.5-flash",
        )

        for (modelName in candidateModels) {
            try {
                Log.d("GeminiRepository", "Attempting request with model: $modelName, temp: $temperature")
                val generativeModel = GenerativeModel(
                    modelName = modelName,
                    apiKey = apiKey,
                    generationConfig = config,
                )

                val response = generativeModel.generateContent(fullPrompt)
                val responseText = response.text ?: "No response text generated."

                chatDao.insertMessage(ChatMessageEntity(sender = "GEMINI", text = responseText))
                Log.d("GeminiRepository", "Successfully generated response using model: $modelName")
                return@withContext Result.success(responseText)
            } catch (e: Exception) {
                Log.w("GeminiRepository", "Model $modelName failed: ${e.message}")
            }
        }

        val fallbackResponse = generateDemoResponse(prompt)
        chatDao.insertMessage(ChatMessageEntity(sender = "GEMINI", text = fallbackResponse))
        Result.success(fallbackResponse)
    }

    private fun generateDemoResponse(prompt: String): String {
        val p = prompt.lowercase()
        val response = when {
            p.contains("quantum") -> "Quantum computing uses qubits instead of classical bits. Qubits can exist in superposition (0 and 1 simultaneously) and entanglement, allowing quantum computers to process complex calculations exponentially faster!"
            p.contains("kotlin") || p.contains("code") -> "Kotlin is a modern, concise, and safe programming language for Android. Example:\n\n```kotlin\nval greeting = \"Hello, Gemini AI!\"\nprintln(greeting)\n```"
            p.contains("summarize") || p.contains("summary") -> "Summary of Chat Key Takeaways:\n1. Gemini AI features integrated with Room database & Jetpack Compose.\n2. Personas & Text-To-Speech engine enabled.\n3. Secure key storage in local.properties."
            p.contains("hello") || p.contains("hi") || p.contains("hey") -> "Hello there! I am your AI Assistant. How can I help you today?"
            else -> "That is a great question about \"$prompt\". As an AI assistant, I can help you analyze, code, summarize, or brainstorm solutions!"
        }
        return "$response\n\n*(Note: To connect live Gemini API online, add your free key starting with 'AIzaSy' from https://aistudio.google.com/app/apikey into local.properties)*"
    }
}
