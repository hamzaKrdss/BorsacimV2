package com.example.borsacimv1.data

data class QuoteResponse(
    val c: Double,  // current price
    val h: Double,  // high
    val l: Double,  // low
    val o: Double,  // open
    val pc: Double  // previous close
)
