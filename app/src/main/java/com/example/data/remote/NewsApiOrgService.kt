package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Retrofit service for NewsAPI v2 (`https://newsapi.org/`).
 * Authenticates via the `X-Api-Key` header populated automatically from `BuildConfig.NEWS_API_KEY`.
 */
interface NewsApiOrgService {

    @GET("v2/everything")
    suspend fun searchEverything(
        @Query("q") query: String,
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 10,
        @Header("X-Api-Key") apiKey: String
    ): NewsApiOrgResponse

    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String? = "in",
        @Query("category") category: String? = null,
        @Query("q") query: String? = null,
        @Query("pageSize") pageSize: Int = 10,
        @Header("X-Api-Key") apiKey: String
    ): NewsApiOrgResponse

    companion object {
        const val BASE_URL = "https://newsapi.org/"

        fun create(): NewsApiOrgService {
            // Do not attach HttpLoggingInterceptor with HEADERS/BODY so X-Api-Key is never leaked to Logcat
            val client = OkHttpClient.Builder()
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "Baleshwar360-Android/1.0")
                        .header("Accept", "application/json")
                        .build()
                    chain.proceed(request)
                }
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(NewsApiOrgService::class.java)
        }
    }
}
