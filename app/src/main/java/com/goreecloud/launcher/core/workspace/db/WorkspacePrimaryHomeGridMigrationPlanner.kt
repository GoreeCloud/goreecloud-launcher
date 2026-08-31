package com.goreecloud.launcher.core.workspace.db

import com.goreecloud.launcher.core.workspace.WorkspaceGridPlacement

data class WorkspacePrimaryHomeGridMigrationPlan(
    val grid: WorkspaceGridPlacement.Grid,
    val sourceItems: List<WorkspaceItemEntity>,
    val migratedItems: List<WorkspaceItemEntity>,
)

sealed interface WorkspacePrimaryHomeGridMigrationPlanningResult {
    data class Planned(
        val plan: WorkspacePrimaryHomeGridMigrationPlan,
    ) : WorkspacePrimaryHomeGridMigrationPlanningResult

    data object Empty : WorkspacePrimaryHomeGridMigrationPlanningResult
    data object AlreadySpatial : WorkspacePrimaryHomeGridMigrationPlanningResult
    data object InvalidPrimaryPage : WorkspacePrimaryHomeGridMigrationPlanningResult
    data object InvalidPrimaryItems : WorkspacePrimaryHomeGridMigrationPlanningResult
}

/**
 * Pure planning contract for a future primary HOME compatibility-to-grid migration.
 *
 * This planner deliberately owns no persistence or runtime authority. The accepted primary
 * compatibility projection remains rank-zero `home:0` with null coordinates until a later,
 * separately reviewed migration transaction changes that contract. Planning only proves that the
 * current canonical Favorite rows can be mapped deterministically into the existing four-column
 * primary Home presentation without collisions or identity changes.
 */
object WorkspacePrimaryHomeGridMigrationPlanner {
    const val PRIMARY_HOME_COLUMNS = 4

    fun plan(
        page: WorkspacePageEntity,
        items: List<WorkspaceItemEntity>,
    ): WorkspacePrimaryHomeGridMigrationPlanningResult {
        if (
            page.pageId != WorkspaceLegacyImportMapper.HOME_PAGE_ID ||
            page.containerType != WorkspaceContainerType.HOME ||
            page.rank != 0
        ) {
            return WorkspacePrimaryHomeGridMigrationPlanningResult.InvalidPrimaryPage
        }
        if (items.isEmpty()) {
            return WorkspacePrimaryHomeGridMigrationPlanningResult.Empty
        }

        val orderedItems = items.sortedBy { it.rank }
        if (orderedItems.map { it.rank } != orderedItems.indices.toList()) {
            return WorkspacePrimaryHomeGridMigrationPlanningResult.InvalidPrimaryItems
        }

        val itemIds = mutableSetOf<String>()
        val appKeys = mutableSetOf<String>()
        for (item in orderedItems) {
            val appKey = item.appKey
            if (
                item.pageId != WorkspaceLegacyImportMapper.HOME_PAGE_ID ||
                item.itemType != WorkspaceItemType.APP ||
                appKey == null ||
                appKey.isBlank() ||
                item.itemId != "legacy:home:$appKey" ||
                item.spanX != 1 ||
                item.spanY != 1 ||
                !itemIds.add(item.itemId) ||
                !appKeys.add(appKey)
            ) {
                return WorkspacePrimaryHomeGridMigrationPlanningResult.InvalidPrimaryItems
            }
        }

        val allCompatibilityCoordinates = orderedItems.all {
            it.cellX == null && it.cellY == null
        }
        val allSpatialCoordinates = orderedItems.all {
            it.cellX != null && it.cellY != null
        }
        if (!allCompatibilityCoordinates && !allSpatialCoordinates) {
            return WorkspacePrimaryHomeGridMigrationPlanningResult.InvalidPrimaryItems
        }

        val rows = maxOf(1, (orderedItems.size + PRIMARY_HOME_COLUMNS - 1) / PRIMARY_HOME_COLUMNS)
        val grid = WorkspaceGridPlacement.Grid(
            columns = PRIMARY_HOME_COLUMNS,
            rows = rows,
        )
        val migratedItems = orderedItems.map { item ->
            item.copy(
                cellX = item.rank % PRIMARY_HOME_COLUMNS,
                cellY = item.rank / PRIMARY_HOME_COLUMNS,
            )
        }
        val placements = migratedItems.map { item ->
            WorkspaceGridPlacement.Placement(
                itemId = item.itemId,
                cellX = checkNotNull(item.cellX),
                cellY = checkNotNull(item.cellY),
                spanX = item.spanX,
                spanY = item.spanY,
            )
        }
        if (WorkspaceGridPlacement.validate(grid, placements) != WorkspaceGridPlacement.Validation.Valid) {
            return WorkspacePrimaryHomeGridMigrationPlanningResult.InvalidPrimaryItems
        }

        if (allSpatialCoordinates) {
            val alreadySpatial = orderedItems.zip(migratedItems).all { (current, target) ->
                current.cellX == target.cellX && current.cellY == target.cellY
            }
            return if (alreadySpatial) {
                WorkspacePrimaryHomeGridMigrationPlanningResult.AlreadySpatial
            } else {
                WorkspacePrimaryHomeGridMigrationPlanningResult.InvalidPrimaryItems
            }
        }

        return WorkspacePrimaryHomeGridMigrationPlanningResult.Planned(
            WorkspacePrimaryHomeGridMigrationPlan(
                grid = grid,
                sourceItems = orderedItems,
                migratedItems = migratedItems,
            )
        )
    }
}
