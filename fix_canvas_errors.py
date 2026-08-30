with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'r') as f:
    content = f.read()

# Fix 'when' exhaustiveness
pattern_when = """            ViewMode.WEB_LAYOUT -> {
                // Continuous fluid layout"""
replacement_when = """            ViewMode.WEB_LAYOUT -> {
                // Continuous fluid layout"""
content = content.replace(pattern_when, replacement_when) # Wait, let's just add READ_MODE -> {}
content = content.replace("            ViewMode.WEB_LAYOUT -> {", "            ViewMode.READ_MODE -> { /* Read Mode implemented separately or fallback */ }\n            ViewMode.WEB_LAYOUT -> {")

with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'w') as f:
    f.write(content)
print("When fixed")
