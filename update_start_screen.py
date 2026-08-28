import re

with open('app/src/main/java/com/example/presentation/editor/StartScreen.kt', 'r') as f:
    content = f.read()

# I will just create a new file entirely, replacing the old one. 
# But wait, I can just use a full file overwrite.

