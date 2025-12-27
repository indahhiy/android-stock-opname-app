package com.indahaha.kasir.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.indahaha.kasir.data.AppDatabase
import com.indahaha.kasir.data.repository.MasterRepository
import com.indahaha.kasir.data.repository.TrafficStockRepository
import com.indahaha.kasir.databinding.ActivityDashboardBinding
import com.indahaha.kasir.ui.adapter.TrafficSummaryAdapter
import com.indahaha.kasir.ui.viewmodel.TrafficStockViewModel
import com.indahaha.kasir.ui.viewmodel.ViewModelFactory
import com.indahaha.kasir.utils.DateUtils
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var viewModel: TrafficStockViewModel
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)
        setupViewModel()
        setupCurrentDate()
        setupRecyclerViews()
        observeData()
        loadData()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this@DashboardActivity)
        val masterRepository = MasterRepository(
            database.masterKategoriDao(),
            database.masterBarangDao()
        )
        val trafficStockRepository = TrafficStockRepository(
            database.trafficDao(),
            database.stockOpnameDao(),
            database.masterBarangDao()
        )
        val factory = ViewModelFactory(masterRepository, trafficStockRepository)
        viewModel = ViewModelProvider(this, factory)[TrafficStockViewModel::class.java]
    }

    private fun setupCurrentDate() {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        val currentDate = dateFormat.format(Date())
        binding.tvCurrentDate.text = currentDate
    }

    private fun setupRecyclerViews() {
        binding.rvTrafficMasuk.layoutManager = LinearLayoutManager(this)
        binding.rvTrafficKeluar.layoutManager = LinearLayoutManager(this)
    }

    private fun observeData() {
        // ✅ Observe financial summary
        viewModel.dashboardSummary.observe(this) { summary ->
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvPengeluaranHariIni.text = formatter.format(summary.totalPengeluaranHariIni)
        }

        // ✅ Observe stock alerts
        viewModel.stockAlerts.observe(this) { alerts ->
            if (alerts.isEmpty()) {
                binding.tvStockAlert.text = "✅ Semua stok aman! Tidak ada barang yang perlu di-restock."
            } else {
                val alertText = buildString {
                    append("${alerts.size} item perlu perhatian:\n")
                    alerts.take(3).forEach { alert ->
                        append("• ${alert.namaBarang}: ${alert.stokSaatIni.toInt()} (min: ${alert.stokMinimum.toInt()})\n")
                    }
                    if (alerts.size > 3) {
                        append("...dan ${alerts.size - 3} item lainnya")
                    }
                }
                binding.tvStockAlert.text = alertText
            }
        }
    }

    private fun loadData() {
        // ✅ FIX: Pakai Date().time (sama dengan ReportActivity)
        val today = Date().time
        val startOfDay = DateUtils.getStartOfDay(today)
        val endOfDay = DateUtils.getEndOfDay(today)

        viewModel.loadDashboardSummary(today)
        viewModel.loadStockAlerts()
        loadTrafficDetails(startOfDay, endOfDay)
    }

    private fun loadTrafficDetails(startOfDay: Long, endOfDay: Long) {
        lifecycleScope.launch {
            try {
                // ✅ Get traffic masuk detail
                val trafficMasukList = db.trafficDao().getTopTrafficMasukToday(startOfDay, endOfDay)
                val masukItems = trafficMasukList.map {
                    TrafficSummaryAdapter.TrafficSummaryItem(
                        it.namaBarang,
                        it.qty,
                        it.satuan
                    )
                }

                if (masukItems.isEmpty()) {
                    binding.rvTrafficMasuk.adapter = TrafficSummaryAdapter(
                        listOf(TrafficSummaryAdapter.TrafficSummaryItem("Belum ada data", 0.0, ""))
                    )
                } else {
                    binding.rvTrafficMasuk.adapter = TrafficSummaryAdapter(masukItems)
                }

                // ✅ Get traffic keluar detail
                val trafficKeluarList = db.trafficDao().getTopTrafficKeluarToday(startOfDay, endOfDay)
                val keluarItems = trafficKeluarList.map {
                    TrafficSummaryAdapter.TrafficSummaryItem(
                        it.namaBarang,
                        it.qty,
                        it.satuan
                    )
                }

                if (keluarItems.isEmpty()) {
                    binding.rvTrafficKeluar.adapter = TrafficSummaryAdapter(
                        listOf(TrafficSummaryAdapter.TrafficSummaryItem("Belum ada data", 0.0, ""))
                    )
                } else {
                    binding.rvTrafficKeluar.adapter = TrafficSummaryAdapter(keluarItems)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}
