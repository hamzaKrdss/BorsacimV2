package com.example.borsacimv1.classes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.borsacimv1.data.dao.StockSearchResult

class StockAdapter(
    private val stocks: List<StockSearchResult>,
    private val onItemClick: (StockSearchResult) -> Unit
) : RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    inner class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stockName: TextView = itemView.findViewById(android.R.id.text1)
        init {
            itemView.setOnClickListener {
                onItemClick(stocks[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return StockViewHolder(view)
    }

    override fun getItemCount() = stocks.size

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.stockName.text = "${stocks[position].symbol} - ${stocks[position].description}"
    }
}
