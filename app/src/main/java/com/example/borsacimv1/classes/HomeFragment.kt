package com.example.borsacimv1.classes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.borsacimv1.R
import com.example.borsacimv1.adapter.StockAdapter
import com.example.borsacimv1.data.toStockItem
import com.example.borsacimv1.retro.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var btnTopGain: Button
    private lateinit var btnTopLos: Button
    private lateinit var btnTopPop: Button
    private lateinit var btnTopFav: Button

    private lateinit var buttonList: List<Button>

    private lateinit var stockRecyclerView: RecyclerView
    private lateinit var stockAdapter: StockAdapter

    private val apiKey = "d24h24hr01qu2jgi8n5gd24h24hr01qu2jgi8n60"

    // Örnek sembol listeleri (bunları istediğin gibi değiştirebilirsin)
    private val topGainersSymbols = listOf("AAPL", "NVDA", "MSFT", "AMZN", "META")
    private val topLosersSymbols = listOf("TSLA", "NFLX", "BABA", "INTC", "PYPL")
    private val popularSymbols = listOf("GOOGL", "AMD", "UBER", "DIS", "SONY")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Butonları tanımla
        btnTopGain = view.findViewById(R.id.btnTopGainers)
        btnTopLos = view.findViewById(R.id.btnTopLosers)
        btnTopPop = view.findViewById(R.id.btnPopular)
        btnTopFav = view.findViewById(R.id.btnFav)

        buttonList = listOf(btnTopGain, btnTopLos, btnTopPop, btnTopFav)

        // RecyclerView ve Adapter
        stockRecyclerView = view.findViewById(R.id.stockRecyclerView)
        stockRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        stockAdapter = StockAdapter(emptyList())
        stockRecyclerView.adapter = stockAdapter

        // Varsayılan seçim
        selectButton(btnTopGain)
        loadStocks(topGainersSymbols)

        // Buton click
        buttonList.forEach { button ->
            button.setOnClickListener {
                selectButton(button)

                when (button.id) {
                    R.id.btnTopGainers -> loadStocks(topGainersSymbols)
                    R.id.btnTopLosers -> loadStocks(topLosersSymbols)
                    R.id.btnPopular -> loadStocks(popularSymbols)
                    R.id.btnFav -> loadFavorites()
                }
            }
        }

        // Arama
        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean = true
        })

        return view
    }

    private fun selectButton(selectedButton: Button) {
        buttonList.forEach { it.isSelected = false }
        selectedButton.isSelected = true
    }

    private fun loadStocks(symbols: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val stockList = symbols.map { symbol ->
                    async {
                        val response = RetrofitInstance.api.getQuote(symbol, apiKey)
                        response.toStockItem(symbol)
                    }
                }.awaitAll()

                withContext(Dispatchers.Main) {
                    stockAdapter.updateList(stockList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadFavorites() {
        // Şu anlık boş favori listesi
        stockAdapter.updateList(listOf())
    }
}
