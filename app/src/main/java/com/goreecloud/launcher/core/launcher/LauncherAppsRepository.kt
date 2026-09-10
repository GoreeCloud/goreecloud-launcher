package com.goreecloud.launcher.core.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import androidx.core.content.ContextCompat
import java.text.Normalizer
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

internal fun launcherLabelSortKey(label: CharSequence): String =
    Normalizer.normalize(label.toString(), Normalizer.Form.NFC).lowercase(Locale.ROOT)

internal enum class LauncherInventoryChange {
    PACKAGE_ADDED,
    PACKAGE_REMOVED,
    PACKAGE_CHANGED,
    PACKAGE_SUSPENDED,
    PACKAGE_UNSUSPENDED,
    PACKAGES_AVAILABLE,
    PACKAGES_UNAVAILABLE,
    PROFILE_TOPOLOGY,
}

internal enum class LauncherInventoryRefreshScope {
    PACKAGE,
    FULL,
}

internal fun launcherInventoryRefreshScope(change: LauncherInventoryChange): LauncherInventoryRefreshScope =
    when (change) {
        LauncherInventoryChange.PACKAGE_ADDED,
        LauncherInventoryChange.PACKAGE_REMOVED,
        LauncherInventoryChange.PACKAGE_CHANGED,
        LauncherInventoryChange.PACKAGE_SUSPENDED,
        LauncherInventoryChange.PACKAGE_UNSUSPENDED,
        -> LauncherInventoryRefreshScope.PACKAGE

        LauncherInventoryChange.PACKAGES_AVAILABLE,
        LauncherInventoryChange.PACKAGES_UNAVAILABLE,
        LauncherInventoryChange.PROFILE_TOPOLOGY,
        -> LauncherInventoryRefreshScope.FULL
    }

private data class LauncherPackageScope(
    val packageName: String,
    val user: UserHandle,
)

private sealed interface LauncherInventoryRefreshRequest {
    data object Full : LauncherInventoryRefreshRequest

    data class PackageScope(
        val scope: LauncherPackageScope,
    ) : LauncherInventoryRefreshRequest
}

class LauncherAppsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val launcherApps = appContext.getSystemService(LauncherApps::class.java)
    private val callbackHandler = Handler(Looper.getMainLooper())

    val apps: Flow<List<LauncherActivityInfo>> = callbackFlow {
        val refreshRequests = Channel<LauncherInventoryRefreshRequest>(Channel.UNLIMITED)
        val refreshWorker = launch(Dispatchers.IO) {
            var currentSnapshot = emptyList<LauncherActivityInfo>()
            var initialized = false

            for (firstRequest in refreshRequests) {
                var requiresFullRefresh = firstRequest is LauncherInventoryRefreshRequest.Full
                val packageScopes = linkedSetOf<LauncherPackageScope>()
                if (firstRequest is LauncherInventoryRefreshRequest.PackageScope) {
                    packageScopes += firstRequest.scope
                }

                while (true) {
                    val queuedRequest = refreshRequests.tryReceive().getOrNull() ?: break
                    when (queuedRequest) {
                        LauncherInventoryRefreshRequest.Full -> requiresFullRefresh = true
                        is LauncherInventoryRefreshRequest.PackageScope -> packageScopes += queuedRequest.scope
                    }
                }

                if (requiresFullRefresh || !initialized) {
                    val snapshot = runCatching { loadApps() }.getOrNull() ?: continue
                    currentSnapshot = snapshot
                    initialized = true
                    trySend(snapshot)
                    continue
                }

                var nextSnapshot = currentSnapshot
                var refreshedAnyScope = false
                for (scope in packageScopes) {
                    val replacement = runCatching { loadPackageApps(scope) }.getOrNull() ?: continue
                    nextSnapshot = replacePackageScope(nextSnapshot, scope, replacement)
                    refreshedAnyScope = true
                }

                if (refreshedAnyScope) {
                    currentSnapshot = normalizeSnapshot(nextSnapshot)
                    trySend(currentSnapshot)
                }
            }
        }

        fun requestPackageRefresh(
            packageName: String,
            user: UserHandle,
            change: LauncherInventoryChange,
        ) {
            if (launcherInventoryRefreshScope(change) != LauncherInventoryRefreshScope.PACKAGE) {
                refreshRequests.trySend(LauncherInventoryRefreshRequest.Full)
                return
            }
            refreshRequests.trySend(
                LauncherInventoryRefreshRequest.PackageScope(
                    LauncherPackageScope(packageName = packageName, user = user),
                )
            )
        }

        fun requestFullRefresh() {
            refreshRequests.trySend(LauncherInventoryRefreshRequest.Full)
        }

        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) =
                requestPackageRefresh(packageName, user, LauncherInventoryChange.PACKAGE_REMOVED)

            override fun onPackageAdded(packageName: String, user: UserHandle) =
                requestPackageRefresh(packageName, user, LauncherInventoryChange.PACKAGE_ADDED)

            override fun onPackageChanged(packageName: String, user: UserHandle) =
                requestPackageRefresh(packageName, user, LauncherInventoryChange.PACKAGE_CHANGED)

            override fun onPackagesAvailable(
                packageNames: Array<out String>,
                user: UserHandle,
                replacing: Boolean,
            ) = requestFullRefresh()

            override fun onPackagesUnavailable(
                packageNames: Array<out String>,
                user: UserHandle,
                replacing: Boolean,
            ) = requestFullRefresh()

            override fun onPackagesSuspended(packageNames: Array<out String>, user: UserHandle) {
                packageNames.forEach { packageName ->
                    requestPackageRefresh(packageName, user, LauncherInventoryChange.PACKAGE_SUSPENDED)
                }
            }

            override fun onPackagesUnsuspended(packageNames: Array<out String>, user: UserHandle) {
                packageNames.forEach { packageName ->
                    requestPackageRefresh(packageName, user, LauncherInventoryChange.PACKAGE_UNSUSPENDED)
                }
            }
        }

        val profileReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_MANAGED_PROFILE_ADDED,
                    Intent.ACTION_MANAGED_PROFILE_REMOVED,
                    Intent.ACTION_MANAGED_PROFILE_AVAILABLE,
                    Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE,
                    Intent.ACTION_PROFILE_ADDED,
                    Intent.ACTION_PROFILE_REMOVED,
                    Intent.ACTION_PROFILE_AVAILABLE,
                    Intent.ACTION_PROFILE_UNAVAILABLE,
                    -> requestFullRefresh()
                }
            }
        }
        val profileFilter = IntentFilter().apply {
            addAction(Intent.ACTION_MANAGED_PROFILE_ADDED)
            addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED)
            addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
            addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
            if (Build.VERSION.SDK_INT >= 34) {
                addAction(Intent.ACTION_PROFILE_ADDED)
                addAction(Intent.ACTION_PROFILE_REMOVED)
            }
            if (Build.VERSION.SDK_INT >= 35) {
                addAction(Intent.ACTION_PROFILE_AVAILABLE)
                addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
            }
        }

        launcherApps.registerCallback(callback, callbackHandler)
        val profileReceiverRegistered = runCatching {
            ContextCompat.registerReceiver(
                appContext,
                profileReceiver,
                profileFilter,
                ContextCompat.RECEIVER_EXPORTED,
            )
        }.isSuccess
        requestFullRefresh()

        awaitClose {
            launcherApps.unregisterCallback(callback)
            if (profileReceiverRegistered) {
                runCatching { appContext.unregisterReceiver(profileReceiver) }
            }
            refreshRequests.close()
            refreshWorker.cancel()
        }
    }.conflate()

    fun launch(app: LauncherActivityInfo) {
        launcherApps.startMainActivity(app.componentName, app.user, Rect(), Bundle.EMPTY)
    }

    private fun loadApps(): List<LauncherActivityInfo> =
        normalizeSnapshot(
            launcherApps.profiles.flatMap { profile -> launcherApps.getActivityList(null, profile) }
        )

    private fun loadPackageApps(scope: LauncherPackageScope): List<LauncherActivityInfo> =
        launcherApps.getActivityList(scope.packageName, scope.user)

    private fun replacePackageScope(
        current: List<LauncherActivityInfo>,
        scope: LauncherPackageScope,
        replacement: List<LauncherActivityInfo>,
    ): List<LauncherActivityInfo> =
        current.filterNot { app ->
            app.user == scope.user && app.componentName.packageName == scope.packageName
        } + replacement

    private fun normalizeSnapshot(apps: List<LauncherActivityInfo>): List<LauncherActivityInfo> =
        apps.distinctBy { app -> "${app.user.hashCode()}:${app.componentName.flattenToString()}" }
            .sortedWith(
                compareBy(
                    { launcherLabelSortKey(it.label) },
                    { it.componentName.packageName },
                    { it.componentName.className },
                )
            )
}
