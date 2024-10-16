package com.maliks.applocker.xtreme.util.helper.file

enum class FileExtension(val extension: String) {
    JPEG(".jpeg"),
    PNG(".png"),
    MP4(".mp4"),
    NONE("");

    companion object {
        fun getFileExtensionFromName(fileName: String): FileExtension {
            val extension = fileName.substringAfterLast('.', "").lowercase()
            return values().find { it.extension == ".$extension" } ?: NONE
        }
    }

}