package com.m22reader.data.model

data class AppSettings(
    val libraryFolderPath: String = "",
    val backupFolderPath: String = "",
    val darkTheme: Boolean = true,
    val defaultReadingMode: String = "VERTICAL_SCROLL",
    val autoScanLibrary: Boolean = true,
)
