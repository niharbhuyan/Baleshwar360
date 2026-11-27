package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface OpenWeatherMapApiService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double = 21.4934,
        @Query("lon") lon: Double = 86.9135,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): OpenWeatherCurrentResponse

    @GET("data/2.5/forecast")
    suspend fun getFiveDayForecast(
        @Query("lat") lat: Double = 21.4934,
        @Query("lon") lon: Double = 86.9135,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): OpenWeatherForecastResponse

    @GET("data/2.5/air_pollution")
    suspend fun getAirPollution(
        @Query("lat") lat: Double = 21.4934,
        @Query("lon") lon: Double = 86.9135,
        @Query("appid") apiKey: String
    ): OpenWeatherAirPollutionResponse

    @GET("data/3.0/onecall")
    suspend fun getOneCallAlerts(
        @Query("lat") lat: Double = 21.4934,
        @Query("lon") lon: Double = 86.9135,
        @Query("exclude") exclude: String = "minutely,hourly,daily",
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): OpenWeatherOneCallAlertsResponse

    companion object {
        private const val BASE_URL = "https://api.openweathermap.org/"

        fun create(): OpenWeatherMapApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val req = chain.request().newBuilder()
                        .header("User-Agent", "Baleshwar360-Android/1.0 (com.niharsales.baleshwar360)")
                        .build()
                    chain.proceed(req)
                }
                .addInterceptor(logging)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(OpenWeatherMapApiService::class.java)
        }
    }
}
