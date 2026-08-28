import re

with open('app/src/main/java/com/example/presentation/editor/StartScreen.kt', 'r') as f:
    content = f.read()

# Update StartScreen signature
content = re.sub(
    r'fun StartScreen\(\s*onNewDocument: \(\) -> Unit,\s*onOpenFile: \(Uri\) -> Unit,\s*onLoadFromCloud: \(com\.example\.presentation\.cloud\.CloudDocument\) -> Unit = \{\}\s*\)',
    'fun StartScreen(\n    onNewDocument: () -> Unit,\n    onNewDocumentFromTemplate: (String, List<com.example.presentation.editor.DocumentBlock>) -> Unit = { _, _ -> },\n    onOpenFile: (Uri) -> Unit,\n    onLoadFromCloud: (com.example.presentation.cloud.CloudDocument) -> Unit = {}\n)',
    content
)

# Update calls to StartScreenContent in the file
content = re.sub(
    r'onNewDocument = onNewDocument,\s*onOpenFileLauncher',
    'onNewDocument = onNewDocument,\n                        onNewDocumentFromTemplate = onNewDocumentFromTemplate,\n                        onOpenFileLauncher',
    content
)

# Update StartScreenContent signature
content = re.sub(
    r'fun StartScreenContent\(\s*isCompact: Boolean,\s*currentUser: com\.google\.firebase\.auth\.FirebaseUser\?,\s*isLoadingCloudDocs: Boolean,\s*cloudDocs: List<com\.example\.presentation\.cloud\.CloudDocument>,\s*onNewDocument: \(\) -> Unit,\s*onOpenFileLauncher: \(\) -> Unit,\s*onLoadFromCloud: \(com\.example\.presentation\.cloud\.CloudDocument\) -> Unit\s*\)',
    'fun StartScreenContent(\n    isCompact: Boolean,\n    currentUser: com.google.firebase.auth.FirebaseUser?,\n    isLoadingCloudDocs: Boolean,\n    cloudDocs: List<com.example.presentation.cloud.CloudDocument>,\n    onNewDocument: () -> Unit,\n    onNewDocumentFromTemplate: (String, List<com.example.presentation.editor.DocumentBlock>) -> Unit,\n    onOpenFileLauncher: () -> Unit,\n    onLoadFromCloud: (com.example.presentation.cloud.CloudDocument) -> Unit\n)',
    content
)

# Find the LazyRow containing the DocumentTemplateCard and update it
lazy_row_pattern = r'LazyRow\(\s*horizontalArrangement = Arrangement\.spacedBy\(16\.dp\),\s*modifier = Modifier\.fillMaxWidth\(\)\.padding\(bottom = if \(isCompact\) 32\.dp else 48\.dp\)\s*\) \{\s*item \{\s*DocumentTemplateCard\(\s*title = "مستند فارغ",\s*isCompact = isCompact,\s*onClick = onNewDocument\s*\)\s*\}\s*item \{\s*DocumentTemplateCard\(\s*title = "فتح من الجهاز",\s*isCompact = isCompact,\s*isFolder = true,\s*onClick = onOpenFileLauncher\s*\)\s*\}\s*\}'

new_lazy_row = """LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = if (isCompact) 32.dp else 48.dp)
    ) {
        item {
            DocumentTemplateCard(
                title = "مستند فارغ",
                isCompact = isCompact,
                onClick = onNewDocument
            )
        }
        item {
            DocumentTemplateCard(
                title = "سيرة ذاتية",
                isCompact = isCompact,
                iconColor = Color(0xFF107C41),
                onClick = {
                    val cvBlocks = listOf(
                        com.example.presentation.editor.TextBlock(
                            id = "blk_${java.util.UUID.randomUUID()}",
                            text = TextFieldValue(
                                annotatedString = buildAnnotatedString {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp)) {
                                        append("الاسم الكامل\\n")
                                    }
                                    append("المسمى الوظيفي | رقم الهاتف | البريد الإلكتروني\\n\\n")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF185ABD))) {
                                        append("الخبرات المهنية\\n")
                                    }
                                    append("• المسمى الوظيفي - اسم الشركة (2020 - الآن)\\n")
                                    append("• المسمى الوظيفي - اسم الشركة (2018 - 2020)\\n\\n")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF185ABD))) {
                                        append("المؤهلات العلمية\\n")
                                    }
                                    append("• درجة البكالوريوس في التخصص - اسم الجامعة (سنة التخرج)\\n\\n")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF185ABD))) {
                                        append("المهارات\\n")
                                    }
                                    append("• المهارة الأولى، المهارة الثانية، المهارة الثالثة")
                                }
                            ),
                            alignment = com.example.presentation.editor.TextAlignment.RIGHT
                        )
                    )
                    onNewDocumentFromTemplate("سيرة ذاتية", cvBlocks)
                }
            )
        }
        item {
            DocumentTemplateCard(
                title = "خطاب رسمي",
                isCompact = isCompact,
                iconColor = Color(0xFFD83B01),
                onClick = {
                    val letterBlocks = listOf(
                        com.example.presentation.editor.TextBlock(
                            id = "blk_${java.util.UUID.randomUUID()}",
                            text = TextFieldValue(
                                annotatedString = buildAnnotatedString {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("التاريخ: [أدخل التاريخ]\\n")
                                        append("الموضوع: [أدخل الموضوع]\\n\\n")
                                        append("إلى السيد/ة: [أدخل اسم المستلم]\\n\\n")
                                    }
                                    append("تحية طيبة وبعد،\\n\\n")
                                    append("[اكتب نص الخطاب هنا. يرجى توضيح الغرض من الخطاب بشكل موجز ومباشر.]\\n\\n")
                                    append("وتفضلوا بقبول فائق الاحترام والتقدير،\\n\\n")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("المرسل: [اسمك/صفتك]\\n")
                                        append("التوقيع: ______________")
                                    }
                                }
                            ),
                            alignment = com.example.presentation.editor.TextAlignment.RIGHT
                        )
                    )
                    onNewDocumentFromTemplate("خطاب رسمي", letterBlocks)
                }
            )
        }
        item {
            DocumentTemplateCard(
                title = "تقرير",
                isCompact = isCompact,
                iconColor = Color(0xFF0078D4),
                onClick = {
                    val reportBlocks = listOf(
                        com.example.presentation.editor.TextBlock(
                            id = "blk_${java.util.UUID.randomUUID()}",
                            text = TextFieldValue(
                                annotatedString = buildAnnotatedString {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color(0xFF185ABD))) {
                                        append("تقرير: [عنوان التقرير]\\n\\n")
                                    }
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                                        append("1. المقدمة\\n")
                                    }
                                    append("[اكتب هنا ملخصاً بسيطاً يوضح الهدف من التقرير وأهميته.]\\n\\n")
                                    
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                                        append("2. التفاصيل والتحليل\\n")
                                    }
                                    append("[اكتب تفاصيل التقرير، البيانات، والملاحظات التي تم جمعها.]\\n\\n")
                                    
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                                        append("3. التوصيات والخلاصة\\n")
                                    }
                                    append("[أضف المقترحات والخطوات القادمة بناءً على التحليل.]\\n")
                                }
                            ),
                            alignment = com.example.presentation.editor.TextAlignment.RIGHT
                        )
                    )
                    onNewDocumentFromTemplate("تقرير", reportBlocks)
                }
            )
        }
        item {
            DocumentTemplateCard(
                title = "فتح من الجهاز",
                isCompact = isCompact,
                isFolder = true,
                onClick = onOpenFileLauncher
            )
        }
    }"""

content = re.sub(lazy_row_pattern, new_lazy_row.replace('\\n', '\\\\n'), content, flags=re.DOTALL)

# Update DocumentTemplateCard to accept iconColor
card_pattern = r'fun DocumentTemplateCard\(\s*title: String,\s*isCompact: Boolean,\s*isFolder: Boolean = false,\s*onClick: \(\) -> Unit\s*\) \{'
new_card = 'fun DocumentTemplateCard(\n    title: String,\n    isCompact: Boolean,\n    isFolder: Boolean = false,\n    iconColor: Color = Color(0xFF185ABD),\n    onClick: () -> Unit\n) {'
content = re.sub(card_pattern, new_card, content)

# Also update the tint inside DocumentTemplateCard
content = re.sub(
    r'tint = MaterialTheme\.colorScheme\.primary,\s*modifier = Modifier\.size\(40\.dp\)',
    'tint = iconColor,\n                    modifier = Modifier.size(40.dp)',
    content
)

with open('app/src/main/java/com/example/presentation/editor/StartScreen.kt', 'w') as f:
    f.write(content)
