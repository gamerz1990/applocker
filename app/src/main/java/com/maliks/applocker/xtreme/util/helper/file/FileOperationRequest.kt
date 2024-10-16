package com.maliks.applocker.xtreme.util.helper.file

data class FileOperationRequest(
    val fileName: String,
    val fileExtension: FileExtension,
    val directoryType: DirectoryType
)