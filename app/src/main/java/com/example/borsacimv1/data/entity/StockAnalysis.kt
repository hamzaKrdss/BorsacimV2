package com.example.borsacimv1.data.entity

data class StockAnalysis(
    val analiz_tarihi: String,
    val analiz_tipi: String,
    val analiz_donemi: String,
    val hisseler: List<Stock>,
    val genel_analiz: String,
    val sektorel_analiz: String
)

data class Stock(
    val hisse_adi: String,
    val guncel_fiyat: Double,
    val haftalik_ortalama_satis: Double,
    val haftalik_tahmini_satis: Double,
    val risk_seviyesi: String
)
