package com.example.presentation.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.DashPathEffect
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
import android.text.style.BackgroundColorSpan
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

        fun drawPageBackgroundAndChrome(canvas: Canvas, currentPageNum: Int) {
            // 1. Page Background Color
            if (state.pageColor.toArgb() != androidx.compose.ui.graphics.Color.White.toArgb()) {
                val bgPaint = Paint().apply {
                    color = state.pageColor.toArgb()
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)
            }

            // 2. Page Border (if configured)
            if (state.pageBorder.setting != BorderSetting.NONE) {
                val borderPaint = Paint().apply {
                    color = state.pageBorder.color.toArgb()
                    style = Paint.Style.STROKE
                    strokeWidth = state.pageBorder.widthPt.coerceAtLeast(0.75f)
                    when (state.pageBorder.style) {
                        BorderStyle.DASHED -> pathEffect = DashPathEffect(floatArrayOf(8f, 4f), 0f)
                        BorderStyle.DOTTED -> pathEffect = DashPathEffect(floatArrayOf(3f, 3f), 0f)
                        else -> {}
                    }
                }
                val inset = 18f
                canvas.drawRect(
                    marginX - inset,
                    marginY - inset,
                    pageWidth - marginX + inset,
                    pageHeight - marginY + inset,
                    borderPaint
                )
            }

            // 3. Watermark
            if (state.watermarkText.isNotEmpty()) {
                val wmPaint = Paint().apply {
                    color = AndroidColor.argb(35, 150, 150, 150)
                    textSize = 46f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.save()
                canvas.rotate(-45f, pageWidth / 2f, pageHeight / 2f)
                canvas.drawText(state.watermarkText, pageWidth / 2f, pageHeight / 2f, wmPaint)
                canvas.restore()
            }

            // 4. Header
            if (state.headerText.text.isNotEmpty()) {
                val headerPaint = Paint().apply {
                    color = AndroidColor.DKGRAY
                    textSize = 9.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    textAlign = if (state.isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
                }
                val headerX = if (state.isRtl) pageWidth - marginX else marginX
                canvas.drawText(state.headerText.text, headerX, marginY / 2f, headerPaint)

                val hLinePaint = Paint().apply {
                    color = AndroidColor.LTGRAY
                    strokeWidth = 0.75f
                }
                canvas.drawLine(marginX, marginY - 12f, pageWidth - marginX, marginY - 12f, hLinePaint)
            }

            // 5. Footer
            val footerX = if (state.isRtl) marginX else pageWidth - marginX
            val footerPaint = Paint().apply {
                color = AndroidColor.GRAY
                textSize = 9.5f
                textAlign = if (state.isRtl) Paint.Align.LEFT else Paint.Align.RIGHT
            }
            val footerStr = if (state.footerText.text.isNotEmpty()) {
                "${state.footerText.text} | $currentPageNum"
            } else {
                "$currentPageNum"
            }
            canvas.drawText(footerStr, footerX, pageHeight - (marginY / 2f), footerPaint)

            val fLinePaint = Paint().apply {
                color = AndroidColor.LTGRAY
                strokeWidth = 0.75f
            }
            canvas.drawLine(marginX, pageHeight - marginY + 12f, pageWidth - marginX, pageHeight - marginY + 12f, fLinePaint)
        }

        fun checkPageBreak(neededHeight: Float) {
            if (currentY + neededHeight > pageHeight - marginY) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = marginY
                drawPageBackgroundAndChrome(canvas, pageNumber)
            }
        }

        drawPageBackgroundAndChrome(canvas, 1)

        // Render document blocks
        state.blocks.forEach { block ->
            when (block) {
                is TextBlock -> {
                    val annotatedString = block.text.annotatedString
                    val textStr = annotatedString.text

                    currentY += block.spaceBeforePt

                    val bulletPrefix = if (block.isBulletedList) {
                        when (block.bulletShape) {
                            BulletShape.DISC -> "• "
                            BulletShape.CIRCLE -> "○ "
                            BulletShape.SQUARE -> "■ "
                            BulletShape.HOLLOW_SQUARE -> "□ "
                            BulletShape.CHECKMARK -> "✓ "
                            BulletShape.ARROW -> "➢ "
                            BulletShape.STAR -> "★ "
                            BulletShape.FLORAL -> "❖ "
                        }
                    } else ""

                    if (textStr.isNotEmpty() || bulletPrefix.isNotEmpty()) {
                        val baseFontName = block.fontFamily.ifEmpty { state.fontFamily }
                        val baseFontSize = if (block.fontSize > 0) block.fontSize.toFloat() else state.fontSize.toFloat().coerceIn(9f, 48f)

                        val isBlockRtl = block.isRtl || state.isRtl
                        val indentPx = block.indentLevel * 18f
                        val availableWidth = (contentWidth - indentPx).coerceAtLeast(60f)

                        val textPaint = TextPaint().apply {
                            color = block.textColor.toArgb()
                            textSize = baseFontSize
                            isAntiAlias = true
                            typeface = com.example.presentation.editor.font.FontEngine.getNativeTypeface(
                                fontName = baseFontName,
                                isBold = block.isBold,
                                isItalic = block.isItalic
                            )
                        }

                        val alignment = when (block.alignment) {
                            TextAlignment.LEFT -> if (isBlockRtl) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL
                            TextAlignment.CENTER -> Layout.Alignment.ALIGN_CENTER
                            TextAlignment.RIGHT -> if (isBlockRtl) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_OPPOSITE
                            TextAlignment.JUSTIFY -> if (isBlockRtl) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL
                        }

                        val paragraphs = textStr.split("\n")
                        var currentParaStart = 0

                        paragraphs.forEachIndexed { pIdx, paragraphText ->
                            val fullParaText = if (pIdx == 0 && bulletPrefix.isNotEmpty()) {
                                bulletPrefix + paragraphText
                            } else {
                                paragraphText
                            }

                            if (fullParaText.isEmpty()) {
                                currentY += baseFontSize * block.lineSpacing
                                checkPageBreak(baseFontSize)
                                currentParaStart += 1
                                return@forEachIndexed
                            }

                            val paraEnd = currentParaStart + paragraphText.length
                            val prefixLen = if (pIdx == 0) bulletPrefix.length else 0
                            val spannable = SpannableStringBuilder(fullParaText)

                            // Apply span styles
                            annotatedString.spanStyles.forEach { span ->
                                val overlapStart = maxOf(currentParaStart, span.start)
                                val overlapEnd = minOf(paraEnd, span.end)
                                if (overlapStart < overlapEnd) {
                                    val relStart = (overlapStart - currentParaStart) + prefixLen
                                    val relEnd = (overlapEnd - currentParaStart) + prefixLen
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

                                    if (style.background != androidx.compose.ui.graphics.Color.Transparent && style.background != androidx.compose.ui.graphics.Color.Unspecified) {
                                        spannable.setSpan(BackgroundColorSpan(style.background.toArgb()), relStart, relEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    }

                                    if (style.textDecoration == TextDecoration.Underline || style.textDecoration == TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))) {
                                        spannable.setSpan(UnderlineSpan(), relStart, relEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    }
                                    if (style.textDecoration == TextDecoration.LineThrough || style.textDecoration == TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))) {
                                        spannable.setSpan(StrikethroughSpan(), relStart, relEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    }
                                }
                            }

                            val staticLayout = StaticLayout.Builder.obtain(spannable, 0, spannable.length, textPaint, availableWidth.toInt())
                                .setAlignment(alignment)
                                .setLineSpacing(0f, block.lineSpacing.coerceAtLeast(1.0f))
                                .setIncludePad(false)
                                .build()

                            checkPageBreak(staticLayout.height.toFloat() + 4f)

                            val drawX = if (isBlockRtl) marginX else marginX + indentPx

                            // Paragraph Shading
                            if (block.paragraphShadingColor != null) {
                                val shdPaint = Paint().apply {
                                    color = block.paragraphShadingColor.toArgb()
                                    style = Paint.Style.FILL
                                }
                                canvas.drawRect(
                                    drawX - 4f,
                                    currentY - 2f,
                                    drawX + availableWidth + 4f,
                                    currentY + staticLayout.height + 2f,
                                    shdPaint
                                )
                            }

                            // Paragraph Border
                            if (block.paragraphBorder != ParagraphBorder.NONE) {
                                val pbPaint = Paint().apply {
                                    color = AndroidColor.DKGRAY
                                    strokeWidth = 1f
                                }
                                val pLeft = drawX - 4f
                                val pRight = drawX + availableWidth + 4f
                                val pTop = currentY - 2f
                                val pBottom = currentY + staticLayout.height + 2f

                                when (block.paragraphBorder) {
                                    ParagraphBorder.ALL, ParagraphBorder.OUTSIDE -> {
                                        pbPaint.style = Paint.Style.STROKE
                                        canvas.drawRect(pLeft, pTop, pRight, pBottom, pbPaint)
                                    }
                                    ParagraphBorder.BOTTOM -> canvas.drawLine(pLeft, pBottom, pRight, pBottom, pbPaint)
                                    ParagraphBorder.TOP -> canvas.drawLine(pLeft, pTop, pRight, pTop, pbPaint)
                                    ParagraphBorder.LEFT -> canvas.drawLine(pLeft, pTop, pLeft, pBottom, pbPaint)
                                    ParagraphBorder.RIGHT -> canvas.drawLine(pRight, pTop, pRight, pBottom, pbPaint)
                                    else -> {}
                                }
                            }

                            canvas.save()
                            canvas.translate(drawX, currentY)
                            staticLayout.draw(canvas)
                            canvas.restore()

                            currentY += staticLayout.height + 4f
                            currentParaStart = paraEnd + 1
                        }
                    }

                    currentY += block.spaceAfterPt
                }

                is TableBlock -> {
                    checkPageBreak(40f)

                    val isTableRtl = block.isRtl || state.isRtl

                    // Calculate column widths based on ratios
                    val colWidths = FloatArray(block.cols) { c ->
                        val ratio = block.colWidthRatios.getOrElse(c) { 1f / block.cols.coerceAtLeast(1) }
                        contentWidth * ratio
                    }

                    val tableBorderPaint = Paint().apply {
                        color = AndroidColor.DKGRAY
                        style = Paint.Style.STROKE
                        strokeWidth = 0.75f
                    }

                    for (r in 0 until block.rows) {
                        var maxRowHeight = 22f
                        val cellLayouts = mutableMapOf<Int, List<StaticLayout>>()

                        for (c in 0 until block.cols) {
                            val cellData = block.cells["${r}_${c}"]
                            val cellW = (colWidths[c] - 8f).coerceAtLeast(20f).toInt()
                            val layouts = mutableListOf<StaticLayout>()
                            var totalCellTextHeight = 0f

                            val textBlocks = cellData?.textBlocks ?: emptyList()
                            for (tb in textBlocks) {
                                val tStr = tb.text.text
                                if (tStr.isNotEmpty()) {
                                    val cellPaint = TextPaint().apply {
                                        color = tb.textColor.toArgb()
                                        textSize = if (tb.fontSize > 0) tb.fontSize.toFloat() else 10f
                                        isAntiAlias = true
                                        typeface = com.example.presentation.editor.font.FontEngine.getNativeTypeface(
                                            fontName = tb.fontFamily.ifEmpty { state.fontFamily },
                                            isBold = tb.isBold,
                                            isItalic = tb.isItalic
                                        )
                                    }
                                    val cellAlign = when (tb.alignment) {
                                        TextAlignment.CENTER -> Layout.Alignment.ALIGN_CENTER
                                        TextAlignment.RIGHT -> if (isTableRtl) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_OPPOSITE
                                        else -> if (isTableRtl) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL
                                    }
                                    val layout = StaticLayout.Builder.obtain(tStr, 0, tStr.length, cellPaint, cellW)
                                        .setAlignment(cellAlign)
                                        .setIncludePad(false)
                                        .build()
                                    layouts.add(layout)
                                    totalCellTextHeight += layout.height + 3f
                                }
                            }
                            cellLayouts[c] = layouts
                            if (totalCellTextHeight + 8f > maxRowHeight) {
                                maxRowHeight = totalCellTextHeight + 8f
                            }
                        }

                        checkPageBreak(maxRowHeight)

                        // Draw cells in row
                        var currentX = marginX
                        for (c in 0 until block.cols) {
                            val colIdx = if (isTableRtl) block.cols - 1 - c else c
                            val colW = colWidths[colIdx]
                            val cellX = currentX
                            val cellY = currentY
                            val cellData = block.cells["${r}_${colIdx}"]

                            // Background fill
                            if (cellData != null && cellData.backgroundColor != androidx.compose.ui.graphics.Color.Transparent) {
                                val cellBgPaint = Paint().apply {
                                    color = cellData.backgroundColor.toArgb()
                                    style = Paint.Style.FILL
                                }
                                canvas.drawRect(cellX, cellY, cellX + colW, cellY + maxRowHeight, cellBgPaint)
                            }

                            // Cell border
                            canvas.drawRect(cellX, cellY, cellX + colW, cellY + maxRowHeight, tableBorderPaint)

                            // Render text layouts
                            val layouts = cellLayouts[colIdx] ?: emptyList()
                            var textY = cellY + 4f
                            for (l in layouts) {
                                canvas.save()
                                canvas.translate(cellX + 4f, textY)
                                l.draw(canvas)
                                canvas.restore()
                                textY += l.height + 3f
                            }

                            currentX += colW
                        }

                        currentY += maxRowHeight
                    }

                    currentY += 12f
                }

                is ImageBlock -> {
                    if (block.imageData != null && block.imageData.isNotEmpty()) {
                        try {
                            val bmp = BitmapFactory.decodeByteArray(block.imageData, 0, block.imageData.size)
                            if (bmp != null) {
                                val targetWidth = if (block.width > 0) {
                                    block.width.coerceAtMost(contentWidth)
                                } else {
                                    (contentWidth * 0.8f).coerceAtMost(bmp.width.toFloat())
                                }

                                val aspectRatio = bmp.height.toFloat() / bmp.width.toFloat().coerceAtLeast(1f)
                                val targetHeight = if (block.height > 0) {
                                    block.height.coerceAtMost(400f)
                                } else {
                                    (targetWidth * aspectRatio).coerceAtMost(350f)
                                }

                                checkPageBreak(targetHeight + 15f)

                                val imgX = when (block.alignment) {
                                    TextAlignment.LEFT -> marginX
                                    TextAlignment.RIGHT -> marginX + contentWidth - targetWidth
                                    else -> marginX + (contentWidth - targetWidth) / 2f
                                }

                                val destRect = RectF(imgX, currentY, imgX + targetWidth, currentY + targetHeight)
                                canvas.drawBitmap(bmp, null as Rect?, destRect, null as Paint?)
                                currentY += targetHeight + 14f
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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
                    drawPageBackgroundAndChrome(canvas, pageNumber)
                }

                is UnsupportedBlock -> {
                    checkPageBreak(30f)
                    val unsupPaint = Paint().apply {
                        color = AndroidColor.GRAY
                        textSize = 10f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                        textAlign = if (state.isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
                    }
                    val uX = if (state.isRtl) pageWidth - marginX else marginX
                    canvas.drawText("[${block.description}]", uX, currentY + 12f, unsupPaint)
                    currentY += 24f
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
