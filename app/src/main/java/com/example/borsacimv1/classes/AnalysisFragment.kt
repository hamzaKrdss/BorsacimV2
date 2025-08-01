package com.example.borsacimv1.classes

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.borsacimv1.R
import com.example.borsacimv1.classes.adapter.StockAdapter
import com.example.borsacimv1.data.objects.RetrofitClient
import com.example.borsacimv1.data.objects.local.GlobalStockList
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AnalysisFragment : Fragment() {

    private val API_KEY = "d268q9pr01qh25lmbgvgd268q9pr01qh25lmbh00" // API anahtarın

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var stockDetailContainer: View
    private lateinit var stockName: TextView
    private lateinit var stockPrice: TextView
    private lateinit var stockChange: TextView
    private lateinit var stockHigh: TextView
    private lateinit var stockLow: TextView

    private lateinit var candleStickChart: CandleStickChart

    private var searchJob: Job? = null
    private var adapter: StockAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_analysis, container, false)

        searchView = view.findViewById(R.id.searchView)
        recyclerView = view.findViewById(R.id.stockRecyclerView)
        stockDetailContainer = view.findViewById(R.id.stockDetailContainer)
        stockName = view.findViewById(R.id.stockName)
        stockPrice = view.findViewById(R.id.stockPrice)
        stockChange = view.findViewById(R.id.stockChange)
        stockHigh = view.findViewById(R.id.stockHigh)
        stockLow = view.findViewById(R.id.stockLow)
        candleStickChart = view.findViewById(R.id.candleStickChart)

        // SearchView içindeki EditText'i bulup yazı rengini ve hint rengini ayarla
        val searchEditText = findSearchEditText(searchView)
        searchEditText?.setTextColor(Color.WHITE)
        searchEditText?.setHintTextColor(Color.LTGRAY)

        // SearchView'i aç, odaklanabilir ve tıklanabilir yap
        searchView.isIconified = false
        searchView.isFocusable = true
        searchView.isFocusableInTouchMode = true
        searchView.isClickable = true
        searchView.requestFocus()

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Search işlemi
        searchEditText?.setTextColor(Color.WHITE)
        searchEditText?.setHintTextColor(Color.LTGRAY)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()
                if (newText.isNullOrBlank()) {
                    recyclerView.visibility = View.GONE
                    stockDetailContainer.visibility = View.GONE
                    return true
                }
                searchJob = lifecycleScope.launch {
                    delay(300) // debounce
                    filterStocksLocally(newText)
                }
                return true
            }
        })

        return view
    }

    private fun findSearchEditText(searchView: SearchView): EditText? {
        // SearchView içindeki EditText'i bulmak için recursive fonksiyon
        for (i in 0 until searchView.childCount) {
            val child = searchView.getChildAt(i)
            if (child is EditText) {
                return child
            }
            if (child is ViewGroup) {
                val editText = findEditTextInViewGroup(child)
                if (editText != null) return editText
            }
        }
        return null
    }

    private fun findEditTextInViewGroup(viewGroup: ViewGroup): EditText? {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is EditText) return child
            if (child is ViewGroup) {
                val editText = findEditTextInViewGroup(child)
                if (editText != null) return editText
            }
        }
        return null
    }

    private fun filterStocksLocally(query: String) {
        val filteredList = GlobalStockList.stocks.filter {
            it.symbol.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }

        if (filteredList.isEmpty()) {
            recyclerView.visibility = View.GONE
            stockDetailContainer.visibility = View.GONE
        } else {
            adapter = StockAdapter(filteredList) { stock ->
                fetchStockPrice(stock.symbol, stock.description)
                recyclerView.visibility = View.GONE
            }
            recyclerView.adapter = adapter
            recyclerView.visibility = View.VISIBLE
            stockDetailContainer.visibility = View.GONE
        }
    }

    private fun fetchStockPrice(symbol: String, name: String) {
        lifecycleScope.launch {
            try {
                val quote = RetrofitClient.api.getQuote(symbol, API_KEY)

                // Hesaplamalar
                val change = quote.c - quote.pc
                val changePercent = if (quote.pc != 0.0) (change / quote.pc) * 100 else 0.0
                val changeText = String.format("%.2f (%.2f%%)", change, changePercent)

                requireActivity().runOnUiThread {
                    stockDetailContainer.visibility = View.VISIBLE
                    stockName.text = "$symbol - $name"
                    stockPrice.text = "Fiyat: ${quote.c} $"
                    stockChange.text = "Değişim: $changeText"
                    stockHigh.text = "Günlük Yüksek: ${quote.h}"
                    stockLow.text = "Günlük Düşük: ${quote.l}"

                    // Değişim pozitifse yeşil, negatifse kırmızı yap
                    stockChange.setTextColor(
                        if (change >= 0) Color.GREEN else Color.RED
                    )

                    // Dummy mum grafiğini göster (sonraki adımda gerçek veriye geçilecek)
                    showDummyCandleChart()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    stockDetailContainer.visibility = View.VISIBLE
                    stockName.text = "$symbol - $name"
                    stockPrice.text = "Fiyat alınamadı"
                    stockChange.text = ""
                    stockHigh.text = ""
                    stockLow.text = ""
                }
            }
        }
    }

    // Örnek mum grafiği verisi
    private fun showDummyCandleChart() {
        val entries = listOf(
            CandleEntry(0f, 130f, 120f, 125f, 123f),
            CandleEntry(1f, 132f, 121f, 131f, 128f),
            CandleEntry(2f, 135f, 130f, 134f, 132f),
            CandleEntry(3f, 133f, 127f, 130f, 129f),
            CandleEntry(4f, 137f, 131f, 136f, 134f),
            CandleEntry(5f, 139f, 133f, 138f, 135f),
            CandleEntry(6f, 140f, 134f, 139f, 137f)
        )

        val dataSet = CandleDataSet(entries, "Mum Grafiği")
        dataSet.color = Color.rgb(80, 80, 80)
        dataSet.shadowColor = Color.DKGRAY
        dataSet.shadowWidth = 1f
        dataSet.decreasingColor = Color.RED
        dataSet.decreasingPaintStyle = android.graphics.Paint.Style.FILL
        dataSet.increasingColor = Color.GREEN
        dataSet.increasingPaintStyle = android.graphics.Paint.Style.FILL
        dataSet.neutralColor = Color.BLUE
        dataSet.setDrawValues(false)

        val data = CandleData(dataSet)
        candleStickChart.data = data

        // Grafik ayarları
        candleStickChart.axisRight.isEnabled = false
        candleStickChart.description.isEnabled = false
        candleStickChart.setBackgroundColor(Color.TRANSPARENT)

        val xAxis = candleStickChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        val yAxisLeft = candleStickChart.axisLeft
        yAxisLeft.setDrawGridLines(false)

        candleStickChart.invalidate() // Grafiği yenile
    }
}
