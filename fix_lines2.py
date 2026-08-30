with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'r') as f:
    lines = f.readlines()

new_lines = lines[:451] + ["}\n", "}\n", "}\n"] + lines[451:]

with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'w') as f:
    f.writelines(new_lines)
