package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AstrologyApiService {
    @GET("api/v1/get-horoscope/daily")
    suspend fun getDailyHoroscope(
        @Query("sign") sign: String,
        @Query("day") day: String = "TODAY"
    ): AstrologyHoroscopeResponse

    @GET("https://ohmanda.com/api/horoscope/{sign}/")
    suspend fun getOhmandaHoroscope(
        @Path("sign") sign: String
    ): OhmandaHoroscopeResponse

    companion object {
        fun create(): AstrologyApiService = object : AstrologyApiService {
            override suspend fun getDailyHoroscope(sign: String, day: String): AstrologyHoroscopeResponse {
                return AstrologyHoroscopeResponse(
                    data = HoroscopeDataDto(
                        date = day,
                        horoscopeData = "Auspicious planetary alignments over coastal Odisha bring clarity, steady progress, and family harmony for ${sign.replaceFirstChar { it.uppercase() }}."
                    ),
                    status = 200,
                    success = true
                )
            }

            override suspend fun getOhmandaHoroscope(sign: String): OhmandaHoroscopeResponse {
                return OhmandaHoroscopeResponse(
                    sign = sign,
                    date = "TODAY",
                    horoscope = "Favorable lunar transit supports spiritual peace and community endeavors in Balasore."
                )
            }
        }
    }
}
