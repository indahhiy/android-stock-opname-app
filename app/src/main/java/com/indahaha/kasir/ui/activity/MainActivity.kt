package com.indahaha.kasir.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.indahaha.kasir.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Dashboard
        binding.btnDashboard.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        // Traffic
        binding.btnTraffic.setOnClickListener {
            startActivity(Intent(this, TrafficActivity::class.java))
        }

        // Stock Opname
        binding.btnStockOpname.setOnClickListener {
            startActivity(Intent(this, StockOpnameActivity::class.java))
        }

        // Master Data
        binding.btnMaster.setOnClickListener {
            startActivity(Intent(this, MasterActivity::class.java))
        }

        // Report
        binding.btnReport.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        // Advanced Report (kalau ada)
        binding.btnAdvancedReport.setOnClickListener {
            startActivity(Intent(this, AdvancedReportActivity::class.java))
        }

    }
}
