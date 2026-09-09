package com.goreecloud.launcher.core.launcher

/**
 * Routes the Launcher-owned universal-search affordance without duplicating Index provider logic.
 *
 * GoreeCloud Index remains the preferred universal provider surface. Launcher guarantees that the
 * affordance is not a dead end when Index cannot be resolved or launched by falling back to its
 * existing on-device app search surface.
 */
class UniversalSearchRouter(
    private val openIndexSearch: () -> Boolean,
    private val openLocalAppSearch: () -> Unit,
) {
    enum class Destination {
        INDEX,
        LOCAL_APPS,
    }

    fun open(): Destination {
        if (openIndexSearch()) {
            return Destination.INDEX
        }

        openLocalAppSearch()
        return Destination.LOCAL_APPS
    }
}
