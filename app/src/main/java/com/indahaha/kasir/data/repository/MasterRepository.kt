package com.indahaha.kasir.data.repository

import com.indahaha.kasir.data.dao.MasterBarangDao
import com.indahaha.kasir.data.dao.MasterKategoriDao
import com.indahaha.kasir.data.entities.MasterBarang
import com.indahaha.kasir.data.entities.MasterKategori
import kotlinx.coroutines.flow.Flow

class MasterRepository(
    private val kategoriDao: MasterKategoriDao,
    private val barangDao: MasterBarangDao
) {

    // KATEGORI
    fun getAllKategori(): Flow<List<MasterKategori>> = kategoriDao.getAllActive()

    suspend fun getKategoriById(id: Int): MasterKategori? = kategoriDao.getById(id)

    suspend fun insertKategori(kategori: MasterKategori) = kategoriDao.insert(kategori)

    suspend fun updateKategori(kategori: MasterKategori) = kategoriDao.update(kategori)

    suspend fun deleteKategori(kategori: MasterKategori) = kategoriDao.delete(kategori)

    // BARANG
    fun getAllBarang(): Flow<List<MasterBarang>> = barangDao.getAllActive()

    suspend fun getBarangByKategori(idKategori: Int): List<MasterBarang> = barangDao.getByKategori(idKategori)

    suspend fun getBarangById(id: Int): MasterBarang? = barangDao.getById(id)

    suspend fun insertBarang(barang: MasterBarang) = barangDao.insert(barang)

    suspend fun updateBarang(barang: MasterBarang) = barangDao.update(barang)

    suspend fun deleteBarang(barang: MasterBarang) = barangDao.delete(barang)
}
