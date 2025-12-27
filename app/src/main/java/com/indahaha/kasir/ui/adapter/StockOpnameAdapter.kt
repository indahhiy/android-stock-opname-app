package com.indahaha.kasir.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.indahaha.kasir.R
import com.indahaha.kasir.data.entities.StockOpnameDisplay

class StockOpnameAdapter(
    private val onItemClick: (StockOpnameDisplay) -> Unit
) : ListAdapter<StockOpnameDisplay, StockOpnameAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock_opname, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNamaBarang: TextView = itemView.findViewById(R.id.tvNamaBarang)
        private val tvSatuan: TextView = itemView.findViewById(R.id.tvSatuan)
        private val tvStokAwal: TextView = itemView.findViewById(R.id.tvStokAwal)
        private val tvTotalMasuk: TextView = itemView.findViewById(R.id.tvTotalMasuk)
        private val tvTotalKeluar: TextView = itemView.findViewById(R.id.tvTotalKeluar)
        private val tvStokAkhir: TextView = itemView.findViewById(R.id.tvStokAkhir)
        private val tvStokAkhirDetail: TextView = itemView.findViewById(R.id.tvStokAkhirDetail)
        private val tvStokFisik: TextView = itemView.findViewById(R.id.tvStokFisik)
        private val tvSelisih: TextView = itemView.findViewById(R.id.tvSelisih)
        private val tvHint: TextView = itemView.findViewById(R.id.tvHint)
        private val layoutStokFisik: LinearLayout = itemView.findViewById(R.id.layoutStokFisik)


        fun bind(item: StockOpnameDisplay, onItemClick: (StockOpnameDisplay) -> Unit) {
            // Set data utama
            tvNamaBarang.text = item.namaBarang
            tvSatuan.text = item.satuan
            tvStokAwal.text = "${item.stokAwal}"
            tvTotalMasuk.text = "+${item.totalMasukHarian}"
            tvTotalKeluar.text = "-${item.totalKeluarHarian}"
            tvStokAkhir.text = "${item.stokAkhir}"
            tvStokAkhirDetail.text = "${item.stokAkhir}"

            // Show/hide stok fisik section
            if (item.stokFisik != null) {
                layoutStokFisik.visibility = View.VISIBLE
                tvHint.visibility = View.GONE

                // ✅ UPDATE FORMAT: "Fisik: 500.0"
                tvStokFisik.text = "Fisik: ${item.stokFisik}"

                // ✅ UPDATE FORMAT: "Δ 10.0"
                val selisih = item.selisih ?: 0.0
                tvSelisih.text = "Δ $selisih"

                // Set color based on selisih
                when {
                    selisih > 0 -> {
                        tvSelisih.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))
                    }
                    selisih < 0 -> {
                        tvSelisih.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_orange_dark))
                    }
                    else -> {
                        tvSelisih.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))
                    }
                }

            } else {
                layoutStokFisik.visibility = View.GONE
                tvHint.visibility = View.VISIBLE
            }

            // Click listener
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<StockOpnameDisplay>() {
        override fun areItemsTheSame(oldItem: StockOpnameDisplay, newItem: StockOpnameDisplay): Boolean {
            return oldItem.idLedger == newItem.idLedger
        }

        override fun areContentsTheSame(oldItem: StockOpnameDisplay, newItem: StockOpnameDisplay): Boolean {
            return oldItem == newItem
        }
    }
}
