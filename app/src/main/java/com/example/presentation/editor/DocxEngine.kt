package com.example.presentation.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import org.apache.poi.wp.usermodel.HeaderFooterType
import org.apache.poi.xwpf.usermodel.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

data class DocumentModel(
    val blocks: List<DocumentBlock>,
    val headerText: String = "",
    val footerText: String = "",
    val isRtl: Boolean = false,
    val pageSize: PageSize = PageSize.A4,
    val pageOrientation: PageOrientation = PageOrientation.PORTRAIT,
    val pageMargin: PageMargin = PageMargin.NORMAL,
    val pageColor: Color = Color.White,
    val pageBorder: PageBorder = PageBorder(),
    val watermarkText: String = "",
    val defaultFontFamily: String = "Calibri",
    val defaultFontSize: Int = 12
)

object DocxEngine {

    fun parseDocx(inputStream: InputStream): DocumentModel {
        val blocks = mutableListOf<DocumentBlock>()
        var headerContent = ""
        var footerContent = ""
        var detectedRtl = false
        var detectedPageSize = PageSize.A4
        var detectedOrientation = PageOrientation.PORTRAIT
        var detectedMargin = PageMargin.NORMAL
        var detectedPageColor = Color.White
        var detectedPageBorder = PageBorder()
        var detectedWatermark = ""
        var defaultFontFamily = "Calibri"
        var defaultFontSize = 12

        val fontFrequencyMap = mutableMapOf<String, Int>()
        val sizeFrequencyMap = mutableMapOf<Int, Int>()

        try {
            val document = XWPFDocument(inputStream)

            // 1. Parse Section Properties (pgSz, pgMar, pgBorders)
            try {
                val body = document.document.body
                val sectPr = if (body != null && body.isSetSectPr) body.sectPr else null
                if (sectPr != null) {
                    if (sectPr.isSetPgSz) {
                        val pgSz = sectPr.pgSz
                        val w = pgSz.w?.toString()?.toLongOrNull() ?: 11906L
                        val h = pgSz.h?.toString()?.toLongOrNull() ?: 16838L
                        val orient = if (pgSz.isSetOrient) pgSz.orient.toString() else ""

                        val isLandscape = orient.equals("LANDSCAPE", ignoreCase = true) || w > h
                        detectedOrientation = if (isLandscape) PageOrientation.LANDSCAPE else PageOrientation.PORTRAIT

                        val minDim = minOf(w, h)
                        val maxDim = maxOf(w, h)

                        detectedPageSize = when {
                            minDim < 9000L -> PageSize.A5
                            minDim in 11000L..12100L -> PageSize.A4
                            minDim in 12100L..13500L -> if (maxDim > 18000L) PageSize.LEGAL else PageSize.LETTER
                            minDim > 14000L -> PageSize.A3
                            else -> PageSize.A4
                        }
                    }

                    if (sectPr.isSetPgMar) {
                        val pgMar = sectPr.pgMar
                        val top = pgMar.top?.toString()?.toLongOrNull() ?: 1440L
                        val left = pgMar.left?.toString()?.toLongOrNull() ?: 1440L

                        detectedMargin = when {
                            top <= 800L && left <= 800L -> PageMargin.NARROW
                            left >= 2000L -> PageMargin.WIDE
                            left in 1000L..1200L -> PageMargin.MODERATE
                            else -> PageMargin.NORMAL
                        }
                    }

                    if (sectPr.isSetPgBorders) {
                        val pgBorders = sectPr.pgBorders
                        val topB = if (pgBorders.isSetTop) pgBorders.top else null
                        if (topB != null && topB.`val` != null && topB.`val` != org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.NONE) {
                            val valStr = topB.`val`.toString().lowercase()
                            val bStyle = when {
                                valStr.contains("dash") -> BorderStyle.DASHED
                                valStr.contains("dot") -> BorderStyle.DOTTED
                                valStr.contains("double") -> BorderStyle.DOUBLE
                                else -> BorderStyle.SOLID
                            }
                            val colorHex = topB.color?.toString()
                            val borderCol = parseHexColor(colorHex) ?: Color.Black
                            val wPt = (topB.sz?.toString()?.toFloatOrNull() ?: 4f) / 8f
                            detectedPageBorder = PageBorder(
                                setting = BorderSetting.BOX,
                                style = bStyle,
                                color = borderCol,
                                widthPt = wPt.coerceAtLeast(0.5f)
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Parse Page Background Color
            try {
                val bg = document.document.background
                if (bg != null && bg.color != null) {
                    val hex = bg.color.toString()
                    parseHexColor(hex)?.let { detectedPageColor = it }
                }
            } catch (e: Exception) {}

            // 3. Header & Footer & Watermark
            try {
                val header = document.headerList.firstOrNull()
                if (header != null && header.text.isNotBlank()) {
                    headerContent = header.text.trim()
                    if (headerContent.contains("DRAFT", ignoreCase = true) || headerContent.contains("مسودة", ignoreCase = true)) {
                        detectedWatermark = "مسودة"
                    } else if (headerContent.contains("CONFIDENTIAL", ignoreCase = true) || headerContent.contains("سري", ignoreCase = true)) {
                        detectedWatermark = "سري للغاية"
                    }
                }
                val footer = document.footerList.firstOrNull()
                if (footer != null && footer.text.isNotBlank()) {
                    footerContent = footer.text.trim()
                }
            } catch (e: Exception) {}

            // 4. Parse Body Elements
            for (element in document.bodyElements) {
                when (element.elementType) {
                    BodyElementType.PARAGRAPH -> {
                        val paragraph = element as XWPFParagraph
                        val parsedBlocks = parseParagraph(paragraph, fontFrequencyMap, sizeFrequencyMap)
                        blocks.addAll(parsedBlocks)
                        if (parsedBlocks.any { it is TextBlock && it.isRtl }) {
                            detectedRtl = true
                        }
                    }

                    BodyElementType.TABLE -> {
                        val table = element as XWPFTable
                        val tableBlock = parseTable(table, fontFrequencyMap, sizeFrequencyMap)
                        if (tableBlock != null) {
                            blocks.add(tableBlock)
                            if (tableBlock.isRtl) detectedRtl = true
                        }
                    }

                    else -> {}
                }
            }

            if (fontFrequencyMap.isNotEmpty()) {
                defaultFontFamily = fontFrequencyMap.maxByOrNull { it.value }?.key ?: "Calibri"
            }
            if (sizeFrequencyMap.isNotEmpty()) {
                defaultFontSize = sizeFrequencyMap.maxByOrNull { it.value }?.key ?: 12
            }

            document.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (blocks.isEmpty()) {
            blocks.add(TextBlock("blk_initial", TextFieldValue("")))
        }

        return DocumentModel(
            blocks = blocks,
            headerText = headerContent,
            footerText = footerContent,
            isRtl = detectedRtl,
            pageSize = detectedPageSize,
            pageOrientation = detectedOrientation,
            pageMargin = detectedMargin,
            pageColor = detectedPageColor,
            pageBorder = detectedPageBorder,
            watermarkText = detectedWatermark,
            defaultFontFamily = defaultFontFamily,
            defaultFontSize = defaultFontSize
        )
    }

    private fun parseParagraph(
        paragraph: XWPFParagraph,
        fontFreq: MutableMap<String, Int>,
        sizeFreq: MutableMap<Int, Int>
    ): List<DocumentBlock> {
        val blocks = mutableListOf<DocumentBlock>()

        if (paragraph.isPageBreak) {
            blocks.add(PageBreakBlock("brk_${UUID.randomUUID()}"))
        }

        var isParagraphRtl = false

        val align = when (paragraph.alignment) {
            ParagraphAlignment.CENTER -> TextAlignment.CENTER
            ParagraphAlignment.RIGHT -> {
                isParagraphRtl = true
                TextAlignment.RIGHT
            }
            ParagraphAlignment.BOTH -> TextAlignment.JUSTIFY
            else -> {
                val jcVal = try { paragraph.ctp?.pPr?.jc?.`val`?.toString()?.lowercase() } catch (e: Exception) { null }
                when (jcVal) {
                    "center" -> TextAlignment.CENTER
                    "right" -> { isParagraphRtl = true; TextAlignment.RIGHT }
                    "both" -> TextAlignment.JUSTIFY
                    else -> TextAlignment.LEFT
                }
            }
        }

        val bidi = try { paragraph.ctp?.pPr?.bidi } catch (e: Exception) { null }
        if (bidi != null) {
            isParagraphRtl = true
        }

        val spacing = try {
            val spacingLine = paragraph.ctp?.pPr?.spacing?.line?.toString()?.toDoubleOrNull()
            if (spacingLine != null && spacingLine > 0) {
                (spacingLine / 240.0).toFloat().coerceIn(1.0f, 3.0f)
            } else {
                val spacingBetween = paragraph.spacingBetween
                if (spacingBetween > 0) (spacingBetween / 240.0).toFloat().coerceAtLeast(1.0f) else 1.15f
            }
        } catch (e: Exception) { 1.15f }

        var listPrefix = ""
        try {
            val numPr = paragraph.ctp?.pPr?.numPr
            if (numPr != null) {
                val numId = numPr.numId?.`val`?.toString()
                if (!numId.isNullOrEmpty() && numId != "0") {
                    val ilvl = numPr.ilvl?.`val`?.toString()?.toIntOrNull() ?: 0
                    val indentStr = "  ".repeat(ilvl)
                    listPrefix = "$indentStr• "
                }
            }
        } catch (e: Exception) {}

        val runs = paragraph.runs
        var builder = AnnotatedString.Builder()

        if (listPrefix.isNotEmpty()) {
            builder.append(listPrefix)
        }

        var runCount = 0

        fun flushText() {
            if (builder.length > 0 || runCount > 0) {
                blocks.add(
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(builder.toAnnotatedString()),
                        alignment = align,
                        lineSpacing = spacing,
                        isRtl = isParagraphRtl
                    )
                )
                builder = AnnotatedString.Builder()
                if (listPrefix.isNotEmpty()) {
                    builder.append(listPrefix)
                }
                runCount = 0
            }
        }

        if (runs.isEmpty()) {
            val pText = paragraph.text
            if (pText.isNotEmpty()) {
                if (pText.any { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' }) {
                    isParagraphRtl = true
                }
                blocks.add(
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue((if (listPrefix.isNotEmpty()) listPrefix else "") + pText),
                        alignment = align,
                        lineSpacing = spacing,
                        isRtl = isParagraphRtl
                    )
                )
            }
            return blocks
        }

        for (run in runs) {
            val runText = run.text() ?: ""
            val isArabic = runText.any { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' || it in '\u08A0'..'\u08FF' || it in '\uFB50'..'\uFDFF' || it in '\uFE70'..'\uFEFF' }
            if (isArabic) {
                isParagraphRtl = true
            }

            try {
                val pictures = run.embeddedPictures
                if (pictures.isNotEmpty()) {
                    flushText()
                    for (pic in pictures) {
                        blocks.add(ImageBlock("img_${UUID.randomUUID()}", imageData = pic.pictureData.data))
                    }
                }
            } catch (e: Exception) {}

            val startIdx = builder.length
            builder.append(runText)
            val endIdx = builder.length

            if (startIdx < endIdx) {
                runCount++

                val ctr = run.ctr
                val rPr = if (ctr.isSetRPr) ctr.rPr else null

                val isBold = run.isBold || (rPr != null && (rPr.sizeOfBArray() > 0 || rPr.sizeOfBCsArray() > 0))
                val fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal

                val isItalic = run.isItalic || (rPr != null && (rPr.sizeOfIArray() > 0 || rPr.sizeOfICsArray() > 0))
                val fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal

                val isUnderline = run.underline != UnderlinePatterns.NONE
                val isStrike = run.isStrikeThrough || (rPr != null && (rPr.sizeOfStrikeArray() > 0 || rPr.sizeOfDstrikeArray() > 0))
                val textDecoration = when {
                    isUnderline && isStrike -> TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
                    isUnderline -> TextDecoration.Underline
                    isStrike -> TextDecoration.LineThrough
                    else -> TextDecoration.None
                }

                var resolvedSizeSp = 12
                if (rPr != null) {
                    if (isArabic && rPr.sizeOfSzCsArray() > 0) {
                        val szCs = rPr.getSzCsArray(0).`val`?.toString()?.toIntOrNull()
                        if (szCs != null && szCs > 0) resolvedSizeSp = szCs / 2
                    } else if (rPr.sizeOfSzArray() > 0) {
                        val sz = rPr.getSzArray(0).`val`?.toString()?.toIntOrNull()
                        if (sz != null && sz > 0) resolvedSizeSp = sz / 2
                    }
                }
                if (resolvedSizeSp == 12 && run.fontSize > 0) {
                    resolvedSizeSp = run.fontSize
                }
                sizeFreq[resolvedSizeSp] = (sizeFreq[resolvedSizeSp] ?: 0) + 1
                val fontSize = resolvedSizeSp.sp

                val rawColorHex = try {
                    if (rPr != null && rPr.sizeOfColorArray() > 0) {
                        rPr.getColorArray(0).`val`?.toString()
                    } else run.color
                } catch (e: Exception) { run.color }
                val textColor = parseHexColor(rawColorHex) ?: Color.Black

                val highlightColor = try {
                    val hl = run.textHighlightColor?.toString()
                    if (hl != null && hl != "none") mapHighlightColor(hl) else Color.Transparent
                } catch (e: Exception) { Color.Transparent }

                val vertAlignStr = try { run.verticalAlignment?.toString()?.lowercase() ?: "" } catch (e: Exception) { "" }
                val baselineShift = when {
                    vertAlignStr.contains("subscript") -> BaselineShift.Subscript
                    vertAlignStr.contains("superscript") -> BaselineShift.Superscript
                    else -> BaselineShift.None
                }

                var fontName: String? = null
                if (rPr != null && rPr.sizeOfRFontsArray() > 0) {
                    val rFonts = rPr.getRFontsArray(0)
                    if (isArabic) {
                        fontName = rFonts.cs ?: rFonts.ascii ?: rFonts.hAnsi
                    } else {
                        fontName = rFonts.ascii ?: rFonts.hAnsi ?: rFonts.cs
                    }
                }
                if (fontName.isNullOrBlank()) {
                    fontName = try { run.fontFamily } catch (e: Exception) { null } ?: try { run.fontName } catch (e: Exception) { null }
                }
                if (fontName.isNullOrBlank() || fontName == "Null" || fontName == "null") {
                    fontName = if (isArabic) "Cairo" else "Calibri"
                }

                fontFreq[fontName] = (fontFreq[fontName] ?: 0) + 1

                val parsedFontFamily = com.example.presentation.editor.components.AppFonts.getFontFamily(fontName)

                com.example.presentation.editor.font.FontEngine.ensureFontDownloaded(fontName)

                builder.addStyle(
                    SpanStyle(
                        color = textColor,
                        background = highlightColor,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        fontStyle = fontStyle,
                        fontFamily = parsedFontFamily,
                        textDecoration = textDecoration,
                        baselineShift = baselineShift
                    ),
                    startIdx,
                    endIdx
                )
            }
        }

        flushText()

        return blocks
    }

    private fun parseTable(
        table: XWPFTable,
        fontFreq: MutableMap<String, Int>,
        sizeFreq: MutableMap<Int, Int>
    ): TableBlock? {
        val rows = table.rows.size
        val cols = if (rows > 0) table.rows.maxOfOrNull { it.tableCells.size } ?: 0 else 0
        if (rows == 0 || cols == 0) return null

        val cells = mutableMapOf<String, TableCellModel>()
        val isTableRtl = try { table.ctTbl?.tblPr?.bidiVisual != null } catch (e: Exception) { false }

        for (r in 0 until rows) {
            val row = table.getRow(r) ?: continue
            val rowCells = row.tableCells
            for (c in 0 until cols) {
                val cell = if (c < rowCells.size) rowCells[c] else null
                val cellTextBlocks = mutableListOf<TextBlock>()
                var cellBgColor = Color.Transparent

                if (cell != null) {
                    val hexColor = try {
                        cell.color ?: cell.ctTc?.tcPr?.shd?.fill?.toString()
                    } catch (e: Exception) { cell.color }

                    cellBgColor = parseHexColor(hexColor) ?: Color.Transparent

                    for (cellPara in cell.paragraphs) {
                        val paraBlocks = parseParagraph(cellPara, fontFreq, sizeFreq)
                        cellTextBlocks.addAll(paraBlocks.filterIsInstance<TextBlock>())
                    }
                }

                cells["${r}_${c}"] = TableCellModel(cellTextBlocks, cellBgColor, isTableRtl)
            }
        }

        return TableBlock("tbl_${UUID.randomUUID()}", rows, cols, cells, isTableRtl)
    }

    private fun mapHighlightColor(hlName: String): Color {
        return when (hlName.lowercase()) {
            "yellow" -> Color(0xFFFEF08A)
            "green" -> Color(0xFFBBF7D0)
            "cyan" -> Color(0xFFA5F3FC)
            "magenta", "pink" -> Color(0xFFFBCFE8)
            "red" -> Color(0xFFFCA5A5)
            "blue" -> Color(0xFFBFDBFE)
            "darkyellow" -> Color(0xFFCA8A04)
            "darkgreen" -> Color(0xFF15803D)
            "darkcyan" -> Color(0xFF0E7490)
            "darkred" -> Color(0xFFB91C1C)
            "darkblue" -> Color(0xFF1D4ED8)
            "lightgray" -> Color(0xFFD1D5DB)
            "darkgray" -> Color(0xFF4B5563)
            "black" -> Color.Black
            else -> Color.Transparent
        }
    }

    fun exportDocx(state: EditorState, outputStream: OutputStream) {
        val document = XWPFDocument()

        val sectPr = document.document.body.addNewSectPr()
        val pageSz = sectPr.addNewPgSz()
        
        // POI uses twips (1/20th of a point). 72 points per inch.
        // A4 = 11906 x 16838 twips
        when (state.pageSize) {
            PageSize.A3 -> { pageSz.w = 16838.toBigInteger(); pageSz.h = 23811.toBigInteger() }
            PageSize.A4 -> { pageSz.w = 11906.toBigInteger(); pageSz.h = 16838.toBigInteger() }
            PageSize.A5 -> { pageSz.w = 8391.toBigInteger(); pageSz.h = 11906.toBigInteger() }
            PageSize.LETTER -> { pageSz.w = 12240.toBigInteger(); pageSz.h = 15840.toBigInteger() }
            PageSize.LEGAL -> { pageSz.w = 12240.toBigInteger(); pageSz.h = 20160.toBigInteger() }
        }
        
        if (state.pageOrientation == PageOrientation.LANDSCAPE) {
            val temp = pageSz.w
            pageSz.w = pageSz.h
            pageSz.h = temp
            pageSz.orient = org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation.LANDSCAPE
        }
        
        val pageMar = sectPr.addNewPgMar()
        when (state.pageMargin) {
            PageMargin.NORMAL -> {
                pageMar.top = 1440.toBigInteger()
                pageMar.bottom = 1440.toBigInteger()
                pageMar.left = 1440.toBigInteger()
                pageMar.right = 1440.toBigInteger()
            }
            PageMargin.NARROW -> {
                pageMar.top = 720.toBigInteger()
                pageMar.bottom = 720.toBigInteger()
                pageMar.left = 720.toBigInteger()
                pageMar.right = 720.toBigInteger()
            }
            PageMargin.MODERATE -> {
                pageMar.top = 1440.toBigInteger()
                pageMar.bottom = 1440.toBigInteger()
                pageMar.left = 1080.toBigInteger()
                pageMar.right = 1080.toBigInteger()
            }
            PageMargin.WIDE -> {
                pageMar.top = 1440.toBigInteger()
                pageMar.bottom = 1440.toBigInteger()
                pageMar.left = 2880.toBigInteger()
                pageMar.right = 2880.toBigInteger()
            }
        }

        if (state.headerText.text.isNotEmpty() || state.watermarkText.isNotEmpty()) {
            try {
                val header = document.createHeader(HeaderFooterType.DEFAULT)
                val p = header.createParagraph()
                p.alignment = ParagraphAlignment.CENTER
                p.createRun().setText(state.headerText.text)
                
                // POI doesn't easily support Watermarks without XML hacking, 
                // but we can append the watermark text in the header as a simple text fallback
                if (state.watermarkText.isNotEmpty()) {
                    val wp = header.createParagraph()
                    wp.alignment = ParagraphAlignment.CENTER
                    val wrun = wp.createRun()
                    wrun.setText("[WATERMARK: ${state.watermarkText}]")
                    wrun.color = "D3D3D3"
                    wrun.fontSize = 24
                }
            } catch (e: Exception) {
            }
        } else if (state.watermarkText.isNotEmpty()) {
            try {
                val header = document.createHeader(HeaderFooterType.DEFAULT)
                val wp = header.createParagraph()
                wp.alignment = ParagraphAlignment.CENTER
                val wrun = wp.createRun()
                wrun.setText("[WATERMARK: ${state.watermarkText}]")
                wrun.color = "D3D3D3"
                wrun.fontSize = 24
            } catch (e: Exception) {}
        }

        try {
            val body = document.document.body ?: document.document.addNewBody()
            
            // Page Color
            if (state.pageColor != Color.White) {
                val background = document.document.background ?: document.document.addNewBackground()
                val hexColor = Integer.toHexString(state.pageColor.toArgb()).substring(2).uppercase().padStart(8, '0').substring(2) // AARRGGBB -> RRGGBB
                background.color = hexColor
            }

            // Page Borders
            if (state.pageBorder.setting != BorderSetting.NONE) {
                val sectPr = body.sectPr ?: body.addNewSectPr()
                val pgBorders = sectPr.pgBorders ?: sectPr.addNewPgBorders()
                
                val valType = when(state.pageBorder.style) {
                    BorderStyle.SOLID -> org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE
                    BorderStyle.DASHED -> org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.DASHED
                    BorderStyle.DOTTED -> org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.DOTTED
                    BorderStyle.DOUBLE -> org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.DOUBLE
                    else -> org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.NONE
                }
                
                val borderColor = Integer.toHexString(state.pageBorder.color.toArgb()).substring(2).uppercase().padStart(8, '0').substring(2)
                val borderWidth = (state.pageBorder.widthPt * 8).toLong() // sz is in 1/8 pt

                listOf(
                    pgBorders.isSetTop.let { if (it) pgBorders.top else pgBorders.addNewTop() },
                    pgBorders.isSetBottom.let { if (it) pgBorders.bottom else pgBorders.addNewBottom() },
                    pgBorders.isSetLeft.let { if (it) pgBorders.left else pgBorders.addNewLeft() },
                    pgBorders.isSetRight.let { if (it) pgBorders.right else pgBorders.addNewRight() }
                ).forEach { border ->
                    border.`val` = valType
                    border.color = borderColor
                    border.sz = java.math.BigInteger.valueOf(borderWidth)
                    border.space = java.math.BigInteger.valueOf(24)
                    if (state.pageBorder.setting == BorderSetting.SHADOW) {
                        // try { border.shadow = org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff.ON } catch(e: Exception) {}
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }

        if (state.footerText.text.isNotEmpty()) {
            try {
                val footer = document.createFooter(HeaderFooterType.DEFAULT)
                val p = footer.createParagraph()
                p.alignment = ParagraphAlignment.CENTER
                p.createRun().setText(state.footerText.text)
            } catch (e: Exception) {
            }
        }

        for (block in state.blocks) {
            when (block) {
                is TextBlock -> processTextBlock(document, block, state)
                is TableBlock -> processTableBlock(document, block, state)
                is PageBreakBlock -> {
                    val p = document.createParagraph()
                    p.isPageBreak = true
                    p.createRun().addBreak(BreakType.PAGE)
                }
                is ImageBlock -> {
                    val p = document.createParagraph()
                    p.alignment = ParagraphAlignment.CENTER
                    val run = p.createRun()
                    if (block.imageData != null && block.imageData.isNotEmpty()) {
                        try {
                            val bais = java.io.ByteArrayInputStream(block.imageData)
                            val widthEmu = org.apache.poi.util.Units.toEMU(300.0)
                            val heightEmu = org.apache.poi.util.Units.toEMU(200.0)
                            run.addPicture(bais, Document.PICTURE_TYPE_PNG, "image_${block.id}.png", widthEmu, heightEmu)
                        } catch (e: Exception) {
                            run.setText("[Image: ${block.uri.ifEmpty { "Embedded Image" }}]")
                        }
                    } else {
                        run.setText("[Image: ${block.uri.ifEmpty { "Embedded Image" }}]")
                    }
                }
                is ShapeBlock -> {
                    val p = document.createParagraph()
                    val run = p.createRun()
                    run.setText("[Shape: ${block.type.name}]")
                }
                is BannerBlock -> {
                    val p = document.createParagraph()
                    val run1 = p.createRun()
                    run1.isBold = true
                    run1.fontSize = 20
                    run1.setText(block.title)
                    if (block.subtitle.isNotEmpty()) {
                        val run2 = p.createRun()
                        run2.fontSize = 13
                        run2.setText("\n" + block.subtitle)
                    }
                }
                is CalloutBlock -> {
                    val p = document.createParagraph()
                    if (block.title.isNotEmpty()) {
                        val run1 = p.createRun()
                        run1.isBold = true
                        run1.fontSize = 14
                        run1.setText(block.title + "\n")
                    }
                    val run2 = p.createRun()
                    run2.fontSize = 12
                    run2.setText(block.text.text)
                }
                is DividerBlock -> {
                    val p = document.createParagraph()
                    val run = p.createRun()
                    run.setText("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                }
            }
        }

        document.write(outputStream)
        document.close()
    }

    fun exportDocxToByteArray(state: EditorState): ByteArray {
        val baos = ByteArrayOutputStream()
        exportDocx(state, baos)
        return baos.toByteArray()
    }

    private fun processTextBlock(document: XWPFDocument, block: TextBlock, state: EditorState, parentCell: XWPFTableCell? = null) {
        val paragraph = parentCell?.addParagraph() ?: document.createParagraph()

        paragraph.alignment = when (block.alignment) {
            TextAlignment.CENTER -> ParagraphAlignment.CENTER
            TextAlignment.RIGHT -> ParagraphAlignment.RIGHT
            TextAlignment.JUSTIFY -> ParagraphAlignment.BOTH
            TextAlignment.LEFT -> if (block.isRtl || state.isRtl) ParagraphAlignment.RIGHT else ParagraphAlignment.LEFT
        }

        try {
            if (block.lineSpacing > 0f) {
                paragraph.spacingBetween = (block.lineSpacing * 240).toDouble()
            }
            
            // RTL / Bidi XML Tag for MS Word compatibility
            if (block.isRtl || state.isRtl) {
                val ctp = paragraph.ctp
                val ppr = if (ctp.isSetPPr) ctp.pPr else ctp.addNewPPr()
                if (!ppr.isSetBidi) {
                    ppr.addNewBidi() // Usually adding the element without setting 'val' is enough to make it true
                }
            }
        } catch (e: Exception) {}

        val annotatedString = block.text.annotatedString
        if (annotatedString.text.isEmpty()) {
            val run = paragraph.createRun()
            run.setText("")
            return
        }

        var currentIndex = 0
        while (currentIndex < annotatedString.text.length) {
            val run = paragraph.createRun()
            
            var runFontName = state.fontFamily
            run.fontFamily = runFontName
            run.fontSize = state.fontSize

            val spanStyles = annotatedString.spanStyles.filter {
                it.start <= currentIndex && it.end > currentIndex
            }

            var nextChangeIndex = annotatedString.text.length
            annotatedString.spanStyles.forEach { span ->
                if (span.start in (currentIndex + 1) until nextChangeIndex) {
                    nextChangeIndex = span.start
                }
                if (span.end in (currentIndex + 1) until nextChangeIndex) {
                    nextChangeIndex = span.end
                }
            }

            for (span in spanStyles) {
                val style = span.item
                if (style.fontWeight == FontWeight.Bold) run.isBold = true
                if (style.fontStyle == FontStyle.Italic) run.isItalic = true
                if (style.fontFamily != null) {
                    runFontName = style.fontFamily.toString()
                    run.fontFamily = runFontName
                }
                if (style.textDecoration == TextDecoration.Underline || style.textDecoration == TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))) {
                    run.underline = UnderlinePatterns.SINGLE
                }
                if (style.textDecoration == TextDecoration.LineThrough || style.textDecoration == TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))) {
                    run.isStrikeThrough = true
                }
                if (style.fontSize.value > 0) {
                    run.fontSize = style.fontSize.value.toInt()
                }
                if (style.baselineShift == BaselineShift.Subscript) {
                    try { run.setSubscript(VerticalAlign.SUBSCRIPT) } catch (e: Exception) {}
                }
                if (style.baselineShift == BaselineShift.Superscript) {
                    try { run.setSubscript(VerticalAlign.SUPERSCRIPT) } catch (e: Exception) {}
                }
                if (style.color != Color.Unspecified && style.color != Color.Black) {
                    run.color = colorToHex(style.color)
                }
            }

            // WordprocessingML Font XML attributes for Arabic (Complex Script) & English
            try {
                val ctr = run.ctr
                val rPr = if (ctr.isSetRPr) ctr.rPr else ctr.addNewRPr()
                val rFonts = rPr.addNewRFonts()
                rFonts.ascii = runFontName
                rFonts.hAnsi = runFontName
                rFonts.cs = runFontName
                rFonts.eastAsia = runFontName
                if (block.isRtl || state.isRtl) {
                    rPr.addNewRtl()
                }
            } catch (e: Exception) {}

            val segmentText = annotatedString.text.substring(currentIndex, nextChangeIndex)
            run.setText(segmentText)

            currentIndex = nextChangeIndex
        }
    }

    private fun processTableBlock(document: XWPFDocument, block: TableBlock, state: EditorState) {
        val table = document.createTable(block.rows, block.cols)
        for (r in 0 until block.rows) {
            val row = table.getRow(r) ?: table.createRow()
            for (c in 0 until block.cols) {
                val cell = row.getCell(c) ?: row.createCell()
                val cellModel = block.cells["${r}_${c}"]
                if (cellModel != null) {
                    if (cellModel.backgroundColor != Color.Transparent) {
                        cell.color = colorToHex(cellModel.backgroundColor)
                    }
                    cell.paragraphs.toList().forEach { cell.removeParagraph(0) }
                    for (tb in cellModel.textBlocks) {
                        processTextBlock(document, tb, state, cell)
                    }
                }
            }
        }
    }

    private fun parseHexColor(hex: String?): Color? {
        if (hex.isNullOrBlank() || hex == "auto") return null
        return try {
            val cleanHex = hex.replace("#", "").trim()
            val colorInt = cleanHex.toLong(16).toInt()
            Color(colorInt or 0xFF000000.toInt())
        } catch (e: Exception) {
            null
        }
    }

    private fun colorToHex(color: Color): String {
        val red = (color.red * 255).toInt().coerceIn(0, 255)
        val green = (color.green * 255).toInt().coerceIn(0, 255)
        val blue = (color.blue * 255).toInt().coerceIn(0, 255)
        return String.format("%02X%02X%02X", red, green, blue)
    }
}
