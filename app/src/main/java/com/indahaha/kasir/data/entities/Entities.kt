package com.indahaha.kasir.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// ============================================
// MASTER TABLES
// ============================================

@Entity(tableName = "master_kategori")
data class MasterKategori(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_kategori")
    val idKategori: Int = 0,

    @ColumnInfo(name = "nama_kategori")
    val namaKategori: String
)

@Entity(tableName = "master_barang")
data class MasterBarang(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_barang")
    val idBarang: Int = 0,

    @ColumnInfo(name = "nama_barang")
    val namaBarang: String,

    @ColumnInfo(name = "id_kategori_default")
    val idKategoriDefault: Int,

    @ColumnInfo(name = "satuan")
    val satuan: String,

    @ColumnInfo(name = "stok_minimum")
    val stokMinimum: Double = 0.0,

    @ColumnInfo(name = "stok_awal")
    val stokAwal: Double = 0.0
)

// ============================================
// TRANSACTION TABLES
// ============================================

@Entity(tableName = "traffic_masuk")
data class TrafficMasuk(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_traffic_masuk")
    val idTrafficMasuk: Int = 0,

    @ColumnInfo(name = "tanggal")
    val tanggal: Long,

    @ColumnInfo(name = "id_barang")
    val idBarang: Int,

    @ColumnInfo(name = "qty")
    val qty: Double,

    @ColumnInfo(name = "harga")
    val harga: Double
)

@Entity(tableName = "traffic_keluar")
data class TrafficKeluar(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_traffic_keluar")
    val idTrafficKeluar: Int = 0,

    @ColumnInfo(name = "tanggal")
    val tanggal: Long,

    @ColumnInfo(name = "id_barang")
    val idBarang: Int,

    @ColumnInfo(name = "qty")
    val qty: Double,

    @ColumnInfo(name = "keterangan")
    val keterangan: String
)

@Entity(tableName = "stock_opname_harian")
data class StockOpnameHarian(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_ledger")
    val idLedger: Int = 0,

    @ColumnInfo(name = "tanggal")
    val tanggal: Long,

    @ColumnInfo(name = "id_barang")
    val idBarang: Int,

    @ColumnInfo(name = "stok_awal")
    val stokAwal: Double = 0.0,

    @ColumnInfo(name = "total_masuk_harian")
    val totalMasukHarian: Double = 0.0,

    @ColumnInfo(name = "total_keluar_harian")
    val totalKeluarHarian: Double = 0.0,

    @ColumnInfo(name = "stok_akhir")
    val stokAkhir: Double = 0.0,

    @ColumnInfo(name = "stok_fisik")
    val stokFisik: Double? = null,

    @ColumnInfo(name = "selisih")
    val selisih: Double? = null,

    @ColumnInfo(name = "catatan")
    val catatan: String? = null
)

// ============================================
// DATA CLASSES FOR DISPLAY (NON-ENTITY)
// ============================================

data class StockOpnameDisplay(
    @ColumnInfo(name = "id_ledger") val idLedger: Int = 0,
    @ColumnInfo(name = "tanggal") val tanggal: Long,
    @ColumnInfo(name = "id_barang") val idBarang: Int,
    @ColumnInfo(name = "nama_barang") val namaBarang: String,
    @ColumnInfo(name = "satuan") val satuan: String,
    @ColumnInfo(name = "stok_awal") val stokAwal: Double,
    @ColumnInfo(name = "total_masuk_harian") val totalMasukHarian: Double,
    @ColumnInfo(name = "total_keluar_harian") val totalKeluarHarian: Double,
    @ColumnInfo(name = "stok_akhir") val stokAkhir: Double,
    @ColumnInfo(name = "stok_fisik") val stokFisik: Double? = null,
    @ColumnInfo(name = "selisih") val selisih: Double? = null,
    @ColumnInfo(name = "catatan") val catatan: String? = null
)

data class TrafficMasukDetail(
    @ColumnInfo(name = "id_traffic_masuk") val idTrafficMasuk: Int,
    @ColumnInfo(name = "tanggal") val tanggal: Long,
    @ColumnInfo(name = "qty") val qty: Double,
    @ColumnInfo(name = "harga") val harga: Double,
    @ColumnInfo(name = "nama_barang") val namaBarang: String,
    @ColumnInfo(name = "satuan") val satuan: String,
    @ColumnInfo(name = "nama_kategori") val namaKategori: String,
    @ColumnInfo(name = "id_barang") val idBarang: Int
)

data class TrafficKeluarDetail(
    @ColumnInfo(name = "id_traffic_keluar") val idTrafficKeluar: Int,
    @ColumnInfo(name = "tanggal") val tanggal: Long,
    @ColumnInfo(name = "qty") val qty: Double,
    @ColumnInfo(name = "keterangan") val keterangan: String,
    @ColumnInfo(name = "nama_barang") val namaBarang: String,
    @ColumnInfo(name = "satuan") val satuan: String,
    @ColumnInfo(name = "id_barang") val idBarang: Int
)

data class FinancialSummary(
    @ColumnInfo(name = "idKategori") val idKategori: Int,
    @ColumnInfo(name = "namaKategori") val namaKategori: String,
    @ColumnInfo(name = "totalPengeluaran") val totalPengeluaran: Double
)

// ✅ TAMBAHAN: Data classes yang diperlukan ViewModel
data class DashboardSummary(
    val totalPengeluaranHariIni: Double = 0.0,
    val totalMasukQtyHariIni: Double = 0.0,
    val totalKeluarQtyHariIni: Double = 0.0,
    val pengeluaranPerKategori: List<FinancialSummary> = emptyList()
)

data class StockAlert(
    val idBarang: Int,
    val namaBarang: String,
    val stokSaatIni: Double,
    val stokMinimum: Double
)

data class TrafficMasukSummary(
    @ColumnInfo(name = "nama_barang") val namaBarang: String,
    @ColumnInfo(name = "qty") val qty: Double,
    @ColumnInfo(name = "satuan") val satuan: String
)

data class TrafficKeluarSummary(
    @ColumnInfo(name = "nama_barang") val namaBarang: String,
    @ColumnInfo(name = "qty") val qty: Double,
    @ColumnInfo(name = "satuan") val satuan: String
)
