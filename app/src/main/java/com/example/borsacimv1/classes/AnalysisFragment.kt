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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class AnalysisFragment : Fragment() {

    private val API_KEY = "d268q9pr01qh25lmbgvgd268q9pr01qh25lmbh00" // Kendi API anahtarını buraya koy

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var stockDetailContainer: View
    private lateinit var stockName: TextView
    private lateinit var stockPrice: TextView
    private lateinit var stockChange: TextView
    private lateinit var stockHigh: TextView
    private lateinit var stockLow: TextView

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

}
