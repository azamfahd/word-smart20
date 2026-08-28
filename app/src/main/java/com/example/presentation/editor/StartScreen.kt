package com.example.presentation.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StartScreen(
    onNewDocument: () -> Unit,
    onOpenFile: (Uri) -> Unit
) {
    val context = LocalContext.current
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onOpenFile(it) }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Blue Sidebar
        Box(
            modifier = Modifier
                .width(250.dp)
                .fillMaxHeight()
                .background(Color(0xFF185ABD))
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Word",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 48.dp)
                )

                // Navigation items in sidebar (mock)
                SidebarItem(text = "Home", isSelected = true)
                Spacer(modifier = Modifier.height(16.dp))
                SidebarItem(text = "New", isSelected = false)
                Spacer(modifier = Modifier.height(16.dp))
                SidebarItem(text = "Open", isSelected = false)
            }
            
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                SidebarItem(text = "Account", isSelected = false)
                Spacer(modifier = Modifier.height(16.dp))
                SidebarItem(text = "Feedback", isSelected = false)
                Spacer(modifier = Modifier.height(16.dp))
                SidebarItem(text = "Options", isSelected = false)
            }
        }

        // Main Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp)
        ) {
            Text(
                text = "New",
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                // Blank Document Card
                DocumentActionCard(
                    icon = Icons.Default.Add,
                    title = "Blank document",
                    onClick = onNewDocument
                )

                // Open File Card
                DocumentActionCard(
                    icon = Icons.Default.FolderOpen,
                    title = "Open a document",
                    onClick = {
                        openFileLauncher.launch(
                            arrayOf(
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "application/msword"
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun SidebarItem(text: String, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent)
            .padding(12.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 16.sp
        )
    }
}

@Composable
fun DocumentActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1 / 1.414f)
                .background(Color.White)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                .padding(1.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF185ABD),
                modifier = Modifier.size(48.dp)
            )
        }
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
