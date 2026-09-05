package com.goreecloud.launcher.core.launcher

/**
 * Orders the bounded portable-restore recovery check ahead of deferred workspace reconciliation.
 *
 * MainActivity must not publish a gate-opening recovery result until this sequence returns. If
 * portable recovery requires attention, workspace reconciliation is skipped. If workspace
 * reconciliation fails after a normally gate-opening recovery result, the failure propagates and
 * no successful result is returned for publication, preserving the startup mutation barrier.
 */
object LauncherPortableRestoreStartupSequence {
    suspend fun reconcileBeforeMutation(
        recoverPortableRestore: suspend () -> LauncherPortableRestoreRecoveryCoordinator.Result,
        reconcileWorkspace: suspend () -> Unit,
    ): LauncherPortableRestoreRecoveryCoordinator.Result {
        val recovery = recoverPortableRestore()
        if (LauncherPortableRestoreStartupGate.allowsMutations(recovery)) {
            reconcileWorkspace()
        }
        return recovery
    }
}
