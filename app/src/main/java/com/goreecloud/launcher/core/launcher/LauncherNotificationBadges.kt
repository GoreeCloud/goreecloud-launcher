package com.goreecloud.launcher.core.launcher

import android.app.Notification
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Only non-sensitive package/profile counts are kept in process memory; no notification content. */
data class LauncherBadgeAppKey(val packageName: String, val profile: android.os.UserHandle)

/**
 * Android's notification-access grant remains an explicit system decision. Preference is opt-in
 * and disabled by default; declining permission or turning badges off clears every in-memory count.
 * No title, message, sender, image, or notification history is read, retained, or transmitted.
 */
object LauncherNotificationBadges {
    private const val STORE = "goreecloud_launcher_badges"
    private const val KEY_ENABLED = "enabled"

    private val enabledState = MutableStateFlow(false)
    val enabled = enabledState.asStateFlow()
    private val accessState = MutableStateFlow(false)
    val accessGranted = accessState.asStateFlow()
    private var refreshFromListener: (() -> Unit)? = null
    private val countState = MutableStateFlow<Map<LauncherBadgeAppKey, Int>>(emptyMap())
    val counts = countState.asStateFlow()

    fun initialize(context: Context) {
        enabledState.value = context.applicationContext.getSharedPreferences(
            STORE, Context.MODE_PRIVATE,
        ).getBoolean(KEY_ENABLED, false)
        refreshAccess(context)
    }

    fun setEnabled(context: Context, newEnabled: Boolean) {
        context.applicationContext.getSharedPreferences(STORE, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED, newEnabled).apply()
        enabledState.value = newEnabled
        if (!newEnabled) countState.value = emptyMap()
        refreshAccess(context)
        if (newEnabled) refreshFromListener?.invoke()
    }

    fun refreshAccess(context: Context) {
        accessState.value = try {
            context.packageName in NotificationManagerCompat.getEnabledListenerPackages(context)
        } catch (_: SecurityException) {
            false
        }
        if (!accessState.value) countState.value = emptyMap()
        else if (enabledState.value) refreshFromListener?.invoke()
    }

    internal fun setActiveListener(refresh: (() -> Unit)?) {
        refreshFromListener = refresh
    }

    fun countFor(app: LauncherActivityInfo, visibleCounts: Map<LauncherBadgeAppKey, Int>): Int =
        visibleCounts[LauncherBadgeAppKey(app.componentName.packageName, app.user)] ?: 0

    internal fun acceptActiveNotifications(notifications: Array<StatusBarNotification>?) {
        if (!enabledState.value || !accessState.value) {
            countState.value = emptyMap()
            return
        }
        countState.value = notifications.orEmpty()
            .filter { sbn ->
                sbn.isClearable && (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) == 0
            }
            .groupingBy { sbn -> LauncherBadgeAppKey(sbn.packageName, sbn.user) }
            .eachCount()
    }

    internal fun clear() { countState.value = emptyMap() }
}

/** User-granted access is used solely to render ephemeral notification counts on app icons. */
class LauncherNotificationBadgeListener : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        LauncherNotificationBadges.initialize(this)
        LauncherNotificationBadges.setActiveListener(::refresh)
        LauncherNotificationBadges.refreshAccess(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) { refresh() }
    override fun onNotificationRemoved(sbn: StatusBarNotification?) { refresh() }

    override fun onListenerDisconnected() {
        LauncherNotificationBadges.setActiveListener(null)
        LauncherNotificationBadges.clear()
        super.onListenerDisconnected()
    }

    private fun refresh() {
        val active = try { activeNotifications } catch (_: SecurityException) { null }
        LauncherNotificationBadges.acceptActiveNotifications(active)
    }
}
