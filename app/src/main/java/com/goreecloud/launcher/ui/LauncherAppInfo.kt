package com.goreecloud.launcher.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object LauncherAppInfo {
    fun packageUri(packageName: String): String {
        val normalized = packageName.trim()
        require(normalized.isNotEmpty()) { "package name is required" }
        return "package:$normalized"
    }

    fun open(context: Context, packageName: String): Boolean {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse(packageUri(packageName)),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }
}
