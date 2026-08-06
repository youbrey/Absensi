package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.AttendanceEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    private fun getTargetFile(context: Context, fileName: String): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        if (!dir.exists()) dir.mkdirs()
        return File(dir, fileName)
    }

    private fun copyToPublicDownloads(context: Context, sourceFile: File, fileName: String) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadsDir != null && (downloadsDir.exists() || downloadsDir.mkdirs())) {
                val publicFile = File(downloadsDir, fileName)
                sourceFile.copyTo(publicFile, overwrite = true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openOrShareFile(context: Context, file: File, mimeType: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Buka / Bagikan File Laporan (${file.name})").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membuka chooser file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportToPdf(context: Context, monthYearLabel: String, list: List<AttendanceEntity>): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size in points
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(15, 23, 42) // Navy
                textSize = 12f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(217, 119, 6) // Gold
                textSize = 10f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }

            val headerPaint = Paint().apply {
                color = Color.BLACK
                textSize = 9f
                isFakeBoldText = true
            }

            val bodyPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 8f
            }

            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }

            var y = 40f

            // Kop Surat Header
            canvas.drawText("PEMERINTAH KOTA BITUNG", 297f, y, titlePaint)
            y += 16f
            canvas.drawText("SEKRETARIAT DPRD KOTA BITUNG", 297f, y, titlePaint)
            y += 16f
            canvas.drawText("FORMULIR ABSENSI KEHADIRAN WORK FROM HOME (WFH) PNS DAN PPPK", 297f, y, subtitlePaint)
            y += 16f
            canvas.drawText("Laporan Periode Rekapitulasi: $monthYearLabel", 297f, y, bodyPaint.apply { textAlign = Paint.Align.CENTER })
            bodyPaint.textAlign = Paint.Align.LEFT

            y += 20f
            canvas.drawLine(30f, y, 565f, y, linePaint.apply { strokeWidth = 2f })
            y += 20f

            // Table Header
            val xNo = 35f
            val xNama = 65f
            val xNip = 200f
            val xJabatan = 310f
            val xJamMasuk = 420f
            val xJamPulang = 485f

            canvas.drawText("NO", xNo, y, headerPaint)
            canvas.drawText("NAMA LENGKAP", xNama, y, headerPaint)
            canvas.drawText("NIP", xNip, y, headerPaint)
            canvas.drawText("JABATAN", xJabatan, y, headerPaint)
            canvas.drawText("MASUK", xJamMasuk, y, headerPaint)
            canvas.drawText("PULANG", xJamPulang, y, headerPaint)

            y += 10f
            canvas.drawLine(30f, y, 565f, y, linePaint)
            y += 15f

            var index = 1
            for (item in list) {
                if (y > 780f) break // Page safety limit
                canvas.drawText("$index", xNo, y, bodyPaint)
                canvas.drawText(item.namaLengkap.take(22), xNama, y, bodyPaint)
                canvas.drawText(item.nip.take(18), xNip, y, bodyPaint)
                canvas.drawText(item.jabatan.take(18), xJabatan, y, bodyPaint)
                canvas.drawText(if (item.jamMasuk.isNotBlank()) item.jamMasuk else "-", xJamMasuk, y, bodyPaint)
                canvas.drawText(if (item.jamPulang.isNotBlank()) item.jamPulang else "-", xJamPulang, y, bodyPaint)

                y += 18f
                canvas.drawLine(30f, y - 5f, 565f, y - 5f, linePaint.apply { strokeWidth = 0.5f })
                index++
            }

            // Footer Signatures
            y += 30f
            if (y < 750f) {
                val dateNow = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
                canvas.drawText("Bitung, $dateNow", 380f, y, bodyPaint)
                y += 15f
                canvas.drawText("Sekretaris DPRD Kota Bitung", 380f, y, headerPaint)
                y += 45f
                canvas.drawText("(________________________)", 380f, y, headerPaint)
            }

            pdfDocument.finishPage(page)

            val fileName = "Rekap_Absensi_DPRD_Bitung_${monthYearLabel.replace(" ", "_")}.pdf"
            val file = getTargetFile(context, fileName)
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            copyToPublicDownloads(context, file, fileName)

            Toast.makeText(context, "Laporan PDF berhasil dibuat: ${file.name}", Toast.LENGTH_SHORT).show()
            openOrShareFile(context, file, "application/pdf")
            file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal mengekspor PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    fun exportToExcelCsv(context: Context, monthYearLabel: String, list: List<AttendanceEntity>): File? {
        return try {
            val fileName = "Rekap_Absensi_DPRD_Bitung_${monthYearLabel.replace(" ", "_")}.csv"
            val file = getTargetFile(context, fileName)
            val outputStream = FileOutputStream(file)

            val header = "NO,NAMA LENGKAP,NIP,JABATAN,JENIS ABSENSI,JAM MASUK,JAM PULANG,TANGGAL,LOKASI GPS,VERIFIKASI WAJAH,STATUS SYNC\n"
            outputStream.write(header.toByteArray(Charsets.UTF_8))

            var i = 1
            for (item in list) {
                val row = "$i,\"${item.namaLengkap}\",\"${item.nip}\",\"${item.jabatan}\",\"${item.jenisAbsensi}\",\"${item.jamMasuk}\",\"${item.jamPulang}\",\"${item.dateFormatted}\",\"${item.locationAddress}\",\"${if (item.faceVerified) "Terverifikasi Wajah" else "Manual"}\",\"${if (item.isSyncedToSheets) "Terhubung Sheets" else "Lokal"}\"\n"
                outputStream.write(row.toByteArray(Charsets.UTF_8))
                i++
            }

            outputStream.flush()
            outputStream.close()

            copyToPublicDownloads(context, file, fileName)

            Toast.makeText(context, "Laporan Excel (CSV) berhasil dibuat: ${file.name}", Toast.LENGTH_SHORT).show()
            openOrShareFile(context, file, "text/csv")
            file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal mengekspor CSV: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }
}
