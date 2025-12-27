package com.indahaha.kasir.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.indahaha.kasir.R
import com.indahaha.kasir.data.entities.MasterBarang

class BarangAdapter(
    private val onItemClick: (MasterBarang) -> Unit
) : ListAdapter<MasterBarang, BarangAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_barang, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onItemClick: (MasterBarang) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvNamaBarang: TextView = itemView.findViewById(R.id.tvNamaBarang)
        private val tvSatuan: TextView = itemView.findViewById(R.id.tvSatuan)

        fun bind(barang: MasterBarang) {
            tvNamaBarang.text = barang.namaBarang
            tvSatuan.text = barang.satuan
            itemView.setOnClickListener {
                onItemClick(barang)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MasterBarang>() {
        override fun areItemsTheSame(oldItem: MasterBarang, newItem: MasterBarang) =
            oldItem.idBarang == newItem.idBarang

        override fun areContentsTheSame(oldItem: MasterBarang, newItem: MasterBarang) =
            oldItem == newItem
    }
}
