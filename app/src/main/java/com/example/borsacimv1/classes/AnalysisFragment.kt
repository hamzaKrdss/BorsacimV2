package com.example.borsacimv1.classes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
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

    private val API_KEY = "d268q9pr01qh25lmbgvgd268q9pr01qh25lmbh00" // Buraya kendi anahtarını koy

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var stockDetailContainer: View
    private lateinit var stockName: TextView
    private lateinit var stockPrice: TextView

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

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Search işlemi
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
                requireActivity().runOnUiThread {
                    stockDetailContainer.visibility = View.VISIBLE
                    stockName.text = "$symbol - $name"
                    stockPrice.text = "Fiyat: ${quote.c} $"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                stockName.text = "$symbol - $name"
                stockPrice.text = "Fiyat alınamadı"
                stockDetailContainer.visibility = View.VISIBLE
            }
        }
    }
}
