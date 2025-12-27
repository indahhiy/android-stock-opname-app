package com.indahaha.kasir.data.repository

import com.indahaha.kasir.data.dao.StockOpnameDao
import com.indahaha.kasir.data.dao.TrafficDao
import com.indahaha.kasir.data.dao.MasterBarangDao
import com.indahaha.kasir.data.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class TrafficStockRepository(
    private val trafficDao: TrafficDao,
    private val stockOpnameDao: StockOpnameDao,
    private val barangDao: MasterBarangDao
) {

    // ==========================================
    // ✅ TRAFFIC MASUK (AUTO-UPDATE SO!)
    // ==========================================

    suspend fun insertTrafficMasuk(trafficMasuk: TrafficMasuk) {
        trafficDao.insertTrafficMasuk(trafficMasuk)
        // ✅ AUTO-UPDATE Stock Opname
        updateStockOpnameForItem(trafficMasuk.tanggal, trafficMasuk.idBarang)
    }

    suspend fun updateTrafficMasuk(trafficMasuk: TrafficMasuk) {
        trafficDao.updateTrafficMasuk(trafficMasuk)
        // ✅ AUTO-UPDATE Stock Opname
        updateStockOpnameForItem(trafficMasuk.tanggal, trafficMasuk.idBarang)
    }

    suspend fun deleteTrafficMasuk(trafficMasuk: TrafficMasuk) {
        trafficDao.deleteTrafficMasuk(trafficMasuk)
        // ✅ AUTO-UPDATE Stock Opname
        updateStockOpnameForItem(trafficMasuk.tanggal, trafficMasuk.idBarang)
    }

    fun getTrafficMasukDetailByDateRange(startDate: Long, endDate: Long): Flow<List<TrafficMasukDetail>> {
        return trafficDao.getTrafficMasukDetailByDateRange(startDate, endDate)
    }

    // ==========================================
    // ✅ TRAFFIC KELUAR (AUTO-UPDATE SO!)
    // ==========================================

    suspend fun insertTrafficKeluar(trafficKeluar: TrafficKeluar) {
        trafficDao.insertTrafficKeluar(trafficKeluar)
        // ✅ AUTO-UPDATE Stock Opname
        updateStockOpnameForItem(trafficKeluar.tanggal, trafficKeluar.idBarang)
    }

    suspend fun updateTrafficKeluar(trafficKeluar: TrafficKeluar) {
        trafficDao.updateTrafficKeluar(trafficKeluar)
        // ✅ AUTO-UPDATE Stock Opname
        updateStockOpnameForItem(trafficKeluar.tanggal, trafficKeluar.idBarang)
    }

    suspend fun deleteTrafficKeluar(trafficKeluar: TrafficKeluar) {
        trafficDao.deleteTrafficKeluar(trafficKeluar)
        // ✅ AUTO-UPDATE Stock Opname
        updateStockOpnameForItem(trafficKeluar.tanggal, trafficKeluar.idBarang)
    }

    fun getTrafficKeluarDetailByDateRange(startDate: Long, endDate: Long): Flow<List<TrafficKeluarDetail>> {
        return trafficDao.getTrafficKeluarDetailByDateRange(startDate, endDate)
    }

    // ==========================================
    // ✅ AUTO-UPDATE STOCK OPNAME (CORE LOGIC!)
    // ==========================================

    private suspend fun updateStockOpnameForItem(timestamp: Long, idBarang: Int) {
        val startOfDay = getStartOfDay(timestamp)
        val endOfDay = getEndOfDay(timestamp)

        // 1. ✅ AMBIL STOK AWAL DARI KEMARIN (OTOMATIS!)
        val stokAwal = stockOpnameDao.getPreviousDayStockAkhir(startOfDay, idBarang)
            ?: barangDao.getById(idBarang)?.stokAwal
            ?: 0.0

        // 2. ✅ Hitung total masuk hari ini (urutan parameter: idBarang, startOfDay, endOfDay)
        val totalMasuk = trafficDao.getTotalMasukByDateAndItem(idBarang, startOfDay, endOfDay)

        // 3. ✅ Hitung total keluar hari ini (urutan parameter: idBarang, startOfDay, endOfDay)
        val totalKeluar = trafficDao.getTotalKeluarByDateAndItem(idBarang, startOfDay, endOfDay)

        // 4. Hitung stok akhir
        val stokAkhir = stokAwal + totalMasuk - totalKeluar

        // 5. Check apakah sudah ada record
        val existing = stockOpnameDao.getByDateAndItem(startOfDay, idBarang)

        if (existing != null) {
            // UPDATE existing record (JANGAN UBAH stokFisik, selisih, catatan!)
            stockOpnameDao.update(existing.copy(
                stokAwal = stokAwal,
                totalMasukHarian = totalMasuk,
                totalKeluarHarian = totalKeluar,
                stokAkhir = stokAkhir
                // stokFisik, selisih, catatan TETAP
            ))
        } else {
            // INSERT new record
            stockOpnameDao.insert(StockOpnameHarian(
                tanggal = startOfDay,
                idBarang = idBarang,
                stokAwal = stokAwal,
                totalMasukHarian = totalMasuk,
                totalKeluarHarian = totalKeluar,
                stokAkhir = stokAkhir
            ))
        }
    }

    // ==========================================
    // ✅ GENERATE SO UNTUK SEMUA BARANG
    // ==========================================

    suspend fun generateStockOpnameForAllItems(date: Long) {
        val allBarang = barangDao.getAllActive().first()

        allBarang.forEach { barang ->
            updateStockOpnameForItem(date, barang.idBarang)
        }
    }

    // ==========================================
    // STOCK OPNAME
    // ==========================================

    suspend fun insertStockOpname(ledger: StockOpnameHarian) {
        stockOpnameDao.insert(ledger)
    }

    suspend fun updateStockOpname(ledger: StockOpnameHarian) {
        stockOpnameDao.update(ledger)
    }

    suspend fun deleteStockOpname(ledger: StockOpnameHarian) {
        stockOpnameDao.delete(ledger)
    }

    fun getStockOpnameByDateRange(startDate: Long, endDate: Long): Flow<List<StockOpnameHarian>> {
        return stockOpnameDao.getLedgerByDateRange(startDate, endDate)
    }

    fun getStockOpnameDisplay(startDate: Long, endDate: Long): Flow<List<StockOpnameDisplay>> {
        return stockOpnameDao.getStockOpnameDisplay(startDate, endDate)
    }

    fun getStockOpnameByDate(date: Long): Flow<List<StockOpnameDisplay>> {
        return stockOpnameDao.getStockOpnameByDate(date)
    }

    suspend fun getLatestStockForAllItems(): List<StockOpnameHarian> {
        return stockOpnameDao.getLatestStockForAllItems()
    }

    // ==========================================
    // DASHBOARD SUMMARY
    // ==========================================

    suspend fun getDashboardSummary(date: Long): DashboardSummary {
        val startOfDay = getStartOfDay(date)
        val endOfDay = getEndOfDay(date)

        val totalPengeluaran = trafficDao.getTotalPengeluaranByDateRange(startOfDay, endOfDay) ?: 0.0
        val totalMasukQty = trafficDao.getTotalMasukQtyByDateRange(startOfDay, endOfDay) ?: 0.0
        val totalKeluarQty = trafficDao.getTotalKeluarQtyByDateRange(startOfDay, endOfDay) ?: 0.0
        val pengeluaranPerKategori = trafficDao.getFinancialSummaryByDateRange(startOfDay, endOfDay)

        return DashboardSummary(
            totalPengeluaranHariIni = totalPengeluaran,
            totalMasukQtyHariIni = totalMasukQty,
            totalKeluarQtyHariIni = totalKeluarQty,
            pengeluaranPerKategori = pengeluaranPerKategori
        )
    }

    // ==========================================
    // STOCK ALERTS
    // ==========================================

    suspend fun getStockAlerts(): List<StockAlert> {
        val allBarang = barangDao.getAllActive().first()
        val latestStock = stockOpnameDao.getLatestStockForAllItems()
        val stockMap = latestStock.associateBy { it.idBarang }

        return allBarang.mapNotNull { barang ->
            val stock = stockMap[barang.idBarang]
            val stokSaatIni = stock?.stokAkhir ?: barang.stokAwal

            if (stokSaatIni <= barang.stokMinimum) {
                StockAlert(
                    idBarang = barang.idBarang,
                    namaBarang = barang.namaBarang,
                    stokSaatIni = stokSaatIni,
                    stokMinimum = barang.stokMinimum
                )
            } else null
        }
    }

    // ==========================================
    // FINANCIAL REPORTS
    // ==========================================

    suspend fun getFinancialSummary(startDate: Long, endDate: Long): List<FinancialSummary> {
        return trafficDao.getFinancialSummaryByDateRange(startDate, endDate)
    }

    suspend fun getTotalPengeluaran(startDate: Long, endDate: Long): Double {
        return trafficDao.getTotalPengeluaranByDateRange(startDate, endDate) ?: 0.0
    }

    // ==========================================
    // HELPER FUNCTIONS
    // ==========================================

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}
