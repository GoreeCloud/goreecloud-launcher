package com.goreecloud.launcher.ui

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap as androidxToBitmap

/**
 * Android's WallpaperManager may return a nullable Drawable on current SDK contracts.
 * Keep launcher rendering fail-safe without requiring storage or wallpaper permissions.
 */
internal fun Drawable?.toBitmap(width: Int, height: Int): Bitmap {
    val safeWidth = width.coerceAtLeast(1)
    val safeHeight = height.coerceAtLeast(1)
    return this?.androidxToBitmap(safeWidth, safeHeight)
        ?: Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
}
