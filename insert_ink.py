with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'r') as f:
    content = f.read()

pattern = """                )
            }
        }
    }
}"""

replacement = """                )
            }
            // Ink Drawing Overlay
            InkCanvasOverlay(state = state, pageIndex = pageNumber - 1, onEvent = onEvent)
        }
    }
}"""

if pattern in content:
    content = content.replace(pattern, replacement)
    with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Pattern not found!")
