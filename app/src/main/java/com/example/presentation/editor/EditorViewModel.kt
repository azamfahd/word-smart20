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
            blocks = listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue(""))),
            documentTitle = "Document1",
            currentUri = null,
            isFileMenuOpen = false
        )
    }

    fun processEvent(event: RibbonEvent) {
        when (event) {
            is RibbonEvent.ChangeTab -> _state.update { it.copy(activeTab = event.tab) }
            is RibbonEvent.OnLanguageToggled -> _state.update { it.copy(isRtl = !it.isRtl) }
            is RibbonEvent.OnZoomChanged -> _state.update { it.copy(zoomScale = event.scale.coerceIn(0.5f, 2.5f)) }
            
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
                val initialBlock = TextBlock("blk_${UUID.randomUUID()}", TextFieldValue(""))
                _state.update {
                    it.copy(
                        blocks = listOf(initialBlock),
                        activeBlockId = initialBlock.id,
                        documentTitle = "Document1",
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
