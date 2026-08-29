package com.example.presentation.editor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class PdfExporter {

    fun exportToPdf(context: Context, state: EditorState): Uri? {
        val pdfDocument = PdfDocument()

        // Standard A4 dimensions in PDF points (72 points per inch)
        val pageWidth = 595
        val pageHeight = 842

        // Create Page 1
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
        }

        var currentY = 60f
        val marginX = 40f
        val contentWidth = pageWidth - (marginX * 2)

        fun drawHeaderAndFooter(canvas: Canvas, currentPageNum: Int) {
            // Draw Background Color
            if (state.pageColor.toArgb() != androidx.compose.ui.graphics.Color.White.toArgb()) {
                val bgPaint = Paint().apply {
                    color = state.pageColor.toArgb()
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)
            }

            // Draw Watermark
            if (state.watermarkText.isNotEmpty()) {
                val wmPaint = Paint().apply {
                    color = AndroidColor.argb(30, 150, 150, 150)
                    textSize = 45f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.save()
                canvas.rotate(-45f, pageWidth / 2f, pageHeight / 2f)
                canvas.drawText(state.watermarkText, pageWidth / 2f, pageHeight / 2f, wmPaint)
                canvas.restore()
            }

            // Draw Header
            if (state.headerText.text.isNotEmpty()) {
                val headerPaint = Paint().apply {
                    color = AndroidColor.DKGRAY
                    textSize = 10f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    textAlign = if (state.isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
                }
                val headerX = if (state.isRtl) pageWidth - marginX else marginX
                canvas.drawText(state.headerText.text, headerX, 30f, headerPaint)
            }

            // Draw Footer & Page Number
            val footerX = if (state.isRtl) marginX else pageWidth - marginX
            val footerPaint = Paint().apply {
                color = AndroidColor.GRAY
                textSize = 10f
                textAlign = if (state.isRtl) Paint.Align.LEFT else Paint.Align.RIGHT
            }
            val footerStr = if (state.footerText.text.isNotEmpty()) {
                "${state.footerText.text} | $currentPageNum"
            } else {
                "$currentPageNum"
            }
            canvas.drawText(footerStr, footerX, pageHeight - 25f, footerPaint)

            // Draw Top/Bottom Border Lines
            val borderLinePaint = Paint().apply {
                color = AndroidColor.LTGRAY
                strokeWidth = 1f
            }
            canvas.drawLine(marginX, 40f, pageWidth - marginX, 40f, borderLinePaint)
            canvas.drawLine(marginX, pageHeight - 40f, pageWidth - marginX, pageHeight - 40f, borderLinePaint)
        }

        fun checkPageBreak(neededHeight: Float) {
            if (currentY + neededHeight > pageHeight - 60f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = 60f
                drawHeaderAndFooter(canvas, pageNumber)
            }
        }

        // Initial Header/Footer on Page 1
        drawHeaderAndFooter(canvas, 1)

        // Draw Document Title
        checkPageBreak(30f)
        val titlePaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = if (state.isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        }
        val titleX = if (state.isRtl) pageWidth - marginX else marginX
        canvas.drawText(state.documentTitle, titleX, currentY + 18f, titlePaint)
        currentY += 35f

        // Draw Blocks
        state.blocks.forEach { block ->
            when (block) {
                is TextBlock -> {
                    val textStr = block.text.text
                    if (textStr.isNotEmpty()) {
                        val textPaint = Paint().apply {
                            color = state.textColor.toArgb()
                            textSize = state.fontSize.toFloat().coerceIn(10f, 24f)
                            typeface = Typeface.create(
                                when (state.fontFamily.lowercase()) {
                                    "times new roman", "georgia", "garamond", "cambria", "book antiqua", "palatino linotype", "aptos serif" -> Typeface.SERIF
                                    "courier new", "consolas", "lucida console" -> Typeface.MONOSPACE
                                    else -> Typeface.SANS_SERIF
                                },
                                if (state.isBold && state.isItalic) Typeface.BOLD_ITALIC
                                else if (state.isBold) Typeface.BOLD
                                else if (state.isItalic) Typeface.ITALIC
                                else Typeface.NORMAL
                            )
                            textAlign = when (block.alignment) {
                                TextAlignment.LEFT -> Paint.Align.LEFT
                                TextAlignment.CENTER -> Paint.Align.CENTER
                                TextAlignment.RIGHT -> Paint.Align.RIGHT
                                TextAlignment.JUSTIFY -> if (state.isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
                            }
                        }

                        // Split into lines manually for simple wrap
                        val words = textStr.split("\n")
                        words.forEach { paragraph ->
                            checkPageBreak(20f)
                            val textX = when (block.alignment) {
                                TextAlignment.LEFT -> marginX
                                TextAlignment.CENTER -> pageWidth / 2f
                                TextAlignment.RIGHT -> pageWidth - marginX
                                TextAlignment.JUSTIFY -> if (state.isRtl) pageWidth - marginX else marginX
                            }
                            canvas.drawText(paragraph, textX, currentY + 12f, textPaint)
                            currentY += textPaint.textSize * state.lineSpacing + 4f
                        }
                    } else {
                        currentY += 15f
                    }
                }

                is BannerBlock -> {
                    checkPageBreak(50f)
                    val bgRect = RectF(marginX, currentY, pageWidth - marginX, currentY + 45f)
                    val bannerBgPaint = Paint().apply {
                        color = block.backgroundColor.toArgb()
                        style = Paint.Style.FILL
                    }
                    canvas.drawRoundRect(bgRect, 6f, 6f, bannerBgPaint)

                    val bannerTitlePaint = Paint().apply {
                        color = block.textColor.toArgb()
                        textSize = 14f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText(block.title, pageWidth / 2f, currentY + 26f, bannerTitlePaint)
                    currentY += 55f
                }

                is CalloutBlock -> {
                    checkPageBreak(55f)
                    val bgRect = RectF(marginX, currentY, pageWidth - marginX, currentY + 50f)
                    val calloutBgPaint = Paint().apply {
                        color = block.backgroundColor.toArgb()
                        style = Paint.Style.FILL
                    }
                    val calloutBorderPaint = Paint().apply {
                        color = block.borderColor.toArgb()
                        style = Paint.Style.STROKE
                        strokeWidth = 3f
                    }
                    canvas.drawRoundRect(bgRect, 8f, 8f, calloutBgPaint)
                    canvas.drawRoundRect(bgRect, 8f, 8f, calloutBorderPaint)

                    val calloutTextPaint = Paint().apply {
                        color = block.textColor.toArgb()
                        textSize = 12f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = if (state.isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
                    }
                    val cX = if (state.isRtl) pageWidth - marginX - 15f else marginX + 15f
                    canvas.drawText(block.title.ifEmpty { block.text.text }, cX, currentY + 30f, calloutTextPaint)
                    currentY += 60f
                }

                is DividerBlock -> {
                    checkPageBreak(15f)
                    val divPaint = Paint().apply {
                        color = block.color.toArgb()
                        strokeWidth = block.thicknessDp
                    }
                    canvas.drawLine(marginX, currentY + 8f, pageWidth - marginX, currentY + 8f, divPaint)
                    currentY += 16f
                }

                is PageBreakBlock -> {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = 60f
                    drawHeaderAndFooter(canvas, pageNumber)
                }

                is TableBlock -> {
                    checkPageBreak(60f)
                    val cellWidth = contentWidth / block.cols
                    val rowHeight = 25f
                    val tablePaint = Paint().apply {
                        color = AndroidColor.GRAY
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                    }
                    val textPaint = Paint().apply {
                        color = AndroidColor.BLACK
                        textSize = 10f
                    }

                    for (r in 0 until block.rows) {
                        checkPageBreak(rowHeight)
                        for (c in 0 until block.cols) {
                            val cellX = marginX + (c * cellWidth)
                            val cellY = currentY
                            canvas.drawRect(cellX, cellY, cellX + cellWidth, cellY + rowHeight, tablePaint)

                            val cellData = block.cells["${r}_${c}"]
                            val cellText = cellData?.textBlocks?.firstOrNull()?.text?.text ?: ""
                            canvas.drawText(cellText, cellX + 5f, cellY + 16f, textPaint)
                        }
                        currentY += rowHeight
                    }
                    currentY += 10f
                }

                else -> {}
            }
        }

        pdfDocument.finishPage(page)

        // Write to File
        val pdfFile = File(context.cacheDir, "${state.documentTitle.replace(" ", "_")}.pdf")
        return try {
            val fileOutputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fileOutputStream)
            pdfDocument.close()
            fileOutputStream.close()
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
