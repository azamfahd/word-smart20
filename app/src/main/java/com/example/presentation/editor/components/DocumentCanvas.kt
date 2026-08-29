package com.example.presentation.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.Image
import com.example.presentation.editor.*

fun createSafeTextStyle(
    fontSizeSp: Int,
    fontFamilyStr: String,
    lineSpacingFactor: Float,
    isBold: Boolean = false,
    isItalic: Boolean = false,
    textColor: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Unspecified,
    isRtl: Boolean = true
): TextStyle {
    val family = when (fontFamilyStr.lowercase()) {
        "times new roman", "georgia", "garamond", "cambria", "book antiqua", "palatino linotype", "aptos serif" -> FontFamily.Serif
        "courier new", "consolas", "lucida console" -> FontFamily.Monospace
        "comic sans ms" -> FontFamily.Cursive
        else -> FontFamily.SansSerif
    }
    val safeLineSpacing = lineSpacingFactor.coerceAtLeast(1.0f)
    return TextStyle(
        fontSize = fontSizeSp.sp,
        fontFamily = family,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
        color = textColor,
        textAlign = textAlign,
        lineHeight = safeLineSpacing.em,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        ),
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        textDirection = if (isRtl) androidx.compose.ui.text.style.TextDirection.ContentOrRtl else androidx.compose.ui.text.style.TextDirection.Ltr
    )
}

@Composable
fun DocumentCanvas(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(state.zoomScale) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(state.zoomScale) {
        scale = state.zoomScale
    }

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // Dynamically paginate real parsed blocks using TextMeasurer
    val pages = remember(state.blocks, state.fontSize, state.fontFamily, state.lineSpacing, state.pageSize, state.pageOrientation, state.pageMargin) {
        val (pageWidth, pageHeight) = when(state.pageSize) {
            PageSize.A4 -> 794.dp to 1123.dp
            PageSize.A3 -> 1123.dp to 1587.dp
            PageSize.LETTER -> 816.dp to 1056.dp
            PageSize.LEGAL -> 816.dp to 1344.dp
            PageSize.A5 -> 559.dp to 794.dp
        }
        
        val finalWidth = if (state.pageOrientation == PageOrientation.LANDSCAPE) pageHeight else pageWidth
        val finalHeight = if (state.pageOrientation == PageOrientation.LANDSCAPE) pageWidth else pageHeight

        val pagePaddingHorizontal = when(state.pageMargin) {
            PageMargin.NORMAL -> 72.dp
            PageMargin.NARROW -> 36.dp
            PageMargin.MODERATE -> 54.dp
            PageMargin.WIDE -> 108.dp
        }

        val pagePaddingVertical = when(state.pageMargin) {
            PageMargin.NORMAL -> 16.dp
            PageMargin.NARROW -> 8.dp
            PageMargin.MODERATE -> 16.dp
            PageMargin.WIDE -> 24.dp
        }

        val availableHeightPx = with(density) { (finalHeight - 128.dp - (pagePaddingVertical * 2)).toPx() } // 128.dp is header(64) + footer(64)
        val availableWidthPx = with(density) { (finalWidth - (pagePaddingHorizontal * 2)).toPx() }
        val spacingPx = with(density) { 10.dp.toPx() }

        val calculatedPages = mutableListOf<MutableList<DocumentBlock>>()
        var currentPage = mutableListOf<DocumentBlock>()
        var currentHeight = 0f

        for (block in state.blocks) {
            if (block is PageBreakBlock) {
                if (currentPage.isNotEmpty()) {
                    calculatedPages.add(currentPage)
                    currentPage = mutableListOf()
                } else if (calculatedPages.isEmpty()) {
                    calculatedPages.add(mutableListOf())
                }
                currentHeight = 0f
                continue
            }

            val blockHeight = when (block) {
                is TextBlock -> {
                    val style = createSafeTextStyle(
                        fontSizeSp = state.fontSize,
                        fontFamilyStr = state.fontFamily,
                        lineSpacingFactor = state.lineSpacing,
                        isBold = state.isBold,
                        isItalic = state.isItalic,
                        isRtl = state.isRtl
                    )
                    try {
                        val result = textMeasurer.measure(
                            text = block.text.annotatedString,
                            style = style,
                            constraints = androidx.compose.ui.unit.Constraints(maxWidth = availableWidthPx.toInt())
                        )
                        result.size.height.toFloat()
                    } catch (e: Exception) {
                        with(density) { 24.dp.toPx() }
                    }
                }
                is TableBlock -> {
                    var totalTableHeight = 0f
                    val cellWidthPx = availableWidthPx / block.cols.coerceAtLeast(1)
                    for (r in 0 until block.rows) {
                        var maxRowHeight = with(density) { 32.dp.toPx() } // Min height
                        for (c in 0 until block.cols) {
                            val cell = block.cells["${r}_${c}"]
                            var cellHeight = with(density) { 16.dp.toPx() } // Padding
                            cell?.textBlocks?.forEach { tb ->
                                val style = TextStyle(
                                    fontSize = state.fontSize.sp,
                                    lineHeight = (state.fontSize.toFloat() * tb.lineSpacing).sp
                                )
                                try {
                                    val result = textMeasurer.measure(
                                        text = tb.text.annotatedString,
                                        style = style,
                                        constraints = Constraints(maxWidth = cellWidthPx.toInt() - with(density){16.dp.toPx()}.toInt())
                                    )
                                    cellHeight += result.size.height.toFloat()
                                } catch (e: Exception) {
                                    cellHeight += with(density) { 24.dp.toPx() }
                                }
                            }
                            if (cellHeight > maxRowHeight) maxRowHeight = cellHeight
                        }
                        totalTableHeight += maxRowHeight
                    }
                    totalTableHeight
                }
                is ImageBlock -> {
                    if (block.imageData != null) {
                        try {
                            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeByteArray(block.imageData, 0, block.imageData.size, opts)
                            if (opts.outWidth > 0 && opts.outHeight > 0) {
                                val aspectRatio = opts.outHeight.toFloat() / opts.outWidth.toFloat()
                                val scaledWidth = minOf(opts.outWidth.toFloat(), availableWidthPx)
                                scaledWidth * aspectRatio
                            } else {
                                with(density) { 216.dp.toPx() }
                            }
                        } catch(e: Exception) { with(density) { 216.dp.toPx() } }
                    } else {
                        with(density) { 216.dp.toPx() }
                    }
                }
                is ShapeBlock -> with(density) { 106.dp.toPx() }
                else -> 0f
            }

            val addedHeight = blockHeight + if (currentPage.isNotEmpty()) spacingPx else 0f

            if (currentHeight + addedHeight > availableHeightPx && currentPage.isNotEmpty()) {
                calculatedPages.add(currentPage)
                currentPage = mutableListOf(block)
                currentHeight = blockHeight
            } else {
                currentPage.add(block)
                currentHeight += addedHeight
            }
        }
        
        if (currentPage.isNotEmpty() || calculatedPages.isEmpty()) {
            calculatedPages.add(currentPage)
        }
        
        calculatedPages
    }

    val workspaceBgColor = when (state.viewMode) {
        ViewMode.PRINT_LAYOUT -> Color(0xFFE5E9EE) // Desktop Office Gray workspace
        ViewMode.WEB_LAYOUT -> Color(0xFFF1F5F9)   // Clean Web Canvas Gray
        ViewMode.READ_MODE -> Color(0xFFFDFBF7)    // Eye-care Warm Reading Canvas
    }

    LaunchedEffect(scale) {
        if (scale <= 1.0f) {
            offsetX = 0f
        }
    }

    LaunchedEffect(state.zoomScale) {
        scale = state.zoomScale
        if (state.zoomScale <= 1.0f) {
            offsetX = 0f
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(workspaceBgColor)
            .clipToBounds(),
        contentAlignment = Alignment.TopCenter
    ) {
        val containerMaxWidth = maxWidth

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(density) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(0.4f, 3.5f)
                        scale = newScale
                        if (newScale <= 1.0f) {
                            offsetX = 0f
                        } else {
                            val canvasWidthPx = containerMaxWidth.toPx()
                            val rawPageWidth = when(state.pageSize) {
                                PageSize.A4 -> if (state.pageOrientation == PageOrientation.LANDSCAPE) 1123.dp else 794.dp
                                PageSize.A3 -> if (state.pageOrientation == PageOrientation.LANDSCAPE) 1587.dp else 1123.dp
                                PageSize.LETTER -> if (state.pageOrientation == PageOrientation.LANDSCAPE) 1056.dp else 816.dp
                                PageSize.LEGAL -> if (state.pageOrientation == PageOrientation.LANDSCAPE) 1344.dp else 816.dp
                                PageSize.A5 -> if (state.pageOrientation == PageOrientation.LANDSCAPE) 794.dp else 559.dp
                            }
                            val scaledPageWidthPx = rawPageWidth.toPx() * newScale
                            val maxOffsetX = ((scaledPageWidthPx - canvasWidthPx) / 2f).coerceAtLeast(0f)
                            offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                        }
                        offsetY += pan.y
                    }
                }
        ) {
        val (pageWidth, pageHeight) = when(state.pageSize) {
            PageSize.A4 -> 794.dp to 1123.dp
            PageSize.A3 -> 1123.dp to 1587.dp
            PageSize.LETTER -> 816.dp to 1056.dp
            PageSize.LEGAL -> 816.dp to 1344.dp
            PageSize.A5 -> 559.dp to 794.dp
        }
        val targetWidth = if (state.pageOrientation == PageOrientation.LANDSCAPE) pageHeight else pageWidth
        val targetHeight = if (state.pageOrientation == PageOrientation.LANDSCAPE) pageWidth else pageHeight

        val effectiveScale = scale
        val scaledWidth = targetWidth * effectiveScale
        val scaledHeight = targetHeight * effectiveScale

        when (state.viewMode) {
            ViewMode.PRINT_LAYOUT -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            translationX = offsetX,
                            translationY = offsetY
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(vertical = 20.dp, horizontal = 8.dp)
                ) {
                    itemsIndexed(pages) { pageIndex, pageBlocks ->
                        Box(
                            modifier = Modifier
                                .size(scaledWidth, scaledHeight)
                                .graphicsLayer(
                                    scaleX = effectiveScale,
                                    scaleY = effectiveScale,
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                                )
                        ) {
                            A4PageCard(
                                pageNumber = pageIndex + 1,
                                totalPages = pages.size,
                                pageBlocks = pageBlocks,
                                state = state,
                                onEvent = onEvent
                            )
                        }

                        // Realistic gray gap between sequential A4 sheets
                        if (pageIndex < pages.size - 1) {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }

            ViewMode.WEB_LAYOUT -> {
                // Continuous fluid layout (Windows Word Web Layout mode)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            translationX = offsetX,
                            translationY = offsetY
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .widthIn(max = 900.dp)
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(4.dp)),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = state.pageColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                state.blocks.forEach { block ->
                                    if (block !is PageBreakBlock) {
                                        RenderDocumentBlock(block = block, state = state, onEvent = onEvent)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ViewMode.READ_MODE -> {
                // Distraction-free Reading view (Windows Word Reading View)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            translationX = offsetX,
                            translationY = offsetY
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(vertical = 24.dp, horizontal = 20.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .widthIn(max = 760.dp)
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 40.dp, vertical = 36.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = state.documentTitle,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    textAlign = if (state.isRtl) TextAlign.Right else TextAlign.Left,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                                state.blocks.forEach { block ->
                                    if (block !is PageBreakBlock) {
                                        RenderDocumentBlock(block = block, state = state, onEvent = onEvent)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun A4PageCard(
    pageNumber: Int,
    totalPages: Int,
    pageBlocks: List<DocumentBlock>,
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit
) {
    val pageNumString = if (state.numeralSystem == NumeralSystem.ARABIC) {
        pageNumber.toString().map { c ->
            when (c) {
                '0' -> '٠'; '1' -> '١'; '2' -> '٢'; '3' -> '٣'; '4' -> '٤'
                '5' -> '٥'; '6' -> '٦'; '7' -> '٧'; '8' -> '٨'; '9' -> '٩'
                else -> c
            }
        }.joinToString("")
    } else {
        pageNumber.toString()
    }

    val totalPagesString = if (state.numeralSystem == NumeralSystem.ARABIC) {
        totalPages.toString().map { c ->
            when (c) {
                '0' -> '٠'; '1' -> '١'; '2' -> '٢'; '3' -> '٣'; '4' -> '٤'
                '5' -> '٥'; '6' -> '٦'; '7' -> '٧'; '8' -> '٨'; '9' -> '٩'
                else -> c
            }
        }.joinToString("")
    } else {
        totalPages.toString()
    }

    val (pageWidth, pageHeight) = when(state.pageSize) {
        PageSize.A4 -> 794.dp to 1123.dp
        PageSize.A3 -> 1123.dp to 1587.dp
        PageSize.LETTER -> 816.dp to 1056.dp
        PageSize.LEGAL -> 816.dp to 1344.dp
        PageSize.A5 -> 559.dp to 794.dp
    }
    
    val finalWidth = if (state.pageOrientation == PageOrientation.LANDSCAPE) pageHeight else pageWidth
    val finalHeight = if (state.pageOrientation == PageOrientation.LANDSCAPE) pageWidth else pageHeight

    val pagePaddingHorizontal = when(state.pageMargin) {
        PageMargin.NORMAL -> 72.dp
        PageMargin.NARROW -> 36.dp
        PageMargin.MODERATE -> 54.dp
        PageMargin.WIDE -> 108.dp
    }
    
    val pagePaddingVertical = when(state.pageMargin) {
        PageMargin.NORMAL -> 16.dp
        PageMargin.NARROW -> 8.dp
        PageMargin.MODERATE -> 16.dp
        PageMargin.WIDE -> 24.dp
    }

    val borderModifier = if (state.pageBorder.setting != BorderSetting.NONE) {
        val strokeWidth = state.pageBorder.widthPt.dp
        when (state.pageBorder.style) {
            BorderStyle.SOLID -> Modifier.border(strokeWidth, state.pageBorder.color)
            BorderStyle.DASHED -> Modifier.border(androidx.compose.foundation.BorderStroke(strokeWidth, state.pageBorder.color)) // Could add dashed effect with PathEffect in Canvas but border is simpler
            BorderStyle.DOUBLE -> Modifier.border(strokeWidth, state.pageBorder.color).padding(strokeWidth * 2).border(strokeWidth, state.pageBorder.color)
            else -> Modifier.border(strokeWidth, state.pageBorder.color)
        }
    } else Modifier

    Card(
        modifier = Modifier
            .width(finalWidth)
            .height(finalHeight)
            .shadow(
                elevation = if (state.pageBorder.setting == BorderSetting.SHADOW) 16.dp else 8.dp,
                shape = RoundedCornerShape(2.dp),
                spotColor = Color(0x33000000)
            )
            .border(1.dp, Color(0xFFD0D7DE), RoundedCornerShape(2.dp)),
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(containerColor = state.pageColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Decorative Page Accent Stripes / Header Bands based on template
            when (state.pageStripeStyle) {
                PageStripeStyle.SIDE_BAR_RIGHT -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .width(28.dp)
                            .fillMaxHeight()
                            .background(state.pageAccentColor ?: Color(0xFF0F766E))
                    )
                }
                PageStripeStyle.SIDE_BAR_LEFT -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .width(28.dp)
                            .fillMaxHeight()
                            .background(state.pageAccentColor ?: Color(0xFF4338CA))
                    )
                }
                PageStripeStyle.TOP_BAR -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .background(state.pageAccentColor ?: Color(0xFF1E3A8A))
                    )
                }
                PageStripeStyle.LETTERHEAD_HEADER -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .background(state.pageAccentColor ?: Color(0xFF1E3A8A))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(state.pageSecondaryColor ?: Color(0xFF93C5FD))
                        )
                    }
                }
                PageStripeStyle.CERTIFICATE_GOLD -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .border(2.dp, state.pageAccentColor ?: Color(0xFFB45309))
                            .padding(4.dp)
                            .border(1.dp, (state.pageAccentColor ?: Color(0xFFB45309)).copy(alpha = 0.5f))
                    )
                }
                PageStripeStyle.RESUME_HEADER_BAND -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(state.pageAccentColor ?: Color(0xFF4338CA))
                    )
                }
                PageStripeStyle.NONE -> {}
            }

            // Watermark Layer (Background)
            if (state.watermarkText.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.watermarkText,
                        color = Color.LightGray.copy(alpha = 0.5f),
                        fontSize = 120.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer(rotationZ = -45f)
                    )
                }
            }

            // Document Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (state.pageBorder.setting != BorderSetting.NONE) 12.dp else 0.dp) // Border spacing
                    .then(borderModifier)
            ) {
                // Header Area (Double-tap to activate header editing)
                PageHeaderZone(
                    isEditing = state.isEditingHeaderFooter,
                    headerText = state.headerText,
                    documentTitle = state.documentTitle,
                    paddingHorizontal = pagePaddingHorizontal,
                    onTextChange = { onEvent(RibbonEvent.OnHeaderTextChanged(it)) },
                    onDoubleTap = { onEvent(RibbonEvent.OnToggleHeaderFooterMode) }
                )

                // Main Document Content Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = pagePaddingHorizontal, vertical = pagePaddingVertical)
                        .alpha(if (state.isEditingHeaderFooter) 0.35f else 1f)
                        .pointerInput(state.isEditingHeaderFooter) {
                            if (state.isEditingHeaderFooter) {
                                detectTapGestures(onDoubleTap = { onEvent(RibbonEvent.OnToggleHeaderFooterMode) })
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (pageBlocks.isEmpty()) {
                            // Empty page placeholder
                            BasicTextField(
                                value = "",
                                onValueChange = {},
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(fontSize = state.fontSize.sp)
                            )
                        } else {
                            pageBlocks.forEach { block ->
                                RenderDocumentBlock(
                                    block = block,
                                    state = state,
                                    onEvent = onEvent
                                )
                            }
                        }
                    }
                }

                // Footer Area (Shows page number & custom footer text)
                PageFooterZone(
                    pageNumber = pageNumString,
                    totalPages = totalPagesString,
                    isEditing = state.isEditingHeaderFooter,
                    footerText = state.footerText,
                    showPageNumbers = state.showPageNumbers,
                    paddingHorizontal = pagePaddingHorizontal,
                    onTextChange = { onEvent(RibbonEvent.OnFooterTextChanged(it)) },
                    onDoubleTap = { onEvent(RibbonEvent.OnToggleHeaderFooterMode) }
                )
            }
        }
    }
}

@Composable
fun RenderDocumentBlock(
    block: DocumentBlock,
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit
) {
    when (block) {
        is TextBlock -> {
            val isFocused = state.activeBlockId == block.id
            
            // Use block's alignment, fallback to state if we are currently editing it and it matches? No, prefer block's alignment.
            // Wait, if we want ribbon buttons to work on the currently focused block, we need the UI to reflect it. But for now, we just want it to render correctly from DOCX.
            // A good compromise: If it's focused, use the state (so Ribbon works). If not focused, use the block's inherent properties!
            val currentAlign = if (isFocused) state.alignment else block.alignment
            val currentLineSpacing = if (isFocused) state.lineSpacing else block.lineSpacing
            val currentIsRtl = if (isFocused) state.isTextRtl || state.isRtl else block.isRtl

            val alignment = when (currentAlign) {
                TextAlignment.LEFT -> if (currentIsRtl) TextAlign.Right else TextAlign.Left
                TextAlignment.CENTER -> TextAlign.Center
                TextAlignment.RIGHT -> TextAlign.Right
                TextAlignment.JUSTIFY -> TextAlign.Justify
            }

            BasicTextField(
                value = block.text,
                onValueChange = { newText ->
                    if (!state.isProtectedView) {
                        onEvent(RibbonEvent.OnDocumentTextChanged(block.id, newText))
                    }
                },
                readOnly = state.isProtectedView,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (it.isFocused && !state.isProtectedView) {
                            onEvent(RibbonEvent.OnBlockFocusChanged(block.id))
                        }
                    },
                textStyle = createSafeTextStyle(
                    fontSizeSp = state.fontSize,
                    fontFamilyStr = state.fontFamily,
                    lineSpacingFactor = currentLineSpacing,
                    isBold = if (isFocused) state.isBold else false,
                    isItalic = if (isFocused) state.isItalic else false,
                    textColor = state.textColor,
                    textAlign = alignment,
                    isRtl = currentIsRtl
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (block.text.text.isEmpty() && isFocused) {
                            Text(
                                text = "Type here...",
                                color = Color(0xFFAAAAAA),
                                fontSize = state.fontSize.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        is TableBlock -> {
            val tableDirection = if (block.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalLayoutDirection provides tableDirection) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        for (r in 0 until block.rows) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                            ) {
                                for (c in 0 until block.cols) {
                                    val cellId = "${r}_${c}"
                                    val cellModel = block.cells[cellId]

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .border(0.5.dp, Color.Black)
                                            .background(cellModel?.backgroundColor ?: Color.Transparent)
                                            .padding(8.dp),
                                        contentAlignment = Alignment.TopStart
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            if (cellModel != null && cellModel.textBlocks.isNotEmpty()) {
                                                cellModel.textBlocks.forEach { tb ->
                                                    RenderDocumentBlock(tb, state, onEvent)
                                                }
                                            } else {
                                                BasicTextField(
                                                    value = TextFieldValue(""),
                                                    onValueChange = {},
                                                    readOnly = state.isProtectedView,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        is ImageBlock -> {
            if (block.imageData != null) {
                val bitmap = remember(block.imageData) {
                    try {
                        BitmapFactory.decodeByteArray(block.imageData, 0, block.imageData.size)?.asImageBitmap()
                    } catch (e: Exception) { null }
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Document Image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                } else {
                    Text("Error loading image", color = Color.Red)
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Image Block",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = block.uri.ifEmpty { "Inline Illustration (Wrap Mode: ${block.wrapMode.name})" },
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        is ShapeBlock -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .padding(vertical = 8.dp)
                    .background(block.fillColor, RoundedCornerShape(4.dp))
                    .border(2.dp, block.strokeColor, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Shape: ${block.type.name}",
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
            }
        }

        is PageBreakBlock -> {
            // Visual page break divider on canvas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color(0xFF94A3B8), thickness = 1.dp)
                Text(
                    text = " -------- Page Break -------- ",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Divider(modifier = Modifier.weight(1f), color = Color(0xFF94A3B8), thickness = 1.dp)
            }
        }

        is BannerBlock -> {
            val align = when (block.alignment) {
                TextAlignment.CENTER -> Alignment.CenterHorizontally
                TextAlignment.LEFT -> Alignment.Start
                else -> Alignment.End
            }
            val textTextAlign = when (block.alignment) {
                TextAlignment.CENTER -> TextAlign.Center
                TextAlignment.LEFT -> TextAlign.Left
                else -> TextAlign.Right
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = block.backgroundColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = align
                ) {
                    Text(
                        text = block.title,
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = block.textColor,
                        textAlign = textTextAlign
                    )
                    if (block.subtitle.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = block.subtitle,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = block.textColor.copy(alpha = 0.88f),
                            textAlign = textTextAlign
                        )
                    }
                }
            }
        }

        is CalloutBlock -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = block.backgroundColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, block.borderColor.copy(alpha = 0.4f))
            ) {
                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .fillMaxHeight()
                            .background(block.borderColor)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        if (block.title.isNotEmpty()) {
                            Text(
                                text = block.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = block.borderColor,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        BasicTextField(
                            value = block.text,
                            onValueChange = { newText ->
                                onEvent(RibbonEvent.OnDocumentTextChanged(block.id, newText))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                color = block.textColor,
                                lineHeight = 19.sp
                            )
                        )
                    }
                }
            }
        }

        is DividerBlock -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = block.paddingVerticalDp.dp)
                    .height(block.thicknessDp.dp)
                    .background(block.color)
            )
        }
    }
}

@Composable
fun PageHeaderZone(
    isEditing: Boolean,
    headerText: androidx.compose.ui.text.input.TextFieldValue,
    documentTitle: String,
    paddingHorizontal: androidx.compose.ui.unit.Dp,
    onTextChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    onDoubleTap: () -> Unit
) {
    val borderModifier = if (isEditing) {
        Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
    } else Modifier

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = paddingHorizontal, vertical = 8.dp)
            .then(borderModifier)
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onDoubleTap() })
            },
        contentAlignment = Alignment.Center
    ) {
        if (isEditing) {
            BasicTextField(
                value = headerText,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                ),
                decorationBox = { inner ->
                    if (headerText.text.isEmpty()) {
                        Text(
                            text = "Header - Type here (Double-tap canvas to exit)",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    inner()
                }
            )
        } else {
            Text(
                text = headerText.text.ifEmpty { documentTitle },
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PageFooterZone(
    pageNumber: String,
    totalPages: String,
    isEditing: Boolean,
    footerText: androidx.compose.ui.text.input.TextFieldValue,
    showPageNumbers: Boolean,
    paddingHorizontal: androidx.compose.ui.unit.Dp,
    onTextChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    onDoubleTap: () -> Unit
) {
    val borderModifier = if (isEditing) {
        Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
    } else Modifier

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = paddingHorizontal, vertical = 8.dp)
            .then(borderModifier)
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onDoubleTap() })
            },
        contentAlignment = Alignment.Center
    ) {
        if (isEditing) {
            BasicTextField(
                value = footerText,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                ),
                decorationBox = { inner ->
                    if (footerText.text.isEmpty()) {
                        Text(
                            text = "Footer - Type here (Double-tap canvas to exit)",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    inner()
                }
            )
        } else {
            val displayFooter = "Page $pageNumber"

            Text(
                text = displayFooter,
                fontSize = 14.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
