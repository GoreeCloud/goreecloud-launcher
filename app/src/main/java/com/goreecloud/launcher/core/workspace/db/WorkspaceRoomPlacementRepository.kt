package com.goreecloud.launcher.core.workspace.db

import com.goreecloud.launcher.core.workspace.MAX_DOCK_ITEMS
import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

sealed interface WorkspaceRoomReadResult {
    data object Reserved : WorkspaceRoomReadResult
    data object Unavailable : WorkspaceRoomReadResult
    data class Loaded(val snapshot: WorkspaceRelationalSnapshot) : WorkspaceRoomReadResult
    data object Mismatch : WorkspaceRoomReadResult
    data class Failed(val failureType: String) : WorkspaceRoomReadResult
}

sealed interface WorkspaceRoomWriteResult {
    data object Reserved : WorkspaceRoomWriteResult
    data object Unavailable : WorkspaceRoomWriteResult
    data class Written(val snapshot: WorkspaceRelationalSnapshot) : WorkspaceRoomWriteResult
    data object Mismatch : WorkspaceRoomWriteResult
    data class Failed(val failureType: String) : WorkspaceRoomWriteResult
}

internal object WorkspaceRoomPlacementModel {
    fun normalize(
        favoriteKeys: List<String>,
        dockKeys: List<String>,
    ): WorkspaceRelationalSnapshot = WorkspaceRelationalSnapshot(
        favoriteKeys = favoriteKeys.distinct(),
        dockKeys = dockKeys.distinct().take(MAX_DOCK_ITEMS),
    )
}

/**
 * Reserved Room-backed placement I/O for the current Home/Dock compatibility containers.
 *
 * Production Home does not call this repository yet. Reads and writes are accepted only after the
 * durable workspace authority has already reached the terminal ROOM phase. Pre-cutover DATASTORE
 * and ROOM_VERIFIED states fail closed as [WorkspaceRoomReadResult.Reserved] or
 * [WorkspaceRoomWriteResult.Reserved].
 */
class WorkspaceRoomPlacementRepository(
    private val authorityRepository: WorkspaceRepository,
    private val workspaceDaoProvider: () -> WorkspaceDao?,
) {
    suspend fun read(): WorkspaceRoomReadResult {
        if (!isRoomAuthoritative()) return WorkspaceRoomReadResult.Reserved
        val workspaceDao = workspaceDaoOrNull() ?: return WorkspaceRoomReadResult.Unavailable

        return try {
            val snapshot = WorkspaceCanonicalRoomPlacementReader.read(workspaceDao)
                ?: return WorkspaceRoomReadResult.Mismatch
            WorkspaceRoomReadResult.Loaded(snapshot)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            WorkspaceRoomReadResult.Failed(exception::class.java.simpleName)
        }
    }

    suspend fun replace(
        favoriteKeys: List<String>,
        dockKeys: List<String>,
    ): WorkspaceRoomWriteResult {
        if (!isRoomAuthoritative()) return WorkspaceRoomWriteResult.Reserved
        val workspaceDao = workspaceDaoOrNull() ?: return WorkspaceRoomWriteResult.Unavailable
        val normalized = WorkspaceRoomPlacementModel.normalize(favoriteKeys, dockKeys)

        return try {
            val currentPrimary = workspaceDao
                .readItems(listOf(WorkspaceLegacyImportMapper.HOME_PAGE_ID))
                .sortedBy { it.rank }
            val currentCompatibility = currentPrimary.all { it.cellX == null && it.cellY == null }
            val currentSpatial = currentPrimary.all { it.cellX != null && it.cellY != null }
            if (!currentCompatibility && !currentSpatial) {
                return WorkspaceRoomWriteResult.Mismatch
            }

            val expected = WorkspaceLegacyImportMapper.map(
                favoriteKeys = normalized.favoriteKeys,
                dockKeys = normalized.dockKeys,
            )
            val expectedDock = expected.items.filter {
                it.pageId == WorkspaceLegacyImportMapper.DOCK_PAGE_ID
            }
            val currentByKey = currentPrimary
                .mapNotNull { item -> item.appKey?.let { it to item } }
                .toMap()
            if (currentByKey.size != currentPrimary.size) {
                return WorkspaceRoomWriteResult.Mismatch
            }

            val primaryItems = if (currentPrimary.isNotEmpty() && currentCompatibility) {
                expected.items.filter { it.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID }
            } else {
                val columns = maxOf(
                    WorkspacePrimaryHomeGridMigrationPlanner.PRIMARY_HOME_COLUMNS,
                    (currentPrimary.mapNotNull { it.cellX }.maxOrNull() ?: -1) + 1,
                )
                val occupied = mutableSetOf<Pair<Int, Int>>()
                val retained = normalized.favoriteKeys.mapNotNull(currentByKey::get)
                retained.forEach { item ->
                    val x = item.cellX ?: return WorkspaceRoomWriteResult.Mismatch
                    val y = item.cellY ?: return WorkspaceRoomWriteResult.Mismatch
                    if (x < 0 || y < 0 || !occupied.add(x to y)) {
                        return WorkspaceRoomWriteResult.Mismatch
                    }
                }

                normalized.favoriteKeys.mapIndexed { rank, appKey ->
                    currentByKey[appKey]?.copy(rank = rank)
                        ?: run {
                            var index = 0
                            var coordinate: Pair<Int, Int>
                            do {
                                coordinate = (index % columns) to (index / columns)
                                index += 1
                            } while (coordinate in occupied)
                            occupied.add(coordinate)
                            WorkspaceItemEntity(
                                itemId = "legacy:home:$appKey",
                                pageId = WorkspaceLegacyImportMapper.HOME_PAGE_ID,
                                itemType = WorkspaceItemType.APP,
                                appKey = appKey,
                                rank = rank,
                                cellX = coordinate.first,
                                cellY = coordinate.second,
                            )
                        }
                }
            }

            workspaceDao.replaceLegacySnapshot(
                pages = expected.pages,
                items = primaryItems + expectedDock,
            )
            val actual = WorkspaceCanonicalRoomPlacementReader.read(workspaceDao)
                ?: return WorkspaceRoomWriteResult.Mismatch

            if (actual == normalized) {
                WorkspaceRoomWriteResult.Written(actual)
            } else {
                WorkspaceRoomWriteResult.Mismatch
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            WorkspaceRoomWriteResult.Failed(exception::class.java.simpleName)
        }
    }

    private suspend fun isRoomAuthoritative(): Boolean {
        val state = authorityRepository.state.first()
        return state.initialized && state.authority == WorkspaceAuthority.ROOM
    }

    private fun workspaceDaoOrNull(): WorkspaceDao? = try {
        workspaceDaoProvider()
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Exception) {
        null
    }
}
