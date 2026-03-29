package com.adrian.queuenote

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object PdfHelper {

    fun generateInventoryPdf(context: Context, articulos: List<Articulo>): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        var y = 40f
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Reporte de Inventario - QueueNote", 50f, y, paint)
        y += 40f

        paint.textSize = 12f
        canvas.drawText("Artículo", 50f, y, paint)
        canvas.drawText("Cant", 350f, y, paint)
        canvas.drawText("Precio", 420f, y, paint)
        canvas.drawText("Costo", 500f, y, paint)
        y += 20f
        canvas.drawLine(50f, y, 550f, y, paint)
        y += 20f

        paint.isFakeBoldText = false
        articulos.forEach { art ->
            val nombre = if ((art.nombre?.length ?: 0) > 40) art.nombre?.substring(0, 37) + "..." else art.nombre ?: "N/A"
            canvas.drawText(nombre, 50f, y, paint)
            canvas.drawText(art.unidadesInt.toString(), 350f, y, paint)
            canvas.drawText("$${String.format("%.2f", art.precioDouble)}", 420f, y, paint)
            canvas.drawText("$${String.format("%.2f", art.costoDouble)}", 500f, y, paint)
            y += 20f
        }

        y += 30f
        paint.isFakeBoldText = true
        canvas.drawLine(50f, y, 550f, y, paint)
        y += 25f
        val totalPrecio = articulos.sumOf { it.precioDouble * (if(it.unidadesInt > 0) it.unidadesInt else 1) }
        val totalCosto = articulos.sumOf { it.costoDouble * (if(it.unidadesInt > 0) it.unidadesInt else 1) }
        canvas.drawText("Total Venta: $${String.format("%.2f", totalPrecio)}", 50f, y, paint)
        y += 20f
        canvas.drawText("Total Costo: $${String.format("%.2f", totalCosto)}", 50f, y, paint)
        y += 20f
        canvas.drawText("Beneficio Total: $${String.format("%.2f", totalPrecio - totalCosto)}", 50f, y, paint)

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "reporte_inventario.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            pdfDocument.close()
            null
        }
    }

    // NUEVO: Exportar a la carpeta de descargas (Downloads)
    fun exportPdfToDownloads(context: Context, sourceFile: File) {
        val fileName = "Reporte_Inventario_${System.currentTimeMillis()}.pdf"
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)
                    sourceFile.inputStream().use { input ->
                        outputStream?.use { output ->
                            input.copyTo(output)
                        }
                    }
                    Toast.makeText(context, "Exportado a Descargas", Toast.LENGTH_SHORT).show()
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val destFile = File(downloadsDir, fileName)
                sourceFile.copyTo(destFile, overwrite = true)
                Toast.makeText(context, "Exportado a Descargas", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al exportar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sharePdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Reporte"))
    }

    fun printPdf(context: Context, file: File) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "QueueNote Document"
        val printAdapter = MyPrintDocumentAdapter(file)
        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
    }
}

class MyPrintDocumentAdapter(private val file: File) : android.print.PrintDocumentAdapter() {
    override fun onLayout(oldAttributes: PrintAttributes?, newAttributes: PrintAttributes?, cancellationSignal: android.os.CancellationSignal?, callback: LayoutResultCallback?, extras: android.os.Bundle?) {
        val info = android.print.PrintDocumentInfo.Builder("print_output.pdf").setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build()
        callback?.onLayoutFinished(info, true)
    }
    override fun onWrite(pages: Array<out android.print.PageRange>?, destination: android.os.ParcelFileDescriptor?, cancellationSignal: android.os.CancellationSignal?, callback: WriteResultCallback?) {
        val input = java.io.FileInputStream(file)
        val output = java.io.FileOutputStream(destination?.fileDescriptor)
        try {
            input.copyTo(output)
            callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback?.onWriteFailed(e.message)
        } finally {
            input.close()
            output.close()
        }
    }
}
