package com.example.presentation.editor.font

import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.example.R
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Single Source of Truth Font Engine / Font Manager for the Document Editor.
 * Responsible for font discovery, registration, selection, style application,
 * BiDi/Mixed text font management, DOCX WordprocessingML rendering, and PDF export.
 */
object FontEngine {

    private val fontProvider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )

    private val fontCache = mutableMapOf<String, FontFamily>()
    private val recentFontsList = mutableListOf<String>()
    private val favoriteFontsSet = mutableSetOf<String>()
    private val importedFontsMap = mutableMapOf<String, FontMetadata>()
    private val downloadedFontsMap = mutableMapOf<String, File>()
    private val downloadingSet = mutableSetOf<String>()

    private var appContext: Context? = null
    private var onFontDownloadedListener: (() -> Unit)? = null

    fun setOnFontDownloadedListener(listener: (() -> Unit)?) {
        onFontDownloadedListener = listener
    }

    private val CDN_URL_MAP = mapOf(
        "Cairo" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/cairo/static/Cairo-Regular.ttf", "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/cairo/Cairo-Regular.ttf"),
        "Amiri" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/amiri/Amiri-Regular.ttf"),
        "Amiri Quran" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/amiriquran/AmiriQuran-Regular.ttf"),
        "Tajawal" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/tajawal/Tajawal-Regular.ttf"),
        "Almarai" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/almarai/Almarai-Regular.ttf"),
        "Aref Ruqaa" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/arefruqaa/ArefRuqaa-Regular.ttf"),
        "Aref Ruqaa Ink" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/arefruqaaink/ArefRuqaaInk-Regular.ttf"),
        "Reem Kufi" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/reemkufi/ReemKufi-Regular.ttf"),
        "Reem Kufi Ink" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/reemkufiink/ReemKufiInk-Regular.ttf"),
        "Lalezar" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/lalezar/Lalezar-Regular.ttf"),
        "Changa" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/changa/static/Changa-Regular.ttf", "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/changa/Changa-Regular.ttf"),
        "Lateef" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/lateef/Lateef-Regular.ttf"),
        "Scheherazade New" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/scheherazadenew/ScheherazadeNew-Regular.ttf"),
        "Markazi Text" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/markazitext/MarkaziText-Regular.ttf"),
        "Katibeh" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/katibeh/Katibeh-Regular.ttf"),
        "Rakkas" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/rakkas/Rakkas-Regular.ttf"),
        "El Messiri" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/elmessiri/static/ElMessiri-Regular.ttf", "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/elmessiri/ElMessiri-Regular.ttf"),
        "Marhey" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/marhey/static/Marhey-Regular.ttf", "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/marhey/Marhey-Regular.ttf"),
        "Readex Pro" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/readexpro/static/ReadexPro-Regular.ttf", "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/readexpro/ReadexPro-Regular.ttf"),
        "Noto Kufi Arabic" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/notokufiarabic/static/NotoKufiArabic-Regular.ttf", "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/notokufiarabic/NotoKufiArabic-Regular.ttf"),
        "Poppins" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/poppins/Poppins-Regular.ttf"),
        "Montserrat" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/montserrat/static/Montserrat-Regular.ttf"),
        "Open Sans" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/opensans/static/OpenSans-Regular.ttf"),
        "Lato" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/lato/Lato-Regular.ttf"),
        "Inter" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/inter/static/Inter-Regular.ttf"),
        "Lora" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/lora/static/Lora-Regular.ttf"),
        "Playfair Display" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/playfairdisplay/static/PlayfairDisplay-Regular.ttf"),
        "Merriweather" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/merriweather/Merriweather-Regular.ttf"),
        "Pacifico" to listOf("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/pacifico/Pacifico-Regular.ttf")
    )

    /**
     * Complete Registry of Curated Google Fonts, Office Fonts, and System Fonts
     */
    val builtInFonts: List<FontMetadata> = listOf(
        // ==========================================
        // 1. ARABIC FONTS (مجموعة الخط العربي)
        // ==========================================
        FontMetadata(
            id = "aref_ruqaa",
            family = "Aref Ruqaa",
            displayName = "Aref Ruqaa",
            arabicName = "عارف رقعة",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.RUQAA,
            description = "خط رقعة كلاسيكي رشيق وأصيل مناسب للرسائل والملاحظات والمراسلات اليدوية",
            arabicPreview = "بسم الله الرحمن الرحيم — رقعة أصيل رشيق",
            fallbackFamily = "casual"
        ),
        FontMetadata(
            id = "aref_ruqaa_ink",
            family = "Aref Ruqaa Ink",
            displayName = "Aref Ruqaa Ink",
            arabicName = "عارف رقعة حبر",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.RUQAA,
            description = "خط رقعة تراثي بمظهر الحبر اليدوي الفاخر للتوقيعات والشهادات",
            arabicPreview = "بسم الله الرحمن الرحيم — حبر يدوي فاخر",
            fallbackFamily = "casual"
        ),
        FontMetadata(
            id = "reem_kufi",
            family = "Reem Kufi",
            displayName = "Reem Kufi",
            arabicName = "ريم كوفي",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.KUFI,
            description = "خط كوفي هندسي متقن مثالي للعناوين والوثائق الرسمية والترويسات",
            arabicPreview = "بسم الله الرحمن الرحيم — كوفي هندسي متقن",
            fallbackFamily = "sans-serif-condensed"
        ),
        FontMetadata(
            id = "reem_kufi_ink",
            family = "Reem Kufi Ink",
            displayName = "Reem Kufi Ink",
            arabicName = "ريم كوفي حبر",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.KUFI,
            description = "خط كوفي فخم ومميز بلمسات كلاسيكية وانحناءات جمالية",
            arabicPreview = "بسم الله الرحمن الرحيم — كوفي حبر فخم",
            fallbackFamily = "sans-serif-condensed"
        ),
        FontMetadata(
            id = "noto_kufi_arabic",
            family = "Noto Kufi Arabic",
            displayName = "Noto Kufi Arabic",
            arabicName = "نوتو كوفي عربي",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.KUFI,
            description = "خط كوفي حديث وشديد الوضوح والتناسق للشاشات والطباعة",
            arabicPreview = "بسم الله الرحمن الرحيم — نوتو كوفي رسمي",
            fallbackFamily = "sans-serif-condensed"
        ),
        FontMetadata(
            id = "amiri",
            family = "Amiri",
            displayName = "Amiri",
            arabicName = "الأميري",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.NASKH_HERITAGE,
            description = "خط النسخ الطباعي الرفيع المعتمد للكتب والبحوث الأكاديمية والوثائق",
            arabicPreview = "بسم الله الرحمن الرحيم — خط النسخ الأكاديمي",
            fallbackFamily = "serif"
        ),
        FontMetadata(
            id = "amiri_quran",
            family = "Amiri Quran",
            displayName = "Amiri Quran",
            arabicName = "الأميري المصحفي",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.NASKH_HERITAGE,
            description = "خط نسخ قرآني كلاسيكي فائق الدقة والجمال للآيات والنصوص التراثية",
            arabicPreview = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ — نسخ مصحفي",
            fallbackFamily = "serif"
        ),
        FontMetadata(
            id = "scheherazade_new",
            family = "Scheherazade New",
            displayName = "Scheherazade New",
            arabicName = "شهرزاد الجديد",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.NASKH_HERITAGE,
            description = "خط تراثي مطبعي عريق مستلهم من الطراز العثماني الأصيل",
            arabicPreview = "بسم الله الرحمن الرحيم — طراز عثماني عريق",
            fallbackFamily = "serif"
        ),
        FontMetadata(
            id = "lateef",
            family = "Lateef",
            displayName = "Lateef",
            arabicName = "لطيف",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.NASKH_HERITAGE,
            description = "خط نسخ شامي ومغربي سلس ومريح للنصوص الطويلة والروايات",
            arabicPreview = "بسم الله الرحمن الرحيم — نسخ شامي سلس",
            fallbackFamily = "serif"
        ),
        FontMetadata(
            id = "markazi_text",
            family = "Markazi Text",
            displayName = "Markazi Text",
            arabicName = "مركزي",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.NASKH_HERITAGE,
            description = "خط مخصص للرسائل والكتب العلمية والمقالات الأكاديمية",
            arabicPreview = "بسم الله الرحمن الرحيم — مقالات وكتب علمية",
            fallbackFamily = "serif"
        ),
        FontMetadata(
            id = "lalezar",
            family = "Lalezar",
            displayName = "Lalezar",
            arabicName = "لاليزار",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.DECORATIVE,
            description = "خط عربي عريض ومزخرف بارز وممتلئ مخصص للإعلانات واللافتات والعناوين الكبرى",
            arabicPreview = "بسم الله الرحمن الرحيم — إعلانات ولافتات بارزة",
            fallbackFamily = "sans-serif-black"
        ),
        FontMetadata(
            id = "rakkas",
            family = "Rakkas",
            displayName = "Rakkas",
            arabicName = "رقاص",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.DECORATIVE,
            description = "خط مزخرف ذو حركة بصرية فنية بديعة وجذابة للبطاقات والأغلفة",
            arabicPreview = "بسم الله الرحمن الرحيم — حركة بصرية بديعة",
            fallbackFamily = "sans-serif-black"
        ),
        FontMetadata(
            id = "katibeh",
            family = "Katibeh",
            displayName = "Katibeh",
            arabicName = "كتيبة",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.DECORATIVE,
            description = "خط عربي تراثي مزخرف ومهيب للشعر والشهادات التقديرية",
            arabicPreview = "بسم الله الرحمن الرحيم — شهادات تقديرية وشعر",
            fallbackFamily = "sans-serif-black"
        ),
        FontMetadata(
            id = "el_messiri",
            family = "El Messiri",
            displayName = "El Messiri",
            arabicName = "المسيري",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.DECORATIVE,
            description = "خط فني مميز بانحناءات جمالية عصرية للعناوين الثقافية",
            arabicPreview = "بسم الله الرحمن الرحيم — انحناءات جمالية عصرية",
            fallbackFamily = "cursive"
        ),
        FontMetadata(
            id = "marhey",
            family = "Marhey",
            displayName = "Marhey",
            arabicName = "مرحي",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.DECORATIVE,
            description = "خط عربي تفاعلي مرح وعصري ومبهج للملصقات والمحتوى الاجتماعي",
            arabicPreview = "بسم الله الرحمن الرحيم — مرح وعصري وتفاعلي",
            fallbackFamily = "cursive"
        ),
        FontMetadata(
            id = "changa",
            family = "Changa",
            displayName = "Changa",
            arabicName = "تشانجا",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.MODERN_ARABIC,
            description = "خط عربي عريض متناسق للعناوين البارزة وتصميم العروض التقديمية",
            arabicPreview = "بسم الله الرحمن الرحيم — عناوين وعروض تقديمية",
            fallbackFamily = "sans-serif-black"
        ),
        FontMetadata(
            id = "mada",
            family = "Mada",
            displayName = "Mada",
            arabicName = "مدى",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.MODERN_ARABIC,
            description = "خط بسيط ومريح للعين في التصفح والقراءة الممتدة في المستندات",
            arabicPreview = "بسم الله الرحمن الرحيم — قراءة ممتدة ومريحة",
            fallbackFamily = "sans-serif-medium"
        ),
        FontMetadata(
            id = "harmattan",
            family = "Harmattan",
            displayName = "Harmattan",
            arabicName = "هرمطان",
            mainGroup = FontMainGroup.ARABIC,
            subCategory = FontSubCategory.MODERN_ARABIC,
            description = "خط عربي خفيف وأنيق وبسيط للنصوص الإدارية والمذكرات",
            arabicPreview = "بسم الله الرحمن الرحيم — خفيف وأنيق وبسيط",
            fallbackFamily = "sans-serif"
        ),

        // ==========================================
        // 2. ENGLISH / LATIN FONTS (مجموعة الخط الإنجليزي)
        // ==========================================
        FontMetadata(
            id = "roboto",
            family = "Roboto",
            displayName = "Roboto",
            arabicName = "روبوتو",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.SANS_SERIF,
            description = "Modern geometric sans-serif, standard Android typeface",
            englishPreview = "The quick brown fox jumps over the lazy dog 123",
            fallbackFamily = "sans-serif"
        ),
        FontMetadata(
            id = "poppins",
            family = "Poppins",
            displayName = "Poppins",
            arabicName = "بوبينز",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.SANS_SERIF,
            description = "Clean geometric sans-serif with balanced circles and curves",
            englishPreview = "Modern Clean Poppins Typography 2026",
            fallbackFamily = "sans-serif"
        ),
        FontMetadata(
            id = "montserrat",
            family = "Montserrat",
            displayName = "Montserrat",
            arabicName = "مونتسيرات",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.SANS_SERIF,
            description = "Modern urban geometric font inspired by classical posters",
            englishPreview = "Inspiring Architectural Typography Design",
            fallbackFamily = "sans-serif"
        ),
        FontMetadata(
            id = "open_sans",
            family = "Open Sans",
            displayName = "Open Sans",
            arabicName = "أوبن سانس",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.SANS_SERIF,
            description = "Humanist sans-serif with exceptional readability",
            englishPreview = "Friendly and neutral corporate communications",
            fallbackFamily = "sans-serif"
        ),
        FontMetadata(
            id = "lato",
            family = "Lato",
            displayName = "Lato",
            arabicName = "لاتو",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.SANS_SERIF,
            description = "Warm and sleek proportions designed for executive reports",
            englishPreview = "Executive Summary & Financial Analysis",
            fallbackFamily = "sans-serif"
        ),
        FontMetadata(
            id = "inter",
            family = "Inter",
            displayName = "Inter",
            arabicName = "إنتر",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.SANS_SERIF,
            description = "High-precision digital interface & document font",
            englishPreview = "Interface Precision & Data Documentation",
            fallbackFamily = "sans-serif"
        ),
        FontMetadata(
            id = "oswald",
            family = "Oswald",
            displayName = "Oswald",
            arabicName = "أوزوالد",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.SANS_SERIF,
            description = "Tight condensed display font for headlines and covers",
            englishPreview = "BOLD HEADLINE CONDENSED IMPACT",
            fallbackFamily = "sans-serif-condensed"
        ),
        FontMetadata(
            id = "times_new_roman",
            family = "Times New Roman",
            displayName = "Times New Roman",
            arabicName = "تايمز نيو رومان",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.SERIF_CLASSIC,
            description = "The world's most recognized formal academic and legal typeface",
            englishPreview = "Formal Legal Documentation & Research Papers",
            fallbackFamily = "serif"
        ),
        FontMetadata(
            id = "georgia",
            family = "Georgia",
            displayName = "Georgia",
            arabicName = "جورجيا",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.SERIF_CLASSIC,
            description = "Elegant high-contrast serif typeface designed for screen & print",
            englishPreview = "Classic Editorial & Published Manuscripts",
            fallbackFamily = "serif"
        ),
        FontMetadata(
            id = "garamond",
            family = "Garamond",
            displayName = "Garamond",
            arabicName = "غاراموند",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.SERIF_CLASSIC,
            description = "Timeless Renaissance book typography for literature",
            englishPreview = "Literary Novels & Historic Archives",
            fallbackFamily = "serif"
        ),
        FontMetadata(
            id = "lora",
            family = "Lora",
            displayName = "Lora",
            arabicName = "لورا",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.SERIF_CLASSIC,
            description = "Contemporary serif with brushed curves and poetic rhythm",
            englishPreview = "Memoirs, Poetry & Expressive Essays",
            fallbackFamily = "serif"
        ),
        FontMetadata(
            id = "playfair_display",
            family = "Playfair Display",
            displayName = "Playfair Display",
            arabicName = "بلاي فير ديسبلاي",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.SERIF_CLASSIC,
            description = "Luxury editorial serif with high contrast for titles",
            englishPreview = "Luxury Editorial Magazine & Titles",
            fallbackFamily = "serif"
        ),
        FontMetadata(
            id = "merriweather",
            family = "Merriweather",
            displayName = "Merriweather",
            arabicName = "ميريويذر",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.SERIF_CLASSIC,
            description = "Sturdy, legible serif engineered for long-form reading",
            englishPreview = "Comfortable long-form academic dissertation",
            fallbackFamily = "serif"
        ),
        FontMetadata(
            id = "consolas",
            family = "Consolas",
            displayName = "Consolas",
            arabicName = "كونسولاس",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.MONOSPACE,
            description = "Standard monospaced font for code snippets and tables",
            englishPreview = "const doc = new WordDocument(); // 123",
            fallbackFamily = "monospace"
        ),
        FontMetadata(
            id = "courier_new",
            family = "Courier New",
            displayName = "Courier New",
            arabicName = "كورييه نيو",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.MONOSPACE,
            description = "Classic typewriter fixed-width typography",
            englishPreview = "TYPEWRITER MEMORANDUM SPECIFICATION",
            fallbackFamily = "monospace"
        ),
        FontMetadata(
            id = "pacifico",
            family = "Pacifico",
            displayName = "Pacifico",
            arabicName = "باسيفيكو",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.DISPLAY_ARTISTIC,
            description = "Fun brush script inspired by 1950s American surf culture",
            englishPreview = "Creative Greeting & Signature Script",
            fallbackFamily = "cursive"
        ),
        FontMetadata(
            id = "impact",
            family = "Impact",
            displayName = "Impact",
            arabicName = "إمباكت",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.DISPLAY_ARTISTIC,
            description = "Ultra-bold condensed font for urgent notices and posters",
            englishPreview = "URGENT NOTICE & POSTER TITLE",
            fallbackFamily = "sans-serif-black"
        ),
        FontMetadata(
            id = "comic_sans",
            family = "Comic Sans MS",
            displayName = "Comic Sans MS",
            arabicName = "كوميك سانس",
            mainGroup = FontMainGroup.ENGLISH,
            subCategory = FontSubCategory.DISPLAY_ARTISTIC,
            description = "Casual, friendly handwriting style for informal notes",
            englishPreview = "Informal friendly notes and memos!",
            fallbackFamily = "casual"
        ),

        // ==========================================
        // 3. COMMON & OFFICE FONTS (مجموعة الخط المشترك)
        // ==========================================
        FontMetadata(
            id = "aptos",
            family = "Aptos",
            displayName = "Aptos",
            arabicName = "أبتوس (أوفيس 365)",
            mainGroup = FontMainGroup.COMMON,
            subCategory = FontSubCategory.OFFICE_STANDARD,
            description = "الخط الافتراضي العالمي الجديد لمايكروسوفت أوفيس 365 — متناسق وذكي ومريح",
            arabicPreview = "أوفيس 365 — وثائق الأعمال والتقارير العالمية",
            englishPreview = "Microsoft 365 Default Universal Typography",
            fallbackFamily = "sans-serif"
        ),
        FontMetadata(
            id = "calibri",
            family = "Calibri",
            displayName = "Calibri",
            arabicName = "كاليبري (أوفيس الافتراضي)",
            mainGroup = FontMainGroup.COMMON,
            subCategory = FontSubCategory.OFFICE_STANDARD,
            description = "الخط القياسي الأكثر شهرة واستخداماً في مايكروسوفت وورد حول العالم",
            arabicPreview = "مايكروسوفت وورد القياسي للمستندات والرسائل",
            englishPreview = "Microsoft Word Standard Global Document",
            fallbackFamily = "sans-serif"
        ),
        FontMetadata(
            id = "segoe_ui",
            family = "Segoe UI",
            displayName = "Segoe UI",
            arabicName = "سيجو يو آي (ويندوز)",
            mainGroup = FontMainGroup.COMMON,
            subCategory = FontSubCategory.OFFICE_STANDARD,
            description = "خط مايكروسوفت ويندوز المتناسق الحديث عالي الوضوح في المستندات",
            arabicPreview = "واجهات ويندوز والمستندات الذكية الحديثة",
            englishPreview = "Windows Modern System & Documents",
            fallbackFamily = "sans-serif"
        ),
        FontMetadata(
            id = "tahoma",
            family = "Tahoma",
            displayName = "Tahoma",
            arabicName = "تاهوما",
            mainGroup = FontMainGroup.COMMON,
            subCategory = FontSubCategory.OFFICE_STANDARD,
            description = "الخط المشترك الأوسع انتشاراً لشاشات ومستندات وورد ثنائية اللغة",
            arabicPreview = "خط الشاشات والمستندات المشترك شديد الوضوح",
            englishPreview = "Universal Dual-Script Clear Screen Typography",
            fallbackFamily = "sans-serif"
        ),
        FontMetadata(
            id = "arial",
            family = "Arial",
            displayName = "Arial",
            arabicName = "أريال القياسي",
            mainGroup = FontMainGroup.COMMON,
            subCategory = FontSubCategory.OFFICE_STANDARD,
            description = "الخط القياسي العالمي الموحد لجميع المنصات والأنظمة والوثائق",
            arabicPreview = "المعيار العالمي الموحد للوثائق والمراسلات",
            englishPreview = "Universal Standard Cross-Platform Typography",
            fallbackFamily = "sans-serif"
        ),
        FontMetadata(
            id = "cairo",
            family = "Cairo",
            displayName = "Cairo",
            arabicName = "كايرو المشترك",
            mainGroup = FontMainGroup.COMMON,
            subCategory = FontSubCategory.BILINGUAL_HARMONY,
            description = "الخط الهندسي الأكثر تناغماً وتطابقاً بين الحروف العربية والإنجليزية",
            arabicPreview = "تناغم تام بين العربية والإنجليزية في التقارير",
            englishPreview = "Perfect Arabic & Latin Geometric Harmony",
            fallbackFamily = "sans-serif"
        ),
        FontMetadata(
            id = "tajawal",
            family = "Tajawal",
            displayName = "Tajawal",
            arabicName = "تجوال المشترك",
            mainGroup = FontMainGroup.COMMON,
            subCategory = FontSubCategory.BILINGUAL_HARMONY,
            description = "خط عربي ولاتيني هندسي نقي وواضح للأعمال والتطبيقات والتقارير",
            arabicPreview = "تصميم هندسي نقي للشركات والمؤسسات",
            englishPreview = "Clean Geometric Corporate Typography",
            fallbackFamily = "sans-serif"
        ),
        FontMetadata(
            id = "almarai",
            family = "Almarai",
            displayName = "Almarai",
            arabicName = "المراعي المشترك",
            mainGroup = FontMainGroup.COMMON,
            subCategory = FontSubCategory.BILINGUAL_HARMONY,
            description = "خط عربي-لاتيني احترافي فائق المقروئية للمستندات الرسمية والشركات",
            arabicPreview = "مقروئية فائقة للمستندات والقرارات الرسمية",
            englishPreview = "High Legibility Corporate & Official Documents",
            fallbackFamily = "sans-serif"
        ),
        FontMetadata(
            id = "readex_pro",
            family = "Readex Pro",
            displayName = "Readex Pro",
            arabicName = "ريديكس برو المشترك",
            mainGroup = FontMainGroup.COMMON,
            subCategory = FontSubCategory.BILINGUAL_HARMONY,
            description = "خط تقني عصري متطور بمقروئية استثنائية لكافة أحجام الشاشات والطباعة",
            arabicPreview = "خط تقني متطور للأبحاث والمستندات الحديثة",
            englishPreview = "Advanced Tech Typography for Modern Layouts",
            fallbackFamily = "sans-serif"
        )
    )

    /**
     * Get all available fonts combining Built-in, System, and Imported Fonts
     */
    fun getAllFonts(): List<FontMetadata> {
        val list = mutableListOf<FontMetadata>()
        list.addAll(builtInFonts)
        list.addAll(importedFontsMap.values)
        return list
    }

    /**
     * Initialize FontEngine with App Context (loads imported fonts & local downloaded fonts)
     */
    fun initialize(context: Context) {
        try {
            appContext = context.applicationContext

            // 1. Scan custom imported fonts
            val customDir = File(context.filesDir, "custom_fonts")
            if (customDir.exists() && customDir.isDirectory) {
                customDir.listFiles { file -> file.extension.lowercase() in listOf("ttf", "otf") }?.forEach { fontFile ->
                    registerImportedFontFile(fontFile)
                }
            }

            // 2. Scan local downloaded fonts
            val downloadDir = File(context.filesDir, "downloaded_fonts")
            if (downloadDir.exists() && downloadDir.isDirectory) {
                downloadDir.listFiles { file -> file.extension.lowercase() in listOf("ttf", "otf") }?.forEach { fontFile ->
                    val normKey = fontFile.nameWithoutExtension.lowercase().replace("_", " ").replace("-", " ")
                    downloadedFontsMap[normKey] = fontFile
                }
            }

            // 3. Preload core Arabic Google Fonts in background
            preloadArabicFonts()
        } catch (e: Exception) {
            // Log/ignore errors during initialization
        }
    }

    private fun preloadArabicFonts() {
        val arabicFontsToPreload = listOf(
            "Cairo", "Amiri", "Tajawal", "Almarai", "Aref Ruqaa",
            "Reem Kufi", "Lalezar", "Changa", "Lateef", "Scheherazade New",
            "Readex Pro", "El Messiri", "Marhey", "Noto Kufi Arabic"
        )
        CoroutineScope(Dispatchers.IO).launch {
            for (font in arabicFontsToPreload) {
                downloadFontFileInternal(font)
            }
        }
    }

    fun ensureFontDownloaded(fontName: String) {
        val clean = fontName.trim()
        if (clean.isBlank()) return
        CoroutineScope(Dispatchers.IO).launch {
            downloadFontFileInternal(clean)
        }
    }

    private fun getDownloadedFontFile(fontName: String): File? {
        val cleanKey = fontName.trim().lowercase().replace("_", " ").replace("-", " ")
        downloadedFontsMap[cleanKey]?.let { if (it.exists()) return it }

        appContext?.let { ctx ->
            val dir = File(ctx.filesDir, "downloaded_fonts")
            val fileName = "${fontName.trim().lowercase().replace(" ", "_")}.ttf"
            val file = File(dir, fileName)
            if (file.exists() && file.length() > 2000) {
                downloadedFontsMap[cleanKey] = file
                return file
            }
        }
        return null
    }

    private fun downloadFontFileInternal(fontName: String): Boolean {
        val clean = fontName.trim()
        val normKey = clean.lowercase().replace("_", " ").replace("-", " ")

        if (downloadedFontsMap.containsKey(normKey) && downloadedFontsMap[normKey]?.exists() == true) {
            return true
        }

        synchronized(downloadingSet) {
            if (downloadingSet.contains(normKey)) return false
            downloadingSet.add(normKey)
        }

        try {
            val ctx = appContext ?: return false
            val dir = File(ctx.filesDir, "downloaded_fonts")
            if (!dir.exists()) dir.mkdirs()

            val fileName = "${clean.lowercase().replace(" ", "_")}.ttf"
            val outputFile = File(dir, fileName)

            val urls = mutableListOf<String>()
            CDN_URL_MAP[clean]?.let { urls.addAll(it) }

            val fontNoSpace = clean.replace(" ", "")
            val fontLowerNoSpace = clean.lowercase().replace(" ", "")
            urls.add("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/$fontLowerNoSpace/static/${fontNoSpace}-Regular.ttf")
            urls.add("https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/$fontLowerNoSpace/${fontNoSpace}-Regular.ttf")
            urls.add("https://raw.githubusercontent.com/google/fonts/main/ofl/$fontLowerNoSpace/static/${fontNoSpace}-Regular.ttf")
            urls.add("https://raw.githubusercontent.com/google/fonts/main/ofl/$fontLowerNoSpace/${fontNoSpace}-Regular.ttf")

            val success = downloadTtfFromUrls(urls, outputFile)
            if (success) {
                downloadedFontsMap[normKey] = outputFile
                fontCache.remove(clean)
                fontCache.remove(clean.lowercase())
                onFontDownloadedListener?.invoke()
                return true
            }
        } catch (e: Exception) {
            // Error
        } finally {
            synchronized(downloadingSet) {
                downloadingSet.remove(normKey)
            }
        }
        return false
    }

    private fun downloadTtfFromUrls(urls: List<String>, outputFile: File): Boolean {
        for (urlStr in urls) {
            try {
                var currentUrl = urlStr
                var redirectCount = 0
                while (redirectCount < 5) {
                    val url = URL(currentUrl)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 5000
                    conn.readTimeout = 8000
                    conn.instanceFollowRedirects = true
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0")

                    val responseCode = conn.responseCode
                    if (responseCode in 300..399) {
                        currentUrl = conn.getHeaderField("Location") ?: break
                        conn.disconnect()
                        redirectCount++
                    } else if (responseCode == HttpURLConnection.HTTP_OK) {
                        val inputStream = conn.inputStream
                        val tempFile = File(outputFile.parentFile, "${outputFile.name}.tmp")
                        FileOutputStream(tempFile).use { out ->
                            inputStream.copyTo(out)
                        }
                        conn.disconnect()
                        if (tempFile.length() > 2000) {
                            if (outputFile.exists()) outputFile.delete()
                            tempFile.renameTo(outputFile)
                            return true
                        } else {
                            tempFile.delete()
                        }
                        break
                    } else {
                        conn.disconnect()
                        break
                    }
                }
            } catch (e: Exception) {
                // Try next URL candidate
            }
        }
        return false
    }

    /**
     * Single Source of Truth for resolving a Compose [FontFamily]
     */
    fun getFontFamily(fontName: String): FontFamily {
        val cleanName = fontName.trim()
        if (cleanName.isBlank()) return FontFamily.SansSerif

        fontCache[cleanName]?.let { return it }

        val created = createFontFamily(cleanName)
        fontCache[cleanName] = created
        return created
    }

    private fun createFontFamily(name: String): FontFamily {
        val clean = name.trim()

        // 1. Check imported fonts
        importedFontsMap[clean]?.let { meta ->
            meta.customFontFile?.let { file ->
                try {
                    val typeface = android.graphics.Typeface.createFromFile(file)
                    return FontFamily(typeface)
                } catch (e: Exception) { }
            }
        }

        // 2. Match against registry
        val all = getAllFonts()
        val fontItem = all.find {
            it.family.equals(clean, ignoreCase = true) ||
            it.arabicName.equals(clean, ignoreCase = true) ||
            clean.contains(it.family, ignoreCase = true) ||
            it.family.contains(clean, ignoreCase = true)
        }

        val googleFontName = fontItem?.family ?: when (clean.lowercase()) {
            "عارف رقعة" -> "Aref Ruqaa"
            "عارف رقعة حبر" -> "Aref Ruqaa Ink"
            "ريم كوفي" -> "Reem Kufi"
            "ريم كوفي حبر" -> "Reem Kufi Ink"
            "نوتو كوفي عربي" -> "Noto Kufi Arabic"
            "الأميري" -> "Amiri"
            "الأميري المصحفي" -> "Amiri Quran"
            "شهرزاد الجديد" -> "Scheherazade New"
            "لطيف" -> "Lateef"
            "مركزي" -> "Markazi Text"
            "لاليزار" -> "Lalezar"
            "رقاص" -> "Rakkas"
            "كتيبة" -> "Katibeh"
            "المسيري" -> "El Messiri"
            "مرحي" -> "Marhey"
            "تشانجا" -> "Changa"
            "القاهرة", "كايرو" -> "Cairo"
            "تاجوال" -> "Tajawal"
            "المرعي" -> "Almarai"
            "ريديكس برو" -> "Readex Pro"
            else -> if (clean.matches(Regex("^[a-zA-Z0-9 ]+$"))) clean else null
        }

        // 3. Check locally downloaded fonts
        val downloadedFile = getDownloadedFontFile(clean) ?: googleFontName?.let { getDownloadedFontFile(it) }
        if (downloadedFile != null && downloadedFile.exists()) {
            try {
                val typeface = android.graphics.Typeface.createFromFile(downloadedFile)
                return FontFamily(typeface)
            } catch (e: Exception) { }
        }

        // 4. Fallback system typeface
        val fallbackFamilyName = fontItem?.fallbackFamily ?: when (clean.lowercase()) {
            "times new roman", "georgia", "garamond", "cambria", "traditional arabic", "arabic typesetting", "sakkal majalla", "andalus", "aldhabi", "الأميري", "نسخ", "تايمز نيو رومان" -> "serif"
            "courier new", "consolas", "lucida console", "fira code", "monospace", "كوريير" -> "monospace"
            "comic sans ms", "pacifico", "caveat", "dancing script", "رقعة", "مرحي" -> "casual"
            "lalezar", "لاليزار", "رقاص", "كتيبة", "كوفي", "تشانجا" -> "sans-serif-black"
            else -> "sans-serif"
        }

        val fallbackTypeface = android.graphics.Typeface.create(fallbackFamilyName, android.graphics.Typeface.NORMAL)
        val fallbackFontFamily = FontFamily(fallbackTypeface)

        // Trigger asynchronous download for local cache
        val fontToDownload = googleFontName ?: clean
        ensureFontDownloaded(fontToDownload)

        if (googleFontName != null) {
            try {
                val gFont = GoogleFont(googleFontName)
                return FontFamily(
                    androidx.compose.ui.text.googlefonts.Font(googleFont = gFont, fontProvider = fontProvider, weight = FontWeight.Normal),
                    androidx.compose.ui.text.googlefonts.Font(googleFont = gFont, fontProvider = fontProvider, weight = FontWeight.Bold),
                    androidx.compose.ui.text.googlefonts.Font(googleFont = gFont, fontProvider = fontProvider, weight = FontWeight.Medium),
                    androidx.compose.ui.text.googlefonts.Font(googleFont = gFont, fontProvider = fontProvider, weight = FontWeight.Light),
                    androidx.compose.ui.text.font.Font(DeviceFontFamilyName(fallbackFamilyName), weight = FontWeight.Normal)
                )
            } catch (e: Exception) {
                return fallbackFontFamily
            }
        }

        return fallbackFontFamily
    }

    /**
     * Get metadata for a font name
     */
    fun getFontMetadata(fontName: String): FontMetadata {
        val clean = fontName.trim()
        val found = getAllFonts().find {
            it.family.equals(clean, ignoreCase = true) ||
            it.arabicName.equals(clean, ignoreCase = true)
        }
        if (found != null) return found

        return FontMetadata(
            id = clean.lowercase().replace(" ", "_"),
            family = clean,
            displayName = clean,
            arabicName = clean,
            mainGroup = FontMainGroup.COMMON,
            subCategory = FontSubCategory.OFFICE_STANDARD,
            description = "خط مستندات قياسي"
        )
    }

    /**
     * Apply font cleanly to selection range (min..max) in AnnotatedString
     */
    fun applyFontToAnnotatedString(
        annotatedString: AnnotatedString,
        selection: TextRange,
        fontName: String
    ): AnnotatedString {
        val totalLen = annotatedString.length
        if (totalLen == 0) return annotatedString

        val resolvedFontFamily = getFontFamily(fontName)
        val builder = AnnotatedString.Builder()
        builder.append(annotatedString.text)

        val min = selection.min.coerceIn(0, totalLen)
        val max = selection.max.coerceIn(0, totalLen)

        val applyFull = selection.collapsed || (min == 0 && max == totalLen)

        if (applyFull) {
            // Apply over full text length
            annotatedString.spanStyles.forEach { range ->
                val s = range.start.coerceIn(0, totalLen)
                val e = range.end.coerceIn(0, totalLen)
                if (s < e) {
                    builder.addStyle(range.item.copy(fontFamily = resolvedFontFamily), s, e)
                }
            }
            builder.addStyle(SpanStyle(fontFamily = resolvedFontFamily), 0, totalLen)
        } else if (min < max) {
            // Apply specifically to [min..max] selection
            annotatedString.spanStyles.forEach { range ->
                val s = range.start.coerceIn(0, totalLen)
                val e = range.end.coerceIn(0, totalLen)
                if (s < e) {
                    val updatedStyle = if (s < max && e > min) {
                        range.item.copy(fontFamily = resolvedFontFamily)
                    } else {
                        range.item
                    }
                    builder.addStyle(updatedStyle, s, e)
                }
            }
            builder.addStyle(SpanStyle(fontFamily = resolvedFontFamily), min, max)
        }

        return builder.toAnnotatedString()
    }

    /**
     * Detect font(s) present in a selection range
     */
    fun detectFontInSelection(
        annotatedString: AnnotatedString,
        selection: TextRange
    ): FontSelectionResult {
        val totalLen = annotatedString.length
        if (totalLen == 0) return FontSelectionResult.DefaultFont

        val min = if (selection.collapsed) 0 else selection.min.coerceIn(0, totalLen)
        val max = if (selection.collapsed) totalLen else selection.max.coerceIn(0, totalLen)

        val fontsInSelection = mutableSetOf<String>()

        annotatedString.spanStyles.forEach { range ->
            if (range.start < max && range.end > min && range.item.fontFamily != null) {
                // Try to identify font family name
                val name = range.item.fontFamily.toString()
                fontsInSelection.add(name)
            }
        }

        return when {
            fontsInSelection.isEmpty() -> FontSelectionResult.DefaultFont
            fontsInSelection.size == 1 -> FontSelectionResult.SingleFont(fontsInSelection.first())
            else -> FontSelectionResult.MixedFonts(fontsInSelection)
        }
    }

    /**
     * Get WordprocessingML fonts mapping for DOCX w:rFonts
     */
    fun getWordprocessingMLFonts(fontName: String): WordprocessingMLFonts {
        val meta = getFontMetadata(fontName)
        val family = meta.family

        // For Arabic/Complex Script fonts, ensure cs = family
        return WordprocessingMLFonts(
            ascii = family,
            hAnsi = family,
            cs = family,
            eastAsia = family
        )
    }

    /**
     * Add to Recently Used Fonts
     */
    fun addRecentFont(fontName: String) {
        val clean = fontName.trim()
        if (clean.isBlank()) return
        recentFontsList.remove(clean)
        recentFontsList.add(0, clean)
        if (recentFontsList.size > 10) {
            recentFontsList.removeAt(recentFontsList.size - 1)
        }
    }

    /**
     * Get Recently Used Fonts
     */
    fun getRecentFonts(): List<FontMetadata> {
        return recentFontsList.map { getFontMetadata(it) }
    }

    /**
     * Toggle Favorite Font
     */
    fun toggleFavoriteFont(fontName: String) {
        val clean = fontName.trim()
        if (favoriteFontsSet.contains(clean)) {
            favoriteFontsSet.remove(clean)
        } else {
            favoriteFontsSet.add(clean)
        }
    }

    /**
     * Register an imported .ttf or .otf font file
     */
    private fun registerImportedFontFile(fontFile: File): FontMetadata? {
        return try {
            val nameWithoutExt = fontFile.nameWithoutExtension.replace("_", " ").replace("-", " ")
            val meta = FontMetadata(
                id = "imported_${fontFile.nameWithoutExtension}",
                family = nameWithoutExt,
                displayName = nameWithoutExt,
                arabicName = nameWithoutExt,
                mainGroup = FontMainGroup.IMPORTED,
                subCategory = FontSubCategory.BILINGUAL_HARMONY,
                description = "خط مستورد من ملف (.${fontFile.extension})",
                source = FontSource.USER_IMPORTED,
                customFontFile = fontFile
            )
            importedFontsMap[nameWithoutExt] = meta
            meta
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Import a custom font file from Uri
     */
    fun importFontFromUri(context: Context, uri: Uri): Result<FontMetadata> {
        return try {
            val contentResolver = context.contentResolver
            val fileName = "font_${System.currentTimeMillis()}.ttf"
            val customDir = File(context.filesDir, "custom_fonts")
            if (!customDir.exists()) customDir.mkdirs()

            val destFile = File(customDir, fileName)
            contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val meta = registerImportedFontFile(destFile)
            if (meta != null) {
                Result.success(meta)
            } else {
                Result.failure(Exception("Could not parse font file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper to create Android native Typeface for Canvas / PDF Paint
     */
    fun getNativeTypeface(fontName: String, isBold: Boolean, isItalic: Boolean): android.graphics.Typeface {
        val clean = fontName.trim()
        val meta = getFontMetadata(clean)
        
        if (meta.customFontFile != null && meta.customFontFile.exists()) {
            try {
                return android.graphics.Typeface.createFromFile(meta.customFontFile)
            } catch (e: Exception) {}
        }

        val downloadedFile = getDownloadedFontFile(clean) ?: getDownloadedFontFile(meta.family)
        if (downloadedFile != null && downloadedFile.exists()) {
            try {
                return android.graphics.Typeface.createFromFile(downloadedFile)
            } catch (e: Exception) {}
        }

        val style = if (isBold && isItalic) android.graphics.Typeface.BOLD_ITALIC
        else if (isBold) android.graphics.Typeface.BOLD
        else if (isItalic) android.graphics.Typeface.ITALIC
        else android.graphics.Typeface.NORMAL

        return android.graphics.Typeface.create(meta.fallbackFamily, style)
    }
}
