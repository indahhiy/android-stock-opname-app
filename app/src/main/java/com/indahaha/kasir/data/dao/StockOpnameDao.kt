package com.indahaha.kasir.data.dao

import androidx.room.*
import com.indahaha.kasir.data.entities.StockOpnameHarian
import kotlinx.coroutines.flow.Flow
import com.indahaha.kasir.data.entities.StockOpnameDisplay

@Dao
interface StockOpnameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ledger: StockOpnameHarian): Long

    @Update
    suspend fun update(ledger: StockOpnameHarian)

    @Delete
    suspend fun delete(ledger: StockOpnameHarian)

    @Query("SELECT * FROM stock_opname_harian WHERE tanggal = :date AND id_barang = :idBarang")
    suspend fun getByDateAndItem(date: Long, idBarang: Int): StockOpnameHarian?

    @Query("""
        SELECT stok_akhir FROM stock_opname_harian
        WHERE tanggal < :date AND id_barang = :idBarang
        ORDER BY tanggal DESC LIMIT 1
    """)
    suspend fun getPreviousDayStockAkhir(date: Long, idBarang: Int): Double?

    @Query("""
        SELECT * FROM stock_opname_harian
        WHERE tanggal BETWEEN :startDate AND :endDate
        ORDER BY tanggal DESC, id_barang ASC
    """)
    fun getLedgerByDateRange(startDate: Long, endDate: Long): Flow<List<StockOpnameHarian>>

    // ✅ TAMBAHAN: Query untuk load data per tanggal (untuk StockOpnameActivity)
    @Query("""
        SELECT 
            soh.id_ledger,
            soh.tanggal,
            soh.id_barang,
            mb.nama_barang,
            mb.satuan,
            soh.stok_awal,
            soh.total_masuk_harian,
            soh.total_keluar_harian,
            soh.stok_akhir,
            soh.stok_fisik,
            soh.selisih,
            soh.catatan
        FROM stock_opname_harian soh
        JOIN master_barang mb ON soh.id_barang = mb.id_barang
        WHERE soh.tanggal = :date
        ORDER BY mb.nama_barang ASC
    """)
    fun getStockOpnameByDate(date: Long): Flow<List<StockOpnameDisplay>>

    @Query("""
        SELECT 
            soh.id_ledger,
            soh.tanggal,
            soh.id_barang,
            mb.nama_barang,
            mb.satuan,
            soh.stok_awal,
            soh.total_masuk_harian,
            soh.total_keluar_harian,
            soh.stok_akhir,
            soh.stok_fisik,
            soh.selisih,
            soh.catatan
        FROM stock_opname_harian soh
        JOIN master_barang mb ON soh.id_barang = mb.id_barang
        WHERE soh.tanggal BETWEEN :startDate AND :endDate
        ORDER BY soh.tanggal DESC, mb.nama_barang ASC
    """)
    fun getStockOpnameDisplay(startDate: Long, endDate: Long): Flow<List<StockOpnameDisplay>>

    @Query("""
        SELECT soh.* FROM stock_opname_harian soh
        INNER JOIN (
            SELECT id_barang, MAX(tanggal) as max_tanggal
            FROM stock_opname_harian
            GROUP BY id_barang
        ) AS latest ON soh.id_barang = latest.id_barang AND soh.tanggal = latest.max_tanggal
    """)
    suspend fun getLatestStockForAllItems(): List<StockOpnameHarian>
}
