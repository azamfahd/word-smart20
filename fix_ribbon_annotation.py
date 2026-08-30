with open('app/src/main/java/com/example/presentation/editor/components/RibbonUI.kt', 'r') as f:
    content = f.read()

content = content.replace("@Composable\n@Composable\nfun DrawTabContent", "@Composable\nfun DrawTabContent")

with open('app/src/main/java/com/example/presentation/editor/components/RibbonUI.kt', 'w') as f:
    f.write(content)
