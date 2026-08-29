package com.example.presentation.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Microsoft Office Standard Theme Palette Matrix (10 columns x 6 luminance rows = 60 Theme Colors)
 */
object OfficeColorPalettes {
    // 10 Base Theme Colors
    val themeBaseColors = listOf(
        Color(0xFFFFFFFF), // White
        Color(0xFF000000), // Black
        Color(0xFFE7E6E6), // Light Grey
        Color(0xFF44546A), // Dark Slate
        Color(0xFF4472C4), // Blue (Accent 1)
        Color(0xFFED7D31), // Orange (Accent 2)
        Color(0xFFA5A5A5), // Grey (Accent 3)
        Color(0xFFFFC000), // Gold (Accent 4)
        Color(0xFF5B9BD5), // Sky Blue (Accent 5)
        Color(0xFF70AD47)  // Green (Accent 6)
    )

    // Complete 60-color theme matrix (row 0 = Base, rows 1-5 = tints and shades)
    val themeFullMatrix: List<List<Color>> = listOf(
        // Row 1 (Base Theme Colors)
        listOf(
            Color(0xFFFFFFFF), Color(0xFF000000), Color(0xFFEEECE1), Color(0xFF1F497D),
            Color(0xFF4F81BD), Color(0xFFC0504D), Color(0xFF9BBB59), Color(0xFF8064A2),
            Color(0xFF4BACC6), Color(0xFFF79646)
        ),
        // Row 2 (Tints 80%)
        listOf(
            Color(0xFFF2F2F2), Color(0xFF7F7F7F), Color(0xFFDDD9C3), Color(0xFFC6D9F1),
            Color(0xFFDBE5F1), Color(0xFFF2DCDB), Color(0xFFEBF1DD), Color(0xFFE5E0EC),
            Color(0xFFDBEEF3), Color(0xFFFDEADA)
        ),
        // Row 3 (Tints 60%)
        listOf(
            Color(0xFFD8D8D8), Color(0xFF595959), Color(0xFFC4BD97), Color(0xFF8DB3E2),
            Color(0xFFB8CCE4), Color(0xFFE5B9B5), Color(0xFFD6E3BC), Color(0xFFCCC0DA),
            Color(0xFFB7DDE8), Color(0xFFFBD5B5)
        ),
        // Row 4 (Tints/Shades 40%)
        listOf(
            Color(0xFFBFBFBF), Color(0xFF3F3F3F), Color(0xFF938953), Color(0xFF548DD4),
            Color(0xFF95B3D7), Color(0xFFD9958F), Color(0xFFC2D59B), Color(0xFFB2A2C7),
            Color(0xFF92CDDC), Color(0xFFFAC08F)
        ),
        // Row 5 (Shades 25%)
        listOf(
            Color(0xFFA5A5A5), Color(0xFF262626), Color(0xFF494429), Color(0xFF17365D),
            Color(0xFF365F91), Color(0xFF953734), Color(0xFF76923C), Color(0xFF5F497A),
            Color(0xFF31849B), Color(0xFFE36C0A)
        ),
        // Row 6 (Shades 50%)
        listOf(
            Color(0xFF7F7F7F), Color(0xFF0C0C0C), Color(0xFF1D1B10), Color(0xFF0F243E),
            Color(0xFF243F60), Color(0xFF632423), Color(0xFF4F6128), Color(0xFF3F3151),
            Color(0xFF205867), Color(0xFF974806)
        )
    )

    // Standard Office Rainbow Row
    val standardColors = listOf(
        Color(0xFFC00000), // Dark Red
        Color(0xFFFF0000), // Red
        Color(0xFFFFC000), // Orange/Gold
        Color(0xFFFFFF00), // Yellow
        Color(0xFF92D050), // Light Green
        Color(0xFF00B050), // Green
        Color(0xFF00B0F0), // Light Blue
        Color(0xFF0070C0), // Blue
        Color(0xFF002060), // Dark Blue
        Color(0xFF7030A0)  // Purple
    )

    // Vibrant Modern UI Palette
    val curatedPresets = listOf(
        Color(0xFF185ABD), Color(0xFF2563EB), Color(0xFF3B82F6), Color(0xFF0284C7),
        Color(0xFF0D9488), Color(0xFF10B981), Color(0xFF16A34A), Color(0xFF65A30D),
        Color(0xFFEAB308), Color(0xFFF59E0B), Color(0xFFEA580C), Color(0xFFEF4444),
        Color(0xFFDC2626), Color(0xFFE11D48), Color(0xFFDB2777), Color(0xFFC026D3),
        Color(0xFF9333EA), Color(0xFF7C3AED), Color(0xFF4F46E5), Color(0xFF475569),
        Color(0xFF0F172A), Color(0xFF78350F), Color(0xFF713F12), Color(0xFF1E293B)
    )

    // Recent colors memory storage during app session
    val recentColors = mutableStateListOf<Color>()

    fun addRecentColor(color: Color) {
        if (color !in recentColors) {
            recentColors.add(0, color)
            if (recentColors.size > 12) {
                recentColors.removeLast()
            }
        }
    }
}

/**
 * Color Studio Dialog - Unrestricted, complete color customization dialog
 * Allows choosing from Theme Palette, Standard Rainbow, or custom RGB/HEX sliders.
 */
@Composable
fun ColorStudioDialog(
    initialColor: Color,
    title: String = "استوديو الألوان والتخصيص الكامل",
    isRtl: Boolean = true,
    allowTransparent: Boolean = false,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Palette, 1: Custom RGB & HEX, 2: Presets
    var currentColor by remember { mutableStateOf(initialColor) }

    // RGB Slider states
    var redVal by remember { mutableFloatStateOf(initialColor.red * 255f) }
    var greenVal by remember { mutableFloatStateOf(initialColor.green * 255f) }
    var blueVal by remember { mutableFloatStateOf(initialColor.blue * 255f) }
    var alphaVal by remember { mutableFloatStateOf(initialColor.alpha * 100f) }

    var hexInput by remember {
        mutableStateOf(
            String.format(
                "#%02X%02X%02X",
                (initialColor.red * 255).toInt().coerceIn(0, 255),
                (initialColor.green * 255).toInt().coerceIn(0, 255),
                (initialColor.blue * 255).toInt().coerceIn(0, 255)
            )
        )
    }

    // Function to sync sliders to a new color
    fun updateFromColor(c: Color) {
        currentColor = c
        redVal = c.red * 255f
        greenVal = c.green * 255f
        blueVal = c.blue * 255f
        alphaVal = c.alpha * 100f
        hexInput = String.format(
            "#%02X%02X%02X",
            (c.red * 255).toInt().coerceIn(0, 255),
            (c.green * 255).toInt().coerceIn(0, 255),
            (c.blue * 255).toInt().coerceIn(0, 255)
        )
    }

    // Update color when sliders change
    fun updateFromSliders() {
        val r = (redVal / 255f).coerceIn(0f, 1f)
        val g = (greenVal / 255f).coerceIn(0f, 1f)
        val b = (blueVal / 255f).coerceIn(0f, 1f)
        val a = (alphaVal / 100f).coerceIn(0f, 1f)
        currentColor = Color(red = r, green = g, blue = b, alpha = a)
        hexInput = String.format(
            "#%02X%02X%02X",
            redVal.toInt().coerceIn(0, 255),
            greenVal.toInt().coerceIn(0, 255),
            blueVal.toInt().coerceIn(0, 255)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header (Microsoft Word Color Dialog Title Bar)
                Surface(
                    color = Color(0xFF185ABD),
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = "Word Colors",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isRtl) "مربع حوار الألوان — Microsoft Word" else "Colors Dialog — Microsoft Word",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Live Color Comparison Bar (Current vs New Color)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Original
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (initialColor == Color.Transparent) Color.White else initialColor)
                                    .border(1.dp, Color.Gray, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (initialColor == Color.Transparent) {
                                    Text("✕", fontSize = 14.sp, color = Color.Red)
                                }
                            }
                            Column {
                                Text(if (isRtl) "اللون السابق" else "Original", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    if (initialColor == Color.Transparent) "شفاف" else String.format("#%06X", (initialColor.toArgb() and 0xFFFFFF)),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )

                        // New Selected
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(if (isRtl) "اللون الجديد المختار" else "Selected", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    if (currentColor == Color.Transparent) "شفاف" else hexInput.uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (currentColor == Color.Transparent) Color.White else currentColor)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentColor == Color.Transparent) {
                                    Text("✕", fontSize = 16.sp, color = Color.Red)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode Tabs (السمات، مخصص RGB/HEX، الألوان المنسقة)
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(if (isRtl) "لوحة وورد (Word Palette)" else "Theme Matrix", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(if (isRtl) "مخصص (RGB / HEX)" else "Custom RGB/HEX", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(if (isRtl) "ألوان عصرية (Vibrant)" else "Curated Presets", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Content for each tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> {
                            // 1. Office Theme Palette Grid (60 colors + Standard Rainbow Row)
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (allowTransparent) {
                                    OutlinedButton(
                                        onClick = { updateFromColor(Color.Transparent) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (isRtl) "بلا لون (شفاف / بدون تعبئة)" else "No Color (Transparent)")
                                    }
                                }

                                Text(
                                    text = if (isRtl) "ألوان نسق مايكروسوفت أوفيس (Theme Colors):" else "Office Theme Colors:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )

                                // 6 rows of 10 colors
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                        .padding(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    OfficeColorPalettes.themeFullMatrix.forEachIndexed { rowIndex, rowColors ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            rowColors.forEach { color ->
                                                val isSelected = currentColor == color
                                                Box(
                                                    modifier = Modifier
                                                        .size(26.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(color)
                                                        .border(
                                                            width = if (isSelected) 2.dp else 1.dp,
                                                            color = if (isSelected) Color(0xFF185ABD) else Color(0xFFE2E8F0),
                                                            shape = RoundedCornerShape(3.dp)
                                                        )
                                                        .clickable { updateFromColor(color) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = if (color.red * 0.3 + color.green * 0.59 + color.blue * 0.11 > 0.6) Color.Black else Color.White,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (rowIndex == 0) {
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = Color(0xFFCBD5E1))
                                        }
                                    }
                                }

                                Text(
                                    text = if (isRtl) "الألوان القياسية (Standard Colors):" else "Standard Colors:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    OfficeColorPalettes.standardColors.forEach { color ->
                                        val isSelected = currentColor == color
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(color)
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) Color(0xFF185ABD) else Color(0xFFE2E8F0),
                                                    shape = RoundedCornerShape(3.dp)
                                                )
                                                .clickable { updateFromColor(color) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (OfficeColorPalettes.recentColors.isNotEmpty()) {
                                    Text(
                                        text = if (isRtl) "الألوان الأخيرة المستخدمة:" else "Recent Colors:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OfficeColorPalettes.recentColors.forEach { color ->
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(color)
                                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                                                    .clickable { updateFromColor(color) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // 2. Custom RGB, HEX & Sliders
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Direct HEX input
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = hexInput,
                                        onValueChange = { input ->
                                            hexInput = input
                                            val clean = input.removePrefix("#").trim()
                                            if (clean.length == 6) {
                                                try {
                                                    val parsed = android.graphics.Color.parseColor("#$clean")
                                                    val newC = Color(parsed)
                                                    currentColor = newC
                                                    redVal = newC.red * 255f
                                                    greenVal = newC.green * 255f
                                                    blueVal = newC.blue * 255f
                                                } catch (_: Exception) {}
                                            }
                                        },
                                        label = { Text(if (isRtl) "كود اللون (Hex Code)" else "Hex Color Code") },
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(currentColor)
                                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                    )
                                }

                                // Quick Rainbow Hue Gradient Strip
                                Column {
                                    Text(
                                        text = if (isRtl) "شريط الطيف اللوني السريع:" else "Spectrum Quick Strip:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(26.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        Color.Red, Color.Yellow, Color.Green,
                                                        Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                                                    )
                                                )
                                            )
                                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                    )
                                }

                                // Red Slider (0-255)
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(if (isRtl) "الأحمر (Red):" else "Red:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                        Text("${redVal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Slider(
                                        value = redVal,
                                        onValueChange = {
                                            redVal = it
                                            updateFromSliders()
                                        },
                                        valueRange = 0f..255f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFFDC2626),
                                            activeTrackColor = Color(0xFFDC2626)
                                        )
                                    )
                                }

                                // Green Slider (0-255)
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(if (isRtl) "الأخضر (Green):" else "Green:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                        Text("${greenVal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Slider(
                                        value = greenVal,
                                        onValueChange = {
                                            greenVal = it
                                            updateFromSliders()
                                        },
                                        valueRange = 0f..255f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFF16A34A),
                                            activeTrackColor = Color(0xFF16A34A)
                                        )
                                    )
                                }

                                // Blue Slider (0-255)
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(if (isRtl) "الأزرق (Blue):" else "Blue:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                        Text("${blueVal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Slider(
                                        value = blueVal,
                                        onValueChange = {
                                            blueVal = it
                                            updateFromSliders()
                                        },
                                        valueRange = 0f..255f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFF2563EB),
                                            activeTrackColor = Color(0xFF2563EB)
                                        )
                                    )
                                }

                                // Opacity / Alpha Slider (0-100%)
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(if (isRtl) "الشفافية / الكثافة (Opacity):" else "Opacity:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                                        Text("${alphaVal.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Slider(
                                        value = alphaVal,
                                        onValueChange = {
                                            alphaVal = it
                                            updateFromSliders()
                                        },
                                        valueRange = 0f..100f
                                    )
                                }
                            }
                        }

                        2 -> {
                            // 3. Curated Modern Presets
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (isRtl) "باقة ألوان الأعمال والتصميم الحديث:" else "Modern Vibrant Presets:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(6),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(OfficeColorPalettes.curatedPresets) { color ->
                                        val isSelected = currentColor == color
                                        Box(
                                            modifier = Modifier
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(color)
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) Color.Black else Color(0xFFCBD5E1),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable { updateFromColor(color) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(10.dp))

                // Actions Footer (Microsoft Word Style OK & Cancel)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (currentColor != Color.Transparent) {
                                OfficeColorPalettes.addRecentColor(currentColor)
                            }
                            onColorSelected(currentColor)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185ABD)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isRtl) "موافق" else "OK", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text(if (isRtl) "إلغاء الأمر" else "Cancel", fontSize = 13.sp, color = Color(0xFF334155))
                    }
                }
            }
        }
    }
}
