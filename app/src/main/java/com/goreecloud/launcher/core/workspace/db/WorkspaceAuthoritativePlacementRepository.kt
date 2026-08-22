package com.goreecloud.launcher.core.workspace.db

import com.goreecloud.launcher.core.workspace.MAX_DOCK_ITEMS
import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceCodec
import com.goreecloud.launcher.core.workspace.WorkspaceMoveDirection
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import kotlinx.coroutines.flow.first

enum class WorkspacePlacementSource {
    DATASTORE,
    ROOM,
}

data class WorkspaceAuthoritativePlacementSnapshot(
    val favoriteKeys: List<String>,
    val dockKeys: List<String>,
    val source: WorkspacePlacementSource,
)

sealed interface WorkspaceAuthoritativeReadResult {
    data object WaitingForInitialization : WorkspaceAuthoritativeReadResult
    data class Loaded(
        val snapshot: WorkspaceAuthoritativePlacementSnapshot,
    ) : WorkspaceAuthoritativeReadResult
    data object AuthorityChanged : WorkspaceAuthoritativeReadResult
    data object Unavailable : WorkspaceAuthoritativeReadResult
    data object Mismatch : WorkspaceAuthoritativeReadResult
    data class Failed(val failureType: String) : WorkspaceAuthoritativeReadResult
}

sealed interface WorkspaceAuthoritativeWriteResult {
    data object WaitingForInitialization : WorkspaceAuthoritativeWriteResult
    data class Written(
        val snapshot: WorkspaceAuthoritativePlacementSnapshot,
    ) : WorkspaceAuthoritativeWriteResult
    data object AuthorityChanged : WorkspaceAuthoritativeWriteResult
    data object Unavailable : WorkspaceAuthoritativeWriteResult
    data object Mismatch : WorkspaceAuthoritativeWriteResult
    data class Failed(val failureType: String) : WorkspaceAuthoritativeWriteResult
}

/**
 * Routes current Home/Dock compatibility placement by the durable workspace authority marker.
 *
 * DATASTORE and ROOM_VERIFIED continue through the legacy repository. Terminal ROOM uses the
 * guarded Room placement repository. This router is intentionally not instantiated by production
 * Home yet; scripts/check_room_cutover.py keeps activation fail-closed until a later cutover PR.
 */
class WorkspaceAuthoritativePlacementRepository(
    private val authorityRepository: WorkspaceRepository,
    private val workspaceDaoProvider: () -> WorkspaceDao?,
) {
    suspend fun read(): WorkspaceAuthoritativeReadResult {
        val state = authorityRepository.state.first()
        if (!state.initialized) return WorkspaceAuthoritativeReadResult.WaitingForInitialization

        return when (state.authority) {
            WorkspaceAuthority.DATASTORE,
            WorkspaceAuthority.ROOM_VERIFIED -> WorkspaceAuthoritativeReadResult.Loaded(
                state.toPlacementSnapshot(WorkspacePlacementSource.DATASTORE)
            )
            WorkspaceAuthority.ROOM -> when (val result = roomRepository().read()) {
                WorkspaceRoomReadResult.Reserved -> WorkspaceAuthoritativeReadResult.AuthorityChanged
                WorkspaceRoomReadResult.Unavailable -> WorkspaceAuthoritativeReadResult.Unavailable
                WorkspaceRoomReadResult.Mismatch -> WorkspaceAuthoritativeReadResult.Mismatch
                is WorkspaceRoomReadResult.Failed -> {
                    WorkspaceAuthoritativeReadResult.Failed(result.failureType)
                }
                is WorkspaceRoomReadResult.Loaded -> WorkspaceAuthoritativeReadResult.Loaded(
                    result.snapshot.toPlacementSnapshot(WorkspacePlacementSource.ROOM)
                )
            }
        }
    }

    suspend fun toggleFavorite(key: String): WorkspaceAuthoritativeWriteResult = mutate(
        legacyMutation = { authorityRepository.toggleFavorite(key) },
        roomMutation = { snapshot ->
            snapshot.copy(favoriteKeys = WorkspaceCodec.toggled(snapshot.favoriteKeys, key))
        },
    )

    suspend fun toggleDock(key: String): WorkspaceAuthoritativeWriteResult = mutate(
        legacyMutation = { authorityRepository.toggleDock(key) },
        roomMutation = { snapshot ->
            snapshot.copy(
                dockKeys = WorkspaceCodec.toggled(snapshot.dockKeys, key, MAX_DOCK_ITEMS)
            )
        },
    )

    suspend fun moveFavorite(
        key: String,
        direction: WorkspaceMoveDirection,
    ): WorkspaceAuthoritativeWriteResult = mutate(
        legacyMutation = { authorityRepository.moveFavorite(key, direction) },
        roomMutation = { snapshot ->
            snapshot.copy(
                favoriteKeys = WorkspaceCodec.moved(snapshot.favoriteKeys, key, direction)
            )
        },
    )

    suspend fun moveDock(
        key: String,
        direction: WorkspaceMoveDirection,
    ): WorkspaceAuthoritativeWriteResult = mutate(
        legacyMutation = { authorityRepository.moveDock(key, direction) },
        roomMutation = { snapshot ->
            snapshot.copy(dockKeys = WorkspaceCodec.moved(snapshot.dockKeys, key, direction))
        },
    )

    suspend fun moveFavoriteToTarget(
        key: String,
        targetKey: String,
    ): WorkspaceAuthoritativeWriteResult = mutate(
        legacyMutation = { authorityRepository.moveFavoriteToTarget(key, targetKey) },
        roomMutation = { snapshot ->
            snapshot.copy(
                favoriteKeys = WorkspaceCodec.movedToTarget(
                    snapshot.favoriteKeys,
                    key,
                    targetKey,
                )
            )
        },
    )

    suspend fun moveDockToTarget(
        key: String,
        targetKey: String,
    ): WorkspaceAuthoritativeWriteResult = mutate(
        legacyMutation = { authorityRepository.moveDockToTarget(key, targetKey) },
        roomMutation = { snapshot ->
            snapshot.copy(
                dockKeys = WorkspaceCodec.movedToTarget(snapshot.dockKeys, key, targetKey)
            )
        },
    )

    private suspend fun mutate(
        legacyMutation: suspend () -> Unit,
        roomMutation: (WorkspaceRelationalSnapshot) -> WorkspaceRelationalSnapshot,
    ): WorkspaceAuthoritativeWriteResult {
        val startingState = authorityRepository.state.first()
        if (!startingState.initialized) {
            return WorkspaceAuthoritativeWriteResult.WaitingForInitialization
        }

        return when (startingState.authority) {
            WorkspaceAuthority.DATASTORE,
            WorkspaceAuthority.ROOM_VERIFIED -> {
                legacyMutation()
                val current = authorityRepository.state.first()
                if (current.authority == WorkspaceAuthority.ROOM) {
                    WorkspaceAuthoritativeWriteResult.AuthorityChanged
                } else {
                    WorkspaceAuthoritativeWriteResult.Written(
                        current.toPlacementSnapshot(WorkspacePlacementSource.DATASTORE)
                    )
                }
            }
            WorkspaceAuthority.ROOM -> mutateRoom(roomMutation)
        }
    }

    private suspend fun mutateRoom(
        transform: (WorkspaceRelationalSnapshot) -> WorkspaceRelationalSnapshot,
    ): WorkspaceAuthoritativeWriteResult {
        val roomRepository = roomRepository()
        val current = when (val read = roomRepository.read()) {
            WorkspaceRoomReadResult.Reserved -> {
                return WorkspaceAuthoritativeWriteResult.AuthorityChanged
            }
            WorkspaceRoomReadResult.Unavailable -> {
                return WorkspaceAuthoritativeWriteResult.Unavailable
            }
            WorkspaceRoomReadResult.Mismatch -> return WorkspaceAuthoritativeWriteResult.Mismatch
            is WorkspaceRoomReadResult.Failed -> {
                return WorkspaceAuthoritativeWriteResult.Failed(read.failureType)
            }
            is WorkspaceRoomReadResult.Loaded -> read.snapshot
        }

        val requested = transform(current)
        return when (
            val write = roomRepository.replace(
                favoriteKeys = requested.favoriteKeys,
                dockKeys = requested.dockKeys,
            )
        ) {
            WorkspaceRoomWriteResult.Reserved -> WorkspaceAuthoritativeWriteResult.AuthorityChanged
            WorkspaceRoomWriteResult.Unavailable -> WorkspaceAuthoritativeWriteResult.Unavailable
            WorkspaceRoomWriteResult.Mismatch -> WorkspaceAuthoritativeWriteResult.Mismatch
            is WorkspaceRoomWriteResult.Failed -> {
                WorkspaceAuthoritativeWriteResult.Failed(write.failureType)
            }
            is WorkspaceRoomWriteResult.Written -> WorkspaceAuthoritativeWriteResult.Written(
                write.snapshot.toPlacementSnapshot(WorkspacePlacementSource.ROOM)
            )
        }
    }

    private fun roomRepository(): WorkspaceRoomPlacementRepository =
        WorkspaceRoomPlacementRepository(
            authorityRepository = authorityRepository,
            workspaceDaoProvider = workspaceDaoProvider,
        )
}

private fun com.goreecloud.launcher.core.workspace.WorkspaceState.toPlacementSnapshot(
    source: WorkspacePlacementSource,
): WorkspaceAuthoritativePlacementSnapshot = WorkspaceAuthoritativePlacementSnapshot(
    favoriteKeys = favoriteKeys,
    dockKeys = dockKeys,
    source = source,
)

private fun WorkspaceRelationalSnapshot.toPlacementSnapshot(
    source: WorkspacePlacementSource,
): WorkspaceAuthoritativePlacementSnapshot = WorkspaceAuthoritativePlacementSnapshot(
    favoriteKeys = favoriteKeys,
    dockKeys = dockKeys,
    source = source,
)
