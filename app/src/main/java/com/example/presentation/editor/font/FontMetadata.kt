package com.example.presentation.editor.font

import androidx.compose.ui.text.font.FontWeight
import java.io.File

/**
 * Source origin of the font
 */
enum class FontSource {
    BUNDLED_GOOGLE,
    SYSTEM_DEVICE,
    USER_IMPORTED,
    DOCUMENT_EMBEDDED
}

/**
 * Script types supported by a font
 */
enum class ScriptType {
    ARABIC,
    LATIN,
    BILINGUAL,
    NUMBERS_PUNCTUATION
}

/**
 * Availability status of a font in the current environment
 */
enum class FontAvailability {
    AVAILABLE,
    MISSING_FALLBACK_ACTIVE,
    DOWNLOADING
}

/**
 * Main Font Groups for UI filtering
 */
enum class FontMainGroup(val titleAr: String, val titleEn: String, val badgeColorHex: Long) {
    ARABIC("الخط العربي", "Arabic Fonts", 0xFF059669),
    ENGLISH("الخط الإنجليزي", "English Fonts", 0xFF2563EB),
    COMMON("الخط المشترك (أوفيس وعالمي)", "Common / Bilingual", 0xFF7C3AED),
    RECENT("الخطوط المستخدمة مؤخراً", "Recently Used", 0xFFD97706),
    SYSTEM("خطوط النظام المتاحة", "System Fonts", 0xFF4B5563),
    IMPORTED("الخطوط المستوردة", "Imported Fonts", 0xFF0891B2)
}

/**
 * Sub-categories for Font categorization
 */
enum class FontSubCategory(val titleAr: String, val titleEn: String) {
    // Arabic Sub-categories
    RUQAA("خطوط الرقعة الأصيلة", "Ruq'ah Style"),
    KUFI("خطوط الكوفي الهندسية", "Kufic Style"),
    NASKH_HERITAGE("خطوط النسخ والتراث الأكاديمي", "Naskh & Heritage"),
    DECORATIVE("خطوط مزخرفة وفنية", "Artistic & Decorative"),
    MODERN_ARABIC("خطوط عربية حديثة وعصرية", "Modern Arabic"),

    // English Sub-categories
    SANS_SERIF("خطوط Sans-Serif عصرية", "Modern Sans-Serif"),
    SERIF_CLASSIC("خطوط Serif كلاسيكية ورسمية", "Classic Serif"),
    MONOSPACE("خطوط برمجية ثابتة العرض", "Monospace / Code"),
    DISPLAY_ARTISTIC("خطوط عناوين وفنية", "Display & Cursive"),

    // Common / Office Sub-categories
    OFFICE_STANDARD("خطوط أوفيس القياسية", "MS Office Standard"),
    BILINGUAL_HARMONY("خطوط متناسقة ثنائية اللغة", "Bilingual Harmony")
}

/**
 * Comprehensive Font Metadata Model
 */
data class FontMetadata(
    val id: String,
    val family: String,
    val displayName: String,
    val arabicName: String,
    val mainGroup: FontMainGroup,
    val subCategory: FontSubCategory,
    val description: String,
    val supportedScripts: Set<ScriptType> = setOf(ScriptType.LATIN, ScriptType.ARABIC),
    val arabicSupport: Boolean = true,
    val latinSupport: Boolean = true,
    val supportedWeights: List<FontWeight> = listOf(
        FontWeight.Normal,
        FontWeight.Medium,
        FontWeight.Bold,
        FontWeight.Light
    ),
    val source: FontSource = FontSource.BUNDLED_GOOGLE,
    val availability: FontAvailability = FontAvailability.AVAILABLE,
    val fallbackFamily: String = "sans-serif",
    val customFontFile: File? = null,
    val arabicPreview: String = "بسم الله الرحمن الرحيم — أ ب ج د 123",
    val englishPreview: String = "The quick brown fox jumps over 123",
    val isFavorite: Boolean = false,
    val isRecent: Boolean = false
)

/**
 * Result of detecting font in a selection range
 */
sealed class FontSelectionResult {
    data class SingleFont(val fontName: String) : FontSelectionResult()
    data class MixedFonts(val fontNames: Set<String>) : FontSelectionResult()
    object DefaultFont : FontSelectionResult()
}

/**
 * Font Mapping for WordprocessingML (DOCX XML)
 */
data class WordprocessingMLFonts(
    val ascii: String,
    val hAnsi: String,
    val cs: String,
    val eastAsia: String = ascii
)
