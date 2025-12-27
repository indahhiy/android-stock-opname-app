package com.indahaha.kasir.ui.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.indahaha.kasir.R
import com.indahaha.kasir.data.AppDatabase
import com.indahaha.kasir.data.entities.MasterBarang
import com.indahaha.kasir.data.entities.MasterKategori
import com.indahaha.kasir.data.repository.MasterRepository
import com.indahaha.kasir.utils.DateUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AdvancedReportActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var masterRepository: MasterRepository

    private lateinit var spinnerKategori: Spinner
    private lateinit var spinnerBarang: Spinner
    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var btnSearch: Button
    private lateinit var scrollResult: ScrollView
    private lateinit var tvResult: TextView

    private var startDate: Long = DateUtils.getStartOfDay(Date().time)
    private var endDate: Long = DateUtils.getEndOfDay(Date().time)

    // ✅ FIX: Simpan list kategori dan barang
    private var kategoriList: List<MasterKategori> = emptyList()
    private var barangList: List<MasterBarang> = emptyList()
    private var selectedIdKategori: Int = 0
    private var selectedIdBarang: Int = 0

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
        maximumFractionDigits = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_report)

        database = AppDatabase.getDatabase(this@AdvancedReportActivity)
        masterRepository = MasterRepository(database.masterKategoriDao(), database.masterBarangDao())

        initializeViews()
        setupListeners()
        loadKategori()
        updateDateDisplay()
    }

    private fun initializeViews() {
        spinnerKategori = findViewById(R.id.spinner_kategori)
        spinnerBarang = findViewById(R.id.spinner_barang)
        btnStartDate = findViewById(R.id.btn_start_date)
        btnEndDate = findViewById(R.id.btn_end_date)
        tvStartDate = findViewById(R.id.tv_start_date)
        tvEndDate = findViewById(R.id.tv_end_date)
        btnSearch = findViewById(R.id.btn_search)
        scrollResult = findViewById(R.id.scroll_result)
        tvResult = findViewById(R.id.tv_result)
    }

    private fun setupListeners() {
        btnStartDate.setOnClickListener { showDatePicker(true) }
        btnEndDate.setOnClickListener { showDatePicker(false) }
        btnSearch.setOnClickListener { searchItemReport() }

        // ✅ FIX: Ambil ID dari list berdasarkan position
        spinnerKategori.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (kategoriList.isNotEmpty() && position < kategoriList.size) {
                    selectedIdKategori = kategoriList[position].idKategori
                    loadBarangByKategori(selectedIdKategori)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // ✅ FIX: Ambil ID dari list berdasarkan position
        spinnerBarang.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (barangList.isNotEmpty() && position < barangList.size) {
                    selectedIdBarang = barangList[position].idBarang
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadKategori() {
        lifecycleScope.launch {
            try {
                // ✅ Simpan ke variable class
                kategoriList = masterRepository.getAllKategori().first()

                if (kategoriList.isEmpty()) {
                    Toast.makeText(this@AdvancedReportActivity, "Belum ada kategori", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val adapter = ArrayAdapter(
                    this@AdvancedReportActivity,
                    android.R.layout.simple_spinner_item,
                    kategoriList.map { it.namaKategori }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerKategori.adapter = adapter

                // ✅ Trigger load barang untuk kategori pertama
                if (kategoriList.isNotEmpty()) {
                    selectedIdKategori = kategoriList[0].idKategori
                    loadBarangByKategori(selectedIdKategori)
                }

            } catch (e: Exception) {
                Toast.makeText(this@AdvancedReportActivity, "Gagal load kategori: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun loadBarangByKategori(idKategori: Int) {
        lifecycleScope.launch {
            try {
                // ✅ Simpan ke variable class
                barangList = masterRepository.getBarangByKategori(idKategori)

                if (barangList.isEmpty()) {
                    Toast.makeText(this@AdvancedReportActivity, "Tidak ada barang di kategori ini", Toast.LENGTH_SHORT).show()
                    spinnerBarang.adapter = null
                    return@launch
                }

                val adapter = ArrayAdapter(
                    this@AdvancedReportActivity,
                    android.R.layout.simple_spinner_item,
                    barangList.map { it.namaBarang }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerBarang.adapter = adapter

                // ✅ Set selected ID untuk barang pertama
                if (barangList.isNotEmpty()) {
                    selectedIdBarang = barangList[0].idBarang
                }

            } catch (e: Exception) {
                Toast.makeText(this@AdvancedReportActivity, "Gagal load barang: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = if (isStartDate) startDate else endDate

        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                if (isStartDate) {
                    startDate = DateUtils.getStartOfDay(calendar.timeInMillis)
                } else {
                    endDate = DateUtils.getEndOfDay(calendar.timeInMillis)
                }
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        tvStartDate.text = DateUtils.formatDate(startDate)
        tvEndDate.text = DateUtils.formatDate(endDate)
    }

    private fun searchItemReport() {
        if (selectedIdBarang == 0) {
            Toast.makeText(this, "Pilih barang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val summary = database.itemReportDao().getItemSummary(selectedIdBarang, startDate, endDate)

                if (summary == null) {
                    tvResult.text = "Tidak ada data pembelian untuk barang ini dalam periode tersebut."
                    scrollResult.visibility = View.VISIBLE
                    return@launch
                }

                val details = database.itemReportDao().getItemDetails(selectedIdBarang, startDate, endDate)

                val resultText = buildString {
                    appendLine("📊 LAPORAN PEMBELIAN BARANG")
                    appendLine()
                    appendLine("Periode: ${DateUtils.formatDate(startDate)} - ${DateUtils.formatDate(endDate)}")
                    appendLine()
                    appendLine("═══════════════════════════════════════════════════════")
                    appendLine("📦 BARANG: ${summary.namaBarang.uppercase()}")
                    appendLine("📂 KATEGORI: ${summary.namaKategori}")
                    appendLine("═══════════════════════════════════════════════════════")
                    appendLine()
                    appendLine("📈 RINGKASAN:")
                    appendLine("─────────────────────────────────────────────────────")
                    appendLine("Total Pembelian: ${summary.totalQty} ${summary.satuan}")
                    appendLine("Total Harga: ${currencyFormat.format(summary.totalHarga)}")
                    val hargaPerUnit = if (summary.totalQty > 0) summary.totalHarga / summary.totalQty else 0.0
                    appendLine("Harga per ${summary.satuan}: ${currencyFormat.format(hargaPerUnit)}")
                    appendLine("Jumlah Transaksi: ${summary.totalTransaksi}x")
                    appendLine()
                    appendLine("📋 DETAIL TRANSAKSI (per tanggal):")
                    appendLine("─────────────────────────────────────────────────────")
                    details.forEach { detail ->
                        val tanggal = DateUtils.formatDate(detail.tanggal)
                        val qty = "${detail.qty} ${detail.satuan}"
                        val harga = currencyFormat.format(detail.harga)
                        appendLine("$tanggal │ Qty: $qty │ Harga: $harga")
                    }
                    appendLine("─────────────────────────────────────────────────────")
                    appendLine("TOTAL: ${summary.totalQty} ${summary.satuan} | ${currencyFormat.format(summary.totalHarga)}")
                }

                tvResult.text = resultText
                scrollResult.visibility = View.VISIBLE

            } catch (e: Exception) {
                Toast.makeText(this@AdvancedReportActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }
}
