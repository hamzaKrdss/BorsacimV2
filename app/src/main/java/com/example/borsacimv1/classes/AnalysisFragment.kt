package com.example.borsacimv1.classes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.example.borsacimv1.R
import com.example.borsacimv1.classes.adapter.StockAdapter
import com.example.borsacimv1.data.objects.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AnalysisFragment : Fragment() {

    private val API_KEY = "BURAYA_FINNHUB_API_KEYİNİ_YAZ"

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var stockDetailContainer: View
    private lateinit var stockName: TextView
    private lateinit var stockPrice: TextView

    private var searchJob: Job? = null
    private var adapter: StockAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analysis, container, false)

        searchView = view.findViewById(R.id.searchView)
        recyclerView = view.findViewById(R.id.stockRecyclerView)
        stockDetailContainer = view.findViewById(R.id.stockDetailContainer)
        stockName = view.findViewById(R.id.stockName)
        stockPrice = view.findViewById(R.id.stockPrice)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()
                if (newText.isNullOrBlank()) {
                    recyclerView.visibility = View.GONE
                    stockDetailContainer.visibility = View.GONE
                    return true
                }
                // Debounce arama (yarım saniye bekle)
                searchJob = lifecycleScope.launch {
                    delay(500)
                    searchStocks(newText)
                }
                return true
            }
        })

        return view
    }

    private suspend fun searchStocks(query: String) {
        try {
            val response = RetrofitClient.api.searchSymbols(query, API_KEY)
            val results = response.result
            requireActivity().runOnUiThread {
                if (results.isEmpty()) {
                    recyclerView.visibility = View.GONE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    adapter = StockAdapter(results) { stock ->
                        fetchStockPrice(stock.symbol, stock.description)
                        recyclerView.visibility = View.GONE
                    }
                    recyclerView.adapter = adapter
                }
                stockDetailContainer.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fetchStockPrice(symbol: String, name: String) {
        lifecycleScope.launch {
            try {
                val quote = RetrofitClient.api.getQuote(symbol, API_KEY)
                requireActivity().runOnUiThread {
                    stockDetailContainer.visibility = View.VISIBLE
                    stockName.text = "$symbol - $name"
                    stockPrice.text = "Fiyat: ${quote.c} ₺"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

