package com.indahaha.kasir.data.dao

import androidx.room.*
import com.indahaha.kasir.data.entities.TrafficKeluar
import com.indahaha.kasir.data.entities.TrafficKeluarDetail
import com.indahaha.kasir.data.entities.TrafficMasuk
import com.indahaha.kasir.data.entities.TrafficMasukDetail
import com.indahaha.kasir.data.entities.FinancialSummary
import kotlinx.coroutines.flow.Flow
import com.indahaha.kasir.data.entities.TrafficMasukSummary
import com.indahaha.kasir.data.entities.TrafficKeluarSummary


@Dao
interface TrafficDao {

    // TRAFFIC MASUK
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrafficMasuk(trafficMasuk: TrafficMasuk): Long

    @Update
    suspend fun updateTrafficMasuk(trafficMasuk: TrafficMasuk)

    @Delete
    suspend fun deleteTrafficMasuk(trafficMasuk: TrafficMasuk)

    @Query("""
        SELECT 
            tm.id_traffic_masuk,
            tm.tanggal,
            tm.id_barang,
            tm.qty,
            tm.harga,
            mb.nama_barang,
            mb.satuan,
            mk.nama_kategori
        FROM traffic_masuk tm
        JOIN master_barang mb ON tm.id_barang = mb.id_barang
        JOIN master_kategori mk ON mb.id_kategori_default = mk.id_kategori
        WHERE tm.tanggal BETWEEN :startDate AND :endDate
        ORDER BY tm.tanggal DESC
    """)
    fun getTrafficMasukDetailByDateRange(startDate: Long, endDate: Long): Flow<List<TrafficMasukDetail>>

    @Query("SELECT SUM(qty) FROM traffic_masuk WHERE tanggal BETWEEN :startDate AND :endDate")
    suspend fun getTotalMasukQtyByDateRange(startDate: Long, endDate: Long): Double?

    @Query("SELECT SUM(qty) FROM traffic_masuk WHERE id_barang = :idBarang AND tanggal BETWEEN :startDate AND :endDate")
    suspend fun getTotalMasukByItemAndDate(idBarang: Int, startDate: Long, endDate: Long): Double?

    // ✅ FIX: Ubah urutan parameter jadi (idBarang, startOfDay, endOfDay)
    @Query("""
        SELECT COALESCE(SUM(qty), 0.0) 
        FROM traffic_masuk 
        WHERE id_barang = :idBarang 
          AND tanggal >= :startOfDay 
          AND tanggal <= :endOfDay
    """)
    suspend fun getTotalMasukByDateAndItem(idBarang: Int, startOfDay: Long, endOfDay: Long): Double

    // TRAFFIC KELUAR
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrafficKeluar(trafficKeluar: TrafficKeluar): Long

    @Update
    suspend fun updateTrafficKeluar(trafficKeluar: TrafficKeluar)

    @Delete
    suspend fun deleteTrafficKeluar(trafficKeluar: TrafficKeluar)

    @Query("""
        SELECT 
            tk.id_traffic_keluar,
            tk.tanggal,
            tk.id_barang,
            tk.qty,
            tk.keterangan,
            mb.nama_barang,
            mb.satuan
        FROM traffic_keluar tk
        JOIN master_barang mb ON tk.id_barang = mb.id_barang
        WHERE tk.tanggal BETWEEN :startDate AND :endDate
        ORDER BY tk.tanggal DESC
    """)
    fun getTrafficKeluarDetailByDateRange(startDate: Long, endDate: Long): Flow<List<TrafficKeluarDetail>>

    @Query("SELECT SUM(qty) FROM traffic_keluar WHERE tanggal BETWEEN :startDate AND :endDate")
    suspend fun getTotalKeluarQtyByDateRange(startDate: Long, endDate: Long): Double?

    @Query("SELECT SUM(qty) FROM traffic_keluar WHERE id_barang = :idBarang AND tanggal BETWEEN :startDate AND :endDate")
    suspend fun getTotalKeluarByItemAndDate(idBarang: Int, startDate: Long, endDate: Long): Double?

    // ✅ FIX: Ubah urutan parameter jadi (idBarang, startOfDay, endOfDay)
    @Query("""
        SELECT COALESCE(SUM(qty), 0.0) 
        FROM traffic_keluar 
        WHERE id_barang = :idBarang 
          AND tanggal >= :startOfDay 
          AND tanggal <= :endOfDay
    """)
    suspend fun getTotalKeluarByDateAndItem(idBarang: Int, startOfDay: Long, endOfDay: Long): Double

    // FINANCIAL
    @Query("""
        SELECT 
            mb.id_kategori_default AS idKategori,
            mk.nama_kategori AS namaKategori,
            SUM(tm.harga) AS totalPengeluaran
        FROM traffic_masuk tm
        JOIN master_barang mb ON tm.id_barang = mb.id_barang
        JOIN master_kategori mk ON mb.id_kategori_default = mk.id_kategori
        WHERE tm.tanggal BETWEEN :startDate AND :endDate
        GROUP BY mb.id_kategori_default, mk.nama_kategori
        ORDER BY mk.nama_kategori
    """)
    suspend fun getFinancialSummaryByDateRange(startDate: Long, endDate: Long): List<FinancialSummary>

    @Query("SELECT SUM(harga) FROM traffic_masuk WHERE tanggal BETWEEN :startDate AND :endDate")
    suspend fun getTotalPengeluaranByDateRange(startDate: Long, endDate: Long): Double?

    @Query("""
        SELECT 
            mb.nama_barang,
            SUM(tm.qty) as qty,
            mb.satuan
        FROM traffic_masuk tm
        JOIN master_barang mb ON tm.id_barang = mb.id_barang
        WHERE tm.tanggal BETWEEN :startDate AND :endDate
        GROUP BY tm.id_barang, mb.nama_barang, mb.satuan
        ORDER BY SUM(tm.qty) DESC
        LIMIT 5
    """)
    suspend fun getTopTrafficMasukToday(startDate: Long, endDate: Long): List<TrafficMasukSummary>

    @Query("""
        SELECT 
            mb.nama_barang,
            SUM(tk.qty) as qty,
            mb.satuan
        FROM traffic_keluar tk
        JOIN master_barang mb ON tk.id_barang = mb.id_barang
        WHERE tk.tanggal BETWEEN :startDate AND :endDate
        GROUP BY tk.id_barang, mb.nama_barang, mb.satuan
        ORDER BY SUM(tk.qty) DESC
        LIMIT 5
    """)
    suspend fun getTopTrafficKeluarToday(startDate: Long, endDate: Long): List<TrafficKeluarSummary>

}
