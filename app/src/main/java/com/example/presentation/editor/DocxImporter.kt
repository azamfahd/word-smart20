package com.example.presentation.editor

import java.io.InputStream

class DocxImporter {
    fun import(inputStream: InputStream): List<DocumentBlock> {
        val model = DocxEngine.parseDocx(inputStream)
        return model.blocks
    }
}
