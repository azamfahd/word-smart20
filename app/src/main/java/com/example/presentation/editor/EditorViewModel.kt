package com.example.presentation.editor

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class EditorViewModel : ViewModel() {
    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private val undoStack = mutableListOf<List<DocumentBlock>>()
    private val redoStack = mutableListOf<List<DocumentBlock>>()

    init {
        com.example.presentation.editor.font.FontEngine.setOnFontDownloadedListener {
            viewModelScope.launch(Dispatchers.Main) {
                try {
                    val stateVal = _state.value
                    val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock
                    if (activeBlock != null) {
                        val fontName = stateVal.fontFamily
                        val newAnnotatedString = com.example.presentation.editor.font.FontEngine.applyFontToAnnotatedString(
                            annotatedString = activeBlock.text.annotatedString,
                            selection = activeBlock.text.selection,
                            fontName = fontName
                        )
                        val newBlocks = stateVal.blocks.map { block ->
                            if (block.id == activeBlock.id && block is TextBlock) {
                                block.copy(text = activeBlock.text.copy(annotatedString = newAnnotatedString))
                            } else block
                        }
                        _state.update { it.copy(blocks = newBlocks) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun pushUndoState() {
        if (undoStack.size > 50) undoStack.removeAt(0)
        undoStack.add(_state.value.blocks)
        redoStack.clear()
        _state.update { it.copy(canUndo = true, canRedo = false) }
    }

    fun loadFromUri(uri: Uri, context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val model = DocxEngine.parseDocx(inputStream)
                        val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Imported Document"
                        withContext(Dispatchers.Main) {
                            _state.update {
                                it.copy(
                                    blocks = model.blocks,
                                    originalDocxBytes = model.originalBytes,
                                    activeBlockId = model.blocks.firstOrNull()?.id ?: "blk_initial",
                                    headerText = TextFieldValue(model.headerText),
                                    footerText = TextFieldValue(model.footerText),
                                    isRtl = model.isRtl,
                                    pageSize = model.pageSize,
                                    pageOrientation = model.pageOrientation,
                                    pageMargin = model.pageMargin,
                                    pageColor = model.pageColor,
                                    pageBorder = model.pageBorder,
                                    watermarkText = model.watermarkText,
                                    fontFamily = model.defaultFontFamily,
                                    fontSize = model.defaultFontSize,
                                    documentTitle = fileName.removeSuffix(".docx").removeSuffix(".doc"),
                                    currentUri = uri,
                                    isProtectedView = true,
                                    viewMode = ViewMode.READ_MODE,
                                    isFileMenuOpen = false
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun createNewBlankDocument() {
        _state.value = EditorState(
            blocks = listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue("ابدأ بكتابة مستندك هنا..."))),
            documentTitle = "مستند1",
            currentUri = null,
            isProtectedView = false,
            viewMode = ViewMode.PRINT_LAYOUT,
            isFileMenuOpen = false
        )
    }
    
    fun createNewDocumentFromTemplate(
        title: String,
        blocks: List<DocumentBlock>,
        pageBorder: PageBorder = PageBorder(),
        pageColor: Color = Color.White,
        pageOrientation: PageOrientation = PageOrientation.PORTRAIT,
        pageSize: PageSize = PageSize.A4,
        pageMargin: PageMargin = PageMargin.NORMAL,
        headerText: TextFieldValue = TextFieldValue(""),
        footerText: TextFieldValue = TextFieldValue(""),
        pageStripeStyle: PageStripeStyle = PageStripeStyle.NONE,
        pageAccentColor: Color? = null,
        pageSecondaryColor: Color? = null
    ) {
        _state.value = EditorState(
            blocks = blocks,
            activeBlockId = blocks.firstOrNull()?.id ?: "blk_${UUID.randomUUID()}",
            documentTitle = title,
            pageBorder = pageBorder,
            pageColor = pageColor,
            pageOrientation = pageOrientation,
            pageSize = pageSize,
            pageMargin = pageMargin,
            headerText = headerText,
            footerText = footerText,
            pageStripeStyle = pageStripeStyle,
            pageAccentColor = pageAccentColor,
            pageSecondaryColor = pageSecondaryColor,
            isProtectedView = true,
            viewMode = ViewMode.READ_MODE,
            currentUri = null,
            isFileMenuOpen = false
        )
    }

    fun clearSavingToCloudFlag() {
        _state.update { it.copy(isSavingToCloud = false) }
    }

    fun processEvent(event: RibbonEvent) {
        try {
            when (event) {
            is RibbonEvent.OnEnableEditing -> {
                _state.update { it.copy(isProtectedView = false, viewMode = ViewMode.PRINT_LAYOUT) }
            }
            is RibbonEvent.OnSaveToCloudClicked -> {
                // Handled in UI layer where we have context and auth
                _state.update { it.copy(isSavingToCloud = true) }
            }
            is RibbonEvent.OnCloudDocIdSaved -> {
                _state.update { it.copy(cloudDocId = event.id) }
            }
            is RibbonEvent.OnLoadFromCloud -> {
                try {
                    val bytes = android.util.Base64.decode(event.base64Data, android.util.Base64.DEFAULT)
                    val model = DocxEngine.parseDocx(java.io.ByteArrayInputStream(bytes))
                    _state.update {
                        it.copy(
                            blocks = model.blocks,
                            originalDocxBytes = model.originalBytes,
                            activeBlockId = model.blocks.firstOrNull()?.id ?: "blk_initial",
                            headerText = TextFieldValue(model.headerText),
                            footerText = TextFieldValue(model.footerText),
                            isRtl = model.isRtl,
                            pageSize = model.pageSize,
                            pageOrientation = model.pageOrientation,
                            pageMargin = model.pageMargin,
                            pageColor = model.pageColor,
                            pageBorder = model.pageBorder,
                            watermarkText = model.watermarkText,
                            fontFamily = model.defaultFontFamily,
                            fontSize = model.defaultFontSize,
                            isProtectedView = true,
                            viewMode = ViewMode.READ_MODE,
                            cloudDocId = event.cloudDocumentId
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("EditorViewModel", "Failed to load from cloud", e)
                }
            }
            is RibbonEvent.ChangeTab -> _state.update { it.copy(activeTab = event.tab) }
            is RibbonEvent.OnLanguageToggled -> _state.update { it.copy(isRtl = !it.isRtl) }
            is RibbonEvent.OnZoomChanged -> _state.update { it.copy(zoomScale = event.scale.coerceIn(0.4f, 3.5f)) }
            is RibbonEvent.OnViewModeChanged -> _state.update { 
                it.copy(
                    viewMode = event.mode,
                    isProtectedView = (event.mode == ViewMode.READ_MODE)
                ) 
            }
            is RibbonEvent.OnToggleProtectedView -> _state.update { it.copy(isProtectedView = !it.isProtectedView) }
            
            is RibbonEvent.OnDocumentImported -> {
                _state.update {
                    it.copy(
                        blocks = event.model.blocks,
                        originalDocxBytes = event.model.originalBytes,
                        activeBlockId = event.model.blocks.firstOrNull()?.id ?: "blk_initial",
                        headerText = TextFieldValue(event.model.headerText),
                        footerText = TextFieldValue(event.model.footerText),
                        isRtl = event.model.isRtl,
                        pageSize = event.model.pageSize,
                        pageOrientation = event.model.pageOrientation,
                        pageMargin = event.model.pageMargin,
                        pageColor = event.model.pageColor,
                        pageBorder = event.model.pageBorder,
                        watermarkText = event.model.watermarkText,
                        fontFamily = event.model.defaultFontFamily,
                        fontSize = event.model.defaultFontSize,
                        isProtectedView = true,
                        viewMode = ViewMode.READ_MODE,
                        currentUri = event.uri,
                        isFileMenuOpen = false
                    )
                }
            }
            is RibbonEvent.OnNewDocument -> {
                val initialBlock = TextBlock("blk_${UUID.randomUUID()}", TextFieldValue("ابدأ بكتابة مستندك هنا..."))
                _state.update {
                    it.copy(
                        blocks = listOf(initialBlock),
                        activeBlockId = initialBlock.id,
                        documentTitle = "مستند1",
                        headerText = TextFieldValue(""),
                        footerText = TextFieldValue(""),
                        isProtectedView = false,
                        viewMode = ViewMode.PRINT_LAYOUT,
                        currentUri = null,
                        isFileMenuOpen = false
                    )
                }
            }
            
            is RibbonEvent.OnToggleFileMenu -> _state.update { it.copy(isFileMenuOpen = !it.isFileMenuOpen) }
            is RibbonEvent.OnShowSaveAsDialog -> _state.update { it.copy(showSaveAsDialog = true) }
            is RibbonEvent.OnDismissSaveAsDialog -> _state.update { it.copy(showSaveAsDialog = false) }
            is RibbonEvent.OnSaveDocumentClicked -> {} // Handled in MainScreen UI
            is RibbonEvent.OnDocumentTitleChanged -> _state.update { it.copy(documentTitle = event.title) }
            
            is RibbonEvent.OnDocumentTextChanged -> {
                _state.update { state ->
                    val oldBlock = state.blocks.find { it.id == event.blockId } as? TextBlock
                    val newBlocks = state.blocks.map { 
                        if (it.id == event.blockId && it is TextBlock) {
                            val updatedText = if (oldBlock != null) {
                                val oldStr = oldBlock.text.annotatedString.text
                                val newStr = event.text.annotatedString.text
                                val oldSpans = oldBlock.text.annotatedString.spanStyles
                                val adjustedSpans = adjustSpanStylesForTextChange(oldSpans, oldStr, newStr, event.text.selection)
                                
                                val builder = AnnotatedString.Builder()
                                builder.append(newStr)
                                adjustedSpans.forEach { range ->
                                    val s = range.start.coerceIn(0, newStr.length)
                                    val e = range.end.coerceIn(0, newStr.length)
                                    if (s < e) {
                                        builder.addStyle(range.item, s, e)
                                    }
                                }
                                oldBlock.text.annotatedString.paragraphStyles.forEach { pRange ->
                                    val ps = pRange.start.coerceIn(0, newStr.length)
                                    val pe = pRange.end.coerceIn(0, newStr.length)
                                    if (ps < pe) {
                                        builder.addStyle(pRange.item, ps, pe)
                                    }
                                }
                                event.text.copy(annotatedString = builder.toAnnotatedString())
                            } else {
                                event.text
                            }
                            it.copy(text = updatedText)
                        } else it 
                    }
                    val targetBlock = newBlocks.find { it.id == event.blockId } as? TextBlock
                    if (targetBlock != null) {
                        val sel = targetBlock.text.selection
                        val len = targetBlock.text.annotatedString.length
                        val charIdx = if (sel.min < sel.max) sel.min else (sel.min - 1).coerceIn(0, maxOf(0, len - 1))
                        val mergedStyle = getMergedSpanStyleAt(targetBlock.text.annotatedString, charIdx)

                        val isBoldActive = if (mergedStyle.fontWeight != null) (mergedStyle.fontWeight == FontWeight.Bold || mergedStyle.fontWeight == FontWeight.ExtraBold || mergedStyle.fontWeight == FontWeight.Black) else targetBlock.isBold
                        val isItalicActive = if (mergedStyle.fontStyle != null) mergedStyle.fontStyle == FontStyle.Italic else targetBlock.isItalic
                        val isUnderlineActive = if (mergedStyle.textDecoration != null) (mergedStyle.textDecoration == TextDecoration.Underline || mergedStyle.textDecoration?.contains(TextDecoration.Underline) == true) else targetBlock.isUnderline
                        val isStrikeActive = if (mergedStyle.textDecoration != null) (mergedStyle.textDecoration == TextDecoration.LineThrough || mergedStyle.textDecoration?.contains(TextDecoration.LineThrough) == true) else targetBlock.isStrikethrough
                        val textColorActive = if (mergedStyle.color != Color.Unspecified) mergedStyle.color else targetBlock.textColor
                        val highlightColorActive = if (mergedStyle.background != Color.Unspecified) mergedStyle.background else targetBlock.highlightColor
                        val fontSizeActive = if (mergedStyle.fontSize != androidx.compose.ui.unit.TextUnit.Unspecified) mergedStyle.fontSize.value.toInt() else targetBlock.fontSize

                        state.copy(
                            blocks = newBlocks, 
                            activeBlockId = event.blockId,
                            isBold = isBoldActive,
                            isItalic = isItalicActive,
                            isUnderline = isUnderlineActive,
                            isStrikethrough = isStrikeActive,
                            textColor = textColorActive,
                            highlightColor = highlightColorActive,
                            fontSize = fontSizeActive,
                            fontFamily = targetBlock.fontFamily
                        )
                    } else {
                        state.copy(blocks = newBlocks, activeBlockId = event.blockId)
                    }
                }
            }
            is RibbonEvent.OnBlockFocusChanged -> {
                _state.update { state ->
                    val block = state.blocks.find { it.id == event.blockId } as? TextBlock
                    if (block != null) {
                        val sel = block.text.selection
                        val len = block.text.annotatedString.length
                        val charIdx = if (sel.min < sel.max) sel.min else (sel.min - 1).coerceIn(0, maxOf(0, len - 1))
                        val mergedStyle = getMergedSpanStyleAt(block.text.annotatedString, charIdx)

                        val isBoldActive = if (mergedStyle.fontWeight != null) (mergedStyle.fontWeight == FontWeight.Bold || mergedStyle.fontWeight == FontWeight.ExtraBold || mergedStyle.fontWeight == FontWeight.Black) else block.isBold
                        val isItalicActive = if (mergedStyle.fontStyle != null) mergedStyle.fontStyle == FontStyle.Italic else block.isItalic
                        val isUnderlineActive = if (mergedStyle.textDecoration != null) (mergedStyle.textDecoration == TextDecoration.Underline || mergedStyle.textDecoration?.contains(TextDecoration.Underline) == true) else block.isUnderline
                        val isStrikeActive = if (mergedStyle.textDecoration != null) (mergedStyle.textDecoration == TextDecoration.LineThrough || mergedStyle.textDecoration?.contains(TextDecoration.LineThrough) == true) else block.isStrikethrough
                        val textColorActive = if (mergedStyle.color != Color.Unspecified) mergedStyle.color else block.textColor
                        val highlightColorActive = if (mergedStyle.background != Color.Unspecified) mergedStyle.background else block.highlightColor
                        val fontSizeActive = if (mergedStyle.fontSize != androidx.compose.ui.unit.TextUnit.Unspecified) mergedStyle.fontSize.value.toInt() else block.fontSize

                        state.copy(
                            activeBlockId = event.blockId,
                            fontSize = fontSizeActive,
                            fontFamily = block.fontFamily,
                            isBold = isBoldActive,
                            isItalic = isItalicActive,
                            isUnderline = isUnderlineActive,
                            isStrikethrough = isStrikeActive,
                            textColor = textColorActive,
                            highlightColor = highlightColorActive,
                            alignment = block.alignment,
                            lineSpacing = block.lineSpacing,
                            isTextRtl = block.isRtl,
                            indentLevel = block.indentLevel,
                            isBulletedList = block.isBulletedList,
                            bulletShape = block.bulletShape,
                            isNumberedList = block.isNumberedList,
                            numberingStyle = block.numberingStyle,
                            paragraphShadingColor = block.paragraphShadingColor,
                            paragraphBorder = block.paragraphBorder
                        )
                    } else {
                        state.copy(activeBlockId = event.blockId)
                    }
                }
            }
            
            is RibbonEvent.OnAddParagraphAfter -> {
                val stateVal = _state.value
                val idx = stateVal.blocks.indexOfFirst { it.id == event.blockId }
                if (idx != -1) {
                    val prevBlock = stateVal.blocks[idx] as? TextBlock
                    val newId = "blk_${UUID.randomUUID()}"
                    val newBlock = if (prevBlock != null) {
                        TextBlock(
                            id = newId,
                            text = TextFieldValue(""),
                            alignment = prevBlock.alignment,
                            lineSpacing = prevBlock.lineSpacing,
                            isRtl = prevBlock.isRtl,
                            indentLevel = prevBlock.indentLevel,
                            isBulletedList = prevBlock.isBulletedList,
                            bulletShape = prevBlock.bulletShape,
                            isNumberedList = prevBlock.isNumberedList,
                            numberingStyle = prevBlock.numberingStyle,
                            paragraphShadingColor = prevBlock.paragraphShadingColor,
                            paragraphBorder = prevBlock.paragraphBorder,
                            fontSize = prevBlock.fontSize,
                            fontFamily = prevBlock.fontFamily,
                            isBold = prevBlock.isBold,
                            isItalic = prevBlock.isItalic,
                            isUnderline = prevBlock.isUnderline,
                            isStrikethrough = prevBlock.isStrikethrough,
                            textColor = prevBlock.textColor,
                            highlightColor = prevBlock.highlightColor
                        )
                    } else {
                        TextBlock(newId, TextFieldValue(""))
                    }
                    val newBlocks = stateVal.blocks.toMutableList()
                    newBlocks.add(idx + 1, newBlock)
                    _state.update { 
                        it.copy(
                            blocks = newBlocks, 
                            activeBlockId = newId,
                            fontSize = newBlock.fontSize,
                            fontFamily = newBlock.fontFamily,
                            isBold = newBlock.isBold,
                            isItalic = newBlock.isItalic,
                            isUnderline = newBlock.isUnderline,
                            isStrikethrough = newBlock.isStrikethrough,
                            textColor = newBlock.textColor,
                            highlightColor = newBlock.highlightColor,
                            isBulletedList = newBlock.isBulletedList,
                            bulletShape = newBlock.bulletShape,
                            isNumberedList = newBlock.isNumberedList,
                            numberingStyle = newBlock.numberingStyle
                        ) 
                    }
                }
            }

            is RibbonEvent.OnDeleteBlockIfEmpty -> {
                val stateVal = _state.value
                if (stateVal.blocks.size > 1) {
                    val idx = stateVal.blocks.indexOfFirst { it.id == event.blockId }
                    if (idx != -1) {
                        val newBlocks = stateVal.blocks.toMutableList()
                        newBlocks.removeAt(idx)
                        val newActiveId = if (idx > 0) newBlocks[idx - 1].id else newBlocks.first().id
                        _state.update { it.copy(blocks = newBlocks, activeBlockId = newActiveId) }
                    }
                }
            }
            is RibbonEvent.OnDeleteBlock -> {
                pushUndoState()
                val stateVal = _state.value
                if (stateVal.blocks.size > 1) {
                    val idx = stateVal.blocks.indexOfFirst { it.id == event.blockId }
                    if (idx != -1) {
                        val newBlocks = stateVal.blocks.toMutableList()
                        newBlocks.removeAt(idx)
                        val newActiveId = if (idx > 0) newBlocks[idx - 1].id else newBlocks.first().id
                        _state.update { it.copy(blocks = newBlocks, activeBlockId = newActiveId) }
                    }
                }
            }
            
            // Header & Footer
            is RibbonEvent.OnToggleHeaderFooterMode -> _state.update { it.copy(isEditingHeaderFooter = !it.isEditingHeaderFooter) }
            is RibbonEvent.OnHeaderTextChanged -> _state.update { it.copy(headerText = event.text) }
            is RibbonEvent.OnFooterTextChanged -> _state.update { it.copy(footerText = event.text) }
            
            // Insert Tab Operations
            is RibbonEvent.OnInsertPageBreakClicked -> insertBlockAtCursor(PageBreakBlock("brk_${UUID.randomUUID()}"))
            is RibbonEvent.OnInsertTableClicked -> {
                pushUndoState()
                val cells = mutableMapOf<String, TableCellModel>()
                for (r in 0 until event.rows) {
                    for (c in 0 until event.cols) {
                        val isHeader = r == 0
                        val text = if (isHeader) "Header ${c + 1}" else "Cell ${r + 1},${c + 1}"
                        cells["${r}_${c}"] = TableCellModel(
                            textBlocks = listOf(
                                TextBlock(
                                    id = "blk_${UUID.randomUUID()}",
                                    text = TextFieldValue(text),
                                    isBold = isHeader,
                                    textColor = if (isHeader) Color.White else Color(0xFF0F172A),
                                    alignment = TextAlignment.CENTER
                                )
                            ),
                            isHeader = isHeader,
                            backgroundColor = if (isHeader) Color(0xFF185ABD) else Color.Transparent
                        )
                    }
                }
                val equalRatio = 1f / event.cols.coerceAtLeast(1)
                val ratios = List(event.cols) { equalRatio }
                val tableId = "tbl_${UUID.randomUUID()}"
                insertBlockAtCursor(
                    TableBlock(
                        id = tableId,
                        rows = event.rows,
                        cols = event.cols,
                        cells = cells,
                        colWidthRatios = ratios,
                        tableStyle = TableStylePreset.BLUE_HEADER,
                        headerBackgroundColor = Color(0xFF185ABD),
                        headerTextColor = Color.White
                    )
                )
                _state.update { it.copy(activeBlockId = tableId) }
            }
            is RibbonEvent.OnTableCellChanged -> {
                _state.update { state ->
                    val newBlocks = state.blocks.map { block ->
                        if (block is TableBlock && block.id == event.blockId) {
                            val existingCell = block.cells[event.cellId]
                            val newTextBlocks = existingCell?.textBlocks?.toMutableList() ?: mutableListOf()
                            if (newTextBlocks.isNotEmpty()) {
                                val firstBlock = newTextBlocks[0]
                                newTextBlocks[0] = firstBlock.copy(text = event.text)
                            } else {
                                newTextBlocks.add(TextBlock("blk_${UUID.randomUUID()}", event.text))
                            }
                            val updatedCell = (existingCell ?: TableCellModel(emptyList())).copy(textBlocks = newTextBlocks)
                            val updatedCells = block.cells.toMutableMap().apply { put(event.cellId, updatedCell) }
                            block.copy(cells = updatedCells)
                        } else block
                    }
                    state.copy(blocks = newBlocks)
                }
            }
            is RibbonEvent.OnAddTableRow -> handleAddTableRow(event.blockId, event.atIndex, event.above)
            is RibbonEvent.OnDeleteTableRow -> handleDeleteTableRow(event.blockId, event.rowIndex)
            is RibbonEvent.OnAddTableColumn -> handleAddTableColumn(event.blockId, event.atIndex, event.left)
            is RibbonEvent.OnDeleteTableColumn -> handleDeleteTableColumn(event.blockId, event.colIndex)
            is RibbonEvent.OnDeleteTable -> {
                pushUndoState()
                _state.update { state ->
                    val newBlocks = state.blocks.filterNot { it.id == event.blockId }
                    val activeId = if (newBlocks.isNotEmpty()) newBlocks.first().id else ""
                    state.copy(blocks = if (newBlocks.isEmpty()) listOf(TextBlock("p_${UUID.randomUUID()}", TextFieldValue(""))) else newBlocks, activeBlockId = activeId)
                }
            }
            is RibbonEvent.OnSetTableCellBackground -> {
                pushUndoState()
                _state.update { state ->
                    val newBlocks = state.blocks.map { block ->
                        if (block is TableBlock && block.id == event.blockId) {
                            val existing = block.cells[event.cellId]
                            val updated = (existing ?: TableCellModel(emptyList())).copy(backgroundColor = event.color)
                            val newCells = block.cells.toMutableMap().apply { put(event.cellId, updated) }
                            block.copy(cells = newCells)
                        } else block
                    }
                    state.copy(blocks = newBlocks)
                }
            }
            is RibbonEvent.OnSetTableStylePreset -> handleSetTableStyle(event.blockId, event.preset)
            is RibbonEvent.OnSetTableBorderColor -> {
                pushUndoState()
                _state.update { state ->
                    val newBlocks = state.blocks.map { block ->
                        if (block is TableBlock && block.id == event.blockId) {
                            block.copy(borderColor = event.color)
                        } else block
                    }
                    state.copy(blocks = newBlocks)
                }
            }
            is RibbonEvent.OnToggleTableHeaderRow -> {
                pushUndoState()
                _state.update { state ->
                    val newBlocks = state.blocks.map { block ->
                        if (block is TableBlock && block.id == event.blockId) {
                            block.copy(hasHeaderRow = !block.hasHeaderRow)
                        } else block
                    }
                    state.copy(blocks = newBlocks)
                }
            }
            is RibbonEvent.OnToggleTableBandedRows -> {
                pushUndoState()
                _state.update { state ->
                    val newBlocks = state.blocks.map { block ->
                        if (block is TableBlock && block.id == event.blockId) {
                            block.copy(hasBandedRows = !block.hasBandedRows)
                        } else block
                    }
                    state.copy(blocks = newBlocks)
                }
            }
            is RibbonEvent.OnSetTableCellAlignment -> {
                pushUndoState()
                _state.update { state ->
                    val newBlocks = state.blocks.map { block ->
                        if (block is TableBlock && block.id == event.blockId) {
                            val existing = block.cells[event.cellId]
                            val newTextBlocks = existing?.textBlocks?.map { it.copy(alignment = event.alignment) } ?: emptyList()
                            val updated = (existing ?: TableCellModel(emptyList())).copy(alignment = event.alignment, textBlocks = newTextBlocks)
                            val newCells = block.cells.toMutableMap().apply { put(event.cellId, updated) }
                            block.copy(cells = newCells)
                        } else block
                    }
                    state.copy(blocks = newBlocks)
                }
            }
            is RibbonEvent.OnSetShapeType -> {
                pushUndoState()
                _state.update { state ->
                    val newBlocks = state.blocks.map { block ->
                        if (block is ShapeBlock && block.id == event.blockId) {
                            block.copy(type = event.type)
                        } else block
                    }
                    state.copy(blocks = newBlocks)
                }
            }
            is RibbonEvent.OnSetShapeFillColor -> {
                pushUndoState()
                _state.update { state ->
                    val newBlocks = state.blocks.map { block ->
                        if (block is ShapeBlock && block.id == event.blockId) {
                            block.copy(fillColor = event.color)
                        } else block
                    }
                    state.copy(blocks = newBlocks)
                }
            }
            is RibbonEvent.OnSetShapeStrokeColor -> {
                pushUndoState()
                _state.update { state ->
                    val newBlocks = state.blocks.map { block ->
                        if (block is ShapeBlock && block.id == event.blockId) {
                            block.copy(strokeColor = event.color)
                        } else block
                    }
                    state.copy(blocks = newBlocks)
                }
            }
            is RibbonEvent.OnSetShapeStrokeWidth -> {
                pushUndoState()
                _state.update { state ->
                    val newBlocks = state.blocks.map { block ->
                        if (block is ShapeBlock && block.id == event.blockId) {
                            block.copy(strokeWidth = event.strokeWidth)
                        } else block
                    }
                    state.copy(blocks = newBlocks)
                }
            }
            is RibbonEvent.OnSetShapeText -> {
                pushUndoState()
                _state.update { state ->
                    val newBlocks = state.blocks.map { block ->
                        if (block is ShapeBlock && block.id == event.blockId) {
                            block.copy(text = event.text)
                        } else block
                    }
                    state.copy(blocks = newBlocks)
                }
            }
            is RibbonEvent.OnSetShapeTextColor -> {
                pushUndoState()
                _state.update { state ->
                    val newBlocks = state.blocks.map { block ->
                        if (block is ShapeBlock && block.id == event.blockId) {
                            block.copy(textColor = event.color)
                        } else block
                    }
                    state.copy(blocks = newBlocks)
                }
            }
            is RibbonEvent.OnSetShapeHeight -> {
                pushUndoState()
                _state.update { state ->
                    val newBlocks = state.blocks.map { block ->
                        if (block is ShapeBlock && block.id == event.blockId) {
                            block.copy(height = event.height)
                        } else block
                    }
                    state.copy(blocks = newBlocks)
                }
            }
            is RibbonEvent.OnInsertImageWithUri -> {
                insertBlockAtCursor(ImageBlock("img_${UUID.randomUUID()}", uri = event.uri, wrapMode = WrapMode.IN_LINE))
            }
            is RibbonEvent.OnInsertImageBytes -> {
                insertBlockAtCursor(ImageBlock("img_${UUID.randomUUID()}", imageData = event.bytes, uri = event.uri, wrapMode = WrapMode.IN_LINE))
            }
            is RibbonEvent.OnPickImageRequested -> {}
            is RibbonEvent.OnInsertShapeClicked -> {
                insertBlockAtCursor(ShapeBlock("shp_${UUID.randomUUID()}", event.type))
            }
            is RibbonEvent.OnInsertPageNumberClicked -> {
                val pageNumChar = if (_state.value.numeralSystem == NumeralSystem.ARABIC) "١" else "1"
                val currentFooter = _state.value.footerText
                val newFooter = currentFooter.copy(text = (currentFooter.text + " Page $pageNumChar ").trim())
                _state.update { it.copy(footerText = newFooter, showPageNumbers = true) }
            }
            
            // Clipboard & Format Painter
            is RibbonEvent.OnCutClicked -> {}
            is RibbonEvent.OnCopyClicked -> {}
            is RibbonEvent.OnPasteClicked -> {}
            is RibbonEvent.OnPasteSpecialClicked -> {} // Handled with platform clipboard in UI
            is RibbonEvent.OnFormatPainterToggled -> {
                val current = _state.value
                if (current.isFormatPainterActive && !event.isLocked) {
                    _state.update { it.copy(isFormatPainterActive = false, isFormatPainterLocked = false) }
                } else {
                    val format = CopiedCharacterFormat(
                        fontFamily = current.fontFamily,
                        fontSize = current.fontSize,
                        isBold = current.isBold,
                        isItalic = current.isItalic,
                        isUnderline = current.isUnderline,
                        isStrikethrough = current.isStrikethrough,
                        isSubscript = current.isSubscript,
                        isSuperscript = current.isSuperscript,
                        textColor = current.textColor,
                        highlightColor = current.highlightColor,
                        alignment = current.alignment,
                        lineSpacing = current.lineSpacing
                    )
                    _state.update { 
                        it.copy(
                            isFormatPainterActive = true, 
                            isFormatPainterLocked = event.isLocked,
                            copiedFormat = format
                        ) 
                    }
                }
            }
            is RibbonEvent.OnCutTextFromSelection -> {
                pushUndoState()
                val stateVal = _state.value
                val activeIdx = stateVal.blocks.indexOfFirst { it.id == stateVal.activeBlockId }
                if (activeIdx != -1 && stateVal.blocks[activeIdx] is TextBlock) {
                    val tb = stateVal.blocks[activeIdx] as TextBlock
                    val textVal = tb.text
                    val min = textVal.selection.min.coerceIn(0, textVal.annotatedString.length)
                    val max = textVal.selection.max.coerceIn(0, textVal.annotatedString.length)
                    if (min < max) {
                        val newAnnotated = buildAnnotatedString {
                            append(textVal.annotatedString.subSequence(0, min))
                            append(textVal.annotatedString.subSequence(max, textVal.annotatedString.length))
                        }
                        val updated = tb.copy(text = TextFieldValue(annotatedString = newAnnotated, selection = TextRange(min)))
                        val newBlocks = stateVal.blocks.toMutableList()
                        newBlocks[activeIdx] = updated
                        _state.update { it.copy(blocks = newBlocks) }
                    }
                }
            }
            is RibbonEvent.OnPasteTextAtSelection -> {
                pushUndoState()
                val stateVal = _state.value
                val activeIdx = stateVal.blocks.indexOfFirst { it.id == stateVal.activeBlockId }
                if (activeIdx != -1 && stateVal.blocks[activeIdx] is TextBlock) {
                    val tb = stateVal.blocks[activeIdx] as TextBlock
                    val textVal = tb.text
                    val min = textVal.selection.min.coerceIn(0, textVal.annotatedString.length)
                    val max = textVal.selection.max.coerceIn(0, textVal.annotatedString.length)
                    val newAnnotated = buildAnnotatedString {
                        append(textVal.annotatedString.subSequence(0, min))
                        append(event.text)
                        append(textVal.annotatedString.subSequence(max, textVal.annotatedString.length))
                    }
                    val newCursorPos = min + event.text.length
                    val updated = tb.copy(text = TextFieldValue(annotatedString = newAnnotated, selection = TextRange(newCursorPos)))
                    val newBlocks = stateVal.blocks.toMutableList()
                    newBlocks[activeIdx] = updated
                    _state.update { it.copy(blocks = newBlocks) }
                }
            }
            
            // Selection actions
            is RibbonEvent.OnSelectAllClicked -> {
                val stateVal = _state.value
                val newBlocks = stateVal.blocks.map { block ->
                    if (block is TextBlock) {
                        val len = block.text.annotatedString.length
                        block.copy(text = block.text.copy(selection = TextRange(0, len)))
                    } else block
                }
                _state.update { it.copy(blocks = newBlocks) }
            }
            is RibbonEvent.OnSelectCurrentBlockClicked -> {
                val stateVal = _state.value
                val activeIdx = stateVal.blocks.indexOfFirst { it.id == stateVal.activeBlockId }
                if (activeIdx != -1 && stateVal.blocks[activeIdx] is TextBlock) {
                    val tb = stateVal.blocks[activeIdx] as TextBlock
                    val len = tb.text.annotatedString.length
                    val updated = tb.copy(text = tb.text.copy(selection = TextRange(0, len)))
                    val newBlocks = stateVal.blocks.toMutableList()
                    newBlocks[activeIdx] = updated
                    _state.update { it.copy(blocks = newBlocks) }
                }
            }
            is RibbonEvent.OnSelectSimilarFormattingClicked -> {
                val stateVal = _state.value
                val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock
                if (activeBlock != null) {
                    val newBlocks = stateVal.blocks.map { block ->
                        if (block is TextBlock && block.fontSize == activeBlock.fontSize) {
                            val len = block.text.annotatedString.length
                            block.copy(text = block.text.copy(selection = TextRange(0, len)))
                        } else block
                    }
                    _state.update { it.copy(blocks = newBlocks) }
                }
            }
            
            // Character Formatting Updates
            is RibbonEvent.OnFontFamilyChanged -> {
                pushUndoState()
                val fontName = event.family
                com.example.presentation.editor.font.FontEngine.addRecentFont(fontName)
                _state.update { it.copy(fontFamily = fontName) }
                
                val stateVal = _state.value
                val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock
                val resolvedFontFamily = com.example.presentation.editor.font.FontEngine.getFontFamily(fontName)
                if (activeBlock != null) {
                    val sel = activeBlock.text.selection
                    if (sel.collapsed || (sel.min == 0 && sel.max == activeBlock.text.annotatedString.length)) {
                        updateActiveBlock { if (it is TextBlock) it.copy(fontFamily = fontName) else it }
                    }
                }
                applyStyleToSelection(SpanStyle(fontFamily = resolvedFontFamily))
            }
            is RibbonEvent.OnFontSizeChanged -> {
                pushUndoState()
                _state.update { it.copy(fontSize = event.size) }
                val stateVal = _state.value
                val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock
                if (activeBlock != null) {
                    val sel = activeBlock.text.selection
                    if (sel.collapsed || (sel.min == 0 && sel.max == activeBlock.text.annotatedString.length)) {
                        updateActiveBlock { if (it is TextBlock) it.copy(fontSize = event.size) else it }
                    }
                }
                applyStyleToSelection(SpanStyle(fontSize = event.size.sp))
            }
            is RibbonEvent.OnBoldClicked -> {
                pushUndoState()
                val stateVal = _state.value
                val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock
                val currentIsBold = if (activeBlock != null) {
                    val sel = activeBlock.text.selection
                    if (sel.min < sel.max) {
                        isRangeBold(activeBlock.text.annotatedString, sel.min, sel.max, activeBlock.isBold)
                    } else {
                        activeBlock.isBold || stateVal.isBold
                    }
                } else stateVal.isBold

                val newBold = !currentIsBold
                _state.update { it.copy(isBold = newBold) }
                if (activeBlock != null) {
                    val sel = activeBlock.text.selection
                    if (sel.collapsed || (sel.min == 0 && sel.max == activeBlock.text.annotatedString.length)) {
                        updateActiveBlock { if (it is TextBlock) it.copy(isBold = newBold) else it }
                    }
                }
                applyStyleToSelection(SpanStyle(fontWeight = if (newBold) FontWeight.Bold else FontWeight.Normal))
            }
            is RibbonEvent.OnItalicClicked -> {
                pushUndoState()
                val stateVal = _state.value
                val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock
                val currentIsItalic = if (activeBlock != null) {
                    val sel = activeBlock.text.selection
                    if (sel.min < sel.max) {
                        isRangeItalic(activeBlock.text.annotatedString, sel.min, sel.max, activeBlock.isItalic)
                    } else {
                        activeBlock.isItalic || stateVal.isItalic
                    }
                } else stateVal.isItalic

                val newItalic = !currentIsItalic
                _state.update { it.copy(isItalic = newItalic) }
                if (activeBlock != null) {
                    val sel = activeBlock.text.selection
                    if (sel.collapsed || (sel.min == 0 && sel.max == activeBlock.text.annotatedString.length)) {
                        updateActiveBlock { if (it is TextBlock) it.copy(isItalic = newItalic) else it }
                    }
                }
                applyStyleToSelection(SpanStyle(fontStyle = if (newItalic) FontStyle.Italic else FontStyle.Normal))
            }
            is RibbonEvent.OnUnderlineClicked -> {
                pushUndoState()
                val stateVal = _state.value
                val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock
                val currentIsUnderline = if (activeBlock != null) {
                    val sel = activeBlock.text.selection
                    if (sel.min < sel.max) {
                        isRangeUnderline(activeBlock.text.annotatedString, sel.min, sel.max, activeBlock.isUnderline)
                    } else {
                        activeBlock.isUnderline || stateVal.isUnderline
                    }
                } else stateVal.isUnderline

                val newUnderline = !currentIsUnderline
                _state.update { it.copy(isUnderline = newUnderline) }
                if (activeBlock != null) {
                    val sel = activeBlock.text.selection
                    if (sel.collapsed || (sel.min == 0 && sel.max == activeBlock.text.annotatedString.length)) {
                        updateActiveBlock { if (it is TextBlock) it.copy(isUnderline = newUnderline) else it }
                    }
                }
                applyStyleToSelection(SpanStyle(textDecoration = if (newUnderline) TextDecoration.Underline else TextDecoration.None))
            }
            is RibbonEvent.OnUnderlineStyleChanged -> { 
                pushUndoState()
                _state.update { it.copy(underlineStyle = event.style) } 
            }
            is RibbonEvent.OnStrikethroughClicked -> {
                pushUndoState()
                val stateVal = _state.value
                val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock
                val currentIsStrike = if (activeBlock != null) {
                    val sel = activeBlock.text.selection
                    if (sel.min < sel.max) {
                        isRangeStrikethrough(activeBlock.text.annotatedString, sel.min, sel.max, activeBlock.isStrikethrough)
                    } else {
                        activeBlock.isStrikethrough || stateVal.isStrikethrough
                    }
                } else stateVal.isStrikethrough

                val newStrike = !currentIsStrike
                _state.update { it.copy(isStrikethrough = newStrike) }
                if (activeBlock != null) {
                    val sel = activeBlock.text.selection
                    if (sel.collapsed || (sel.min == 0 && sel.max == activeBlock.text.annotatedString.length)) {
                        updateActiveBlock { if (it is TextBlock) it.copy(isStrikethrough = newStrike) else it }
                    }
                }
                applyStyleToSelection(SpanStyle(textDecoration = if (newStrike) TextDecoration.LineThrough else TextDecoration.None))
            }
            is RibbonEvent.OnSubscriptClicked -> {
                pushUndoState()
                val newSub = !_state.value.isSubscript
                _state.update { it.copy(isSubscript = newSub, isSuperscript = false) }
                applyStyleToSelection(SpanStyle(baselineShift = if (newSub) BaselineShift.Subscript else BaselineShift.None))
            }
            is RibbonEvent.OnSuperscriptClicked -> {
                pushUndoState()
                val newSuper = !_state.value.isSuperscript
                _state.update { it.copy(isSuperscript = newSuper, isSubscript = false) }
                applyStyleToSelection(SpanStyle(baselineShift = if (newSuper) BaselineShift.Superscript else BaselineShift.None))
            }
            is RibbonEvent.OnTextColorChanged -> {
                pushUndoState()
                _state.update { it.copy(textColor = event.color) }
                val stateVal = _state.value
                val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock
                if (activeBlock != null) {
                    val sel = activeBlock.text.selection
                    if (sel.collapsed || (sel.min == 0 && sel.max == activeBlock.text.annotatedString.length)) {
                        updateActiveBlock { if (it is TextBlock) it.copy(textColor = event.color) else it }
                    }
                }
                applyStyleToSelection(SpanStyle(color = event.color))
            }
            is RibbonEvent.OnHighlightColorChanged -> {
                pushUndoState()
                _state.update { it.copy(highlightColor = event.color) }
                val stateVal = _state.value
                val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock
                if (activeBlock != null) {
                    val sel = activeBlock.text.selection
                    if (sel.collapsed || (sel.min == 0 && sel.max == activeBlock.text.annotatedString.length)) {
                        updateActiveBlock { if (it is TextBlock) it.copy(highlightColor = event.color) else it }
                    }
                }
                applyStyleToSelection(SpanStyle(background = event.color))
            }
            is RibbonEvent.OnTextEffectChanged -> {
                pushUndoState()
                _state.update { it.copy(textEffect = event.effect) }
                when (event.effect) {
                    TextEffectType.SHADOW -> applyStyleToSelection(SpanStyle(shadow = androidx.compose.ui.graphics.Shadow(color = Color(0x88000000), offset = androidx.compose.ui.geometry.Offset(2f, 2f), blurRadius = 3f)))
                    TextEffectType.GLOW -> applyStyleToSelection(SpanStyle(shadow = androidx.compose.ui.graphics.Shadow(color = Color(0xFFFFB300), blurRadius = 8f)))
                    TextEffectType.REFLECTION -> applyStyleToSelection(SpanStyle(shadow = androidx.compose.ui.graphics.Shadow(color = Color(0x664A90E2), offset = androidx.compose.ui.geometry.Offset(0f, 4f), blurRadius = 4f)))
                    TextEffectType.OUTLINE -> applyStyleToSelection(SpanStyle(color = Color(0xFF1D4ED8), fontWeight = FontWeight.ExtraBold))
                    TextEffectType.GRADIENT -> applyStyleToSelection(SpanStyle(color = Color(0xFF7C3AED)))
                    TextEffectType.NONE -> applyStyleToSelection(SpanStyle(shadow = null))
                }
            }
            is RibbonEvent.OnClearFormattingClicked -> {
                pushUndoState()
                clearFormattingFromSelection()
            }
            is RibbonEvent.OnNumeralSystemChanged -> {
                pushUndoState()
                applyNumeralSystem(event.system)
            }
            
            // Paragraph Formatting
            is RibbonEvent.OnAlignmentChanged -> {
                pushUndoState()
                _state.update { it.copy(alignment = event.alignment) }
                updateActiveBlock { block ->
                    if (block is TextBlock) block.copy(alignment = event.alignment) else block
                }
            }
            is RibbonEvent.OnLineSpacingChanged -> {
                pushUndoState()
                _state.update { it.copy(lineSpacing = event.spacing) }
                updateActiveBlock { block ->
                    if (block is TextBlock) block.copy(lineSpacing = event.spacing) else block
                }
            }
            is RibbonEvent.OnIncreaseIndentClicked -> {
                pushUndoState()
                _state.update { it.copy(indentLevel = it.indentLevel + 1) }
                updateActiveBlock { block ->
                    if (block is TextBlock) block.copy(indentLevel = _state.value.indentLevel) else block
                }
            }
            is RibbonEvent.OnDecreaseIndentClicked -> {
                pushUndoState()
                _state.update { it.copy(indentLevel = maxOf(0, it.indentLevel - 1)) }
                updateActiveBlock { block ->
                    if (block is TextBlock) block.copy(indentLevel = _state.value.indentLevel) else block
                }
            }
            is RibbonEvent.OnTextDirectionToggled -> {
                pushUndoState()
                val newRtl = !_state.value.isTextRtl
                _state.update { it.copy(isTextRtl = newRtl) }
                updateActiveBlock { block ->
                    if (block is TextBlock) block.copy(isRtl = newRtl) else block
                }
            }
            is RibbonEvent.OnTextDirectionChanged -> {
                pushUndoState()
                val newRtl = event.isRtl
                _state.update { it.copy(isTextRtl = newRtl) }
                updateActiveBlock { block ->
                    if (block is TextBlock) block.copy(isRtl = newRtl) else block
                }
            }
            is RibbonEvent.OnBulletedListToggled -> {
                pushUndoState()
                _state.update { it.copy(isBulletedList = !it.isBulletedList, isNumberedList = false) }
                updateActiveBlock { block ->
                    if (block is TextBlock) block.copy(isBulletedList = _state.value.isBulletedList, isNumberedList = false) else block
                }
            }
            is RibbonEvent.OnBulletShapeChanged -> {
                pushUndoState()
                _state.update { it.copy(isBulletedList = true, bulletShape = event.shape, isNumberedList = false) }
                updateActiveBlock { block ->
                    if (block is TextBlock) block.copy(isBulletedList = true, bulletShape = event.shape, isNumberedList = false) else block
                }
            }
            is RibbonEvent.OnNumberedListToggled -> {
                pushUndoState()
                _state.update { it.copy(isNumberedList = !it.isNumberedList, isBulletedList = false) }
                updateActiveBlock { block ->
                    if (block is TextBlock) block.copy(isNumberedList = _state.value.isNumberedList, isBulletedList = false) else block
                }
            }
            is RibbonEvent.OnNumberingStyleChanged -> {
                pushUndoState()
                _state.update { it.copy(isNumberedList = true, numberingStyle = event.style, isBulletedList = false) }
                updateActiveBlock { block ->
                    if (block is TextBlock) block.copy(isNumberedList = true, numberingStyle = event.style, isBulletedList = false) else block
                }
            }
            is RibbonEvent.OnMultilevelStyleChanged -> {
                pushUndoState()
                _state.update { it.copy(multilevelStyle = event.style) }
            }
            is RibbonEvent.OnSortParagraphsClicked -> {
                pushUndoState()
                sortParagraphs(event.ascending)
            }
            is RibbonEvent.OnToggleNonPrintingCharacters -> {
                _state.update { it.copy(showNonPrintingCharacters = !it.showNonPrintingCharacters) }
            }
            is RibbonEvent.OnParagraphShadingChanged -> {
                pushUndoState()
                _state.update { it.copy(paragraphShadingColor = event.color) }
                updateActiveBlock { block ->
                    if (block is TextBlock) block.copy(paragraphShadingColor = event.color) else block
                }
            }
            is RibbonEvent.OnParagraphBorderChanged -> {
                pushUndoState()
                _state.update { it.copy(paragraphBorder = event.border) }
                updateActiveBlock { block ->
                    if (block is TextBlock) block.copy(paragraphBorder = event.border) else block
                }
            }
            
            // Custom Styles
            is RibbonEvent.OnCreateCustomStyle -> {
                val newStyle = CustomStyleModel(
                    id = "style_${UUID.randomUUID()}",
                    name = event.name,
                    fontFamily = event.fontFamily,
                    fontSize = event.fontSize,
                    isBold = event.isBold,
                    isItalic = event.isItalic,
                    textColor = event.textColor,
                    alignment = event.alignment
                )
                _state.update { it.copy(customStyles = it.customStyles + newStyle) }
            }
            is RibbonEvent.OnApplyCustomStyle -> {
                pushUndoState()
                val st = event.style
                _state.update {
                    it.copy(
                        fontFamily = st.fontFamily,
                        fontSize = st.fontSize,
                        isBold = st.isBold,
                        isItalic = st.isItalic,
                        textColor = st.textColor,
                        alignment = st.alignment
                    )
                }
                updateActiveBlock { block ->
                    if (block is TextBlock) {
                        block.copy(
                            fontFamily = st.fontFamily,
                            fontSize = st.fontSize,
                            isBold = st.isBold,
                            isItalic = st.isItalic,
                            textColor = st.textColor,
                            alignment = st.alignment
                        )
                    } else block
                }
                applyStyleToSelection(
                    SpanStyle(
                        fontSize = st.fontSize.sp,
                        fontWeight = if (st.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (st.isItalic) FontStyle.Italic else FontStyle.Normal,
                        color = st.textColor
                    )
                )
            }
            is RibbonEvent.OnTableCellFocused -> {
                _state.update { it.copy(activeBlockId = event.blockId, activeTableCellId = event.cellId) }
            }
            is RibbonEvent.OnMergeCells -> {
                handleMergeTableCells(event.blockId)
            }
            is RibbonEvent.OnSplitCells -> {
                handleSplitTableCell(event.blockId, event.cellId)
            }
            is RibbonEvent.OnSetUserError -> { _state.update { it.copy(userErrorMessage = event.message) } }
            is RibbonEvent.OnDismissUserError -> { _state.update { it.copy(userErrorMessage = null) } }
            
            // Page Layout
            is RibbonEvent.OnPageSizeChanged -> {
                pushUndoState()
                _state.update { it.copy(pageSize = event.size) }
            }
            is RibbonEvent.OnPageOrientationChanged -> {
                pushUndoState()
                _state.update { it.copy(pageOrientation = event.orientation) }
            }
            is RibbonEvent.OnPageMarginChanged -> {
                pushUndoState()
                _state.update { it.copy(pageMargin = event.margin) }
            }
            is RibbonEvent.OnPageColumnsChanged -> {
                pushUndoState()
                _state.update { it.copy(pageColumns = event.columns) }
            }
            is RibbonEvent.OnToggleRuler -> {
                _state.update { it.copy(showRuler = !it.showRuler) }
            }
            is RibbonEvent.OnFirstLineIndentChanged -> {
                pushUndoState()
                _state.update { it.copy(firstLineIndentDp = event.indentDp) }
                updateActiveBlock { block ->
                    if (block is TextBlock) block.copy(firstLineIndentDp = event.indentDp) else block
                }
            }
            is RibbonEvent.OnHangingIndentChanged -> {
                pushUndoState()
                _state.update { it.copy(hangingIndentDp = event.indentDp) }
                updateActiveBlock { block ->
                    if (block is TextBlock) block.copy(hangingIndentDp = event.indentDp) else block
                }
            }
            
            // Page Design
            is RibbonEvent.OnWatermarkChanged -> {
                pushUndoState()
                _state.update { it.copy(watermarkText = event.text) }
            }
            is RibbonEvent.OnPageColorChanged -> {
                pushUndoState()
                _state.update { it.copy(pageColor = event.color) }
            }
            is RibbonEvent.OnPageBorderChanged -> {
                pushUndoState()
                _state.update { it.copy(pageBorder = event.border) }
            }
            is RibbonEvent.OnApplyDocumentTheme -> {
                pushUndoState()
                val (font, textColor, pageColor) = when (event.themeName) {
                    "Office" -> Triple("Calibri", Color(0xFF0F172A), Color.White)
                    "Modern" -> Triple("Arial", Color(0xFF1E293B), Color(0xFFF8FAFC))
                    "Formal" -> Triple("Times New Roman", Color(0xFF0F172A), Color(0xFFFFFDF5))
                    "Classic" -> Triple("Georgia", Color(0xFF1A1A1A), Color(0xFFFAFAFA))
                    "Emerald" -> Triple("Calibri", Color(0xFF064E3B), Color(0xFFF0FDF4))
                    "Warm Amber" -> Triple("Times New Roman", Color(0xFF78350F), Color(0xFFFFFBEB))
                    else -> Triple("Calibri", Color(0xFF0F172A), Color.White)
                }
                _state.update { state ->
                    val updatedBlocks = state.blocks.map { block ->
                        if (block is TextBlock) {
                            block.copy(fontFamily = font, textColor = textColor)
                        } else block
                    }
                    state.copy(
                        fontFamily = font,
                        textColor = textColor,
                        pageColor = pageColor,
                        blocks = updatedBlocks
                    )
                }
            }

            // Tools & Export & Dialogs
            is RibbonEvent.OnUndoClicked -> {
                if (undoStack.isNotEmpty()) {
                    val prev = undoStack.removeAt(undoStack.lastIndex)
                    redoStack.add(_state.value.blocks)
                    _state.update {
                        it.copy(
                            blocks = prev,
                            canUndo = undoStack.isNotEmpty(),
                            canRedo = true
                        )
                    }
                }
            }
            is RibbonEvent.OnRedoClicked -> {
                if (redoStack.isNotEmpty()) {
                    val next = redoStack.removeAt(redoStack.lastIndex)
                    undoStack.add(_state.value.blocks)
                    _state.update {
                        it.copy(
                            blocks = next,
                            canUndo = true,
                            canRedo = redoStack.isNotEmpty()
                        )
                    }
                }
            }
            is RibbonEvent.OnIncreaseFontSizeClicked -> {
                pushUndoState()
                val newSize = (_state.value.fontSize + 2).coerceAtMost(72)
                _state.update { it.copy(fontSize = newSize) }
                applyStyleToSelection(SpanStyle(fontSize = newSize.sp))
            }
            is RibbonEvent.OnDecreaseFontSizeClicked -> {
                pushUndoState()
                val newSize = (_state.value.fontSize - 2).coerceAtLeast(6)
                _state.update { it.copy(fontSize = newSize) }
                applyStyleToSelection(SpanStyle(fontSize = newSize.sp))
            }
            is RibbonEvent.OnChangeCaseClicked -> {
                pushUndoState()
                changeTextCase(event.caseType)
            }
            is RibbonEvent.OnApplyHeadingStyle -> {
                pushUndoState()
                applyHeadingStyle(event.styleName)
            }
            is RibbonEvent.OnShowFindReplaceDialog -> _state.update { it.copy(showFindReplaceDialog = true) }
            is RibbonEvent.OnDismissFindReplaceDialog -> _state.update { it.copy(showFindReplaceDialog = false) }
            is RibbonEvent.OnShowTemplatesDialog -> _state.update { it.copy(showTemplatesDialog = true) }
            is RibbonEvent.OnDismissTemplatesDialog -> _state.update { it.copy(showTemplatesDialog = false) }
            is RibbonEvent.OnApplyDocumentTemplate -> {
                pushUndoState()
                val newBlocks = when (event.templateId) {
                    "RESUME" -> DocumentFactory.createResumeTemplate()
                    "BUSINESS_LETTER" -> DocumentFactory.createBusinessLetterTemplate()
                    "EXECUTIVE_REPORT" -> DocumentFactory.createExecutiveReportTemplate()
                    "ACADEMIC_PAPER" -> DocumentFactory.createAcademicPaperTemplate()
                    else -> DocumentFactory.createComprehensiveTestDocument()
                }
                _state.update {
                    it.copy(
                        blocks = newBlocks,
                        activeBlockId = newBlocks.firstOrNull()?.id ?: "blk_initial",
                        isProtectedView = true,
                        viewMode = ViewMode.READ_MODE,
                        showTemplatesDialog = false
                    )
                }
            }
            is RibbonEvent.OnFindAndReplaceClicked -> {
                pushUndoState()
                performFindAndReplace(event.findText, event.replaceText)
            }
            is RibbonEvent.OnShowGroupDetails -> _state.update { it.copy(activeGroupDetailsDialog = event.groupName) }
            is RibbonEvent.OnDismissGroupDetails -> _state.update { it.copy(activeGroupDetailsDialog = null) }
            is RibbonEvent.OnExportPdfClicked -> {}
            is RibbonEvent.OnShowWordCountClicked -> _state.update { it.copy(showWordCountDialog = true) }
            is RibbonEvent.OnDismissWordCountClicked -> _state.update { it.copy(showWordCountDialog = false) }
            is RibbonEvent.OnDismissExportPdfDialog -> _state.update { it.copy(showExportPdfSuccessDialog = false) }
            is RibbonEvent.OnInsertBannerClicked -> insertBlockAtCursor(BannerBlock("bnr_${UUID.randomUUID()}", title = event.title, subtitle = event.subtitle))
            is RibbonEvent.OnInsertCalloutClicked -> insertBlockAtCursor(CalloutBlock("clt_${UUID.randomUUID()}", title = event.title, text = TextFieldValue(event.text)))
            is RibbonEvent.OnInsertDividerClicked -> insertBlockAtCursor(DividerBlock("div_${UUID.randomUUID()}"))
            is RibbonEvent.OnInsertSignatureLineClicked -> insertBlockAtCursor(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue("\n_________________________\nالتوقيع / Signature\n")))
            
            // Drawing
            is RibbonEvent.OnToggleDrawingMode -> { _state.update { it.copy(isDrawingMode = !it.isDrawingMode, isEraserMode = false) } }
            is RibbonEvent.OnToggleHighlighterMode -> { _state.update { it.copy(isDrawingMode = true, isHighlighterMode = !it.isHighlighterMode, isEraserMode = false) } }
            is RibbonEvent.OnToggleEraserMode -> { _state.update { it.copy(isDrawingMode = true, isEraserMode = !it.isEraserMode, isHighlighterMode = false) } }
            is RibbonEvent.OnInkColorChanged -> { _state.update { it.copy(inkColor = event.color, isEraserMode = false) } }
            is RibbonEvent.OnInkThicknessChanged -> { _state.update { it.copy(inkThickness = event.thickness) } }
            is RibbonEvent.OnDrawPathAdded -> { _state.update { it.copy(drawingPaths = it.drawingPaths + event.path) } }
            is RibbonEvent.OnClearDrawing -> { _state.update { it.copy(drawingPaths = emptyList()) } }
            is RibbonEvent.OnInsertSymbolClicked -> {
                pushUndoState()
                val activeIdx = _state.value.blocks.indexOfFirst { it.id == _state.value.activeBlockId }
                if (activeIdx != -1 && _state.value.blocks[activeIdx] is TextBlock) {
                    val tb = _state.value.blocks[activeIdx] as TextBlock
                    val newText = tb.text.text + event.symbol
                    val updated = tb.copy(text = tb.text.copy(text = newText))
                    val newBlocks = _state.value.blocks.toMutableList()
                    newBlocks[activeIdx] = updated
                    _state.update { it.copy(blocks = newBlocks) }
                }
            }
            is RibbonEvent.OnInsertTableOfContents -> {
                pushUndoState()
                val headings = _state.value.blocks.filterIsInstance<TextBlock>().filter { it.isBold || it.fontSize >= 14 }
                val tocLines = StringBuilder()
                val isRtl = _state.value.isRtl
                tocLines.append(if (isRtl) "📑 جدول المحتويات الفهرسي\n" else "📑 Table of Contents\n")
                tocLines.append("════════════════════════════════════════\n")
                if (headings.isNotEmpty()) {
                    headings.forEachIndexed { i, h ->
                        val title = h.text.text.trim().take(40)
                        val dots = ".".repeat((35 - title.length).coerceAtLeast(3))
                        tocLines.append("${i + 1}. $title $dots ${i + 1}\n")
                    }
                } else {
                    tocLines.append(if (isRtl) "1. المقدمة والأهداف العامة ........................... 1\n2. منهجية العمل والتحليل ............................ 2\n3. التوصيات والنتائج الختامية ........................ 3\n" else "1. Introduction & Overview ........................... 1\n2. Operational Methodology .......................... 2\n3. Final Recommendations ............................ 3\n")
                }
                insertBlockAtCursor(CalloutBlock("toc_${UUID.randomUUID()}", title = if (isRtl) "جدول المحتويات" else "Table of Contents", text = TextFieldValue(tocLines.toString())))
            }
            is RibbonEvent.OnInsertFootnote -> {
                pushUndoState()
                val footnoteId = "fn_${UUID.randomUUID()}"
                val stateVal = _state.value
                val activeIdx = stateVal.blocks.indexOfFirst { it.id == stateVal.activeBlockId }
                if (activeIdx != -1 && stateVal.blocks[activeIdx] is TextBlock) {
                    val tb = stateVal.blocks[activeIdx] as TextBlock
                    val updated = tb.copy(text = tb.text.copy(text = tb.text.text + " [¹]"))
                    val newBlocks = stateVal.blocks.toMutableList()
                    newBlocks[activeIdx] = updated
                    newBlocks.add(TextBlock(footnoteId, TextFieldValue("────────────────────\n[¹] ${event.noteText}"), fontSize = 10, isItalic = true))
                    _state.update { it.copy(blocks = newBlocks) }
                } else {
                    insertBlockAtCursor(TextBlock(footnoteId, TextFieldValue("────────────────────\n[¹] ${event.noteText}"), fontSize = 10, isItalic = true))
                }
            }
            is RibbonEvent.OnInsertCitation -> {
                pushUndoState()
                val citationText = " (${event.author}, ${event.year})"
                val activeIdx = _state.value.blocks.indexOfFirst { it.id == _state.value.activeBlockId }
                if (activeIdx != -1 && _state.value.blocks[activeIdx] is TextBlock) {
                    val tb = _state.value.blocks[activeIdx] as TextBlock
                    val updated = tb.copy(text = tb.text.copy(text = tb.text.text + citationText))
                    val newBlocks = _state.value.blocks.toMutableList()
                    newBlocks[activeIdx] = updated
                    _state.update { it.copy(blocks = newBlocks) }
                } else {
                    insertBlockAtCursor(TextBlock("cit_${UUID.randomUUID()}", TextFieldValue("${event.style} Citation: ${event.author} (${event.year}). ${event.title}.")))
                }
            }
            is RibbonEvent.OnInsertEnvelopes -> {
                pushUndoState()
                val envelopeContent = "================ ENVELOPE [${event.size}] ================\n\n" +
                        "FROM:\n${event.returnAddress}\n\n" +
                        "TO:\n${event.recipientName}\n${event.deliveryAddress}\n\n" +
                        "==========================================================="
                insertBlockAtCursor(CalloutBlock("env_${UUID.randomUUID()}", title = if (_state.value.isRtl) "مغلف بريدي رسمي" else "Official Mailing Envelope", text = TextFieldValue(envelopeContent)))
            }
            is RibbonEvent.OnApplyMailMerge -> {
                pushUndoState()
                val mergeBlocks = mutableListOf<DocumentBlock>()
                event.recipients.forEachIndexed { idx, r ->
                    mergeBlocks.add(BannerBlock("mbnr_${UUID.randomUUID()}", title = "Mail Merge Copy #${idx + 1}", subtitle = "Recipient: ${r.name} (${r.email})"))
                    _state.value.blocks.filterIsInstance<TextBlock>().forEach { tb ->
                        val mergedText = tb.text.text
                            .replace("«Name»", r.name)
                            .replace("«الاسم»", r.name)
                            .replace("«Email»", r.email)
                            .replace("«البريد»", r.email)
                            .replace("«Date»", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                        mergeBlocks.add(tb.copy(id = "mblk_${UUID.randomUUID()}", text = TextFieldValue(mergedText)))
                    }
                    mergeBlocks.add(PageBreakBlock("mpb_${UUID.randomUUID()}"))
                }
                if (mergeBlocks.isNotEmpty()) {
                    _state.update { it.copy(blocks = mergeBlocks) }
                }
            }
            is RibbonEvent.OnApplyAiResult -> {
                pushUndoState()
                insertBlockAtCursor(TextBlock("ai_${UUID.randomUUID()}", TextFieldValue(event.newText)))
            }
            is RibbonEvent.OnFormatPicture -> {
                pushUndoState()
                val activeIdx = _state.value.blocks.indexOfFirst { it.id == event.blockId }
                if (activeIdx != -1 && _state.value.blocks[activeIdx] is ImageBlock) {
                    val img = _state.value.blocks[activeIdx] as ImageBlock
                    val updated = img.copy(wrapMode = event.wrapMode)
                    val newBlocks = _state.value.blocks.toMutableList()
                    newBlocks[activeIdx] = updated
                    _state.update { it.copy(blocks = newBlocks) }
                }
            }
            is RibbonEvent.OnFormatShape -> {
                pushUndoState()
                val activeIdx = _state.value.blocks.indexOfFirst { it.id == event.blockId }
                if (activeIdx != -1 && _state.value.blocks[activeIdx] is ShapeBlock) {
                    val shp = _state.value.blocks[activeIdx] as ShapeBlock
                    val updated = shp.copy(fillColor = event.fillColor, strokeColor = event.strokeColor)
                    val newBlocks = _state.value.blocks.toMutableList()
                    newBlocks[activeIdx] = updated
                    _state.update { it.copy(blocks = newBlocks) }
                }
            }
        }
        } catch (e: Exception) {
            android.util.Log.e("EditorViewModel", "Safe event processing caught error", e)
            _state.update { it.copy(userErrorMessage = e.localizedMessage ?: "حدث خطأ غير متوقع أثناء معالجة العملية") }
        }
    }

    private fun changeTextCase(caseType: String) {
        val stateVal = _state.value
        val activeIdx = stateVal.blocks.indexOfFirst { it.id == stateVal.activeBlockId }
        if (activeIdx != -1 && stateVal.blocks[activeIdx] is TextBlock) {
            val tb = stateVal.blocks[activeIdx] as TextBlock
            val original = tb.text.text
            val newTextStr = when (caseType) {
                "UPPERCASE" -> original.uppercase()
                "lowercase" -> original.lowercase()
                "Capitalize Each Word" -> original.split(" ").joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
                "Sentence case" -> original.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                else -> original
            }
            val updated = tb.copy(text = tb.text.copy(text = newTextStr))
            val newBlocks = stateVal.blocks.toMutableList()
            newBlocks[activeIdx] = updated
            _state.update { it.copy(blocks = newBlocks) }
        }
    }

    private fun applyHeadingStyle(styleName: String) {
        val stateVal = _state.value
        val activeIdx = stateVal.blocks.indexOfFirst { it.id == stateVal.activeBlockId }
        if (activeIdx != -1 && stateVal.blocks[activeIdx] is TextBlock) {
            val tb = stateVal.blocks[activeIdx] as TextBlock
            val size = when (styleName) {
                "Title" -> 24
                "Heading 1" -> 20
                "Heading 2" -> 16
                "Heading 3", "Subtitle", "Intense Quote" -> 14
                "Footnote" -> 10
                else -> 12
            }
            val isBold = when (styleName) {
                "Title", "Heading 1", "Heading 2", "Heading 3", "Intense Emphasis", "Intense Quote", "Book Title" -> true
                else -> false
            }
            val isItalic = when (styleName) {
                "Subtitle", "Emphasis", "Intense Emphasis", "Quote", "Intense Quote", "Book Title" -> true
                else -> false
            }
            val color = when (styleName) {
                "Title" -> Color(0xFF1E3A8A)
                "Heading 1", "Emphasis" -> Color(0xFF2563EB)
                "Heading 2", "Intense Emphasis", "Intense Quote" -> Color(0xFF1D4ED8)
                "Subtitle", "Quote", "Footnote" -> Color(0xFF475569)
                "Heading 3" -> Color(0xFF374151)
                else -> Color.Black
            }
            val updatedBlock = tb.copy(
                fontSize = size,
                isBold = isBold,
                isItalic = isItalic,
                textColor = color,
                spaceBeforePt = if (styleName == "Title" || styleName.startsWith("Heading")) 8f else 2f,
                spaceAfterPt = if (styleName == "Title" || styleName.startsWith("Heading")) 4f else 2f
            )
            val newBlocks = stateVal.blocks.toMutableList()
            newBlocks[activeIdx] = updatedBlock
            _state.update { 
                it.copy(
                    blocks = newBlocks,
                    fontSize = size, 
                    isBold = isBold, 
                    isItalic = isItalic, 
                    textColor = color
                ) 
            }
            applyStyleToSelection(
                SpanStyle(
                    fontSize = size.sp,
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                    color = color
                )
            )
        }
    }

    private fun performFindAndReplace(findText: String, replaceText: String) {
        if (findText.isEmpty()) return
        val stateVal = _state.value
        val newBlocks = stateVal.blocks.map { block ->
            if (block is TextBlock) {
                var currentIndex = 0
                val currentText = block.text.annotatedString.text
                val lowerCurrent = currentText.lowercase()
                val lowerFind = findText.lowercase()
                
                val builder = androidx.compose.ui.text.AnnotatedString.Builder()
                var matchIndex = lowerCurrent.indexOf(lowerFind, currentIndex)
                
                while (matchIndex != -1) {
                    builder.append(block.text.annotatedString.subSequence(currentIndex, matchIndex))
                    builder.append(replaceText) // We lose the exact spans specifically for the replaced text length, but we keep the rest of the paragraph's spans intact
                    currentIndex = matchIndex + findText.length
                    matchIndex = lowerCurrent.indexOf(lowerFind, currentIndex)
                }
                
                if (currentIndex < currentText.length) {
                    builder.append(block.text.annotatedString.subSequence(currentIndex, currentText.length))
                }
                
                block.copy(text = block.text.copy(annotatedString = builder.toAnnotatedString()))
            } else block
        }
        _state.update { it.copy(blocks = newBlocks, showFindReplaceDialog = false) }
    }

    fun exportPdf(context: Context) {
        _state.update { it.copy(isProtectedView = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val uri = PdfExporter().exportToPdf(context, _state.value)
            withContext(Dispatchers.Main) {
                _state.update {
                    it.copy(
                        exportedPdfUri = uri,
                        showExportPdfSuccessDialog = uri != null
                    )
                }
            }
        }
    }

    private fun insertBlockAtCursor(newBlock: DocumentBlock) {
        val stateVal = _state.value
        val activeIdx = stateVal.blocks.indexOfFirst { it.id == stateVal.activeBlockId }
        
        if (activeIdx == -1) {
            val newBlocks = stateVal.blocks.toMutableList()
            newBlocks.add(newBlock)
            val nextTextBlockId = "blk_${UUID.randomUUID()}"
            newBlocks.add(TextBlock(nextTextBlockId, TextFieldValue("")))
            _state.update { it.copy(blocks = newBlocks, activeBlockId = nextTextBlockId) }
            return
        }

        val activeBlock = stateVal.blocks[activeIdx]
        if (activeBlock is TextBlock) {
            val textVal = activeBlock.text
            val min = textVal.selection.min.coerceIn(0, textVal.annotatedString.length)
            val max = textVal.selection.max.coerceIn(0, textVal.annotatedString.length)
            
            val textBefore = textVal.copy(
                annotatedString = textVal.annotatedString.subSequence(0, min),
                selection = TextRange(min)
            )
            val textAfter = TextFieldValue(
                annotatedString = textVal.annotatedString.subSequence(max, textVal.annotatedString.length)
            )
            
            val newBlocks = stateVal.blocks.toMutableList()
            newBlocks[activeIdx] = activeBlock.copy(text = textBefore)
            newBlocks.add(activeIdx + 1, newBlock)
            
            val nextTextBlockId = "blk_${UUID.randomUUID()}"
            newBlocks.add(activeIdx + 2, TextBlock(nextTextBlockId, textAfter))
            
            _state.update { it.copy(blocks = newBlocks, activeBlockId = nextTextBlockId) }
        } else {
            val newBlocks = stateVal.blocks.toMutableList()
            newBlocks.add(activeIdx + 1, newBlock)
            val nextTextBlockId = "blk_${UUID.randomUUID()}"
            newBlocks.add(activeIdx + 2, TextBlock(nextTextBlockId, TextFieldValue("")))
            _state.update { it.copy(blocks = newBlocks, activeBlockId = nextTextBlockId) }
        }
    }

    private fun applyParagraphStyleToSelection(paragraphStyle: ParagraphStyle) {
        try {
            val stateVal = _state.value
            val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock ?: return
            val currentText = activeBlock.text
            val annotated = currentText.annotatedString
            val totalLength = annotated.length
            if (totalLength == 0) return

            val min = if (currentText.selection.collapsed) 0 else currentText.selection.min.coerceIn(0, totalLength)
            val max = if (currentText.selection.collapsed) totalLength else currentText.selection.max.coerceIn(0, totalLength)
            if (min >= max) return

            fun buildSafeAnnotated(filterOverlaps: Boolean): AnnotatedString {
                val builder = AnnotatedString.Builder()
                builder.append(annotated.text)

                // Preserve span styles (characters/fonts/colors)
                annotated.spanStyles.forEach { range ->
                    val start = range.start.coerceIn(0, totalLength)
                    val end = range.end.coerceIn(0, totalLength)
                    if (start < end) {
                        builder.addStyle(range.item, start, end)
                    }
                }

                // Copy non-overlapping existing paragraph styles if normalizing
                annotated.paragraphStyles.forEach { range ->
                    val pStart = range.start.coerceIn(0, totalLength)
                    val pEnd = range.end.coerceIn(0, totalLength)
                    if (pStart < pEnd) {
                        val overlaps = (pStart < max && pEnd > min)
                        if (!filterOverlaps || !overlaps) {
                            builder.addStyle(range.item, pStart, pEnd)
                        }
                    }
                }

                // Apply new paragraph style safely
                builder.addStyle(paragraphStyle, min, max)
                return builder.toAnnotatedString()
            }

            val newAnnotatedString = try {
                buildSafeAnnotated(filterOverlaps = true)
            } catch (e: IllegalArgumentException) {
                // Fallback normalization: rebuild strictly without conflicting paragraph ranges
                val fallbackBuilder = AnnotatedString.Builder()
                fallbackBuilder.append(annotated.text)
                annotated.spanStyles.forEach { range ->
                    fallbackBuilder.addStyle(range.item, range.start.coerceIn(0, totalLength), range.end.coerceIn(0, totalLength))
                }
                fallbackBuilder.addStyle(paragraphStyle, 0, totalLength)
                fallbackBuilder.toAnnotatedString()
            }

            val newBlocks = stateVal.blocks.map {
                if (it.id == activeBlock.id) activeBlock.copy(text = currentText.copy(annotatedString = newAnnotatedString)) else it
            }
            _state.update { it.copy(blocks = newBlocks) }
        } catch (e: Exception) {
            android.util.Log.e("EditorViewModel", "applyParagraphStyleToSelection caught error", e)
        }
    }

    private fun applyStyleToSelection(spanStyle: SpanStyle) {
        try {
            val stateVal = _state.value
            val selectedBlocks = stateVal.blocks.filterIsInstance<TextBlock>().filter {
                it.text.selection.min < it.text.selection.max
            }

            val targetBlockIds = if (selectedBlocks.isNotEmpty()) {
                selectedBlocks.map { it.id }.toSet()
            } else {
                val active = stateVal.blocks.find { it.id == stateVal.activeBlockId }
                if (active != null) setOf(active.id) else emptySet()
            }

            if (targetBlockIds.isEmpty()) return

            val newBlocks = stateVal.blocks.map { block ->
                if (block is TextBlock && block.id in targetBlockIds) {
                    applyStyleToBlock(block, spanStyle)
                } else block
            }
            _state.update { it.copy(blocks = newBlocks) }
        } catch (e: Exception) {
            android.util.Log.e("EditorViewModel", "applyStyleToSelection caught error", e)
        }
    }

    private fun applyStyleToBlock(activeBlock: TextBlock, spanStyle: SpanStyle): TextBlock {
        val currentText = activeBlock.text
        val textStr = currentText.annotatedString.text
        val totalLength = textStr.length

        if (totalLength == 0) {
            return updateBlockBaseStyle(activeBlock, spanStyle)
        }

        val min = currentText.selection.min.coerceIn(0, totalLength)
        val max = currentText.selection.max.coerceIn(0, totalLength)

        val (targetStart, targetEnd) = if (min < max) {
            min to max
        } else {
            val cursor = min.coerceIn(0, totalLength)
            var wStart = cursor
            while (wStart > 0 && !textStr[wStart - 1].isWhitespace()) {
                wStart--
            }
            var wEnd = cursor
            while (wEnd < totalLength && !textStr[wEnd].isWhitespace()) {
                wEnd++
            }
            if (wStart < wEnd) wStart to wEnd else cursor to cursor
        }

        if (targetStart >= targetEnd) return activeBlock

        val charStyles = Array(totalLength) { SpanStyle() }

        currentText.annotatedString.spanStyles.forEach { range ->
            val s = range.start.coerceIn(0, totalLength)
            val e = range.end.coerceIn(0, totalLength)
            for (i in s until e) {
                charStyles[i] = mergeSpanStyles(charStyles[i], range.item)
            }
        }

        for (i in targetStart until targetEnd) {
            charStyles[i] = mergeSpanStyles(charStyles[i], spanStyle)
        }

        val builder = AnnotatedString.Builder()
        builder.append(textStr)

        if (totalLength > 0) {
            var spanStart = 0
            var currentStyle = charStyles[0]

            for (i in 1 until totalLength) {
                if (charStyles[i] != currentStyle) {
                    if (!isEmptySpan(currentStyle)) {
                        builder.addStyle(currentStyle, spanStart, i)
                    }
                    spanStart = i
                    currentStyle = charStyles[i]
                }
            }
            if (!isEmptySpan(currentStyle)) {
                builder.addStyle(currentStyle, spanStart, totalLength)
            }
        }

        currentText.annotatedString.paragraphStyles.forEach { pRange ->
            val ps = pRange.start.coerceIn(0, totalLength)
            val pe = pRange.end.coerceIn(0, totalLength)
            if (ps < pe) {
                builder.addStyle(pRange.item, ps, pe)
            }
        }

        val newAnnotatedString = builder.toAnnotatedString()
        val updatedBase = if (targetStart == 0 && targetEnd == totalLength) {
            updateBlockBaseStyle(activeBlock, spanStyle)
        } else {
            activeBlock
        }

        return updatedBase.copy(text = currentText.copy(annotatedString = newAnnotatedString))
    }

    private fun updateBlockBaseStyle(block: TextBlock, style: SpanStyle): TextBlock {
        var updated = block
        if (style.fontWeight != null) {
            updated = updated.copy(isBold = style.fontWeight == FontWeight.Bold || style.fontWeight == FontWeight.ExtraBold || style.fontWeight == FontWeight.Black)
        }
        if (style.fontStyle != null) {
            updated = updated.copy(isItalic = style.fontStyle == FontStyle.Italic)
        }
        if (style.textDecoration != null) {
            val isUnder = style.textDecoration == TextDecoration.Underline || style.textDecoration?.contains(TextDecoration.Underline) == true
            val isStrike = style.textDecoration == TextDecoration.LineThrough || style.textDecoration?.contains(TextDecoration.LineThrough) == true
            updated = updated.copy(isUnderline = isUnder, isStrikethrough = isStrike)
        }
        if (style.color != Color.Unspecified) {
            updated = updated.copy(textColor = style.color)
        }
        if (style.background != Color.Unspecified) {
            updated = updated.copy(highlightColor = if (style.background == Color.Transparent) Color.Transparent else style.background)
        }
        if (style.fontSize != androidx.compose.ui.unit.TextUnit.Unspecified) {
            updated = updated.copy(fontSize = style.fontSize.value.toInt())
        }
        return updated
    }

    private fun isEmptySpan(style: SpanStyle): Boolean {
        return style == SpanStyle() || (
            style.color == Color.Unspecified &&
            style.fontSize == androidx.compose.ui.unit.TextUnit.Unspecified &&
            style.fontWeight == null &&
            style.fontStyle == null &&
            style.fontFamily == null &&
            style.background == Color.Unspecified &&
            style.textDecoration == null &&
            style.baselineShift == null &&
            style.shadow == null
        )
    }

    private fun isRangeBold(annotatedString: AnnotatedString, start: Int, end: Int, defaultBold: Boolean): Boolean {
        val s = start.coerceIn(0, annotatedString.length)
        val e = end.coerceIn(0, annotatedString.length)
        if (s >= e) return defaultBold
        
        for (range in annotatedString.spanStyles) {
            if (range.start < e && range.end > s) {
                if (range.item.fontWeight == FontWeight.Bold || range.item.fontWeight == FontWeight.ExtraBold || range.item.fontWeight == FontWeight.Black) {
                    return true
                }
                if (range.item.fontWeight == FontWeight.Normal) {
                    return false
                }
            }
        }
        return defaultBold
    }

    private fun isRangeItalic(annotatedString: AnnotatedString, start: Int, end: Int, defaultItalic: Boolean): Boolean {
        val s = start.coerceIn(0, annotatedString.length)
        val e = end.coerceIn(0, annotatedString.length)
        if (s >= e) return defaultItalic
        for (range in annotatedString.spanStyles) {
            if (range.start < e && range.end > s) {
                if (range.item.fontStyle == FontStyle.Italic) return true
                if (range.item.fontStyle == FontStyle.Normal) return false
            }
        }
        return defaultItalic
    }

    private fun isRangeUnderline(annotatedString: AnnotatedString, start: Int, end: Int, defaultUnderline: Boolean): Boolean {
        val s = start.coerceIn(0, annotatedString.length)
        val e = end.coerceIn(0, annotatedString.length)
        if (s >= e) return defaultUnderline
        for (range in annotatedString.spanStyles) {
            if (range.start < e && range.end > s) {
                if (range.item.textDecoration == TextDecoration.Underline || range.item.textDecoration?.contains(TextDecoration.Underline) == true) return true
                if (range.item.textDecoration == TextDecoration.None) return false
            }
        }
        return defaultUnderline
    }

    private fun isRangeStrikethrough(annotatedString: AnnotatedString, start: Int, end: Int, defaultStrike: Boolean): Boolean {
        val s = start.coerceIn(0, annotatedString.length)
        val e = end.coerceIn(0, annotatedString.length)
        if (s >= e) return defaultStrike
        for (range in annotatedString.spanStyles) {
            if (range.start < e && range.end > s) {
                if (range.item.textDecoration == TextDecoration.LineThrough || range.item.textDecoration?.contains(TextDecoration.LineThrough) == true) return true
                if (range.item.textDecoration == TextDecoration.None) return false
            }
        }
        return defaultStrike
    }

    private fun getMergedSpanStyleAt(annotatedString: AnnotatedString, index: Int): SpanStyle {
        var merged = SpanStyle()
        val len = annotatedString.length
        if (len > 0 && index in 0 until len) {
            annotatedString.spanStyles.forEach { range ->
                if (range.start <= index && range.end > index) {
                    merged = mergeSpanStyles(merged, range.item)
                }
            }
        }
        return merged
    }

    private fun adjustSpanStylesForTextChange(
        oldSpans: List<AnnotatedString.Range<SpanStyle>>,
        oldStr: String,
        newStr: String,
        selection: TextRange
    ): List<AnnotatedString.Range<SpanStyle>> {
        if (oldStr == newStr) {
            return oldSpans
        }
        if (oldStr.isEmpty() || newStr.isEmpty()) {
            return emptyList()
        }

        var p = 0
        while (p < oldStr.length && p < newStr.length && oldStr[p] == newStr[p]) {
            p++
        }

        var s = 0
        while (s < (oldStr.length - p) && s < (newStr.length - p) &&
            oldStr[oldStr.length - 1 - s] == newStr[newStr.length - 1 - s]) {
            s++
        }

        val deletedLen = oldStr.length - p - s
        val insertedLen = newStr.length - p - s
        val editStart = p
        val editEnd = p + deletedLen

        val result = mutableListOf<AnnotatedString.Range<SpanStyle>>()

        for (range in oldSpans) {
            val start = range.start
            val end = range.end

            if (end < editStart) {
                if (start < end) {
                    result.add(AnnotatedString.Range(range.item, start, end))
                }
            } else if (start > editEnd) {
                val newStart = start - deletedLen + insertedLen
                val newEnd = end - deletedLen + insertedLen
                if (newStart < newEnd && newStart >= 0 && newEnd <= newStr.length) {
                    result.add(AnnotatedString.Range(range.item, newStart, newEnd))
                }
            } else {
                val newStart = if (start > editStart) {
                    (start - deletedLen + insertedLen).coerceAtLeast(editStart)
                } else {
                    start
                }
                val newEnd = if (end >= editStart) {
                    (end - deletedLen + insertedLen).coerceAtLeast(newStart)
                } else {
                    end
                }

                if (newStart < newEnd && newEnd <= newStr.length) {
                    result.add(AnnotatedString.Range(range.item, newStart, newEnd))
                }
            }
        }
        return result
    }

    private fun mergeSpanStyles(base: SpanStyle, override: SpanStyle): SpanStyle {
        val newBackground = when {
            override.background == Color.Transparent -> Color.Transparent
            override.background != Color.Unspecified -> override.background
            else -> base.background
        }

        val newDecoration = when {
            override.textDecoration == TextDecoration.None -> null
            override.textDecoration != null -> override.textDecoration
            else -> base.textDecoration
        }

        return SpanStyle(
            color = if (override.color != Color.Unspecified) override.color else base.color,
            fontSize = if (override.fontSize != androidx.compose.ui.unit.TextUnit.Unspecified) override.fontSize else base.fontSize,
            fontWeight = override.fontWeight ?: base.fontWeight,
            fontStyle = override.fontStyle ?: base.fontStyle,
            fontSynthesis = override.fontSynthesis ?: base.fontSynthesis,
            fontFamily = override.fontFamily ?: base.fontFamily,
            fontFeatureSettings = override.fontFeatureSettings ?: base.fontFeatureSettings,
            letterSpacing = if (override.letterSpacing != androidx.compose.ui.unit.TextUnit.Unspecified) override.letterSpacing else base.letterSpacing,
            baselineShift = override.baselineShift ?: base.baselineShift,
            textGeometricTransform = override.textGeometricTransform ?: base.textGeometricTransform,
            localeList = override.localeList ?: base.localeList,
            background = newBackground,
            textDecoration = newDecoration,
            shadow = override.shadow ?: base.shadow
        )
    }

    private fun updateActiveBlock(update: (DocumentBlock) -> DocumentBlock) {
        val stateVal = _state.value
        val selectedBlockIds = stateVal.blocks.filterIsInstance<TextBlock>()
            .filter { it.text.selection.min < it.text.selection.max }
            .map { it.id }.toSet()

        val targetIds = if (selectedBlockIds.isNotEmpty()) selectedBlockIds else setOf(stateVal.activeBlockId)

        val newBlocks = stateVal.blocks.map { block ->
            if (block.id in targetIds) {
                if (block is TableBlock && stateVal.activeTableCellId != null) {
                    val cellKey = stateVal.activeTableCellId
                    val cell = block.cells[cellKey]
                    if (cell != null) {
                        val updatedCellTextBlocks = cell.textBlocks.map { tb ->
                            val updated = update(tb)
                            if (updated is TextBlock) updated else tb
                        }
                        val sampleTb = cell.textBlocks.firstOrNull() ?: TextBlock("dummy", TextFieldValue(""))
                        val updatedSample = update(sampleTb)
                        val newCellAlign = if (updatedSample is TextBlock) updatedSample.alignment else cell.alignment
                        val newCellRtl = if (updatedSample is TextBlock) updatedSample.isRtl else cell.isRtl
                        val newCell = cell.copy(
                            textBlocks = updatedCellTextBlocks,
                            alignment = newCellAlign,
                            isRtl = newCellRtl
                        )
                        val updatedCells = block.cells.toMutableMap()
                        updatedCells[cellKey] = newCell
                        block.copy(cells = updatedCells)
                    } else {
                        update(block)
                    }
                } else {
                    update(block)
                }
            } else block
        }
        _state.update { it.copy(blocks = newBlocks) }
    }

    private fun clearFormattingFromSelection() {
        val stateVal = _state.value
        val selectedBlocks = stateVal.blocks.filterIsInstance<TextBlock>().filter {
            it.text.selection.min < it.text.selection.max
        }
        val targetIds = if (selectedBlocks.isNotEmpty()) {
            selectedBlocks.map { it.id }.toSet()
        } else {
            val active = stateVal.blocks.find { it.id == stateVal.activeBlockId }
            if (active != null) setOf(active.id) else emptySet()
        }

        if (targetIds.isEmpty()) return

        val newBlocks = stateVal.blocks.map { block ->
            if (block is TextBlock && block.id in targetIds) {
                clearFormattingFromSingleBlock(block)
            } else block
        }
        _state.update {
            it.copy(
                blocks = newBlocks,
                isBold = false,
                isItalic = false,
                isUnderline = false,
                isStrikethrough = false,
                textColor = Color(0xFF0F172A),
                highlightColor = Color.Transparent,
                fontSize = 12,
                fontFamily = "Calibri"
            )
        }
    }

    private fun clearFormattingFromSingleBlock(activeBlock: TextBlock): TextBlock {
        val currentText = activeBlock.text
        val totalLength = currentText.annotatedString.length
        if (totalLength == 0) return activeBlock

        val min = currentText.selection.min.coerceIn(0, totalLength)
        val max = currentText.selection.max.coerceIn(0, totalLength)

        val newAnnotatedString = if (min < max) {
            val builder = AnnotatedString.Builder()
            builder.append(currentText.annotatedString.text)
            currentText.annotatedString.spanStyles.forEach { range ->
                val s = range.start.coerceIn(0, totalLength)
                val e = range.end.coerceIn(0, totalLength)
                if (s < e) {
                    if (e <= min || s >= max) {
                        builder.addStyle(range.item, s, e)
                    } else {
                        if (s < min) builder.addStyle(range.item, s, min)
                        if (e > max) builder.addStyle(range.item, max, e)
                    }
                }
            }
            builder.toAnnotatedString()
        } else {
            buildAnnotatedString {
                append(currentText.annotatedString.text)
            }
        }

        return if (min == max || (min == 0 && max == totalLength)) {
            activeBlock.copy(
                text = currentText.copy(annotatedString = newAnnotatedString),
                isBold = false,
                isItalic = false,
                isUnderline = false,
                isStrikethrough = false,
                textColor = Color(0xFF0F172A),
                highlightColor = Color.Transparent,
                fontSize = 12,
                fontFamily = "Calibri"
            )
        } else {
            activeBlock.copy(text = currentText.copy(annotatedString = newAnnotatedString))
        }
    }

    private fun applyNumeralSystem(system: NumeralSystem) {
        val stateVal = _state.value
        val selectedBlockIds = stateVal.blocks.filterIsInstance<TextBlock>()
            .filter { it.text.selection.min < it.text.selection.max }
            .map { it.id }.toSet()

        val targetIds = if (selectedBlockIds.isNotEmpty()) selectedBlockIds else setOf(stateVal.activeBlockId)

        val newBlocks = stateVal.blocks.map { block ->
            if (block is TextBlock && block.id in targetIds) {
                val currentText = block.text
                val originalText = currentText.annotatedString.text
                val transformed = if (system == NumeralSystem.ARABIC) {
                    originalText.map { c ->
                        when (c) {
                            '0' -> '٠'; '1' -> '١'; '2' -> '٢'; '3' -> '٣'; '4' -> '٤'
                            '5' -> '٥'; '6' -> '٦'; '7' -> '٧'; '8' -> '٨'; '9' -> '٩'
                            else -> c
                        }
                    }.joinToString("")
                } else {
                    originalText.map { c ->
                        when (c) {
                            '٠' -> '0'; '١' -> '1'; '٢' -> '2'; '٣' -> '3'; '٤' -> '4'
                            '٥' -> '5'; '٦' -> '6'; '٧' -> '7'; '٨' -> '8'; '٩' -> '9'
                            else -> c
                        }
                    }.joinToString("")
                }

                val newAnnotatedString = buildAnnotatedString {
                    append(transformed)
                    currentText.annotatedString.spanStyles.forEach { span ->
                        val start = span.start.coerceIn(0, transformed.length)
                        val end = span.end.coerceIn(0, transformed.length)
                        if (start < end) addStyle(span.item, start, end)
                    }
                }
                block.copy(text = currentText.copy(annotatedString = newAnnotatedString))
            } else block
        }
        _state.update { it.copy(numeralSystem = system, blocks = newBlocks) }
    }

    private fun sortParagraphs(ascending: Boolean) {
        val stateVal = _state.value
        val textBlocks = stateVal.blocks.filterIsInstance<TextBlock>()
        if (textBlocks.size <= 1) return

        val sortedTextBlocks = if (ascending) {
            textBlocks.sortedBy { it.text.text }
        } else {
            textBlocks.sortedByDescending { it.text.text }
        }

        var sortedIdx = 0
        val newBlocks = stateVal.blocks.map { block ->
            if (block is TextBlock && sortedIdx < sortedTextBlocks.size) {
                val nextSorted = sortedTextBlocks[sortedIdx++]
                block.copy(text = nextSorted.text, alignment = nextSorted.alignment, lineSpacing = nextSorted.lineSpacing)
            } else block
        }
        _state.update { it.copy(blocks = newBlocks) }
    }

    private fun handleAddTableRow(blockId: String, atIndex: Int, above: Boolean) {
        pushUndoState()
        _state.update { state ->
            val newBlocks = state.blocks.map { block ->
                if (block is TableBlock && block.id == blockId) {
                    val currentRows = block.rows
                    val cols = block.cols
                    val insertAt = if (atIndex in 0 until currentRows) {
                        if (above) atIndex else atIndex + 1
                    } else currentRows

                    val newCells = mutableMapOf<String, TableCellModel>()
                    for (r in 0 until currentRows) {
                        val targetR = if (r >= insertAt) r + 1 else r
                        for (c in 0 until cols) {
                            block.cells["${r}_${c}"]?.let { cell ->
                                newCells["${targetR}_${c}"] = cell
                            }
                        }
                    }
                    for (c in 0 until cols) {
                        newCells["${insertAt}_${c}"] = TableCellModel(
                            textBlocks = listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue("")))
                        )
                    }
                    block.copy(rows = currentRows + 1, cells = newCells)
                } else block
            }
            state.copy(blocks = newBlocks)
        }
    }

    private fun handleDeleteTableRow(blockId: String, rowIndex: Int) {
        pushUndoState()
        _state.update { state ->
            val newBlocks = state.blocks.mapNotNull { block ->
                if (block is TableBlock && block.id == blockId) {
                    if (block.rows <= 1) {
                        null
                    } else {
                        val targetRow = if (rowIndex in 0 until block.rows) rowIndex else block.rows - 1
                        val newCells = mutableMapOf<String, TableCellModel>()
                        var newR = 0
                        for (r in 0 until block.rows) {
                            if (r == targetRow) continue
                            for (c in 0 until block.cols) {
                                block.cells["${r}_${c}"]?.let { cell ->
                                    newCells["${newR}_${c}"] = cell
                                }
                            }
                            newR++
                        }
                        block.copy(rows = block.rows - 1, cells = newCells)
                    }
                } else block
            }
            state.copy(blocks = newBlocks)
        }
    }

    private fun handleAddTableColumn(blockId: String, atIndex: Int, left: Boolean) {
        pushUndoState()
        _state.update { state ->
            val newBlocks = state.blocks.map { block ->
                if (block is TableBlock && block.id == blockId) {
                    val rows = block.rows
                    val currentCols = block.cols
                    val insertAt = if (atIndex in 0 until currentCols) {
                        if (left) atIndex else atIndex + 1
                    } else currentCols

                    val newCells = mutableMapOf<String, TableCellModel>()
                    for (r in 0 until rows) {
                        for (c in 0 until currentCols) {
                            val targetC = if (c >= insertAt) c + 1 else c
                            block.cells["${r}_${c}"]?.let { cell ->
                                newCells["${r}_${targetC}"] = cell
                            }
                        }
                    }
                    for (r in 0 until rows) {
                        newCells["${r}_${insertAt}"] = TableCellModel(
                            textBlocks = listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue("")))
                        )
                    }
                    val newCols = currentCols + 1
                    val equalRatio = 1f / newCols
                    val newRatios = List(newCols) { equalRatio }
                    block.copy(cols = newCols, cells = newCells, colWidthRatios = newRatios)
                } else block
            }
            state.copy(blocks = newBlocks)
        }
    }

    private fun handleDeleteTableColumn(blockId: String, colIndex: Int) {
        pushUndoState()
        _state.update { state ->
            val newBlocks = state.blocks.mapNotNull { block ->
                if (block is TableBlock && block.id == blockId) {
                    if (block.cols <= 1) {
                        null
                    } else {
                        val targetCol = if (colIndex in 0 until block.cols) colIndex else block.cols - 1
                        val newCells = mutableMapOf<String, TableCellModel>()
                        for (r in 0 until block.rows) {
                            var newC = 0
                            for (c in 0 until block.cols) {
                                if (c == targetCol) continue
                                block.cells["${r}_${c}"]?.let { cell ->
                                    newCells["${r}_${newC}"] = cell
                                }
                                newC++
                            }
                        }
                        val newCols = block.cols - 1
                        val equalRatio = 1f / newCols
                        val newRatios = List(newCols) { equalRatio }
                        block.copy(cols = newCols, cells = newCells, colWidthRatios = newRatios)
                    }
                } else block
            }
            state.copy(blocks = newBlocks)
        }
    }

    private fun handleSetTableStyle(blockId: String, preset: TableStylePreset) {
        pushUndoState()
        _state.update { state ->
            val newBlocks = state.blocks.map { block ->
                if (block is TableBlock && block.id == blockId) {
                    when (preset) {
                        TableStylePreset.GRID -> block.copy(
                            tableStyle = preset,
                            headerBackgroundColor = Color(0xFFF1F5F9),
                            headerTextColor = Color(0xFF0F172A),
                            borderColor = Color(0xFFCBD5E1),
                            alternatingRowColor = Color(0xFFF8FAFC)
                        )
                        TableStylePreset.PLAIN -> block.copy(
                            tableStyle = preset,
                            headerBackgroundColor = Color.Transparent,
                            headerTextColor = Color(0xFF0F172A),
                            borderColor = Color(0xFFE2E8F0),
                            alternatingRowColor = null
                        )
                        TableStylePreset.BLUE_HEADER -> block.copy(
                            tableStyle = preset,
                            headerBackgroundColor = Color(0xFF185ABD),
                            headerTextColor = Color.White,
                            borderColor = Color(0xFF93C5FD),
                            alternatingRowColor = Color(0xFFEFF6FF)
                        )
                        TableStylePreset.DARK_MODERN -> block.copy(
                            tableStyle = preset,
                            headerBackgroundColor = Color(0xFF1E293B),
                            headerTextColor = Color.White,
                            borderColor = Color(0xFF475569),
                            alternatingRowColor = Color(0xFFF1F5F9)
                        )
                        TableStylePreset.MINIMAL_LINES -> block.copy(
                            tableStyle = preset,
                            headerBackgroundColor = Color.Transparent,
                            headerTextColor = Color(0xFF1E293B),
                            borderColor = Color(0xFFCBD5E1),
                            alternatingRowColor = null
                        )
                        TableStylePreset.COLORFUL_ACCENT -> block.copy(
                            tableStyle = preset,
                            headerBackgroundColor = Color(0xFF7C3AED),
                            headerTextColor = Color.White,
                            borderColor = Color(0xFFDDD6FE),
                            alternatingRowColor = Color(0xFFF5F3FF)
                        )
                        TableStylePreset.WARM_ORANGE -> block.copy(
                            tableStyle = preset,
                            headerBackgroundColor = Color(0xFFD97706),
                            headerTextColor = Color.White,
                            borderColor = Color(0xFFFDE68A),
                            alternatingRowColor = Color(0xFFFFFBEB)
                        )
                        TableStylePreset.EMERALD_GREEN -> block.copy(
                            tableStyle = preset,
                            headerBackgroundColor = Color(0xFF059669),
                            headerTextColor = Color.White,
                            borderColor = Color(0xFFA7F3D0),
                            alternatingRowColor = Color(0xFFECFDF5)
                        )
                    }
                } else block
            }
            state.copy(blocks = newBlocks)
        }
    }

    private fun handleMergeTableCells(blockId: String) {
        pushUndoState()
        _state.update { state ->
            val activeCell = state.activeTableCellId ?: "0_0"
            val parts = activeCell.split("_")
            val r = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val c = parts.getOrNull(1)?.toIntOrNull() ?: 0

            val newBlocks = state.blocks.map { block ->
                if (block is TableBlock && block.id == blockId) {
                    val currentCells = block.cells.toMutableMap()
                    val targetCell = currentCells["${r}_${c}"] ?: TableCellModel(
                        textBlocks = listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue("")))
                    )
                    
                    val combinedTextList = mutableListOf<TextBlock>()
                    val nextC = c + 1
                    val targetCols = if (nextC < block.cols) 2 else 1
                    
                    for (colOffset in 0 until targetCols) {
                        val cellKey = "${r}_${c + colOffset}"
                        currentCells[cellKey]?.let { cell ->
                            combinedTextList.addAll(cell.textBlocks)
                        }
                        if (colOffset > 0) {
                            currentCells[cellKey] = (currentCells[cellKey] ?: TableCellModel(emptyList())).copy(isMergedCovered = true)
                        }
                    }
                    currentCells["${r}_${c}"] = targetCell.copy(
                        textBlocks = if (combinedTextList.isNotEmpty()) combinedTextList else targetCell.textBlocks,
                        colSpan = targetCols,
                        isMergedCovered = false
                    )
                    block.copy(cells = currentCells)
                } else block
            }
            state.copy(blocks = newBlocks)
        }
    }

    private fun handleSplitTableCell(blockId: String, cellId: String) {
        pushUndoState()
        _state.update { state ->
            val newBlocks = state.blocks.map { block ->
                if (block is TableBlock && block.id == blockId) {
                    val currentCells = block.cells.toMutableMap()
                    val targetCell = currentCells[cellId]
                    if (targetCell != null && targetCell.colSpan > 1) {
                        val parts = cellId.split("_")
                        val r = parts.getOrNull(0)?.toIntOrNull() ?: 0
                        val c = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        
                        for (colOffset in 0 until targetCell.colSpan) {
                            val key = "${r}_${c + colOffset}"
                            val existing = currentCells[key] ?: TableCellModel(emptyList())
                            currentCells[key] = existing.copy(
                                colSpan = 1,
                                rowSpan = 1,
                                isMergedCovered = false
                            )
                        }
                        block.copy(cells = currentCells)
                    } else block
                } else block
            }
            state.copy(blocks = newBlocks)
        }
    }
}
