with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'r') as f:
    content = f.read()

pattern = """                        Card(
                            modifier = Modifier
                                .widthIn(max = 760.dp)
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 40.dp, vertical = 36.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {"""

replacement = """                        Card(
                            modifier = Modifier
                                .widthIn(max = 760.dp)
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9))
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 40.dp, vertical = 36.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {"""

pattern2 = """                                state.blocks.forEach { block ->
                                    if (block !is PageBreakBlock) {
                                        RenderDocumentBlock(block = block, state = state, onEvent = onEvent)
                                    }
                                }
                            }
                        }
                    }
                }"""

replacement2 = """                                state.blocks.forEach { block ->
                                    if (block !is PageBreakBlock) {
                                        RenderDocumentBlock(block = block, state = state, onEvent = onEvent)
                                    }
                                }
                            }
                            InkCanvasOverlay(state = state, pageIndex = 0, onEvent = onEvent)
                        }
                        }
                    }
                }"""

if pattern in content and pattern2 in content:
    content = content.replace(pattern, replacement).replace(pattern2, replacement2)
    with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Pattern not found!")
