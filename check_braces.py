with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'r') as f:
    content = f.read()

# find where A4PageCard starts
index = content.find('fun A4PageCard')
if index != -1:
    content = content[:index]

# count { and }
opens = content.count('{')
closes = content.count('}')
print(f"Opens: {opens}, Closes: {closes}, Diff: {opens - closes}")
