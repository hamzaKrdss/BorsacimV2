package com.example.borsacimv1.classes

import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.borsacimv1.R
import okhttp3.*
import com.google.gson.Gson
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import com.example.borsacimv1.data.entity.StockAnalysisGit



class HomeFragment : Fragment() {

    private lateinit var dailyShare: Button
    private lateinit var weekShare: Button
    private lateinit var monthShare: Button
    private lateinit var shareTask: TextView

    private lateinit var stockNameList: List<TextView>
    private lateinit var stockCurrentList: List<TextView>
    private lateinit var stockPredictedList: List<TextView>
    private lateinit var stockAverageList: List<TextView>

    private lateinit var buttonList: List<Button>

    private val client = OkHttpClient()
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        dailyShare = view.findViewById(R.id.dailShare)
        weekShare = view.findViewById(R.id.weekShare)
        monthShare = view.findViewById(R.id.monthShare)
        shareTask = view.findViewById(R.id.ShareTask)

        buttonList = listOf(dailyShare, weekShare, monthShare)

        stockNameList = (1..10).map { i ->
            view.findViewById(resources.getIdentifier("stock_name_$i", "id", requireContext().packageName))
        }
        stockCurrentList = (1..10).map { i ->
            view.findViewById(resources.getIdentifier("stock_current_$i", "id", requireContext().packageName))
        }
        stockPredictedList = (1..10).map { i ->
            view.findViewById(resources.getIdentifier("stock_predicted_$i", "id", requireContext().packageName))
        }
        stockAverageList = (1..10).map { i ->
            view.findViewById(resources.getIdentifier("stock_average_$i", "id", requireContext().packageName))
        }

        buttonList.forEach { button ->
            button.setOnClickListener {
                selectButton(button)
                val anim = AnimationUtils.loadAnimation(context, R.anim.button_click)
                button.startAnimation(anim)

                when (button) {
                    dailyShare -> loadDailyTask()
                    weekShare -> loadWeekTask()
                    monthShare -> loadMonthTask()
                }
            }
        }

        loadDailyTask()
        selectButton(dailyShare)


        return view
    }

    private fun selectButton(selectedButton: Button) {
        buttonList.forEach { it.isSelected = false }
        selectedButton.isSelected = true
    }

    private fun updateTable(nameList: List<String>, currentList: List<String>, predictedList: List<String>, averageList: List<String>) {
        for (i in 0 until 10) {
            stockNameList[i].text = nameList.getOrElse(i) { "" }
            stockCurrentList[i].text = currentList.getOrElse(i) { "" }
            stockPredictedList[i].text = predictedList.getOrElse(i) { "" }
            stockAverageList[i].text = averageList.getOrElse(i) { "" }
        }
    }



    private fun getFileName(period: String): String {
        val calendar = Calendar.getInstance()
        return when (period) {
            "gunluk" -> {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                "gunluk-${sdf.format(calendar.time)}"
            }
            "haftalik" -> {
                // Haftalık dosya adı sadece yıl ve ay
                val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                "haftalik-${sdf.format(calendar.time)}"
            }
            "aylik" -> {
                val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                "aylik-${sdf.format(calendar.time)}"
            }
            else -> ""
        }
    }



    private fun fetchJsonFromGitHub(fileName: String, onResult: (StockAnalysisGit?) -> Unit) {
        val url = "https://raw.githubusercontent.com/hamzaKrdss/hisseler/main/$fileName.json"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    onResult(null)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { jsonString ->
                        try {
                            val analysis = gson.fromJson(jsonString, StockAnalysisGit::class.java)
                            requireActivity().runOnUiThread {
                                onResult(analysis)
                            }
                        } catch (e: Exception) {
                            requireActivity().runOnUiThread {
                                onResult(null)
                            }
                        }
                    } ?: run {
                        requireActivity().runOnUiThread {
                            onResult(null)
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        onResult(null)
                    }
                }
            }
        })
    }

    private fun updateUIFromAnalysis(analysis: StockAnalysisGit) {
        shareTask.text = "${analysis.analiz_tipi} (${analysis.analiz_tarihi})"

        val nameList = analysis.hisseler.map { it.hisse_adi }
        val currentList = analysis.hisseler.map { "${it.guncel_fiyat} ₺" }
        val predictedList = analysis.hisseler.map { it.tahmini_satis_fiyati?.let { v -> "$v ₺" } ?: "-" }
        val averageList = analysis.hisseler.map { it.ortalama_satis_fiyati?.let { v -> "$v ₺" } ?: "-" }

        updateTable(nameList, currentList, predictedList, averageList)
    }



    private fun loadDailyTask() {
        val fileName = getFileName("gunluk")
        fetchJsonFromGitHub(fileName) { analysis ->
            if (analysis != null) {
                updateUIFromAnalysis(analysis)
            } else {
                shareTask.text = "Günlük veri alınamadı."
                updateTable(emptyList(), emptyList(), emptyList(), emptyList())
            }
        }
    }


    private fun loadWeekTask() {
        val fileName = getFileName("haftalik")
        fetchJsonFromGitHub(fileName) { analysis ->
            if (analysis != null) {
                updateUIFromAnalysis(analysis)
            } else {
                shareTask.text = "Haftalık veri alınamadı."
                updateTable(emptyList(), emptyList(), emptyList(), emptyList())
            }
        }
    }

    private fun loadMonthTask() {
        val fileName = getFileName("aylik")
        fetchJsonFromGitHub(fileName) { analysis ->
            if (analysis != null) {
                updateUIFromAnalysis(analysis)
            } else {
                shareTask.text = "Aylık veri alınamadı."
                updateTable(emptyList(), emptyList(), emptyList(), emptyList())
            }
        }
    }
}
