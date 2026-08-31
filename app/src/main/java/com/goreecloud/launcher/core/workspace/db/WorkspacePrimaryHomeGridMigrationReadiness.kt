package com.goreecloud.launcher.core.workspace.db

sealed interface WorkspacePrimaryHomeGridMigrationReadiness {
    data class Ready(
        val itemCount: Int,
        val columns: Int,
        val rows: Int,
    ) : WorkspacePrimaryHomeGridMigrationReadiness

    data object NotNeededEmpty : WorkspacePrimaryHomeGridMigrationReadiness
    data object NotNeededAlreadySpatial : WorkspacePrimaryHomeGridMigrationReadiness

    data class Blocked(
        val reason: Reason,
    ) : WorkspacePrimaryHomeGridMigrationReadiness {
        enum class Reason {
            INVALID_PRIMARY_PAGE,
            INVALID_PRIMARY_ITEMS,
        }
    }
}

/**
 * Read-only projection of the existing deterministic migration planner.
 *
 * This helper deliberately performs no Room writes and owns no cutover authority. It exists so
 * Development diagnostics and future user-facing migration review can distinguish a canonical
 * primary Home page that is ready to migrate from one that must remain blocked.
 */
object WorkspacePrimaryHomeGridMigrationReadinessEvaluator {
    fun evaluate(
        page: WorkspacePageEntity,
        items: List<WorkspaceItemEntity>,
    ): WorkspacePrimaryHomeGridMigrationReadiness =
        when (val result = WorkspacePrimaryHomeGridMigrationPlanner.plan(page, items)) {
            is WorkspacePrimaryHomeGridMigrationPlanningResult.Planned ->
                WorkspacePrimaryHomeGridMigrationReadiness.Ready(
                    itemCount = result.plan.migratedItems.size,
                    columns = result.plan.grid.columns,
                    rows = result.plan.grid.rows,
                )

            WorkspacePrimaryHomeGridMigrationPlanningResult.Empty ->
                WorkspacePrimaryHomeGridMigrationReadiness.NotNeededEmpty

            WorkspacePrimaryHomeGridMigrationPlanningResult.AlreadySpatial ->
                WorkspacePrimaryHomeGridMigrationReadiness.NotNeededAlreadySpatial

            WorkspacePrimaryHomeGridMigrationPlanningResult.InvalidPrimaryPage ->
                WorkspacePrimaryHomeGridMigrationReadiness.Blocked(
                    WorkspacePrimaryHomeGridMigrationReadiness.Blocked.Reason.INVALID_PRIMARY_PAGE,
                )

            WorkspacePrimaryHomeGridMigrationPlanningResult.InvalidPrimaryItems ->
                WorkspacePrimaryHomeGridMigrationReadiness.Blocked(
                    WorkspacePrimaryHomeGridMigrationReadiness.Blocked.Reason.INVALID_PRIMARY_ITEMS,
                )
        }
}
