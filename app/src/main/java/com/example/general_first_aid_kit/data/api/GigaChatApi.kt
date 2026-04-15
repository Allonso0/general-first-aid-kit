package com.example.general_first_aid_kit.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface GigaChatApi {
    @FormUrlEncoded
    @Headers(
        "Accept: application/json"
    )
    @POST("https://ngw.devices.sberbank.ru:9443/api/v2/oauth")
    suspend fun getAccessToken(
        @Header("Authorization") authHeader: String,
        @Header("RqUID") rqUid: String,
        @Field("scope") scope: String = "GIGACHAT_API_PERS"
    ): GigaTokenResponse

    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    suspend fun getMedicationDetails(
        @Header("Authorization") token: String,
        @Body request: GigaChatRequest
    ): GigaChatResponse
}

data class GigaChatRequest(val model: String, val messages: List<Message>, val responseFormat: ResponseFormat)
data class Message(
    val role: String,
    val content: String)
data class ResponseFormat(val type: String = "json_object")
data class GigaChatResponse(val choices: List<Choice>)
data class Choice(val message: Message)
data class GigaTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_at") val expiresAt: Long
)