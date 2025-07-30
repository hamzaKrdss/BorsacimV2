package com.example.borsacimv1.classes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.borsacimv1.R
import com.example.borsacimv1.adapter.StockAdapter
import com.example.borsacimv1.data.StockItem
import com.example.borsacimv1.data.SymbolResponse
import com.example.borsacimv1.data.toStockItem
import com.example.borsacimv1.retro.RetrofitInstance
import kotlinx.coroutines.*

class HomeFragment : Fragment() {

    private lateinit var btnTopGain: Button
    private lateinit var btnTopLos: Button
    private lateinit var btnTopPop: Button
    private lateinit var btnTopFav: Button

    private lateinit var buttonList: List<Button>

    private lateinit var stockRecyclerView: RecyclerView
    private lateinit var stockAdapter: StockAdapter

    private val apiKey = "d24h24hr01qu2jgi8n5gd24h24hr01qu2jgi8n60"

    private var allStockList: List<StockItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        btnTopGain = view.findViewById(R.id.btnTopGainers)
        btnTopLos = view.findViewById(R.id.btnTopLosers)
        btnTopPop = view.findViewById(R.id.btnPopular)
        btnTopFav = view.findViewById(R.id.btnFav)

        buttonList = listOf(btnTopGain, btnTopLos, btnTopPop, btnTopFav)
        val buttons = listOf(btnTopGain, btnTopLos, btnTopPop, btnTopFav)



        stockRecyclerView = view.findViewById(R.id.stockRecyclerView)
        stockRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        stockAdapter = StockAdapter(emptyList())
        stockRecyclerView.adapter = stockAdapter
        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                filterStocks(newText)
                return true
            }
        })

        searchView.clearFocus()

        selectButton(btnTopGain)
        loadAllStocks()

        buttonList.forEach { button ->
            button.setOnClickListener {
                selectButton(button)
                val anim = AnimationUtils.loadAnimation(context, R.anim.button_click)
                button.startAnimation(anim)
                when (button.id) {
                    R.id.btnTopGainers -> loadTopGainers()
                    R.id.btnTopLosers -> loadTopLosers()
                    R.id.btnPopular -> loadPopularStocks()
                    R.id.btnFav -> loadFavorites()
                }
            }
        }




        return view
    }

    private fun selectButton(selectedButton: Button) {
        buttonList.forEach { it.isSelected = false }
        selectedButton.isSelected = true
    }

    private suspend fun fetchQuotesInBatches(symbols: List<String>, batchSize: Int = 5): List<StockItem> {
        val result = mutableListOf<StockItem>()
        for (i in symbols.indices step batchSize) {
            val batch = symbols.subList(i, minOf(i + batchSize, symbols.size))
            val batchStocks = coroutineScope {
                batch.map { symbol ->
                    async(Dispatchers.IO) {
                        val quote = RetrofitInstance.api.getQuote(symbol, apiKey)
                        quote.toStockItem(symbol)
                    }
                }.awaitAll()
            }
            result.addAll(batchStocks)
            delay(2000) // 2 saniye bekle (istek hızını düşürmek için)
        }
        return result
    }


    private fun loadAllStocks() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Tüm sembolleri çekiyoruz
                val symbolsResponse: List<SymbolResponse> = RetrofitInstance.api.getAllSymbols(token = apiKey)
                val symbols = symbolsResponse.map { it.symbol }.take(100)  // ilk 100 sembol

                val stockList = fetchQuotesInBatches(symbols)
                allStockList = stockList
                stockAdapter.updateList(stockList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // En çok yükselen 10 hisseyi getirir
    private fun loadTopGainers() {
        val topGainers = allStockList
            .filter { it.percentChange != null }
            .sortedByDescending { it.percentChange }
            .take(10)
        stockAdapter.updateList(topGainers)
    }

    // En çok düşen 10 hisseyi getirir
    private fun loadTopLosers() {
        val topLosers = allStockList
            .filter { it.percentChange != null }
            .sortedBy { it.percentChange }
            .take(10)
        stockAdapter.updateList(topLosers)
    }

    // Popüler 10 hisseyi göster (elle belirlenmiş)
    private fun loadPopularStocks() {
        val popularSymbols = listOf("AAPL", "GOOGL", "AMZN", "MSFT", "TSLA", "META", "NVDA", "NFLX", "BABA", "DIS")
        val popularStocks = allStockList.filter { it.symbol in popularSymbols }
        stockAdapter.updateList(popularStocks.take(10))
    }

    private fun loadStocks(symbols: List<String>) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val stockList = fetchQuotesInBatches(symbols)
                stockAdapter.updateList(stockList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadFavorites() {
        stockAdapter.updateList(emptyList()) // Şimdilik boş
    }

    private fun filterStocks(query: String?) {
        if (query.isNullOrBlank()) {
            stockAdapter.updateList(allStockList)
            return
        }
        val filtered = allStockList.filter {
            it.symbol.contains(query, ignoreCase = true)
        }
        stockAdapter.updateList(filtered)
    }
}
