package com.example.presentation.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.auth.AuthManager
import com.example.presentation.auth.UserSession
import com.example.presentation.templates.DocumentTemplatesRepository
import com.example.presentation.templates.TemplateCategory
import com.example.presentation.templates.TemplateCard
import com.example.presentation.templates.TemplateItem
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun StartScreen(
    onNewDocument: () -> Unit,
    onNewDocumentFromTemplate: (TemplateItem) -> Unit = {},
    onOpenFile: (Uri) -> Unit,
    onLoadFromCloud: (com.example.presentation.cloud.CloudDocument) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authManager = remember { AuthManager(context) }
    var currentUser by remember { mutableStateOf(authManager.currentUser) }

    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onOpenFile(it) }
    }
    
    val cloudSyncManager = remember { com.example.presentation.cloud.CloudSyncManager() }
    var cloudDocs by remember { mutableStateOf<List<com.example.presentation.cloud.CloudDocument>>(emptyList()) }
    var isLoadingCloudDocs by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showFirebaseConfigDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            isLoadingCloudDocs = true
            try {
                cloudDocs = cloudSyncManager.getDocuments(currentUser!!.uid)
            } catch (e: Exception) {
            }
            isLoadingCloudDocs = false
        } else {
            cloudDocs = emptyList()
        }
    }

    val handleLoginLogout: () -> Unit = {
        if (currentUser == null) {
            coroutineScope.launch {
                val result = authManager.signInWithGoogle()
                result.onSuccess {
                    currentUser = authManager.currentUser
                    android.widget.Toast.makeText(context, "تم تسجيل الدخول بنجاح", android.widget.Toast.LENGTH_SHORT).show()
                }.onFailure { e ->
                    val isFirebaseError = e.message?.contains("Firebase") == true || e.message?.contains("google-services") == true
                    if (isFirebaseError) {
                        showFirebaseConfigDialog = true
                    } else {
                        android.widget.Toast.makeText(context, e.message ?: "فشل تسجيل الدخول", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            authManager.signOut()
            currentUser = null
            android.widget.Toast.makeText(context, "تم تسجيل الخروج", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F2F1)) // Fluent Design Light Gray Background
    ) {
        val isCompact = maxWidth < 600.dp

        if (isCompact) {
            // Mobile Layout
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar for Mobile (Respects Status Bar and Cutouts)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsTopHeight(WindowInsets.statusBars)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Outlined.Article, contentDescription = "Word", tint = Color.White, modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Word",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row {
                            IconButton(onClick = handleLoginLogout) {
                                Icon(
                                    imageVector = if (currentUser != null) Icons.Default.AccountCircle else Icons.AutoMirrored.Filled.Login,
                                    contentDescription = "Profile",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = { showSettingsDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                
                // Mobile Content (Respects Gesture Bar / Navigation Bar)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    StartScreenContent(
                        isCompact = true,
                        currentUser = currentUser,
                        isLoadingCloudDocs = isLoadingCloudDocs,
                        cloudDocs = cloudDocs,
                        onNewDocument = onNewDocument,
                        onNewDocumentFromTemplate = onNewDocumentFromTemplate,
                        onOpenFileLauncher = { openFileLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword")) },
                        onLoadFromCloud = onLoadFromCloud
                    )
                }
            }
        } else {
            // Desktop/Tablet Layout (Microsoft Office Style - Respects System Bars)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Sidebar
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary) // Classic Word Blue
                ) {
                    Column(modifier = Modifier.fillMaxHeight().padding(vertical = 24.dp, horizontal = 12.dp)) {
                        Text(
                            text = "Word",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 12.dp, bottom = 32.dp)
                        )

                        SidebarItem(text = "الرئيسية", icon = Icons.Default.Home, isSelected = true)
                        Spacer(modifier = Modifier.height(4.dp))
                        SidebarItem(text = "جديد", icon = Icons.Default.Add, isSelected = false, onClick = onNewDocument)
                        Spacer(modifier = Modifier.height(4.dp))
                        SidebarItem(text = "فتح", icon = Icons.Default.FolderOpen, isSelected = false, onClick = {
                            openFileLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword"))
                        })
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        SidebarItem(
                            text = if (currentUser != null) currentUser?.displayName?.split(" ")?.firstOrNull() ?: "حسابي" else "تسجيل الدخول",
                            icon = if (currentUser != null) Icons.Default.AccountCircle else Icons.Default.Login,
                            isSelected = false,
                            onClick = handleLoginLogout
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SidebarItem(
                            text = "التعليقات",
                            icon = Icons.Default.Feedback,
                            isSelected = false,
                            onClick = { showFeedbackDialog = true }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SidebarItem(
                            text = "الخيارات",
                            icon = Icons.Default.Settings,
                            isSelected = false,
                            onClick = { showSettingsDialog = true }
                        )
                    }
                }

                // Desktop Content Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(0xFFF3F2F1)) // Fluent Light Gray
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 40.dp, vertical = 32.dp)
                ) {
                    StartScreenContent(
                        isCompact = false,
                        currentUser = currentUser,
                        isLoadingCloudDocs = isLoadingCloudDocs,
                        cloudDocs = cloudDocs,
                        onNewDocument = onNewDocument,
                        onNewDocumentFromTemplate = onNewDocumentFromTemplate,
                        onOpenFileLauncher = { openFileLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword")) },
                        onLoadFromCloud = onLoadFromCloud
                    )
                }
            }
        }
    }

    if (showFeedbackDialog) {
        FeedbackDialog(onDismiss = { showFeedbackDialog = false })
    }

    if (showSettingsDialog) {
        SettingsDialog(onDismiss = { showSettingsDialog = false })
    }

    if (showFirebaseConfigDialog) {
        FirebaseConfigDialog(
            onDismiss = { showFirebaseConfigDialog = false },
            onSignInAsDemo = {
                authManager.signInAsDemoUser()
                currentUser = authManager.currentUser
                showFirebaseConfigDialog = false
                android.widget.Toast.makeText(context, "تم تسجيل الدخول كحساب تجريبي محلي", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun StartScreenContent(
    isCompact: Boolean,
    currentUser: com.example.presentation.auth.UserSession?,
    isLoadingCloudDocs: Boolean,
    cloudDocs: List<com.example.presentation.cloud.CloudDocument>,
    onNewDocument: () -> Unit,
    onNewDocumentFromTemplate: (TemplateItem) -> Unit,
    onOpenFileLauncher: () -> Unit,
    onLoadFromCloud: (com.example.presentation.cloud.CloudDocument) -> Unit
) {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 5..11 -> "صباح الخير"
        in 12..17 -> "مساء الخير"
        else -> "مرحباً"
    }

    var selectedCategory by remember { mutableStateOf(TemplateCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredTemplates = remember(selectedCategory, searchQuery) {
        DocumentTemplatesRepository.templates.filter { template ->
            val matchesCategory = if (selectedCategory == TemplateCategory.ALL) {
                true
            } else {
                template.category == selectedCategory
            }
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                template.title.contains(searchQuery, ignoreCase = true) ||
                template.description.contains(searchQuery, ignoreCase = true) ||
                template.category.title.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    // Greeting & Header
    Text(
        text = greeting,
        fontSize = if (isCompact) 22.sp else 30.sp,
        fontWeight = FontWeight.Light,
        color = Color(0xFF202124),
        modifier = Modifier.padding(bottom = 12.dp)
    )

    // Section Title & Search
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "قوالب المستندات والتصاميم الجاهزة",
            fontSize = if (isCompact) 17.sp else 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
    }

    // Category Filter Chips
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        items(TemplateCategory.values()) { category ->
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = { selectedCategory = category },
                label = {
                    Text(
                        text = category.title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = Color(0xFF334155)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1),
                    enabled = true,
                    selected = isSelected
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }

    // Templates Horizontal Carousel with Visual Mock Paper Renderers
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isCompact) 28.dp else 36.dp)
    ) {
        // Option to open existing file directly
        item {
            Column(
                modifier = Modifier
                    .width(if (isCompact) 130.dp else 165.dp)
                    .clickable { onOpenFileLauncher() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isCompact) 180.dp else 225.dp)
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(6.dp))
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "فتح من الجهاز",
                            tint = Color(0xFFF59E0B), // Warm Yellow
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "فتح ملف",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "فتح من الجهاز",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    fontSize = if (isCompact) 13.sp else 14.sp
                )
                Text(
                    text = "استيراد docx",
                    color = Color(0xFF64748B),
                    fontSize = if (isCompact) 11.sp else 12.sp
                )
            }
        }

        // Render filtered templates
        items(filteredTemplates, key = { it.id }) { template ->
            TemplateCard(
                template = template,
                isCompact = isCompact,
                onClick = {
                    if (template.previewType == com.example.presentation.templates.TemplatePreviewType.BLANK) {
                        onNewDocument()
                    } else {
                        onNewDocumentFromTemplate(template)
                    }
                }
            )
        }
    }
    
    // Recent Cloud Documents Section
    Text(
        text = "المستندات الأخيرة",
        fontSize = if (isCompact) 17.sp else 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF202124),
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    if (currentUser == null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudQueue,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "يرجى تسجيل الدخول لعرض ومزامنة مستنداتك السحابية والوصول إليها من أي مكان.", 
                    color = Color(0xFF605E5C),
                    fontSize = 14.sp
                )
            }
        }
    } else if (isLoadingCloudDocs) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else if (cloudDocs.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Text(
                "لا توجد مستندات محفوظة في السحابة حتى الآن. سيتم حفظ أي مستند تنشئه تلقائياً عند طلب الحفظ السحابي.", 
                color = Color(0xFF605E5C),
                modifier = Modifier.padding(20.dp),
                fontSize = 14.sp
            )
        }
    } else {
        // List View for Recent Documents
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFEDEBE9), RoundedCornerShape(8.dp))
        ) {
            cloudDocs.forEachIndexed { index, doc ->
                RecentDocumentListItem(
                    doc = doc,
                    onClick = { onLoadFromCloud(doc) }
                )
                if (index < cloudDocs.size - 1) {
                    Divider(color = Color(0xFFEDEBE9), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
fun SidebarItem(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 15.sp
        )
    }
}

@Composable
fun RecentDocumentListItem(
    doc: com.example.presentation.cloud.CloudDocument,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Article,
            contentDescription = "Document",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = doc.title.ifEmpty { "مستند بدون اسم" },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF202124),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudQueue,
                    contentDescription = "Cloud",
                    tint = Color(0xFF605E5C),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "محفوظ في السحابة",
                    fontSize = 12.sp,
                    color = Color(0xFF605E5C)
                )
            }
        }
    }
}

@Composable
fun FeedbackDialog(onDismiss: () -> Unit) {
    var feedbackText by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "إرسال تعليق", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "يسعدنا سماع رأيك أو اقتراحاتك لتطوير البرنامج:", modifier = Modifier.padding(bottom = 12.dp))
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("اكتب تعليقك هنا...") },
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (feedbackText.isNotBlank()) {
                        android.widget.Toast.makeText(context, "شكراً لتعليقك! سيتم مراجعته.", android.widget.Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("إرسال", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.Gray)
            }
        }
    )
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoSaveEnabled by remember { mutableStateOf(true) }
    val currentTheme by com.example.ui.theme.ThemeManager.currentTheme.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "الخيارات", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("تفعيل الإشعارات", color = Color.Black)
                    Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                }
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("الحفظ التلقائي", color = Color.Black)
                    Switch(checked = autoSaveEnabled, onCheckedChange = { autoSaveEnabled = it })
                }
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(text = "مظهر البرنامج (الثيم)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    com.example.ui.theme.AppThemeOption.values().forEach { themeOption ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { com.example.ui.theme.ThemeManager.setTheme(context, themeOption) }
                                .background(if (currentTheme == themeOption) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == themeOption,
                                onClick = { com.example.ui.theme.ThemeManager.setTheme(context, themeOption) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = themeOption.title, color = Color.Black)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "حول البرنامج",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text("برنامج معالجة النصوص الاحترافي", fontSize = 14.sp, color = Color.Black)
                Text("الإصدار 1.0.0", fontSize = 12.sp, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    android.widget.Toast.makeText(context, "تم حفظ الإعدادات", android.widget.Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("موافق", color = Color.White)
            }
        }
    )
}

@Composable
fun FirebaseConfigDialog(
    onDismiss: () -> Unit,
    onSignInAsDemo: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Options, 1: Firebase setup steps

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تكوين الحساب السحابي",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tab Headers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { activeTab = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == 0) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0),
                            contentColor = if (activeTab == 0) Color.White else Color(0xFF475569)
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Text("الخيارات السريعة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { activeTab = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == 1) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0),
                            contentColor = if (activeTab == 1) Color.White else Color(0xFF475569)
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Text("خطوات التثبيت", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (activeTab == 0) {
                    Text(
                        text = "لم يتم العثور على ملف إعدادات Firebase الخاص بهذا التطبيق للتخزين السحابي. يمكنك الاختيار بين الخيارين أدناه:",
                        fontSize = 14.sp,
                        color = Color(0xFF334155),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Demo mode button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSignInAsDemo() },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                tint = Color(0xFF2563EB),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "تسجيل الدخول كحساب تجريبي محلي",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E40AF)
                                )
                                Text(
                                    text = "يسمح لك باستكشاف وحفظ ملفاتك السحابية مؤقتاً في ذاكرة التطبيق دون الحاجة لـ Firebase.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF1E40AF).copy(alpha = 0.8f),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "لتهيئة وتفعيل التخزين السحابي الفعلي في نسختك الخاصة من التطبيق، يرجى اتباع الخطوات التالية:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    val steps = listOf(
                        "1. اذهب إلى وحدة تحكم Firebase (firebase.google.com) وأنشئ مشروعاً جديداً.",
                        "2. أضف تطبيق Android جديد داخل المشروع باستخدام المعرّف (Application ID) الخاص بتطبيقك.",
                        "3. قم بتنزيل ملف الإعدادات المسمى `google-services.json`.",
                        "4. ضع الملف الذي قمت بتنزيله في المجلد الرئيسي لموديول التطبيق بالمسار التالي: `/app/google-services.json`.",
                        "5. أعد تصدير وبناء التطبيق لتفعيل الحفظ والاتصال بقاعدة بيانات السحابية مباشرة!"
                    )

                    steps.forEach { step ->
                        Text(
                            text = step,
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("حسناً")
            }
        }
    )
}
