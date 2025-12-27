package com.indahaha.kasir.ui.activity

import android.app.DatePickerDialog
import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.indahaha.kasir.R
import com.indahaha.kasir.data.AppDatabase
import com.indahaha.kasir.data.repository.MasterRepository
import com.indahaha.kasir.data.repository.TrafficStockRepository
import com.indahaha.kasir.ui.viewmodel.TrafficStockViewModel
import com.indahaha.kasir.ui.viewmodel.ViewModelFactory
import com.indahaha.kasir.utils.DateUtils
import com.indahaha.kasir.utils.ReportFormatter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ReportActivity : AppCompatActivity() {

    private lateinit var trafficStockViewModel: TrafficStockViewModel
    private lateinit var webViewReport: WebView
    private lateinit var btnDownload: Button
    private var startDate: Long = DateUtils.getStartOfDay(Date().time)
    private var endDate: Long = DateUtils.getEndOfDay(Date().time)
    private var currentHtmlContent: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        setupViewModel()
        setupViews()
        updateDateDisplay()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this@ReportActivity)
        val masterRepository = MasterRepository(database.masterKategoriDao(), database.masterBarangDao())
        val trafficStockRepository = TrafficStockRepository(
            database.trafficDao(),
            database.stockOpnameDao(),
            database.masterBarangDao()
        )

        val factory = ViewModelFactory(masterRepository, trafficStockRepository)
        trafficStockViewModel = ViewModelProvider(this, factory)[TrafficStockViewModel::class.java]
    }

    private fun setupViews() {
        webViewReport = findViewById(R.id.webview_report)
        btnDownload = findViewById(R.id.btn_download_report)

        findViewById<Button>(R.id.btn_select_start_date).setOnClickListener {
            showDatePicker(true)
        }

        findViewById<Button>(R.id.btn_select_end_date).setOnClickListener {
            showDatePicker(false)
        }

        findViewById<Button>(R.id.btn_generate_report).setOnClickListener {
            generateReport()
        }

        btnDownload.setOnClickListener {
            downloadReport()
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
        findViewById<TextView>(R.id.tv_start_date).text = DateUtils.formatDate(startDate)
        findViewById<TextView>(R.id.tv_end_date).text = DateUtils.formatDate(endDate)
    }

    private fun generateReport() {
        lifecycleScope.launch {
            try {
                val financialSummary = trafficStockViewModel.repository.getFinancialSummary(startDate, endDate)
                val trafficMasukDetails = trafficStockViewModel.repository.getTrafficMasukDetailByDateRange(startDate, endDate).first()

                if (trafficMasukDetails.isEmpty()) {
                    Toast.makeText(this@ReportActivity, "Tidak ada data pada periode ini", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Format to HTML
                currentHtmlContent = ReportFormatter.formatFinancialReportToHtml(
                    startDate,
                    endDate,
                    financialSummary,
                    trafficMasukDetails
                )

                // Tampilkan di WebView
                webViewReport.loadDataWithBaseURL(null, currentHtmlContent, "text/html", "UTF-8", null)
                webViewReport.visibility = android.view.View.VISIBLE
                btnDownload.visibility = android.view.View.VISIBLE

                Toast.makeText(this@ReportActivity, "✅ Laporan berhasil dibuat", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this@ReportActivity, "Gagal membuat laporan: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun downloadReport() {
        if (currentHtmlContent.isEmpty()) {
            Toast.makeText(this, "Buat laporan terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val fileName = "Laporan_Keuangan_${SimpleDateFormat("ddMMyyyy", Locale.getDefault()).format(startDate)}_${SimpleDateFormat("ddMMyyyy", Locale.getDefault()).format(endDate)}.html"
                val savedFile = saveHtmlFile(currentHtmlContent, fileName)

                if (savedFile != null) {
                    Toast.makeText(this@ReportActivity, "✅ Laporan berhasil disimpan di folder Downloads", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@ReportActivity, "❌ Gagal menyimpan laporan", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@ReportActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun saveHtmlFile(htmlContent: String, fileName: String): File? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/html")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    contentResolver.openOutputStream(it)?.use { output ->
                        output.write(htmlContent.toByteArray())
                    }
                }

                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { output ->
                    output.write(htmlContent.toByteArray())
                }
                file
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
