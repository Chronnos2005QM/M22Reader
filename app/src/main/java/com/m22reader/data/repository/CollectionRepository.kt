package com.m22reader.data.repository

import android.content.Context
import com.m22reader.data.dao.CollectionDao
import com.m22reader.data.model.BookFormat
import com.m22reader.data.model.Chapter
import com.m22reader.data.model.Collection
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton

private val SUPPORTED = setOf("pdf", "epub", "cbz", "cbr")
private val COVER_NAMES = setOf("cover.jpg", "cover.jpeg", "cover.png", "folder.jpg", "folder.png")

private fun extractChapterNumber(fileName: String): Int {
    val name = fileName.substringBeforeLast('.')
    val patterns = listOf(
        Regex("""(?:cap|chapter|ch|capitulo|ep|episode)[.\s_-]*(\d+)""", RegexOption.IGNORE_CASE),
        Regex("""(\d+)$"""),
        Regex("""(\d+)"""),
    )
    for (pattern in patterns) {
        val match = pattern.find(name)
        if (match != null) return match.groupValues[1].toIntOrNull() ?: 0
    }
    return 0
}

@Singleton
class CollectionRepository @Inject constructor(
    private val dao: CollectionDao,
    @ApplicationContext private val context: Context,
) {
    val allCollections: Flow<List<Collection>> = dao.getAllCollections()
    val favorites: Flow<List<Collection>> = dao.getFavoriteCollections()

    fun search(q: String): Flow<List<Collection>> = dao.searchCollections(q)
    fun getChapters(collectionId: Long) = dao.getChaptersForCollection(collectionId)

    suspend fun scanLibraryFolder(rootPath: String) = withContext(Dispatchers.IO) {
        val root = File(rootPath)
        if (!root.exists() || !root.isDirectory) return@withContext
        scanFolderRecursive(root, depth = 0)
    }

    private suspend fun scanFolderRecursive(folder: File, depth: Int) {
        if (depth > 3) return
        val files = folder.listFiles() ?: return
        val supportedFiles = files
            .filter { it.isFile && it.extension.lowercase() in SUPPORTED }
            .sortedWith(compareBy({ extractChapterNumber(it.name) }, { it.name }))
        val subFolders = files.filter { it.isDirectory }

        if (supportedFiles.isNotEmpty()) {
            processFolderAsCollection(folder, supportedFiles)
        }

        for (sub in subFolders) {
            scanFolderRecursive(sub, depth + 1)
        }
    }

    private suspend fun processFolderAsCollection(folder: File, chapters: List<File>) {
        val coverFile = folder.listFiles()?.firstOrNull { it.name.lowercase() in COVER_NAMES }
        val coverPath = coverFile?.absolutePath ?: extractCoverFromCbz(chapters.firstOrNull())

        val existing = dao.getByPath(folder.absolutePath)
        val collectionId: Long

        if (existing == null) {
            collectionId = dao.insertCollection(Collection(
                name = folder.name.replace(Regex("[-_]"), " "),
                folderPath = folder.absolutePath,
                coverPath = coverPath,
                chapterCount = chapters.size,
            ))
        } else {
            collectionId = existing.id
            dao.updateCollection(existing.copy(
                chapterCount = chapters.size,
                coverPath = coverPath ?: existing.coverPath,
            ))
        }

        if (collectionId <= 0) return

        chapters.forEach { file ->
            if (dao.getChapterByPath(file.absolutePath) == null) {
                val format = try {
                    BookFormat.valueOf(file.extension.uppercase())
                } catch (_: Exception) { return@forEach }
                dao.insertChapter(Chapter(
                    collectionId = collectionId,
                    fileName = file.name,
                    filePath = file.absolutePath,
                    chapterNumber = extractChapterNumber(file.name),
                    format = format,
                ))
            }
        }
    }

    private fun extractCoverFromCbz(file: File?): String? {
        if (file == null || file.extension.lowercase() != "cbz") return null
        return try {
            val zip = ZipFile(file)
            val entry = zip.entries().asSequence()
                .filter { !it.isDirectory }
                .filter { it.name.lowercase().let { n ->
                    n.endsWith(".jpg") || n.endsWith(".jpeg") ||
                    n.endsWith(".png") || n.endsWith(".webp")
                }}
                .minByOrNull { it.name }
            if (entry != null) {
                val coverFile = File(file.parent, ".cover_${file.nameWithoutExtension}.jpg")
                zip.getInputStream(entry).use { input ->
                    coverFile.outputStream().use { out -> input.copyTo(out) }
                }
                zip.close()
                coverFile.absolutePath
            } else { zip.close(); null }
        } catch (_: Exception) { null }
    }

    suspend fun toggleFavorite(id: Long, current: Boolean) = dao.setFavorite(id, !current)
    suspend fun updateProgress(id: Long, chapId: Long, chapNum: Int) = dao.updateProgress(id, chapId, chapNum)
    suspend fun getById(id: Long) = dao.getById(id)
    suspend fun getChaptersSync(id: Long) = dao.getChaptersSync(id)
    suspend fun markChapterRead(chapId: Long, page: Int) = dao.markChapterRead(chapId, true, page)
}
