package com.wojthom.app.pdf

import android.content.ContentResolver
import android.graphics.Color
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
    private const val MARGIN = 34f
    private const val ROW_HEIGHT = 30f
    private const val BLUE = 0xFF0C5C86.toInt()
    private const val BLUE_DARK = 0xFF09283A.toInt()
    private const val BLUE_LIGHT = 0xFFEAF6FB.toInt()
    private const val LINE = 0xFFD5E2E8.toInt()
    private const val TEXT = 0xFF17232B.toInt()
    private const val MUTED = 0xFF61727C.toInt()

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
        var page = newPage(document, pageNumber)
        var canvas = page.canvas
        var y = drawPageHeader(canvas, paint, header, strings)
        y = drawTableHeader(canvas, paint, y, strings)

        entries.forEachIndexed { index, entry ->
            if (y + ROW_HEIGHT > PAGE_HEIGHT - 92f) {
                drawFooter(canvas, paint, pageNumber, strings)
                document.finishPage(page)
                pageNumber += 1
                page = newPage(document, pageNumber)
                canvas = page.canvas
                y = drawPageHeader(canvas, paint, header, strings)
                y = drawTableHeader(canvas, paint, y, strings)
            }
            drawEntryRow(canvas, paint, y, entry, dateFormatter, index % 2 == 0)
            y += ROW_HEIGHT
        }

        if (y + 92f > PAGE_HEIGHT - 70f) {
            drawFooter(canvas, paint, pageNumber, strings)
            document.finishPage(page)
            pageNumber += 1
            page = newPage(document, pageNumber)
            canvas = page.canvas
            y = drawPageHeader(canvas, paint, header, strings)
        }

        drawSummary(canvas, paint, y + 18f, totalMinutes, entries.size, strings)
        drawFooter(canvas, paint, pageNumber, strings)
        document.finishPage(page)

        resolver.openOutputStream(uri)?.use(document::writeTo)
            ?: error("Could not open output stream")
        document.close()
    }.isSuccess

    private fun newPage(document: PdfDocument, pageNumber: Int): PdfDocument.Page =
        document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())

    private fun drawPageHeader(
        canvas: android.graphics.Canvas,
        paint: Paint,
        header: String,
        strings: PdfStrings
    ): Float {
        val defaultHeaders = setOf("Lista Czasu Pracy", "Work Time List", "Arbeidstidsliste")
        val title = header.trim().takeIf { it.isNotBlank() && it !in defaultHeaders } ?: strings.title

        paint.style = Paint.Style.FILL
        paint.color = BLUE_DARK
        canvas.drawRoundRect(MARGIN, 28f, PAGE_WIDTH - MARGIN, 100f, 10f, 10f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 21f
        paint.color = Color.WHITE
        canvas.drawText(ellipsize(title, 43), MARGIN + 18f, 61f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        paint.color = 0xFFBFE9FF.toInt()
        canvas.drawText(strings.generatedBy, MARGIN + 18f, 82f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        paint.color = 0xFF63D2FF.toInt()
        canvas.drawText("WOJTHOM APEX", PAGE_WIDTH - MARGIN - 112f, 82f, paint)
        return 122f
    }

    private fun drawTableHeader(
        canvas: android.graphics.Canvas,
        paint: Paint,
        top: Float,
        strings: PdfStrings
    ): Float {
        paint.style = Paint.Style.FILL
        paint.color = BLUE
        canvas.drawRoundRect(MARGIN, top, PAGE_WIDTH - MARGIN, top + ROW_HEIGHT, 5f, 5f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.5f
        paint.color = Color.WHITE
        drawCellText(canvas, paint, strings.date, 42f, top)
        drawCellText(canvas, paint, strings.client, 112f, top)
        drawCellText(canvas, paint, strings.start, 342f, top)
        drawCellText(canvas, paint, strings.end, 402f, top)
        drawCellText(canvas, paint, strings.duration, 463f, top)
        return top + ROW_HEIGHT
    }

    private fun drawEntryRow(
        canvas: android.graphics.Canvas,
        paint: Paint,
        top: Float,
        entry: TimeEntry,
        dateFormatter: DateTimeFormatter,
        striped: Boolean
    ) {
        if (striped) {
            paint.style = Paint.Style.FILL
            paint.color = BLUE_LIGHT
            canvas.drawRect(MARGIN, top, PAGE_WIDTH - MARGIN, top + ROW_HEIGHT, paint)
        }

        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = TEXT

        drawCellText(canvas, paint, entry.date?.format(dateFormatter) ?: "-", 42f, top)
        drawCellText(canvas, paint, ellipsize(entry.client.ifBlank { "-" }, 38), 112f, top)
        drawCellText(canvas, paint, entry.start, 342f, top)
        drawCellText(canvas, paint, entry.end, 402f, top)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = BLUE
        drawCellText(canvas, paint, "${entry.durationText} h", 463f, top)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.45f
        paint.color = LINE
        canvas.drawLine(MARGIN, top + ROW_HEIGHT, PAGE_WIDTH - MARGIN, top + ROW_HEIGHT, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawCellText(canvas: android.graphics.Canvas, paint: Paint, text: String, x: Float, top: Float) {
        canvas.drawText(text, x, top + 19f, paint)
    }

    private fun drawSummary(
        canvas: android.graphics.Canvas,
        paint: Paint,
        y: Float,
        totalMinutes: Int,
        entryCount: Int,
        strings: PdfStrings
    ) {
        val boxHeight = 62f
        paint.style = Paint.Style.FILL
        paint.color = BLUE_LIGHT
        canvas.drawRoundRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + boxHeight, 8f, 8f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = BLUE_DARK
        paint.textSize = 10f
        canvas.drawText(strings.total.uppercase(), MARGIN + 14f, y + 20f, paint)
        canvas.drawText(strings.entries.uppercase(), MARGIN + 250f, y + 20f, paint)

        paint.textSize = 18f
        paint.color = BLUE
        val total = "%d:%02d h".format(totalMinutes / 60, totalMinutes % 60)
        canvas.drawText(total, MARGIN + 14f, y + 46f, paint)
        canvas.drawText(entryCount.toString(), MARGIN + 250f, y + 46f, paint)
    }

    private fun drawFooter(
        canvas: android.graphics.Canvas,
        paint: Paint,
        pageNumber: Int,
        strings: PdfStrings
    ) {
        paint.style = Paint.Style.STROKE
        paint.color = LINE
        paint.strokeWidth = 0.7f
        canvas.drawLine(MARGIN, PAGE_HEIGHT - 48f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 48f, paint)

        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        paint.color = MUTED
        canvas.drawText(strings.generatedBy, MARGIN, PAGE_HEIGHT - 30f, paint)
        canvas.drawText("$pageNumber", PAGE_WIDTH - MARGIN - 12f, PAGE_HEIGHT - 30f, paint)
    }

    private fun ellipsize(text: String, maxLength: Int): String {
        if (text.length <= maxLength) return text
        return text.take(maxLength - 1) + "…"
    }
}
