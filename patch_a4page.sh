sed -i '/PageFooterZone(/i \
            }\n            // Ink Drawing Overlay\n            InkCanvasOverlay(state = state, pageIndex = pageNumber - 1, onEvent = onEvent)\n' app/src/main/java/com/example/presentation/editor/components/DocumentCanvas.kt
