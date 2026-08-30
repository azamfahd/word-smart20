with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'r') as f:
    lines = f.readlines()

new_lines = lines[:448] + ["    }\n", "}\n", "\n", "@Composable\n", "fun A4PageCard(\n"] + lines[456:]

with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'w') as f:
    f.writelines(new_lines)

print("Fixed lines")
