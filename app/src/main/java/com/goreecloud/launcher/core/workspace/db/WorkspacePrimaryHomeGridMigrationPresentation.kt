package com.goreecloud.launcher.core.workspace.db

data class WorkspacePrimaryHomeGridMigrationPresentation(
    val title: String,
    val detail: String,
    val status: Status,
    val executionAvailable: Boolean,
) {
    enum class Status {
        READY,
        NOT_NEEDED,
        BLOCKED,
    }
}

/**
 * User-facing, read-only copy derived from migration readiness evidence.
 *
 * executionAvailable deliberately remains false in every state. This projection may be rendered by
 * Development diagnostics without granting Room mutation or migration authority to the UI.
 */
object WorkspacePrimaryHomeGridMigrationPresenter {
    fun present(
        readiness: WorkspacePrimaryHomeGridMigrationReadiness,
    ): WorkspacePrimaryHomeGridMigrationPresentation =
        when (readiness) {
            is WorkspacePrimaryHomeGridMigrationReadiness.Ready ->
                WorkspacePrimaryHomeGridMigrationPresentation(
                    title = "Primary Home is ready for spatial migration",
                    detail = "${readiness.itemCount} app${if (readiness.itemCount == 1) "" else "s"} can be placed on a ${readiness.columns} × ${readiness.rows} grid after a separately authorized migration.",
                    status = WorkspacePrimaryHomeGridMigrationPresentation.Status.READY,
                    executionAvailable = false,
                )

            WorkspacePrimaryHomeGridMigrationReadiness.NotNeededEmpty ->
                WorkspacePrimaryHomeGridMigrationPresentation(
                    title = "No primary Home migration needed",
                    detail = "The protected primary Home page has no compatibility app placements to migrate.",
                    status = WorkspacePrimaryHomeGridMigrationPresentation.Status.NOT_NEEDED,
                    executionAvailable = false,
                )

            WorkspacePrimaryHomeGridMigrationReadiness.NotNeededAlreadySpatial ->
                WorkspacePrimaryHomeGridMigrationPresentation(
                    title = "Primary Home is already spatial",
                    detail = "The protected primary Home page already uses canonical grid coordinates.",
                    status = WorkspacePrimaryHomeGridMigrationPresentation.Status.NOT_NEEDED,
                    executionAvailable = false,
                )

            is WorkspacePrimaryHomeGridMigrationReadiness.Blocked ->
                WorkspacePrimaryHomeGridMigrationPresentation(
                    title = "Primary Home migration is blocked",
                    detail = when (readiness.reason) {
                        WorkspacePrimaryHomeGridMigrationReadiness.Blocked.Reason.INVALID_PRIMARY_PAGE ->
                            "The protected primary Home page is not canonical enough to plan migration safely."
                        WorkspacePrimaryHomeGridMigrationReadiness.Blocked.Reason.INVALID_PRIMARY_ITEMS ->
                            "Primary Home compatibility items are not canonical enough to plan migration safely."
                    },
                    status = WorkspacePrimaryHomeGridMigrationPresentation.Status.BLOCKED,
                    executionAvailable = false,
                )
        }
}
