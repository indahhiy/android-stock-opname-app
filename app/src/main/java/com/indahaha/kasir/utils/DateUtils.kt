package com.indahaha.kasir.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private const val DATE_FORMAT = "dd/MM/yyyy"
    private const val DATE_FORMAT_INPUT = "yyyy-MM-dd"

    fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date(timestamp))
    }

    fun formatDateInput(timestamp: Long): String {
        return SimpleDateFormat(DATE_FORMAT_INPUT, Locale.getDefault()).format(Date(timestamp))
    }

    fun getCurrentDateTime(): Long = System.currentTimeMillis()
}
