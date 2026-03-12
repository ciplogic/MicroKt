package org.example.common

import java.io.File

@CppExternal
fun readFileAsText(fileName: String): String {
    val file = File(fileName)
    return file.readText()
}

fun writeFileAsText(fileName: String, content: String) {
    val file = File(fileName)
    file.writeText(content)
}

fun scanFolderByExtension(folderName: String, extension: String): List<File> {
    val folder = File(folderName)
    if (!folder.exists() || !folder.isDirectory) return emptyList()

    val normalizedExtension = extension.removePrefix(".")
    return folder
        .walkTopDown()
        .filter { it.isFile && it.extension == normalizedExtension }
        .toList()
}