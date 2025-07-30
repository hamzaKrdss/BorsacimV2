package com.example.borsacimv1.data


data class StockItem(
    val symbol: String,
    val currentPrice: Double,
    val high: Double,
    val low: Double,
    val open: Double,
    val prevClose: Double,
    val percentChange: Double
)

fun QuoteResponse.toStockItem(symbol: String): StockItem {
    val percentChange = if (pc != 0.0) ((c - pc) / pc) * 100 else 0.0
    return StockItem(
        symbol = symbol,
        currentPrice = c,
        high = h,
        low = l,
        open = o,
        prevClose = pc,
        percentChange = percentChange
    )
}
