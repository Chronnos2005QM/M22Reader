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
import javax.inject.Inject
import javax.inject.Singleton

private val SUPPORTED = setOf("pdf", "epub", "cbz", "cbr")
private val COVER_NAMES = setOf("cover.jpg", "cover.jpeg", "cover.png", "folder.jpg", "folder.png")

// Extrai número do capítulo do nome do ficheiro
// Ex: "Solo_Leveling_Cap_001.cbz" → 1
//     "Chapter 42.pdf" → 42
private fun extractChapterNumber(fileName: String): Int {
    val name = fileName.substringBeforeLast('.')
    // Procura padrões: cap/chapter/ch seguido de número, ou número no final
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

        // ── Subpastas = Coleções ──────────────────────────────────
        root.listFiles()?.filter { it.isDirectory }?.forEach { folder ->
            processFolderAsCollection(folder)
        }

        // ── Ficheiros soltos na raiz = Coleções individuais ────────
        root.listFiles()?.filter {
            it.isFile && it.extension.lowercase() in SUPPORTED
        }?.forEach { file ->
            if (dao.getByPath(file.absolutePath) == null) {
                val colId = dao.insertCollection(Collection(
                    name       = file.nameWithoutExtension.replace(Regex("[_-]"), " "),
                    folderPath = file.absolutePath,
                    chapterCount = 1,
                ))
                if (colId > 0) {
                    dao.insertChapter(Chapter(
                        collectionId  = colId,
                        fileName      = file.name,
                        filePath      = file.absolutePath,
                        chapterNumber = 1,
                        format        = BookFormat.valueOf(file.extension.uppercase()),
                    ))
                }
            }
        }
    }

    private suspend fun processFolderAsCollection(folder: File) {
        val chapters = folder.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in SUPPORTED }
            ?.sortedWith(compareBy({ extractChapterNumber(it.name) }, { it.name }))
            ?: return

        if (chapters.isEmpty()) return

        // Encontrar capa
        val coverFile = folder.listFiles()?.firstOrNull { it.name.lowercase() in COVER_NAMES }

        // Verificar se coleção já existe
        val existing = dao.getByPath(folder.absolutePath)
        val collectionId: Long

        if (existing == null) {
            // Nova coleção
            collectionId = dao.insertCollection(Collection(
                name         = folder.name.replace(Regex("[_-]"), " "),
                folderPath   = folder.absolutePath,
                coverPath    = coverFile?.absolutePath,
                chapterCount = chapters.size,
            ))
        } else {
            // Atualizar existente
            collectionId = existing.id
            dao.updateCollection(existing.copy(
                chapterCount = chapters.size,
                coverPath    = coverFile?.absolutePath ?: existing.coverPath,
            ))
        }

        if (collectionId <= 0) return

        // Inserir capítulos novos
        chapters.forEach { file ->
            val exists = dao.getChapterByPath(file.absolutePath)
            if (exists == null) {
                val format = try { BookFormat.valueOf(file.extension.uppercase()) }
                             catch (_: Exception) { return@forEach }
                dao.insertChapter(Chapter(
                    collectionId  = collectionId,
                    fileName      = file.name,
                    filePath      = file.absolutePath,
                    chapterNumber = extractChapterNumber(file.name),
                    format        = format,
                ))
            }
        }
    }

    suspend fun toggleFavorite(id: Long, current: Boolean) = dao.setFavorite(id, !current)
    suspend fun updateProgress(id: Long, chapId: Long, chapNum: Int) = dao.updateProgress(id, chapId, chapNum)
    suspend fun getById(id: Long) = dao.getById(id)
    suspend fun getChaptersSync(id: Long) = dao.getChaptersSync(id)
    suspend fun markChapterRead(chapId: Long, page: Int) = dao.markChapterRead(chapId, true, page)
}
