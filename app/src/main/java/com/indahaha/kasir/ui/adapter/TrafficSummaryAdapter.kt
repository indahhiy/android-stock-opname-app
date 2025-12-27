package com.indahaha.kasir.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.indahaha.kasir.R

class TrafficSummaryAdapter(
    private val items: List<TrafficSummaryItem>
) : RecyclerView.Adapter<TrafficSummaryAdapter.ViewHolder>() {

    data class TrafficSummaryItem(
        val namaBarang: String,
        val qty: Double,
        val satuan: String
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNamaBarang: TextView = view.findViewById(R.id.tv_nama_barang)
        val tvQty: TextView = view.findViewById(R.id.tv_qty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_traffic_summary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvNamaBarang.text = item.namaBarang
        holder.tvQty.text = "${item.qty.toInt()} ${item.satuan}"
    }

    override fun getItemCount() = items.size
}
