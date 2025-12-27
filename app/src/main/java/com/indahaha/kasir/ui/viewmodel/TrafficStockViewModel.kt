package com.indahaha.kasir.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.indahaha.kasir.data.entities.*
import com.indahaha.kasir.data.repository.MasterRepository
import com.indahaha.kasir.data.repository.TrafficStockRepository
import kotlinx.coroutines.launch

class TrafficStockViewModel(
    private val trafficRepository: TrafficStockRepository,
    private val masterRepository: MasterRepository
) : ViewModel() {

    // ✅ EXPOSE repository untuk diakses Activity
    val repository: TrafficStockRepository = trafficRepository

    private val _dashboardSummary = MutableLiveData<DashboardSummary>()
    val dashboardSummary: LiveData<DashboardSummary> = _dashboardSummary

    private val _stockAlerts = MutableLiveData<List<StockAlert>>()
    val stockAlerts: LiveData<List<StockAlert>> = _stockAlerts

    val allBarang: LiveData<List<MasterBarang>> = masterRepository.getAllBarang().asLiveData()

    fun loadDashboardSummary(date: Long) = viewModelScope.launch {
        _dashboardSummary.value = trafficRepository.getDashboardSummary(date)
    }

    fun loadStockAlerts() = viewModelScope.launch {
        _stockAlerts.value = trafficRepository.getStockAlerts()
    }

    fun insertTrafficMasuk(trafficMasuk: TrafficMasuk) = viewModelScope.launch {
        trafficRepository.insertTrafficMasuk(trafficMasuk)
    }

    fun updateTrafficMasuk(trafficMasuk: TrafficMasuk) = viewModelScope.launch {
        trafficRepository.updateTrafficMasuk(trafficMasuk)
    }

    fun deleteTrafficMasuk(trafficMasuk: TrafficMasuk) = viewModelScope.launch {
        trafficRepository.deleteTrafficMasuk(trafficMasuk)
    }

    fun insertTrafficKeluar(trafficKeluar: TrafficKeluar) = viewModelScope.launch {
        trafficRepository.insertTrafficKeluar(trafficKeluar)
    }

    fun updateTrafficKeluar(trafficKeluar: TrafficKeluar) = viewModelScope.launch {
        trafficRepository.updateTrafficKeluar(trafficKeluar)
    }

    fun deleteTrafficKeluar(trafficKeluar: TrafficKeluar) = viewModelScope.launch {
        trafficRepository.deleteTrafficKeluar(trafficKeluar)
    }

    fun updateStockOpname(ledger: StockOpnameHarian) = viewModelScope.launch {
        trafficRepository.updateStockOpname(ledger)
    }

    fun getTrafficMasukToday(startDate: Long, endDate: Long) =
        trafficRepository.getTrafficMasukDetailByDateRange(startDate, endDate).asLiveData()

    fun getTrafficKeluarToday(startDate: Long, endDate: Long) =
        trafficRepository.getTrafficKeluarDetailByDateRange(startDate, endDate).asLiveData()

    fun getStockOpnameByDate(date: Long) =
        trafficRepository.getStockOpnameByDate(date).asLiveData()
}
