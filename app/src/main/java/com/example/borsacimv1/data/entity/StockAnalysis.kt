package com.example.borsacimv1.data.entity

data class StockAnalysis(
    val analiz_tipi: String,
    val analiz_tarihi: String,
    val hisseler: List<Hisse>
)

data class Hisse(
    val hisse_adi: String,
    val guncel_fiyat: Double,
    val haftalik_tahmini_satis: Double,
    val haftalik_ortalama_satis: Double
)
