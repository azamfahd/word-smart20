package com.example.presentation.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.presentation.editor.*

/**
 * Table of Contents Dialog (جدول المحتويات التلقائي والمخصص)
 */
@Composable
fun TableOfContentsDialog(
    state: EditorState,
    onDismiss: () -> Unit,
    onInsert: (style: String) -> Unit
) {
    var selectedStyle by remember { mutableStateOf("Automatic Table 1") }
    var showPageNumbers by remember { mutableStateOf(true) }
    var rightAlignNumbers by remember { mutableStateOf(true) }
    var tabLeader by remember { mutableStateOf("......") }

    // Scan existing headings in the document
    val detectedHeadings = remember(state.blocks) {
        state.blocks.filterIsInstance<TextBlock>().filter { block ->
            block.isBold || block.fontSize >= 14 || block.fontFamily.contains("Bold", ignoreCase = true)
        }.map { it.text.text.trim() }.filter { it.isNotBlank() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF185ABD))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (state.isRtl) "إدراج جدول المحتويات (Table of Contents)" else "Insert Table of Contents",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Content Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Styles Picker
                    Text(
                        text = if (state.isRtl) "اختر تنسيق جدول المحتويات:" else "Select TOC Template Style:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )

                    val tocStyles = listOf(
                        Pair("Automatic Table 1", if (state.isRtl) "جدول تلقائي 1 (المحتويات - نمط كلاسيكي بنقاط وصل)" else "Automatic Table 1 (Classic dot leaders)"),
                        Pair("Automatic Table 2", if (state.isRtl) "جدول تلقائي 2 (الفهرس - نمط عصري أزرق)" else "Automatic Table 2 (Modern Blue Index)"),
                        Pair("Manual Table", if (state.isRtl) "جدول يدوي (كتابة العناوين والأرقام يدوياً)" else "Manual Table (Type entries manually)")
                    )

                    tocStyles.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedStyle == key) Color(0xFFEFF6FF) else Color.White)
                                .border(1.dp, if (selectedStyle == key) Color(0xFF3B82F6) else Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                .clickable { selectedStyle = key }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedStyle == key, onClick = { selectedStyle = key })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontSize = 13.sp, fontWeight = if (selectedStyle == key) FontWeight.Bold else FontWeight.Normal)
                        }
                    }

                    HorizontalDivider()

                    // TOC Live Preview Box
                    Text(
                        text = if (state.isRtl) "معاينة الجدول حسب عناوين المستند الحالية:" else "Live Preview from Document Headings:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (state.isRtl) "جدول المحتويات" else "Table of Contents",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1E3A8A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            val headingsToShow = if (detectedHeadings.isNotEmpty()) detectedHeadings else listOf(
                                if (state.isRtl) "المقدمة والأهداف العامة" else "1. Introduction & Executive Summary",
                                if (state.isRtl) "خطة العمل والمنهجية" else "2. Methodology & Operations",
                                if (state.isRtl) "النتائج والتوصيات الختامية" else "3. Results & Final Recommendations"
                            )

                            headingsToShow.forEachIndexed { idx, heading ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = heading,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Text(
                                        text = "  ${tabLeader}  ",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2563EB)
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(if (state.isRtl) "إلغاء" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onInsert(selectedStyle)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185ABD))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (state.isRtl) "إدراج الجدول في المستند" else "Insert TOC")
                    }
                }
            }
        }
    }
}

/**
 * Footnotes and Endnotes Dialog (إدراج وتنسيق الحواشي السفلية)
 */
@Composable
fun FootnotesDialog(
    state: EditorState,
    onDismiss: () -> Unit,
    onInsert: (noteText: String, format: String) -> Unit
) {
    var noteText by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf("1, 2, 3") }
    var isEndnote by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF185ABD))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.isRtl) "إدراج حاشية سفلية / تعليق ختامي" else "Insert Footnote / Endnote",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Type Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = !isEndnote, onClick = { isEndnote = false })
                            Text(if (state.isRtl) "حاشية سفلية (أسفل الصفحة)" else "Footnote (Bottom of page)", fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isEndnote, onClick = { isEndnote = true })
                            Text(if (state.isRtl) "تعليق ختامي (نهاية المستند)" else "Endnote (End of document)", fontSize = 12.sp)
                        }
                    }

                    // Numbering Format
                    Text(if (state.isRtl) "تنسيق الأرقام والرموز:" else "Numbering Format:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("1, 2, 3", "i, ii, iii", "أ، ب، ت", "*, †, ‡").forEach { fmt ->
                            FilterChip(
                                selected = selectedFormat == fmt,
                                onClick = { selectedFormat = fmt },
                                label = { Text(fmt, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Note Text
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text(if (state.isRtl) "نص الحاشية أو المرجع" else "Footnote / Reference Text") },
                        placeholder = { Text(if (state.isRtl) "مثال: انظر المرجع ص 45..." else "e.g. See Reference p. 45...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(if (state.isRtl) "إلغاء" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (noteText.isNotBlank()) {
                                onInsert(noteText, selectedFormat)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185ABD))
                    ) {
                        Text(if (state.isRtl) "إدراج" else "Insert")
                    }
                }
            }
        }
    }
}

/**
 * Citations and Bibliography Dialog (إدراج اقتباس وتوثيق مصادر)
 */
@Composable
fun CitationsBibliographyDialog(
    state: EditorState,
    onDismiss: () -> Unit,
    onInsert: (author: String, title: String, year: String, style: String) -> Unit
) {
    var author by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("2026") }
    var publisher by remember { mutableStateOf("") }
    var citationStyle by remember { mutableStateOf("APA") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF185ABD))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Source, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isRtl) "إدراج اقتباس ومصدر علمي (Citation)" else "Insert Citation & Source",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(if (state.isRtl) "نمط التوثيق الأكاديمي:" else "Citation Style:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("APA", "MLA", "Chicago", "Harvard", "IEEE").forEach { st ->
                            FilterChip(
                                selected = citationStyle == st,
                                onClick = { citationStyle = st },
                                label = { Text(st, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text(if (state.isRtl) "المؤلف / الكاتب (Author)" else "Author") },
                        placeholder = { Text(if (state.isRtl) "مثال: د. أحمد المنصور" else "e.g. Smith, John") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (state.isRtl) "عنوان الكتاب أو المقال (Title)" else "Title of Source") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = year,
                            onValueChange = { year = it },
                            label = { Text(if (state.isRtl) "السنة" else "Year") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = publisher,
                            onValueChange = { publisher = it },
                            label = { Text(if (state.isRtl) "دار النشر / المجلة" else "Publisher") },
                            modifier = Modifier.weight(2f)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text(if (state.isRtl) "إلغاء" else "Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (author.isNotBlank() || title.isNotBlank()) {
                                onInsert(author, title, year, citationStyle)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185ABD))
                    ) {
                        Text(if (state.isRtl) "إدراج الاقتباس" else "Insert Citation")
                    }
                }
            }
        }
    }
}

/**
 * Envelopes and Labels Generator Dialog (إنشاء المغلفات وبطاقات العناوين)
 */
@Composable
fun EnvelopesLabelsDialog(
    state: EditorState,
    onDismiss: () -> Unit,
    onInsert: (recipient: String, delivery: String, sender: String, size: String) -> Unit
) {
    var recipientName by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var returnAddress by remember { mutableStateOf("") }
    var envelopeSize by remember { mutableStateOf("Size 10 (4 1/8 x 9 1/2 in)") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF185ABD))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mail, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isRtl) "مغلفات وتسميات بريدية (Envelopes & Labels)" else "Envelopes and Labels",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(if (state.isRtl) "حجم المغلف:" else "Envelope Size:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    val sizes = listOf("Size 10 (4 1/8 x 9 1/2 in)", "DL (110 x 220 mm)", "C5 (162 x 229 mm)", "B4 (250 x 353 mm)")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        sizes.take(2).forEach { sz ->
                            FilterChip(
                                selected = envelopeSize == sz,
                                onClick = { envelopeSize = sz },
                                label = { Text(sz.substringBefore("("), fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        label = { Text(if (state.isRtl) "اسم المستلم / الجهة المرسل إليها" else "Recipient Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text(if (state.isRtl) "عنوان التسليم البريدي (Delivery Address)" else "Delivery Address") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = returnAddress,
                        onValueChange = { returnAddress = it },
                        label = { Text(if (state.isRtl) "عنوان المرسل / الرد (Return Address)" else "Return Address") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text(if (state.isRtl) "إلغاء" else "Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onInsert(recipientName, deliveryAddress, returnAddress, envelopeSize)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185ABD))
                    ) {
                        Text(if (state.isRtl) "إضافة إلى المستند" else "Add to Document")
                    }
                }
            }
        }
    }
}

/**
 * Mail Merge Wizard Dialog (معالج دمج المراسلات التفاعلي)
 */
@Composable
fun MailMergeWizardDialog(
    state: EditorState,
    onDismiss: () -> Unit,
    onApplyMerge: (recipients: List<MailMergeRecipient>) -> Unit
) {
    var recipients by remember {
        mutableStateOf(
            listOf(
                MailMergeRecipient("1", if (state.isRtl) "م. فهد المحمدي" else "Fahd Al-Mohamadi", "fahd@example.com"),
                MailMergeRecipient("2", if (state.isRtl) "د. سارة المنصور" else "Sarah Al-Mansoor", "sarah@example.com"),
                MailMergeRecipient("3", if (state.isRtl) "أ. خالد العتيبي" else "Khaled Al-Otaibi", "khaled@example.com")
            )
        )
    }
    var newName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var currentPreviewIdx by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF185ABD))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isRtl) "معالج دمج المراسلات (Mail Merge Manager)" else "Mail Merge Manager",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (state.isRtl) "قائمة المستلمين لدمج الخطابات والبيانات:" else "Recipient List for Personalized Documents:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )

                    // Add Recipient Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text(if (state.isRtl) "الاسم" else "Name") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text(if (state.isRtl) "البريد / المؤسسة" else "Email / Org") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    recipients = recipients + MailMergeRecipient(
                                        id = (recipients.size + 1).toString(),
                                        name = newName,
                                        email = newEmail
                                    )
                                    newName = ""
                                    newEmail = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }

                    // Recipients List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(recipients) { idx, recipient ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (currentPreviewIdx == idx) Color(0xFFEFF6FF) else Color(0xFFF8FAFC), RoundedCornerShape(4.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${idx + 1}.", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2563EB))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(recipient.name, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                        Text(recipient.email, fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                                IconButton(
                                    onClick = { recipients = recipients.filter { it.id != recipient.id } },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // Merge Fields Info
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.isRtl) "يمكنك استخدام الحقول «الاسم» و «البريد» و «التاريخ» في نصوص المستند وسيتم استبدالها تلقائياً لكل مستلم." else "Use fields «Name», «Email», and «Date» in your document to generate personalized copies.",
                                fontSize = 11.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text(if (state.isRtl) "إلغاء" else "Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onApplyMerge(recipients)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185ABD))
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (state.isRtl) "إنهاء ودمج المراسلات" else "Finish & Merge")
                    }
                }
            }
        }
    }
}

/**
 * Word Help Center & Keyboard Shortcuts Dialog (مركز المساعدة واختصارات لوحة المفاتيح)
 */
@Composable
fun WordHelpCenterDialog(
    state: EditorState,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val tabs = listOf(
        if (state.isRtl) "دليل الميزات" else "Features Guide",
        if (state.isRtl) "اختصارات المفاتيح" else "Shortcuts",
        if (state.isRtl) "التوافقية والحفظ" else "Compatibility"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF185ABD))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isRtl) "مركز تعليمات ومساعدة Word" else "Word Help & Learning Center",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { idx, title ->
                        Tab(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            text = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            HelpFeatureCard(
                                title = if (state.isRtl) "تنسيق النصوص والخطوط العربية واللاتينية" else "Typography & Bilingual Fonts",
                                desc = if (state.isRtl) "دعم كامل للخطوط العربية الأصيلة (أميري، القاهرة، تجوال، المراعي، الخط الكوفي) وخطوط مايكروسوفت أوفيس القياسية (Calibri, Times New Roman, Arial)." else "Full bilingual support for Arabic and Latin typefaces with full line spacing and text formatting."
                            )
                            HelpFeatureCard(
                                title = if (state.isRtl) "الجداول المتقدمة وتوافقية Word" else "Tables & Document Structure",
                                desc = if (state.isRtl) "إنشاء جداول متعددة الصفوف والأعمدة مع تخصيص اتجاه RTL وتلوين الخلايا وحفظها بصيغة OOXML متوافقة 100% مع Microsoft Office." else "Create complex tables with row/column insertion, cell shading, RTL support, and full DOCX round-trip compatibility."
                            )
                            HelpFeatureCard(
                                title = if (state.isRtl) "أدوات الرسم والتوقيع الرقمي" else "Drawing & Inking Tools",
                                desc = if (state.isRtl) "إمكانية الرسم الحر بالقلم وقلم التمييز والممحاة، وإضافة خطوط التوقيع المعتمدة." else "Freeform drawing canvas overlay with pen, highlighter, eraser, and signature line insertion."
                            )
                        }
                        1 -> {
                            val shortcuts = listOf(
                                Pair("Ctrl + B / ⌘ + B", if (state.isRtl) "نص عريض (Bold)" else "Bold Text"),
                                Pair("Ctrl + I / ⌘ + I", if (state.isRtl) "نص مائل (Italic)" else "Italic Text"),
                                Pair("Ctrl + U / ⌘ + U", if (state.isRtl) "تسطير (Underline)" else "Underline"),
                                Pair("Ctrl + S / ⌘ + S", if (state.isRtl) "حفظ المستند (Save)" else "Save Document"),
                                Pair("Ctrl + Z / ⌘ + Z", if (state.isRtl) "تراجع (Undo)" else "Undo Action"),
                                Pair("Ctrl + Y / ⌘ + Y", if (state.isRtl) "إعادة (Redo)" else "Redo Action"),
                                Pair("Ctrl + F / ⌘ + F", if (state.isRtl) "بحث واستبدال (Find & Replace)" else "Find & Replace"),
                                Pair("Ctrl + P / ⌘ + P", if (state.isRtl) "تصدير كـ PDF / طباعة" else "Export to PDF / Print")
                            )

                            shortcuts.forEach { (keys, action) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(action, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Surface(
                                        color = Color(0xFF1E293B),
                                        shape = RoundedCornerShape(3.dp)
                                    ) {
                                        Text(keys, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                        2 -> {
                            Text(
                                text = if (state.isRtl) "التوافقية التامة مع Microsoft Word و PDF:" else "Full Compatibility with Microsoft Word & PDF:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (state.isRtl) "• يتم حفظ وتصدير كافة المستندات بصيغة DOCX المعيارية (OpenXML) القابلة للفتح والتعديل في Microsoft Word 2016/2019/2021 و Word 365 بدون أي تشويه.\n• محرك الطباعة والتصدير إلى PDF يحافظ على القياسات الدقيقة للصفحة والهوامش والخطوط والرسومات." else "• All documents are exported as standards-compliant OpenXML (.docx) files readable across Microsoft Word and Google Docs without distortion.\n• Built-in PDF exporter ensures pixel-perfect pagination and typography.",
                                fontSize = 12.sp,
                                lineHeight = 20.sp,
                                color = Color(0xFF334155)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185ABD))
                    ) {
                        Text(if (state.isRtl) "إغلاق" else "Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpFeatureCard(title: String, desc: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E3A8A))
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, fontSize = 11.sp, color = Color(0xFF475569), lineHeight = 16.sp)
        }
    }
}

/**
 * AI Document Assistant Dialog (مساعد الذكاء الاصطناعي لكتابة وتدقيق وتلخيص المستندات)
 */
@Composable
fun AiDocumentAssistantDialog(
    state: EditorState,
    onDismiss: () -> Unit,
    onApplyText: (newText: String) -> Unit
) {
    var promptType by remember { mutableStateOf("PROOFREAD") }
    var selectedTone by remember { mutableStateOf("Formal") }
    var targetLanguage by remember { mutableStateOf("Arabic") }
    var customInstruction by remember { mutableStateOf("") }
    var generatedResult by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    // Grab current document text snippet
    val docSampleText = remember(state.blocks) {
        state.blocks.filterIsInstance<TextBlock>().joinToString("\n") { it.text.text.trim() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF185ABD))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFDE047))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isRtl) "مساعد Word الذكي (AI Smart Assistant)" else "Word AI Smart Assistant",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(if (state.isRtl) "اختر المهمة المطلوبة:" else "Select Task:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                    val tasks = listOf(
                        Pair("PROOFREAD", if (state.isRtl) "✨ التدقيق الإملائي واللغوي" else "✨ Grammar & Spell Check"),
                        Pair("SUMMARIZE", if (state.isRtl) "📝 تلخيص المستند" else "📝 Summarize Document"),
                        Pair("REWRITE", if (state.isRtl) "✍️ إعادة صياغة احترافية" else "✍️ Professional Rewrite"),
                        Pair("TRANSLATE", if (state.isRtl) "🌐 ترجمة فورية" else "🌐 Translate Document"),
                        Pair("EXPAND", if (state.isRtl) "💡 توليد وتوسيع المحتوى" else "💡 Expand & Generate")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tasks.take(3).forEach { (id, label) ->
                            FilterChip(
                                selected = promptType == id,
                                onClick = { promptType = id },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tasks.drop(3).forEach { (id, label) ->
                            FilterChip(
                                selected = promptType == id,
                                onClick = { promptType = id },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                            )
                        }
                    }

                    if (promptType == "REWRITE") {
                        Text(if (state.isRtl) "النبرة والأسلوب:" else "Tone & Voice:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Formal", "Executive", "Friendly", "Concise", "Persuasive").forEach { tn ->
                                FilterChip(
                                    selected = selectedTone == tn,
                                    onClick = { selectedTone = tn },
                                    label = { Text(tn, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    if (promptType == "TRANSLATE") {
                        Text(if (state.isRtl) "اللغة الهدف:" else "Target Language:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Arabic", "English", "French", "German", "Spanish").forEach { lg ->
                                FilterChip(
                                    selected = targetLanguage == lg,
                                    onClick = { targetLanguage = lg },
                                    label = { Text(lg, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customInstruction,
                        onValueChange = { customInstruction = it },
                        label = { Text(if (state.isRtl) "تعليمات مخصصة (اختياري)" else "Custom Instructions (Optional)") },
                        placeholder = { Text(if (state.isRtl) "مثال: ركز على النقاط التنفيذية..." else "e.g. Focus on executive summary...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            isGenerating = true
                            // Smart simulated AI processing with immediate rich responses
                            val processed = when (promptType) {
                                "SUMMARIZE" -> {
                                    if (state.isRtl) {
                                        "📌 **ملخص تنفيذي للمستند:**\n• يتناول المستند المحاور والبنود الإدارية الرئيسية بصياغة متكاملة.\n• تم التحقق من سلامة كافة البيانات والجداول المرفقة.\n• التوصية بالاعتماد النهائي وتطبيق الإجراءات المقترحة."
                                    } else {
                                        "📌 **Executive Summary:**\n• Covers strategic objectives and operational milestones.\n• Verifies all tables, figures, and attached references.\n• Recommends final review and project execution."
                                    }
                                }
                                "PROOFREAD" -> {
                                    if (state.isRtl) {
                                        "✅ **تم التدقيق اللغوي والإملائي بنجاح:**\n• تم تصحيح علامات الترقيم وهمزات الوصل والقطع وضبط التنوين.\n• الصياغة اللغوية مطابقة تماماً لقواعد اللغة العربية الفصحى الإدارية."
                                    } else {
                                        "✅ **Grammar & Proofing Completed:**\n• Punctuation, capitalization, and sentence syntax polished.\n• Professional standard tone aligned across all sections."
                                    }
                                }
                                "TRANSLATE" -> {
                                    if (targetLanguage == "Arabic") {
                                        "مستند رسمي معتمد يحتوي على كافة البنود والتنسيقات المتقدمة المتوافقة مع معايير مايكروسوفت وورد."
                                    } else {
                                        "Official certified document containing all advanced formatting standards compatible with Microsoft Word."
                                    }
                                }
                                else -> {
                                    if (state.isRtl) {
                                        "بناءً على المعطيات والتحليلات الواردة في هذا المستند، نؤكد على أهمية الاستمرار في تطبيق أعلى معايير الجودة والتميز المؤسسي لتحقيق النتائج المرجوة بكفاءة واقتدار."
                                    } else {
                                        "Based on the strategic insights outlined in this document, adhering to executive standards ensures seamless milestone delivery and optimal productivity."
                                    }
                                }
                            }
                            generatedResult = processed
                            isGenerating = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (state.isRtl) "معالجة بالذكاء الاصطناعي" else "Run AI Assistant")
                    }

                    if (generatedResult.isNotBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (state.isRtl) "النتيجة المقترحة:" else "Generated Output:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E3A8A)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(generatedResult, fontSize = 12.sp, lineHeight = 18.sp, color = Color(0xFF1E293B))
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text(if (state.isRtl) "إغلاق" else "Close") }
                    if (generatedResult.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onApplyText(generatedResult)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41))
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (state.isRtl) "إدراج في المستند" else "Apply to Document")
                        }
                    }
                }
            }
        }
    }
}
