package com.goreecloud.launcher.core.workspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceWidgetPlacementPolicyTest {
    @Test
    fun widgetKeysRoundTripWithoutChangingAndroidBindingIdentity() {
        val android = WorkspaceWidgetDescriptor.Android(
            appWidgetId = 42,
            providerComponent = "com.example/.WeatherWidget",
        )
        val builtIn = WorkspaceWidgetDescriptor.BuiltIn(WorkspaceWidgetCatalog.CLOCK)

        assertEquals(android, WorkspaceWidgetKeyCodec.decode(WorkspaceWidgetKeyCodec.encode(android)))
        assertEquals(builtIn, WorkspaceWidgetKeyCodec.decode(WorkspaceWidgetKeyCodec.encode(builtIn)))
        assertNull(WorkspaceWidgetKeyCodec.decode("not-a-widget"))
        assertNull(WorkspaceWidgetKeyCodec.decode("widget|v1|android|-1|com.example/.Widget"))
    }

    @Test
    fun firstAvailableRespectsExistingSpansAndUsesRowMajorPlacement() {
        val grid = WorkspaceGridPlacement.Grid(columns = 4, rows = 4)
        val existing = listOf(
            WorkspaceGridPlacement.Placement("app-a", 0, 0),
            WorkspaceGridPlacement.Placement("app-b", 1, 0),
            WorkspaceGridPlacement.Placement("wide", 2, 0, spanX = 2, spanY = 2),
        )

        val placement = WorkspaceWidgetPlacementPolicy.firstAvailable(
            grid = grid,
            existing = existing,
            itemId = "widget",
            spanX = 2,
            spanY = 2,
        )

        assertEquals(
            WorkspaceGridPlacement.Placement("widget", 0, 1, spanX = 2, spanY = 2),
            placement,
        )
    }

    @Test
    fun firstAvailableFailsClosedWhenRequestedSpanCannotFit() {
        val grid = WorkspaceGridPlacement.Grid(columns = 2, rows = 2)
        val existing = listOf(
            WorkspaceGridPlacement.Placement("a", 0, 0),
            WorkspaceGridPlacement.Placement("b", 1, 0),
            WorkspaceGridPlacement.Placement("c", 0, 1),
            WorkspaceGridPlacement.Placement("d", 1, 1),
        )

        assertNull(
            WorkspaceWidgetPlacementPolicy.firstAvailable(
                grid = grid,
                existing = existing,
                itemId = "widget",
                spanX = 1,
                spanY = 1,
            ),
        )
    }

    @Test
    fun resizeRejectsCollisionAndAcceptsFreeExpansion() {
        val grid = WorkspaceGridPlacement.Grid(columns = 4, rows = 4)
        val existing = listOf(
            WorkspaceGridPlacement.Placement("widget", 0, 0),
            WorkspaceGridPlacement.Placement("app", 2, 0),
        )

        val free = WorkspaceWidgetPlacementPolicy.resize(
            grid = grid,
            existing = existing,
            itemId = "widget",
            spanX = 2,
            spanY = 2,
        )
        assertEquals(2, free?.spanX)
        assertEquals(2, free?.spanY)

        val collision = WorkspaceWidgetPlacementPolicy.resize(
            grid = grid,
            existing = existing,
            itemId = "widget",
            spanX = 3,
            spanY = 1,
        )
        assertNull(collision)
        assertTrue(WorkspaceWidgetCatalog.defaultSpan(WorkspaceWidgetCatalog.CLOCK) != null)
    }
}
