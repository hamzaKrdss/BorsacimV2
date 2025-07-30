package com.example.borsacimv1.retro

import com.example.borsacimv1.data.QuoteResponse
import com.example.borsacimv1.data.SymbolResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FinnhubApi {
    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("token") token: String
    ): QuoteResponse

    @GET("stock/symbol")
    suspend fun getAllSymbols(
        @Query("exchange") exchange: String = "US",
        @Query("token") token: String
    ): List<SymbolResponse>

}


