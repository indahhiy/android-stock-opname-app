package com.indahaha.kasir.ui.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.indahaha.kasir.R
import com.indahaha.kasir.data.AppDatabase
import com.indahaha.kasir.data.entities.MasterBarang
import com.indahaha.kasir.data.entities.MasterKategori
import com.indahaha.kasir.data.entities.TrafficKeluar
import com.indahaha.kasir.data.entities.TrafficMasuk
import com.indahaha.kasir.data.repository.MasterRepository
import com.indahaha.kasir.data.repository.TrafficStockRepository
import com.indahaha.kasir.ui.adapter.TrafficKeluarAdapter
import com.indahaha.kasir.ui.adapter.TrafficMasukAdapter
import com.indahaha.kasir.ui.viewmodel.TrafficStockViewModel
import com.indahaha.kasir.ui.viewmodel.ViewModelFactory
import com.indahaha.kasir.utils.DateUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class TrafficActivity : AppCompatActivity() {

    private lateinit var viewModel: TrafficStockViewModel
    private lateinit var masterRepository: MasterRepository
    private lateinit var adapterMasuk: TrafficMasukAdapter
    private lateinit var adapterKeluar: TrafficKeluarAdapter
    private lateinit var viewMasuk: LinearLayout
    private lateinit var viewKeluar: LinearLayout
    private lateinit var tvSelectedDate: TextView
    private lateinit var btnToday: Button

    private var barangList: List<MasterBarang> = emptyList()
    private var kategoriList: List<MasterKategori> = emptyList()

    // ✅ Selected date untuk filter
    private var selectedDate: Long = DateUtils.getCurrentDateTime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traffic)

        setupViewModel()
        setupAdapters()
        setupViews()
        setupTabs()
        setupDateFilter()
        loadMasterData()
        loadDataByDate()
    }

    private fun setupViewModel() {
        val db = AppDatabase.getDatabase(this)
        masterRepository = MasterRepository(
            db.masterKategoriDao(),
            db.masterBarangDao()
        )
        val trafficRepo = TrafficStockRepository(
            db.trafficDao(),
            db.stockOpnameDao(),
            db.masterBarangDao()
        )
        val factory = ViewModelFactory(masterRepository, trafficRepo)
        viewModel = ViewModelProvider(this, factory)[TrafficStockViewModel::class.java]
    }

    private fun setupAdapters() {
        adapterMasuk = TrafficMasukAdapter(
            onItemClick = { item ->
                showEditTrafficMasukDialog(item)
            },
            onDeleteClick = { item ->
                showDeleteConfirmationDialog(
                    itemName = item.namaBarang,
                    onConfirm = {
                        val trafficMasuk = TrafficMasuk(
                            idTrafficMasuk = item.idTrafficMasuk,
                            tanggal = item.tanggal,
                            idBarang = item.idBarang,
                            qty = item.qty,
                            harga = item.harga
                        )
                        viewModel.deleteTrafficMasuk(trafficMasuk)
                        Toast.makeText(this, "✅ ${item.namaBarang} dihapus", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )

        adapterKeluar = TrafficKeluarAdapter(
            onItemClick = { item ->
                showEditTrafficKeluarDialog(item)
            },
            onDeleteClick = { item ->
                showDeleteConfirmationDialog(
                    itemName = item.namaBarang,
                    onConfirm = {
                        val trafficKeluar = TrafficKeluar(
                            idTrafficKeluar = item.idTrafficKeluar,
                            tanggal = item.tanggal,
                            idBarang = item.idBarang,
                            qty = item.qty,
                            keterangan = item.keterangan
                        )
                        viewModel.deleteTrafficKeluar(trafficKeluar)
                        Toast.makeText(this, "✅ ${item.namaBarang} dihapus", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    private fun setupViews() {
        viewMasuk = findViewById(R.id.viewMasuk)
        viewKeluar = findViewById(R.id.viewKeluar)
        tvSelectedDate = findViewById(R.id.tv_selected_date)
        btnToday = findViewById(R.id.btn_today)

        findViewById<RecyclerView>(R.id.rvTrafficMasuk).apply {
            layoutManager = LinearLayoutManager(this@TrafficActivity)
            adapter = adapterMasuk
        }

        findViewById<RecyclerView>(R.id.rvTrafficKeluar).apply {
            layoutManager = LinearLayoutManager(this@TrafficActivity)
            adapter = adapterKeluar
        }

        findViewById<Button>(R.id.btnAddMasuk).setOnClickListener {
            showAddTrafficMasukDialog()
        }

        findViewById<Button>(R.id.btnAddKeluar).setOnClickListener {
            showAddTrafficKeluarDialog()
        }
    }

    private fun setupTabs() {
        findViewById<TabLayout>(R.id.tabLayout).addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        viewMasuk.visibility = View.VISIBLE
                        viewKeluar.visibility = View.GONE
                    }
                    1 -> {
                        viewMasuk.visibility = View.GONE
                        viewKeluar.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // ✅ SETUP FILTER TANGGAL
    private fun setupDateFilter() {
        tvSelectedDate.text = DateUtils.formatDate(selectedDate)

        tvSelectedDate.setOnClickListener {
            showDatePickerForFilter()
        }

        btnToday.setOnClickListener {
            selectedDate = DateUtils.getCurrentDateTime()
            tvSelectedDate.text = DateUtils.formatDate(selectedDate)
            loadDataByDate()
        }
    }

    private fun showDatePickerForFilter() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.timeInMillis
                tvSelectedDate.text = DateUtils.formatDate(selectedDate)
                loadDataByDate()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // ✅ VALIDASI: Tidak bisa pilih tanggal masa depan
        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun loadDataByDate() {
        val startDate = DateUtils.getStartOfDay(selectedDate)
        val endDate = DateUtils.getEndOfDay(selectedDate)

        viewModel.getTrafficMasukToday(startDate, endDate).observe(this) { list ->
            adapterMasuk.submitList(list)
        }

        viewModel.getTrafficKeluarToday(startDate, endDate).observe(this) { list ->
            adapterKeluar.submitList(list)
        }
    }

    private fun loadMasterData() {
        lifecycleScope.launch {
            kategoriList = masterRepository.getAllKategori().first()
        }

        viewModel.allBarang.observe(this) { list ->
            barangList = list
        }
    }

    private fun showAddTrafficMasukDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_traffic_masuk, null)
        val etTanggal = view.findViewById<TextInputEditText>(R.id.etTanggal)
        val spinnerKategori = view.findViewById<AutoCompleteTextView>(R.id.spinnerKategori)
        val spinnerBarang = view.findViewById<AutoCompleteTextView>(R.id.spinnerBarang)
        val etQty = view.findViewById<TextInputEditText>(R.id.etQty)
        val etHarga = view.findViewById<TextInputEditText>(R.id.etHarga)

        var selectedDateDialog = DateUtils.getCurrentDateTime()
        var filteredBarangList = barangList

        etTanggal.setText(DateUtils.formatDate(selectedDateDialog))
        etTanggal.setOnClickListener {
            showDatePickerDialog(selectedDateDialog) { newDate ->
                selectedDateDialog = newDate
                etTanggal.setText(DateUtils.formatDate(selectedDateDialog))
            }
        }

        val kategoriNames = kategoriList.map { it.namaKategori }
        val kategoriAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, kategoriNames)
        spinnerKategori.setAdapter(kategoriAdapter)

        spinnerKategori.setOnItemClickListener { _, _, position, _ ->
            if (kategoriList.isNotEmpty() && position < kategoriList.size) {
                val selectedKategoriId = kategoriList[position].idKategori
                filteredBarangList = barangList.filter { it.idKategoriDefault == selectedKategoriId }

                val barangNames = filteredBarangList.map { it.namaBarang }
                val barangAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, barangNames)
                spinnerBarang.setAdapter(barangAdapter)
                spinnerBarang.setText("", false)
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Tambah Belanja")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val selectedBarangName = spinnerBarang.text.toString()
                val selectedBarang = filteredBarangList.find { it.namaBarang == selectedBarangName }
                val qty = etQty.text.toString().toDoubleOrNull() ?: 0.0
                val harga = etHarga.text.toString().toDoubleOrNull() ?: 0.0

                if (selectedBarang != null && qty > 0 && harga > 0) {
                    viewModel.insertTrafficMasuk(
                        TrafficMasuk(
                            tanggal = selectedDateDialog,
                            idBarang = selectedBarang.idBarang,
                            qty = qty,
                            harga = harga
                        )
                    )
                    Toast.makeText(this, "✅ Berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Data tidak lengkap", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditTrafficMasukDialog(item: com.indahaha.kasir.data.entities.TrafficMasukDetail) {
        val view = layoutInflater.inflate(R.layout.dialog_add_traffic_masuk, null)
        val etTanggal = view.findViewById<TextInputEditText>(R.id.etTanggal)
        val spinnerKategori = view.findViewById<AutoCompleteTextView>(R.id.spinnerKategori)
        val spinnerBarang = view.findViewById<AutoCompleteTextView>(R.id.spinnerBarang)
        val etQty = view.findViewById<TextInputEditText>(R.id.etQty)
        val etHarga = view.findViewById<TextInputEditText>(R.id.etHarga)

        var selectedDateDialog = item.tanggal
        etQty.setText(item.qty.toString())
        etHarga.setText(item.harga.toString())
        etTanggal.setText(DateUtils.formatDate(selectedDateDialog))

        etTanggal.setOnClickListener {
            showDatePickerDialog(selectedDateDialog) { newDate ->
                selectedDateDialog = newDate
                etTanggal.setText(DateUtils.formatDate(selectedDateDialog))
            }
        }

        var filteredBarangList = barangList
        val currentBarang = barangList.find { it.idBarang == item.idBarang }
        val currentKategori = kategoriList.find { it.idKategori == currentBarang?.idKategoriDefault }

        val kategoriNames = kategoriList.map { it.namaKategori }
        val kategoriAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, kategoriNames)
        spinnerKategori.setAdapter(kategoriAdapter)
        spinnerKategori.setText(currentKategori?.namaKategori ?: "", false)

        if (currentKategori != null) {
            filteredBarangList = barangList.filter { it.idKategoriDefault == currentKategori.idKategori }

            val barangNames = filteredBarangList.map { it.namaBarang }
            val barangAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, barangNames)
            spinnerBarang.setAdapter(barangAdapter)
            spinnerBarang.setText(item.namaBarang, false)
        }

        spinnerKategori.setOnItemClickListener { _, _, position, _ ->
            if (kategoriList.isNotEmpty() && position < kategoriList.size) {
                val selectedKategoriId = kategoriList[position].idKategori
                filteredBarangList = barangList.filter { it.idKategoriDefault == selectedKategoriId }

                val barangNames = filteredBarangList.map { it.namaBarang }
                val barangAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, barangNames)
                spinnerBarang.setAdapter(barangAdapter)
                spinnerBarang.setText("", false)
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Belanja")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                val selectedBarangName = spinnerBarang.text.toString()
                val selectedBarang = filteredBarangList.find { it.namaBarang == selectedBarangName }
                val qty = etQty.text.toString().toDoubleOrNull() ?: 0.0
                val harga = etHarga.text.toString().toDoubleOrNull() ?: 0.0

                if (selectedBarang != null && qty > 0 && harga > 0) {
                    viewModel.updateTrafficMasuk(
                        TrafficMasuk(
                            idTrafficMasuk = item.idTrafficMasuk,
                            tanggal = selectedDateDialog,
                            idBarang = selectedBarang.idBarang,
                            qty = qty,
                            harga = harga
                        )
                    )
                    Toast.makeText(this, "✅ Berhasil diupdate", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Data tidak lengkap", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showAddTrafficKeluarDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_traffic_keluar, null)
        val etTanggal = view.findViewById<TextInputEditText>(R.id.etTanggal)
        val spinnerKategori = view.findViewById<AutoCompleteTextView>(R.id.spinnerKategori)
        val spinnerBarang = view.findViewById<AutoCompleteTextView>(R.id.spinnerBarang)
        val etQty = view.findViewById<TextInputEditText>(R.id.etQty)
        val etKeterangan = view.findViewById<TextInputEditText>(R.id.etKeterangan)

        var selectedDateDialog = DateUtils.getCurrentDateTime()
        var filteredBarangList = barangList

        etTanggal.setText(DateUtils.formatDate(selectedDateDialog))
        etTanggal.setOnClickListener {
            showDatePickerDialog(selectedDateDialog) { newDate ->
                selectedDateDialog = newDate
                etTanggal.setText(DateUtils.formatDate(selectedDateDialog))
            }
        }

        val kategoriNames = kategoriList.map { it.namaKategori }
        val kategoriAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, kategoriNames)
        spinnerKategori.setAdapter(kategoriAdapter)

        spinnerKategori.setOnItemClickListener { _, _, position, _ ->
            if (kategoriList.isNotEmpty() && position < kategoriList.size) {
                val selectedKategoriId = kategoriList[position].idKategori
                filteredBarangList = barangList.filter { it.idKategoriDefault == selectedKategoriId }

                val barangNames = filteredBarangList.map { it.namaBarang }
                val barangAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, barangNames)
                spinnerBarang.setAdapter(barangAdapter)
                spinnerBarang.setText("", false)
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Tambah Pemakaian")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val selectedBarangName = spinnerBarang.text.toString()
                val selectedBarang = filteredBarangList.find { it.namaBarang == selectedBarangName }
                val qty = etQty.text.toString().toDoubleOrNull() ?: 0.0
                val keterangan = etKeterangan.text.toString()

                if (selectedBarang != null && qty > 0) {
                    viewModel.insertTrafficKeluar(
                        TrafficKeluar(
                            tanggal = selectedDateDialog,
                            idBarang = selectedBarang.idBarang,
                            qty = qty,
                            keterangan = keterangan
                        )
                    )
                    Toast.makeText(this, "✅ Berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Data tidak lengkap", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditTrafficKeluarDialog(item: com.indahaha.kasir.data.entities.TrafficKeluarDetail) {
        val view = layoutInflater.inflate(R.layout.dialog_add_traffic_keluar, null)
        val etTanggal = view.findViewById<TextInputEditText>(R.id.etTanggal)
        val spinnerKategori = view.findViewById<AutoCompleteTextView>(R.id.spinnerKategori)
        val spinnerBarang = view.findViewById<AutoCompleteTextView>(R.id.spinnerBarang)
        val etQty = view.findViewById<TextInputEditText>(R.id.etQty)
        val etKeterangan = view.findViewById<TextInputEditText>(R.id.etKeterangan)

        var selectedDateDialog = item.tanggal
        etQty.setText(item.qty.toString())
        etKeterangan.setText(item.keterangan)
        etTanggal.setText(DateUtils.formatDate(selectedDateDialog))

        etTanggal.setOnClickListener {
            showDatePickerDialog(selectedDateDialog) { newDate ->
                selectedDateDialog = newDate
                etTanggal.setText(DateUtils.formatDate(selectedDateDialog))
            }
        }

        var filteredBarangList = barangList
        val currentBarang = barangList.find { it.idBarang == item.idBarang }
        val currentKategori = kategoriList.find { it.idKategori == currentBarang?.idKategoriDefault }

        val kategoriNames = kategoriList.map { it.namaKategori }
        val kategoriAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, kategoriNames)
        spinnerKategori.setAdapter(kategoriAdapter)
        spinnerKategori.setText(currentKategori?.namaKategori ?: "", false)

        if (currentKategori != null) {
            filteredBarangList = barangList.filter { it.idKategoriDefault == currentKategori.idKategori }

            val barangNames = filteredBarangList.map { it.namaBarang }
            val barangAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, barangNames)
            spinnerBarang.setAdapter(barangAdapter)
            spinnerBarang.setText(item.namaBarang, false)
        }

        spinnerKategori.setOnItemClickListener { _, _, position, _ ->
            if (kategoriList.isNotEmpty() && position < kategoriList.size) {
                val selectedKategoriId = kategoriList[position].idKategori
                filteredBarangList = barangList.filter { it.idKategoriDefault == selectedKategoriId }

                val barangNames = filteredBarangList.map { it.namaBarang }
                val barangAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, barangNames)
                spinnerBarang.setAdapter(barangAdapter)
                spinnerBarang.setText("", false)
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Pemakaian")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                val selectedBarangName = spinnerBarang.text.toString()
                val selectedBarang = filteredBarangList.find { it.namaBarang == selectedBarangName }
                val qty = etQty.text.toString().toDoubleOrNull() ?: 0.0
                val keterangan = etKeterangan.text.toString()

                if (selectedBarang != null && qty > 0) {
                    viewModel.updateTrafficKeluar(
                        TrafficKeluar(
                            idTrafficKeluar = item.idTrafficKeluar,
                            tanggal = selectedDateDialog,
                            idBarang = selectedBarang.idBarang,
                            qty = qty,
                            keterangan = keterangan
                        )
                    )
                    Toast.makeText(this, "✅ Berhasil diupdate", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Data tidak lengkap", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDatePickerDialog(currentDate: Long, onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentDate

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // ✅ VALIDASI: Tidak bisa pilih tanggal masa depan
        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun showDeleteConfirmationDialog(itemName: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Data")
            .setMessage("Yakin ingin menghapus '$itemName'?")
            .setPositiveButton("Hapus") { _, _ ->
                onConfirm()
            }
            .setNegativeButton("Batal", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadDataByDate()
    }
}
