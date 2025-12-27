package com.indahaha.kasir.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.indahaha.kasir.data.repository.MasterRepository
import com.indahaha.kasir.data.repository.TrafficStockRepository

class ViewModelFactory(
    private val masterRepository: MasterRepository,
    private val trafficStockRepository: TrafficStockRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MasterViewModel::class.java) -> {
                MasterViewModel(masterRepository) as T
            }

            modelClass.isAssignableFrom(TrafficStockViewModel::class.java) -> {
                TrafficStockViewModel(trafficStockRepository, masterRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
