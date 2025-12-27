package com.indahaha.kasir.data.dao

import androidx.room.*
import com.indahaha.kasir.data.entities.TrafficMasukDetail

@Dao
interface ItemReportDao {

    // ✅ FIX: Tambah nama_kategori di query
    @Query("""
        SELECT 
            COUNT(tm.id_traffic_masuk) as totalTransaksi,
            COALESCE(SUM(tm.qty), 0) as totalQty,
            COALESCE(SUM(tm.harga), 0) as totalHarga,
            mb.nama_barang as namaBarang,
            mb.satuan as satuan,
            mk.nama_kategori as namaKategori
        FROM master_barang mb
        LEFT JOIN traffic_masuk tm ON tm.id_barang = mb.id_barang 
            AND tm.tanggal BETWEEN :startDate AND :endDate
        LEFT JOIN master_kategori mk ON mb.id_kategori_default = mk.id_kategori
        WHERE mb.id_barang = :idBarang
        GROUP BY mb.id_barang, mb.nama_barang, mb.satuan, mk.nama_kategori
    """)
    suspend fun getItemSummary(idBarang: Int, startDate: Long, endDate: Long): ItemSummary?

    // Get detail transactions for specific item
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
        WHERE tm.id_barang = :idBarang 
        AND tm.tanggal BETWEEN :startDate AND :endDate
        ORDER BY tm.tanggal ASC
    """)
    suspend fun getItemDetails(idBarang: Int, startDate: Long, endDate: Long): List<TrafficMasukDetail>
}

// ✅ FIX: Tambah field namaKategori
data class ItemSummary(
    @ColumnInfo(name = "totalTransaksi") val totalTransaksi: Int,
    @ColumnInfo(name = "totalQty") val totalQty: Double,
    @ColumnInfo(name = "totalHarga") val totalHarga: Double,
    @ColumnInfo(name = "namaBarang") val namaBarang: String,
    @ColumnInfo(name = "satuan") val satuan: String,
    @ColumnInfo(name = "namaKategori") val namaKategori: String
)
