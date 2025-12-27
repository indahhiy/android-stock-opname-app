package com.indahaha.kasir.data.dao

import androidx.room.*
import com.indahaha.kasir.data.entities.MasterKategori
import kotlinx.coroutines.flow.Flow

@Dao
interface MasterKategoriDao {

    @Query("SELECT * FROM master_kategori ORDER BY nama_kategori ASC")
    fun getAllActive(): Flow<List<MasterKategori>>

    @Query("SELECT * FROM master_kategori WHERE id_kategori = :id")
    suspend fun getById(id: Int): MasterKategori?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(kategori: MasterKategori): Long

    @Update
    suspend fun update(kategori: MasterKategori)

    @Delete
    suspend fun delete(kategori: MasterKategori)
}
