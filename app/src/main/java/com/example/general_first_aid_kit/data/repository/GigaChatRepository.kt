package com.example.general_first_aid_kit.data.repository

import android.util.Log
import com.example.general_first_aid_kit.BuildConfig
import com.example.general_first_aid_kit.data.api.EanDbApi
import com.example.general_first_aid_kit.data.api.GigaChatApi
import com.example.general_first_aid_kit.data.api.GigaChatRequest
import com.example.general_first_aid_kit.data.api.Message
import com.example.general_first_aid_kit.data.api.MedicationInfoJson
import com.example.general_first_aid_kit.data.api.ResponseFormat
import com.google.gson.Gson
import javax.inject.Inject

class GigaChatRepository @Inject constructor(
    private val api: GigaChatApi,
    private val eanDbApi: EanDbApi,
    private val credentials: String
) {
    private var cachedToken: String? = null
    private var tokenExpiresAt: Long = 0
    private val gson = Gson()

    private suspend fun getToken(): String {
        val currentTime = System.currentTimeMillis()
        if (cachedToken != null && currentTime < tokenExpiresAt - 60000) {
            return "Bearer $cachedToken"
        }

        val response = api.getAccessToken(
            authHeader = "Basic $credentials",
            rqUid = java.util.UUID.randomUUID().toString()
        )

        cachedToken = response.accessToken
        tokenExpiresAt = response.expiresAt
        return "Bearer $cachedToken"
    }

    suspend fun getMedicationByBarcode(barcode: String): Result<MedicationInfoJson> {
        return try {

            val eanResponse = try {
                eanDbApi.getProductByBarcode(barcode, "Bearer ${BuildConfig.EANDB_TOKEN}")
            } catch (e: Exception) {
                Log.e("EAN-DB", "API error", e)
                null
            }

            val productTitle = eanResponse?.product?.titles?.get("ru")
                ?: eanResponse?.product?.titles?.get("en")
                ?: eanResponse?.product?.titles?.values?.firstOrNull()

            if (productTitle.isNullOrBlank()) {
                return Result.failure(Exception("Лекарство не найдено в базе данных штрих-кодов"))
            }

            Log.d("GigaChat", "Extracted Title from EAN-DB: $productTitle")

            val token = getToken()
            val systemMessage = Message(
                role = "system",
                content = """
                Ты — эксперт-фармацевт. Твоя задача — распарсить название лекарства и вернуть JSON.
                Правила для полей:
                1. "name": только основное название и дозировка (напр. "Детримакс 230мг").
                2. "category": выбери из списка (Обезболивающее, Жаропонижающее, Антибиотик, Витамины, Антисептик, Спазмолитик, Антигистаминное, Без категории).
                3. "quantity": только число.
                4. "unit": сокращай (таблетки -> табл., капсулы -> капс., мл -> мл, грамм -> г).
                Ответ строго JSON: {"name":"..","category":"..","quantity":"..","unit":".."}
            """.trimIndent()
            )

            val userMessage = Message(
                role = "user",
                content = productTitle
            )

            val request = GigaChatRequest(
                model = "GigaChat",
                messages = listOf(systemMessage, userMessage),
                responseFormat = ResponseFormat("json_object")
            )

            val response = api.getMedicationDetails(token, request)
            val jsonContent = response.choices.first().message.content

            Log.d("GigaChat", "GigaChat Structured Response: $jsonContent")

            val info = gson.fromJson(jsonContent, MedicationInfoJson::class.java)

            val finalInfo = if (info.name.isBlank() || info.name == "..") {
                info.copy(name = productTitle)
            } else info

            Result.success(finalInfo)
        } catch (e: Exception) {
            Log.e("GigaChat", "Error in pipeline", e)
            Result.failure(e)
        }
    }
}