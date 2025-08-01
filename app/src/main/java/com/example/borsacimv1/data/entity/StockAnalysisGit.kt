package com.example.borsacimv1.data.entity

data class StockAnalysisGit(
    val analiz_tarihi: String,
    val analiz_tipi: String,
    val analiz_donemi: String? = null,
    val hisseler: List<StockGit>,
    val genel_analiz: String? = null,
    val sektorel_analiz: String? = null,
    val makroekonomik_etkiler: String? = null
)

data class StockGit(
    val hisse_adi: String,
    val guncel_fiyat: Double,
    val ortalama_satis_fiyati: Double? = null,
    val tahmini_satis_fiyati: Double? = null,
    val risk_seviyesi: String? = null
)
