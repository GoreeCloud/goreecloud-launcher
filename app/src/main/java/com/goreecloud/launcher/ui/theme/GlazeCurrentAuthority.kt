package com.goreecloud.launcher.ui.theme

/**
 * Truthful current-Stable Glaze UI authority boundary for GoreeCloud Launcher.
 *
 * Launcher still implements its retained V1.1 native source baseline. This object records the
 * separately governed current shared Glaze UI target so source, CI, documentation, and platform
 * declarations cannot silently keep treating a superseded Stable release as current.
 *
 * Recording the current target does not establish V1.6 consumer conformance, rendered acceptance,
 * accessibility acceptance, representative-device acceptance, production eligibility, or Stable
 * qualification for Launcher.
 */
object GlazeCurrentAuthority {
    const val currentRequiredVersion = "1.6.0"
    const val currentStableSourceRevision = "a7180679ea851389e0f3004515f9a25f420e716d"

    const val implementedBaselineVersion = GlazeMetrics.targetVersion
    const val implementedBaselineSourceRevision = GlazeMetrics.sourceRevision

    const val currentConsumerConformanceEstablished = false

    fun migrationRequired(): Boolean =
        implementedBaselineVersion != currentRequiredVersion ||
            !currentConsumerConformanceEstablished
}
