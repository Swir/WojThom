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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PdfExporter {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 32f
    private const val ROW_HEIGHT = 31f

    private const val NAVY = 0xFF082A3C.toInt()
    private const val NAVY_2 = 0xFF103B50.toInt()
    private const val BLUE = 0xFF0879A6.toInt()
    private const val CYAN = 0xFF53CCF4.toInt()
    private const val ICE = 0xFFF1F9FC.toInt()
    private const val STRIPE = 0xFFE8F5FA.toInt()
    private const val LINE = 0xFFD5E4EA.toInt()
    private const val TEXT = 0xFF18272F.toInt()
    private const val MUTED = 0xFF637780.toInt()
    private const val WHITE_SOFT = 0xFFDDF5FF.toInt()

    private data class ReportLabels(
        val report: String,
        val period: String,
        val generated: String,
        val number: String,
        val workDays: String,
        val averageDay: String,
        val page: String
    )

    fun write(
        resolver: ContentResolver,
        uri: Uri,
        header: String,
        entries: List<TimeEntry>,
        language: AppLanguage
    ): Boolean = runCatching {
        val strings = pdfStrings(language)
        val labels = reportLabels(language)
        val document = PdfDocument()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val generatedFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val generatedAt = LocalDateTime.now().format(generatedFormatter)
        val totalMinutes = entries.sumOf { it.minutes }
        val workDays = entries.mapNotNull { it.date }.distinct().size
        val averageMinutes = if (workDays > 0) totalMinutes / workDays else 0
        val period = periodText(entries, dateFormatter)

        var pageNumber = 1
        var page = newPage(document, pageNumber)
        var canvas = page.canvas
        var y = drawPageHeader(
            canvas = canvas,
            paint = paint,
            header = header,
            strings = strings,
            labels = labels,
            period = period,
            generatedAt = generatedAt
        )
        y = drawTableHeader(canvas, paint, y, strings, labels)

        entries.forEachIndexed { index, entry ->
            if (y + ROW_HEIGHT > PAGE_HEIGHT - 88f) {
                drawFooter(canvas, paint, pageNumber, strings, labels)
                document.finishPage(page)
                pageNumber += 1
                page = newPage(document, pageNumber)
                canvas = page.canvas
                y = drawPageHeader(
                    canvas = canvas,
                    paint = paint,
                    header = header,
                    strings = strings,
                    labels = labels,
                    period = period,
                    generatedAt = generatedAt
                )
                y = drawTableHeader(canvas, paint, y, strings, labels)
            }

            drawEntryRow(
                canvas = canvas,
                paint = paint,
                top = y,
                entry = entry,
                rowNumber = index + 1,
                dateFormatter = dateFormatter,
                striped = index % 2 == 0
            )
            y += ROW_HEIGHT
        }

        val summaryHeight = 94f
        if (y + summaryHeight > PAGE_HEIGHT - 68f) {
            drawFooter(canvas, paint, pageNumber, strings, labels)
            document.finishPage(page)
            pageNumber += 1
            page = newPage(document, pageNumber)
            canvas = page.canvas
            y = drawPageHeader(
                canvas = canvas,
                paint = paint,
                header = header,
                strings = strings,
                labels = labels,
                period = period,
                generatedAt = generatedAt
            )
        }

        drawSummary(
            canvas = canvas,
            paint = paint,
            y = y + 16f,
            totalMinutes = totalMinutes,
            entryCount = entries.size,
            workDays = workDays,
            averageMinutes = averageMinutes,
            strings = strings,
            labels = labels
        )
        drawFooter(canvas, paint, pageNumber, strings, labels)
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
        strings: PdfStrings,
        labels: ReportLabels,
        period: String,
        generatedAt: String
    ): Float {
        val defaultHeaders = setOf("Lista Czasu Pracy", "Work Time List", "Arbeidstidsliste")
        val title = header.trim().takeIf { it.isNotBlank() && it !in defaultHeaders } ?: strings.title

        paint.style = Paint.Style.FILL
        paint.color = NAVY
        canvas.drawRoundRect(MARGIN, 24f, PAGE_WIDTH - MARGIN, 108f, 12f, 12f, paint)

        paint.color = CYAN
        canvas.drawRoundRect(MARGIN + 14f, 39f, MARGIN + 18f, 92f, 2f, 2f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.5f
        paint.color = CYAN
        canvas.drawText("WOJTHOM APEX", MARGIN + 29f, 45f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 20f
        paint.color = Color.WHITE
        canvas.drawText(ellipsize(title, 40), MARGIN + 29f, 70f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = WHITE_SOFT
        canvas.drawText(labels.report, MARGIN + 29f, 91f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8f
        paint.color = CYAN
        canvas.drawText(labels.period.uppercase(), 383f, 47f, paint)
        canvas.drawText(labels.generated.uppercase(), 383f, 75f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        paint.color = Color.WHITE
        canvas.drawText(ellipsize(period, 23), 383f, 60f, paint)
        canvas.drawText(generatedAt, 383f, 88f, paint)

        paint.color = ICE
        canvas.drawRoundRect(MARGIN, 118f, PAGE_WIDTH - MARGIN, 142f, 6f, 6f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = MUTED
        canvas.drawText(strings.generatedBy, MARGIN + 12f, 134f, paint)

        return 154f
    }

    private fun drawTableHeader(
        canvas: android.graphics.Canvas,
        paint: Paint,
        top: Float,
        strings: PdfStrings,
        labels: ReportLabels
    ): Float {
        paint.style = Paint.Style.FILL
        paint.color = BLUE
        canvas.drawRoundRect(MARGIN, top, PAGE_WIDTH - MARGIN, top + ROW_HEIGHT, 6f, 6f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.2f
        paint.color = Color.WHITE

        drawCellText(canvas, paint, labels.number, 40f, top)
        drawCellText(canvas, paint, strings.date, 66f, top)
        drawCellText(canvas, paint, strings.client, 130f, top)
        drawCellText(canvas, paint, strings.start, 354f, top)
        drawCellText(canvas, paint, strings.end, 413f, top)
        drawCellText(canvas, paint, strings.duration, 470f, top)
        return top + ROW_HEIGHT
    }

    private fun drawEntryRow(
        canvas: android.graphics.Canvas,
        paint: Paint,
        top: Float,
        entry: TimeEntry,
        rowNumber: Int,
        dateFormatter: DateTimeFormatter,
        striped: Boolean
    ) {
        paint.style = Paint.Style.FILL
        paint.color = if (striped) STRIPE else Color.WHITE
        canvas.drawRect(MARGIN, top, PAGE_WIDTH - MARGIN, top + ROW_HEIGHT, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.4f
        paint.color = MUTED
        drawCellText(canvas, paint, rowNumber.toString(), 40f, top)

        paint.color = TEXT
        drawCellText(canvas, paint, entry.date?.format(dateFormatter) ?: "-", 66f, top)
        drawCellText(canvas, paint, ellipsize(entry.client.ifBlank { "-" }, 36), 130f, top)
        drawCellText(canvas, paint, entry.start, 354f, top)
        drawCellText(canvas, paint, entry.end, 413f, top)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = BLUE
        drawCellText(canvas, paint, "${entry.durationText} h", 470f, top)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.45f
        paint.color = LINE
        canvas.drawLine(MARGIN, top + ROW_HEIGHT, PAGE_WIDTH - MARGIN, top + ROW_HEIGHT, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawCellText(
        canvas: android.graphics.Canvas,
        paint: Paint,
        text: String,
        x: Float,
        top: Float
    ) {
        canvas.drawText(text, x, top + 20f, paint)
    }

    private fun drawSummary(
        canvas: android.graphics.Canvas,
        paint: Paint,
        y: Float,
        totalMinutes: Int,
        entryCount: Int,
        workDays: Int,
        averageMinutes: Int,
        strings: PdfStrings,
        labels: ReportLabels
    ) {
        paint.style = Paint.Style.FILL
        paint.color = NAVY_2
        canvas.drawRoundRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 76f, 10f, 10f, paint)

        val gap = 7f
        val innerLeft = MARGIN + 8f
        val innerRight = PAGE_WIDTH - MARGIN - 8f
        val cardWidth = (innerRight - innerLeft - gap * 3f) / 4f

        val values = listOf(
            strings.total to formatDuration(totalMinutes),
            strings.entries to entryCount.toString(),
            labels.workDays to workDays.toString(),
            labels.averageDay to formatDuration(averageMinutes)
        )

        values.forEachIndexed { index, (label, value) ->
            val left = innerLeft + index * (cardWidth + gap)
            paint.color = if (index == 0) 0xFF0E6F96.toInt() else 0xFF17485E.toInt()
            canvas.drawRoundRect(left, y + 9f, left + cardWidth, y + 67f, 7f, 7f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 7.2f
            paint.color = WHITE_SOFT
            canvas.drawText(ellipsize(label.uppercase(), 18), left + 9f, y + 28f, paint)

            paint.textSize = if (index == 0) 16f else 14f
            paint.color = Color.WHITE
            canvas.drawText(value, left + 9f, y + 52f, paint)
        }
    }

    private fun drawFooter(
        canvas: android.graphics.Canvas,
        paint: Paint,
        pageNumber: Int,
        strings: PdfStrings,
        labels: ReportLabels
    ) {
        paint.style = Paint.Style.STROKE
        paint.color = LINE
        paint.strokeWidth = 0.7f
        canvas.drawLine(MARGIN, PAGE_HEIGHT - 47f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 47f, paint)

        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 7.8f
        paint.color = MUTED
        canvas.drawText(strings.generatedBy, MARGIN, PAGE_HEIGHT - 29f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = BLUE
        val pageText = "${labels.page} $pageNumber"
        canvas.drawText(pageText, PAGE_WIDTH - MARGIN - paint.measureText(pageText), PAGE_HEIGHT - 29f, paint)
    }

    private fun periodText(entries: List<TimeEntry>, formatter: DateTimeFormatter): String {
        val dates = entries.mapNotNull { it.date }
        if (dates.isEmpty()) return "-"
        val start: LocalDate = dates.minOrNull() ?: return "-"
        val end: LocalDate = dates.maxOrNull() ?: start
        return if (start == end) start.format(formatter) else "${start.format(formatter)} – ${end.format(formatter)}"
    }

    private fun formatDuration(minutes: Int): String = "%d:%02d h".format(minutes / 60, minutes % 60)

    private fun reportLabels(language: AppLanguage): ReportLabels = when (language) {
        AppLanguage.POLISH -> ReportLabels(
            report = "Raport czasu pracy",
            period = "Okres",
            generated = "Wygenerowano",
            number = "Lp.",
            workDays = "Dni pracy",
            averageDay = "Średnio / dzień",
            page = "Strona"
        )

        AppLanguage.ENGLISH -> ReportLabels(
            report = "Work time report",
            period = "Period",
            generated = "Generated",
            number = "No.",
            workDays = "Work days",
            averageDay = "Avg / day",
            page = "Page"
        )

        AppLanguage.NORWEGIAN -> ReportLabels(
            report = "Arbeidstidsrapport",
            period = "Periode",
            generated = "Generert",
            number = "Nr.",
            workDays = "Arbeidsdager",
            averageDay = "Snitt / dag",
            page = "Side"
        )
    }

    private fun ellipsize(text: String, maxLength: Int): String {
        if (text.length <= maxLength) return text
        return text.take(maxLength - 1) + "…"
    }
}
