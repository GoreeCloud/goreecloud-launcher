package com.goreecloud.launcher.core.workspace.db

import com.goreecloud.launcher.core.workspace.WorkspaceAuthority
import com.goreecloud.launcher.core.workspace.WorkspaceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

sealed interface WorkspacePrimaryHomeGridMigrationObservationState {
    data object WaitingForRoom : WorkspacePrimaryHomeGridMigrationObservationState

    data class Evidence(
        val presentation: WorkspacePrimaryHomeGridMigrationPresentation,
    ) : WorkspacePrimaryHomeGridMigrationObservationState

    data class Unavailable(
        val reason: Reason,
    ) : WorkspacePrimaryHomeGridMigrationObservationState {
        enum class Reason {
            ROOM_UNAVAILABLE,
            MISSING_PRIMARY_PAGE,
            READ_FAILED,
        }
    }
}

object WorkspacePrimaryHomeGridMigrationObservationMapper {
    fun map(
        pages: List<WorkspacePageEntity>,
        items: List<WorkspaceItemEntity>,
    ): WorkspacePrimaryHomeGridMigrationObservationState {
        val primaryPage = pages.firstOrNull {
            it.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID
        } ?: return WorkspacePrimaryHomeGridMigrationObservationState.Unavailable(
            WorkspacePrimaryHomeGridMigrationObservationState.Unavailable.Reason.MISSING_PRIMARY_PAGE,
        )
        val primaryItems = items.filter {
            it.pageId == WorkspaceLegacyImportMapper.HOME_PAGE_ID
        }
        return WorkspacePrimaryHomeGridMigrationObservationState.Evidence(
            WorkspacePrimaryHomeGridMigrationPresenter.present(
                WorkspacePrimaryHomeGridMigrationReadinessEvaluator.evaluate(
                    page = primaryPage,
                    items = primaryItems,
                ),
            ),
        )
    }
}

/**
 * Read-only observation path for Development migration diagnostics.
 *
 * This observer waits for terminal Room authority before reading migration evidence, performs no
 * writes, and never exposes migration execution. Read failures remain explicit unavailable states
 * rather than being converted into favorable migration evidence.
 */
class WorkspacePrimaryHomeGridMigrationObservation(
    private val authorityRepository: WorkspaceRepository,
    private val workspaceDaoProvider: () -> WorkspaceDao?,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(): Flow<WorkspacePrimaryHomeGridMigrationObservationState> = authorityRepository.state
        .map { it.initialized to it.authority }
        .distinctUntilChanged()
        .flatMapLatest { (initialized, authority) ->
            if (!initialized || authority != WorkspaceAuthority.ROOM) {
                return@flatMapLatest flowOf(
                    WorkspacePrimaryHomeGridMigrationObservationState.WaitingForRoom,
                )
            }

            val dao = try {
                workspaceDaoProvider()
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                null
            }
            if (dao == null) {
                return@flatMapLatest flowOf(
                    WorkspacePrimaryHomeGridMigrationObservationState.Unavailable(
                        WorkspacePrimaryHomeGridMigrationObservationState.Unavailable.Reason.ROOM_UNAVAILABLE,
                    ),
                )
            }

            combine(dao.observePages(), dao.observeItems()) { pages, items ->
                WorkspacePrimaryHomeGridMigrationObservationMapper.map(pages, items)
            }.catch { exception ->
                if (exception is CancellationException) throw exception
                emit(
                    WorkspacePrimaryHomeGridMigrationObservationState.Unavailable(
                        WorkspacePrimaryHomeGridMigrationObservationState.Unavailable.Reason.READ_FAILED,
                    ),
                )
            }
        }
        .distinctUntilChanged()
}
