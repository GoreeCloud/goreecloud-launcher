package com.goreecloud.launcher.ui.theme

/**
 * Native Launcher mapping of the bounded GLAZE UI V1.5.1 Stable
 * context/capability presentation contract.
 *
 * Glaze remains presentation-only. This resolver consumes authority state that
 * another approved owner already supplied. It cannot grant permissions, infer
 * authorization, select a provider winner, navigate, or execute an action.
 */
object GlazeCapabilityV15 {
    const val targetVersion = "1.5.1"
    const val stableSourceRevision = "98da57064ede0f334627b632bc16801f580331af"
    const val reviewedImplementationAnchor = "ee1032a0822ab8e103f8afe48e5c1859fde65cc9"
    const val opticalBaselineVersion = "1.4.1"
    const val opticalBaselineRevision = "4fab9da0fad2e5c974e0e66ec88632c61745751c"
    const val rollbackVersion = "1.5.0"
    const val rollbackSourceRevision = "b7fa8164bfdeaa1dc0acb21b770e7601120da04e"

    enum class CapabilityState {
        AVAILABLE,
        TEMPORARILY_UNAVAILABLE,
        RESTRICTED,
        UNKNOWN,
        CONFLICT,
    }

    data class Capability(
        val id: String,
        val state: CapabilityState,
        val authorityDomain: String,
    )

    data class ActionRequest(
        val id: String,
        val requiredCapabilityIds: Set<String>,
        val consequential: Boolean = false,
    )

    data class ActionPresentation(
        val actionId: String,
        val enabled: Boolean,
        val state: CapabilityState,
        val reasonCodes: Set<String>,
        val automaticExecutionAllowed: Boolean = false,
        val authorityInferred: Boolean = false,
        val providerPrecedenceInferred: Boolean = false,
    )

    fun resolveAction(
        action: ActionRequest,
        capabilities: Collection<Capability>,
    ): ActionPresentation {
        val byId = capabilities.groupBy { it.id }
        val required = action.requiredCapabilityIds.sorted()

        if (required.isEmpty()) {
            return ActionPresentation(
                actionId = action.id,
                enabled = true,
                state = CapabilityState.AVAILABLE,
                reasonCodes = emptySet(),
            )
        }

        val reasons = linkedSetOf<String>()
        var resolvedState = CapabilityState.AVAILABLE

        for (capabilityId in required) {
            val records = byId[capabilityId].orEmpty()
            val state = when {
                records.isEmpty() -> CapabilityState.UNKNOWN
                records.size > 1 -> CapabilityState.CONFLICT
                else -> records.single().state
            }

            when (state) {
                CapabilityState.AVAILABLE -> Unit
                CapabilityState.TEMPORARILY_UNAVAILABLE -> {
                    resolvedState = strongest(resolvedState, state)
                    reasons += "temporarily-unavailable:$capabilityId"
                }
                CapabilityState.RESTRICTED -> {
                    resolvedState = strongest(resolvedState, state)
                    reasons += "restricted-by-authority:$capabilityId"
                }
                CapabilityState.UNKNOWN -> {
                    resolvedState = strongest(resolvedState, state)
                    reasons += "capability-unknown:$capabilityId"
                }
                CapabilityState.CONFLICT -> {
                    resolvedState = strongest(resolvedState, state)
                    reasons += "capability-conflict:$capabilityId"
                }
            }
        }

        return ActionPresentation(
            actionId = action.id,
            enabled = resolvedState == CapabilityState.AVAILABLE,
            state = resolvedState,
            reasonCodes = reasons,
            automaticExecutionAllowed = false,
            authorityInferred = false,
            providerPrecedenceInferred = false,
        )
    }

    private fun strongest(
        current: CapabilityState,
        candidate: CapabilityState,
    ): CapabilityState {
        val order = mapOf(
            CapabilityState.AVAILABLE to 0,
            CapabilityState.TEMPORARILY_UNAVAILABLE to 1,
            CapabilityState.RESTRICTED to 2,
            CapabilityState.UNKNOWN to 3,
            CapabilityState.CONFLICT to 4,
        )
        return if (order.getValue(candidate) > order.getValue(current)) candidate else current
    }
}
