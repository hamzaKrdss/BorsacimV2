package com.example.borsacimv1.data.dao

import retrofit2.http.GET
import retrofit2.http.Query

interface FinnhubApi {
    @GET("search")
    suspend fun searchSymbols(
        @Query("q") query: String,
        @Query("token") token: String
    ): SearchResponse

    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("token") token: String
    ): QuoteResponse
}

data class SearchResponse(
    val count: Int,
    val result: List<StockSearchResult>
)

data class StockSearchResult(
    val description: String,
    val symbol: String
)

data class QuoteResponse(
    val c: Double,  // Current price
    val h: Double,
    val l: Double,
    val o: Double,
    val pc: Double,
    val t: Long
)
