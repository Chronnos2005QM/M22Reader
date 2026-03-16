package com.m22reader.data.repository

import android.content.Context
import com.m22reader.data.dao.CollectionDao
import com.m22reader.data.model.Collection
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private val SUPPORTED_FORMATS = setOf("pdf", "epub", "cbz", "cbr")
private val COVER_NAMES = setOf("cover.jpg", "cover.jpeg", "cover.png", "cover.webp", "folder.jpg", "folder.png")

@Singleton
class CollectionRepository @Inject constructor(
    private val dao: CollectionDao,
    @ApplicationContext private val context: Context,
) {
    val allCollections: Flow<List<Collection>> = dao.getAllCollections()
    val favorites: Flow<List<Collection>> = dao.getFavoriteCollections()

    fun search(query: String): Flow<List<Collection>> = dao.searchCollections(query)

    /**
     * Escaneia a pasta raiz da biblioteca e agrupa ficheiros por subpasta.
     * Cada subpasta = 1 Coleção.
     * Guarda a capa (cover.jpg) no Room para evitar re-leitura.
     * Usa fallback: se não houver cover.jpg, extrai a capa do 1º ficheiro.
     */
    suspend fun scanLibraryFolder(rootPath: String) = withContext(Dispatchers.IO) {
        val root = File(rootPath)
        if (!root.exists() || !root.isDirectory) return@withContext

        // Cada subpasta é uma coleção
        root.listFiles()?.filter { it.isDirectory }?.forEach { folder ->
            val chapters = folder.listFiles()
                ?.filter { it.extension.lowercase() in SUPPORTED_FORMATS }
                ?.sortedWith(compareBy({ it.name.length }, { it.name }))
                ?: return@forEach

            if (chapters.isEmpty()) return@forEach

            // Verificar se já existe no Room
            val existing = dao.getByPath(folder.absolutePath)

            // Encontrar capa — verificar cover.jpg primeiro (cache), depois extrair
            val coverPath = findCover(folder, chapters.first(), existing?.coverPath)

            val collection = Collection(
                id           = existing?.id ?: 0,
                title        = existing?.title ?: folder.name.replace("_", " ").replace("-", " "),
                folderPath   = folder.absolutePath,
                coverPath    = coverPath,
                chapterCount = chapters.size,
                lastReadChapter = existing?.lastReadChapter ?: 0,
                isFavorite   = existing?.isFavorite ?: false,
                addedAt      = existing?.addedAt ?: java.util.Date(),
                lastReadAt   = existing?.lastReadAt,
            )

            if (existing == null) dao.insert(collection)
            else dao.update(collection)
        }

        // Também indexar ficheiros soltos na pasta raiz como coleções individuais
        root.listFiles()?.filter {
            it.isFile && it.extension.lowercase() in SUPPORTED_FORMATS
        }?.forEach { file ->
            val existing = dao.getByPath(file.absolutePath)
            if (existing == null) {
                dao.insert(Collection(
                    title        = file.nameWithoutExtension.replace("_", " "),
                    folderPath   = file.absolutePath,
                    chapterCount = 1,
                ))
            }
        }
    }

    /**
     * Encontra a capa da coleção com prioridade:
     * 1. Capa já cacheada no Room (coverPath existente)
     * 2. cover.jpg/png na pasta
     * 3. null (Coil vai usar o gradient fallback)
     */
    private fun findCover(folder: File, firstChapter: File, cachedPath: String?): String? {
        // 1. Cache válido
        if (cachedPath != null && File(cachedPath).exists()) return cachedPath

        // 2. cover.jpg na pasta
        val coverFile = folder.listFiles()?.firstOrNull {
            it.name.lowercase() in COVER_NAMES
        }
        if (coverFile != null) return coverFile.absolutePath

        // 3. Sem capa — retorna null, Coil vai usar gradient
        return null
    }

    suspend fun toggleFavorite(id: Long, current: Boolean) = dao.setFavorite(id, !current)
    suspend fun updateProgress(id: Long, chapter: Int) = dao.updateProgress(id, chapter)
    suspend fun updateCover(id: Long, path: String) = dao.updateCover(id, path)
    suspend fun getById(id: Long): Collection? = dao.getById(id)
    suspend fun delete(collection: Collection) = dao.delete(collection)
}
