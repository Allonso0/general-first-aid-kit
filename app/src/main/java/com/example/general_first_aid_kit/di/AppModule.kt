package com.example.general_first_aid_kit.di

import com.example.general_first_aid_kit.BuildConfig
import com.example.general_first_aid_kit.data.api.EanDbApi
import com.example.general_first_aid_kit.data.api.GigaChatApi
import com.example.general_first_aid_kit.data.repository.GigaChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFireStore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideGigaChatApi(): GigaChatApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // TODO: добавить сертификат Минцифры
        val trustAllCerts = object : javax.net.ssl.X509TrustManager {
            override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
        }

        val sslContext = javax.net.ssl.SSLContext.getInstance("TLSv1.2")
        sslContext.init(null, arrayOf(trustAllCerts), java.security.SecureRandom())

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()

                if (original.url.host == "gigachat.devices.sberbank.ru") {
                    requestBuilder.header("Host", "gigachat.devices.sberbank.ru")
                }

                chain.proceed(requestBuilder.build())
            }
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts)
            .hostnameVerifier { _, _ -> true }
            .build()

        return Retrofit.Builder()
            .baseUrl("https://gigachat.devices.sberbank.ru/api/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GigaChatApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGigaChatRepository(api: GigaChatApi, eanDbApi: EanDbApi): GigaChatRepository {
        return GigaChatRepository(
            api = api,
            credentials = BuildConfig.GIGACHAT_CREDENTIALS,
            eanDbApi = eanDbApi
        )
    }

    @Provides
    @Singleton
    fun provideEanDbApi(): EanDbApi {
        return Retrofit.Builder()
            .baseUrl("https://ean-db.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EanDbApi::class.java)
    }
}