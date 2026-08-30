cat << 'INNER_EOF' > app/src/main/java/com/example/presentation/editor/components/InkCanvasOverlay.kt
package com.example.presentation.editor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.presentation.editor.DrawingPath
import com.example.presentation.editor.EditorState
import com.example.presentation.editor.RibbonEvent

@Composable
fun InkCanvasOverlay(state: EditorState, onEvent: (RibbonEvent) -> Unit) {
    var currentPath by remember { mutableStateOf<Path?>(null) }
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(state.isDrawingMode, state.inkColor, state.inkThickness, state.isHighlighterMode, state.isEraserMode) {
                if (state.isDrawingMode) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val path = Path().apply { moveTo(offset.x, offset.y) }
                            currentPath = path
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            currentPath?.lineTo(change.position.x, change.position.y)
                        },
                        onDragEnd = {
                            currentPath?.let {
                                val dp = DrawingPath(
                                    path = it,
                                    color = if (state.isEraserMode) Color.Transparent else if (state.isHighlighterMode) state.inkColor.copy(alpha = 0.4f) else state.inkColor,
                                    strokeWidth = if (state.isEraserMode) 30f else state.inkThickness,
                                    isHighlighter = state.isHighlighterMode
                                )
                                onEvent(RibbonEvent.OnDrawPathAdded(dp))
                            }
                            currentPath = null
                        },
                        onDragCancel = {
                            currentPath = null
                        }
                    )
                }
            }
    ) {
        // Draw existing paths
        state.drawingPaths.forEach { dp ->
            drawPath(
                path = dp.path,
                color = dp.color,
                style = Stroke(
                    width = dp.strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                blendMode = if (dp.color == Color.Transparent) BlendMode.Clear else BlendMode.SrcOver
            )
        }
        
        // Draw current path
        currentPath?.let {
            drawPath(
                path = it,
                color = if (state.isEraserMode) Color.Transparent else if (state.isHighlighterMode) state.inkColor.copy(alpha = 0.4f) else state.inkColor,
                style = Stroke(
                    width = if (state.isEraserMode) 30f else state.inkThickness,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                blendMode = if (state.isEraserMode) BlendMode.Clear else BlendMode.SrcOver
            )
        }
    }
}
INNER_EOF
