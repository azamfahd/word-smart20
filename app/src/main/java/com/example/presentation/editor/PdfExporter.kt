package com.example.presentation.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class PdfExporter {

    fun exportToPdf(context: Context, state: EditorState): Uri? {
        val pdfDocument = PdfDocument()

        // Dimensions in PDF points (72 points per inch)
        val (baseWidth, baseHeight) = when (state.pageSize) {
            PageSize.A3 -> 842 to 1190
            PageSize.A4 -> 595 to 842
            PageSize.A5 -> 420 to 595
            PageSize.LETTER -> 612 to 792
            PageSize.LEGAL -> 612 to 1008
        }

        val pageWidth = if (state.pageOrientation == PageOrientation.LANDSCAPE) baseHeight else baseWidth
        val pageHeight = if (state.pageOrientation == PageOrientation.LANDSCAPE) baseWidth else baseHeight

        val marginX = when (state.pageMargin) {
            PageMargin.NORMAL -> 72f
            PageMargin.NARROW -> 36f
            PageMargin.MODERATE -> 54f
            PageMargin.WIDE -> 108f
        }
        val marginY = when (state.pageMargin) {
            PageMargin.NORMAL -> 72f
            PageMargin.NARROW -> 36f
            PageMargin.MODERATE -> 72f
            PageMargin.WIDE -> 72f
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var currentY = marginY
        val contentWidth = pageWidth - (marginX * 2)

        fun drawHeaderAndFooter(canvas: Canvas, currentPageNum: Int) {
            if (state.pageColor.toArgb() != androidx.compose.ui.graphics.Color.White.toArgb()) {
                val bgPaint = Paint().apply {
                    color = state.pageColor.toArgb()
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)
            }

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

            if (state.headerText.text.isNotEmpty()) {
                val headerPaint = Paint().apply {
                    color = AndroidColor.DKGRAY
                    textSize = 10f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    textAlign = if (state.isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
                }
                val headerX = if (state.isRtl) pageWidth - marginX else marginX
                canvas.drawText(state.headerText.text, headerX, marginY / 2f, headerPaint)
            }

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
            canvas.drawText(footerStr, footerX, pageHeight - (marginY / 2f), footerPaint)

            val borderLinePaint = Paint().apply {
                color = AndroidColor.LTGRAY
                strokeWidth = 1f
            }
            canvas.drawLine(marginX, marginY - 10f, pageWidth - marginX, marginY - 10f, borderLinePaint)
            canvas.drawLine(marginX, pageHeight - marginY + 10f, pageWidth - marginX, pageHeight - marginY + 10f, borderLinePaint)
        }

        fun checkPageBreak(neededHeight: Float) {
            if (currentY + neededHeight > pageHeight - marginY) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = marginY
                drawHeaderAndFooter(canvas, pageNumber)
            }
        }

        drawHeaderAndFooter(canvas, 1)

        checkPageBreak(30f)
        val titlePaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = if (state.isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        }
        val titleX = if (state.isRtl) pageWidth - marginX else marginX
        canvas.drawText(state.documentTitle, titleX, currentY + 18f, titlePaint)
        currentY += 40f

        state.blocks.forEach { block ->
            when (block) {
                is TextBlock -> {
                    val annotatedString = block.text.annotatedString
                    val textStr = annotatedString.text
                    if (textStr.isNotEmpty()) {
                        val baseFontName = state.fontFamily
                        val baseFontSize = state.fontSize.toFloat().coerceIn(10f, 48f)

                        val textPaint = TextPaint().apply {
                            color = AndroidColor.BLACK
                            textSize = baseFontSize
                            isAntiAlias = true
                            typeface = com.example.presentation.editor.font.FontEngine.getNativeTypeface(
                                fontName = baseFontName,
                                isBold = false,
                                isItalic = false
                            )
                        }

                        val alignment = when (block.alignment) {
                            TextAlignment.LEFT -> if (block.isRtl || state.isRtl) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL
                            TextAlignment.CENTER -> Layout.Alignment.ALIGN_CENTER
                            TextAlignment.RIGHT -> if (block.isRtl || state.isRtl) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_OPPOSITE
                            TextAlignment.JUSTIFY -> if (block.isRtl || state.isRtl) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL
                        }

                        val paragraphs = textStr.split("\n")
                        var currentParaStart = 0

                        paragraphs.forEach { paragraphText ->
                            if (paragraphText.isEmpty()) {
                                currentY += baseFontSize * block.lineSpacing
                                checkPageBreak(baseFontSize)
                                currentParaStart += 1 // \n character
                                return@forEach
                            }

                            val paraEnd = currentParaStart + paragraphText.length
                            val spannable = SpannableStringBuilder(paragraphText)

                            // Apply span styles from AnnotatedString to SpannableStringBuilder
                            annotatedString.spanStyles.forEach { span ->
                                val overlapStart = maxOf(currentParaStart, span.start)
                                val overlapEnd = minOf(paraEnd, span.end)
                                if (overlapStart < overlapEnd) {
                                    val relStart = overlapStart - currentParaStart
                                    val relEnd = overlapEnd - currentParaStart
                                    val style = span.item

                                    if (style.fontWeight == FontWeight.Bold && style.fontStyle == FontStyle.Italic) {
                                        spannable.setSpan(StyleSpan(Typeface.BOLD_ITALIC), relStart, relEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    } else if (style.fontWeight == FontWeight.Bold) {
                                        spannable.setSpan(StyleSpan(Typeface.BOLD), relStart, relEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    } else if (style.fontStyle == FontStyle.Italic) {
                                        spannable.setSpan(StyleSpan(Typeface.ITALIC), relStart, relEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    }

                                    if (style.fontSize.value > 0) {
                                        spannable.setSpan(AbsoluteSizeSpan(style.fontSize.value.toInt(), true), relStart, relEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    }

                                    if (style.color != androidx.compose.ui.graphics.Color.Unspecified) {
                                        spannable.setSpan(ForegroundColorSpan(style.color.toArgb()), relStart, relEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    }

                                    if (style.textDecoration == TextDecoration.Underline || style.textDecoration == TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))) {
                                        spannable.setSpan(UnderlineSpan(), relStart, relEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    }
                                    if (style.textDecoration == TextDecoration.LineThrough || style.textDecoration == TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))) {
                                        spannable.setSpan(StrikethroughSpan(), relStart, relEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    }
                                }
                            }

                            val staticLayout = StaticLayout.Builder.obtain(spannable, 0, spannable.length, textPaint, contentWidth.toInt())
                                .setAlignment(alignment)
                                .setLineSpacing(0f, block.lineSpacing.coerceAtLeast(1.0f))
                                .setIncludePad(false)
                                .build()

                            checkPageBreak(staticLayout.height.toFloat())

                            canvas.save()
                            canvas.translate(marginX, currentY)
                            staticLayout.draw(canvas)
                            canvas.restore()

                            currentY += staticLayout.height + 6f
                            currentParaStart = paraEnd + 1 // +1 for newline
                        }
                    } else {
                        currentY += 12f
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
                    currentY = marginY
                    drawHeaderAndFooter(canvas, pageNumber)
                }

                is TableBlock -> {
                    checkPageBreak(60f)
                    val cellWidth = contentWidth / block.cols.coerceAtLeast(1)
                    val tableBorderPaint = Paint().apply {
                        color = AndroidColor.GRAY
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                    }
                    val defaultTextPaint = TextPaint().apply {
                        color = AndroidColor.BLACK
                        textSize = 10f
                    }

                    for (r in 0 until block.rows) {
                        var maxRowHeight = 25f
                        val staticLayouts = mutableMapOf<Int, StaticLayout>()

                        for (c in 0 until block.cols) {
                            val cellData = block.cells["${r}_${c}"]
                            val firstTextBlock = cellData?.textBlocks?.firstOrNull()
                            val cellText = firstTextBlock?.text?.text ?: ""

                            if (cellText.isNotEmpty()) {
                                val layout = StaticLayout.Builder.obtain(cellText, 0, cellText.length, defaultTextPaint, cellWidth.toInt() - 10)
                                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                                    .build()
                                staticLayouts[c] = layout
                                if (layout.height + 10f > maxRowHeight) maxRowHeight = layout.height + 10f
                            }
                        }

                        checkPageBreak(maxRowHeight)

                        for (c in 0 until block.cols) {
                            val cellX = marginX + (c * cellWidth)
                            val cellY = currentY
                            val cellData = block.cells["${r}_${c}"]

                            if (cellData != null && cellData.backgroundColor != androidx.compose.ui.graphics.Color.Transparent) {
                                val cellBgPaint = Paint().apply {
                                    color = cellData.backgroundColor.toArgb()
                                    style = Paint.Style.FILL
                                }
                                canvas.drawRect(cellX, cellY, cellX + cellWidth, cellY + maxRowHeight, cellBgPaint)
                            }

                            canvas.drawRect(cellX, cellY, cellX + cellWidth, cellY + maxRowHeight, tableBorderPaint)

                            val layout = staticLayouts[c]
                            if (layout != null) {
                                canvas.save()
                                canvas.translate(cellX + 5f, cellY + 5f)
                                layout.draw(canvas)
                                canvas.restore()
                            }
                        }
                        currentY += maxRowHeight
                    }
                    currentY += 10f
                }

                is ImageBlock -> {
                    if (block.imageData != null) {
                        try {
                            val bmp = BitmapFactory.decodeByteArray(block.imageData, 0, block.imageData.size)
                            if (bmp != null) {
                                val targetWidth = (contentWidth * 0.8f).coerceAtMost(bmp.width.toFloat())
                                val aspectRatio = bmp.height.toFloat() / bmp.width.toFloat()
                                val targetHeight = (targetWidth * aspectRatio).coerceAtMost(300f)

                                checkPageBreak(targetHeight + 20f)

                                val destRect = RectF(
                                    marginX + (contentWidth - targetWidth) / 2f,
                                    currentY,
                                    marginX + (contentWidth - targetWidth) / 2f + targetWidth,
                                    currentY + targetHeight
                                )
                                canvas.drawBitmap(bmp, null as Rect?, destRect, null as Paint?)
                                currentY += targetHeight + 15f
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                else -> {}
            }
        }

        pdfDocument.finishPage(page)

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

