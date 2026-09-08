package com.module.notelycompose.modelDownloader

import com.module.notelycompose.onboarding.data.PreferencesRepository
import kotlinx.coroutines.flow.first

const val NO_MODEL_SELECTION = -1
const val STANDARD_MODEL_SELECTION = 0
const val OPTIMIZED_MODEL_SELECTION = 1
const val ENGLISH_MODEL = "en"
const val OPTIMIZED_MODEL = "en"
const val HINDI_MODEL = "hi"
const val FARSI = "fa"
const val GUJARATI = "gu"
const val VIETNAMESE_MODEL = "vi"

data class TranscriptionModel(val name:String, val modelType: String, val size:String, val description:String, val url:String){
    fun getModelDownloadSize():String = size
    fun getModelDownloadType():String = modelType
}

class ModelSelection(private val preferencesRepository: PreferencesRepository) {

    /**
     * Available Whisper models
     */
    private val models = listOf(
        TranscriptionModel(
            "ggml-base-en.bin",
            ENGLISH_MODEL,
            "142 MB",
            "Multilingual model (supports 50+ languages)",
            "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.bin"
        ),
        TranscriptionModel(
            "ggml-small.bin",
            OPTIMIZED_MODEL,
            "465 MB",
            "Multilingual model (supports 50+ languages, slower, more-accurate)",
            "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.bin"
        ),
        // index 2 model hindi
        TranscriptionModel(
            "ggml-base-hi.bin",
            HINDI_MODEL,
            "140 MB",
            "Hindi/Gujarati optimized model",
            "https://huggingface.co/khidrew/whisper-base-hindi-ggml/resolve/main/ggml-base-hi.bin"
        ),
        // index 3 model: large-v3-turbo, quantized. Tonal languages (Vietnamese, Thai, etc.)
        // need this tier — the base/small multilingual models above miss diacritics and tones
        // far too often to be usable.
        TranscriptionModel(
            "ggml-large-v3-turbo-q5_0.bin",
            VIETNAMESE_MODEL,
            "547 MB",
            "Multilingual large model (best accuracy for tonal languages like Vietnamese, slower)",
            "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-large-v3-turbo-q5_0.bin"
        )
    )

    /**
     * Get the appropriate model based on transcription language
     * @return The model to use
     */
    suspend fun getSelectedModel(): TranscriptionModel {
        val defaultLanguage = preferencesRepository.getDefaultTranscriptionLanguage().first()
        return when (defaultLanguage) {
            HINDI_MODEL, GUJARATI -> models[2] // hindi
            FARSI -> models[1] // optimised
            // Vietnamese: "ggml-base" (index 0) is unusable for tones/diacritics, so large-v3-turbo
            // is the default here. Only an explicit "standard" (fast/light) choice falls back to
            // small — for weaker devices, not as the out-of-the-box experience.
            VIETNAMESE_MODEL -> {
                if (preferencesRepository.getModelSelection().first() == STANDARD_MODEL_SELECTION) {
                    models[1] // small, lighter fallback for weaker devices
                } else {
                    models[3] // large-v3-turbo, best accuracy — default for Vietnamese
                }
            }
            else -> {
                if(preferencesRepository.getModelSelection().first() == STANDARD_MODEL_SELECTION
                    || preferencesRepository.getModelSelection().first() == NO_MODEL_SELECTION) {
                    models[0] // standard
                } else {
                    models[1] // optimised
                }
            }
        }
    }

    fun getDefaultTranscriptionModel() = models[0]


}