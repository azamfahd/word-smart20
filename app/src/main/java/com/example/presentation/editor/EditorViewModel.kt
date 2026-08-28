package com.example.presentation.editor

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
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
            isProtectedView = true,
            currentUri = null,
            isFileMenuOpen = false
        )
    }

    fun clearSavingToCloudFlag() {
        _state.update { it.copy(isSavingToCloud = false) }
    }

    fun processEvent(event: RibbonEvent) {
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
                    val blocks = DocxImporter().import(java.io.ByteArrayInputStream(bytes))
                    _state.update {
                        it.copy(
                            blocks = blocks,
                            activeBlockId = blocks.firstOrNull()?.id ?: "blk_initial",
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
            is RibbonEvent.OnViewModeChanged -> _state.update { it.copy(viewMode = event.mode) }
            
            is RibbonEvent.OnDocumentImported -> {
                _state.update {
                    it.copy(
                        blocks = event.model.blocks,
                        activeBlockId = event.model.blocks.firstOrNull()?.id ?: "blk_initial",
                        headerText = TextFieldValue(event.model.headerText),
                        footerText = TextFieldValue(event.model.footerText),
                        isRtl = event.model.isRtl,
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
            is RibbonEvent.OnBlockFocusChanged -> _state.update { it.copy(activeBlockId = event.blockId) }
            
            is RibbonEvent.OnAddParagraphAfter -> {
                val stateVal = _state.value
                val idx = stateVal.blocks.indexOfFirst { it.id == event.blockId }
                if (idx != -1) {
                    val newId = "blk_${UUID.randomUUID()}"
                    val newBlock = TextBlock(newId, TextFieldValue(""))
                    val newBlocks = stateVal.blocks.toMutableList()
                    newBlocks.add(idx + 1, newBlock)
                    _state.update { it.copy(blocks = newBlocks, activeBlockId = newId) }
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
            is RibbonEvent.OnInsertShapeClicked -> {
                insertBlockAtCursor(ShapeBlock("shp_${UUID.randomUUID()}", event.type))
            }
            is RibbonEvent.OnInsertPageNumberClicked -> {
                val pageNumChar = if (_state.value.numeralSystem == NumeralSystem.ARABIC) "١" else "1"
                val currentFooter = _state.value.footerText
                val newFooter = currentFooter.copy(text = (currentFooter.text + " Page $pageNumChar ").trim())
                _state.update { it.copy(footerText = newFooter, showPageNumbers = true) }
            }
            
            // Clipboard
            is RibbonEvent.OnCutClicked -> {}
            is RibbonEvent.OnCopyClicked -> {}
            is RibbonEvent.OnPasteClicked -> {}
            
            // Character Formatting Updates
            is RibbonEvent.OnFontFamilyChanged -> {
                _state.update { it.copy(fontFamily = event.family) }
                applyStyleToSelection(SpanStyle(fontSize = _state.value.fontSize.sp))
            }
            is RibbonEvent.OnFontSizeChanged -> {
                _state.update { it.copy(fontSize = event.size) }
                applyStyleToSelection(SpanStyle(fontSize = event.size.sp))
            }
            is RibbonEvent.OnBoldClicked -> {
                val newBold = !_state.value.isBold
                _state.update { it.copy(isBold = newBold) }
                applyStyleToSelection(SpanStyle(fontWeight = if (newBold) FontWeight.Bold else FontWeight.Normal))
            }
            is RibbonEvent.OnItalicClicked -> {
                val newItalic = !_state.value.isItalic
                _state.update { it.copy(isItalic = newItalic) }
                applyStyleToSelection(SpanStyle(fontStyle = if (newItalic) FontStyle.Italic else FontStyle.Normal))
            }
            is RibbonEvent.OnUnderlineClicked -> {
                val newUnderline = !_state.value.isUnderline
                _state.update { it.copy(isUnderline = newUnderline) }
                applyStyleToSelection(SpanStyle(textDecoration = if (newUnderline) TextDecoration.Underline else TextDecoration.None))
            }
            is RibbonEvent.OnUnderlineStyleChanged -> { _state.update { it.copy(underlineStyle = event.style) } }
            is RibbonEvent.OnStrikethroughClicked -> {
                val newStrike = !_state.value.isStrikethrough
                _state.update { it.copy(isStrikethrough = newStrike) }
                applyStyleToSelection(SpanStyle(textDecoration = if (newStrike) TextDecoration.LineThrough else TextDecoration.None))
            }
            is RibbonEvent.OnSubscriptClicked -> {
                val newSub = !_state.value.isSubscript
                _state.update { it.copy(isSubscript = newSub, isSuperscript = false) }
                applyStyleToSelection(SpanStyle(baselineShift = if (newSub) BaselineShift.Subscript else BaselineShift.None))
            }
            is RibbonEvent.OnSuperscriptClicked -> {
                val newSuper = !_state.value.isSuperscript
                _state.update { it.copy(isSuperscript = newSuper, isSubscript = false) }
                applyStyleToSelection(SpanStyle(baselineShift = if (newSuper) BaselineShift.Superscript else BaselineShift.None))
            }
            is RibbonEvent.OnTextColorChanged -> {
                _state.update { it.copy(textColor = event.color) }
                applyStyleToSelection(SpanStyle(color = event.color))
            }
            is RibbonEvent.OnHighlightColorChanged -> {
                _state.update { it.copy(highlightColor = event.color) }
                applyStyleToSelection(SpanStyle(background = event.color))
            }
            is RibbonEvent.OnClearFormattingClicked -> clearFormattingFromSelection()
            is RibbonEvent.OnNumeralSystemChanged -> applyNumeralSystem(event.system)
            
            // Paragraph Formatting
            is RibbonEvent.OnAlignmentChanged -> {
                _state.update { it.copy(alignment = event.alignment) }
                val align = when (event.alignment) {
                    TextAlignment.LEFT -> TextAlign.Left
                    TextAlignment.CENTER -> TextAlign.Center
                    TextAlignment.RIGHT -> TextAlign.Right
                    TextAlignment.JUSTIFY -> TextAlign.Justify
                }
                applyParagraphStyleToSelection(ParagraphStyle(textAlign = align))
            }
            is RibbonEvent.OnLineSpacingChanged -> {
                _state.update { it.copy(lineSpacing = event.spacing) }
                applyParagraphStyleToSelection(ParagraphStyle(lineHeight = event.spacing.em))
            }
            is RibbonEvent.OnIncreaseIndentClicked -> {
                _state.update { it.copy(indentLevel = it.indentLevel + 1) }
                applyParagraphStyleToSelection(ParagraphStyle(textIndent = TextIndent(firstLine = (_state.value.indentLevel * 1.5).em, restLine = (_state.value.indentLevel * 1.5).em)))
            }
            is RibbonEvent.OnDecreaseIndentClicked -> {
                _state.update { it.copy(indentLevel = maxOf(0, it.indentLevel - 1)) }
                applyParagraphStyleToSelection(ParagraphStyle(textIndent = TextIndent(firstLine = (_state.value.indentLevel * 1.5).em, restLine = (_state.value.indentLevel * 1.5).em)))
            }
            is RibbonEvent.OnTextDirectionToggled -> {
                val newRtl = !_state.value.isTextRtl
                _state.update { it.copy(isTextRtl = newRtl) }
                applyParagraphStyleToSelection(ParagraphStyle(textDirection = if (newRtl) TextDirection.Rtl else TextDirection.Ltr))
            }
            is RibbonEvent.OnBulletedListToggled -> { _state.update { it.copy(isBulletedList = !it.isBulletedList, isNumberedList = false) } }
            is RibbonEvent.OnNumberedListToggled -> { _state.update { it.copy(isNumberedList = !it.isNumberedList, isBulletedList = false) } }
            
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
            val (size, isBold, color) = when (styleName) {
                "Title" -> Triple(24, true, Color(0xFF1E3A8A))
                "Subtitle" -> Triple(14, false, Color(0xFF4B5563))
                "Heading 1" -> Triple(20, true, Color(0xFF2563EB))
                "Heading 2" -> Triple(16, true, Color(0xFF1D4ED8))
                "Heading 3" -> Triple(14, true, Color(0xFF374151))
                else -> Triple(12, false, Color.Black)
            }
            _state.update { it.copy(fontSize = size, isBold = isBold, textColor = color) }
            applyStyleToSelection(SpanStyle(fontSize = size.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = color))
        }
    }

    private fun performFindAndReplace(findText: String, replaceText: String) {
        if (findText.isEmpty()) return
        val stateVal = _state.value
        val newBlocks = stateVal.blocks.map { block ->
            if (block is TextBlock) {
                val replacedStr = block.text.text.replace(findText, replaceText, ignoreCase = true)
                block.copy(text = block.text.copy(text = replacedStr))
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

    private fun applyStyleToSelection(spanStyle: SpanStyle) {
        val stateVal = _state.value
        val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock ?: return
        val currentText = activeBlock.text
        
        if (currentText.selection.collapsed) {
            // Apply to the whole block if nothing selected
            val newAnnotatedString = buildAnnotatedString {
                append(currentText.annotatedString)
                addStyle(spanStyle, 0, currentText.annotatedString.length)
            }
            val newBlocks = stateVal.blocks.map { 
                if (it.id == activeBlock.id) activeBlock.copy(text = currentText.copy(annotatedString = newAnnotatedString)) else it 
            }
            _state.update { it.copy(blocks = newBlocks) }
            return
        }
        
        val min = currentText.selection.min.coerceIn(0, currentText.annotatedString.length)
        val max = currentText.selection.max.coerceIn(0, currentText.annotatedString.length)

        val newAnnotatedString = buildAnnotatedString {
            append(currentText.annotatedString)
            addStyle(spanStyle, min, max)
        }
        
        val newBlocks = stateVal.blocks.map { 
            if (it.id == activeBlock.id) activeBlock.copy(text = currentText.copy(annotatedString = newAnnotatedString)) else it 
        }
        _state.update { it.copy(blocks = newBlocks) }
    }

    private fun applyParagraphStyleToSelection(paragraphStyle: ParagraphStyle) {
        val stateVal = _state.value
        val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock ?: return
        val currentText = activeBlock.text
        
        val newAnnotatedString = buildAnnotatedString {
            append(currentText.annotatedString)
            addStyle(paragraphStyle, 0, currentText.annotatedString.length)
        }
        
        val newBlocks = stateVal.blocks.map { 
            if (it.id == activeBlock.id) activeBlock.copy(text = currentText.copy(annotatedString = newAnnotatedString)) else it 
        }
        _state.update { it.copy(blocks = newBlocks) }
    }

    private fun clearFormattingFromSelection() {
        val stateVal = _state.value
        val activeBlock = stateVal.blocks.find { it.id == stateVal.activeBlockId } as? TextBlock ?: return
        val currentText = activeBlock.text
        
        val plainText = currentText.annotatedString.text
        val newAnnotatedString = buildAnnotatedString {
            append(plainText)
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
}
