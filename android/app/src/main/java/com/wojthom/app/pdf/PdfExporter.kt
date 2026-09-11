package com.wojthom.app.pdf

import android.content.ContentResolver
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.wojthom.app.i18n.AppLanguage
import com.wojthom.app.i18n.PdfStrings
import com.wojthom.app.i18n.pdfStrings
import com.wojthom.app.model.TimeEntry
import java.time.format.DateTimeFormatter

object PdfExporter {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
    private const val ROW_HEIGHT = 28f

    fun write(
        resolver: ContentResolver,
        uri: Uri,
        header: String,
        entries: List<TimeEntry>,
        language: AppLanguage
    ): Boolean = runCatching {
        val strings = pdfStrings(language)
        val document = PdfDocument()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val totalMinutes = entries.sumOf { it.minutes }

        var pageNumber = 1
        var page = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        )
        var canvas = page.canvas
        var y = drawPageHeader(canvas, paint, header, strings)
        y = drawTableHeader(canvas, paint, y, strings)

        entries.forEach { entry ->
            if (y + ROW_HEIGHT > PAGE_HEIGHT - 74f) {
                drawFooter(canvas, paint, pageNumber, strings)
                document.finishPage(page)
                pageNumber += 1
                page = document.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                )
                canvas = page.canvas
                y = drawPageHeader(canvas, paint, header, strings)
                y = drawTableHeader(canvas, paint, y, strings)
            }

            drawEntryRow(canvas, paint, y, entry, dateFormatter)
            y += ROW_HEIGHT
        }

        if (y + 80f > PAGE_HEIGHT - 70f) {
            drawFooter(canvas, paint, pageNumber, strings)
            document.finishPage(page)
            pageNumber += 1
            page = document.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            )
            canvas = page.canvas
            y = drawPageHeader(canvas, paint, header, strings)
        }

        drawSummary(canvas, paint, y + 18f, totalMinutes, entries.size, strings)
        drawFooter(canvas, paint, pageNumber, strings)
        document.finishPage(page)

        resolver.openOutputStream(uri)?.use { output ->
            document.writeTo(output)
        } ?: error("Could not open output stream")

        document.close()
    }.isSuccess

    private fun drawPageHeader(
        canvas: android.graphics.Canvas,
        paint: Paint,
        header: String,
        strings: PdfStrings
    ): Float {
        val defaultHeaders = setOf("Lista Czasu Pracy", "Work Time List", "Arbeidstidsliste")
        val title = header.trim().takeIf { it.isNotBlank() && it !in defaultHeaders } ?: strings.title

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 22f
        canvas.drawText(title.take(48), MARGIN, 58f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        canvas.drawText(strings.generatedBy, MARGIN, 78f, paint)

        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN, 90f, PAGE_WIDTH - MARGIN, 90f, paint)
        return 112f
    }

    private fun drawTableHeader(
        canvas: android.graphics.Canvas,
        paint: Paint,
        top: Float,
        strings: PdfStrings
    ): Float {
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f

        val left = MARGIN
        val right = PAGE_WIDTH - MARGIN
        canvas.drawLine(left, top, right, top, paint)
        canvas.drawLine(left, top + ROW_HEIGHT, right, top + ROW_HEIGHT, paint)

        drawCellText(canvas, paint, strings.date, 40f, top)
        drawCellText(canvas, paint, strings.client, 112f, top)
        drawCellText(canvas, paint, strings.start, 330f, top)
        drawCellText(canvas, paint, strings.end, 390f, top)
        drawCellText(canvas, paint, strings.duration, 450f, top)

        return top + ROW_HEIGHT
    }

    private fun drawEntryRow(
        canvas: android.graphics.Canvas,
        paint: Paint,
        top: Float,
        entry: TimeEntry,
        dateFormatter: DateTimeFormatter
    ) {
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f

        drawCellText(canvas, paint, entry.date?.format(dateFormatter) ?: "-", 40f, top)
        drawCellText(canvas, paint, ellipsize(entry.client.ifBlank { "-" }, 31), 112f, top)
        drawCellText(canvas, paint, entry.start, 330f, top)
        drawCellText(canvas, paint, entry.end, 390f, top)
        drawCellText(canvas, paint, "${entry.durationText} h", 450f, top)

        paint.strokeWidth = 0.5f
        canvas.drawLine(MARGIN, top + ROW_HEIGHT, PAGE_WIDTH - MARGIN, top + ROW_HEIGHT, paint)
    }

    private fun drawCellText(
        canvas: android.graphics.Canvas,
        paint: Paint,
        text: String,
        x: Float,
        top: Float
    ) {
        canvas.drawText(text, x, top + 18f, paint)
    }

    private fun drawSummary(
        canvas: android.graphics.Canvas,
        paint: Paint,
        y: Float,
        totalMinutes: Int,
        entryCount: Int,
        strings: PdfStrings
    ) {
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        val total = "%d:%02d h".format(totalMinutes / 60, totalMinutes % 60)
        canvas.drawText("${strings.total}: $total", MARGIN, y, paint)
        canvas.drawText("${strings.entries}: $entryCount", MARGIN, y + 22f, paint)
    }

    private fun drawFooter(
        canvas: android.graphics.Canvas,
        paint: Paint,
        pageNumber: Int,
        strings: PdfStrings
    ) {
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        canvas.drawLine(MARGIN, PAGE_HEIGHT - 42f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 42f, paint)
        canvas.drawText(strings.generatedBy, MARGIN, PAGE_HEIGHT - 26f, paint)
        canvas.drawText(pageNumber.toString(), PAGE_WIDTH - MARGIN - 12f, PAGE_HEIGHT - 26f, paint)
    }

    private fun ellipsize(text: String, maxLength: Int): String {
        if (text.length <= maxLength) return text
        return text.take(maxLength - 1) + "…"
    }
}
