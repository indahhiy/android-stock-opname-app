package com.indahaha.kasir.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.indahaha.kasir.R
import com.indahaha.kasir.data.entities.MasterKategori

class KategoriAdapter(
    private val onItemClick: (MasterKategori) -> Unit
) : ListAdapter<MasterKategori, KategoriAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kategori, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onItemClick: (MasterKategori) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvNamaKategori: TextView = itemView.findViewById(R.id.tvNamaKategori)

        fun bind(kategori: MasterKategori) {
            tvNamaKategori.text = kategori.namaKategori
            itemView.setOnClickListener {
                onItemClick(kategori)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MasterKategori>() {
        override fun areItemsTheSame(oldItem: MasterKategori, newItem: MasterKategori) =
            oldItem.idKategori == newItem.idKategori

        override fun areContentsTheSame(oldItem: MasterKategori, newItem: MasterKategori) =
            oldItem == newItem
    }
}
