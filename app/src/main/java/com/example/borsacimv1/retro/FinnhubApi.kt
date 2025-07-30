package com.example.borsacimv1.retro

import com.example.borsacimv1.data.QuoteResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FinnhubApi {
    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("token") token: String
    ): QuoteResponse
}
