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
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: BookRepository,
    private val metadataExtractor: MetadataExtractor
) {
    suspend fun import(uri: Uri): Long? = withContext(Dispatchers.IO) {
        val fileName  = uri.resolveFileName(context) ?: return@withContext null
        val extension = fileName.substringAfterLast('.', "").uppercase()
        val format    = BookFormat.entries.firstOrNull { it.name == extension } ?: return@withContext null

        val destDir  = File(context.filesDir, "books").also { it.mkdirs() }
        val destFile = File(destDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output -> input.copyTo(output) }
        } ?: return@withContext null

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
            val zip = ZipFile(file)
            val count = zip.entries().asSequence()
                .count { it.name.endsWith(".html") || it.name.endsWith(".xhtml") }
            zip.close()
            BookMeta(chapterCount = count)
        } catch (e: Exception) { BookMeta() }
    }

    private fun extractCbz(file: File): BookMeta {
        return try {
            val zip = ZipFile(file)
            val count = zip.entries().asSequence()
                .count { it.name.lowercase().let { n -> n.endsWith(".jpg") || n.endsWith(".png") || n.endsWith(".webp") } }
            zip.close()
            BookMeta(chapterCount = count)
        } catch (e: Exception) { BookMeta() }
    }

    private fun extractCbr(file: File): BookMeta {
        return try {
            val archive = com.github.junrar.Archive(file)
            val count = archive.fileHeaders.count {
                it.fileName.lowercase().let { n -> n.endsWith(".jpg") || n.endsWith(".png") || n.endsWith(".webp") }
            }
            archive.close()
            BookMeta(chapterCount = count)
        } catch (e: Exception) { BookMeta() }
    }

    private fun extractPdf(file: File): BookMeta {
        return try {
            val fd = android.os.ParcelFileDescriptor.open(file, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = android.graphics.pdf.PdfRenderer(fd)
            val count = renderer.pageCount
            renderer.close()
            fd.close()
            BookMeta(chapterCount = count)
        } catch (e: Exception) { BookMeta() }
    }
}
