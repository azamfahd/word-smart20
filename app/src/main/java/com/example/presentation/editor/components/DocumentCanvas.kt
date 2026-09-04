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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.AnnotatedString
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
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import com.example.presentation.editor.*

fun createSafeTextStyle(
    fontSizeSp: Int,
    fontFamilyStr: String,
    lineSpacingFactor: Float,
    isBold: Boolean = false,
    isItalic: Boolean = false,
    textColor: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Unspecified,
    textDecoration: TextDecoration? = null,
    background: Color = Color.Unspecified,
    isRtl: Boolean = true
): TextStyle {
    val family = AppFonts.getFontFamily(fontFamilyStr)
    val safeLineSpacing = lineSpacingFactor.coerceAtLeast(1.0f)
    return TextStyle(
        fontSize = fontSizeSp.sp,
        fontFamily = family,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
        color = textColor,
        textAlign = textAlign,
        textDecoration = textDecoration,
        background = background,
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
                        fontSizeSp = if (block.fontSize > 0) block.fontSize else state.fontSize,
                        fontFamilyStr = block.fontFamily.ifEmpty { state.fontFamily },
                        lineSpacingFactor = if (block.lineSpacing > 0f) block.lineSpacing else state.lineSpacing,
                        isBold = block.isBold,
                        isItalic = block.isItalic,
                        isRtl = block.isRtl || state.isRtl
                    )
                    val spacingBonus = with(density) { (block.spaceBeforePt + block.spaceAfterPt).dp.toPx() }
                    try {
                        val result = textMeasurer.measure(
                            text = block.text.annotatedString,
                            style = style,
                            constraints = androidx.compose.ui.unit.Constraints(maxWidth = availableWidthPx.toInt())
                        )
                        result.size.height.toFloat() + spacingBonus
                    } catch (e: Exception) {
                        with(density) { 24.dp.toPx() } + spacingBonus
                    }
                }
                is TableBlock -> {
                    var totalTableHeight = 0f
                    for (r in 0 until block.rows) {
                        var maxRowHeight = with(density) { 28.dp.toPx() }
                        for (c in 0 until block.cols) {
                            val colRatio = block.colWidthRatios.getOrElse(c) { 1f / block.cols.coerceAtLeast(1) }
                            val cellWidthPx = (availableWidthPx * colRatio).coerceAtLeast(40f)
                            val cell = block.cells["${r}_${c}"]
                            var cellHeight = with(density) { 12.dp.toPx() }
                            cell?.textBlocks?.forEach { tb ->
                                val tbSize = if (tb.fontSize > 0) tb.fontSize else (state.fontSize - 2).coerceAtLeast(9)
                                val style = TextStyle(
                                    fontSize = tbSize.sp,
                                    lineHeight = (tbSize.toFloat() * tb.lineSpacing).sp
                                )
                                try {
                                    val result = textMeasurer.measure(
                                        text = tb.text.annotatedString,
                                        style = style,
                                        constraints = Constraints(maxWidth = (cellWidthPx - with(density){12.dp.toPx()}).toInt().coerceAtLeast(20))
                                    )
                                    cellHeight += result.size.height.toFloat()
                                } catch (e: Exception) {
                                    cellHeight += with(density) { 20.dp.toPx() }
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
        ViewMode.WEB_LAYOUT -> Color(0xFFF8FAFC)   // Clean Mobile/Web Canvas Gray
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

    LaunchedEffect(state.viewMode) {
        offsetX = 0f
        offsetY = 0f
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(workspaceBgColor)
            .clipToBounds(),
        contentAlignment = Alignment.TopCenter
    ) {
        val containerMaxWidth = maxWidth

        when (state.viewMode) {
            ViewMode.PRINT_LAYOUT -> {
                // Windows Desktop Standard 1:1 Page Layout (تخطيط الطباعة / حجم الويندوز)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(density) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(0.35f, 3.5f)
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

                    Column(modifier = Modifier.fillMaxSize()) {
                        if (state.showRuler) {
                            InteractiveRulerBar(
                                state = state,
                                onEvent = onEvent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 12.dp)
                            )
                        }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .graphicsLayer(
                                    translationX = offsetX,
                                    translationY = offsetY
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp)
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

                                // Realistic gap between sequential A4 sheets
                                if (pageIndex < pages.size - 1) {
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }
                        }
                    }

                    // Floating Header/Footer Exit Bar when editing header/footer
                    if (state.isEditingHeaderFooter) {
                        Surface(
                            color = Color(0xFF1E3A8A),
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                                .clickable { onEvent(RibbonEvent.OnToggleHeaderFooterMode) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (state.isRtl) "إغلاق وحفظ الرأس والتذييل (انقر مرتين في أي مكان للعودة)" else "Close Header & Footer (Double-tap anywhere to return)",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            ViewMode.WEB_LAYOUT -> {
                // Smart Mobile View / Fluid Layout (عرض الجوال الذكي المتجاوب)
                // Content reflows perfectly across the mobile width without horizontal clipping
                SmartMobileCanvas(
                    state = state,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize()
                )
            }

            ViewMode.READ_MODE -> {
                // Eye-care Dedicated Read Mode (وضع القراءة الاحترافي المريح للعين)
                DedicatedReadModeCanvas(
                    pages = pages,
                    state = state,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize()
                )
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
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) {
                            if (!state.isEditingHeaderFooter && !state.isProtectedView) {
                                val lastBlock = pageBlocks.lastOrNull()
                                if (lastBlock != null) {
                                    onEvent(RibbonEvent.OnBlockFocusChanged(lastBlock.id))
                                } else {
                                    onEvent(RibbonEvent.OnAddParagraphAfter(state.activeBlockId))
                                }
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (pageBlocks.isEmpty()) {
                            // Empty page placeholder with interactive input
                            val emptyText = remember { mutableStateOf(TextFieldValue("")) }
                            BasicTextField(
                                value = emptyText.value,
                                onValueChange = { newT ->
                                    emptyText.value = newT
                                    if (newT.text.isNotEmpty() && !state.isProtectedView) {
                                        onEvent(RibbonEvent.OnAddParagraphAfter(state.activeBlockId))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = createSafeTextStyle(
                                    fontSizeSp = state.fontSize,
                                    fontFamilyStr = state.fontFamily,
                                    lineSpacingFactor = state.lineSpacing,
                                    isBold = state.isBold,
                                    isItalic = state.isItalic,
                                    textColor = state.textColor,
                                    textAlign = when (state.alignment) {
                                        TextAlignment.LEFT -> if (state.isRtl) TextAlign.Right else TextAlign.Left
                                        TextAlignment.CENTER -> TextAlign.Center
                                        TextAlignment.RIGHT -> TextAlign.Right
                                        TextAlignment.JUSTIFY -> TextAlign.Justify
                                    },
                                    isRtl = state.isRtl
                                ),
                                decorationBox = { inner ->
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        if (emptyText.value.text.isEmpty()) {
                                            Text(
                                                text = if (state.isRtl) "انقر واكتب هنا..." else "Click and type here...",
                                                color = Color(0xFFAAAAAA),
                                                fontSize = state.fontSize.sp,
                                                fontFamily = AppFonts.getFontFamily(state.fontFamily)
                                            )
                                        }
                                        inner()
                                    }
                                }
                            )
                        } else {
                            when (state.pageColumns) {
                                PageColumns.ONE -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        pageBlocks.forEach { block ->
                                            RenderDocumentBlock(
                                                block = block,
                                                state = state,
                                                onEvent = onEvent
                                            )
                                        }
                                    }
                                }
                                PageColumns.TWO -> {
                                    val mid = (pageBlocks.size + 1) / 2
                                    val col1 = pageBlocks.take(mid)
                                    val col2 = pageBlocks.drop(mid)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            col1.forEach { RenderDocumentBlock(block = it, state = state, onEvent = onEvent) }
                                        }
                                        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0xFFCBD5E1)))
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            col2.forEach { RenderDocumentBlock(block = it, state = state, onEvent = onEvent) }
                                        }
                                    }
                                }
                                PageColumns.THREE -> {
                                    val chunkSize = (pageBlocks.size + 2) / 3
                                    val col1 = pageBlocks.take(chunkSize)
                                    val col2 = pageBlocks.drop(chunkSize).take(chunkSize)
                                    val col3 = pageBlocks.drop(chunkSize * 2)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            col1.forEach { RenderDocumentBlock(block = it, state = state, onEvent = onEvent) }
                                        }
                                        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0xFFCBD5E1)))
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            col2.forEach { RenderDocumentBlock(block = it, state = state, onEvent = onEvent) }
                                        }
                                        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0xFFCBD5E1)))
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            col3.forEach { RenderDocumentBlock(block = it, state = state, onEvent = onEvent) }
                                        }
                                    }
                                }
                                PageColumns.LEFT_UNEQUAL -> {
                                    val splitIdx = (pageBlocks.size * 0.35f).toInt().coerceAtLeast(1)
                                    val col1 = pageBlocks.take(splitIdx)
                                    val col2 = pageBlocks.drop(splitIdx)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(0.35f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            col1.forEach { RenderDocumentBlock(block = it, state = state, onEvent = onEvent) }
                                        }
                                        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0xFFCBD5E1)))
                                        Column(modifier = Modifier.weight(0.65f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            col2.forEach { RenderDocumentBlock(block = it, state = state, onEvent = onEvent) }
                                        }
                                    }
                                }
                                PageColumns.RIGHT_UNEQUAL -> {
                                    val splitIdx = (pageBlocks.size * 0.65f).toInt().coerceAtLeast(1)
                                    val col1 = pageBlocks.take(splitIdx)
                                    val col2 = pageBlocks.drop(splitIdx)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(0.65f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            col1.forEach { RenderDocumentBlock(block = it, state = state, onEvent = onEvent) }
                                        }
                                        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0xFFCBD5E1)))
                                        Column(modifier = Modifier.weight(0.35f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            col2.forEach { RenderDocumentBlock(block = it, state = state, onEvent = onEvent) }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Intercepting overlay during Header/Footer editing:
                    // Clicking or double-tapping anywhere on the document body immediately exits and saves!
                    if (state.isEditingHeaderFooter) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.04f))
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            onEvent(RibbonEvent.OnToggleHeaderFooterMode)
                                        },
                                        onTap = {
                                            onEvent(RibbonEvent.OnToggleHeaderFooterMode)
                                        }
                                    )
                                }
                        )
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
            // Ink Drawing Overlay
            InkCanvasOverlay(state = state, pageIndex = pageNumber - 1, onEvent = onEvent)
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
            val currentAlign = block.alignment
            val currentLineSpacing = block.lineSpacing
            val currentIsRtl = block.isRtl
            val currentIndentLevel = block.indentLevel
            val currentShadingBg = block.paragraphShadingColor
            val currentBorderType = block.paragraphBorder
            val currentIsBulletedList = block.isBulletedList
            val currentBulletShape = block.bulletShape
            val currentIsNumberedList = block.isNumberedList
            val currentNumberingStyle = block.numberingStyle

            val currentFontSize = block.fontSize
            val currentFontFamily = block.fontFamily
            val currentIsBold = block.isBold
            val currentIsItalic = block.isItalic
            val currentIsUnderline = block.isUnderline
            val currentIsStrikethrough = block.isStrikethrough
            val currentTextColor = block.textColor
            val currentHighlightColor = block.highlightColor

            val alignment = when (currentAlign) {
                TextAlignment.LEFT -> TextAlign.Left
                TextAlignment.CENTER -> TextAlign.Center
                TextAlignment.RIGHT -> TextAlign.Right
                TextAlignment.JUSTIFY -> TextAlign.Justify
            }

            val textDecoration = if (currentIsUnderline && currentIsStrikethrough) {
                TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
            } else if (currentIsUnderline) {
                TextDecoration.Underline
            } else if (currentIsStrikethrough) {
                TextDecoration.LineThrough
            } else null

            val indentPadding = if (currentIndentLevel > 0) {
                (currentIndentLevel * 24).dp
            } else 0.dp

            val firstLineIndent = if (block.firstLineIndentDp > 0f) block.firstLineIndentDp.dp else if (state.firstLineIndentDp > 0f) state.firstLineIndentDp.dp else 0.dp
            val hangingIndent = if (block.hangingIndentDp > 0f) block.hangingIndentDp.dp else if (state.hangingIndentDp > 0f) state.hangingIndentDp.dp else 0.dp

            val startPadding = if (currentIsRtl) hangingIndent else (indentPadding + firstLineIndent)
            val endPadding = if (currentIsRtl) (indentPadding + firstLineIndent) else hangingIndent

            val shadingBg = currentShadingBg ?: Color.Transparent

            val borderModifier = if (currentBorderType != ParagraphBorder.NONE) {
                when (currentBorderType) {
                    ParagraphBorder.ALL, ParagraphBorder.OUTSIDE -> Modifier.border(1.dp, Color(0xFF475569), RoundedCornerShape(2.dp))
                    ParagraphBorder.BOTTOM -> Modifier.drawBehind { drawLine(Color(0xFF475569), start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = 2f) }
                    ParagraphBorder.TOP -> Modifier.drawBehind { drawLine(Color(0xFF475569), start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(size.width, 0f), strokeWidth = 2f) }
                    ParagraphBorder.LEFT -> Modifier.drawBehind { drawLine(Color(0xFF475569), start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(0f, size.height), strokeWidth = 2f) }
                    ParagraphBorder.RIGHT -> Modifier.drawBehind { drawLine(Color(0xFF475569), start = androidx.compose.ui.geometry.Offset(size.width, 0f), end = androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = 2f) }
                    else -> Modifier
                }
            } else Modifier

            val bulletPrefix = if (currentIsBulletedList) {
                when (currentBulletShape) {
                    BulletShape.DISC -> "• "
                    BulletShape.CIRCLE -> "○ "
                    BulletShape.SQUARE -> "■ "
                    BulletShape.HOLLOW_SQUARE -> "□ "
                    BulletShape.CHECKMARK -> "✓ "
                    BulletShape.ARROW -> "➢ "
                    BulletShape.STAR -> "★ "
                    BulletShape.FLORAL -> "❖ "
                }
            } else if (currentIsNumberedList) {
                val blockIdx = state.blocks.indexOfFirst { it.id == block.id }
                var itemIndex = 1
                if (blockIdx > 0) {
                    var prevIdx = blockIdx - 1
                    var count = 0
                    while (prevIdx >= 0) {
                        val prevB = state.blocks[prevIdx]
                        if (prevB is TextBlock && prevB.isNumberedList) {
                            count++
                            prevIdx--
                        } else {
                            break
                        }
                    }
                    itemIndex = 1 + count
                }
                formatNumberingPrefix(itemIndex, currentNumberingStyle)
            } else ""

            val formatPainterModifier = if (state.isFormatPainterActive) {
                Modifier.clickable {
                    state.copiedFormat?.let { fmt ->
                        onEvent(RibbonEvent.OnFontFamilyChanged(fmt.fontFamily))
                        onEvent(RibbonEvent.OnFontSizeChanged(fmt.fontSize))
                        if (fmt.isBold != state.isBold) onEvent(RibbonEvent.OnBoldClicked)
                        if (fmt.isItalic != state.isItalic) onEvent(RibbonEvent.OnItalicClicked)
                        if (fmt.isUnderline != state.isUnderline) onEvent(RibbonEvent.OnUnderlineClicked)
                        onEvent(RibbonEvent.OnTextColorChanged(fmt.textColor))
                        onEvent(RibbonEvent.OnHighlightColorChanged(fmt.highlightColor))
                        onEvent(RibbonEvent.OnAlignmentChanged(fmt.alignment))
                        onEvent(RibbonEvent.OnLineSpacingChanged(fmt.lineSpacing))
                    }
                    if (!state.isFormatPainterLocked) {
                        onEvent(RibbonEvent.OnFormatPainterToggled(false))
                    }
                }
            } else Modifier

            CompositionLocalProvider(LocalLayoutDirection provides (if (currentIsRtl) LayoutDirection.Rtl else LayoutDirection.Ltr)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(formatPainterModifier)
                        .background(shadingBg, RoundedCornerShape(2.dp))
                        .then(borderModifier)
                        .padding(
                            start = startPadding,
                            end = endPadding,
                            top = (block.spaceBeforePt * 0.75f).dp.coerceAtLeast(2.dp),
                            bottom = (block.spaceAfterPt * 0.75f).dp.coerceAtLeast(2.dp)
                        ),
                    verticalAlignment = Alignment.Top
                ) {
                    if (bulletPrefix.isNotEmpty()) {
                        Text(
                            text = bulletPrefix,
                            fontSize = currentFontSize.sp,
                            fontFamily = AppFonts.getFontFamily(currentFontFamily),
                            fontWeight = if (currentIsBold) FontWeight.Bold else FontWeight.Normal,
                            color = currentTextColor,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
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
                                fontSizeSp = currentFontSize,
                                fontFamilyStr = currentFontFamily,
                                lineSpacingFactor = currentLineSpacing,
                                isBold = currentIsBold,
                                isItalic = currentIsItalic,
                                textColor = currentTextColor,
                                textAlign = alignment,
                                textDecoration = textDecoration,
                                background = if (currentHighlightColor != Color.Transparent) currentHighlightColor else Color.Unspecified,
                                isRtl = currentIsRtl
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (block.text.text.isEmpty() && isFocused) {
                                        Text(
                                            text = if (state.isRtl) "ابدأ بالكتابة هنا..." else "Type here...",
                                            color = Color(0xFFAAAAAA),
                                            fontSize = currentFontSize.sp,
                                            fontFamily = AppFonts.getFontFamily(currentFontFamily),
                                            textAlign = alignment,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    innerTextField()
                                    if (state.showNonPrintingCharacters && isFocused) {
                                        Text(
                                            text = " ¶",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp,
                                            modifier = Modifier.align(if (currentIsRtl) Alignment.CenterStart else Alignment.CenterEnd)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        is TableBlock -> {
            TableBlockView(
                block = block,
                state = state,
                onEvent = onEvent
            )
        }

        is ImageBlock -> {
            if (block.imageData != null) {
                val bitmap = remember(block.imageData) {
                    try {
                        BitmapFactory.decodeByteArray(block.imageData, 0, block.imageData.size)?.asImageBitmap()
                    } catch (e: Exception) { null }
                }
                if (bitmap != null) {
                    val isFocused = state.activeBlockId == block.id
                    val outlineModifier = if (isFocused) Modifier.border(2.dp, Color(0xFF185ABD)) else Modifier
                    val imgAlignment = when (block.alignment) {
                        TextAlignment.LEFT -> Alignment.CenterStart
                        TextAlignment.RIGHT -> Alignment.CenterEnd
                        else -> Alignment.Center
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        contentAlignment = imgAlignment
                    ) {
                        val sizeModifier = if (block.width > 0 && block.height > 0) {
                            Modifier.size(block.width.dp, block.height.dp)
                        } else {
                            Modifier.fillMaxWidth(0.85f)
                        }
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Document Image",
                            contentScale = ContentScale.Fit,
                            modifier = sizeModifier
                                .then(outlineModifier)
                                .clickable { onEvent(RibbonEvent.OnBlockFocusChanged(block.id)) }
                        )
                    }
                } else {
                    Text("Error loading image", color = Color.Red)
                }
            } else {
                val isFocused = state.activeBlockId == block.id
                val outlineModifier = if (isFocused) Modifier.border(2.dp, Color(0xFF185ABD)) else Modifier
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                        .then(outlineModifier)
                        .clickable { onEvent(RibbonEvent.OnBlockFocusChanged(block.id)) },
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
            ShapeBlockView(
                block = block,
                state = state,
                onEvent = onEvent
            )
        }

        is PageBreakBlock -> {
            // Visual page break divider on canvas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF94A3B8), thickness = 1.dp)
                Text(
                    text = " -------- Page Break -------- ",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF94A3B8), thickness = 1.dp)
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

        is UnsupportedBlock -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = block.description,
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
fun TableBlockView(
    block: TableBlock,
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit
) {
    val isFocused = state.activeBlockId == block.id
    val tableDirection = if (block.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        // Mini Floating Table Action Bar when focused
        if (isFocused && !state.isProtectedView) {
            Surface(
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                color = Color(0xFF185ABD),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.clickable {
                                onEvent(RibbonEvent.OnAddTableRow(block.id, atIndex = -1, above = false))
                            }
                        ) {
                            Text(
                                text = if (state.isRtl) "+ صف أسفل" else "+ Row",
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.clickable {
                                onEvent(RibbonEvent.OnAddTableColumn(block.id, atIndex = -1, left = false))
                            }
                        ) {
                            Text(
                                text = if (state.isRtl) "+ عمود" else "+ Col",
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.clickable {
                                onEvent(RibbonEvent.OnDeleteTableRow(block.id, rowIndex = -1))
                            }
                        ) {
                            Text(
                                text = if (state.isRtl) "🗑 صف" else "Del Row",
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.clickable {
                                onEvent(RibbonEvent.OnDeleteTableColumn(block.id, colIndex = -1))
                            }
                        ) {
                            Text(
                                text = if (state.isRtl) "🗑 عمود" else "Del Col",
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.clickable {
                                onEvent(RibbonEvent.ChangeTab(RibbonTab.TABLE_DESIGN))
                            }
                        ) {
                            Text(
                                text = if (state.isRtl) "🎨 الأنماط" else "Styles",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFEF4444),
                        modifier = Modifier.clickable {
                            onEvent(RibbonEvent.OnDeleteTable(block.id))
                        }
                    ) {
                        Text(
                            text = if (state.isRtl) "حذف ✕" else "Delete ✕",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        CompositionLocalProvider(LocalLayoutDirection provides tableDirection) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isFocused) Modifier.border(1.5.dp, Color(0xFF185ABD)) else Modifier),
                shape = RoundedCornerShape(0.dp),
                border = androidx.compose.foundation.BorderStroke(block.borderWidth.dp, block.borderColor),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    for (r in 0 until block.rows) {
                        val isHeaderRow = r == 0 && block.hasHeaderRow
                        val isBandedRow = r > 0 && block.hasBandedRows && (r % 2 == 1)
                        val defaultRowBg = when {
                            isHeaderRow -> block.headerBackgroundColor
                            isBandedRow -> block.alternatingRowColor ?: Color(0xFFF8FAFC)
                            else -> Color.Transparent
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            for (c in 0 until block.cols) {
                                val cellId = "${r}_${c}"
                                val cellModel = block.cells[cellId]
                                if (cellModel?.isMergedCovered == true) {
                                    continue
                                }
                                val cSpan = cellModel?.colSpan ?: 1
                                val rSpan = cellModel?.rowSpan ?: 1
                                var totalColWeight = 0f
                                for (spanC in c until (c + cSpan).coerceAtMost(block.cols)) {
                                    totalColWeight += block.colWidthRatios.getOrElse(spanC) { 1f / block.cols.coerceAtLeast(1) }.coerceAtLeast(0.05f)
                                }
                                val cellBg = cellModel?.backgroundColor?.takeIf { it != Color.Transparent } ?: defaultRowBg
                                val isCellFocused = state.activeBlockId == block.id && state.activeTableCellId == cellId

                                Box(
                                    modifier = Modifier
                                        .weight(totalColWeight)
                                        .fillMaxHeight()
                                        .border(if (isCellFocused) 1.5.dp else 0.5.dp, if (isCellFocused) Color(0xFF185ABD) else block.borderColor)
                                        .background(cellBg)
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    contentAlignment = when (cellModel?.alignment) {
                                        TextAlignment.CENTER -> Alignment.Center
                                        TextAlignment.RIGHT -> Alignment.CenterEnd
                                        else -> Alignment.CenterStart
                                    }
                                ) {
                                    val cellBlocks = cellModel?.textBlocks ?: emptyList()
                                    if (cellBlocks.isEmpty()) {
                                        val cellText = TextFieldValue("")
                                        BasicTextField(
                                            value = cellText,
                                            onValueChange = { newText ->
                                                if (!state.isProtectedView) {
                                                    onEvent(RibbonEvent.OnTableCellChanged(block.id, cellId, newText))
                                                }
                                            },
                                            readOnly = state.isProtectedView,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .onFocusChanged { focusState ->
                                                    if (focusState.isFocused) {
                                                        onEvent(RibbonEvent.OnBlockFocusChanged(block.id))
                                                        onEvent(RibbonEvent.OnTableCellFocused(block.id, cellId))
                                                    }
                                                },
                                            textStyle = createSafeTextStyle(
                                                fontSizeSp = (state.fontSize - 1).coerceAtLeast(9),
                                                fontFamilyStr = state.fontFamily,
                                                lineSpacingFactor = 1.0f,
                                                isBold = isHeaderRow,
                                                isItalic = false,
                                                textColor = if (isHeaderRow) block.headerTextColor else state.textColor,
                                                textAlign = when (cellModel?.alignment) {
                                                    TextAlignment.CENTER -> TextAlign.Center
                                                    TextAlignment.RIGHT -> TextAlign.Right
                                                    else -> TextAlign.Start
                                                },
                                                isRtl = state.isRtl || block.isRtl
                                            )
                                        )
                                    } else {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            cellBlocks.forEach { tb ->
                                                BasicTextField(
                                                    value = tb.text,
                                                    onValueChange = { newText ->
                                                        if (!state.isProtectedView) {
                                                            onEvent(RibbonEvent.OnTableCellChanged(block.id, cellId, newText))
                                                        }
                                                    },
                                                    readOnly = state.isProtectedView,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .onFocusChanged { focusState ->
                                                            if (focusState.isFocused) {
                                                                onEvent(RibbonEvent.OnBlockFocusChanged(block.id))
                                                            }
                                                        },
                                                    textStyle = createSafeTextStyle(
                                                        fontSizeSp = if (tb.fontSize > 0) tb.fontSize else (state.fontSize - 1).coerceAtLeast(9),
                                                        fontFamilyStr = tb.fontFamily.ifEmpty { state.fontFamily },
                                                        lineSpacingFactor = if (tb.lineSpacing > 0f) tb.lineSpacing else 1.0f,
                                                        isBold = if (isHeaderRow) true else tb.isBold,
                                                        isItalic = tb.isItalic,
                                                        textColor = if (isHeaderRow) block.headerTextColor else tb.textColor,
                                                        textAlign = when (cellModel?.alignment ?: tb.alignment) {
                                                            TextAlignment.CENTER -> TextAlign.Center
                                                            TextAlignment.RIGHT -> TextAlign.Right
                                                            TextAlignment.JUSTIFY -> TextAlign.Justify
                                                            else -> TextAlign.Start
                                                        },
                                                        isRtl = tb.isRtl || state.isRtl || block.isRtl
                                                    )
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
    }
}

@Composable
fun ShapeBlockView(
    block: ShapeBlock,
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit
) {
    val isFocused = state.activeBlockId == block.id
    var isEditingText by remember { mutableStateOf(false) }
    var tempText by remember(block.text) { mutableStateOf(TextFieldValue(block.text)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mini Floating Shape Action Bar when focused
        if (isFocused && !state.isProtectedView) {
            Surface(
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                color = Color(0xFF185ABD),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.clickable {
                                onEvent(RibbonEvent.ChangeTab(RibbonTab.SHAPE_FORMAT))
                            }
                        ) {
                            Text(
                                text = if (state.isRtl) "📐 الأشكال والألوان" else "Shape & Colors",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.clickable {
                                isEditingText = !isEditingText
                            }
                        ) {
                            Text(
                                text = if (isEditingText) "✓ حفظ النص" else if (state.isRtl) "✍️ كتابة نص" else "Edit Text",
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFEF4444),
                        modifier = Modifier.clickable {
                            onEvent(RibbonEvent.OnDeleteBlock(block.id))
                        }
                    ) {
                        Text(
                            text = if (state.isRtl) "حذف ✕" else "Delete ✕",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(block.height.dp)
                .then(if (isFocused) Modifier.border(2.dp, Color(0xFF185ABD), RoundedCornerShape(6.dp)) else Modifier)
                .clickable { onEvent(RibbonEvent.OnBlockFocusChanged(block.id)) },
            contentAlignment = Alignment.Center
        ) {
            // Draw vector shape
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val strokePx = (block.strokeWidth * density).coerceAtLeast(1f)
                val fillStyle = Fill
                val strokeStyle = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)

                when (block.type) {
                    ShapeType.RECTANGLE -> {
                        if (block.fillColor != Color.Transparent) {
                            drawRect(color = block.fillColor)
                        }
                        if (block.strokeColor != Color.Transparent) {
                            drawRect(color = block.strokeColor, style = strokeStyle)
                        }
                    }
                    ShapeType.ROUNDED_RECTANGLE -> {
                        val cr = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                        if (block.fillColor != Color.Transparent) {
                            drawRoundRect(color = block.fillColor, cornerRadius = cr)
                        }
                        if (block.strokeColor != Color.Transparent) {
                            drawRoundRect(color = block.strokeColor, cornerRadius = cr, style = strokeStyle)
                        }
                    }
                    ShapeType.OVAL -> {
                        if (block.fillColor != Color.Transparent) {
                            drawOval(color = block.fillColor)
                        }
                        if (block.strokeColor != Color.Transparent) {
                            drawOval(color = block.strokeColor, style = strokeStyle)
                        }
                    }
                    ShapeType.LINE -> {
                        drawLine(
                            color = block.strokeColor,
                            start = Offset(0f, h / 2),
                            end = Offset(w, h / 2),
                            strokeWidth = strokePx
                        )
                    }
                    ShapeType.TRIANGLE -> {
                        val path = Path().apply {
                            moveTo(w / 2, 4f)
                            lineTo(w - 4f, h - 4f)
                            lineTo(4f, h - 4f)
                            close()
                        }
                        if (block.fillColor != Color.Transparent) drawPath(path, block.fillColor, style = fillStyle)
                        if (block.strokeColor != Color.Transparent) drawPath(path, block.strokeColor, style = strokeStyle)
                    }
                    ShapeType.DIAMOND -> {
                        val path = Path().apply {
                            moveTo(w / 2, 4f)
                            lineTo(w - 4f, h / 2)
                            lineTo(w / 2, h - 4f)
                            lineTo(4f, h / 2)
                            close()
                        }
                        if (block.fillColor != Color.Transparent) drawPath(path, block.fillColor, style = fillStyle)
                        if (block.strokeColor != Color.Transparent) drawPath(path, block.strokeColor, style = strokeStyle)
                    }
                    ShapeType.STAR -> {
                        val path = Path().apply {
                            val cx = w / 2
                            val cy = h / 2
                            val outerR = minOf(w, h) / 2 - 6f
                            val innerR = outerR * 0.45f
                            val points = 5
                            val step = Math.PI / points
                            for (i in 0 until 2 * points) {
                                val r = if (i % 2 == 0) outerR else innerR
                                val angle = i * step - Math.PI / 2
                                val x = (cx + r * Math.cos(angle)).toFloat()
                                val y = (cy + r * Math.sin(angle)).toFloat()
                                if (i == 0) moveTo(x, y) else lineTo(x, y)
                            }
                            close()
                        }
                        if (block.fillColor != Color.Transparent) drawPath(path, block.fillColor, style = fillStyle)
                        if (block.strokeColor != Color.Transparent) drawPath(path, block.strokeColor, style = strokeStyle)
                    }
                    ShapeType.HEART -> {
                        val path = Path().apply {
                            moveTo(w / 2, h * 0.85f)
                            cubicTo(w * 0.1f, h * 0.6f, 0f, h * 0.25f, w * 0.25f, h * 0.15f)
                            cubicTo(w * 0.45f, h * 0.05f, w / 2, h * 0.35f, w / 2, h * 0.35f)
                            cubicTo(w / 2, h * 0.35f, w * 0.55f, h * 0.05f, w * 0.75f, h * 0.15f)
                            cubicTo(w, h * 0.25f, w * 0.9f, h * 0.6f, w / 2, h * 0.85f)
                            close()
                        }
                        if (block.fillColor != Color.Transparent) drawPath(path, block.fillColor, style = fillStyle)
                        if (block.strokeColor != Color.Transparent) drawPath(path, block.strokeColor, style = strokeStyle)
                    }
                    ShapeType.CLOUD -> {
                        val path = Path().apply {
                            moveTo(w * 0.2f, h * 0.7f)
                            cubicTo(w * 0.05f, h * 0.7f, w * 0.05f, h * 0.45f, w * 0.25f, h * 0.4f)
                            cubicTo(w * 0.2f, h * 0.15f, w * 0.5f, h * 0.1f, w * 0.55f, h * 0.3f)
                            cubicTo(w * 0.7f, h * 0.15f, w * 0.95f, h * 0.3f, w * 0.85f, h * 0.55f)
                            cubicTo(w * 0.98f, h * 0.65f, w * 0.9f, h * 0.8f, w * 0.75f, h * 0.8f)
                            close()
                        }
                        if (block.fillColor != Color.Transparent) drawPath(path, block.fillColor, style = fillStyle)
                        if (block.strokeColor != Color.Transparent) drawPath(path, block.strokeColor, style = strokeStyle)
                    }
                    ShapeType.SPEECH_BUBBLE -> {
                        val path = Path().apply {
                            val r = 16f
                            moveTo(r, 0f)
                            lineTo(w - r, 0f)
                            cubicTo(w, 0f, w, 0f, w, r)
                            lineTo(w, h * 0.75f - r)
                            cubicTo(w, h * 0.75f, w, h * 0.75f, w - r, h * 0.75f)
                            lineTo(w * 0.45f, h * 0.75f)
                            lineTo(w * 0.25f, h)
                            lineTo(w * 0.3f, h * 0.75f)
                            lineTo(r, h * 0.75f)
                            cubicTo(0f, h * 0.75f, 0f, h * 0.75f, 0f, h * 0.75f - r)
                            lineTo(0f, r)
                            cubicTo(0f, 0f, 0f, 0f, r, 0f)
                            close()
                        }
                        if (block.fillColor != Color.Transparent) drawPath(path, block.fillColor, style = fillStyle)
                        if (block.strokeColor != Color.Transparent) drawPath(path, block.strokeColor, style = strokeStyle)
                    }
                    ShapeType.ARROW_RIGHT -> {
                        val path = Path().apply {
                            moveTo(0f, h * 0.3f)
                            lineTo(w * 0.65f, h * 0.3f)
                            lineTo(w * 0.65f, 0f)
                            lineTo(w, h / 2)
                            lineTo(w * 0.65f, h)
                            lineTo(w * 0.65f, h * 0.7f)
                            lineTo(0f, h * 0.7f)
                            close()
                        }
                        if (block.fillColor != Color.Transparent) drawPath(path, block.fillColor, style = fillStyle)
                        if (block.strokeColor != Color.Transparent) drawPath(path, block.strokeColor, style = strokeStyle)
                    }
                    ShapeType.ARROW_LEFT -> {
                        val path = Path().apply {
                            moveTo(w, h * 0.3f)
                            lineTo(w * 0.35f, h * 0.3f)
                            lineTo(w * 0.35f, 0f)
                            lineTo(0f, h / 2)
                            lineTo(w * 0.35f, h)
                            lineTo(w * 0.35f, h * 0.7f)
                            lineTo(w, h * 0.7f)
                            close()
                        }
                        if (block.fillColor != Color.Transparent) drawPath(path, block.fillColor, style = fillStyle)
                        if (block.strokeColor != Color.Transparent) drawPath(path, block.strokeColor, style = strokeStyle)
                    }
                    ShapeType.ARROW_DOUBLE -> {
                        val path = Path().apply {
                            moveTo(w * 0.25f, 0f)
                            lineTo(0f, h / 2)
                            lineTo(w * 0.25f, h)
                            lineTo(w * 0.25f, h * 0.65f)
                            lineTo(w * 0.75f, h * 0.65f)
                            lineTo(w * 0.75f, h)
                            lineTo(w, h / 2)
                            lineTo(w * 0.75f, 0f)
                            lineTo(w * 0.75f, h * 0.35f)
                            lineTo(w * 0.25f, h * 0.35f)
                            close()
                        }
                        if (block.fillColor != Color.Transparent) drawPath(path, block.fillColor, style = fillStyle)
                        if (block.strokeColor != Color.Transparent) drawPath(path, block.strokeColor, style = strokeStyle)
                    }
                    else -> {
                        if (block.fillColor != Color.Transparent) {
                            drawRoundRect(color = block.fillColor, cornerRadius = CornerRadius(12f, 12f))
                        }
                        if (block.strokeColor != Color.Transparent) {
                            drawRoundRect(color = block.strokeColor, cornerRadius = CornerRadius(12f, 12f), style = strokeStyle)
                        }
                    }
                }
            }

            // Text overlay inside shape
            if (isEditingText) {
                BasicTextField(
                    value = tempText,
                    onValueChange = {
                        tempText = it
                        onEvent(RibbonEvent.OnSetShapeText(block.id, it.text))
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(8.dp),
                    textStyle = TextStyle(
                        color = block.textColor,
                        fontSize = block.fontSize.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )
            } else {
                Text(
                    text = block.text.ifEmpty { "شكل: ${block.type.name}" },
                    color = block.textColor,
                    fontSize = block.fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
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

private fun formatNumberingPrefix(index: Int, style: NumberingStyle): String {
    return when (style) {
        NumberingStyle.DECIMAL_DOT -> "$index. "
        NumberingStyle.DECIMAL_PAREN -> "$index) "
        NumberingStyle.ARABIC_ALIF_BAA -> {
            val abjad = listOf("أ", "ب", "ج", "د", "هـ", "و", "ز", "ح", "ط", "ي", "ك", "ل", "م", "ن", "س", "ع", "ف", "ص", "ق", "ر", "ش", "ت", "ث", "خ", "ذ", "ض", "ظ", "غ")
            val letter = abjad.getOrElse((index - 1) % abjad.size) { "$index" }
            "$letter. "
        }
        NumberingStyle.ARABIC_INDIC -> {
            val indicDigits = mapOf('0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤', '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩')
            val str = index.toString().map { indicDigits[it] ?: it }.joinToString("")
            "$str. "
        }
        NumberingStyle.ALPHA_UPPER -> {
            val char = ('A' + (index - 1) % 26)
            "$char. "
        }
        NumberingStyle.ALPHA_LOWER -> {
            val char = ('a' + (index - 1) % 26)
            "$char. "
        }
        NumberingStyle.ROMAN_UPPER -> {
            val roman = toRomanNumeral(index)
            "$roman. "
        }
        NumberingStyle.ROMAN_LOWER -> {
            val roman = toRomanNumeral(index).lowercase()
            "$roman. "
        }
    }
}

private fun toRomanNumeral(number: Int): String {
    val numbers = intArrayOf(100, 90, 50, 40, 10, 9, 5, 4, 1)
    val letters = arrayOf("C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
    var num = number
    val result = StringBuilder()
    for (i in numbers.indices) {
        while (num >= numbers[i]) {
            num -= numbers[i]
            result.append(letters[i])
        }
    }
    return result.toString().ifEmpty { "$number" }
}

@Composable
fun SmartMobileCanvas(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = state.pageColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Smart Mobile Header Zone (Double-tap to edit)
                        PageHeaderZone(
                            isEditing = state.isEditingHeaderFooter,
                            headerText = state.headerText,
                            documentTitle = state.documentTitle,
                            paddingHorizontal = 8.dp,
                            onTextChange = { onEvent(RibbonEvent.OnHeaderTextChanged(it)) },
                            onDoubleTap = { onEvent(RibbonEvent.OnToggleHeaderFooterMode) }
                        )

                        HorizontalDivider(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Main Content Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (state.isEditingHeaderFooter) 0.35f else 1f)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                ) {
                                    if (!state.isEditingHeaderFooter && !state.isProtectedView) {
                                        val lastBlock = state.blocks.lastOrNull()
                                        if (lastBlock != null) {
                                            onEvent(RibbonEvent.OnBlockFocusChanged(lastBlock.id))
                                        } else {
                                            onEvent(RibbonEvent.OnAddParagraphAfter(state.activeBlockId))
                                        }
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                state.blocks.forEach { block ->
                                    if (block !is PageBreakBlock) {
                                        RenderDocumentBlock(
                                            block = block,
                                            state = state,
                                            onEvent = onEvent
                                        )
                                    }
                                }
                            }

                            // Intercepting overlay during header/footer editing:
                            // Double-tapping or clicking anywhere exits and saves!
                            if (state.isEditingHeaderFooter) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.04f))
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onDoubleTap = { onEvent(RibbonEvent.OnToggleHeaderFooterMode) },
                                                onTap = { onEvent(RibbonEvent.OnToggleHeaderFooterMode) }
                                            )
                                        }
                                )
                            }
                        }

                        HorizontalDivider(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Smart Mobile Footer Zone (Double-tap to edit)
                        val pageNumString = if (state.numeralSystem == NumeralSystem.ARABIC) "١" else "1"
                        PageFooterZone(
                            pageNumber = pageNumString,
                            totalPages = pageNumString,
                            isEditing = state.isEditingHeaderFooter,
                            footerText = state.footerText,
                            showPageNumbers = state.showPageNumbers,
                            paddingHorizontal = 8.dp,
                            onTextChange = { onEvent(RibbonEvent.OnFooterTextChanged(it)) },
                            onDoubleTap = { onEvent(RibbonEvent.OnToggleHeaderFooterMode) }
                        )
                    }

                    // Ink Overlay for Smart Mobile
                    InkCanvasOverlay(state = state, pageIndex = 0, onEvent = onEvent)
                }
            }
        }

        // Floating Header/Footer Exit Bar when editing header/footer
        if (state.isEditingHeaderFooter) {
            Surface(
                color = Color(0xFF1E3A8A),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .clickable { onEvent(RibbonEvent.OnToggleHeaderFooterMode) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (state.isRtl) "إغلاق وحفظ الرأس والتذييل (انقر مرتين للعودة)" else "Close & Save Header/Footer",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DedicatedReadModeCanvas(
    pages: List<List<DocumentBlock>>,
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var readingFontScale by remember { mutableFloatStateOf(1.1f) }
    var readingThemeMode by remember { mutableStateOf("SEPIA") } // SEPIA, WHITE, DARK

    val readingBgColor = when (readingThemeMode) {
        "DARK" -> Color(0xFF1E293B)
        "WHITE" -> Color(0xFFFFFFFF)
        else -> Color(0xFFFDFBF7) // Warm Sepia Eye-Care
    }
    val readingTextColor = when (readingThemeMode) {
        "DARK" -> Color(0xFFF1F5F9)
        else -> Color(0xFF1E293B)
    }

    val totalPagesCount = pages.size.coerceAtLeast(1)
    val safePageIndex = currentPageIndex.coerceIn(0, totalPagesCount - 1)
    val currentBlocks = pages.getOrElse(safePageIndex) { emptyList() }

    val pageNumString = if (state.numeralSystem == NumeralSystem.ARABIC) {
        (safePageIndex + 1).toString().map { c ->
            when (c) {
                '0' -> '٠'; '1' -> '١'; '2' -> '٢'; '3' -> '٣'; '4' -> '٤'
                '5' -> '٥'; '6' -> '٦'; '7' -> '٧'; '8' -> '٨'; '9' -> '٩'
                else -> c
            }
        }.joinToString("")
    } else "${safePageIndex + 1}"

    val totalPagesString = if (state.numeralSystem == NumeralSystem.ARABIC) {
        totalPagesCount.toString().map { c ->
            when (c) {
                '0' -> '٠'; '1' -> '١'; '2' -> '٢'; '3' -> '٣'; '4' -> '٤'
                '5' -> '٥'; '6' -> '٦'; '7' -> '٧'; '8' -> '٨'; '9' -> '٩'
                else -> c
            }
        }.joinToString("")
    } else "$totalPagesCount"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(readingBgColor)
    ) {
        // Top Reading Toolbar
        Surface(
            color = if (readingThemeMode == "DARK") Color(0xFF0F172A) else Color(0xFFF1EFE9),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Return to Edit Button
                Button(
                    onClick = { onEvent(RibbonEvent.OnViewModeChanged(ViewMode.PRINT_LAYOUT)) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.isRtl) "تعديل المستند" else "Edit Document",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Document Title & Page Count
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.documentTitle.ifEmpty { if (state.isRtl) "مستند ورد" else "Word Document" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = readingTextColor
                    )
                    Text(
                        text = if (state.isRtl) "صفحة $pageNumString من $totalPagesString" else "Page $pageNumString of $totalPagesString",
                        fontSize = 11.sp,
                        color = readingTextColor.copy(alpha = 0.7f)
                    )
                }

                // Font Size Adjusters (A- / A+)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { readingFontScale = (readingFontScale - 0.1f).coerceAtLeast(0.8f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(text = "A-", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = readingTextColor)
                    }
                    IconButton(
                        onClick = { readingFontScale = (readingFontScale + 0.1f).coerceAtMost(2.0f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(text = "A+", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = readingTextColor)
                    }
                }
            }
        }

        // Reading Content Body
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header display in reading mode
                if (state.headerText.text.isNotEmpty()) {
                    item {
                        Text(
                            text = state.headerText.text,
                            fontSize = (11 * readingFontScale).sp,
                            color = readingTextColor.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        HorizontalDivider(
                            color = readingTextColor.copy(alpha = 0.15f),
                            thickness = 0.5.dp
                        )
                    }
                }

                itemsIndexed(currentBlocks) { _, block ->
                    // Unify text block rendering in Read Mode to look exactly like Edit Mode
                    val preparedBlock = if (block is TextBlock) {
                        block.copy(
                            fontSize = (block.fontSize * readingFontScale).toInt().coerceAtLeast(8),
                            textColor = if (block.textColor == Color.Black || block.textColor == Color(0xFF1E293B) || block.textColor == Color.Unspecified) readingTextColor else block.textColor
                        )
                    } else {
                        block
                    }
                    RenderDocumentBlock(
                        block = preparedBlock,
                        state = state.copy(isProtectedView = true, activeBlockId = ""),
                        onEvent = onEvent
                    )
                }

                // Footer display in reading mode
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(
                        color = readingTextColor.copy(alpha = 0.15f),
                        thickness = 0.5.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (state.footerText.text.isNotEmpty()) state.footerText.text else "Page $pageNumString",
                        fontSize = (11 * readingFontScale).sp,
                        color = readingTextColor.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Bottom Page Navigation Bar
        Surface(
            color = if (readingThemeMode == "DARK") Color(0xFF0F172A) else Color(0xFFF1EFE9),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Previous Page Button
                Button(
                    onClick = { if (safePageIndex > 0) currentPageIndex-- },
                    enabled = safePageIndex > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF475569),
                        disabledContainerColor = Color.LightGray.copy(alpha = 0.3f)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (state.isRtl) "السابق" else "Previous",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                // Page Progress Pill
                Surface(
                    color = Color(0xFF2563EB).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (state.isRtl) "صفحة $pageNumString / $totalPagesString" else "Page $pageNumString / $totalPagesString",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Next Page Button
                Button(
                    onClick = { if (safePageIndex < totalPagesCount - 1) currentPageIndex++ },
                    enabled = safePageIndex < totalPagesCount - 1,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        disabledContainerColor = Color.LightGray.copy(alpha = 0.3f)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (state.isRtl) "التالي" else "Next",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveRulerBar(
    state: EditorState,
    onEvent: (RibbonEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val (pageWidth, pageHeight) = when (state.pageSize) {
        PageSize.A4 -> 794.dp to 1123.dp
        PageSize.A3 -> 1123.dp to 1587.dp
        PageSize.LETTER -> 816.dp to 1056.dp
        PageSize.LEGAL -> 816.dp to 1344.dp
        PageSize.A5 -> 559.dp to 794.dp
    }
    val finalWidth = if (state.pageOrientation == PageOrientation.LANDSCAPE) pageHeight else pageWidth

    val pagePaddingHorizontal = when (state.pageMargin) {
        PageMargin.NORMAL -> 72.dp
        PageMargin.NARROW -> 36.dp
        PageMargin.MODERATE -> 54.dp
        PageMargin.WIDE -> 108.dp
    }

    var firstLineIndent by remember(state.firstLineIndentDp) { mutableFloatStateOf(state.firstLineIndentDp) }
    var hangingIndent by remember(state.hangingIndentDp) { mutableFloatStateOf(state.hangingIndentDp) }

    Card(
        modifier = modifier
            .width(finalWidth)
            .height(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val rulerWidthPx = size.width
                val leftMarginPx = (pagePaddingHorizontal.toPx() / finalWidth.toPx()) * rulerWidthPx
                val rightMarginPx = rulerWidthPx - leftMarginPx

                // Left Margin (Gray)
                drawRect(
                    color = Color(0xFFCBD5E1),
                    topLeft = Offset(0f, 0f),
                    size = Size(leftMarginPx, size.height)
                )

                // Printable Area (White)
                drawRect(
                    color = Color.White,
                    topLeft = Offset(leftMarginPx, 0f),
                    size = Size((rightMarginPx - leftMarginPx).coerceAtLeast(0f), size.height)
                )

                // Right Margin (Gray)
                drawRect(
                    color = Color(0xFFCBD5E1),
                    topLeft = Offset(rightMarginPx, 0f),
                    size = Size((rulerWidthPx - rightMarginPx).coerceAtLeast(0f), size.height)
                )

                // Outer Borders
                drawRect(
                    color = Color(0xFF94A3B8),
                    style = Stroke(width = 1f)
                )

                // Tick Marks (CM/Inches)
                val totalTicks = 20
                val tickStep = rulerWidthPx / totalTicks
                for (i in 0..totalTicks) {
                    val x = i * tickStep
                    val isMajor = i % 2 == 0
                    val tickHeight = if (isMajor) 10f else 5f
                    drawLine(
                        color = Color(0xFF64748B),
                        start = Offset(x, size.height - tickHeight),
                        end = Offset(x, size.height),
                        strokeWidth = 1.5f
                    )
                }
            }

            // Draggable Indent Handles (First Line Indent & Hanging Indent)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // First Line Indent Handle Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clickable {
                            val newFirst = if (firstLineIndent == 0f) 24f else if (firstLineIndent == 24f) 48f else 0f
                            firstLineIndent = newFirst
                            onEvent(RibbonEvent.OnFirstLineIndentChanged(newFirst))
                        }
                        .background(Color(0xFFDBEAFE), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (state.isRtl) "بادئة الأول: ▼ ${firstLineIndent.toInt()}dp" else "1st Line: ▼ ${firstLineIndent.toInt()}dp",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E40AF)
                    )
                }

                // Hanging Indent Handle Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clickable {
                            val newHanging = if (hangingIndent == 0f) 16f else if (hangingIndent == 16f) 32f else 0f
                            hangingIndent = newHanging
                            onEvent(RibbonEvent.OnHangingIndentChanged(newHanging))
                        }
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (state.isRtl) "معلقة: ▲ ${hangingIndent.toInt()}dp" else "Hanging: ▲ ${hangingIndent.toInt()}dp",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                }

                // Page Margin Quick Selector Button
                Text(
                    text = when (state.pageMargin) {
                        PageMargin.NORMAL -> if (state.isRtl) "هوامش عادية" else "Normal Margins"
                        PageMargin.NARROW -> if (state.isRtl) "هوامش ضيقة" else "Narrow Margins"
                        PageMargin.MODERATE -> if (state.isRtl) "هوامش متوسطة" else "Moderate Margins"
                        PageMargin.WIDE -> if (state.isRtl) "هوامش عريضة" else "Wide Margins"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    modifier = Modifier
                        .clickable {
                            val nextMargin = when (state.pageMargin) {
                                PageMargin.NORMAL -> PageMargin.NARROW
                                PageMargin.NARROW -> PageMargin.MODERATE
                                PageMargin.MODERATE -> PageMargin.WIDE
                                PageMargin.WIDE -> PageMargin.NORMAL
                            }
                            onEvent(RibbonEvent.OnPageMarginChanged(nextMargin))
                        }
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
