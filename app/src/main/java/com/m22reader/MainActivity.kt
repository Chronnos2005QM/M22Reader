package com.m22reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.m22reader.ui.M22AppScaffold
import com.m22reader.ui.theme.M22ReaderTheme
import com.m22reader.utils.FileImporter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var fileImporter: FileImporter

    // File picker — supports PDF, EPUB, CBZ, CBR
    private val filePicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult
        CoroutineScope(Dispatchers.IO).launch {
            fileImporter.import(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            var darkTheme by remember { mutableStateOf(true) }

            M22ReaderTheme(darkTheme) {
                M22AppScaffold(
                    darkTheme      = darkTheme,
                    onToggleTheme  = { darkTheme = !darkTheme },
                    onImportFile   = {
                        filePicker.launch(
                            arrayOf(
                                "application/pdf",
                                "application/epub+zip",
                                "application/x-cbz",
                                "application/x-cbr",
                                "application/zip",
                                "application/x-rar-compressed",
                            )
                        )
                    }
                )
            }
        }
    }
}
