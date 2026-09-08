package com.module.notelycompose.ai

import com.module.notelycompose.core.debugPrintln
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Thin wrapper over OpenRouter's OpenAI-compatible chat completion endpoint. OpenRouter fronts
 * every provider (Google, Anthropic, OpenAI, ...) behind this one API, so switching the model the
 * user has chosen in Settings is just a different `model` string — no per-provider code needed.
 *
 * IMPORTANT: never log the Authorization header — it carries the user's API key.
 */
class OpenRouterClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            // Summarizing a 15-20 minute transcript can legitimately take a while on a
            // reasoning-heavy model — don't time out too aggressively.
            requestTimeoutMillis = 90_000
            connectTimeoutMillis = 20_000
        }
    }

    suspend fun complete(
        apiKey: String,
        model: String,
        systemPrompt: String,
        userText: String
    ): AiResult {
        if (apiKey.isBlank()) {
            return AiResult.Failure(
                AiFailureReason.NO_API_KEY,
                "Chưa cấu hình OpenRouter API key trong Cài đặt."
            )
        }
        if (userText.isBlank()) {
            return AiResult.Failure(
                AiFailureReason.EMPTY_TRANSCRIPT,
                "Chưa có nội dung ghi âm để xử lý."
            )
        }

        return try {
            val response: HttpResponse = httpClient.post(OPENROUTER_CHAT_COMPLETIONS_URL) {
                header("Authorization", "Bearer $apiKey")
                header("HTTP-Referer", "https://github.com/Notely-Voice/NotelyVoice")
                header("X-Title", "Notely Voice")
                contentType(ContentType.Application.Json)
                setBody(
                    ChatCompletionRequest(
                        model = model,
                        messages = listOf(
                            ChatMessage(role = "system", content = systemPrompt),
                            ChatMessage(role = "user", content = userText)
                        )
                    )
                )
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val completion: ChatCompletionResponse = response.body()
                    val text = completion.choices.firstOrNull()?.message?.content?.trim()
                    if (text.isNullOrBlank()) {
                        AiResult.Failure(
                            AiFailureReason.UNKNOWN,
                            "AI không trả về nội dung nào. Thử lại sau."
                        )
                    } else {
                        AiResult.Success(text)
                    }
                }

                HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> {
                    AiResult.Failure(
                        AiFailureReason.AUTH,
                        "API key không hợp lệ hoặc đã bị thu hồi."
                    )
                }

                HttpStatusCode.TooManyRequests, HttpStatusCode.PaymentRequired -> {
                    AiResult.Failure(
                        AiFailureReason.RATE_LIMIT_OR_QUOTA,
                        "Đã hết quota hoặc bị giới hạn tốc độ gọi API. Thử lại sau ít phút."
                    )
                }

                else -> {
                    val errorMessage = runCatching { response.body<ChatCompletionResponse>().error?.message }
                        .getOrNull()
                    AiResult.Failure(
                        AiFailureReason.UNKNOWN,
                        errorMessage ?: "Lỗi không xác định từ OpenRouter (mã ${response.status.value})."
                    )
                }
            }
        } catch (e: HttpRequestTimeoutException) {
            AiResult.Failure(
                AiFailureReason.NETWORK,
                "Hết thời gian chờ phản hồi — kiểm tra kết nối mạng và thử lại."
            )
        } catch (e: Exception) {
            debugPrintln { "OpenRouterClient error: ${e.message}" }
            AiResult.Failure(
                AiFailureReason.NETWORK,
                "Không thể kết nối tới OpenRouter — kiểm tra kết nối mạng."
            )
        }
    }
}
