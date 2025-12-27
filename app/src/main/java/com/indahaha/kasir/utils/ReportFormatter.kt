package com.indahaha.kasir.utils

import com.indahaha.kasir.data.entities.FinancialSummary
import com.indahaha.kasir.data.entities.TrafficMasukDetail
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object ReportFormatter {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun formatFinancialReportToHtml(
        startDate: Long,
        endDate: Long,
        financialSummary: List<FinancialSummary>,
        trafficMasukDetails: List<TrafficMasukDetail>
    ): String {
        val grandTotal = financialSummary.sumOf { it.totalPengeluaran }

        // Group by kategori
        val groupedByKategori = trafficMasukDetails.groupBy { it.namaKategori }

        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html>")
            appendLine("<head>")
            appendLine("<meta charset='UTF-8'>")
            appendLine("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
            appendLine("<title>Laporan Keuangan</title>")
            appendLine("<style>")
            appendLine("body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }")
            appendLine("h1 { text-align: center; color: #2C3E50; margin-bottom: 5px; }")
            appendLine(".periode { text-align: center; color: #7F8C8D; margin-bottom: 30px; font-size: 14px; }")
            appendLine("h2 { color: #34495E; border-bottom: 3px solid #3498DB; padding-bottom: 8px; margin-top: 30px; }")
            appendLine("h3 { color: #16A085; margin-top: 20px; margin-bottom: 10px; background: #E8F8F5; padding: 10px; border-radius: 5px; }")
            appendLine(".summary-box { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }")
            appendLine("table { width: 100%; border-collapse: collapse; margin: 15px 0; background: white; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }")
            appendLine("th { background-color: #3498DB; color: white; padding: 12px; text-align: left; font-weight: bold; }")
            appendLine("td { padding: 10px; border-bottom: 1px solid #ddd; }")
            appendLine("tr:hover { background-color: #f8f9fa; }")
            appendLine(".total-row { font-weight: bold; background-color: #E8F6F3 !important; font-size: 16px; }")
            appendLine(".grand-total { font-size: 24px; text-align: center; color: #27AE60; font-weight: bold; margin: 30px 0; padding: 20px; background: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }")
            appendLine(".kategori-section { background: white; padding: 15px; margin-bottom: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }")
            appendLine(".text-right { text-align: right; }")
            appendLine(".footer { text-align: center; color: #7F8C8D; margin-top: 40px; font-size: 12px; }")
            appendLine(".subtotal-row { background-color: #D5F4E6 !important; font-weight: bold; }")
            appendLine("</style>")
            appendLine("</head>")
            appendLine("<body>")

            appendLine("<h1>📊 LAPORAN KEUANGAN</h1>")
            appendLine("<p class='periode'>Periode: ${dateFormat.format(Date(startDate))} - ${dateFormat.format(Date(endDate))}</p>")

            // TOTAL KESELURUHAN
            appendLine("<div class='grand-total'>")
            appendLine("💰 TOTAL PENGELUARAN: ${currencyFormatter.format(grandTotal)}")
            appendLine("</div>")

            // RINGKASAN PER KATEGORI
            appendLine("<div class='summary-box'>")
            appendLine("<h2>📈 Ringkasan Per Kategori</h2>")
            appendLine("<table>")
            appendLine("<tr><th>Kategori</th><th class='text-right'>Total Pengeluaran</th></tr>")

            financialSummary.forEach { summary ->
                val formattedTotal = currencyFormatter.format(summary.totalPengeluaran)
                appendLine("<tr>")
                appendLine("<td><strong>${summary.namaKategori}</strong></td>")
                appendLine("<td class='text-right'>$formattedTotal</td>")
                appendLine("</tr>")
            }

            appendLine("</table>")
            appendLine("</div>")

            // DETAIL PER KATEGORI
            appendLine("<h2>📦 Detail Pembelian Per Kategori</h2>")

            groupedByKategori.forEach { (kategori, items) ->
                val totalKategori = items.sumOf { it.harga }

                appendLine("<div class='kategori-section'>")
                appendLine("<h3>🏷️ $kategori</h3>")
                appendLine("<table>")
                appendLine("<tr>")
                appendLine("<th>Tanggal</th>")
                appendLine("<th>Nama Barang</th>")
                appendLine("<th class='text-right'>Qty</th>")
                appendLine("<th class='text-right'>Harga</th>")
                appendLine("</tr>")

                items.sortedBy { it.tanggal }.forEach { detail ->
                    val formattedDate = dateFormat.format(Date(detail.tanggal))
                    val formattedHarga = currencyFormatter.format(detail.harga)
                    val qtyUnit = "${detail.qty} ${detail.satuan}"

                    appendLine("<tr>")
                    appendLine("<td>$formattedDate</td>")
                    appendLine("<td>${detail.namaBarang}</td>")
                    appendLine("<td class='text-right'>$qtyUnit</td>")
                    appendLine("<td class='text-right'>$formattedHarga</td>")
                    appendLine("</tr>")
                }

                appendLine("<tr class='subtotal-row'>")
                appendLine("<td colspan='3'><strong>SUBTOTAL $kategori</strong></td>")
                appendLine("<td class='text-right'><strong>${currencyFormatter.format(totalKategori)}</strong></td>")
                appendLine("</tr>")
                appendLine("</table>")
                appendLine("</div>")
            }

            // SEMUA TRANSAKSI (CHRONOLOGICAL)
            appendLine("<h2>📋 Semua Transaksi (Urut Tanggal)</h2>")
            appendLine("<table>")
            appendLine("<tr>")
            appendLine("<th>Tanggal</th>")
            appendLine("<th>Barang</th>")
            appendLine("<th>Kategori</th>")
            appendLine("<th class='text-right'>Qty</th>")
            appendLine("<th class='text-right'>Harga</th>")
            appendLine("</tr>")

            trafficMasukDetails.sortedByDescending { it.tanggal }.forEach { detail ->
                val formattedDate = dateFormat.format(Date(detail.tanggal))
                val formattedHarga = currencyFormatter.format(detail.harga)
                val qtyUnit = "${detail.qty} ${detail.satuan}"

                appendLine("<tr>")
                appendLine("<td>$formattedDate</td>")
                appendLine("<td>${detail.namaBarang}</td>")
                appendLine("<td>${detail.namaKategori}</td>")
                appendLine("<td class='text-right'>$qtyUnit</td>")
                appendLine("<td class='text-right'>$formattedHarga</td>")
                appendLine("</tr>")
            }

            appendLine("<tr class='total-row'>")
            appendLine("<td colspan='4'><strong>TOTAL KESELURUHAN</strong></td>")
            appendLine("<td class='text-right'><strong>${currencyFormatter.format(grandTotal)}</strong></td>")
            appendLine("</tr>")
            appendLine("</table>")

            appendLine("<p class='footer'>")
            appendLine("📅 Dibuat pada ${SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date())}")
            appendLine("</p>")
            appendLine("</body>")
            appendLine("</html>")
        }
    }
}
