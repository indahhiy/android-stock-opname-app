package com.indahaha.kasir.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.indahaha.kasir.R
import com.indahaha.kasir.data.entities.TrafficMasukDetail
import com.indahaha.kasir.utils.DateUtils
import java.text.NumberFormat
import java.util.*

class TrafficMasukAdapter(
    private val onItemClick: (TrafficMasukDetail) -> Unit,
    private val onDeleteClick: (TrafficMasukDetail) -> Unit
) : ListAdapter<TrafficMasukDetail, TrafficMasukAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_traffic_masuk, parent, false)
        return ViewHolder(view, onItemClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onItemClick: (TrafficMasukDetail) -> Unit,
        private val onDeleteClick: (TrafficMasukDetail) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvNamaBarang: TextView = itemView.findViewById(R.id.tvNamaBarang)
        private val tvDetails: TextView = itemView.findViewById(R.id.tvDetails)
        private val tvHarga: TextView = itemView.findViewById(R.id.tvHarga)
        private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        fun bind(item: TrafficMasukDetail) {
            tvNamaBarang.text = item.namaBarang
            tvDetails.text = "Qty: ${item.qty} ${item.satuan} | ${DateUtils.formatDate(item.tanggal)}"
            tvHarga.text = currencyFormat.format(item.harga)

            // Click untuk edit
            itemView.setOnClickListener {
                onItemClick(item)
            }

            // Long click untuk hapus
            itemView.setOnLongClickListener {
                onDeleteClick(item)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TrafficMasukDetail>() {
        override fun areItemsTheSame(oldItem: TrafficMasukDetail, newItem: TrafficMasukDetail) =
            oldItem.idTrafficMasuk == newItem.idTrafficMasuk

        override fun areContentsTheSame(oldItem: TrafficMasukDetail, newItem: TrafficMasukDetail) =
            oldItem == newItem
    }
}
