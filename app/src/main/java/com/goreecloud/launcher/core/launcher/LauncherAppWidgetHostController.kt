package com.goreecloud.launcher.core.launcher

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context

class LauncherAppWidgetHostController(context: Context) {
    private val appContext = context.applicationContext
    private val host = AppWidgetHost(appContext, HOST_ID)
    private val manager = AppWidgetManager.getInstance(appContext)

    fun startListening() {
        host.startListening()
    }

    fun stopListening() {
        host.stopListening()
    }

    fun allocateAppWidgetId(): Int = host.allocateAppWidgetId()

    fun deleteAppWidgetId(appWidgetId: Int) {
        if (appWidgetId > 0) {
            runCatching { host.deleteAppWidgetId(appWidgetId) }
        }
    }

    fun providerInfo(appWidgetId: Int): AppWidgetProviderInfo? =
        if (appWidgetId > 0) manager.getAppWidgetInfo(appWidgetId) else null

    fun createHostView(appWidgetId: Int): AppWidgetHostView? {
        val info = providerInfo(appWidgetId) ?: return null
        return host.createView(appContext, appWidgetId, info)
    }

    companion object {
        // Stable within the GoreeCloud Launcher package; AppWidgetHost IDs are host-package scoped.
        private const val HOST_ID = 0x4743
    }
}
