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
            isProtectedView = false,
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
                _state.update { it.copy(isProtectedView = false) }
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
                    val newBlocks = state.blocks.map { 
                        if (it.id == event.blockId && it is TextBlock) it.copy(text = event.text) else it 
                    }
                    state.copy(blocks = newBlocks, activeBlockId = event.blockId)
                }
            }
            is RibbonEvent.OnBlockFocusChanged -> {
                _state.update { state ->
                    val block = state.blocks.find { it.id == event.blockId } as? TextBlock
                    if (block != null) {
                        state.copy(
                            activeBlockId = event.blockId,
                            fontSize = block.fontSize,
                            fontFamily = block.fontFamily,
                            isBold = block.isBold,
                            isItalic = block.isItalic,
                            isUnderline = block.isUnderline,
                            isStrikethrough = block.isStrikethrough,
                            textColor = block.textColor,
                            highlightColor = block.highlightColor,
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
                val cells = mutableMapOf<String, TableCellModel>()
                for (r in 0 until event.rows) {
                    for (c in 0 until event.cols) {
                        val text = if (r == 0) "Header ${c + 1}" else "Data"
                        cells["${r}_${c}"] = TableCellModel(listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue(text))))
                    }
                }
                insertBlockAtCursor(TableBlock("tbl_${UUID.randomUUID()}", event.rows, event.cols, cells))
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
                // Select block
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
            
            // Character Formatting Updates
            is RibbonEvent.OnFontFamilyChanged -> {
                pushUndoState()
                val fontName = event.family
                com.example.presentation.editor.font.FontEngine.addRecentFont(fontName)
                _state.update { it.copy(fontFamily = fontName) }
                updateActiveBlock { if (it is TextBlock) it.copy(fontFamily = fontName) else it }
                
                val stateVal = _state.value
                val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock
                
                if (activeBlock != null) {
                    val newAnnotatedString = com.example.presentation.editor.font.FontEngine.applyFontToAnnotatedString(
                        annotatedString = activeBlock.text.annotatedString,
                        selection = activeBlock.text.selection,
                        fontName = fontName
                    )
                    val newBlocks = stateVal.blocks.map { block ->
                        if (block.id == activeBlock.id) {
                            (block as TextBlock).copy(text = activeBlock.text.copy(annotatedString = newAnnotatedString))
                        } else block
                    }
                    _state.update { it.copy(blocks = newBlocks) }
                }
            }
            is RibbonEvent.OnFontSizeChanged -> {
                pushUndoState()
                _state.update { it.copy(fontSize = event.size) }
                updateActiveBlock { if (it is TextBlock) it.copy(fontSize = event.size) else it }
                applyStyleToSelection(SpanStyle(fontSize = event.size.sp))
            }
            is RibbonEvent.OnBoldClicked -> {
                pushUndoState()
                val newBold = !_state.value.isBold
                _state.update { it.copy(isBold = newBold) }
                updateActiveBlock { if (it is TextBlock) it.copy(isBold = newBold) else it }
                applyStyleToSelection(SpanStyle(fontWeight = if (newBold) FontWeight.Bold else FontWeight.Normal))
            }
            is RibbonEvent.OnItalicClicked -> {
                pushUndoState()
                val newItalic = !_state.value.isItalic
                _state.update { it.copy(isItalic = newItalic) }
                updateActiveBlock { if (it is TextBlock) it.copy(isItalic = newItalic) else it }
                applyStyleToSelection(SpanStyle(fontStyle = if (newItalic) FontStyle.Italic else FontStyle.Normal))
            }
            is RibbonEvent.OnUnderlineClicked -> {
                pushUndoState()
                val newUnderline = !_state.value.isUnderline
                _state.update { it.copy(isUnderline = newUnderline) }
                updateActiveBlock { if (it is TextBlock) it.copy(isUnderline = newUnderline) else it }
                applyStyleToSelection(SpanStyle(textDecoration = if (newUnderline) TextDecoration.Underline else TextDecoration.None))
            }
            is RibbonEvent.OnUnderlineStyleChanged -> { 
                pushUndoState()
                _state.update { it.copy(underlineStyle = event.style) } 
            }
            is RibbonEvent.OnStrikethroughClicked -> {
                pushUndoState()
                val newStrike = !_state.value.isStrikethrough
                _state.update { it.copy(isStrikethrough = newStrike) }
                updateActiveBlock { if (it is TextBlock) it.copy(isStrikethrough = newStrike) else it }
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
                updateActiveBlock { if (it is TextBlock) it.copy(textColor = event.color) else it }
                applyStyleToSelection(SpanStyle(color = event.color))
            }
            is RibbonEvent.OnHighlightColorChanged -> {
                pushUndoState()
                _state.update { it.copy(highlightColor = event.color) }
                updateActiveBlock { if (it is TextBlock) it.copy(highlightColor = event.color) else it }
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
                val newRtl = !_state.value.isTextRtl
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
                applyStyleToSelection(
                    SpanStyle(
                        fontSize = st.fontSize.sp,
                        fontWeight = if (st.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (st.isItalic) FontStyle.Italic else FontStyle.Normal,
                        color = st.textColor
                    )
                )
            }
            is RibbonEvent.OnSetUserError -> { _state.update { it.copy(userErrorMessage = event.message) } }
            is RibbonEvent.OnDismissUserError -> { _state.update { it.copy(userErrorMessage = null) } }
            
            // Page Layout
            is RibbonEvent.OnPageSizeChanged -> { _state.update { it.copy(pageSize = event.size) } }
            is RibbonEvent.OnPageOrientationChanged -> { _state.update { it.copy(pageOrientation = event.orientation) } }
            is RibbonEvent.OnPageMarginChanged -> { _state.update { it.copy(pageMargin = event.margin) } }
            
            // Page Design
            is RibbonEvent.OnWatermarkChanged -> { _state.update { it.copy(watermarkText = event.text) } }
            is RibbonEvent.OnPageColorChanged -> { _state.update { it.copy(pageColor = event.color) } }
            is RibbonEvent.OnPageBorderChanged -> { _state.update { it.copy(pageBorder = event.border) } }
            is RibbonEvent.OnApplyDocumentTheme -> {
                when (event.themeName) {
                    "Office" -> _state.update { it.copy(fontFamily = "Calibri", textColor = Color.Black, pageColor = Color.White) }
                    "Modern" -> _state.update { it.copy(fontFamily = "Arial", textColor = Color(0xFF1E293B), pageColor = Color(0xFFF8FAFC)) }
                    "Formal" -> _state.update { it.copy(fontFamily = "Times New Roman", textColor = Color(0xFF0F172A), pageColor = Color(0xFFFFFDF5)) }
                    "Classic" -> _state.update { it.copy(fontFamily = "Georgia", textColor = Color(0xFF1A1A1A), pageColor = Color(0xFFFAFAFA)) }
                    "Emerald" -> _state.update { it.copy(fontFamily = "Calibri", textColor = Color(0xFF064E3B), pageColor = Color(0xFFF0FDF4)) }
                    "Warm Amber" -> _state.update { it.copy(fontFamily = "Times New Roman", textColor = Color(0xFF78350F), pageColor = Color(0xFFFFFBEB)) }
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
            _state.update { it.copy(fontSize = size, isBold = isBold, isItalic = isItalic, textColor = color) }
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
            val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock ?: return
            val currentText = activeBlock.text
            val totalLength = currentText.annotatedString.length
            if (totalLength == 0) return

            val min = currentText.selection.min.coerceIn(0, totalLength)
            val max = currentText.selection.max.coerceIn(0, totalLength)

            // Determine target formatting range
            val (targetStart, targetEnd) = if (min < max) {
                min to max
            } else {
                // If cursor is at a point, find the word boundary around cursor
                val textStr = currentText.annotatedString.text
                val cursor = min.coerceIn(0, textStr.length)
                var wStart = cursor
                while (wStart > 0 && !textStr[wStart - 1].isWhitespace()) {
                    wStart--
                }
                var wEnd = cursor
                while (wEnd < textStr.length && !textStr[wEnd].isWhitespace()) {
                    wEnd++
                }
                if (wStart < wEnd) wStart to wEnd else 0 to totalLength
            }

            val builder = AnnotatedString.Builder()
            builder.append(currentText.annotatedString.text)

            // If no existing spans, apply directly
            if (currentText.annotatedString.spanStyles.isEmpty()) {
                builder.addStyle(spanStyle, targetStart, targetEnd)
            } else {
                // Apply the spanStyle as base for target range
                builder.addStyle(spanStyle, targetStart, targetEnd)

                // Re-apply existing spans, merging where they overlap the target range
                currentText.annotatedString.spanStyles.forEach { range ->
                    val s = range.start.coerceIn(0, totalLength)
                    val e = range.end.coerceIn(0, totalLength)
                    if (s >= e) return@forEach

                    if (e <= targetStart || s >= targetEnd) {
                        // Completely outside target: keep unmodified
                        builder.addStyle(range.item, s, e)
                    } else {
                        // Portion before target
                        if (s < targetStart) {
                            builder.addStyle(range.item, s, targetStart)
                        }
                        // Portion inside target: merge attributes
                        val insideStart = maxOf(s, targetStart)
                        val insideEnd = minOf(e, targetEnd)
                        if (insideStart < insideEnd) {
                            val merged = mergeSpanStyles(range.item, spanStyle)
                            builder.addStyle(merged, insideStart, insideEnd)
                        }
                        // Portion after target
                        if (e > targetEnd) {
                            builder.addStyle(range.item, targetEnd, e)
                        }
                    }
                }
            }

            // Preserve paragraph styles
            currentText.annotatedString.paragraphStyles.forEach { pRange ->
                val ps = pRange.start.coerceIn(0, totalLength)
                val pe = pRange.end.coerceIn(0, totalLength)
                if (ps < pe) {
                    builder.addStyle(pRange.item, ps, pe)
                }
            }

            val newAnnotatedString = builder.toAnnotatedString()
            val newBlocks = stateVal.blocks.map {
                if (it.id == activeBlock.id) activeBlock.copy(text = currentText.copy(annotatedString = newAnnotatedString)) else it
            }
            _state.update { it.copy(blocks = newBlocks) }
        } catch (e: Exception) {
            android.util.Log.e("EditorViewModel", "applyStyleToSelection caught error", e)
        }
    }

    private fun mergeSpanStyles(base: SpanStyle, override: SpanStyle): SpanStyle {
        val newBackground = if (override.background == Color.Transparent) {
            Color.Transparent
        } else if (override.background != Color.Unspecified) {
            override.background
        } else {
            base.background
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
            textDecoration = override.textDecoration ?: base.textDecoration,
            shadow = override.shadow ?: base.shadow
        )
    }

    private fun updateActiveBlock(update: (DocumentBlock) -> DocumentBlock) {
        val stateVal = _state.value
        val newBlocks = stateVal.blocks.map {
            if (it.id == stateVal.activeBlockId) update(it) else it
        }
        _state.update { it.copy(blocks = newBlocks) }
    }

    private fun clearFormattingFromSelection() {
        val stateVal = _state.value
        val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock ?: return
        val currentText = activeBlock.text
        val totalLength = currentText.annotatedString.length
        if (totalLength == 0) return

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

        val newBlocks = stateVal.blocks.map { 
            if (it.id == activeBlock.id) activeBlock.copy(text = currentText.copy(annotatedString = newAnnotatedString)) else it 
        }
        _state.update { it.copy(blocks = newBlocks) }
    }

    private fun applyNumeralSystem(system: NumeralSystem) {
        val stateVal = _state.value
        val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock ?: return
        val currentText = activeBlock.text

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

        val newBlocks = stateVal.blocks.map { 
            if (it.id == activeBlock.id) activeBlock.copy(text = currentText.copy(annotatedString = newAnnotatedString)) else it 
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
}
