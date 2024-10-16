package com.maliks.applocker.xtreme.util.encryptor

import com.maliks.applocker.xtreme.util.helper.file.DirectoryType
import com.maliks.applocker.xtreme.util.helper.file.FileExtension

data class EncryptFileOperationRequest(
    val fileName: String,
    val fileExtension: FileExtension,
    val directoryType: DirectoryType
)