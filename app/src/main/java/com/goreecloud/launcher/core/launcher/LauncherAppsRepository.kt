package com.goreecloud.launcher.core.launcher

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
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

class LauncherAppsRepository(context: Context) {
    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val callbackHandler = Handler(Looper.getMainLooper())

    val apps: Flow<List<LauncherActivityInfo>> = callbackFlow {
        val refreshRequests = Channel<Unit>(Channel.CONFLATED)
        val refreshWorker = launch(Dispatchers.IO) {
            for (ignored in refreshRequests) {
                val snapshot = runCatching { loadApps() }.getOrNull() ?: continue
                trySend(snapshot)
            }
        }

        fun requestRefresh() {
            refreshRequests.trySend(Unit)
        }

        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) = requestRefresh()
            override fun onPackageAdded(packageName: String, user: UserHandle) = requestRefresh()
            override fun onPackageChanged(packageName: String, user: UserHandle) = requestRefresh()
            override fun onPackagesAvailable(
                packageNames: Array<out String>,
                user: UserHandle,
                replacing: Boolean,
            ) = requestRefresh()

            override fun onPackagesUnavailable(
                packageNames: Array<out String>,
                user: UserHandle,
                replacing: Boolean,
            ) = requestRefresh()

            override fun onPackagesSuspended(packageNames: Array<out String>, user: UserHandle) = requestRefresh()
            override fun onPackagesUnsuspended(packageNames: Array<out String>, user: UserHandle) = requestRefresh()
        }

        launcherApps.registerCallback(callback, callbackHandler)
        requestRefresh()
        awaitClose {
            launcherApps.unregisterCallback(callback)
            refreshRequests.close()
            refreshWorker.cancel()
        }
    }.conflate()

    fun launch(app: LauncherActivityInfo) {
        launcherApps.startMainActivity(app.componentName, app.user, Rect(), Bundle.EMPTY)
    }

    private fun loadApps(): List<LauncherActivityInfo> =
        launcherApps.profiles
            .flatMap { profile -> launcherApps.getActivityList(null, profile) }
            .distinctBy { app -> "${app.user.hashCode()}:${app.componentName.flattenToString()}" }
            .sortedWith(
                compareBy(
                    { launcherLabelSortKey(it.label) },
                    { it.componentName.packageName },
                    { it.componentName.className },
                )
            )
}
