package com.example.presentation.templates

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.presentation.editor.*
import java.util.UUID

enum class TemplateCategory(val title: String) {
    ALL("الكل"),
    RESUME("السير الذاتية"),
    CERTIFICATE("الشهادات والتقدير"),
    RESEARCH("الأبحاث والمذكرات"),
    LETTER("الخطابات والمراسلات"),
    BUSINESS("الأعمال والفواتير")
}

enum class TemplatePreviewType {
    BLANK,
    RESUME_SIDEBAR,
    RESUME_HEADER,
    CERTIFICATE_GOLD,
    CERTIFICATE_MODERN,
    RESEARCH_PAPER,
    STUDY_NOTE,
    FORMAL_LETTER,
    COVER_LETTER,
    BUSINESS_REPORT,
    INVOICE
}

data class TemplateItem(
    val id: String,
    val title: String,
    val category: TemplateCategory,
    val description: String,
    val previewType: TemplatePreviewType,
    val primaryColor: Color,
    val secondaryColor: Color = Color(0xFFE2E8F0),
    val accentColor: Color = Color(0xFF185ABD),
    val isLandscape: Boolean = false,
    val badgeText: String? = null,
    val pageBorder: PageBorder = PageBorder(),
    val pageColor: Color = Color.White,
    val pageOrientation: PageOrientation = PageOrientation.PORTRAIT,
    val pageSize: PageSize = PageSize.A4,
    val pageMargin: PageMargin = PageMargin.NORMAL,
    val stripeStyle: PageStripeStyle = PageStripeStyle.NONE,
    val generateBlocks: () -> List<DocumentBlock>
)

object DocumentTemplatesRepository {

    val templates: List<TemplateItem> = listOf(
        // 1. Blank
        TemplateItem(
            id = "tpl_blank",
            title = "مستند فارغ",
            category = TemplateCategory.ALL,
            description = "مستند جديد خالي تماماً للبدء من الصفر",
            previewType = TemplatePreviewType.BLANK,
            primaryColor = Color(0xFF185ABD),
            stripeStyle = PageStripeStyle.NONE,
            generateBlocks = {
                listOf(
                    TextBlock("blk_${UUID.randomUUID()}", TextFieldValue("ابدأ بكتابة مستندك هنا..."))
                )
            }
        ),

        // 2. Modern Executive Resume (CV)
        TemplateItem(
            id = "tpl_cv_modern",
            title = "سيرة ذاتية عصرية",
            category = TemplateCategory.RESUME,
            description = "تصميم حديث بشريط جانبي ملون لعرض الخبرات والمهارات باحترافية",
            previewType = TemplatePreviewType.RESUME_SIDEBAR,
            primaryColor = Color(0xFF0F766E), // Deep Teal
            secondaryColor = Color(0xFFCCFBF1),
            accentColor = Color(0xFF14B8A6),
            badgeText = "شائع",
            stripeStyle = PageStripeStyle.SIDE_BAR_RIGHT,
            generateBlocks = {
                listOf(
                    BannerBlock(
                        id = "blk_${UUID.randomUUID()}",
                        title = "محمد عبدالله السعيد",
                        subtitle = "مطور برمجيات أول | مهندس نظم\nالرياض، المملكة العربية السعودية | +966 50 123 4567 | email@example.com",
                        backgroundColor = Color(0xFF0F766E),
                        textColor = Color.White,
                        alignment = TextAlignment.RIGHT
                    ),
                    DividerBlock(id = "blk_${UUID.randomUUID()}", color = Color(0xFF0F766E), thicknessDp = 2f),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F766E))) {
                                    append("━━━  الملف الشخصي  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                                }
                                append("مهندس برمجيات ذو خبرة تزيد عن 6 سنوات في تطوير التطبيقات والحلول الرقمية وإدارة قواعد البيانات، شغوف بالابتكار وتحسين كفاءة الأنظمة السحابية.")
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    ),
                    DividerBlock(id = "blk_${UUID.randomUUID()}", color = Color(0xFFCBD5E1)),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F766E))) {
                                    append("━━━  الخبرات المهنية  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                                }
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp)) {
                                    append("• كبير المطورين - شركة التقنية المتقدمة (2021 - حتى الآن)\n")
                                }
                                append("  - قيادة فريق تطوير تطبيقات الأندرويد والخدمات السحابية.\n")
                                append("  - تحسين سرعة استجابة النظام بنسبة 40% وإدارة أمان البيانات.\n\n")

                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp)) {
                                    append("• مهندس برمجيات - مؤسسة الحلول الرقمية (2018 - 2021)\n")
                                }
                                append("  - بناء وتطوير واجهات المستخدم وتكامل واجهات البرمجة RESTful APIs.")
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    ),
                    DividerBlock(id = "blk_${UUID.randomUUID()}", color = Color(0xFFCBD5E1)),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F766E))) {
                                    append("━━━  المؤهلات العلمية  ━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                                }
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp)) {
                                    append("• بكالوريوس علوم الحاسب والمعلومات (مرتبة الشرف)\n")
                                }
                                append("  جامعة الملك سعود | سنة التخرج: 2018")
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    ),
                    CalloutBlock(
                        id = "blk_${UUID.randomUUID()}",
                        title = "المهارات التقنية واللغات",
                        text = TextFieldValue("• Kotlin, Jetpack Compose, Android Architecture Components\n• Cloud Services, Firebase, SQL, Clean Architecture\n• إدارة المشاريع و Git & CI/CD Pipelines"),
                        backgroundColor = Color(0xFFCCFBF1),
                        borderColor = Color(0xFF0F766E),
                        textColor = Color(0xFF0F766E)
                    )
                )
            }
        ),

        // 3. Technical & Creative Resume
        TemplateItem(
            id = "tpl_cv_tech",
            title = "سيرة ذاتية تقنية",
            category = TemplateCategory.RESUME,
            description = "تنسيق أنيق مع شريط علوي بارز مخصص للمصممين والمهندسين",
            previewType = TemplatePreviewType.RESUME_HEADER,
            primaryColor = Color(0xFF4338CA), // Indigo
            secondaryColor = Color(0xFFE0E7FF),
            accentColor = Color(0xFF6366F1),
            stripeStyle = PageStripeStyle.RESUME_HEADER_BAND,
            generateBlocks = {
                listOf(
                    BannerBlock(
                        id = "blk_${UUID.randomUUID()}",
                        title = "سارة أحمد المنصور",
                        subtitle = "مصممة واجهات وتجربة المستخدم (UI/UX Designer)\nportfolio.example.com | الهاتف: +966 55 987 6543",
                        backgroundColor = Color(0xFF4338CA),
                        textColor = Color.White,
                        alignment = TextAlignment.RIGHT
                    ),
                    DividerBlock(id = "blk_${UUID.randomUUID()}", color = Color(0xFF4338CA), thicknessDp = 2f),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF4338CA))) {
                                    append("◆ نبذة مهنية\n")
                                }
                                append("مصممة واجهات مستخدم متخصصة في ابتكار تجارب تفاعلية ممتعة للمستخدمين، وتطبيق أفضل معايير Material Design 3 وتصميم النظم الرقمية الحديثة.")
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    ),
                    DividerBlock(id = "blk_${UUID.randomUUID()}", color = Color(0xFFE0E7FF)),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF4338CA))) {
                                    append("◆ أبرز المشاريع والإنجازات\n")
                                }
                                append("• إعادة تصميم تطبيق الخدمات البنكية (أكثر من 500 ألف مستخدم نشط).\n")
                                append("• إنشاء مكتبة متكاملة لنظام التصميم (Design System) تدعم اللغتين العربية والإنجليزية.")
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    ),
                    DividerBlock(id = "blk_${UUID.randomUUID()}", color = Color(0xFFE0E7FF)),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF4338CA))) {
                                    append("◆ التعليم والشهادات الاحترافية\n")
                                }
                                append("• شهادة Google Professional UX Design Certificate\n")
                                append("• بكالوريوس تصميم تفاعلي وجرافيك - جامعة الإمام (2020)")
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    ),
                    CalloutBlock(
                        id = "blk_${UUID.randomUUID()}",
                        title = "الأدوات والمهارات الفنية",
                        text = TextFieldValue("Figma, Adobe XD, Design Tokens, Prototyping, Wireframing, User Testing"),
                        backgroundColor = Color(0xFFE0E7FF),
                        borderColor = Color(0xFF4338CA),
                        textColor = Color(0xFF3730A3)
                    )
                )
            }
        ),

        // 4. Golden Appreciation Certificate (Landscape)
        TemplateItem(
            id = "tpl_cert_appreciation",
            title = "شهادة شكر وتقدير فخمة",
            category = TemplateCategory.CERTIFICATE,
            description = "شهادة تقديرية مذهبة بإطار ملكي مزدوج جاهزة للتكريم والمناسبات",
            previewType = TemplatePreviewType.CERTIFICATE_GOLD,
            primaryColor = Color(0xFFB45309), // Amber Gold
            secondaryColor = Color(0xFFFEF3C7),
            accentColor = Color(0xFFD97706),
            isLandscape = true,
            badgeText = "مميز",
            pageOrientation = PageOrientation.LANDSCAPE,
            stripeStyle = PageStripeStyle.CERTIFICATE_GOLD,
            pageBorder = PageBorder(
                setting = BorderSetting.BOX,
                style = BorderStyle.DOUBLE,
                color = Color(0xFFB45309),
                widthPt = 3.0f
            ),
            generateBlocks = {
                listOf(
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF92400E))) {
                                    append("المملكة العربية السعودية  •  وزارة التعليم  •  مؤسسة التميز والابتكار")
                                }
                            }
                        ),
                        alignment = TextAlignment.CENTER
                    ),
                    BannerBlock(
                        id = "blk_${UUID.randomUUID()}",
                        title = "★ شـهـادة شـكـر وتـقـديـر ★",
                        subtitle = "CERTIFICATE OF APPRECIATION",
                        backgroundColor = Color(0xFFB45309),
                        textColor = Color.White,
                        alignment = TextAlignment.CENTER
                    ),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontSize = 17.sp, color = Color(0xFF4B5563))) {
                                    append("يسر إدارة المؤسسة أن تمنح هذه الشهادة بفخر واعتزاز إلى الفاضل/ة:\n\n")
                                }
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color(0xFF1E293B))) {
                                    append("[ اسـم المـكـرم / المـكـرمـة هـنـا ]\n\n")
                                }
                                withStyle(SpanStyle(fontSize = 16.sp, color = Color(0xFF374151))) {
                                    append("تقديراً للجهود المتميزة، والإخلاص والتفاني في أداء العمل وتحقيق أعلى معايير الجودة والإبداع خلال العام.\n")
                                    append("متمنين له/ا دوام التوفيق والنجاح ومزيداً من العطاء والتميز.")
                                }
                            }
                        ),
                        alignment = TextAlignment.CENTER
                    ),
                    DividerBlock(id = "blk_${UUID.randomUUID()}", color = Color(0xFFB45309), thicknessDp = 1.5f),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))) {
                                    append("تحريراً في: ___ / ___ / 2026 م                                    المدير العام للمؤسسة:\n")
                                    append("الختم الرسمي: [ .................... ]                                    التوقيع: [ .................... ]")
                                }
                            }
                        ),
                        alignment = TextAlignment.CENTER
                    )
                )
            }
        ),

        // 5. Training Completion Certificate
        TemplateItem(
            id = "tpl_cert_completion",
            title = "شهادة إتمام دورة تدريبية",
            category = TemplateCategory.CERTIFICATE,
            description = "شهادة اجتياز تدريبية حديثة بألوان كحلية وذهبية مع كود الاعتماد",
            previewType = TemplatePreviewType.CERTIFICATE_MODERN,
            primaryColor = Color(0xFF1E3A8A), // Royal Blue
            secondaryColor = Color(0xFFDBEAFE),
            accentColor = Color(0xFF2563EB),
            isLandscape = true,
            pageOrientation = PageOrientation.LANDSCAPE,
            stripeStyle = PageStripeStyle.TOP_BAR,
            pageBorder = PageBorder(
                setting = BorderSetting.BOX,
                style = BorderStyle.SOLID,
                color = Color(0xFF1E3A8A),
                widthPt = 2.0f
            ),
            generateBlocks = {
                listOf(
                    BannerBlock(
                        id = "blk_${UUID.randomUUID()}",
                        title = "أكاديمية التدريب والتطوير المهني المتقدم",
                        subtitle = "شهادة إنجاز واجتياز دورة تدريبية  •  CERTIFICATE OF COMPLETION",
                        backgroundColor = Color(0xFF1E3A8A),
                        textColor = Color.White,
                        alignment = TextAlignment.CENTER
                    ),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontSize = 17.sp)) {
                                    append("تشهد الأكاديمية بأن المتدرب/ـة:\n\n")
                                }
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color(0xFF0F172A))) {
                                    append("[ اسـم المـتـدرب الكـامـل ]\n\n")
                                }
                                withStyle(SpanStyle(fontSize = 17.sp)) {
                                    append("قد أتم بنجاح متطلبات البرنامج التدريبي المكثف بعنوان:\n")
                                }
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1E3A8A))) {
                                    append("« تطوير التطبيقات والحلول الرقمية المتقدمة »\n")
                                }
                                withStyle(SpanStyle(fontSize = 15.sp, color = Color(0xFF475569))) {
                                    append("بمعدل (40 ساعة تدريبية معتمدة) في الفترة من 01/08/2026 إلى 25/08/2026")
                                }
                            }
                        ),
                        alignment = TextAlignment.CENTER
                    ),
                    CalloutBlock(
                        id = "blk_${UUID.randomUUID()}",
                        title = "معلومات الاعتماد والتحقق الرسمي",
                        text = TextFieldValue("رقم الاعتماد الأكاديمي: CERT-2026-88942                                المشرف الأكاديمي: د. أحمد القحطاني\nرابط التحقق: verify.academy.org/cert/88942                             التوقيع: ____________________"),
                        backgroundColor = Color(0xFFDBEAFE),
                        borderColor = Color(0xFF1E3A8A),
                        textColor = Color(0xFF1E3A8A)
                    )
                )
            }
        ),

        // 6. Academic Research Paper
        TemplateItem(
            id = "tpl_research_paper",
            title = "بحث جامعي / ورقة علمية",
            category = TemplateCategory.RESEARCH,
            description = "هيكل قياسي للأبحاث والرسائل العلمية يتضمن المستخلص، المنهجية، والمراجع",
            previewType = TemplatePreviewType.RESEARCH_PAPER,
            primaryColor = Color(0xFF1E293B), // Slate
            secondaryColor = Color(0xFFF1F5F9),
            accentColor = Color(0xFF3B82F6),
            badgeText = "أكاديمي",
            stripeStyle = PageStripeStyle.NONE,
            generateBlocks = {
                listOf(
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF475569))) {
                                    append("جامعة الملك سعود  |  كلية علوم الحاسب والمعلومات  |  قسم تقنية المعلومات\n\n")
                                }
                                withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF0F172A))) {
                                    append("أثر استخدام تقنيات الذكاء الاصطناعي في تحسين أداء تطبيقات الهواتف الذكية\n\n")
                                }
                                withStyle(SpanStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))) {
                                    append("إعداد الباحث: [اسم الباحث]  |  إشراف الأستاذ الدكتور: [اسم المشرف]\n")
                                    append("تاريخ التقديم: أغسطس 2026 م")
                                }
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    ),
                    CalloutBlock(
                        id = "blk_${UUID.randomUUID()}",
                        title = "المستخلص (Abstract)",
                        text = TextFieldValue("تهدف هذه الدراسة إلى بحث وتقييم فاعلية إدماج نماذج الذكاء الاصطناعي الحديثة في تحسين تجربة المستخدم وسرعة معالجة البيانات على الأجهزة الطرفية. اعتمد البحث على منهج تجريبي بمقارنة الأداء واستهلاك الذاكرة قبل وبعد إدماج النماذج الذكية. أظهرت النتائج زيادة ملحوظة في الكفاءة بنسبة 35%."),
                        backgroundColor = Color(0xFFF1F5F9),
                        borderColor = Color(0xFF3B82F6),
                        textColor = Color(0xFF1E293B)
                    ),
                    DividerBlock(id = "blk_${UUID.randomUUID()}", color = Color(0xFFCBD5E1)),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))) {
                                    append("1. المقدمة وأهمية البحث\n")
                                }
                                append("مع التطور المتسارع في المنظومات الذكية، أصبحت التطبيقات بحاجة لمعالجة البيانات التفاعلية في الوقت الفعلي. يستعرض هذا البحث الإطار النظري والتطبيقي للأدوات المستحدثة...\n\n")

                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))) {
                                    append("2. منهجية البحث وجمع البيانات\n")
                                }
                                append("تم تطبيق الدراسة على عينة معيارية تضم أكثر من 100 اختبار أداء ومقارنة معدلات الاستجابة...\n\n")

                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))) {
                                    append("3. المراجع (References)\n")
                                }
                                append("1. السعيد، محمد (2024). النظم الذكية وهندسة البرمجيات، دار النشر العلمي.\n")
                                append("2. Smith, J. & Davis, K. (2025). Mobile AI Architectures, IEEE Transactions.")
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    )
                )
            }
        ),

        // 7. Study Notes & Summary
        TemplateItem(
            id = "tpl_study_notes",
            title = "مذكرة دراسية وتلخيص",
            category = TemplateCategory.RESEARCH,
            description = "قالب منسق مع مساحات للمفاهيم الأساسية، الملاحظات الجانبية، ونقاط المراجعة",
            previewType = TemplatePreviewType.STUDY_NOTE,
            primaryColor = Color(0xFFC2410C), // Orange Amber
            secondaryColor = Color(0xFFFFEDD5),
            accentColor = Color(0xFFEA580C),
            stripeStyle = PageStripeStyle.SIDE_BAR_LEFT,
            generateBlocks = {
                listOf(
                    BannerBlock(
                        id = "blk_${UUID.randomUUID()}",
                        title = "مذكرة دراسية وتلخيص شامل",
                        subtitle = "المادة: هندسة البرمجيات  |  المحاضرة: 04  |  التاريخ: 28/08/2026",
                        backgroundColor = Color(0xFFC2410C),
                        textColor = Color.White,
                        alignment = TextAlignment.RIGHT
                    ),
                    CalloutBlock(
                        id = "blk_${UUID.randomUUID()}",
                        title = "■ المفاهيم والمصطلحات الجوهرية (Key Concepts)",
                        text = TextFieldValue("1. المفهوم الأول: [اكتب التعريف الدقيق والشرح المختصر هنا]\n2. المفهوم الثاني: [اكتب التعريف الدقيق والشرح المختصر هنا]"),
                        backgroundColor = Color(0xFFFFEDD5),
                        borderColor = Color(0xFFC2410C),
                        textColor = Color(0xFF9A3412)
                    ),
                    DividerBlock(id = "blk_${UUID.randomUUID()}", color = Color(0xFFC2410C)),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFC2410C))) {
                                    append("■ الشرح التفصيلي والنقاط الهامة:\n")
                                }
                                append("• النقطة الأساسية 1: شرح تفصيلي مع أمثلة عملية توضيحية.\n")
                                append("• النقطة الأساسية 2: الفروقات الجوهرية والمقارنات مع الحالات السابقة.\n")
                                append("• النقطة الأساسية 3: المعادلات والقوانين الواجب حفظها.")
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    ),
                    CalloutBlock(
                        id = "blk_${UUID.randomUUID()}",
                        title = "■ أسئلة مراجعة متوقعة للاختبار",
                        text = TextFieldValue("؟ السؤال الأول المتوقع: _________________________________\n؟ السؤال الثاني المتوقع: _________________________________"),
                        backgroundColor = Color(0xFFFEF3C7),
                        borderColor = Color(0xFFD97706),
                        textColor = Color(0xFF92400E)
                    )
                )
            }
        ),

        // 8. Formal Business Letter
        TemplateItem(
            id = "tpl_formal_letter",
            title = "خطاب رسمي للشركات والمؤسسات",
            category = TemplateCategory.LETTER,
            description = "ترويسة وشريط رسمي علوي متقن للمراسلات الإدارية والخطابات الحكومية",
            previewType = TemplatePreviewType.FORMAL_LETTER,
            primaryColor = Color(0xFF1E3A8A), // Corporate Navy
            secondaryColor = Color(0xFFEFF6FF),
            accentColor = Color(0xFF3B82F6),
            badgeText = "رسمي",
            stripeStyle = PageStripeStyle.LETTERHEAD_HEADER,
            generateBlocks = {
                listOf(
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E3A8A))) {
                                    append("شركة الرواد للحلول التقنية والاستثمار\n")
                                }
                                withStyle(SpanStyle(fontSize = 13.sp, color = Color(0xFF64748B))) {
                                    append("الرقم المرجعي: رواد/2026/1084                                التاريخ: 28/08/2026 م\n")
                                    append("المرفقات: (2) ملفات توضيحية")
                                }
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    ),
                    DividerBlock(id = "blk_${UUID.randomUUID()}", color = Color(0xFF1E3A8A), thicknessDp = 1.5f),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))) {
                                    append("سعادة الأستاذ / [ اسم المستلم المحترم ]\n")
                                    append("[ المسمى الوظيفي / اسم الشركة أو الجهة ]")
                                }
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    ),
                    CalloutBlock(
                        id = "blk_${UUID.randomUUID()}",
                        title = "الموضوع",
                        text = TextFieldValue("[ كتابة موضوع الخطاب الرسمي هنا بشكل واضح وموجز ]"),
                        backgroundColor = Color(0xFFEFF6FF),
                        borderColor = Color(0xFF1E3A8A),
                        textColor = Color(0xFF1E3A8A)
                    ),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontSize = 16.sp)) {
                                    append("السلام عليكم ورحمة الله وبركاته،، وبعد،،\n\n")
                                    append("يطيب لنا في البداية أن نهديكم أطيب التحيات والتقدير، متمنين لكم ولمؤسستكم الموقرة دوام التوفيق والازدهار.\n\n")
                                    append("إشارة إلى الموضوع الموضح أعلاه، نود إحاطة سعادتكم علماً بأننا [اكتب تفاصيل الطلب أو البيان الرسمي هنا مع التوضيح الكامل للغرض والمقترحات المطلوبة]...\n\n")
                                    append("شاكرين لكم حسن تعاونكم الدائم واهتمامكم الكريم، ونتطلع إلى تعزيز سبل التعاون المثمر بيننا.\n\n")
                                    append("وتفضلوا بقبول فائق التحية والاحترام والتقدير،،،")
                                }
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    ),
                    DividerBlock(id = "blk_${UUID.randomUUID()}", color = Color(0xFFCBD5E1)),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))) {
                                    append("المرسل: [اسم المسؤول المفوض]\n")
                                    append("الصفة: الرئيس التنفيذي\n")
                                    append("التوقيع والختم الرسمي: __________________")
                                }
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    )
                )
            }
        ),

        // 9. Cover Letter
        TemplateItem(
            id = "tpl_cover_letter",
            title = "خطاب تقديم للوظائف (Cover Letter)",
            category = TemplateCategory.LETTER,
            description = "خطاب تعريفي احترافي موجه لإدارة الموارد البشرية والشركات",
            previewType = TemplatePreviewType.COVER_LETTER,
            primaryColor = Color(0xFF0369A1), // Sky Blue
            secondaryColor = Color(0xFFE0F2FE),
            accentColor = Color(0xFF0284C7),
            stripeStyle = PageStripeStyle.LETTERHEAD_HEADER,
            generateBlocks = {
                listOf(
                    BannerBlock(
                        id = "blk_${UUID.randomUUID()}",
                        title = "خالد عبدالكريم المنصور",
                        subtitle = "الهاتف: +966 54 000 1122 | البريد: khaled@example.com | الرياض",
                        backgroundColor = Color(0xFF0369A1),
                        textColor = Color.White,
                        alignment = TextAlignment.RIGHT
                    ),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                                    append("إلى: لجنة التوظيف واستقطاب الكفاءات\n")
                                    append("شركة: [اسم الشركة المتقدم إليها]\n")
                                    append("بشأن: التقديم على وظيفة [المسمى الوظيفي المعلن عنه]")
                                }
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    ),
                    DividerBlock(id = "blk_${UUID.randomUUID()}", color = Color(0xFF0369A1)),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontSize = 16.sp)) {
                                    append("تحية طيبة وبعد،،\n\n")
                                    append("يسعدني التقدم بطلب الانضمام إلى فريق عملكم المتميز للعمل كـ [المسمى الوظيفي]. نظراً لما تتمتع به مؤسستكم من سمعة رائدة في هذا المجال، أرى في خبراتي ومهاراتي إضافة نوعية تسهم في تحقيق أهدافكم.\n\n")
                                    append("خلال مسيرتي المهنية، نجحت في [أذكر أهم إنجازين مرتبطين بالوظيفة، مثل: قيادة مشاريع تقنية أو زيادة المبيعات بنسبة معينة]. بالإضافة إلى ذلك، أتمتع بمهارات تواصل وحل مشكلات عالية.\n\n")
                                    append("مرفق لكم سيرتي الذاتية التفصيلية للاطلاع. ويسعدني إجراء مقابلة شخصية لمناقشة كيفية تسخير خبراتي لخدمة أهداف الشركة.\n\n")
                                    append("وتفضلوا بقبول خالص الشكر والتقدير،،\n\n")
                                }
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                                    append("مقدم الطلب: خالد عبدالكريم المنصور")
                                }
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    )
                )
            }
        ),

        // 10. Commercial Service Invoice
        TemplateItem(
            id = "tpl_invoice",
            title = "فاتورة مبيعات وخدمات تجارية",
            category = TemplateCategory.BUSINESS,
            description = "قالب فاتورة مالية متكامل بجدول حسابات، الضرائب، وحساب الإجمالي",
            previewType = TemplatePreviewType.INVOICE,
            primaryColor = Color(0xFF047857), // Emerald Green
            secondaryColor = Color(0xFFD1FAE5),
            accentColor = Color(0xFF059669),
            badgeText = "مالي",
            stripeStyle = PageStripeStyle.TOP_BAR,
            generateBlocks = {
                val tableCells = mutableMapOf<String, TableCellModel>()
                // Row 0: Headers
                tableCells["0_0"] = TableCellModel(
                    textBlocks = listOf(TextBlock("t_0_0", TextFieldValue("البند / وصف الخدمة"), alignment = TextAlignment.CENTER)),
                    backgroundColor = Color(0xFF047857)
                )
                tableCells["0_1"] = TableCellModel(
                    textBlocks = listOf(TextBlock("t_0_1", TextFieldValue("الكمية"), alignment = TextAlignment.CENTER)),
                    backgroundColor = Color(0xFF047857)
                )
                tableCells["0_2"] = TableCellModel(
                    textBlocks = listOf(TextBlock("t_0_2", TextFieldValue("سعر الوحدة (ر.س)"), alignment = TextAlignment.CENTER)),
                    backgroundColor = Color(0xFF047857)
                )
                tableCells["0_3"] = TableCellModel(
                    textBlocks = listOf(TextBlock("t_0_3", TextFieldValue("الإجمالي (ر.س)"), alignment = TextAlignment.CENTER)),
                    backgroundColor = Color(0xFF047857)
                )

                // Row 1: Item 1
                tableCells["1_0"] = TableCellModel(listOf(TextBlock("t_1_0", TextFieldValue("تصميم وتطوير واجهات التطبيق"))))
                tableCells["1_1"] = TableCellModel(listOf(TextBlock("t_1_1", TextFieldValue("1"), alignment = TextAlignment.CENTER)))
                tableCells["1_2"] = TableCellModel(listOf(TextBlock("t_1_2", TextFieldValue("4,500"), alignment = TextAlignment.CENTER)))
                tableCells["1_3"] = TableCellModel(listOf(TextBlock("t_1_3", TextFieldValue("4,500"), alignment = TextAlignment.CENTER)))

                // Row 2: Item 2
                tableCells["2_0"] = TableCellModel(listOf(TextBlock("t_2_0", TextFieldValue("ربط الخدمات السحابية وقواعد البيانات"))))
                tableCells["2_1"] = TableCellModel(listOf(TextBlock("t_2_1", TextFieldValue("1"), alignment = TextAlignment.CENTER)))
                tableCells["2_2"] = TableCellModel(listOf(TextBlock("t_2_2", TextFieldValue("2,500"), alignment = TextAlignment.CENTER)))
                tableCells["2_3"] = TableCellModel(listOf(TextBlock("t_2_3", TextFieldValue("2,500"), alignment = TextAlignment.CENTER)))

                // Row 3: Item 3
                tableCells["3_0"] = TableCellModel(listOf(TextBlock("t_3_0", TextFieldValue("الدعم الفني والصيانة لمدة 6 أشهر"))))
                tableCells["3_1"] = TableCellModel(listOf(TextBlock("t_3_1", TextFieldValue("1"), alignment = TextAlignment.CENTER)))
                tableCells["3_2"] = TableCellModel(listOf(TextBlock("t_3_2", TextFieldValue("1,000"), alignment = TextAlignment.CENTER)))
                tableCells["3_3"] = TableCellModel(listOf(TextBlock("t_3_3", TextFieldValue("1,000"), alignment = TextAlignment.CENTER)))

                listOf(
                    BannerBlock(
                        id = "blk_${UUID.randomUUID()}",
                        title = "فاتـورة ضـريبـية  |  INVOICE",
                        subtitle = "شركة الإبداع الرقمي لتقنية المعلومات  |  السجل التجاري: 1010899842\nالرقم الضريبي: 300984920400003  |  رقم الفاتورة: INV-2026-0492",
                        backgroundColor = Color(0xFF047857),
                        textColor = Color.White,
                        alignment = TextAlignment.RIGHT
                    ),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))) {
                                    append("بيانات العميل المستفيد:\n")
                                    append("اسم العميل / الشركة: [اسم العميل هنا]  |  تاريخ الاستحقاق: 10/09/2026")
                                }
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    ),
                    TableBlock(
                        id = "blk_${UUID.randomUUID()}",
                        rows = 4,
                        cols = 4,
                        cells = tableCells,
                        isRtl = true
                    ),
                    CalloutBlock(
                        id = "blk_${UUID.randomUUID()}",
                        title = "ملخص المبلغ المالي المستحق",
                        text = TextFieldValue("المجموع الفرعي: 8,000 ر.س\nضريبة القيمة المضافة (15%): 1,200 ر.س\nالإجمالي النهائي المستحق: 9,200 ر.س"),
                        backgroundColor = Color(0xFFD1FAE5),
                        borderColor = Color(0xFF047857),
                        textColor = Color(0xFF065F46)
                    ),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontSize = 13.sp, color = Color(0xFF6B7280))) {
                                    append("معلومات التحويل البنكي:\n")
                                    append("اسم الحساب: شركة الإبداع الرقمي | البنك الأهلي السعودي | IBAN: SA03 1000 0001 2345 6789 0001")
                                }
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    )
                )
            }
        ),

        // 11. Executive Business Report
        TemplateItem(
            id = "tpl_business_report",
            title = "تقرير أعمال وإنجاز إداري",
            category = TemplateCategory.BUSINESS,
            description = "تقرير احترافي للإدارة العليا يتضمن مؤشرات الأداء وجداول التحليل",
            previewType = TemplatePreviewType.BUSINESS_REPORT,
            primaryColor = Color(0xFF0369A1),
            secondaryColor = Color(0xFFE0F2FE),
            accentColor = Color(0xFF0284C7),
            stripeStyle = PageStripeStyle.LETTERHEAD_HEADER,
            generateBlocks = {
                listOf(
                    BannerBlock(
                        id = "blk_${UUID.randomUUID()}",
                        title = "التقرير الإداري الفصلي (الربع الثالث 2026)",
                        subtitle = "إعداد: إدارة التخطيط والتطوير الاستراتيجي | حالة التقرير: معتمد نهائياً",
                        backgroundColor = Color(0xFF0369A1),
                        textColor = Color.White,
                        alignment = TextAlignment.RIGHT
                    ),
                    CalloutBlock(
                        id = "blk_${UUID.randomUUID()}",
                        title = "1. الملخص التنفيذي (Executive Summary)",
                        text = TextFieldValue("يستعرض هذا التقرير مؤشرات الأداء التشغيلي والمالي للمؤسسة خلال الربع الحالي، حيث حققت الأقسام نسبة إنجاز بلغت 115% من الأهداف المستهدفة مع تقليل التكاليف التشغيلية بنسبة 8%."),
                        backgroundColor = Color(0xFFE0F2FE),
                        borderColor = Color(0xFF0369A1),
                        textColor = Color(0xFF075985)
                    ),
                    DividerBlock(id = "blk_${UUID.randomUUID()}", color = Color(0xFF0369A1)),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0369A1))) {
                                    append("2. أبرز الإنجازات ومؤشرات الأداء الرئيسية (KPIs)\n")
                                }
                                append("• إطلاق المنصة الرقمية الجديدة واستقطاب 25 ألف عميل جديد.\n")
                                append("• إتمام شراكات استراتيجية مع 3 جهات رائدة في قطاع التوزيع.\n")
                                append("• رفع نسبة رضا العملاء إلى 94.2% مقارنة بـ 88% في الربع السابق.")
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    ),
                    DividerBlock(id = "blk_${UUID.randomUUID()}", color = Color(0xFFCBD5E1)),
                    TextBlock(
                        id = "blk_${UUID.randomUUID()}",
                        text = TextFieldValue(
                            annotatedString = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0369A1))) {
                                    append("3. التحديات والتوصيات الاستراتيجية\n")
                                }
                                append("• التوصية الأولى: التوسع في البنية التحتية السحابية لمواكبة زيادة عدد المستخدمين.\n")
                                append("• التوصية الثانية: تخصيص ميزانية إضافية لبرامج التدريب والتطوير الداخلي.")
                            }
                        ),
                        alignment = TextAlignment.RIGHT
                    )
                )
            }
        )
    )
}
