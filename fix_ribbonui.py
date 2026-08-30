import re

with open('app/src/main/java/com/example/presentation/editor/components/RibbonUI.kt', 'r') as f:
    content = f.read()

pattern = re.compile(r'fun DrawTabContent\(state: EditorState, onEvent: \(RibbonEvent\) -> Unit\) \{.*?(?=fun ReferencesTabContent)', re.DOTALL)
replacement = """fun DrawTabContent(state: EditorState, onEvent: (RibbonEvent) -> Unit) {
    var showColorMenu by remember { mutableStateOf(false) }
    var showThicknessMenu by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        RibbonGroup(localize("Tools", state.isRtl)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RibbonToggleButton(
                        icon = Icons.Default.Edit,
                        isChecked = state.isDrawingMode && !state.isHighlighterMode && !state.isEraserMode
                    ) { onEvent(RibbonEvent.OnToggleDrawingMode) }
                    Text(localize("Draw", state.isRtl), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RibbonToggleButton(
                        icon = Icons.Default.Brush,
                        isChecked = state.isDrawingMode && state.isHighlighterMode
                    ) { onEvent(RibbonEvent.OnToggleHighlighterMode) }
                    Text(localize("Highlighter", state.isRtl), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RibbonToggleButton(
                        icon = Icons.Default.FormatPaint,
                        isChecked = state.isDrawingMode && state.isEraserMode
                    ) { onEvent(RibbonEvent.OnToggleEraserMode) }
                    Text(localize("Eraser", state.isRtl), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }

        RibbonGroup(localize("Format", state.isRtl)) {
            Box {
                RibbonLargeButton(
                    icon = Icons.Default.Palette,
                    label = localize("Color", state.isRtl)
                ) { showColorMenu = true }
                
                DropdownMenu(expanded = showColorMenu, onDismissRequest = { showColorMenu = false }) {
                    val colors = listOf(Color.Black, Color.Red, Color.Blue, Color.Green, Color(0xFFFFEB3B))
                    colors.forEach { color ->
                        DropdownMenuItem(
                            text = { Text("Color") },
                            onClick = { 
                                onEvent(RibbonEvent.OnInkColorChanged(color))
                                showColorMenu = false
                            }
                        )
                    }
                }
            }

            Box {
                RibbonLargeButton(
                    icon = Icons.Default.Edit,
                    label = localize("Thickness", state.isRtl)
                ) { showThicknessMenu = true }
                
                DropdownMenu(expanded = showThicknessMenu, onDismissRequest = { showThicknessMenu = false }) {
                    val thicknesses = listOf(2f, 4f, 8f, 12f)
                    thicknesses.forEach { thickness ->
                        DropdownMenuItem(
                            text = { Text("${thickness.toInt()} pt") },
                            onClick = { 
                                onEvent(RibbonEvent.OnInkThicknessChanged(thickness))
                                showThicknessMenu = false
                            }
                        )
                    }
                }
            }
        }

        RibbonGroup(localize("Canvas", state.isRtl)) {
            RibbonLargeButton(
                icon = Icons.Default.Delete,
                label = localize("Clear All", state.isRtl)
            ) { onEvent(RibbonEvent.OnClearDrawing) }
        }
    }
}

@Composable
"""

content = pattern.sub(replacement, content)
with open('app/src/main/java/com/example/presentation/editor/components/RibbonUI.kt', 'w') as f:
    f.write(content)
print("DrawTabContent fixed")
