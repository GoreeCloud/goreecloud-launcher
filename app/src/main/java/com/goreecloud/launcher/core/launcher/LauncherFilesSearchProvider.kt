package com.goreecloud.launcher.core.launcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class LauncherOpenDocumentSearchAction(
    val uri: String,
    val mimeType: String?,
) : LauncherSearchAction

/**
 * Local filename search over Storage Access Framework trees explicitly selected by the user.
 *
 * The provider keeps a bounded process-local filename index. It does not read file contents, request
 * broad storage access, persist search results, or enumerate outside the selected trees.
 */
class LauncherFilesSearchProvider(
    context: Context,
    private val roots: List<Uri>,
) : LauncherSearchProvider, LauncherAsyncSearchProvider {
    private val appContext = context.applicationContext
    private val indexMutex = Mutex()

    @Volatile
    private var cachedEntries: List<FileEntry>? = null

    override val id: String = PROVIDER_ID

    override fun search(rawQuery: String): List<LauncherSearchResult> = emptyList()

    override suspend fun searchAsync(
        request: LauncherSearchRequest,
    ): List<LauncherSearchResult> = withContext(Dispatchers.IO) {
        val rawQuery = request.rawQuery.trim()
        if (rawQuery.isBlank() || roots.isEmpty()) return@withContext emptyList()

        fileIndex()
            .mapNotNull { entry ->
                val score = LauncherSearchTextRanking.score(
                    title = entry.displayName,
                    subtitle = entry.mimeType,
                    rawQuery = rawQuery,
                ) ?: return@mapNotNull null

                LauncherSearchResult(
                    providerId = id,
                    resultId = entry.uri,
                    title = entry.displayName,
                    subtitle = entry.mimeType.takeUnless { it == GENERIC_MIME_TYPE },
                    category = LauncherSearchCategory.FILE,
                    score = score,
                    action = LauncherOpenDocumentSearchAction(
                        uri = entry.uri,
                        mimeType = entry.mimeType.takeUnless { it == GENERIC_MIME_TYPE },
                    ),
                )
            }
            .sortedByDescending { result -> result.score }
            .take(MAX_RESULTS)
    }

    private suspend fun fileIndex(): List<FileEntry> =
        cachedEntries ?: indexMutex.withLock {
            cachedEntries ?: buildIndex().also { cachedEntries = it }
        }

    private fun buildIndex(): List<FileEntry> {
        val entries = mutableListOf<FileEntry>()
        val normalizedRoots = roots
            .filter(DocumentsContract::isTreeUri)
            .distinctBy(Uri::toString)

        for (treeUri in normalizedRoots) {
            if (entries.size >= MAX_INDEXED_FILES) break
            val rootDocumentId = runCatching {
                DocumentsContract.getTreeDocumentId(treeUri)
            }.getOrNull() ?: continue
            val pending = ArrayDeque<PendingDirectory>()
            pending.add(PendingDirectory(rootDocumentId, depth = 0))

            while (pending.isNotEmpty() && entries.size < MAX_INDEXED_FILES) {
                val directory = pending.removeFirst()
                val childUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                    treeUri,
                    directory.documentId,
                )
                runCatching {
                    appContext.contentResolver.query(
                        childUri,
                        PROJECTION,
                        null,
                        null,
                        null,
                    )
                }.getOrNull()?.use { cursor ->
                    val idIndex = cursor.getColumnIndexOrThrow(
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    )
                    val nameIndex = cursor.getColumnIndexOrThrow(
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    )
                    val mimeIndex = cursor.getColumnIndexOrThrow(
                        DocumentsContract.Document.COLUMN_MIME_TYPE,
                    )
                    while (cursor.moveToNext() && entries.size < MAX_INDEXED_FILES) {
                        val documentId = cursor.getString(idIndex) ?: continue
                        val displayName = cursor.getString(nameIndex)?.trim().orEmpty()
                        val mimeType = cursor.getString(mimeIndex).orEmpty()
                            .ifBlank { GENERIC_MIME_TYPE }

                        if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                            if (directory.depth < MAX_DEPTH) {
                                pending.add(
                                    PendingDirectory(
                                        documentId = documentId,
                                        depth = directory.depth + 1,
                                    ),
                                )
                            }
                            continue
                        }
                        if (displayName.isBlank()) continue

                        val documentUri = DocumentsContract.buildDocumentUriUsingTree(
                            treeUri,
                            documentId,
                        )
                        entries += FileEntry(
                            uri = documentUri.toString(),
                            displayName = displayName,
                            mimeType = mimeType,
                        )
                    }
                }
            }
        }
        return entries
    }

    private data class PendingDirectory(
        val documentId: String,
        val depth: Int,
    )

    private data class FileEntry(
        val uri: String,
        val displayName: String,
        val mimeType: String,
    )

    companion object {
        const val PROVIDER_ID = "launcher.files"
        const val GENERIC_MIME_TYPE = "application/octet-stream"
        private const val MAX_RESULTS = 24
        private const val MAX_INDEXED_FILES = 1_500
        private const val MAX_DEPTH = 8

        private val PROJECTION = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
        )
    }
}

object LauncherFilesSearchProviderRegistration {
    fun registration(
        context: Context,
        roots: List<Uri>,
    ): LauncherSearchProviderRegistration = LauncherSearchProviderRegistration(
        provider = LauncherFilesSearchProvider(context, roots),
        metadata = LauncherSearchProviderMetadata(
            providerId = LauncherFilesSearchProvider.PROVIDER_ID,
            contractVersion = LauncherSearchProviderContract.currentVersion,
            provenance = LauncherSearchProviderProvenance.LAUNCHER_BUILT_IN,
            offlineBehavior = LauncherSearchOfflineBehavior.LOCAL_ONLY,
            authorizationRequirement = LauncherSearchAuthorizationRequirement.USER_CONSENT,
            remoteProcessing = LauncherSearchRemoteProcessing.NONE,
            queryRetention = LauncherSearchQueryRetention.NONE,
        ),
    )
}
