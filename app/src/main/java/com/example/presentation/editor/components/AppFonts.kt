package com.example.presentation.editor.components

import androidx.compose.ui.text.font.FontFamily
import com.example.presentation.editor.font.FontEngine
import com.example.presentation.editor.font.FontMainGroup
import com.example.presentation.editor.font.FontMetadata
import com.example.presentation.editor.font.FontSubCategory

// Re-export typealiases for backwards compatibility across existing UI files
typealias FontMainGroup = com.example.presentation.editor.font.FontMainGroup
typealias FontSubCategory = com.example.presentation.editor.font.FontSubCategory

data class FontItem(
    val name: String,
    val arabicName: String,
    val mainGroup: FontMainGroup,
    val subCategory: FontSubCategory,
    val description: String,
    val arabicPreview: String = "بسم الله الرحمن الرحيم — أ ب ج د 123",
    val englishPreview: String = "The quick brown fox jumps 123"
)

/**
 * AppFonts adapter bridging existing components to the new unified FontEngine.
 */
object AppFonts {

    val allFonts: List<FontItem>
        get() = FontEngine.getAllFonts().map { meta ->
            FontItem(
                name = meta.family,
                arabicName = meta.arabicName,
                mainGroup = meta.mainGroup,
                subCategory = meta.subCategory,
                description = meta.description,
                arabicPreview = meta.arabicPreview,
                englishPreview = meta.englishPreview
            )
        }

    fun getFontFamily(fontName: String): FontFamily {
        return FontEngine.getFontFamily(fontName)
    }
}
