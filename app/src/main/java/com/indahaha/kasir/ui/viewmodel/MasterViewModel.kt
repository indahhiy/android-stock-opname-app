package com.indahaha.kasir.ui.viewmodel

import androidx.lifecycle.*
import com.indahaha.kasir.data.entities.MasterBarang
import com.indahaha.kasir.data.entities.MasterKategori
import com.indahaha.kasir.data.repository.MasterRepository
import kotlinx.coroutines.launch

class MasterViewModel(private val repository: MasterRepository) : ViewModel() {

    val allKategori: LiveData<List<MasterKategori>> = repository.getAllKategori().asLiveData()
    val allBarang: LiveData<List<MasterBarang>> = repository.getAllBarang().asLiveData()

    fun insertKategori(kategori: MasterKategori) = viewModelScope.launch {
        repository.insertKategori(kategori)
    }

    fun updateKategori(kategori: MasterKategori) = viewModelScope.launch {
        repository.updateKategori(kategori)
    }

    fun deleteKategori(kategori: MasterKategori) = viewModelScope.launch {
        repository.deleteKategori(kategori)
    }

    fun insertBarang(barang: MasterBarang) = viewModelScope.launch {
        repository.insertBarang(barang)
    }

    fun updateBarang(barang: MasterBarang) = viewModelScope.launch {
        repository.updateBarang(barang)
    }

    fun deleteBarang(barang: MasterBarang) = viewModelScope.launch {
        repository.deleteBarang(barang)
    }

    suspend fun getBarangByKategori(idKategori: Int): List<MasterBarang> {
        return repository.getBarangByKategori(idKategori)
    }
}
