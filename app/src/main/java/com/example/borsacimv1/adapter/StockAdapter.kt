package com.example.borsacimv1.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.borsacimv1.R
import com.example.borsacimv1.data.StockItem

class StockAdapter(
    private var stockList: List<StockItem>
) : RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    inner class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val symbolTv: TextView = itemView.findViewById(R.id.tvSymbol)
        val priceTv: TextView = itemView.findViewById(R.id.tvPrice)
        val changeTv: TextView = itemView.findViewById(R.id.tvChange)
        // İstersen diğer TextView'leri ekle
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stock, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val stock = stockList[position]
        holder.symbolTv.text = stock.symbol
        holder.priceTv.text = "Fiyat: ${stock.currentPrice}"
        holder.changeTv.text = "%${stock.percentChange}"
        // İstersen diğer alanları da göster
    }

    override fun getItemCount(): Int = stockList.size

    fun updateList(newList: List<StockItem>) {
        stockList = newList
        notifyDataSetChanged()
    }
}
