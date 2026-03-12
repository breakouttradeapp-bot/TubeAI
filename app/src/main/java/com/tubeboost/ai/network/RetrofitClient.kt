package com.tubeboost.ai.network

import com.tubeboost.ai.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client singleton for Cerebras AI API
 */
object RetrofitClient {

    private const val BASE_URL = "https://api.cerebras.ai/"
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 60L
    private const val WRITE_TIMEOUT = 30L

    /**
     * Auth interceptor that adds the Bearer token to every request
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest: Request = chain.request()
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${BuildConfig.CEREBRAS_API_KEY}")
            .header("User-Agent", "TubeBoostAI/1.0 Android")
            .build()
        chain.proceed(authenticatedRequest)
    }

    /**
     * Logging interceptor (only active in debug builds)
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    /**
     * OkHttpClient with timeouts and interceptors
     */
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Retrofit instance
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * API service instance
     */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
