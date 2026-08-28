package com.example.presentation.editor

import java.io.OutputStream

class DocxExporter {
    fun export(state: EditorState, outputStream: OutputStream) {
        DocxEngine.exportDocx(state, outputStream)
    }
}
