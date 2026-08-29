package com.goreecloud.launcher.core.workspace.db

import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceMoveDirection
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

sealed interface WorkspaceProductionRuntimeResult {
    data object WaitingForInitialization : WorkspaceProductionRuntimeResult
    data object DataStoreReady : WorkspaceProductionRuntimeResult
    data object RoomReady : WorkspaceProductionRuntimeResult
    data class RecoveryRequired(
        val health: WorkspacePostCutoverHealthResult,
    ) : WorkspaceProductionRuntimeResult
}

/**
 * Owns the reviewed production workspace cutover boundary.
 *
 * Before terminal Room authority, DataStore remains usable while startup reconciliation establishes
 * verified Room evidence and the production promotion coordinator performs the guarded one-way
 * authority transaction. After terminal ROOM, startup health is checked before authoritative Room
 * placement is exposed. Placement writes always use the authority-aware router.
 */
class WorkspaceProductionRuntimeCoordinator(
    private val authorityRepository: WorkspaceRepository,
    private val workspaceDaoProvider: () -> WorkspaceDao?,
) {
    private val refreshEpoch = MutableStateFlow(0L)
    private val startupReconciler = WorkspaceStartupReconciler(
        repository = authorityRepository,
        workspaceDaoProvider = workspaceDaoProvider,
    )
    private val promotionCoordinator = WorkspaceProductionPromotionCoordinator(
        repository = authorityRepository,
        workspaceDaoProvider = workspaceDaoProvider,
    )
    private val postCutoverStartupCoordinator = WorkspacePostCutoverStartupCoordinator(
        repository = authorityRepository,
        workspaceDaoProvider = workspaceDaoProvider,
    )
    private val placementObserver = WorkspaceAuthoritativePlacementObserver(
        authorityRepository = authorityRepository,
        workspaceDaoProvider = workspaceDaoProvider,
    )
    private val pagedHomeObserver = WorkspacePagedHomeObserver(
        authorityRepository = authorityRepository,
        workspaceDaoProvider = workspaceDaoProvider,
    )
    private val placementRepository = WorkspaceAuthoritativePlacementRepository(
        authorityRepository = authorityRepository,
        workspaceDaoProvider = workspaceDaoProvider,
    )
    private val pagedMutationRepository = WorkspacePagedRoomMutationRepository(
        authorityRepository = authorityRepository,
        workspaceDaoProvider = workspaceDaoProvider,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observePlacement(): Flow<WorkspaceAuthoritativePlacementState> = combine(
        authorityRepository.state.map { it.authority }.distinctUntilChanged(),
        refreshEpoch,
    ) { authority, epoch -> authority to epoch }
        .flatMapLatest { (authority, _) ->
            if (authority == WorkspaceAuthority.ROOM) {
                observeTerminalRoomAfterHealthGate()
            } else {
                placementObserver.observe()
            }
        }
        .distinctUntilChanged()

    fun observeHomePages(): Flow<WorkspacePagedHomeState> = pagedHomeObserver.observe()

    suspend fun reconcileAndActivate(): WorkspaceProductionRuntimeResult {
        val starting = authorityRepository.state.first()
        if (!starting.initialized) {
            refresh()
            return WorkspaceProductionRuntimeResult.WaitingForInitialization
        }

        val result = if (starting.authority == WorkspaceAuthority.ROOM) {
            reconcileTerminalRoom()
        } else {
            startupReconciler.reconcile()
            val reconciled = authorityRepository.state.first()
            if (reconciled.authority == WorkspaceAuthority.ROOM_VERIFIED) {
                when (val promotion = promotionCoordinator.promote()) {
                    WorkspaceProductionPromotionResult.PromotedHealthy -> {
                        WorkspaceProductionRuntimeResult.RoomReady
                    }
                    is WorkspaceProductionPromotionResult.PromotedRecoveryRequired -> {
                        WorkspaceProductionRuntimeResult.RecoveryRequired(promotion.health)
                    }
                    WorkspaceProductionPromotionResult.AlreadyRoomAuthoritative -> {
                        reconcileTerminalRoom()
                    }
                    else -> WorkspaceProductionRuntimeResult.DataStoreReady
                }
            } else {
                WorkspaceProductionRuntimeResult.DataStoreReady
            }
        }

        refresh()
        return result
    }

    suspend fun toggleFavorite(key: String): WorkspaceAuthoritativeWriteResult =
        placementRepository.toggleFavorite(key)

    suspend fun toggleDock(key: String): WorkspaceAuthoritativeWriteResult =
        placementRepository.toggleDock(key)

    suspend fun moveFavorite(
        key: String,
        direction: WorkspaceMoveDirection,
    ): WorkspaceAuthoritativeWriteResult = placementRepository.moveFavorite(key, direction)

    suspend fun moveDock(
        key: String,
        direction: WorkspaceMoveDirection,
    ): WorkspaceAuthoritativeWriteResult = placementRepository.moveDock(key, direction)

    suspend fun moveFavoriteToTarget(
        key: String,
        targetKey: String,
    ): WorkspaceAuthoritativeWriteResult = placementRepository.moveFavoriteToTarget(key, targetKey)

    suspend fun moveDockToTarget(
        key: String,
        targetKey: String,
    ): WorkspaceAuthoritativeWriteResult = placementRepository.moveDockToTarget(key, targetKey)

    suspend fun moveHomePage(
        pageId: String,
        targetRank: Int,
    ): WorkspacePagedRoomMutationResult {
        val result = pagedMutationRepository.moveHomePage(pageId, targetRank)
        if (result is WorkspacePagedRoomMutationResult.Updated) {
            refresh()
        }
        return result
    }

    private suspend fun reconcileTerminalRoom(): WorkspaceProductionRuntimeResult =
        when (val startup = postCutoverStartupCoordinator.reconcile()) {
            WorkspacePostCutoverStartupResult.Ready -> WorkspaceProductionRuntimeResult.RoomReady
            WorkspacePostCutoverStartupResult.NotRoomAuthoritative -> {
                WorkspaceProductionRuntimeResult.DataStoreReady
            }
            is WorkspacePostCutoverStartupResult.RecoveryRequired -> {
                WorkspaceProductionRuntimeResult.RecoveryRequired(startup.health)
            }
        }

    private fun observeTerminalRoomAfterHealthGate(): Flow<WorkspaceAuthoritativePlacementState> = flow {
        when (val startup = postCutoverStartupCoordinator.reconcile()) {
            WorkspacePostCutoverStartupResult.Ready -> emitAll(placementObserver.observe())
            WorkspacePostCutoverStartupResult.NotRoomAuthoritative -> emitAll(placementObserver.observe())
            is WorkspacePostCutoverStartupResult.RecoveryRequired -> emit(
                WorkspaceAuthoritativePlacementState.RecoveryRequired(
                    startup.health.toPlacementRecoveryReason()
                )
            )
        }
    }

    private fun refresh() {
        refreshEpoch.value += 1
    }
}

private fun WorkspacePostCutoverHealthResult.toPlacementRecoveryReason():
    WorkspaceAuthoritativePlacementRecoveryReason = when (this) {
        WorkspacePostCutoverHealthResult.Unavailable -> {
            WorkspaceAuthoritativePlacementRecoveryReason.Unavailable
        }
        WorkspacePostCutoverHealthResult.Mismatch,
        WorkspacePostCutoverHealthResult.AuthorityChanged,
        WorkspacePostCutoverHealthResult.NotRoomAuthoritative -> {
            WorkspaceAuthoritativePlacementRecoveryReason.Mismatch
        }
        is WorkspacePostCutoverHealthResult.Failed -> {
            WorkspaceAuthoritativePlacementRecoveryReason.Failed(failureType)
        }
        WorkspacePostCutoverHealthResult.Healthy -> {
            WorkspaceAuthoritativePlacementRecoveryReason.Mismatch
        }
    }
