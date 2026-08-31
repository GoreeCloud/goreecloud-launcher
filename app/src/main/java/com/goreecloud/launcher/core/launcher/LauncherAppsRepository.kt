package com.goreecloud.launcher.core.launcher

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

class LauncherAppsRepository(context: Context) {
    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val callbackHandler = Handler(Looper.getMainLooper())

    val apps: Flow<List<LauncherActivityInfo>> = callbackFlow {
        fun publish() {
            trySend(loadApps())
        }

        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) = publish()
            override fun onPackageAdded(packageName: String, user: UserHandle) = publish()
            override fun onPackageChanged(packageName: String, user: UserHandle) = publish()
            override fun onPackagesAvailable(
                packageNames: Array<out String>,
                user: UserHandle,
                replacing: Boolean,
            ) = publish()

            override fun onPackagesUnavailable(
                packageNames: Array<out String>,
                user: UserHandle,
                replacing: Boolean,
            ) = publish()

            override fun onPackagesSuspended(packageNames: Array<out String>, user: UserHandle) = publish()
            override fun onPackagesUnsuspended(packageNames: Array<out String>, user: UserHandle) = publish()
        }

        launcherApps.registerCallback(callback, callbackHandler)
        publish()
        awaitClose { launcherApps.unregisterCallback(callback) }
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
                    { it.label.toString().lowercase() },
                    { it.componentName.packageName },
                    { it.componentName.className },
                )
            )
}
