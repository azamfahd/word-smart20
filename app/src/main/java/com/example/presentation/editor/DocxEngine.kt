package com.example.presentation.editor

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import org.apache.poi.util.Units
import org.apache.poi.wp.usermodel.HeaderFooterType
import org.apache.poi.xwpf.usermodel.*
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
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
    val defaultFontSize: Int = 12,
    val originalBytes: ByteArray? = null
)

data class ResolvedStyle(
    val styleId: String,
    val name: String,
    val fontSizeSp: Int? = null,
    val fontFamily: String? = null,
    val fontFamilyCs: String? = null,
    val isBold: Boolean? = null,
    val isItalic: Boolean? = null,
    val color: Color? = null,
    val alignment: TextAlignment? = null,
    val spaceBeforePt: Float? = null,
    val spaceAfterPt: Float? = null,
    val lineSpacing: Float? = null,
    val shadingColor: Color? = null,
    val isRtl: Boolean? = null
)

data class NumberingInfo(
    val isBullet: Boolean,
    val bulletShape: BulletShape = BulletShape.DISC,
    val numberingStyle: NumberingStyle = NumberingStyle.DECIMAL_DOT
)

object DocxEngine {

    // =========================================================================
    // 1. HIGH-FIDELITY OOXML PARSING (DOCX -> DocumentModel)
    // =========================================================================

    fun parseDocx(inputStream: InputStream, originalBytes: ByteArray? = null): DocumentModel {
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
        val styleCache = mutableMapOf<String, ResolvedStyle>()
        val numberingCache = mutableMapOf<String, NumberingInfo>()

        // Capture raw byte stream for subsequent in-place round-trip preservation
        val docxBytes = originalBytes ?: try { inputStream.readBytes() } catch (e: Exception) { ByteArray(0) }

        try {
            val document = XWPFDocument(ByteArrayInputStream(docxBytes))

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
                        if (topB != null && topB.`val` != null && topB.`val` != STBorder.NONE) {
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

            // 4. Parse Body Elements with position tagging for round-trip synchronization
            document.bodyElements.forEachIndexed { index, element ->
                when (element.elementType) {
                    BodyElementType.PARAGRAPH -> {
                        val paragraph = element as XWPFParagraph
                        val parsedBlocks = parseParagraph(
                            paragraph = paragraph,
                            document = document,
                            bodyElementIndex = index,
                            fontFreq = fontFrequencyMap,
                            sizeFreq = sizeFrequencyMap,
                            styleCache = styleCache,
                            numCache = numberingCache
                        )
                        blocks.addAll(parsedBlocks)
                        if (parsedBlocks.any { it is TextBlock && it.isRtl }) {
                            detectedRtl = true
                        }
                    }

                    BodyElementType.TABLE -> {
                        val table = element as XWPFTable
                        val tableBlock = parseTable(
                            table = table,
                            document = document,
                            bodyElementIndex = index,
                            fontFreq = fontFrequencyMap,
                            sizeFreq = sizeFrequencyMap,
                            styleCache = styleCache,
                            numCache = numberingCache
                        )
                        if (tableBlock != null) {
                            blocks.add(tableBlock)
                            if (tableBlock.isRtl) detectedRtl = true
                        }
                    }

                    else -> {
                        // Preserve unsupported body elements (equations, SDT, shapes) as UnsupportedBlock
                        blocks.add(
                            UnsupportedBlock(
                                id = "unsupported_${UUID.randomUUID()}",
                                description = "عنصر مدمج محفوظ (${element.elementType.name})",
                                rawXml = element.toString(),
                                sourceElementIndex = index
                            )
                        )
                    }
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
            defaultFontSize = defaultFontSize,
            originalBytes = if (docxBytes.isNotEmpty()) docxBytes else null
        )
    }

    private fun resolveParagraphStyle(
        document: XWPFDocument,
        paragraph: XWPFParagraph,
        styleCache: MutableMap<String, ResolvedStyle>
    ): ResolvedStyle? {
        val styleId = paragraph.styleID ?: paragraph.style
        val effectiveId = styleId ?: "Normal"
        if (styleCache.containsKey(effectiveId)) {
            return styleCache[effectiveId]
        }

        var resolvedName = effectiveId
        var rFontSize: Int? = null
        var rFontFamily: String? = null
        var rFontFamilyCs: String? = null
        var rIsBold: Boolean? = null
        var rIsItalic: Boolean? = null
        var rColor: Color? = null
        var rAlignment: TextAlignment? = null
        var rSpaceBefore: Float? = null
        var rSpaceAfter: Float? = null
        var rLineSpacing: Float? = null
        var rShading: Color? = null
        var rIsRtl: Boolean? = null

        try {
            val styles = document.styles
            val xwpfStyle = if (styles != null && !styleId.isNullOrEmpty()) styles.getStyle(styleId) else null
            if (xwpfStyle != null) {
                val ctStyle = xwpfStyle.ctStyle
                if (ctStyle.isSetName) resolvedName = ctStyle.name?.`val`?.toString() ?: effectiveId

                val rPr = if (ctStyle.isSetRPr) ctStyle.rPr else null
                if (rPr != null) {
                    if (rPr.sizeOfBArray() > 0 || rPr.sizeOfBCsArray() > 0) rIsBold = true
                    if (rPr.sizeOfIArray() > 0 || rPr.sizeOfICsArray() > 0) rIsItalic = true
                    if (rPr.sizeOfSzArray() > 0) {
                        val sz = rPr.getSzArray(0).`val`?.toString()?.toIntOrNull()
                        if (sz != null && sz > 0) rFontSize = sz / 2
                    }
                    if (rPr.sizeOfSzCsArray() > 0) {
                        val szCs = rPr.getSzCsArray(0).`val`?.toString()?.toIntOrNull()
                        if (szCs != null && szCs > 0 && rFontSize == null) rFontSize = szCs / 2
                    }
                    if (rPr.sizeOfColorArray() > 0) {
                        val c = rPr.getColorArray(0)
                        rColor = resolveDocxColor(
                            c.`val`?.toString(),
                            c.themeColor?.toString(),
                            c.themeTint?.toString(),
                            c.themeShade?.toString()
                        )
                    }
                    if (rPr.sizeOfRFontsArray() > 0) {
                        val rf = rPr.getRFontsArray(0)
                        rFontFamily = rf.ascii ?: rf.hAnsi
                        rFontFamilyCs = rf.cs
                    }
                    if (rPr.sizeOfRtlArray() > 0) rIsRtl = true
                }

                val pPr = if (ctStyle.isSetPPr) ctStyle.pPr else null
                if (pPr != null) {
                    if (pPr.isSetJc) {
                        when (pPr.jc.`val`?.toString()?.lowercase()) {
                            "center" -> rAlignment = TextAlignment.CENTER
                            "right" -> { rAlignment = TextAlignment.RIGHT; rIsRtl = true }
                            "both" -> rAlignment = TextAlignment.JUSTIFY
                            "left" -> rAlignment = TextAlignment.LEFT
                        }
                    }
                    if (pPr.isSetSpacing) {
                        val sp = pPr.spacing
                        val b = sp.before?.toString()?.toDoubleOrNull()
                        if (b != null && b > 0) rSpaceBefore = (b / 20.0).toFloat().coerceIn(0f, 72f)
                        val a = sp.after?.toString()?.toDoubleOrNull()
                        if (a != null && a > 0) rSpaceAfter = (a / 20.0).toFloat().coerceIn(0f, 72f)
                        val line = sp.line?.toString()?.toDoubleOrNull()
                        if (line != null && line > 0) rLineSpacing = (line / 240.0).toFloat().coerceIn(1.0f, 3.0f)
                    }
                    if (pPr.isSetShd) {
                        val f = pPr.shd.fill?.toString()
                        if (!f.isNullOrBlank() && f != "auto" && f != "none") rShading = parseHexColor(f)
                    }
                    if (pPr.isSetBidi) rIsRtl = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Apply Microsoft Word standard style defaults
        val idLower = effectiveId.lowercase()
        val nameLower = resolvedName.lowercase()
        val isH1 = idLower.contains("heading1") || idLower.contains("heading 1") || nameLower.contains("heading 1") || nameLower.contains("عنوان 1")
        val isH2 = idLower.contains("heading2") || idLower.contains("heading 2") || nameLower.contains("heading 2") || nameLower.contains("عنوان 2")
        val isH3 = idLower.contains("heading3") || idLower.contains("heading 3") || nameLower.contains("heading 3") || nameLower.contains("عنوان 3")
        val isH4 = idLower.contains("heading4") || idLower.contains("heading 4") || nameLower.contains("heading 4") || nameLower.contains("عنوان 4")
        val isTitle = idLower.contains("title") || nameLower == "title" || nameLower.contains("عنوان رئيسي")
        val isSubtitle = idLower.contains("subtitle") || nameLower.contains("subtitle") || nameLower.contains("عنوان فرعي")
        val isQuote = idLower.contains("quote") || nameLower.contains("quote") || nameLower.contains("اقتباس")

        if (isH1) {
            if (rFontSize == null) rFontSize = 20
            if (rIsBold == null) rIsBold = true
            if (rColor == null) rColor = Color(0xFF2E74B5)
            if (rSpaceBefore == null) rSpaceBefore = 12f
            if (rSpaceAfter == null) rSpaceAfter = 6f
        } else if (isH2) {
            if (rFontSize == null) rFontSize = 16
            if (rIsBold == null) rIsBold = true
            if (rColor == null) rColor = Color(0xFF2E74B5)
            if (rSpaceBefore == null) rSpaceBefore = 8f
            if (rSpaceAfter == null) rSpaceAfter = 4f
        } else if (isH3) {
            if (rFontSize == null) rFontSize = 13
            if (rIsBold == null) rIsBold = true
            if (rColor == null) rColor = Color(0xFF1F497D)
            if (rSpaceBefore == null) rSpaceBefore = 6f
            if (rSpaceAfter == null) rSpaceAfter = 2f
        } else if (isH4) {
            if (rFontSize == null) rFontSize = 12
            if (rIsBold == null) rIsBold = true
            if (rIsItalic == null) rIsItalic = true
            if (rColor == null) rColor = Color(0xFF2E74B5)
            if (rSpaceBefore == null) rSpaceBefore = 4f
        } else if (isTitle) {
            if (rFontSize == null) rFontSize = 26
            if (rIsBold == null) rIsBold = true
            if (rColor == null) rColor = Color(0xFF1F497D)
            if (rSpaceAfter == null) rSpaceAfter = 10f
        } else if (isSubtitle) {
            if (rFontSize == null) rFontSize = 13
            if (rColor == null) rColor = Color(0xFF595959)
            if (rSpaceAfter == null) rSpaceAfter = 8f
        } else if (isQuote) {
            if (rFontSize == null) rFontSize = 11
            if (rIsItalic == null) rIsItalic = true
            if (rColor == null) rColor = Color(0xFF595959)
        }

        val resolved = ResolvedStyle(
            styleId = effectiveId,
            name = resolvedName,
            fontSizeSp = rFontSize,
            fontFamily = rFontFamily,
            fontFamilyCs = rFontFamilyCs,
            isBold = rIsBold,
            isItalic = rIsItalic,
            color = rColor,
            alignment = rAlignment,
            spaceBeforePt = rSpaceBefore,
            spaceAfterPt = rSpaceAfter,
            lineSpacing = rLineSpacing,
            shadingColor = rShading,
            isRtl = rIsRtl
        )
        styleCache[effectiveId] = resolved
        return resolved
    }

    private fun resolveNumbering(
        document: XWPFDocument,
        paragraph: XWPFParagraph,
        numCache: MutableMap<String, NumberingInfo>
    ): Pair<NumberingInfo?, Int> {
        try {
            val numPr = paragraph.ctp?.pPr?.numPr ?: return Pair(null, 0)
            val numId = numPr.numId?.`val`?.toString()
            val ilvl = numPr.ilvl?.`val`?.toString()?.toIntOrNull() ?: 0
            if (numId.isNullOrEmpty() || numId == "0") return Pair(null, ilvl)

            if (numCache.containsKey(numId)) {
                return Pair(numCache[numId], ilvl)
            }

            var isBullet = true
            var bulletShape = BulletShape.DISC
            var numberingStyle = NumberingStyle.DECIMAL_DOT

            val numbering = document.numbering
            if (numbering != null) {
                try {
                    val num = numbering.getNum(BigInteger(numId))
                    val abstractNumId = num?.ctNum?.abstractNumId?.`val`
                    if (abstractNumId != null) {
                        val abstractNum = numbering.getAbstractNum(abstractNumId)
                        val ctAbstract = abstractNum?.ctAbstractNum
                        val lvl = ctAbstract?.lvlList?.getOrNull(ilvl) ?: ctAbstract?.lvlList?.firstOrNull()
                        val numFmt = lvl?.numFmt?.`val`?.toString()?.lowercase() ?: ""
                        val lvlText = lvl?.lvlText?.`val`?.toString() ?: ""

                        if (numFmt == "bullet" || numFmt.contains("bullet")) {
                            isBullet = true
                            bulletShape = when {
                                lvlText.contains("o") || lvlText.contains("○") -> BulletShape.CIRCLE
                                lvlText.contains("■") || lvlText.contains("▪") -> BulletShape.SQUARE
                                lvlText.contains("□") -> BulletShape.HOLLOW_SQUARE
                                lvlText.contains("✓") -> BulletShape.CHECKMARK
                                lvlText.contains("➢") || lvlText.contains("➔") -> BulletShape.ARROW
                                lvlText.contains("★") -> BulletShape.STAR
                                lvlText.contains("❖") -> BulletShape.FLORAL
                                else -> BulletShape.DISC
                            }
                        } else {
                            isBullet = false
                            numberingStyle = when {
                                numFmt == "decimal" -> NumberingStyle.DECIMAL_DOT
                                numFmt == "lowerletter" -> NumberingStyle.ALPHA_LOWER
                                numFmt == "upperletter" -> NumberingStyle.ALPHA_UPPER
                                numFmt == "lowerroman" -> NumberingStyle.ROMAN_LOWER
                                numFmt == "upperroman" -> NumberingStyle.ROMAN_UPPER
                                numFmt.contains("arabic") -> NumberingStyle.ARABIC_ALIF_BAA
                                else -> NumberingStyle.DECIMAL_DOT
                            }
                        }
                    }
                } catch (e: Exception) {}
            }

            val info = NumberingInfo(isBullet, bulletShape, numberingStyle)
            numCache[numId] = info
            return Pair(info, ilvl)
        } catch (e: Exception) {
            return Pair(null, 0)
        }
    }

    private fun resolveDocxColor(
        colorStr: String?,
        themeColorStr: String?,
        themeTintStr: String?,
        themeShadeStr: String?
    ): Color? {
        if (!colorStr.isNullOrBlank() && colorStr != "auto" && colorStr != "none") {
            val parsed = parseHexColor(colorStr)
            if (parsed != null) return parsed
        }
        if (!themeColorStr.isNullOrBlank()) {
            val baseColor = when (themeColorStr.lowercase()) {
                "accent1" -> Color(0xFF4472C4)
                "accent2" -> Color(0xFFED7D31)
                "accent3" -> Color(0xFFA5A5A5)
                "accent4" -> Color(0xFFFFC000)
                "accent5" -> Color(0xFF5B9BD5)
                "accent6" -> Color(0xFF70AD47)
                "dark1" -> Color(0xFF000000)
                "light1" -> Color(0xFFFFFFFF)
                "dark2" -> Color(0xFF44546A)
                "light2" -> Color(0xFFE7E6E6)
                "hyperlink" -> Color(0xFF0563C1)
                "followedhyperlink" -> Color(0xFF954F72)
                else -> null
            }
            if (baseColor != null) {
                try {
                    if (!themeShadeStr.isNullOrBlank()) {
                        val shadeVal = themeShadeStr.toIntOrNull(16) ?: 255
                        val factor = shadeVal / 255f
                        return Color(
                            red = baseColor.red * factor,
                            green = baseColor.green * factor,
                            blue = baseColor.blue * factor,
                            alpha = 1f
                        )
                    }
                    if (!themeTintStr.isNullOrBlank()) {
                        val tintVal = themeTintStr.toIntOrNull(16) ?: 255
                        val factor = tintVal / 255f
                        return Color(
                            red = baseColor.red + (1f - baseColor.red) * (1f - factor),
                            green = baseColor.green + (1f - baseColor.green) * (1f - factor),
                            blue = baseColor.blue + (1f - baseColor.blue) * (1f - factor),
                            alpha = 1f
                        )
                    }
                } catch (e: Exception) {}
                return baseColor
            }
        }
        return null
    }

    private fun parseParagraph(
        paragraph: XWPFParagraph,
        document: XWPFDocument,
        bodyElementIndex: Int,
        fontFreq: MutableMap<String, Int>,
        sizeFreq: MutableMap<Int, Int>,
        styleCache: MutableMap<String, ResolvedStyle>,
        numCache: MutableMap<String, NumberingInfo>
    ): List<DocumentBlock> {
        val blocks = mutableListOf<DocumentBlock>()

        if (paragraph.isPageBreak) {
            blocks.add(PageBreakBlock("brk_${UUID.randomUUID()}", sourceElementIndex = bodyElementIndex))
        }

        val style = resolveParagraphStyle(document, paragraph, styleCache)
        val (numberingInfo, ilvl) = resolveNumbering(document, paragraph, numCache)

        var isParagraphRtl = style?.isRtl ?: false

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
                    else -> style?.alignment ?: TextAlignment.LEFT
                }
            }
        }

        val bidi = try { paragraph.ctp?.pPr?.bidi } catch (e: Exception) { null }
        if (bidi != null || paragraph.ctp?.pPr?.isSetBidi == true) {
            isParagraphRtl = true
        }

        val fullParaText = try { paragraph.text ?: "" } catch (e: Exception) { "" }
        if (fullParaText.any {
            it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' || it in '\u08A0'..'\u08FF' ||
            it in '\uFB50'..'\uFDFF' || it in '\uFE70'..'\uFEFF'
        }) {
            isParagraphRtl = true
        }

        val pPr = paragraph.ctp?.pPr
        val spacing = try {
            val spacingLine = pPr?.spacing?.line?.toString()?.toDoubleOrNull()
            if (spacingLine != null && spacingLine > 0) {
                (spacingLine / 240.0).toFloat().coerceIn(1.0f, 3.0f)
            } else {
                val spacingBetween = paragraph.spacingBetween
                if (spacingBetween > 0) (spacingBetween / 240.0).toFloat().coerceAtLeast(1.0f)
                else style?.lineSpacing ?: 1.15f
            }
        } catch (e: Exception) { style?.lineSpacing ?: 1.15f }

        var spaceBeforePt = style?.spaceBeforePt ?: 0f
        var spaceAfterPt = style?.spaceAfterPt ?: 4f
        try {
            if (pPr?.isSetSpacing == true) {
                val sp = pPr.spacing
                val b = sp.before?.toString()?.toDoubleOrNull()
                if (b != null && b > 0) spaceBeforePt = (b / 20.0).toFloat().coerceIn(0f, 72f)
                val a = sp.after?.toString()?.toDoubleOrNull()
                if (a != null && a > 0) spaceAfterPt = (a / 20.0).toFloat().coerceIn(0f, 72f)
            }
        } catch (e: Exception) {}

        var indentLevel = ilvl
        try {
            if (indentLevel == 0 && pPr?.isSetInd == true) {
                val ind = pPr.ind
                val left = ind.left?.toString()?.toDoubleOrNull() ?: ind.start?.toString()?.toDoubleOrNull()
                if (left != null && left >= 360.0) {
                    indentLevel = (left / 720.0).toInt().coerceIn(1, 8)
                }
            }
        } catch (e: Exception) {}

        var paragraphShadingColor = style?.shadingColor
        try {
            if (pPr?.isSetShd == true) {
                val shdFill = pPr.shd.fill?.toString()
                if (!shdFill.isNullOrBlank() && shdFill != "auto" && shdFill != "none") {
                    parseHexColor(shdFill)?.let { paragraphShadingColor = it }
                }
            }
        } catch (e: Exception) {}

        var paragraphBorder = ParagraphBorder.NONE
        try {
            if (pPr?.isSetPBdr == true) {
                val pbdr = pPr.pBdr
                val hasTop = pbdr.isSetTop && pbdr.top.`val` != STBorder.NONE
                val hasBottom = pbdr.isSetBottom && pbdr.bottom.`val` != STBorder.NONE
                val hasLeft = pbdr.isSetLeft && pbdr.left.`val` != STBorder.NONE
                val hasRight = pbdr.isSetRight && pbdr.right.`val` != STBorder.NONE
                paragraphBorder = when {
                    hasTop && hasBottom && hasLeft && hasRight -> ParagraphBorder.ALL
                    hasBottom && !hasTop -> ParagraphBorder.BOTTOM
                    hasTop && !hasBottom -> ParagraphBorder.TOP
                    hasLeft || hasRight -> ParagraphBorder.LEFT
                    else -> ParagraphBorder.NONE
                }
            }
        } catch (e: Exception) {}

        val runs = paragraph.runs
        var builder = AnnotatedString.Builder()
        var runCount = 0

        var dominantFontSize: Int? = null
        var dominantFontFamily: String? = null
        var dominantIsBold: Boolean? = null
        var dominantIsItalic: Boolean? = null
        var dominantColor: Color? = null
        val runSizes = mutableListOf<Int>()
        val runFonts = mutableListOf<String>()

        fun flushText() {
            if (builder.length > 0 || runCount > 0) {
                dominantFontSize = if (runSizes.isNotEmpty()) runSizes.groupBy { it }.maxByOrNull { it.value.size }?.key else style?.fontSizeSp
                dominantFontFamily = if (runFonts.isNotEmpty()) runFonts.groupBy { it }.maxByOrNull { it.value.size }?.key else style?.fontFamily

                blocks.add(
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(builder.toAnnotatedString()),
                        alignment = align,
                        lineSpacing = spacing,
                        isRtl = isParagraphRtl,
                        indentLevel = indentLevel,
                        isBulletedList = numberingInfo?.isBullet == true,
                        bulletShape = numberingInfo?.bulletShape ?: BulletShape.DISC,
                        isNumberedList = numberingInfo != null && !numberingInfo.isBullet,
                        numberingStyle = numberingInfo?.numberingStyle ?: NumberingStyle.DECIMAL_DOT,
                        paragraphShadingColor = paragraphShadingColor,
                        paragraphBorder = paragraphBorder,
                        fontSize = dominantFontSize ?: style?.fontSizeSp ?: 12,
                        fontFamily = dominantFontFamily ?: style?.fontFamily ?: (if (isParagraphRtl) "Cairo" else "Calibri"),
                        isBold = dominantIsBold ?: style?.isBold ?: false,
                        isItalic = dominantIsItalic ?: style?.isItalic ?: false,
                        textColor = dominantColor ?: style?.color ?: Color.Black,
                        spaceBeforePt = spaceBeforePt,
                        spaceAfterPt = spaceAfterPt,
                        sourceElementIndex = bodyElementIndex
                    )
                )
                builder = AnnotatedString.Builder()
                runCount = 0
                runSizes.clear()
                runFonts.clear()
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
                        text = TextFieldValue(pText),
                        alignment = align,
                        lineSpacing = spacing,
                        isRtl = isParagraphRtl,
                        indentLevel = indentLevel,
                        isBulletedList = numberingInfo?.isBullet == true,
                        bulletShape = numberingInfo?.bulletShape ?: BulletShape.DISC,
                        isNumberedList = numberingInfo != null && !numberingInfo.isBullet,
                        numberingStyle = numberingInfo?.numberingStyle ?: NumberingStyle.DECIMAL_DOT,
                        paragraphShadingColor = paragraphShadingColor,
                        paragraphBorder = paragraphBorder,
                        fontSize = style?.fontSizeSp ?: 12,
                        fontFamily = style?.fontFamily ?: (if (isParagraphRtl) "Cairo" else "Calibri"),
                        isBold = style?.isBold ?: false,
                        isItalic = style?.isItalic ?: false,
                        textColor = style?.color ?: Color.Black,
                        spaceBeforePt = spaceBeforePt,
                        spaceAfterPt = spaceAfterPt,
                        sourceElementIndex = bodyElementIndex
                    )
                )
            }
            return blocks
        }

        for (run in runs) {
            val runText = run.text() ?: run.getText(0) ?: try { run.ctr.tList.joinToString("") { it.stringValue } } catch (e: Exception) { "" }
            val isArabic = runText.any {
                it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' || it in '\u08A0'..'\u08FF' ||
                it in '\uFB50'..'\uFDFF' || it in '\uFE70'..'\uFEFF'
            }
            if (isArabic) {
                isParagraphRtl = true
            }

            // Embedded Pictures inside run
            try {
                val pictures = run.embeddedPictures
                if (pictures.isNotEmpty()) {
                    flushText()
                    for (pic in pictures) {
                        val picData = pic.pictureData.data
                        var imgWidth = 0f
                        var imgHeight = 0f

                        if (picData != null && picData.isNotEmpty()) {
                            try {
                                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                                BitmapFactory.decodeByteArray(picData, 0, picData.size, opts)
                                if (opts.outWidth > 0 && opts.outHeight > 0) {
                                    imgWidth = opts.outWidth.toFloat().coerceAtMost(480f)
                                    val ratio = opts.outHeight.toFloat() / opts.outWidth.toFloat()
                                    imgHeight = (imgWidth * ratio).coerceAtMost(600f)
                                }
                            } catch (e: Exception) {}
                        }

                        blocks.add(
                            ImageBlock(
                                id = "img_${UUID.randomUUID()}",
                                imageData = picData,
                                width = imgWidth,
                                height = imgHeight,
                                alignment = align,
                                sourceElementIndex = bodyElementIndex
                            )
                        )
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

                val isBold = run.isBold || (rPr != null && (rPr.sizeOfBArray() > 0 || rPr.sizeOfBCsArray() > 0)) || (style?.isBold == true)
                if (dominantIsBold == null && isBold) dominantIsBold = true
                val fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal

                val isItalic = run.isItalic || (rPr != null && (rPr.sizeOfIArray() > 0 || rPr.sizeOfICsArray() > 0)) || (style?.isItalic == true)
                if (dominantIsItalic == null && isItalic) dominantIsItalic = true
                val fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal

                val isUnderline = run.underline != UnderlinePatterns.NONE || (rPr != null && rPr.sizeOfUArray() > 0)
                val isStrike = run.isStrikeThrough || (rPr != null && (rPr.sizeOfStrikeArray() > 0 || rPr.sizeOfDstrikeArray() > 0))
                val textDecoration = when {
                    isUnderline && isStrike -> TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
                    isUnderline -> TextDecoration.Underline
                    isStrike -> TextDecoration.LineThrough
                    else -> TextDecoration.None
                }

                var resolvedSizeSp: Int? = null
                if (rPr != null) {
                    if (isArabic && rPr.sizeOfSzCsArray() > 0) {
                        val szCs = rPr.getSzCsArray(0).`val`?.toString()?.toIntOrNull()
                        if (szCs != null && szCs > 0) resolvedSizeSp = szCs / 2
                    }
                    if (resolvedSizeSp == null && rPr.sizeOfSzArray() > 0) {
                        val sz = rPr.getSzArray(0).`val`?.toString()?.toIntOrNull()
                        if (sz != null && sz > 0) resolvedSizeSp = sz / 2
                    }
                }
                if (resolvedSizeSp == null && run.fontSize > 0) {
                    resolvedSizeSp = run.fontSize
                }
                if (resolvedSizeSp == null) {
                    resolvedSizeSp = style?.fontSizeSp ?: 12
                }

                runSizes.add(resolvedSizeSp)
                sizeFreq[resolvedSizeSp] = (sizeFreq[resolvedSizeSp] ?: 0) + 1
                val fontSize = resolvedSizeSp.sp

                val rawColorHex = try {
                    if (rPr != null && rPr.sizeOfColorArray() > 0) {
                        rPr.getColorArray(0).`val`?.toString()
                    } else run.color
                } catch (e: Exception) { run.color }

                val themeColor = try {
                    if (rPr != null && rPr.sizeOfColorArray() > 0) {
                        rPr.getColorArray(0).themeColor?.toString()
                    } else null
                } catch (e: Exception) { null }

                val themeTint = try {
                    if (rPr != null && rPr.sizeOfColorArray() > 0) {
                        rPr.getColorArray(0).themeTint?.toString()
                    } else null
                } catch (e: Exception) { null }

                val themeShade = try {
                    if (rPr != null && rPr.sizeOfColorArray() > 0) {
                        rPr.getColorArray(0).themeShade?.toString()
                    } else null
                } catch (e: Exception) { null }

                val textColor = resolveDocxColor(rawColorHex, themeColor, themeTint, themeShade)
                    ?: style?.color ?: Color.Black
                if (dominantColor == null && textColor != Color.Black) dominantColor = textColor

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
                        fontName = rFonts.cs ?: style?.fontFamilyCs ?: rFonts.ascii ?: rFonts.hAnsi
                    } else {
                        fontName = rFonts.ascii ?: rFonts.hAnsi ?: style?.fontFamily ?: rFonts.cs
                    }
                }
                if (fontName.isNullOrBlank()) {
                    fontName = try { run.fontFamily } catch (e: Exception) { null }
                        ?: try { run.fontName } catch (e: Exception) { null }
                }
                if (fontName.isNullOrBlank()) {
                    fontName = if (isArabic) (style?.fontFamilyCs ?: style?.fontFamily ?: "Cairo")
                    else (style?.fontFamily ?: "Calibri")
                }
                if (fontName.isNullOrBlank() || fontName == "Null" || fontName == "null") {
                    fontName = if (isArabic) "Cairo" else "Calibri"
                }

                runFonts.add(fontName)
                fontFreq[fontName] = (fontFreq[fontName] ?: 0) + 1

                val parsedFontFamily = try {
                    com.example.presentation.editor.components.AppFonts.getFontFamily(fontName)
                } catch (e: Throwable) {
                    androidx.compose.ui.text.font.FontFamily.Default
                }
                try {
                    com.example.presentation.editor.font.FontEngine.ensureFontDownloaded(fontName)
                } catch (e: Throwable) {}

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
        document: XWPFDocument,
        bodyElementIndex: Int,
        fontFreq: MutableMap<String, Int>,
        sizeFreq: MutableMap<Int, Int>,
        styleCache: MutableMap<String, ResolvedStyle>,
        numCache: MutableMap<String, NumberingInfo>
    ): TableBlock? {
        val rows = table.rows.size
        val cols = if (rows > 0) table.rows.maxOfOrNull { it.tableCells.size } ?: 0 else 0
        if (rows == 0 || cols == 0) return null

        val colWidthRatios = mutableListOf<Float>()
        try {
            val gridCols = table.ctTbl?.tblGrid?.gridColList
            if (!gridCols.isNullOrEmpty() && gridCols.size == cols) {
                val widths = gridCols.map { it.w?.toString()?.toDoubleOrNull() ?: 1000.0 }
                val total = widths.sum()
                if (total > 0) {
                    widths.forEach { colWidthRatios.add((it / total).toFloat()) }
                }
            }
        } catch (e: Exception) {}

        if (colWidthRatios.isEmpty() && rows > 0) {
            try {
                val firstRowCells = table.getRow(0)?.tableCells
                if (firstRowCells != null && firstRowCells.size == cols) {
                    val widths = firstRowCells.map { cell ->
                        cell.ctTc?.tcPr?.tcW?.w?.toString()?.toDoubleOrNull() ?: 1000.0
                    }
                    val total = widths.sum()
                    if (total > 0) {
                        widths.forEach { colWidthRatios.add((it / total).toFloat()) }
                    }
                }
            } catch (e: Exception) {}
        }

        val cells = mutableMapOf<String, TableCellModel>()
        val isTableRtl = try {
            table.ctTbl?.tblPr?.bidiVisual != null || table.ctTbl?.tblPr?.isSetBidiVisual == true
        } catch (e: Exception) { false }

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
                        val paraBlocks = parseParagraph(
                            paragraph = cellPara,
                            document = document,
                            bodyElementIndex = bodyElementIndex,
                            fontFreq = fontFreq,
                            sizeFreq = sizeFreq,
                            styleCache = styleCache,
                            numCache = numCache
                        )
                        cellTextBlocks.addAll(paraBlocks.filterIsInstance<TextBlock>())
                    }
                }

                cells["${r}_${c}"] = TableCellModel(cellTextBlocks, cellBgColor, isTableRtl)
            }
        }

        return TableBlock("tbl_${UUID.randomUUID()}", rows, cols, cells, isTableRtl, colWidthRatios, sourceElementIndex = bodyElementIndex)
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

    // =========================================================================
    // 2. EXPORT ENTRY POINT (ROUND-TRIP PRESERVATION & HIGH-FIDELITY CREATION)
    // =========================================================================

    fun exportDocx(state: EditorState, outputStream: OutputStream) {
        if (state.originalDocxBytes != null && state.originalDocxBytes.isNotEmpty()) {
            try {
                exportRoundTripDocx(state, state.originalDocxBytes, outputStream)
                return
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback cleanly to high-fidelity export if in-place sync hits unexpected corruptions
            }
        }
        exportNewDocx(state, outputStream)
    }

    fun exportDocxToByteArray(state: EditorState): ByteArray {
        val baos = ByteArrayOutputStream()
        exportDocx(state, baos)
        return baos.toByteArray()
    }

    // =========================================================================
    // 3. TRUE IN-PLACE ROUND-TRIP PRESERVATION ENGINE
    // =========================================================================

    private fun exportRoundTripDocx(
        state: EditorState,
        originalBytes: ByteArray,
        outputStream: OutputStream
    ) {
        val document = XWPFDocument(ByteArrayInputStream(originalBytes))

        // 1. Synchronize Section Properties (pgSz, pgMar, pgBorders, orientation)
        try {
            val body = document.document.body ?: document.document.addNewBody()
            val sectPr = if (body.isSetSectPr) body.sectPr else body.addNewSectPr()

            val pageSz = if (sectPr.isSetPgSz) sectPr.pgSz else sectPr.addNewPgSz()
            when (state.pageSize) {
                PageSize.A3 -> { pageSz.w = 16838.toBigInteger(); pageSz.h = 23811.toBigInteger() }
                PageSize.A4 -> { pageSz.w = 11906.toBigInteger(); pageSz.h = 16838.toBigInteger() }
                PageSize.A5 -> { pageSz.w = 8391.toBigInteger(); pageSz.h = 11906.toBigInteger() }
                PageSize.LETTER -> { pageSz.w = 12240.toBigInteger(); pageSz.h = 15840.toBigInteger() }
                PageSize.LEGAL -> { pageSz.w = 12240.toBigInteger(); pageSz.h = 20160.toBigInteger() }
            }

            val wVal = pageSz.w?.toString()?.toLongOrNull() ?: 11906L
            val hVal = pageSz.h?.toString()?.toLongOrNull() ?: 16838L
            if (state.pageOrientation == PageOrientation.LANDSCAPE) {
                if (wVal < hVal) {
                    pageSz.w = BigInteger.valueOf(hVal)
                    pageSz.h = BigInteger.valueOf(wVal)
                }
                pageSz.orient = STPageOrientation.LANDSCAPE
            } else {
                if (wVal > hVal) {
                    pageSz.w = BigInteger.valueOf(hVal)
                    pageSz.h = BigInteger.valueOf(wVal)
                }
                pageSz.orient = STPageOrientation.PORTRAIT
            }

            val pageMar = if (sectPr.isSetPgMar) sectPr.pgMar else sectPr.addNewPgMar()
            when (state.pageMargin) {
                PageMargin.NORMAL -> {
                    pageMar.top = 1440.toBigInteger(); pageMar.bottom = 1440.toBigInteger()
                    pageMar.left = 1440.toBigInteger(); pageMar.right = 1440.toBigInteger()
                }
                PageMargin.NARROW -> {
                    pageMar.top = 720.toBigInteger(); pageMar.bottom = 720.toBigInteger()
                    pageMar.left = 720.toBigInteger(); pageMar.right = 720.toBigInteger()
                }
                PageMargin.MODERATE -> {
                    pageMar.top = 1440.toBigInteger(); pageMar.bottom = 1440.toBigInteger()
                    pageMar.left = 1080.toBigInteger(); pageMar.right = 1080.toBigInteger()
                }
                PageMargin.WIDE -> {
                    pageMar.top = 1440.toBigInteger(); pageMar.bottom = 1440.toBigInteger()
                    pageMar.left = 2880.toBigInteger(); pageMar.right = 2880.toBigInteger()
                }
            }

            // Page Background Color
            if (state.pageColor != Color.White) {
                val background = document.document.background ?: document.document.addNewBackground()
                background.color = colorToHex(state.pageColor)
            }

            // Page Borders
            if (state.pageBorder.setting != BorderSetting.NONE) {
                applyPageBorder(sectPr, state.pageBorder)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Synchronize Header & Footer
        try {
            if (state.headerText.text.isNotEmpty() || state.watermarkText.isNotEmpty()) {
                val header = document.headerList.firstOrNull() ?: document.createHeader(HeaderFooterType.DEFAULT)
                if (header.paragraphs.isEmpty()) header.createParagraph()
                val hp = header.paragraphs.first()
                while (hp.runs.isNotEmpty()) hp.removeRun(0)
                hp.alignment = ParagraphAlignment.CENTER
                if (state.headerText.text.isNotEmpty()) {
                    val hr = hp.createRun()
                    hr.setText(state.headerText.text)
                    hr.fontFamily = state.fontFamily
                }
                if (state.watermarkText.isNotEmpty()) {
                    val wr = hp.createRun()
                    wr.setText("  [${state.watermarkText}]")
                    wr.color = "D3D3D3"
                    wr.fontSize = 20
                }
            }

            if (state.footerText.text.isNotEmpty()) {
                val footer = document.footerList.firstOrNull() ?: document.createFooter(HeaderFooterType.DEFAULT)
                if (footer.paragraphs.isEmpty()) footer.createParagraph()
                val fp = footer.paragraphs.first()
                while (fp.runs.isNotEmpty()) fp.removeRun(0)
                fp.alignment = ParagraphAlignment.CENTER
                val fr = fp.createRun()
                fr.setText(state.footerText.text + "  |  صفحة ")
                fr.fontFamily = state.fontFamily
                // Native OOXML Dynamic Page Number Field
                val fldSimple = fp.ctp.addNewFldSimple()
                fldSimple.instr = "PAGE"
                val fldRun = fldSimple.addNewR()
                fldRun.addNewT().stringValue = "1"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Synchronize Body Elements
        // Map original elements by index
        val originalElements = document.bodyElements.toList()

        for (block in state.blocks) {
            val srcIdx = block.sourceElementIndex
            val origElement = if (srcIdx != null && srcIdx in originalElements.indices) originalElements[srcIdx] else null

            when (block) {
                is TextBlock -> {
                    if (origElement is XWPFParagraph) {
                        updateExistingParagraph(origElement, block, state)
                    } else {
                        // Append newly added paragraph
                        processTextBlock(document, block, state)
                    }
                }

                is TableBlock -> {
                    if (origElement is XWPFTable) {
                        updateExistingTable(origElement, block, state)
                    } else {
                        // Append newly added table
                        processTableBlock(document, block, state)
                    }
                }

                is ImageBlock -> {
                    if (origElement !is XWPFParagraph) {
                        appendImageBlock(document, block)
                    }
                }

                is PageBreakBlock -> {
                    if (origElement !is XWPFParagraph) {
                        val p = document.createParagraph()
                        p.isPageBreak = true
                        p.createRun().addBreak(BreakType.PAGE)
                    }
                }

                is UnsupportedBlock -> {
                    // Left untouched in the original document
                }

                is BannerBlock, is CalloutBlock, is DividerBlock, is ShapeBlock -> {
                    if (origElement == null) {
                        appendGenericBlock(document, block)
                    }
                }
            }
        }

        document.write(outputStream)
        document.close()
    }

    private fun updateExistingParagraph(
        paragraph: XWPFParagraph,
        block: TextBlock,
        state: EditorState
    ) {
        // Clear old text runs while preserving paragraph properties, bookmarks, and IDs
        while (paragraph.runs.isNotEmpty()) {
            paragraph.removeRun(0)
        }

        paragraph.alignment = when (block.alignment) {
            TextAlignment.CENTER -> ParagraphAlignment.CENTER
            TextAlignment.RIGHT -> ParagraphAlignment.RIGHT
            TextAlignment.JUSTIFY -> ParagraphAlignment.BOTH
            TextAlignment.LEFT -> if (block.isRtl || state.isRtl) ParagraphAlignment.RIGHT else ParagraphAlignment.LEFT
        }

        try {
            val ctp = paragraph.ctp
            val ppr = if (ctp.isSetPPr) ctp.pPr else ctp.addNewPPr()

            if (block.isRtl || state.isRtl) {
                if (!ppr.isSetBidi) ppr.addNewBidi()
            }

            if (block.paragraphShadingColor != null) {
                val shd = if (ppr.isSetShd) ppr.shd else ppr.addNewShd()
                shd.fill = colorToHex(block.paragraphShadingColor)
            }
        } catch (e: Exception) {}

        populateRunsForBlock(paragraph, block, state)
    }

    private fun updateExistingTable(
        table: XWPFTable,
        block: TableBlock,
        state: EditorState
    ) {
        for (r in 0 until block.rows) {
            val row = table.getRow(r) ?: continue
            for (c in 0 until block.cols) {
                val cell = row.getCell(c) ?: continue
                val cellModel = block.cells["${r}_${c}"] ?: continue

                if (cellModel.backgroundColor != Color.Transparent) {
                    cell.color = colorToHex(cellModel.backgroundColor)
                }

                if (cellModel.textBlocks.isNotEmpty()) {
                    while (cell.paragraphs.isNotEmpty()) {
                        cell.removeParagraph(0)
                    }
                    for (tb in cellModel.textBlocks) {
                        processTextBlock(table.body.xwpfDocument, tb, state, cell)
                    }
                }
            }
        }
    }

    // =========================================================================
    // 4. HIGH-FIDELITY NEW OOXML GENERATION (FROM SCRATCH)
    // =========================================================================

    private fun exportNewDocx(state: EditorState, outputStream: OutputStream) {
        val document = XWPFDocument()

        // 1. Initialize Standard OOXML Styles (Normal, Heading 1, 2, 3, Title, Subtitle, Quote)
        ensureStandardStyles(document)

        // 2. Initialize Real OOXML Numbering Definitions
        ensureStandardNumbering(document)

        // 3. Configure Section Properties (pgSz, pgMar, pgBorders)
        val sectPr = document.document.body.addNewSectPr()
        val pageSz = sectPr.addNewPgSz()

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
            pageSz.orient = STPageOrientation.LANDSCAPE
        }

        val pageMar = sectPr.addNewPgMar()
        when (state.pageMargin) {
            PageMargin.NORMAL -> {
                pageMar.top = 1440.toBigInteger(); pageMar.bottom = 1440.toBigInteger()
                pageMar.left = 1440.toBigInteger(); pageMar.right = 1440.toBigInteger()
            }
            PageMargin.NARROW -> {
                pageMar.top = 720.toBigInteger(); pageMar.bottom = 720.toBigInteger()
                pageMar.left = 720.toBigInteger(); pageMar.right = 720.toBigInteger()
            }
            PageMargin.MODERATE -> {
                pageMar.top = 1440.toBigInteger(); pageMar.bottom = 1440.toBigInteger()
                pageMar.left = 1080.toBigInteger(); pageMar.right = 1080.toBigInteger()
            }
            PageMargin.WIDE -> {
                pageMar.top = 1440.toBigInteger(); pageMar.bottom = 1440.toBigInteger()
                pageMar.left = 2880.toBigInteger(); pageMar.right = 2880.toBigInteger()
            }
        }

        // Page Color
        if (state.pageColor != Color.White) {
            val background = document.document.addNewBackground()
            background.color = colorToHex(state.pageColor)
        }

        // Page Borders
        if (state.pageBorder.setting != BorderSetting.NONE) {
            applyPageBorder(sectPr, state.pageBorder)
        }

        // Header & Watermark
        if (state.headerText.text.isNotEmpty() || state.watermarkText.isNotEmpty()) {
            try {
                val header = document.createHeader(HeaderFooterType.DEFAULT)
                val p = header.createParagraph()
                p.alignment = ParagraphAlignment.CENTER
                if (state.headerText.text.isNotEmpty()) {
                    val hr = p.createRun()
                    hr.setText(state.headerText.text)
                    hr.fontFamily = state.fontFamily
                }
                if (state.watermarkText.isNotEmpty()) {
                    val wp = p.createRun()
                    wp.setText("  [${state.watermarkText}]")
                    wp.color = "D3D3D3"
                    wp.fontSize = 20
                }
            } catch (e: Exception) {}
        }

        // Footer & Native Page Number Field
        if (state.footerText.text.isNotEmpty()) {
            try {
                val footer = document.createFooter(HeaderFooterType.DEFAULT)
                val p = footer.createParagraph()
                p.alignment = ParagraphAlignment.CENTER
                val fr = p.createRun()
                fr.setText(state.footerText.text + "  |  صفحة ")
                fr.fontFamily = state.fontFamily

                // Native OOXML Dynamic Page Number Field
                val fldSimple = p.ctp.addNewFldSimple()
                fldSimple.instr = "PAGE"
                val fldRun = fldSimple.addNewR()
                fldRun.addNewT().stringValue = "1"
            } catch (e: Exception) {}
        }

        // Process all document blocks
        for (block in state.blocks) {
            when (block) {
                is TextBlock -> processTextBlock(document, block, state)
                is TableBlock -> processTableBlock(document, block, state)
                is PageBreakBlock -> {
                    val p = document.createParagraph()
                    p.isPageBreak = true
                    p.createRun().addBreak(BreakType.PAGE)
                }
                is ImageBlock -> appendImageBlock(document, block)
                is ShapeBlock, is BannerBlock, is CalloutBlock, is DividerBlock -> appendGenericBlock(document, block)
                is UnsupportedBlock -> {
                    val p = document.createParagraph()
                    val run = p.createRun()
                    run.isItalic = true
                    run.color = "888888"
                    run.setText("[عنصر مدمج محفوظ: ${block.description}]")
                }
            }
        }

        document.write(outputStream)
        document.close()
    }

    private fun ensureStandardStyles(document: XWPFDocument) {
        try {
            val styles = document.createStyles()
            if (styles.getStyle("Normal") == null) {
                val ctStyle = CTStyle.Factory.newInstance().apply {
                    styleId = "Normal"
                    type = STStyleType.PARAGRAPH
                    addNewName().`val` = "Normal"
                    val rPr = addNewRPr()
                    val rFonts = rPr.addNewRFonts()
                    rFonts.ascii = "Calibri"
                    rFonts.hAnsi = "Calibri"
                    rFonts.cs = "Cairo"
                    rFonts.eastAsia = "Calibri"
                }
                styles.addStyle(XWPFStyle(ctStyle))
            }
        } catch (e: Exception) {}
    }

    private fun ensureStandardNumbering(document: XWPFDocument) {
        try {
            val numbering = document.createNumbering()
            
            // 1. Bullet List Definition (abstractNumId = 1, numId = 1)
            val ctAbstractNumBullet = CTAbstractNum.Factory.newInstance().apply {
                abstractNumId = BigInteger.valueOf(1L)
                for (lvlIdx in 0..8) {
                    val lvl = addNewLvl()
                    lvl.ilvl = BigInteger.valueOf(lvlIdx.toLong())
                    lvl.addNewNumFmt().`val` = STNumberFormat.BULLET
                    lvl.addNewLvlText().`val` = "•"
                    val pPr = lvl.addNewPPr()
                    val ind = pPr.addNewInd()
                    ind.left = BigInteger.valueOf((720 * (lvlIdx + 1)).toLong())
                    ind.hanging = BigInteger.valueOf(360L)
                }
            }
            numbering.addAbstractNum(XWPFAbstractNum(ctAbstractNumBullet))
            numbering.addNum(BigInteger.valueOf(1L))

            // 2. Decimal Numbered List Definition (abstractNumId = 2, numId = 2)
            val ctAbstractNumDecimal = CTAbstractNum.Factory.newInstance().apply {
                abstractNumId = BigInteger.valueOf(2L)
                for (lvlIdx in 0..8) {
                    val lvl = addNewLvl()
                    lvl.ilvl = BigInteger.valueOf(lvlIdx.toLong())
                    lvl.addNewNumFmt().`val` = STNumberFormat.DECIMAL
                    lvl.addNewLvlText().`val` = "%${lvlIdx + 1}."
                    val pPr = lvl.addNewPPr()
                    val ind = pPr.addNewInd()
                    ind.left = BigInteger.valueOf((720 * (lvlIdx + 1)).toLong())
                    ind.hanging = BigInteger.valueOf(360L)
                }
            }
            numbering.addAbstractNum(XWPFAbstractNum(ctAbstractNumDecimal))
            numbering.addNum(BigInteger.valueOf(2L))
        } catch (e: Exception) {}
    }

    private fun applyPageBorder(sectPr: CTSectPr, border: PageBorder) {
        try {
            val pgBorders = if (sectPr.isSetPgBorders) sectPr.pgBorders else sectPr.addNewPgBorders()
            val valType = when (border.style) {
                BorderStyle.SOLID -> STBorder.SINGLE
                BorderStyle.DASHED -> STBorder.DASHED
                BorderStyle.DOTTED -> STBorder.DOTTED
                BorderStyle.DOUBLE -> STBorder.DOUBLE
                else -> STBorder.NONE
            }
            val borderColor = colorToHex(border.color)
            val borderWidth = (border.widthPt * 8).toLong()

            listOf(
                if (pgBorders.isSetTop) pgBorders.top else pgBorders.addNewTop(),
                if (pgBorders.isSetBottom) pgBorders.bottom else pgBorders.addNewBottom(),
                if (pgBorders.isSetLeft) pgBorders.left else pgBorders.addNewLeft(),
                if (pgBorders.isSetRight) pgBorders.right else pgBorders.addNewRight()
            ).forEach { b ->
                b.`val` = valType
                b.color = borderColor
                b.sz = BigInteger.valueOf(borderWidth)
                b.space = BigInteger.valueOf(24)
            }
        } catch (e: Exception) {}
    }

    private fun processTextBlock(
        document: XWPFDocument,
        block: TextBlock,
        state: EditorState,
        parentCell: XWPFTableCell? = null
    ) {
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
            if (block.spaceBeforePt > 0f) {
                paragraph.spacingBefore = (block.spaceBeforePt * 20).toInt()
            }
            if (block.spaceAfterPt > 0f) {
                paragraph.spacingAfter = (block.spaceAfterPt * 20).toInt()
            }
            if (block.indentLevel > 0) {
                paragraph.indentationLeft = (block.indentLevel * 720)
            }

            val ctp = paragraph.ctp
            val ppr = if (ctp.isSetPPr) ctp.pPr else ctp.addNewPPr()

            // RTL / Bidi XML Tag for MS Word compatibility
            if (block.isRtl || state.isRtl) {
                if (!ppr.isSetBidi) {
                    ppr.addNewBidi()
                }
            }

            // Real OOXML Native Numbering / Bullets
            if (block.isBulletedList || block.isNumberedList) {
                try {
                    val numPr = if (ppr.isSetNumPr) ppr.numPr else ppr.addNewNumPr()
                    val ilvl = if (numPr.isSetIlvl) numPr.ilvl else numPr.addNewIlvl()
                    ilvl.`val` = BigInteger.valueOf(block.indentLevel.toLong())
                    val numId = if (numPr.isSetNumId) numPr.numId else numPr.addNewNumId()
                    numId.`val` = BigInteger.valueOf(if (block.isBulletedList) 1L else 2L)
                } catch (e: Exception) {}
            }

            // Paragraph Shading
            if (block.paragraphShadingColor != null) {
                val shd = if (ppr.isSetShd) ppr.shd else ppr.addNewShd()
                shd.fill = colorToHex(block.paragraphShadingColor)
            }
        } catch (e: Exception) {}

        populateRunsForBlock(paragraph, block, state)
    }

    private fun populateRunsForBlock(
        paragraph: XWPFParagraph,
        block: TextBlock,
        state: EditorState
    ) {
        val annotatedString = block.text.annotatedString

        if (annotatedString.text.isEmpty()) {
            return
        }

        var currentIndex = 0
        while (currentIndex < annotatedString.text.length) {
            val run = paragraph.createRun()

            var runFontName = block.fontFamily.ifEmpty { state.fontFamily }
            run.fontFamily = runFontName
            run.fontSize = if (block.fontSize > 0) block.fontSize else state.fontSize
            if (block.isBold) run.isBold = true
            if (block.isItalic) run.isItalic = true
            if (block.textColor != Color.Black && block.textColor != Color.Unspecified) {
                run.color = colorToHex(block.textColor)
            }

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
                if (style.background != Color.Transparent && style.background != Color.Unspecified) {
                    try {
                        val hlName = when {
                            style.background == Color.Yellow -> "yellow"
                            style.background == Color.Green -> "green"
                            style.background == Color.Cyan -> "cyan"
                            style.background == Color.Magenta -> "magenta"
                            style.background == Color.Red -> "red"
                            style.background == Color.LightGray -> "lightGray"
                            else -> "yellow"
                        }
                        run.setTextHighlightColor(hlName)
                    } catch (e: Exception) {}
                }
            }

            // WordprocessingML Font XML attributes for Arabic (Complex Script) & English
            try {
                val ctr = run.ctr
                val rPr = if (ctr.isSetRPr) ctr.rPr else ctr.addNewRPr()
                val rFonts = if (rPr.sizeOfRFontsArray() > 0) rPr.getRFontsArray(0) else rPr.addNewRFonts()
                rFonts.ascii = runFontName
                rFonts.hAnsi = runFontName
                rFonts.cs = if (block.isRtl || state.isRtl) runFontName else "Cairo"
                rFonts.eastAsia = runFontName

                // Set CS bold & italic for Arabic scripts
                if (run.isBold && rPr.sizeOfBCsArray() == 0) rPr.addNewBCs()
                if (run.isItalic && rPr.sizeOfICsArray() == 0) rPr.addNewICs()

                // Set CS size (half-points)
                val halfPoints = BigInteger.valueOf((run.fontSize * 2).toLong())
                if (rPr.sizeOfSzArray() == 0) rPr.addNewSz().`val` = halfPoints
                if (rPr.sizeOfSzCsArray() == 0) rPr.addNewSzCs().`val` = halfPoints

                if (block.isRtl || state.isRtl) {
                    if (rPr.sizeOfRtlArray() == 0) rPr.addNewRtl()
                }
            } catch (e: Exception) {}

            val segmentText = annotatedString.text.substring(currentIndex, nextChangeIndex)
            run.setText(segmentText)

            currentIndex = nextChangeIndex
        }
    }

    private fun processTableBlock(document: XWPFDocument, block: TableBlock, state: EditorState) {
        val table = document.createTable(block.rows, block.cols)

        // RTL Visual & Alignment
        try {
            val tblPr = table.ctTbl.tblPr ?: table.ctTbl.addNewTblPr()
            if (block.isRtl || state.isRtl) {
                if (!tblPr.isSetBidiVisual) tblPr.addNewBidiVisual()
            }
            val jc = if (tblPr.isSetJc) tblPr.jc else tblPr.addNewJc()
            jc.`val` = STJcTable.CENTER
        } catch (e: Exception) {}

        // Column width ratios via tblGrid
        try {
            if (block.colWidthRatios.isNotEmpty() && block.colWidthRatios.size == block.cols) {
                val tblGrid = table.ctTbl.tblGrid ?: table.ctTbl.addNewTblGrid()
                tblGrid.gridColList.clear()
                for (ratio in block.colWidthRatios) {
                    val gc = tblGrid.addNewGridCol()
                    gc.w = BigInteger.valueOf((ratio * 9000).toLong())
                }
            }
        } catch (e: Exception) {}

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

    private fun appendImageBlock(document: XWPFDocument, block: ImageBlock) {
        val p = document.createParagraph()
        p.alignment = when (block.alignment) {
            TextAlignment.LEFT -> ParagraphAlignment.LEFT
            TextAlignment.RIGHT -> ParagraphAlignment.RIGHT
            else -> ParagraphAlignment.CENTER
        }
        val run = p.createRun()
        if (block.imageData != null && block.imageData.isNotEmpty()) {
            try {
                val bais = ByteArrayInputStream(block.imageData)
                val wPt = if (block.width > 0) block.width.toDouble() else 300.0
                val hPt = if (block.height > 0) block.height.toDouble() else 200.0
                val widthEmu = Units.toEMU(wPt)
                val heightEmu = Units.toEMU(hPt)
                run.addPicture(bais, Document.PICTURE_TYPE_PNG, "image_${block.id}.png", widthEmu, heightEmu)
            } catch (e: Exception) {
                run.setText("[Image: ${block.uri.ifEmpty { "Embedded Image" }}]")
            }
        } else {
            run.setText("[Image: ${block.uri.ifEmpty { "Embedded Image" }}]")
        }
    }

    private fun appendGenericBlock(document: XWPFDocument, block: DocumentBlock) {
        val p = document.createParagraph()
        when (block) {
            is ShapeBlock -> {
                val run = p.createRun()
                run.setText("[Shape: ${block.type.name}]")
            }
            is BannerBlock -> {
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
                val run = p.createRun()
                run.setText("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            }
            else -> {}
        }
    }

    // =========================================================================
    // 5. COLOR UTILITIES
    // =========================================================================

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
