package com.m22reader.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.m22reader.data.model.Book
import com.m22reader.data.model.BookFormat
import com.m22reader.data.repository.BookRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: BookRepository,
    private val metadataExtractor: MetadataExtractor
) {
    /**
     * Import a file from a content URI into the app's internal storage.
     * Returns the new Book ID on success, or null on failure.
     */
    suspend fun import(uri: Uri): Long? = withContext(Dispatchers.IO) {
        val fileName  = uri.resolveFileName(context) ?: return@withContext null
        val extension = fileName.substringAfterLast('.', "").uppercase()
        val format    = BookFormat.entries.firstOrNull { it.name == extension } ?: return@withContext null

        // Copy to internal storage
        val destDir = File(context.filesDir, "books").also { it.mkdirs() }
        val destFile = File(destDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output -> input.copyTo(output) }
        } ?: return@withContext null

        // Extract metadata
        val meta = metadataExtractor.extract(destFile, format)

        val book = Book(
            title         = meta.title ?: fileName.substringBeforeLast('.'),
            author        = meta.author ?: "Desconhecido",
            filePath      = destFile.absolutePath,
            format        = format,
            coverPath     = meta.coverPath,
            totalChapters = meta.chapterCount,
        )
        repo.addBook(book)
    }

    private fun Uri.resolveFileName(ctx: Context): String? {
        ctx.contentResolver.query(this, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (col >= 0) return cursor.getString(col)
            }
        }
        return lastPathSegment
    }
}

// ── Metadata ──────────────────────────────────────────────────────────────────
data class BookMeta(
    val title: String? = null,
    val author: String? = null,
    val coverPath: String? = null,
    val chapterCount: Int = 0,
)

@Singleton
class MetadataExtractor @Inject constructor(@ApplicationContext private val context: Context) {
    fun extract(file: File, format: BookFormat): BookMeta {
        return when (format) {
            BookFormat.EPUB -> extractEpub(file)
            BookFormat.CBZ  -> extractCbz(file)
            BookFormat.CBR  -> extractCbr(file)
            BookFormat.PDF  -> extractPdf(file)
        }
    }

    private fun extractEpub(file: File): BookMeta {
        return try {
            val book = nl.siegmann.epublib.epub.EpubReader().readEpub(file.inputStream())
            BookMeta(
                title        = book.title,
                author       = book.metadata.authors.firstOrNull()?.let { "${it.firstname} ${it.lastname}".trim() },
                chapterCount = book.tableOfContents.tocReferences.size,
            )
        } catch (e: Exception) { BookMeta() }
    }

    private fun extractCbz(file: File): BookMeta {
        return try {
            val zip = java.util.zip.ZipFile(file)
            val imageCount = zip.entries().asSequence().count { it.name.isImageFile() }
            zip.close()
            BookMeta(chapterCount = imageCount)
        } catch (e: Exception) { BookMeta() }
    }

    private fun extractCbr(file: File): BookMeta {
        return try {
            val archive = com.github.junrar.Archive(file)
            val count = archive.fileHeaders.count { it.fileName.isImageFile() }
            archive.close()
            BookMeta(chapterCount = count)
        } catch (e: Exception) { BookMeta() }
    }

    private fun extractPdf(file: File): BookMeta {
        return try {
            val fd = android.os.ParcelFileDescriptor.open(file, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = android.graphics.pdf.PdfRenderer(fd)
            val pageCount = renderer.pageCount
            renderer.close()
            fd.close()
            BookMeta(chapterCount = pageCount)
        } catch (e: Exception) { BookMeta() }
    }

    private fun String.isImageFile() = lowercase().let {
        it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".png") || it.endsWith(".webp")
    }
}
