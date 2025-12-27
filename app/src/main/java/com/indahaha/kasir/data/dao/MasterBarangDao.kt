package com.indahaha.kasir.data.dao

import androidx.room.*
import com.indahaha.kasir.data.entities.MasterBarang
import kotlinx.coroutines.flow.Flow

@Dao
interface MasterBarangDao {

    @Query("SELECT * FROM master_barang ORDER BY nama_barang ASC")
    fun getAllActive(): Flow<List<MasterBarang>>

    // ✅ FLOW version (untuk observe realtime)
    @Query("SELECT * FROM master_barang ORDER BY nama_barang ASC")
    fun getAllBarang(): Flow<List<MasterBarang>>

    // ✅ SUSPEND version (untuk single query, LEBIH STABIL!)
    @Query("SELECT * FROM master_barang ORDER BY nama_barang ASC")
    suspend fun getAllBarangList(): List<MasterBarang>

    @Query("SELECT * FROM master_barang WHERE id_kategori_default = :idKategori ORDER BY nama_barang ASC")
    suspend fun getByKategori(idKategori: Int): List<MasterBarang>

    @Query("SELECT * FROM master_barang WHERE id_barang = :id")
    suspend fun getById(id: Int): MasterBarang?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(barang: MasterBarang): Long

    @Update
    suspend fun update(barang: MasterBarang)

    @Delete
    suspend fun delete(barang: MasterBarang)
}
