package com.example.presentation.editor

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue

data class DrawingPath(
    val path: androidx.compose.ui.graphics.Path,
    val pageIndex: Int = 0,
    val color: Color,
    val strokeWidth: Float,
    val isHighlighter: Boolean = false
)

data class EditorState(
    // Core Document Engine (Block-based for Compose & OOXML compatibility)
    val blocks: List<DocumentBlock> = DocumentFactory.createComprehensiveTestDocument(),
    val drawingPaths: List<DrawingPath> = emptyList(),
    val isDrawingMode: Boolean = false,
    val inkColor: Color = Color.Black,
    val inkThickness: Float = 4f,
    val isHighlighterMode: Boolean = false,
    val isEraserMode: Boolean = false,
    val activeBlockId: String = blocks.firstOrNull()?.id ?: "blk_initial",
    val documentTitle: String = "مستند1",
    val currentUri: Uri? = null,
    val cloudDocId: String? = null,
    val isSavingToCloud: Boolean = false,
    
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
    val textEffect: TextEffectType = TextEffectType.NONE,
    
    // Clipboard & Format Painter State
    val isFormatPainterActive: Boolean = false,
    val isFormatPainterLocked: Boolean = false,
    val copiedFormat: CopiedCharacterFormat? = null,
    
    // Paragraph State
    val alignment: TextAlignment = TextAlignment.RIGHT,
    val isTextRtl: Boolean = true,
    val lineSpacing: Float = 1.15f,
    val indentLevel: Int = 0,
    val isBulletedList: Boolean = false,
    val bulletShape: BulletShape = BulletShape.DISC,
    val isNumberedList: Boolean = false,
    val numberingStyle: NumberingStyle = NumberingStyle.DECIMAL_DOT,
    val multilevelStyle: MultilevelStyle = MultilevelStyle.NONE,
    val paragraphShadingColor: Color? = null,
    val paragraphBorder: ParagraphBorder = ParagraphBorder.NONE,
    val showNonPrintingCharacters: Boolean = false,
    
    // Custom Styles Library
    val customStyles: List<CustomStyleModel> = emptyList(),
    val selectedStyleName: String = "Normal",
    
    // Page Layout State
    val pageSize: PageSize = PageSize.A4,
    val pageOrientation: PageOrientation = PageOrientation.PORTRAIT,
    val pageMargin: PageMargin = PageMargin.NORMAL,
    
    // Page Design State
    val watermarkText: String = "",
    val pageColor: Color = Color.White,
    val pageBorder: PageBorder = PageBorder(),
    val pageStripeStyle: PageStripeStyle = PageStripeStyle.NONE,
    val pageAccentColor: Color? = null,
    val pageSecondaryColor: Color? = null,
    
    // Canvas Zoom & Pan & View Customization (Windows Style)
    val zoomScale: Float = 1.0f,
    val viewMode: ViewMode = ViewMode.PRINT_LAYOUT,
    
    // Localization & Typography
    val isRtl: Boolean = true,
    val numeralSystem: NumeralSystem = NumeralSystem.WESTERN,
    
    // Dialogs & Exports
    val showWordCountDialog: Boolean = false,
    val showFindReplaceDialog: Boolean = false,
    val showTemplatesDialog: Boolean = false,
    val showExportPdfSuccessDialog: Boolean = false,
    val activeGroupDetailsDialog: String? = null,
    val exportedPdfUri: Uri? = null,
    val isProtectedView: Boolean = true,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val userErrorMessage: String? = null
)

sealed class DocumentBlock {
    abstract val id: String
}

data class TextBlock(
    override val id: String,
    val text: TextFieldValue,
    val alignment: TextAlignment = TextAlignment.RIGHT,
    val lineSpacing: Float = 1.15f,
    val isRtl: Boolean = true,
    val indentLevel: Int = 0,
    val isBulletedList: Boolean = false,
    val bulletShape: BulletShape = BulletShape.DISC,
    val isNumberedList: Boolean = false,
    val numberingStyle: NumberingStyle = NumberingStyle.DECIMAL_DOT,
    val paragraphShadingColor: Color? = null,
    val paragraphBorder: ParagraphBorder = ParagraphBorder.NONE,
    val fontSize: Int = 12,
    val fontFamily: String = "Calibri",
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val textColor: Color = Color.Black,
    val highlightColor: Color = Color.Transparent
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

data class BannerBlock(
    override val id: String,
    val title: String,
    val subtitle: String = "",
    val backgroundColor: Color = Color(0xFF1E3A8A),
    val textColor: Color = Color.White,
    val alignment: TextAlignment = TextAlignment.RIGHT
) : DocumentBlock()

data class CalloutBlock(
    override val id: String,
    val text: TextFieldValue,
    val title: String = "",
    val backgroundColor: Color = Color(0xFFF1F5F9),
    val borderColor: Color = Color(0xFF3B82F6),
    val textColor: Color = Color(0xFF1E293B)
) : DocumentBlock()

data class DividerBlock(
    override val id: String,
    val color: Color = Color(0xFFCBD5E1),
    val thicknessDp: Float = 1.5f,
    val paddingVerticalDp: Float = 8f
) : DocumentBlock()

enum class PageStripeStyle {
    NONE,
    SIDE_BAR_RIGHT,
    SIDE_BAR_LEFT,
    TOP_BAR,
    LETTERHEAD_HEADER,
    RESUME_HEADER_BAND,
    CERTIFICATE_GOLD
}

enum class WrapMode { IN_LINE, SQUARE, TIGHT, BEHIND, IN_FRONT }
enum class ShapeType { RECTANGLE, OVAL, ARROW, LINE, STAR }

enum class RibbonTab(val title: String) {
    FILE("File"), 
    HOME("Home"), 
    INSERT("Insert"),
    DRAW("Draw"),
    DESIGN("Design"),
    LAYOUT("Layout"), 
    REFERENCES("References"),
    MAILINGS("Mailings"),
    REVIEW("Review"),
    VIEW("View"),
    HELP("Help"),
    PICTURE_FORMAT("Picture Format"),
    TABLE_DESIGN("Table Design"),
    SHAPE_FORMAT("Shape Format");
    
    companion object {
        val standardTabs = listOf(FILE, HOME, INSERT, DRAW, DESIGN, LAYOUT, REFERENCES, MAILINGS, REVIEW, VIEW, HELP)
    }
}

enum class TextAlignment { LEFT, CENTER, RIGHT, JUSTIFY }
enum class NumeralSystem { WESTERN, ARABIC }
enum class UnderlineStyle { SINGLE, DOUBLE, DOTTED, DASHED, WAVY, NONE }
enum class TextEffectType { NONE, SHADOW, OUTLINE, GLOW, REFLECTION, GRADIENT }

enum class PasteMode { KEEP_SOURCE, MERGE_FORMATTING, KEEP_TEXT_ONLY, SPECIAL }
enum class BulletShape { DISC, CIRCLE, SQUARE, HOLLOW_SQUARE, CHECKMARK, ARROW, STAR, FLORAL }
enum class NumberingStyle { DECIMAL_DOT, DECIMAL_PAREN, ARABIC_ALIF_BAA, ARABIC_INDIC, ALPHA_UPPER, ALPHA_LOWER, ROMAN_UPPER, ROMAN_LOWER }
enum class MultilevelStyle { NONE, NUMERIC_LEVELS, ALPHA_NUMERIC, HEADING_LEVELS }
enum class ParagraphBorder { NONE, BOTTOM, TOP, LEFT, RIGHT, ALL, OUTSIDE }

data class CopiedCharacterFormat(
    val fontFamily: String = "Calibri",
    val fontSize: Int = 12,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val isSubscript: Boolean = false,
    val isSuperscript: Boolean = false,
    val textColor: Color = Color.Black,
    val highlightColor: Color = Color.Transparent,
    val alignment: TextAlignment = TextAlignment.RIGHT,
    val lineSpacing: Float = 1.15f
)

data class CustomStyleModel(
    val id: String,
    val name: String,
    val fontFamily: String = "Calibri",
    val fontSize: Int = 12,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val textColor: Color = Color.Black,
    val alignment: TextAlignment = TextAlignment.RIGHT,
    val lineSpacing: Float = 1.15f
)

enum class ViewMode { PRINT_LAYOUT, WEB_LAYOUT, READ_MODE }

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
    data class OnViewModeChanged(val mode: ViewMode) : RibbonEvent()
    object OnToggleProtectedView : RibbonEvent()
    object OnSaveToCloudClicked : RibbonEvent()
    data class OnCloudDocIdSaved(val id: String) : RibbonEvent()
    data class OnLoadFromCloud(val cloudDocumentId: String, val base64Data: String) : RibbonEvent()
    
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
    data class OnDeleteBlock(val blockId: String) : RibbonEvent()
    
    // Header & Footer
    object OnToggleHeaderFooterMode : RibbonEvent()
    data class OnHeaderTextChanged(val text: TextFieldValue) : RibbonEvent()
    data class OnFooterTextChanged(val text: TextFieldValue) : RibbonEvent()
    
    // Insert Tab Actions
    data class OnInsertTableClicked(val rows: Int, val cols: Int) : RibbonEvent()
    data class OnInsertImageWithUri(val uri: String) : RibbonEvent()
    data class OnInsertImageBytes(val bytes: ByteArray, val uri: String = "") : RibbonEvent()
    object OnPickImageRequested : RibbonEvent()
    data class OnInsertShapeClicked(val type: ShapeType) : RibbonEvent()
    object OnInsertPageBreakClicked : RibbonEvent()
    object OnInsertPageNumberClicked : RibbonEvent()
    
    // Table Interaction
    data class OnTableCellChanged(val blockId: String, val cellId: String, val text: TextFieldValue) : RibbonEvent()
    
    // Clipboard
    object OnCutClicked : RibbonEvent()
    object OnCopyClicked : RibbonEvent()
    object OnPasteClicked : RibbonEvent()
    data class OnPasteSpecialClicked(val pasteMode: PasteMode) : RibbonEvent()
    data class OnFormatPainterToggled(val isLocked: Boolean = false) : RibbonEvent()
    object OnCutTextFromSelection : RibbonEvent()
    data class OnPasteTextAtSelection(val text: String) : RibbonEvent()
    
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
    data class OnTextEffectChanged(val effect: TextEffectType) : RibbonEvent()
    object OnClearFormattingClicked : RibbonEvent()
    data class OnNumeralSystemChanged(val system: NumeralSystem) : RibbonEvent()
    
    // Paragraph Formatting
    data class OnAlignmentChanged(val alignment: TextAlignment) : RibbonEvent()
    data class OnLineSpacingChanged(val spacing: Float) : RibbonEvent()
    object OnIncreaseIndentClicked : RibbonEvent()
    object OnDecreaseIndentClicked : RibbonEvent()
    object OnTextDirectionToggled : RibbonEvent()
    object OnBulletedListToggled : RibbonEvent()
    data class OnBulletShapeChanged(val shape: BulletShape) : RibbonEvent()
    object OnNumberedListToggled : RibbonEvent()
    data class OnNumberingStyleChanged(val style: NumberingStyle) : RibbonEvent()
    data class OnMultilevelStyleChanged(val style: MultilevelStyle) : RibbonEvent()
    data class OnSortParagraphsClicked(val ascending: Boolean = true) : RibbonEvent()
    object OnToggleNonPrintingCharacters : RibbonEvent()
    data class OnParagraphShadingChanged(val color: Color?) : RibbonEvent()
    data class OnParagraphBorderChanged(val border: ParagraphBorder) : RibbonEvent()
    
    // Styles
    data class OnApplyHeadingStyle(val styleName: String) : RibbonEvent()
    data class OnCreateCustomStyle(
        val name: String,
        val fontFamily: String,
        val fontSize: Int,
        val isBold: Boolean,
        val isItalic: Boolean,
        val textColor: Color,
        val alignment: TextAlignment
    ) : RibbonEvent()
    data class OnApplyCustomStyle(val style: CustomStyleModel) : RibbonEvent()
    
    // Selection & Editing
    object OnSelectAllClicked : RibbonEvent()
    object OnSelectCurrentBlockClicked : RibbonEvent()
    object OnSelectSimilarFormattingClicked : RibbonEvent()
    
    // Page Layout
    data class OnPageSizeChanged(val size: PageSize) : RibbonEvent()
    data class OnPageOrientationChanged(val orientation: PageOrientation) : RibbonEvent()
    data class OnPageMarginChanged(val margin: PageMargin) : RibbonEvent()
    
    // Page Design
    data class OnWatermarkChanged(val text: String) : RibbonEvent()
    data class OnPageColorChanged(val color: Color) : RibbonEvent()
    data class OnPageBorderChanged(val border: PageBorder) : RibbonEvent()
    data class OnApplyDocumentTheme(val themeName: String) : RibbonEvent()
    
    // Tools & Export & Dialogs
    object OnUndoClicked : RibbonEvent()
    object OnRedoClicked : RibbonEvent()
    object OnIncreaseFontSizeClicked : RibbonEvent()
    object OnDecreaseFontSizeClicked : RibbonEvent()
    data class OnChangeCaseClicked(val caseType: String) : RibbonEvent()
    object OnShowFindReplaceDialog : RibbonEvent()
    object OnDismissFindReplaceDialog : RibbonEvent()
    object OnShowTemplatesDialog : RibbonEvent()
    object OnDismissTemplatesDialog : RibbonEvent()
    data class OnApplyDocumentTemplate(val templateId: String) : RibbonEvent()
    data class OnFindAndReplaceClicked(val findText: String, val replaceText: String) : RibbonEvent()
    data class OnShowGroupDetails(val groupName: String) : RibbonEvent()
    object OnDismissGroupDetails : RibbonEvent()
    object OnExportPdfClicked : RibbonEvent()
    object OnShowWordCountClicked : RibbonEvent()
    object OnDismissWordCountClicked : RibbonEvent()
    object OnDismissExportPdfDialog : RibbonEvent()
    data class OnInsertBannerClicked(val title: String, val subtitle: String) : RibbonEvent()
    data class OnInsertCalloutClicked(val title: String, val text: String) : RibbonEvent()
    object OnInsertDividerClicked : RibbonEvent()
    object OnInsertSignatureLineClicked : RibbonEvent()
    
    // Drawing Events
    object OnToggleDrawingMode : RibbonEvent()
    object OnToggleHighlighterMode : RibbonEvent()
    object OnToggleEraserMode : RibbonEvent()
    data class OnInkColorChanged(val color: Color) : RibbonEvent()
    data class OnInkThicknessChanged(val thickness: Float) : RibbonEvent()
    data class OnDrawPathAdded(val path: DrawingPath) : RibbonEvent()
    object OnClearDrawing : RibbonEvent()
    data class OnInsertSymbolClicked(val symbol: String) : RibbonEvent()
    object OnEnableEditing : RibbonEvent()
    data class OnSetUserError(val message: String) : RibbonEvent()
    object OnDismissUserError : RibbonEvent()
}

// Mail Merge Architecture Enums
enum class MailMergeState { IDLE, SELECTING_RECIPIENTS, INSERTING_FIELDS, PREVIEWING, FINISHED }
data class MailMergeRecipient(val id: String, val name: String, val email: String)
data class MailMergeField(val id: String, val label: String)
