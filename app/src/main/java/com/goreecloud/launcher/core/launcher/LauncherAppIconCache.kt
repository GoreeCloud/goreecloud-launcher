package com.goreecloud.launcher.core.launcher

import android.content.ComponentName
import android.content.pm.LauncherActivityInfo
import android.graphics.Bitmap
import android.os.UserHandle
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal const val LAUNCHER_ICON_DECODE_SIZE_PX = 144
internal const val LAUNCHER_ICON_CACHE_MAX_KIB = 8 * 1024

internal data class LauncherIconCacheKey(
    val user: UserHandle,
    val componentName: ComponentName,
)

private data class LauncherIconPackageKey(
    val user: UserHandle,
    val packageName: String,
)

private data class LauncherIconCacheStamp(
    val generation: Long,
    val packageGeneration: Long,
)

private data class LauncherIconLoadKey(
    val cacheKey: LauncherIconCacheKey,
    val stamp: LauncherIconCacheStamp,
)

internal class LauncherIconSingleFlightLoader<K : Any, V>(
    private val scope: CoroutineScope,
) {
    private val inFlight = ConcurrentHashMap<K, CompletableDeferred<V>>()

    suspend fun load(key: K, block: suspend () -> V): V {
        val candidate = CompletableDeferred<V>()
        val existing = inFlight.putIfAbsent(key, candidate)
        if (existing != null) return existing.await()

        scope.launch {
            try {
                candidate.complete(block())
            } catch (failure: Throwable) {
                candidate.completeExceptionally(failure)
            } finally {
                inFlight.remove(key, candidate)
            }
        }
        return candidate.await()
    }
}

/**
 * Bounded process-local cache for Android-provided badged launcher icons.
 *
 * LauncherApps remains inventory authority. This cache owns presentation bitmaps only; nothing is
 * persisted and package invalidation makes stale decode work unable to re-enter the cache.
 */
internal object LauncherAppIconCache {
    private val stateLock = Any()
    private var generation = 0L
    private val packageGenerations = mutableMapOf<LauncherIconPackageKey, Long>()
    private val loadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val singleFlight = LauncherIconSingleFlightLoader<LauncherIconLoadKey, Bitmap?>(loadScope)

    private val cache = object : LruCache<LauncherIconCacheKey, Bitmap>(LAUNCHER_ICON_CACHE_MAX_KIB) {
        override fun sizeOf(key: LauncherIconCacheKey, value: Bitmap): Int =
            ((value.allocationByteCount.toLong() + 1023L) / 1024L)
                .coerceAtLeast(1L)
                .coerceAtMost(Int.MAX_VALUE.toLong())
                .toInt()
    }

    fun peek(app: LauncherActivityInfo): Bitmap? = synchronized(stateLock) {
        cache.get(app.cacheKey())
    }

    suspend fun load(app: LauncherActivityInfo): Bitmap? {
        val key = app.cacheKey()
        val requestedStamp = synchronized(stateLock) {
            cache.get(key)?.let { return it }
            stampFor(app)
        }
        val loadKey = LauncherIconLoadKey(key, requestedStamp)

        return singleFlight.load(loadKey) {
            val cachedAfterClaim = synchronized(stateLock) {
                if (stampFor(app) == requestedStamp) cache.get(key) else null
            }
            if (cachedAfterClaim != null) {
                cachedAfterClaim
            } else if (!isCurrentStamp(app, requestedStamp)) {
                null
            } else {
                val decoded = runCatching {
                    app.getBadgedIcon(0).toBitmap(
                        width = LAUNCHER_ICON_DECODE_SIZE_PX,
                        height = LAUNCHER_ICON_DECODE_SIZE_PX,
                    )
                }.getOrNull()

                if (decoded == null) {
                    null
                } else {
                    synchronized(stateLock) {
                        if (stampFor(app) != requestedStamp) {
                            null
                        } else {
                            cache.get(key) ?: decoded.also { cache.put(key, it) }
                        }
                    }
                }
            }
        }
    }

    fun invalidatePackage(packageName: String, user: UserHandle) = synchronized(stateLock) {
        val packageKey = LauncherIconPackageKey(user, packageName)
        packageGenerations[packageKey] = (packageGenerations[packageKey] ?: 0L) + 1L
        cache.snapshot().keys
            .filter { key -> key.user == user && key.componentName.packageName == packageName }
            .forEach(cache::remove)
    }

    fun clear() = synchronized(stateLock) {
        generation += 1L
        packageGenerations.clear()
        cache.evictAll()
    }

    private fun isCurrentStamp(
        app: LauncherActivityInfo,
        expected: LauncherIconCacheStamp,
    ): Boolean = synchronized(stateLock) {
        stampFor(app) == expected
    }

    private fun stampFor(app: LauncherActivityInfo): LauncherIconCacheStamp =
        LauncherIconCacheStamp(
            generation = generation,
            packageGeneration = packageGenerations[
                LauncherIconPackageKey(app.user, app.componentName.packageName)
            ] ?: 0L,
        )

    private fun LauncherActivityInfo.cacheKey(): LauncherIconCacheKey =
        LauncherIconCacheKey(user = user, componentName = componentName)
}
