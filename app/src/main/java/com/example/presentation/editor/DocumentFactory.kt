package com.example.presentation.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import java.util.UUID

object DocumentFactory {
    fun createResumeTemplate(): List<DocumentBlock> {
        return listOf(
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue(
                    AnnotatedString(
                        "الاسم الكامل - Full Name",
                        spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A)), 0, 23))
                    )
                ),
                alignment = TextAlignment.CENTER
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue(
                    AnnotatedString(
                        "مسمك الوظيفي المستهدف / Professional Title",
                        spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontSize = 14.sp, color = Color(0xFF4B5563), fontStyle = FontStyle.Italic), 0, 42))
                    )
                ),
                alignment = TextAlignment.CENTER
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("البريد الإلكتروني: email@example.com | الهاتف: +967 770 000 000 | المدينة، الدولة"),
                alignment = TextAlignment.CENTER,
                paragraphShadingColor = Color(0xFFF1F5F9)
            ),
            DividerBlock(id = "blk_${UUID.randomUUID()}"),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue(
                    AnnotatedString(
                        "النبذة المهنية (Executive Summary)",
                        spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB)), 0, 34))
                    )
                ),
                alignment = TextAlignment.RIGHT
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("محترف متخصص يمتلك خبرة ممتازة في إدارة المشاريع وتطوير الأنظمة، يسعى لتقديم إضافة نوعية وتحقيق أهداف المؤسسة بكفاءة عالية."),
                alignment = TextAlignment.JUSTIFY
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue(
                    AnnotatedString(
                        "الخبرات المهنية (Professional Experience)",
                        spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB)), 0, 41))
                    )
                ),
                alignment = TextAlignment.RIGHT
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("• مدير مشاريع - شركة التكنولوجيا والحلول (2022 - الحالي)\n  - قيادة فريق عمل مكون من 10 مهندسين وإكمال 15 مشروع بنجاح.\n  - زيادة الإنتاجية بنسبة 30% وتحسين جودة المخرجات."),
                alignment = TextAlignment.RIGHT
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("• مطور نظم رئيسي - شركة التقنية المتقدمة (2019 - 2022)\n  - تصميم وبناء برمجيات متكاملة واجهات مستخدم حديثة."),
                alignment = TextAlignment.RIGHT
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue(
                    AnnotatedString(
                        "التعليم والشهادات (Education & Credentials)",
                        spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB)), 0, 42))
                    )
                ),
                alignment = TextAlignment.RIGHT
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("• بكالوريوس علوم الحاسوب والهندسة - تقدير ممتاز مع مرتبة الشرف (2015 - 2019)"),
                alignment = TextAlignment.RIGHT
            )
        )
    }

    fun createBusinessLetterTemplate(): List<DocumentBlock> {
        return listOf(
            BannerBlock(id = "blk_${UUID.randomUUID()}", title = "شركة الحلول والابتكار الذكي", subtitle = "Smart Innovation Solutions Ltd."),
            TextBlock(id = "blk_${UUID.randomUUID()}", text = TextFieldValue("التاريخ: 30 أغسطس 2026\nالرقم الإشاري: SIS/2026/104"), alignment = TextAlignment.RIGHT),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue(
                    AnnotatedString(
                        "إلى المحترم / مدير عام المؤسسة الوطنية",
                        spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)), 0, 36))
                    )
                ),
                alignment = TextAlignment.RIGHT
            ),
            TextBlock(id = "blk_${UUID.randomUUID()}", text = TextFieldValue("تحية طيبة وبعد،،،"), alignment = TextAlignment.RIGHT),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue(
                    AnnotatedString(
                        "الموضوع: تقديم عرض خدمات وتطوير الشراكة الاستراتيجية",
                        spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8)), 0, 52))
                    )
                ),
                alignment = TextAlignment.RIGHT
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("يسرنا في شركة الحلول والابتكار الذكي أن نتقدم إليكم بخالص الشكر والتقدير لجهودكم المتميزة. نود بموجب هذا الخطاب تقديم عرضنا الفني والمالي الخاص ببرامج التحول الرقمي والتطوير."),
                alignment = TextAlignment.JUSTIFY
            ),
            CalloutBlock(id = "blk_${UUID.randomUUID()}", title = "ملاحظة هامة", text = TextFieldValue("تم إرفاق جدول الكميات والشروط المرجعية في الملحق رقم (1) لهذا الخطاب.")),
            TextBlock(id = "blk_${UUID.randomUUID()}", text = TextFieldValue("وتفضلوا بقبول فائق الاحترام والتقدير،،،"), alignment = TextAlignment.CENTER),
            TextBlock(id = "blk_${UUID.randomUUID()}", text = TextFieldValue("\n_________________________\nالتوقيع والختم / Authorized Signature"), alignment = TextAlignment.LEFT)
        )
    }

    fun createExecutiveReportTemplate(): List<DocumentBlock> {
        val cells = mutableMapOf<String, TableCellModel>()
        cells["0_0"] = TableCellModel(listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue(AnnotatedString("الربع السنوي", spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold), 0, 11)))), alignment = TextAlignment.CENTER)))
        cells["0_1"] = TableCellModel(listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue(AnnotatedString("المبيعات (USD)", spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold), 0, 14)))), alignment = TextAlignment.CENTER)))
        cells["0_2"] = TableCellModel(listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue(AnnotatedString("نسبة النمو", spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold), 0, 10)))), alignment = TextAlignment.CENTER)))
        cells["1_0"] = TableCellModel(listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue("الربع الأول Q1"), alignment = TextAlignment.CENTER)))
        cells["1_1"] = TableCellModel(listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue("$150,000"), alignment = TextAlignment.CENTER)))
        cells["1_2"] = TableCellModel(listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue("+12%"), alignment = TextAlignment.CENTER)))
        cells["2_0"] = TableCellModel(listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue("الربع الثاني Q2"), alignment = TextAlignment.CENTER)))
        cells["2_1"] = TableCellModel(listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue("$185,000"), alignment = TextAlignment.CENTER)))
        cells["2_2"] = TableCellModel(listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue("+23%"), alignment = TextAlignment.CENTER)))

        return listOf(
            BannerBlock(id = "blk_${UUID.randomUUID()}", title = "التقرير التنفيذي السنوي لعام 2026", subtitle = "Annual Executive Performance Report"),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue(
                    AnnotatedString(
                        "1. الملخص التنفيذي (Executive Summary)",
                        spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A)), 0, 39))
                    )
                ),
                alignment = TextAlignment.RIGHT
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("شهد هذا العام أداءً استثنائياً في جميع القطاعات التشغيلية والمالية. تم تحقيق الأهداف المخططة بنسبة 115% بفضل خطة التحول الإستراتيجي وتوسيع نطاق المبيعات."),
                alignment = TextAlignment.JUSTIFY
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue(
                    AnnotatedString(
                        "2. مؤشرات الأداء المالي (Financial Metrics)",
                        spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A)), 0, 42))
                    )
                ),
                alignment = TextAlignment.RIGHT
            ),
            TableBlock(id = "blk_${UUID.randomUUID()}", rows = 3, cols = 3, cells = cells, isRtl = true),
            CalloutBlock(id = "blk_${UUID.randomUUID()}", title = "التوصيات المستقبلية", text = TextFieldValue("زيادة الاستثمار في التقنيات الذكية والأتمتة لخفض التكاليف التشغيلية بنسبة 15% خلال العام القادم."))
        )
    }

    fun createAcademicPaperTemplate(): List<DocumentBlock> {
        return listOf(
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue(
                    AnnotatedString(
                        "عنوان البحث الأكاديمي الشامل",
                        spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)), 0, 26))
                    )
                ),
                alignment = TextAlignment.CENTER
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("إعداد الباحث: د. أحمد خالد | قسم علوم الحاسوب والمعلومات | جامعة العلوم"),
                alignment = TextAlignment.CENTER,
                paragraphShadingColor = Color(0xFFF8FAFC)
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue(
                    AnnotatedString(
                        "الملخص (Abstract)",
                        spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155)), 0, 17))
                    )
                ),
                alignment = TextAlignment.CENTER
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("تناقش هذه الورقة العلمية منهجيات المعالجة الحديثة وتأثيرها على كفاءة الأداء البرمجي. تم إجراء اختبارات تجريبية مقارنة لتحديد الخوارزمية الأكثر فاعلية."),
                alignment = TextAlignment.JUSTIFY,
                paragraphBorder = ParagraphBorder.LEFT
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue(
                    AnnotatedString(
                        "1. المقدمة (Introduction)",
                        spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8)), 0, 25))
                    )
                ),
                alignment = TextAlignment.RIGHT
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("تعتبر معالجة البيانات من أهم المحاور في البرمجيات الحديثة. تهدف هذه الدراسة إلى تقديم تحليل دقيق ومقارن لأداء النظام تحت مختلف ظروف الضغط التشغيلي."),
                alignment = TextAlignment.JUSTIFY
            )
        )
    }

    fun createComprehensiveTestDocument(): List<DocumentBlock> {
        val cells = mutableMapOf<String, TableCellModel>()
        cells["0_0"] = TableCellModel(listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue(AnnotatedString("الاسم", spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold), 0, 5)))), alignment = TextAlignment.CENTER)))
        cells["0_1"] = TableCellModel(listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue(AnnotatedString("العمر", spanStyles = listOf(AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold), 0, 5)))), alignment = TextAlignment.CENTER)))
        cells["1_0"] = TableCellModel(listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue("أحمد"), alignment = TextAlignment.CENTER)))
        cells["1_1"] = TableCellModel(listOf(TextBlock("blk_${UUID.randomUUID()}", TextFieldValue("30"), alignment = TextAlignment.CENTER)))

        return listOf(
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue(
                    AnnotatedString(
                        "مستند الاختبار الشامل - Comprehensive Test",
                        spanStyles = listOf(
                            AnnotatedString.Range(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF185ABD)), 0, 42)
                        )
                    )
                ),
                alignment = TextAlignment.CENTER
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("هذا المستند مصمم لاختبار قدرات المحرر بشكل شامل. يحتوي على نصوص متداخلة (عربي وإنجليزي)، قوائم، جداول، وتنسيقات مختلفة."),
                alignment = TextAlignment.JUSTIFY
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue(
                    AnnotatedString.Builder().apply {
                        pushStyle(SpanStyle(color = Color.Black, fontSize = 16.sp))
                        append("اختبار التنسيق المتداخل: ")
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.Red))
                        append("نص عريض (Bold) ")
                        pop()
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = Color.Blue))
                        append("نص مائل (Italic) ")
                        pop()
                        pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                        append("Underlined Text ")
                        pop()
                        pushStyle(SpanStyle(background = Color.Yellow))
                        append("Highlighted Text")
                        pop()
                    }.toAnnotatedString()
                ),
                alignment = TextAlignment.RIGHT
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("قائمة نقطية (Bulleted List):"),
                alignment = TextAlignment.RIGHT,
                isBulletedList = false
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("العنصر الأول"),
                alignment = TextAlignment.RIGHT,
                isBulletedList = true,
                bulletShape = BulletShape.DISC,
                indentLevel = 1
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("العنصر الثاني"),
                alignment = TextAlignment.RIGHT,
                isBulletedList = true,
                bulletShape = BulletShape.CIRCLE,
                indentLevel = 2
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("قائمة رقمية (Numbered List):"),
                alignment = TextAlignment.RIGHT,
                isBulletedList = false
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("الخطوة الأولى"),
                alignment = TextAlignment.RIGHT,
                isNumberedList = true,
                numberingStyle = NumberingStyle.DECIMAL_DOT,
                indentLevel = 1
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("الخطوة الثانية"),
                alignment = TextAlignment.RIGHT,
                isNumberedList = true,
                numberingStyle = NumberingStyle.ARABIC_INDIC,
                indentLevel = 1
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue("جدول اختباري (Test Table):"),
                alignment = TextAlignment.RIGHT
            ),
            TableBlock(
                id = "blk_${UUID.randomUUID()}",
                rows = 2,
                cols = 2,
                cells = cells,
                isRtl = true
            ),
            TextBlock(
                id = "blk_${UUID.randomUUID()}",
                text = TextFieldValue(
                    AnnotatedString(
                        "فقرة بخلفية ملونة وحدود (Shaded & Bordered Paragraph)",
                        spanStyles = listOf(AnnotatedString.Range(SpanStyle(color = Color.White), 0, 53))
                    )
                ),
                alignment = TextAlignment.CENTER,
                paragraphShadingColor = Color(0xFF1E293B),
                paragraphBorder = ParagraphBorder.ALL
            )
        )
    }
}
