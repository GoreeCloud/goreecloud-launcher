package com.goreecloud.launcher.ui

import android.content.pm.LauncherActivityInfo
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.goreecloud.launcher.core.workspace.workspaceKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class LauncherIconCacheKey(
    val workspaceKey: String,
    val inventoryToken: Any,
)

/**
 * Activity-scoped, bounded cache for Android launcher icons.
 *
 * Icon acquisition, badging, drawable rasterization, and ImageBitmap conversion
 * are performed away from the UI thread. The inventory token is replaced for
 * every new LauncherApps snapshot so package/profile refreshes cannot reuse an
 * icon from an older snapshot even when component identity stays the same.
 */
internal class LauncherIconCache(
    maxKilobytes: Int = MAX_CACHE_KILOBYTES,
) {
    private val cache = object : LruCache<LauncherIconCacheKey, ImageBitmap>(maxKilobytes) {
        override fun sizeOf(key: LauncherIconCacheKey, value: ImageBitmap): Int =
            ((value.width.toLong() * value.height.toLong() * BYTES_PER_PIXEL + 1023L) / 1024L)
                .coerceAtLeast(1L)
                .coerceAtMost(Int.MAX_VALUE.toLong())
                .toInt()
    }

    fun peek(app: LauncherActivityInfo, inventoryToken: Any): ImageBitmap? =
        cache.get(keyFor(app, inventoryToken))

    suspend fun load(app: LauncherActivityInfo, inventoryToken: Any): ImageBitmap? {
        val key = keyFor(app, inventoryToken)
        cache.get(key)?.let { return it }

        val rendered = withContext(Dispatchers.IO) {
            runCatching {
                app.getBadgedIcon(0)
                    .toBitmap(ICON_RASTER_SIZE_PX, ICON_RASTER_SIZE_PX)
                    .asImageBitmap()
            }.getOrNull()
        } ?: return null

        cache.put(key, rendered)
        return rendered
    }

    fun retainInventory(inventoryToken: Any) {
        cache.snapshot().keys
            .filterNot { it.inventoryToken === inventoryToken }
            .forEach(cache::remove)
    }

    internal fun cachedEntryCount(): Int = cache.snapshot().size

    private fun keyFor(app: LauncherActivityInfo, inventoryToken: Any): LauncherIconCacheKey =
        LauncherIconCacheKey(
            workspaceKey = app.workspaceKey(),
            inventoryToken = inventoryToken,
        )

    companion object {
        internal const val ICON_RASTER_SIZE_PX = 144
        internal const val MAX_CACHE_KILOBYTES = 8 * 1024
        private const val BYTES_PER_PIXEL = 4L
    }
}

@Composable
internal fun rememberLauncherIcon(
    app: LauncherActivityInfo,
    cache: LauncherIconCache,
    inventoryToken: Any,
): ImageBitmap? {
    val key = remember(app, inventoryToken) {
        LauncherIconCacheKey(app.workspaceKey(), inventoryToken)
    }
    var icon by remember(key) { mutableStateOf(cache.peek(app, inventoryToken)) }

    LaunchedEffect(key, cache) {
        if (icon == null) {
            icon = cache.load(app, inventoryToken)
        }
    }

    return icon
}
