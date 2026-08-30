with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'r') as f:
    content = f.read()

pattern1 = """                            InkCanvasOverlay(state = state, pageIndex = 0, onEvent = onEvent)
                        }
                        }
                    }
                }
            }"""

replacement1 = """                            InkCanvasOverlay(state = state, pageIndex = 0, onEvent = onEvent)
                        }
                    }
                }
            }"""

# Do it for both occurrences
content = content.replace(pattern1, replacement1)

with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'w') as f:
    f.write(content)
print("Braces fixed")
