package com.goreecloud.launcher.core.workspace

sealed interface WorkspaceWidgetDescriptor {
    data class BuiltIn(val typeId: String) : WorkspaceWidgetDescriptor
    data class Android(
        val appWidgetId: Int,
        val providerComponent: String,
    ) : WorkspaceWidgetDescriptor
}

object WorkspaceWidgetKeyCodec {
    private const val PREFIX = "widget|v1|"
    private const val BUILT_IN = "builtin"
    private const val ANDROID = "android"

    fun encode(descriptor: WorkspaceWidgetDescriptor): String = when (descriptor) {
        is WorkspaceWidgetDescriptor.BuiltIn -> {
            require(descriptor.typeId.isNotBlank() && '|' !in descriptor.typeId)
            "${PREFIX}${BUILT_IN}|${descriptor.typeId}"
        }
        is WorkspaceWidgetDescriptor.Android -> {
            require(descriptor.appWidgetId > 0)
            require(descriptor.providerComponent.isNotBlank() && '|' !in descriptor.providerComponent)
            "${PREFIX}${ANDROID}|${descriptor.appWidgetId}|${descriptor.providerComponent}"
        }
    }

    fun decode(raw: String?): WorkspaceWidgetDescriptor? {
        if (raw.isNullOrBlank() || !raw.startsWith(PREFIX)) return null
        val parts = raw.split('|')
        if (parts.size < 4 || parts[0] != "widget" || parts[1] != "v1") return null
        return when (parts[2]) {
            BUILT_IN -> {
                if (parts.size != 4 || parts[3].isBlank()) null
                else WorkspaceWidgetDescriptor.BuiltIn(parts[3])
            }
            ANDROID -> {
                if (parts.size != 5) return null
                val id = parts[3].toIntOrNull()?.takeIf { it > 0 } ?: return null
                val provider = parts[4].takeIf { it.isNotBlank() } ?: return null
                WorkspaceWidgetDescriptor.Android(id, provider)
            }
            else -> null
        }
    }
}

object WorkspaceWidgetCatalog {
    const val CLOCK = "goreecloud.clock"
    const val LAUNCHER_STATUS = "goreecloud.launcher-status"

    val builtInTypeIds: Set<String> = setOf(CLOCK, LAUNCHER_STATUS)

    fun defaultSpan(typeId: String): Pair<Int, Int>? = when (typeId) {
        CLOCK -> 2 to 2
        LAUNCHER_STATUS -> 2 to 1
        else -> null
    }
}

object WorkspaceWidgetPlacementPolicy {
    fun firstAvailable(
        grid: WorkspaceGridPlacement.Grid,
        existing: List<WorkspaceGridPlacement.Placement>,
        itemId: String,
        spanX: Int,
        spanY: Int,
    ): WorkspaceGridPlacement.Placement? {
        if (itemId.isBlank() || spanX <= 0 || spanY <= 0) return null
        if (spanX > grid.columns || spanY > grid.rows) return null
        for (cellY in 0..(grid.rows - spanY)) {
            for (cellX in 0..(grid.columns - spanX)) {
                val candidate = WorkspaceGridPlacement.Placement(
                    itemId = itemId,
                    cellX = cellX,
                    cellY = cellY,
                    spanX = spanX,
                    spanY = spanY,
                )
                if (
                    WorkspaceGridPlacement.validate(grid, existing + candidate) ==
                    WorkspaceGridPlacement.Validation.Valid
                ) {
                    return candidate
                }
            }
        }
        return null
    }

    fun resize(
        grid: WorkspaceGridPlacement.Grid,
        existing: List<WorkspaceGridPlacement.Placement>,
        itemId: String,
        spanX: Int,
        spanY: Int,
    ): WorkspaceGridPlacement.Placement? {
        val current = existing.singleOrNull { it.itemId == itemId } ?: return null
        if (spanX <= 0 || spanY <= 0) return null
        val updated = current.copy(spanX = spanX, spanY = spanY)
        val next = existing.map { if (it.itemId == itemId) updated else it }
        return if (
            WorkspaceGridPlacement.validate(grid, next) == WorkspaceGridPlacement.Validation.Valid
        ) updated else null
    }
}
