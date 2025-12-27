package com.indahaha.kasir.ui.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.indahaha.kasir.R
import com.indahaha.kasir.data.AppDatabase
import com.indahaha.kasir.data.entities.StockOpnameHarian
import com.indahaha.kasir.ui.adapter.StockOpnameAdapter
import com.indahaha.kasir.utils.DateUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class StockOpnameActivity : AppCompatActivity() {

    private lateinit var adapter: StockOpnameAdapter
    private lateinit var etTanggal: TextInputEditText
    private lateinit var db: AppDatabase
    private var selectedDate: Long = DateUtils.getStartOfDay(Date().time)

    companion object {
        private const val TAG = "STOCK_OPNAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_opname)

        Log.e(TAG, "═══════════════════════════════════════")
        Log.e(TAG, "✅ onCreate() STARTED")

        db = AppDatabase.getDatabase(this)
        setupUI()
        setupButtons()

        Log.e(TAG, "✅ onCreate() FINISHED")
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "═══════════════════════════════════════")
        Log.e(TAG, "✅ onResume() CALLED - AKAN LOAD DATA")
        loadStockData()
    }

    private fun setupUI() {
        Log.e(TAG, "setupUI() started")

        etTanggal = findViewById(R.id.etTanggal)
        etTanggal.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
        Log.e(TAG, "etTanggal initialized: ${etTanggal.text}")

        adapter = StockOpnameAdapter { display ->
            showUpdateStokFisikDialog(display)
        }
        Log.e(TAG, "adapter initialized")

        findViewById<RecyclerView>(R.id.rvStockOpname).apply {
            layoutManager = LinearLayoutManager(this@StockOpnameActivity)
            adapter = this@StockOpnameActivity.adapter
        }
        Log.e(TAG, "RecyclerView initialized")
    }

    private fun setupButtons() {
        Log.e(TAG, "setupButtons() started")

        etTanggal.setOnClickListener {
            Log.e(TAG, "etTanggal clicked")
            showDatePicker()
        }

        val btnLoadData = findViewById<Button>(R.id.btnLoadData)
        Log.e(TAG, "btnLoadData found: ${btnLoadData != null}")

        if (btnLoadData == null) {
            Log.e(TAG, "❌❌❌ btnLoadData NULL! Check activity_stock_opname.xml!")
        } else {
            btnLoadData.setOnClickListener {
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "✅ BUTTON LOAD DATA DIKLIK!")
                loadStockData()
            }
        }

        val btnExport = findViewById<Button>(R.id.btnExportExcel)
        Log.e(TAG, "btnExportExcel found: ${btnExport != null}")

        if (btnExport == null) {
            Log.e(TAG, "❌ btnExportExcel NULL! Check activity_stock_opname.xml!")
        } else {
            btnExport.setOnClickListener {
                Log.e(TAG, "btnExportExcel clicked")
                exportToExcel()
            }
        }

        Log.e(TAG, "setupButtons() finished")
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate

        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                selectedDate = calendar.timeInMillis
                etTanggal.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time))
                Log.e(TAG, "Date selected: $selectedDate")
                loadStockData()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadStockData() {
        Log.e(TAG, "═══════════════════════════════════════")
        Log.e(TAG, "✅✅✅ loadStockData() DIPANGGIL!")
        Log.e(TAG, "selectedDate = $selectedDate")
        Log.e(TAG, "selectedDate formatted = ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(selectedDate))}")

        lifecycleScope.launch {
            Log.e(TAG, "✅ lifecycleScope.launch STARTED")

            try {
                Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.e(TAG, "🔄 MULAI LOAD DATA untuk tanggal: $selectedDate")

                // ✅ STEP 1: AUTO-GENERATE/UPDATE dulu (di IO thread)
                withContext(Dispatchers.IO) {
                    Log.e(TAG, "✅ Entering generateStockOpname()")
                    generateStockOpname(selectedDate)
                    Log.e(TAG, "✅ Exiting generateStockOpname()")
                }

                // ✅ STEP 2: LOAD & DISPLAY
                Log.e(TAG, "✅ Starting to collect data from DAO")
                db.stockOpnameDao().getStockOpnameByDate(selectedDate)
                    .collect { displayList ->
                        Log.e(TAG, "📦 Data collected: ${displayList.size} items")

                        adapter.submitList(displayList)

                        if (displayList.isNotEmpty()) {
                            displayList.forEach {
                                Log.e(TAG, "  ├─ ${it.namaBarang}: ${it.stokAwal} + ${it.totalMasukHarian} - ${it.totalKeluarHarian} = ${it.stokAkhir}")
                            }
                            Toast.makeText(
                                this@StockOpnameActivity,
                                "✅ Data loaded: ${displayList.size} barang",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Log.e(TAG, "⚠️ displayList KOSONG!")
                            Toast.makeText(
                                this@StockOpnameActivity,
                                "ℹ️ Belum ada barang. Tambah master barang dulu!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } catch (e: CancellationException) {
                // ✅ NORMAL: Activity ditutup
                Log.e(TAG, "⚠️ Job cancelled (activity closed)")
                throw e
            } catch (e: Exception) {
                // ❌ ERROR BENERAN
                Log.e(TAG, "❌ ERROR: ${e.message}", e)
                e.printStackTrace()
                Toast.makeText(
                    this@StockOpnameActivity,
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ✅ FUNGSI AUTO-GENERATE/UPDATE STOCK OPNAME (SELALU REFRESH!)
    private suspend fun generateStockOpname(date: Long) {
        try {
            Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.e(TAG, "🔍 generateStockOpname() STARTED")

            // 1️⃣ Ambil semua barang
            val allBarang = db.masterBarangDao().getAllBarangList()
            Log.e(TAG, "🔍 Generate untuk ${allBarang.size} barang")

            if (allBarang.isEmpty()) {
                Log.e(TAG, "⚠️ TIDAK ADA BARANG! Tambah master barang dulu!")
                return
            }

            // ✅ Hitung START & END OF DAY
            val startOfDay = DateUtils.getStartOfDay(date)
            val endOfDay = DateUtils.getEndOfDay(date)

            Log.e(TAG, "📅 Date range:")
            Log.e(TAG, "   Start: $startOfDay (${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(startOfDay))})")
            Log.e(TAG, "   End:   $endOfDay (${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(endOfDay))})")

            // 2️⃣ Loop setiap barang - SELALU RECALCULATE!
            allBarang.forEachIndexed { index, barang ->
                Log.e(TAG, "━━━ Processing barang ${index + 1}/${allBarang.size}: ${barang.namaBarang} (id=${barang.idBarang}) ━━━")

                // Cek: sudah ada record untuk tanggal ini?
                val existing = db.stockOpnameDao().getByDateAndItem(startOfDay, barang.idBarang)

                // 3️⃣ Ambil stok kemarin
                val stokKemarin = existing?.stokAwal ?: (db.stockOpnameDao().getPreviousDayStockAkhir(startOfDay, barang.idBarang) ?: 0.0)
                Log.e(TAG, "  ├─ Stok kemarin: $stokKemarin")

                // ✅ 4️⃣ Hitung belanja hari ini
                Log.e(TAG, "  ├─ Querying traffic masuk for idBarang=${barang.idBarang}")
                val belanja = db.trafficDao().getTotalMasukByItemAndDate(barang.idBarang, startOfDay, endOfDay) ?: 0.0
                Log.e(TAG, "  ├─ Belanja result: $belanja")

                // ✅ 5️⃣ Hitung pemakaian hari ini
                Log.e(TAG, "  ├─ Querying traffic keluar for idBarang=${barang.idBarang}")
                val pemakaian = db.trafficDao().getTotalKeluarByItemAndDate(barang.idBarang, startOfDay, endOfDay) ?: 0.0
                Log.e(TAG, "  ├─ Pemakaian result: $pemakaian")

                // 6️⃣ Hitung stok akhir
                val stokAkhir = stokKemarin + belanja - pemakaian

                Log.e(TAG, "  ├─ ${barang.namaBarang} SUMMARY:")
                Log.e(TAG, "      Stok Kemarin: $stokKemarin")
                Log.e(TAG, "      Belanja:      $belanja")
                Log.e(TAG, "      Pemakaian:    $pemakaian")
                Log.e(TAG, "      Stok Akhir:   $stokAkhir")

                // ✅ 7️⃣ INSERT atau UPDATE (SELALU!)
                if (existing != null) {
                    // UPDATE existing record (stokFisik, selisih, catatan tetap dipertahankan!)
                    val updated = existing.copy(
                        stokAwal = stokKemarin,
                        totalMasukHarian = belanja,
                        totalKeluarHarian = pemakaian,
                        stokAkhir = stokAkhir
                        // stokFisik, selisih, catatan TIDAK diubah!
                    )
                    Log.e(TAG, "  ├─ Updating database...")
                    db.stockOpnameDao().update(updated)
                    Log.e(TAG, "  └─ ${barang.namaBarang}: ✅ UPDATED")
                } else {
                    // INSERT new record
                    val ledger = StockOpnameHarian(
                        tanggal = startOfDay,
                        idBarang = barang.idBarang,
                        stokAwal = stokKemarin,
                        totalMasukHarian = belanja,
                        totalKeluarHarian = pemakaian,
                        stokAkhir = stokAkhir
                    )
                    Log.e(TAG, "  ├─ Inserting to database...")
                    val insertedId = db.stockOpnameDao().insert(ledger)
                    Log.e(TAG, "  └─ ${barang.namaBarang}: ✅ INSERTED (id=$insertedId)")
                }
            }

            Log.e(TAG, "✅ Generate selesai!")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Generate error: ${e.message}", e)
            e.printStackTrace()
            throw e
        }
    }

    private fun showUpdateStokFisikDialog(display: com.indahaha.kasir.data.entities.StockOpnameDisplay) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input_stok_fisik, null)
        val etStokFisik = dialogView.findViewById<TextInputEditText>(R.id.etStokFisik)
        val etCatatan = dialogView.findViewById<TextInputEditText>(R.id.etCatatan)

        etStokFisik.setText(display.stokFisik?.toString() ?: "")
        etCatatan.setText(display.catatan ?: "")

        AlertDialog.Builder(this)
            .setTitle("Input Stok Fisik")
            .setMessage("${display.namaBarang}\nStok Sistem: ${display.stokAkhir} ${display.satuan}")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val stokFisik = etStokFisik.text.toString().toDoubleOrNull()
                val catatan = etCatatan.text.toString()

                if (stokFisik != null) {
                    lifecycleScope.launch {
                        val ledger = db.stockOpnameDao().getByDateAndItem(
                            display.tanggal,
                            display.idBarang
                        )

                        if (ledger != null) {
                            val updated = ledger.copy(
                                stokFisik = stokFisik,
                                selisih = ledger.stokAkhir - stokFisik,
                                catatan = catatan.ifBlank { null }
                            )

                            db.stockOpnameDao().update(updated)
                            Toast.makeText(
                                this@StockOpnameActivity,
                                "✅ Stok fisik tersimpan",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Reload
                            loadStockData()
                        }
                    }
                } else {
                    Toast.makeText(this, "❌ Stok fisik harus diisi angka", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun exportToExcel() {
        lifecycleScope.launch {
            try {
                db.stockOpnameDao().getStockOpnameByDate(selectedDate)
                    .collect { displayList ->
                        if (displayList.isEmpty()) {
                            Toast.makeText(
                                this@StockOpnameActivity,
                                "❌ Tidak ada data untuk diekspor",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@collect
                        }

                        val csv = buildString {
                            appendLine("Nama Barang,Satuan,Stok Awal,Masuk,Keluar,Stok Akhir,Stok Fisik,Selisih,Catatan")
                            displayList.forEach { item ->
                                appendLine(
                                    "${item.namaBarang},${item.satuan},${item.stokAwal}," +
                                            "${item.totalMasukHarian},${item.totalKeluarHarian}," +
                                            "${item.stokAkhir},${item.stokFisik ?: ""},${item.selisih ?: ""}," +
                                            "\"${item.catatan ?: ""}\""
                                )
                            }
                        }

                        Log.d("EXPORT_CSV", csv)
                        Toast.makeText(
                            this@StockOpnameActivity,
                            "✅ Export berhasil! ${displayList.size} items\nCek logcat",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(
                    this@StockOpnameActivity,
                    "❌ Export gagal: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
