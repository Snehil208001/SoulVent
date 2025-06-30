package com.example.soulvent.data

import android.graphics.Bitmap
import android.util.Log
import com.example.soulvent.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.ImagePart
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Sealed class to represent the result of the image generation
sealed class GenerationResult {
    data class Success(val bitmap: Bitmap) : GenerationResult()
    data class Error(val message: String) : GenerationResult()
}

object AIArtGenerator {

    private const val MODEL_NAME = "gemini-1.5-flash"

    suspend fun generateImage(prompt: String): GenerationResult {
        if (BuildConfig.GEMINI_API_KEY.isBlank() || BuildConfig.GEMINI_API_KEY == "null") {
            return GenerationResult.Error("API Key is missing. Please add it to your local.properties file.")
        }

        val generativeModel = GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.9f
            }
        )

        return withContext(Dispatchers.IO) {
            try {
                val fullPrompt = "An abstract, dream-like, and ethereal digital art piece representing the feeling of: '$prompt'. Use a soft, pastel color palette with flowing lines and gentle gradients."
                Log.d("AIArtGenerator", "Generating image with prompt: $fullPrompt")

                val response: GenerateContentResponse = generativeModel.generateContent(fullPrompt)

                val imagePart = response.candidates.firstOrNull()?.content?.parts?.firstOrNull() as? ImagePart
                val bitmap = imagePart?.image

                if (bitmap != null) {
                    GenerationResult.Success(bitmap)
                } else {
                    val errorReason = response.promptFeedback?.blockReason?.name ?: "Unknown - the model did not return an image."
                    Log.e("AIArtGenerator", "Image generation failed. Reason: $errorReason")
                    GenerationResult.Error("Failed to generate image. Reason: $errorReason")
                }

            } catch (e: Exception) {
                Log.e("AIArtGenerator", "Error generating image: ${e.message}", e)
                GenerationResult.Error("An unexpected error occurred: ${e.localizedMessage}")
            }
        }
    }
}