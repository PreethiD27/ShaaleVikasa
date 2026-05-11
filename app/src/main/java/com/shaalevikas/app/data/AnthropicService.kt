package com.shaalevikas.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AnthropicService {
    // TODO: Replace with your Anthropic API key
    private const val API_KEY = "YOUR_ANTHROPIC_API_KEY"
    private const val API_URL = "https://api.anthropic.com/v1/messages"
    private const val MODEL   = "claude-sonnet-4-20250514"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateNeedDescription(briefPrompt: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val body = JSONObject().apply {
                    put("model", MODEL)
                    put("max_tokens", 300)
                    put("messages", JSONArray().put(JSONObject().apply {
                        put("role", "user")
                        put("content",
                            "You are helping a rural school headmaster in Karnataka describe a school " +
                            "repair/resource need for alumni donors. Write a compelling, empathetic, and " +
                            "clear 3-sentence description in English for the following need. Be specific " +
                            "about the impact on students.\n\nNeed: $briefPrompt"
                        )
                    }))
                }.toString()

                val req = Request.Builder()
                    .url(API_URL)
                    .addHeader("x-api-key", API_KEY)
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("content-type", "application/json")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                val resp = client.newCall(req).execute()
                val json = JSONObject(resp.body!!.string())
                json.getJSONArray("content").getJSONObject(0).getString("text").trim()
            }
        }

    suspend fun generateImpactSummary(needTitle: String, beforeDesc: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val body = JSONObject().apply {
                    put("model", MODEL)
                    put("max_tokens", 200)
                    put("messages", JSONArray().put(JSONObject().apply {
                        put("role", "user")
                        put("content",
                            "Write a short 2-sentence impact summary for a rural school improvement " +
                            "completed thanks to alumni donations.\n\nNeed: $needTitle\n" +
                            "Original problem: $beforeDesc\n\nFocus on positive impact on students."
                        )
                    }))
                }.toString()

                val req = Request.Builder()
                    .url(API_URL)
                    .addHeader("x-api-key", API_KEY)
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("content-type", "application/json")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                val resp = client.newCall(req).execute()
                val json = JSONObject(resp.body!!.string())
                json.getJSONArray("content").getJSONObject(0).getString("text").trim()
            }
        }
}
