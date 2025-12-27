package com.indahaha.kasir.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.indahaha.kasir.R
import com.indahaha.kasir.data.AppDatabase
import com.indahaha.kasir.data.entities.MasterBarang
import com.indahaha.kasir.data.entities.MasterKategori
import com.indahaha.kasir.data.repository.MasterRepository
import com.indahaha.kasir.data.repository.TrafficStockRepository
import com.indahaha.kasir.databinding.ActivityMasterBinding
import com.indahaha.kasir.ui.adapter.BarangAdapter
import com.indahaha.kasir.ui.adapter.KategoriAdapter
import com.indahaha.kasir.ui.viewmodel.MasterViewModel
import com.indahaha.kasir.ui.viewmodel.ViewModelFactory

class MasterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMasterBinding
    private lateinit var viewModel: MasterViewModel
    private lateinit var kategoriAdapter: KategoriAdapter
    private lateinit var barangAdapter: BarangAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMasterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupAdapters()
        setupRecyclerViews()
        setupButtons()
        observeData()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this@MasterActivity)

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
        viewModel = ViewModelProvider(this, factory)[MasterViewModel::class.java]
    }

    private fun setupAdapters() {
        kategoriAdapter = KategoriAdapter { kategori ->
            showEditKategoriDialog(kategori)
        }

        barangAdapter = BarangAdapter { barang ->
            showEditBarangDialog(barang)
        }
    }

    private fun setupRecyclerViews() {
        binding.rvKategori.apply {
            layoutManager = LinearLayoutManager(this@MasterActivity)
            adapter = kategoriAdapter
        }

        binding.rvBarang.apply {
            layoutManager = LinearLayoutManager(this@MasterActivity)
            adapter = barangAdapter
        }
    }

    private fun setupButtons() {
        binding.btnAddKategori.setOnClickListener {
            showAddKategoriDialog()
        }

        binding.btnAddBarang.setOnClickListener {
            showAddBarangDialog()
        }
    }

    private fun observeData() {
        viewModel.allKategori.observe(this) { kategoriList ->
            kategoriAdapter.submitList(kategoriList)
        }

        viewModel.allBarang.observe(this) { barangList ->
            barangAdapter.submitList(barangList)
        }
    }

    // ============================================
    // DIALOG KATEGORI
    // ============================================

    private fun showAddKategoriDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_kategori, null)
        val etNamaKategori = dialogView.findViewById<TextInputEditText>(R.id.etNamaKategori)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNamaKategori.text.toString().trim()
                if (nama.isNotBlank()) {
                    viewModel.insertKategori(MasterKategori(namaKategori = nama))
                    Toast.makeText(this, "✅ Kategori '$nama' ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Nama kategori tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditKategoriDialog(kategori: MasterKategori) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_kategori, null)
        val etNamaKategori = dialogView.findViewById<TextInputEditText>(R.id.etNamaKategori)

        // Pre-fill dengan data existing
        etNamaKategori.setText(kategori.namaKategori)

        // Ubah title jadi "Edit Kategori"
        dialogView.findViewById<android.widget.TextView>(R.id.tvTitle)?.text = "✏️ Edit Kategori"

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val nama = etNamaKategori.text.toString().trim()
                if (nama.isNotBlank()) {
                    viewModel.updateKategori(kategori.copy(namaKategori = nama))
                    Toast.makeText(this, "✅ Kategori diupdate", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hapus") { _, _ ->
                showDeleteKategoriConfirmation(kategori)
            }
            .setNeutralButton("Batal", null)
            .show()
    }

    private fun showDeleteKategoriConfirmation(kategori: MasterKategori) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Kategori?")
            .setMessage("Yakin hapus kategori '${kategori.namaKategori}'?\n\n⚠️ Data tidak bisa dikembalikan!")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteKategori(kategori)
                Toast.makeText(this, "✅ Kategori dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // ============================================
    // DIALOG BARANG
    // ============================================

    private fun showAddBarangDialog() {
        val kategoriList = viewModel.allKategori.value

        if (kategoriList.isNullOrEmpty()) {
            Toast.makeText(this, "❌ Tambah kategori dulu!", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_barang, null)

        // ✅ FIX: Pakai AutoCompleteTextView (bukan Spinner)
        val spinnerKategori = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerKategori)
        val etNamaBarang = dialogView.findViewById<TextInputEditText>(R.id.etNamaBarang)
        val etSatuan = dialogView.findViewById<TextInputEditText>(R.id.etSatuan)
        val etMinStock = dialogView.findViewById<TextInputEditText>(R.id.etMinStock) // ✅ ID: etMinStock
        val etStokAwal = dialogView.findViewById<TextInputEditText>(R.id.etStokAwal)

        // Setup AutoCompleteTextView Kategori
        val kategoriNames = kategoriList.map { it.namaKategori }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, kategoriNames)
        spinnerKategori.setAdapter(adapter)
        spinnerKategori.setText(kategoriNames[0], false) // Set default

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val selectedKategoriName = spinnerKategori.text.toString()
                val selectedKategori = kategoriList.find { it.namaKategori == selectedKategoriName }

                if (selectedKategori == null) {
                    Toast.makeText(this, "❌ Pilih kategori dulu!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val nama = etNamaBarang.text.toString().trim()
                val satuan = etSatuan.text.toString().trim()
                val stokMin = etMinStock.text.toString().toDoubleOrNull() ?: 0.0
                val stokAwal = etStokAwal.text.toString().toDoubleOrNull() ?: 0.0

                if (nama.isNotBlank() && satuan.isNotBlank()) {
                    viewModel.insertBarang(
                        MasterBarang(
                            namaBarang = nama,
                            satuan = satuan,
                            idKategoriDefault = selectedKategori.idKategori,
                            stokMinimum = stokMin,
                            stokAwal = stokAwal
                        )
                    )
                    Toast.makeText(this, "✅ Barang '$nama' ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Nama dan satuan harus diisi", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditBarangDialog(barang: MasterBarang) {
        val kategoriList = viewModel.allKategori.value

        if (kategoriList.isNullOrEmpty()) {
            Toast.makeText(this, "❌ Data kategori tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_barang, null)

        val spinnerKategori = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerKategori)
        val etNamaBarang = dialogView.findViewById<TextInputEditText>(R.id.etNamaBarang)
        val etSatuan = dialogView.findViewById<TextInputEditText>(R.id.etSatuan)
        val etMinStock = dialogView.findViewById<TextInputEditText>(R.id.etMinStock)
        val etStokAwal = dialogView.findViewById<TextInputEditText>(R.id.etStokAwal)

        // Setup AutoCompleteTextView
        val kategoriNames = kategoriList.map { it.namaKategori }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, kategoriNames)
        spinnerKategori.setAdapter(adapter)

        // Set current values
        val currentKategori = kategoriList.find { it.idKategori == barang.idKategoriDefault }
        spinnerKategori.setText(currentKategori?.namaKategori ?: kategoriNames[0], false)

        etNamaBarang.setText(barang.namaBarang)
        etSatuan.setText(barang.satuan)
        etMinStock.setText(barang.stokMinimum.toString())
        etStokAwal.setText(barang.stokAwal.toString())

        // Ubah title
        dialogView.findViewById<android.widget.TextView>(R.id.tvTitle)?.text = "✏️ Edit Barang"

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val selectedKategoriName = spinnerKategori.text.toString()
                val selectedKategori = kategoriList.find { it.namaKategori == selectedKategoriName }

                if (selectedKategori == null) {
                    Toast.makeText(this, "❌ Pilih kategori dulu!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val nama = etNamaBarang.text.toString().trim()
                val satuan = etSatuan.text.toString().trim()
                val stokMin = etMinStock.text.toString().toDoubleOrNull() ?: 0.0
                val stokAwal = etStokAwal.text.toString().toDoubleOrNull() ?: 0.0

                if (nama.isNotBlank() && satuan.isNotBlank()) {
                    viewModel.updateBarang(
                        barang.copy(
                            namaBarang = nama,
                            satuan = satuan,
                            idKategoriDefault = selectedKategori.idKategori,
                            stokMinimum = stokMin,
                            stokAwal = stokAwal
                        )
                    )
                    Toast.makeText(this, "✅ Barang diupdate", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Nama dan satuan harus diisi", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hapus") { _, _ ->
                showDeleteBarangConfirmation(barang)
            }
            .setNeutralButton("Batal", null)
            .show()
    }

    private fun showDeleteBarangConfirmation(barang: MasterBarang) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Barang?")
            .setMessage("Yakin hapus barang '${barang.namaBarang}'?\n\n⚠️ Data tidak bisa dikembalikan!")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteBarang(barang)
                Toast.makeText(this, "✅ Barang dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
