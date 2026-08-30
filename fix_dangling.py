with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'r') as f:
    content = f.read()

content = content.replace("@Composable\n}\nfun A4PageCard", "}\n@Composable\nfun A4PageCard")

with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'w') as f:
    f.write(content)
print("Dangling fixed")
