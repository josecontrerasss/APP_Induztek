package com.example.app_induztek.ui.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.app_induztek.ui.viewmodel.CertificadoData
import java.io.File
import java.io.FileOutputStream

object CertificadoPdf {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val LINE_HEIGHT = 16f
    private const val HEADER_BG = 0xFF1B5E20.toInt()
    private const val TEXT_COLOR = 0xFF212121.toInt()
    private const val SUBTEXT_COLOR = 0xFF616161.toInt()
    private const val LINE_COLOR = 0xFFBDBDBD.toInt()

    fun generar(context: Context, data: CertificadoData): File {
        val dir = File(context.cacheDir, "certificados")
        if (!dir.exists()) dir.mkdirs()
        val sanitizado = data.protocolo.numero.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val archivo = File(dir, "$sanitizado.pdf")

        val documento = PdfDocument()

        val tituloPaint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subtituloPaint = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            isAntiAlias = true
        }
        val seccionPaint = Paint().apply {
            color = HEADER_BG
            textSize = 13f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            color = SUBTEXT_COLOR
            textSize = 10f
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = TEXT_COLOR
            textSize = 11f
            isAntiAlias = true
        }
        val boldPaint = Paint().apply {
            color = TEXT_COLOR
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = LINE_COLOR
            strokeWidth = 0.5f
        }
        val headerPaint = Paint().apply {
            color = HEADER_BG
        }

        var pageNumber = 1
        var page = documento.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        )
        var canvas = page.canvas

        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 70f, headerPaint)
        canvas.drawText("INDUZTEK - Certificado de Protocolo", MARGIN, 40f, tituloPaint)
        canvas.drawText("Documento técnico oficial", MARGIN, 58f, subtituloPaint)

        var y = 100f

        val protocolo = data.protocolo
        val equipo = data.equipo
        val instalacion = data.instalacion
        val cliente = data.cliente

        val infoRows = mutableListOf(
            "N° Protocolo" to protocolo.numero,
            "Fecha" to protocolo.fecha_hora_prueba,
            "Empresa cliente" to (cliente?.nombre ?: "No informado"),
            "RUT" to (cliente?.rut ?: "No informado"),
            "Instalación" to (instalacion?.let { "${it.codigo} · ${it.nombre}" } ?: "No informado"),
            "Ubicación" to (instalacion?.ubicacion ?: "No informado"),
            "Equipo (tag)" to (equipo?.tag ?: "No informado"),
            "Marca / Modelo" to (equipo?.let { "${it.marca} / ${it.modelo}" } ?: "No informado"),
            "N° de serie" to (equipo?.numero_serie ?: "No informado")
        )

        canvas.drawText("INFORMACIÓN GENERAL", MARGIN, y, seccionPaint)
        y += 8f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += LINE_HEIGHT

        for ((label, value) in infoRows) {
            canvas.drawText("$label:", MARGIN, y, labelPaint)
            canvas.drawText(value, MARGIN + 130f, y, textPaint)
            y += LINE_HEIGHT
        }

        y += 8f
        canvas.drawText("MEDICIONES", MARGIN, y, seccionPaint)
        y += 8f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += LINE_HEIGHT

        val colX = floatArrayOf(MARGIN, MARGIN + 200f, MARGIN + 320f, MARGIN + 430f)
        canvas.drawText("Parámetro", colX[0], y, boldPaint)
        canvas.drawText("Valor", colX[1], y, boldPaint)
        canvas.drawText("Unidad", colX[2], y, boldPaint)
        canvas.drawText("Estado", colX[3], y, boldPaint)
        y += 4f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += LINE_HEIGHT

        if (data.mediciones.isEmpty()) {
            canvas.drawText("Sin mediciones registradas.", MARGIN, y, textPaint)
            y += LINE_HEIGHT
        } else {
            for (m in data.mediciones) {
                if (y > PAGE_HEIGHT - 90f) {
                    documento.finishPage(page)
                    pageNumber++
                    page = documento.startPage(
                        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    )
                    canvas = page.canvas
                    y = 60f
                }
                canvas.drawText(truncar(m.parametro_nombre, 34), colX[0], y, textPaint)
                canvas.drawText(truncar(m.valor, 14), colX[1], y, textPaint)
                canvas.drawText(truncar(m.unidad, 12), colX[2], y, textPaint)
                canvas.drawText(truncar(m.estado_alerta, 12), colX[3], y, textPaint)
                y += LINE_HEIGHT
            }
        }

        y += 8f
        if (y > PAGE_HEIGHT - 200f) {
            documento.finishPage(page)
            pageNumber++
            page = documento.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            )
            canvas = page.canvas
            y = 60f
        }
        canvas.drawText("RESULTADO", MARGIN, y, seccionPaint)
        y += 8f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += LINE_HEIGHT
        canvas.drawText("Temperatura: ${protocolo.temperatura_c} °C", MARGIN, y, textPaint)
        y += LINE_HEIGHT
        canvas.drawText("Humedad: ${protocolo.humedad_pct} %", MARGIN, y, textPaint)
        y += LINE_HEIGHT
        canvas.drawText("Resultado: ${protocolo.resultado}", MARGIN, y, boldPaint)
        y += LINE_HEIGHT
        canvas.drawText("Estado: ${protocolo.estado}", MARGIN, y, textPaint)
        y += LINE_HEIGHT

        y += 8f
        if (y > PAGE_HEIGHT - 140f) {
            documento.finishPage(page)
            pageNumber++
            page = documento.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            )
            canvas = page.canvas
            y = 60f
        }
        canvas.drawText("OBSERVACIONES", MARGIN, y, seccionPaint)
        y += 8f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += LINE_HEIGHT

        val observaciones = protocolo.observaciones.ifBlank { "Sin observaciones." }
        for (linea in envolverTexto(observaciones, 88)) {
            if (y > PAGE_HEIGHT - 60f) {
                documento.finishPage(page)
                pageNumber++
                page = documento.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                )
                canvas = page.canvas
                y = 60f
            }
            canvas.drawText(linea, MARGIN, y, textPaint)
            y += LINE_HEIGHT
        }

        canvas.drawText(
            "Documento generado por la app Induztek",
            MARGIN,
            PAGE_HEIGHT - 30f,
            labelPaint
        )

        documento.finishPage(page)

        FileOutputStream(archivo).use { out ->
            documento.writeTo(out)
        }
        documento.close()

        return archivo
    }

    fun compartir(context: Context, archivo: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(viewIntent, "Abrir certificado")
        chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (context !is Activity) {
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val sendChooser = Intent.createChooser(sendIntent, "Compartir certificado")
            sendChooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (context !is Activity) {
                sendChooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(sendChooser)
        }
    }

    private fun truncar(texto: String, max: Int): String =
        if (texto.length <= max) texto else texto.substring(0, max - 1) + "…"

    private fun envolverTexto(texto: String, maxChars: Int): List<String> {
        val resultado = mutableListOf<String>()
        val parrafos = texto.split("\n")
        for (parrafo in parrafos) {
            if (parrafo.length <= maxChars) {
                resultado.add(parrafo)
            } else {
                var actual = ""
                for (palabra in parrafo.split(" ")) {
                    when {
                        actual.isEmpty() -> actual = palabra
                        actual.length + 1 + palabra.length <= maxChars -> actual += " $palabra"
                        else -> {
                            resultado.add(actual)
                            actual = palabra
                        }
                    }
                }
                if (actual.isNotEmpty()) resultado.add(actual)
            }
        }
        return resultado
    }
}
