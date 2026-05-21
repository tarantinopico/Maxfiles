package com.example.domain.models

import java.io.File

data class FileItem(
    val file: File,
    val name: String = file.name,
    val isDirectory: Boolean = file.isDirectory,
    val path: String = file.absolutePath,
    val size: Long = file.length(),
    val lastModified: Long = file.lastModified()
)
