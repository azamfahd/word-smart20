import os

content = """package com.example.presentation.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.auth.AuthManager
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun StartScreen(
    onNewDocument: () -> Unit,
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
                    android.widget.Toast.makeText(context, e.message ?: "فشل تسجيل الدخول", android.widget.Toast.LENGTH_SHORT).show()
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
                // Top App Bar for Mobile
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Article, contentDescription = "Word", tint = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Word",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row {
                        IconButton(onClick = handleLoginLogout) {
                            Icon(
                                imageVector = if (currentUser != null) Icons.Default.AccountCircle else Icons.Default.Login,
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
                
                // Mobile Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    StartScreenContent(
                        isCompact = true,
                        currentUser = currentUser,
                        isLoadingCloudDocs = isLoadingCloudDocs,
                        cloudDocs = cloudDocs,
                        onNewDocument = onNewDocument,
                        onOpenFileLauncher = { openFileLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword")) },
                        onLoadFromCloud = onLoadFromCloud
                    )
                }
            }
        } else {
            // Desktop/Tablet Layout (Microsoft Office Style)
            Row(modifier = Modifier.fillMaxSize()) {
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
                        .padding(horizontal = 48.dp, vertical = 40.dp)
                ) {
                    StartScreenContent(
                        isCompact = false,
                        currentUser = currentUser,
                        isLoadingCloudDocs = isLoadingCloudDocs,
                        cloudDocs = cloudDocs,
                        onNewDocument = onNewDocument,
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
}

@Composable
fun StartScreenContent(
    isCompact: Boolean,
    currentUser: com.google.firebase.auth.FirebaseUser?,
    isLoadingCloudDocs: Boolean,
    cloudDocs: List<com.example.presentation.cloud.CloudDocument>,
    onNewDocument: () -> Unit,
    onOpenFileLauncher: () -> Unit,
    onLoadFromCloud: (com.example.presentation.cloud.CloudDocument) -> Unit
) {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 5..11 -> "صباح الخير"
        in 12..17 -> "مساء الخير"
        else -> "مرحباً"
    }

    Text(
        text = greeting,
        fontSize = if (isCompact) 24.sp else 32.sp,
        fontWeight = FontWeight.Light,
        color = Color(0xFF202124),
        modifier = Modifier.padding(bottom = if (isCompact) 20.dp else 32.dp)
    )
    
    Text(
        text = "جديد",
        fontSize = if (isCompact) 18.sp else 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF202124),
        modifier = Modifier.padding(bottom = 16.dp)
    )

    LazyRow(
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
                title = "فتح من الجهاز",
                isCompact = isCompact,
                isFolder = true,
                onClick = onOpenFileLauncher
            )
        }
    }
    
    Text(
        text = "الأخيرة",
        fontSize = if (isCompact) 18.sp else 20.sp,
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
            Text(
                "يرجى تسجيل الدخول لعرض ومزامنة مستنداتك السحابية.", 
                color = Color(0xFF605E5C),
                modifier = Modifier.padding(20.dp)
            )
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
                "لا توجد مستندات محفوظة في السحابة حتى الآن.", 
                color = Color(0xFF605E5C),
                modifier = Modifier.padding(20.dp)
            )
        }
    } else {
        // List View for Recent Documents (Like MS Word)
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
fun DocumentTemplateCard(
    title: String,
    isCompact: Boolean,
    isFolder: Boolean = false,
    onClick: () -> Unit
) {
    val cardWidth = if (isCompact) 130.dp else 160.dp
    val cardHeight = if (isCompact) 180.dp else 220.dp
    
    Column(
        modifier = Modifier
            .width(cardWidth)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .shadow(2.dp, RoundedCornerShape(4.dp))
                .background(Color.White)
                .clip(RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isFolder) {
                 Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = title,
                    tint = Color(0xFFF3C31A), // Folder Yellow
                    modifier = Modifier.size(48.dp)
                )
            } else {
                // Blank Page Icon representation
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Text(
            text = title,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF202124),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 12.dp)
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
            imageVector = Icons.Outlined.Article,
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
"""

with open("app/src/main/java/com/example/presentation/editor/StartScreen.kt", "w") as f:
    f.write(content)

