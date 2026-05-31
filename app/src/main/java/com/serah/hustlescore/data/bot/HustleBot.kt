package com.serah.hustlescore.data.bot

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.serah.hustlescore.models.ChatMessage
import kotlinx.coroutines.tasks.await

suspend fun askFinancialBot(
    question: String,
    chatHistory: List<ChatMessage>
): String {
    return try {
        val functions = FirebaseFunctions.getInstance()

        // Build history payload (exclude current question — passed separately)
        val historyPayload = chatHistory.dropLast(1).map { msg ->
            mapOf("text" to msg.text, "isUser" to msg.isUser)
        }

        val data = mapOf(
            "question" to question,
            "history"  to historyPayload
        )

        val result = functions
            .getHttpsCallable("askHustleBot")
            .call(data)
            .await()

        @Suppress("UNCHECKED_CAST")
        val resultMap = result.data as? Map<String, Any>
        resultMap?.get("reply") as? String
            ?: "Sorry, I didn't get a response. Please try again."

    } catch (e: FirebaseFunctionsException) {
        when (e.code) {
            FirebaseFunctionsException.Code.UNAUTHENTICATED ->
                "Please log in to use HustleBot."
            FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED ->
                "You've reached your daily limit of 20 messages. Come back tomorrow! 🌅"
            FirebaseFunctionsException.Code.INVALID_ARGUMENT ->
                "Please type a valid question."
            else ->
                "Something went wrong: ${e.message}. Please try again."
        }
    } catch (e: Exception) {
        "Connection error: ${e.localizedMessage ?: "Unknown error"}. Check your internet and try again."
    }
}