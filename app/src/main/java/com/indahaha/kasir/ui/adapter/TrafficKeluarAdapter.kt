package com.indahaha.kasir.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.indahaha.kasir.R
import com.indahaha.kasir.data.entities.TrafficKeluarDetail
import com.indahaha.kasir.utils.DateUtils

class TrafficKeluarAdapter(
    private val onItemClick: (TrafficKeluarDetail) -> Unit,
    private val onDeleteClick: (TrafficKeluarDetail) -> Unit
) : ListAdapter<TrafficKeluarDetail, TrafficKeluarAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_traffic_keluar, parent, false)
        return ViewHolder(view, onItemClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onItemClick: (TrafficKeluarDetail) -> Unit,
        private val onDeleteClick: (TrafficKeluarDetail) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvNamaBarang: TextView = itemView.findViewById(R.id.tvNamaBarang)
        private val tvDetails: TextView = itemView.findViewById(R.id.tvDetails)

        fun bind(item: TrafficKeluarDetail) {
            tvNamaBarang.text = item.namaBarang
            tvDetails.text = "Qty: ${item.qty} ${item.satuan} | ${DateUtils.formatDate(item.tanggal)}"

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

    class DiffCallback : DiffUtil.ItemCallback<TrafficKeluarDetail>() {
        override fun areItemsTheSame(oldItem: TrafficKeluarDetail, newItem: TrafficKeluarDetail) =
            oldItem.idTrafficKeluar == newItem.idTrafficKeluar

        override fun areContentsTheSame(oldItem: TrafficKeluarDetail, newItem: TrafficKeluarDetail) =
            oldItem == newItem
    }
}
