with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'r') as f:
    content = f.read()

bad_string = """@Composable
            }
            // Ink Drawing Overlay
            InkCanvasOverlay(state = state, pageIndex = pageNumber - 1, onEvent = onEvent)
fun PageFooterZone("""

good_string = """@Composable
fun PageFooterZone("""

content = content.replace(bad_string, good_string)

with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'w') as f:
    f.write(content)
print("Garbage fixed")
