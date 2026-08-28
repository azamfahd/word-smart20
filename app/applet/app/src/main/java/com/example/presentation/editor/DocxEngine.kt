package com.example.presentation.editor

import androidx.compose.ui.graphics.Color
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
    val isRtl: Boolean = false
)

object DocxEngine {

    fun parseDocx(inputStream: InputStream): DocumentModel {
        val blocks = mutableListOf<DocumentBlock>()
        var headerContent = ""
        var footerContent = ""
        var detectedRtl = false

        try {
            val document = XWPFDocument(inputStream)

            try {
                val header = document.headerList.firstOrNull()
                if (header != null && header.text.isNotBlank()) {
                    headerContent = header.text.trim()
                }
                val footer = document.footerList.firstOrNull()
                if (footer != null && footer.text.isNotBlank()) {
                    footerContent = footer.text.trim()
                }
            } catch (e: Exception) {
            }

            for (element in document.bodyElements) {
                when (element.elementType) {
                    BodyElementType.PARAGRAPH -> {
                        val paragraph = element as XWPFParagraph
                        val parsedBlocks = parseParagraph(paragraph)
                        blocks.addAll(parsedBlocks)
                        if (parsedBlocks.any { it is TextBlock && it.isRtl }) {
                            detectedRtl = true
                        }
                    }

                    BodyElementType.TABLE -> {
                        val table = element as XWPFTable
                        val rows = table.rows.size
                        val cols = if (rows > 0) table.rows.maxOfOrNull { it.tableCells.size } ?: 0 else 0
                        val cells = mutableMapOf<String, TableCellModel>()

                        val isTableRtl = try { table.ctTbl?.tblPr?.bidiVisual?.`val`?.toBoolean() ?: false } catch (e: Exception) { false }

                        for (r in 0 until rows) {
                            val row = table.getRow(r)
                            if (row == null) continue
                            val rowCells = row.tableCells
                            for (c in 0 until cols) {
                                val cell = if (c < rowCells.size) rowCells[c] else null
                                val cellTextBlocks = mutableListOf<TextBlock>()
                                var cellBgColor = Color.Transparent

                                if (cell != null) {
                                    val hexColor = cell.color
                                    cellBgColor = parseHexColor(hexColor) ?: Color.Transparent
                                    
                                    for (cellPara in cell.paragraphs) {
                                        val paraBlocks = parseParagraph(cellPara)
                                        cellTextBlocks.addAll(paraBlocks.filterIsInstance<TextBlock>())
                                    }
                                }
                                cells["${r}_${c}"] = TableCellModel(cellTextBlocks, cellBgColor, isTableRtl)
                            }
                        }

                        if (rows > 0 && cols > 0) {
                            blocks.add(TableBlock("tbl_${UUID.randomUUID()}", rows, cols, cells, isTableRtl))
                        }
                    }

                    else -> {}
                }
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
            isRtl = detectedRtl
        )
    }

    private fun parseParagraph(paragraph: XWPFParagraph): List<DocumentBlock> {
        val blocks = mutableListOf<DocumentBlock>()
        
        if (paragraph.isPageBreak) {
            blocks.add(PageBreakBlock("brk_${UUID.randomUUID()}"))
        }

        val runs = paragraph.runs
        val containsBreak = runs.any { run -> 
            val t = run.text() ?: ""
            t.contains("\u000c")
        }

        var isParagraphRtl = false
        val align = when (paragraph.alignment) {
            ParagraphAlignment.CENTER -> TextAlignment.CENTER
            ParagraphAlignment.RIGHT -> {
                isParagraphRtl = true
                TextAlignment.RIGHT
            }
            ParagraphAlignment.BOTH -> TextAlignment.JUSTIFY
            else -> TextAlignment.LEFT
        }

        val bidi = try { paragraph.ctp?.pPr?.bidi } catch (e: Exception) { null }
        if (bidi != null) {
            isParagraphRtl = true
        }

        val spacing = try { 
            val spacingBetween = paragraph.spacingBetween
            if (spacingBetween > 0) (spacingBetween / 240.0).toFloat().coerceAtLeast(1f) else 1.15f
        } catch (e: Exception) { 1.15f }

        var builder = AnnotatedString.Builder()
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
                runCount = 0
            }
        }

        if (runs.isEmpty()) {
            if (paragraph.text.isNotEmpty()) {
                blocks.add(
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(paragraph.text),
                        alignment = align,
                        lineSpacing = spacing,
                        isRtl = isParagraphRtl
                    )
                )
            }
            if (containsBreak) {
                blocks.add(PageBreakBlock("brk_${UUID.randomUUID()}"))
            }
            return blocks
        }

        for (run in runs) {
            val runText = run.text() ?: ""
            if (runText.any { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' }) {
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
                val fontWeight = if (run.isBold) FontWeight.Bold else FontWeight.Normal
                val fontStyle = if (run.isItalic) FontStyle.Italic else FontStyle.Normal
                val textDecoration = when {
                    run.underline != UnderlinePatterns.NONE && run.isStrikeThrough -> 
                        TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
                    run.underline != UnderlinePatterns.NONE -> TextDecoration.Underline
                    run.isStrikeThrough -> TextDecoration.LineThrough
                    else -> TextDecoration.None
                }
                val fontSize = if (run.fontSize > 0) run.fontSize.sp else 14.sp
                val textColor = parseHexColor(run.color) ?: Color.Black
                
                val highlightColor = try {
                    val hl = run.textHighlightColor?.toString()
                    if (hl != null && hl != "none") {
                        when(hl.lowercase()) {
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
                    } else Color.Transparent
                } catch (e: Exception) { Color.Transparent }

                val vertAlignStr = try { run.verticalAlignment?.toString()?.lowercase() ?: "" } catch (e: Exception) { "" }
                val baselineShift = when {
                    vertAlignStr.contains("subscript") -> BaselineShift.Subscript
                    vertAlignStr.contains("superscript") -> BaselineShift.Superscript
                    else -> BaselineShift.None
                }

                builder.addStyle(
                    SpanStyle(
                        color = textColor,
                        background = highlightColor,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        fontStyle = fontStyle,
                        textDecoration = textDecoration,
                        baselineShift = baselineShift
                    ),
                    startIdx,
                    endIdx
                )
            }
        }
        flushText()

        if (containsBreak) {
            blocks.add(PageBreakBlock("brk_${UUID.randomUUID()}"))
        }

        return blocks
    }

    fun exportDocx(state: EditorState, outputStream: OutputStream) {
        val document = XWPFDocument()

        if (state.headerText.text.isNotEmpty()) {
            try {
                val header = document.createHeader(HeaderFooterType.DEFAULT)
                val p = header.createParagraph()
                p.alignment = ParagraphAlignment.CENTER
                p.createRun().setText(state.headerText.text)
            } catch (e: Exception) {
            }
        }

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
                    val run = p.createRun()
                    run.setText("[Image: ${block.uri.ifEmpty { "Embedded Illustration" }}]")
                }
                is ShapeBlock -> {
                    val p = document.createParagraph()
                    val run = p.createRun()
                    run.setText("[Shape: ${block.type.name}]")
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

        paragraph.alignment = when (state.alignment) {
            TextAlignment.CENTER -> ParagraphAlignment.CENTER
            TextAlignment.RIGHT -> ParagraphAlignment.RIGHT
            TextAlignment.JUSTIFY -> ParagraphAlignment.BOTH
            TextAlignment.LEFT -> if (state.isTextRtl || state.isRtl) ParagraphAlignment.RIGHT else ParagraphAlignment.LEFT
        }

        val annotatedString = block.text.annotatedString
        if (annotatedString.text.isEmpty()) {
            val run = paragraph.createRun()
            run.setText("")
            return
        }

        var currentIndex = 0
        while (currentIndex < annotatedString.text.length) {
            val run = paragraph.createRun()
            
            run.fontFamily = state.fontFamily
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
