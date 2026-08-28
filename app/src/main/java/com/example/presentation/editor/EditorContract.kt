package com.example.presentation.editor

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue

data class EditorState(
    // Core Document Engine (Block-based for Compose & OOXML compatibility)
    val blocks: List<DocumentBlock> = listOf(
        TextBlock("blk_initial", TextFieldValue("Start typing your document here..."))
    ),
    val activeBlockId: String = "blk_initial",
    val documentTitle: String = "Document1",
    val currentUri: Uri? = null,
    
    // File / Backstage View State
    val isFileMenuOpen: Boolean = false,
    val showSaveAsDialog: Boolean = false,

    val activeTab: RibbonTab = RibbonTab.HOME,
    
    // Header & Footer State
    val isEditingHeaderFooter: Boolean = false,
    val headerText: TextFieldValue = TextFieldValue(""),
    val footerText: TextFieldValue = TextFieldValue(""),
    val showPageNumbers: Boolean = true,
    
    // Font State (Reflects UI state for the current selection/cursor)
    val fontFamily: String = "Calibri",
    val fontSize: Int = 12,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val isSubscript: Boolean = false,
    val isSuperscript: Boolean = false,
    val underlineStyle: UnderlineStyle = UnderlineStyle.SINGLE,
    val textColor: Color = Color.Black,
    val highlightColor: Color = Color.Transparent,
    
    // Paragraph State
    val alignment: TextAlignment = TextAlignment.LEFT,
    val isTextRtl: Boolean = false,
    val lineSpacing: Float = 1.15f,
    val indentLevel: Int = 0,
    val isBulletedList: Boolean = false,
    val isNumberedList: Boolean = false,
    
    // Page Layout State
    val pageSize: PageSize = PageSize.A4,
    val pageOrientation: PageOrientation = PageOrientation.PORTRAIT,
    val pageMargin: PageMargin = PageMargin.NORMAL,
    
    // Page Design State
    val watermarkText: String = "",
    val pageColor: Color = Color.White,
    val pageBorder: PageBorder = PageBorder(),
    
    // Canvas Zoom & Pan
    val zoomScale: Float = 1f,
    
    // Localization & Typography
    val isRtl: Boolean = false,
    val numeralSystem: NumeralSystem = NumeralSystem.WESTERN
)

sealed class DocumentBlock {
    abstract val id: String
}

data class TextBlock(
    override val id: String,
    val text: TextFieldValue,
    val alignment: TextAlignment = TextAlignment.LEFT,
    val lineSpacing: Float = 1.15f,
    val isRtl: Boolean = false
) : DocumentBlock()

data class TableCellModel(
    val textBlocks: List<TextBlock>,
    val backgroundColor: Color = Color.Transparent,
    val isRtl: Boolean = false
)

data class TableBlock(
    override val id: String,
    val rows: Int,
    val cols: Int,
    val cells: Map<String, TableCellModel>,
    val isRtl: Boolean = false
) : DocumentBlock()

data class ImageBlock(
    override val id: String,
    val imageData: ByteArray? = null,
    val width: Float = 0f,
    val height: Float = 0f,
    val uri: String = "",
    val wrapMode: WrapMode = WrapMode.IN_LINE
) : DocumentBlock() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ImageBlock
        if (id != other.id) return false
        if (imageData != null) {
            if (other.imageData == null) return false
            if (!imageData.contentEquals(other.imageData)) return false
        } else if (other.imageData != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        return result
    }
}

data class ShapeBlock(
    override val id: String,
    val type: ShapeType,
    val strokeColor: Color = Color.Black,
    val fillColor: Color = Color(0xFFE2E8F0)
) : DocumentBlock()

data class PageBreakBlock(
    override val id: String
) : DocumentBlock()

enum class WrapMode { IN_LINE, SQUARE, TIGHT, BEHIND, IN_FRONT }
enum class ShapeType { RECTANGLE, OVAL, ARROW, LINE, STAR }

enum class RibbonTab(val title: String) {
    FILE("File"), 
    HOME("Home"), 
    INSERT("Insert"), 
    DESIGN("Design"),
    LAYOUT("Layout"), 
    VIEW("View")
}

enum class TextAlignment { LEFT, CENTER, RIGHT, JUSTIFY }
enum class NumeralSystem { WESTERN, ARABIC }
enum class UnderlineStyle { SINGLE, DOUBLE, DOTTED, NONE }

enum class PageSize { A4, A3, LETTER, LEGAL, A5 }
enum class PageOrientation { PORTRAIT, LANDSCAPE }
enum class PageMargin { NORMAL, NARROW, MODERATE, WIDE }

enum class BorderStyle { NONE, SOLID, DASHED, DOTTED, DOUBLE }
enum class BorderSetting { NONE, BOX, SHADOW }

data class PageBorder(
    val setting: BorderSetting = BorderSetting.NONE,
    val style: BorderStyle = BorderStyle.SOLID,
    val color: Color = Color.Black,
    val widthPt: Float = 0.5f
)

sealed class RibbonEvent {
    data class ChangeTab(val tab: RibbonTab) : RibbonEvent()
    
    // App Level
    object OnLanguageToggled : RibbonEvent()
    data class OnDocumentImported(val model: DocumentModel, val uri: Uri? = null) : RibbonEvent()
    object OnNewDocument : RibbonEvent()
    data class OnZoomChanged(val scale: Float) : RibbonEvent()
    
    // File Menu Actions
    object OnToggleFileMenu : RibbonEvent()
    object OnShowSaveAsDialog : RibbonEvent()
    object OnDismissSaveAsDialog : RibbonEvent()
    object OnSaveDocumentClicked : RibbonEvent()
    data class OnDocumentTitleChanged(val title: String) : RibbonEvent()
    
    // Document Level
    data class OnDocumentTextChanged(val blockId: String, val text: TextFieldValue) : RibbonEvent()
    data class OnBlockFocusChanged(val blockId: String) : RibbonEvent()
    data class OnAddParagraphAfter(val blockId: String) : RibbonEvent()
    data class OnDeleteBlockIfEmpty(val blockId: String) : RibbonEvent()
    
    // Header & Footer
    object OnToggleHeaderFooterMode : RibbonEvent()
    data class OnHeaderTextChanged(val text: TextFieldValue) : RibbonEvent()
    data class OnFooterTextChanged(val text: TextFieldValue) : RibbonEvent()
    
    // Insert Tab Actions
    data class OnInsertTableClicked(val rows: Int, val cols: Int) : RibbonEvent()
    data class OnInsertImageWithUri(val uri: String) : RibbonEvent()
    data class OnInsertShapeClicked(val type: ShapeType) : RibbonEvent()
    object OnInsertPageBreakClicked : RibbonEvent()
    object OnInsertPageNumberClicked : RibbonEvent()
    
    // Table Interaction
    data class OnTableCellChanged(val blockId: String, val cellId: String, val text: TextFieldValue) : RibbonEvent()
    
    // Clipboard
    object OnCutClicked : RibbonEvent()
    object OnCopyClicked : RibbonEvent()
    object OnPasteClicked : RibbonEvent()
    
    // Character Formatting
    data class OnFontFamilyChanged(val family: String) : RibbonEvent()
    data class OnFontSizeChanged(val size: Int) : RibbonEvent()
    object OnBoldClicked : RibbonEvent()
    object OnItalicClicked : RibbonEvent()
    object OnUnderlineClicked : RibbonEvent()
    data class OnUnderlineStyleChanged(val style: UnderlineStyle) : RibbonEvent()
    object OnStrikethroughClicked : RibbonEvent()
    object OnSubscriptClicked : RibbonEvent()
    object OnSuperscriptClicked : RibbonEvent()
    data class OnTextColorChanged(val color: Color) : RibbonEvent()
    data class OnHighlightColorChanged(val color: Color) : RibbonEvent()
    object OnClearFormattingClicked : RibbonEvent()
    data class OnNumeralSystemChanged(val system: NumeralSystem) : RibbonEvent()
    
    // Paragraph Formatting
    data class OnAlignmentChanged(val alignment: TextAlignment) : RibbonEvent()
    data class OnLineSpacingChanged(val spacing: Float) : RibbonEvent()
    object OnIncreaseIndentClicked : RibbonEvent()
    object OnDecreaseIndentClicked : RibbonEvent()
    object OnTextDirectionToggled : RibbonEvent()
    object OnBulletedListToggled : RibbonEvent()
    object OnNumberedListToggled : RibbonEvent()
    
    // Page Layout
    data class OnPageSizeChanged(val size: PageSize) : RibbonEvent()
    data class OnPageOrientationChanged(val orientation: PageOrientation) : RibbonEvent()
    data class OnPageMarginChanged(val margin: PageMargin) : RibbonEvent()
    
    // Page Design
    data class OnWatermarkChanged(val text: String) : RibbonEvent()
    data class OnPageColorChanged(val color: Color) : RibbonEvent()
    data class OnPageBorderChanged(val border: PageBorder) : RibbonEvent()
}
