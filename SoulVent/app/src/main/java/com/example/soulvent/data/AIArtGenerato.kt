package com.example.soulvent.data

import android.graphics.Bitmap
import com.example.soulvent.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AIArtGenerator {

    suspend fun generateImage(prompt: String): Bitmap? {
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.9f
            }
        )

        return withContext(Dispatchers.IO) {
            try {
                val fullPrompt = "An abstract, dream-like, and ethereal digital art piece representing the feeling of: '$prompt'. Use a soft, pastel color palette with flowing lines and gentle gradients."

                val response = generativeModel.generateContent(fullPrompt)

                // Correctly and safely access the image from the response
                return@withContext response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.let { part ->
                    (part as? com.google.ai.client.generativeai.type.ImagePart)?.image
                }

            } catch (e: Exception) {
                null
            }
        }
    }
}