cat << 'INNER_EOF' > script.py
import re

with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'r') as f:
    content = f.read()

bad_pattern = """                // Footer Area (Shows page number & custom footer text)
            }
            // Ink Drawing Overlay
            InkCanvasOverlay(state = state, pageIndex = pageNumber - 1, onEvent = onEvent)

                PageFooterZone("""

good_pattern = """                // Footer Area (Shows page number & custom footer text)
                PageFooterZone("""

content = content.replace(bad_pattern, good_pattern)

with open('app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt', 'w') as f:
    f.write(content)

INNER_EOF
python3 script.py
