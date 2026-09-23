package com.goreecloud.launcher.core.launcher

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.max

enum class LauncherBuiltInWallpaperId {
    AURORA,
    HORIZON,
    NOCTURNE,
    CASCADE,
}

data class LauncherBuiltInWallpaper(
    val id: LauncherBuiltInWallpaperId,
    val name: String,
    val description: String,
    val startColor: Int,
    val middleColor: Int,
    val endColor: Int,
    val accentColor: Int,
    val secondaryAccentColor: Int,
)

object LauncherBuiltInWallpapers {
    val all: List<LauncherBuiltInWallpaper> = listOf(
        LauncherBuiltInWallpaper(
            id = LauncherBuiltInWallpaperId.AURORA,
            name = "Glaze Aurora",
            description = "Deep indigo with soft aqua and violet light.",
            startColor = Color.rgb(9, 16, 38),
            middleColor = Color.rgb(28, 64, 92),
            endColor = Color.rgb(35, 19, 66),
            accentColor = Color.rgb(95, 225, 221),
            secondaryAccentColor = Color.rgb(170, 115, 255),
        ),
        LauncherBuiltInWallpaper(
            id = LauncherBuiltInWallpaperId.HORIZON,
            name = "Glaze Horizon",
            description = "Midnight blue fading into a warm GoreeCloud horizon.",
            startColor = Color.rgb(8, 23, 43),
            middleColor = Color.rgb(33, 74, 105),
            endColor = Color.rgb(81, 42, 68),
            accentColor = Color.rgb(103, 214, 234),
            secondaryAccentColor = Color.rgb(255, 156, 126),
        ),
        LauncherBuiltInWallpaper(
            id = LauncherBuiltInWallpaperId.NOCTURNE,
            name = "Glaze Nocturne",
            description = "Near-black depth with restrained cobalt and teal bloom.",
            startColor = Color.rgb(3, 8, 18),
            middleColor = Color.rgb(12, 28, 48),
            endColor = Color.rgb(4, 15, 27),
            accentColor = Color.rgb(55, 134, 255),
            secondaryAccentColor = Color.rgb(55, 222, 197),
        ),
        LauncherBuiltInWallpaper(
            id = LauncherBuiltInWallpaperId.CASCADE,
            name = "Glaze Cascade",
            description = "Cool slate layers with emerald and sky-blue highlights.",
            startColor = Color.rgb(12, 26, 34),
            middleColor = Color.rgb(25, 57, 66),
            endColor = Color.rgb(15, 36, 58),
            accentColor = Color.rgb(68, 220, 172),
            secondaryAccentColor = Color.rgb(89, 167, 255),
        ),
    )

    fun find(id: LauncherBuiltInWallpaperId): LauncherBuiltInWallpaper =
        checkNotNull(all.firstOrNull { it.id == id })

    fun render(
        id: LauncherBuiltInWallpaperId,
        width: Int,
        height: Int,
    ): Bitmap {
        require(width > 0 && height > 0)
        val wallpaper = find(id)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            intArrayOf(
                wallpaper.startColor,
                wallpaper.middleColor,
                wallpaper.endColor,
            ),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        val longSide = max(width, height).toFloat()
        drawBloom(
            canvas = canvas,
            paint = paint,
            centerX = width * 0.18f,
            centerY = height * 0.22f,
            radius = longSide * 0.46f,
            color = wallpaper.accentColor,
            alpha = 116,
        )
        drawBloom(
            canvas = canvas,
            paint = paint,
            centerX = width * 0.88f,
            centerY = height * 0.62f,
            radius = longSide * 0.52f,
            color = wallpaper.secondaryAccentColor,
            alpha = 98,
        )
        drawBloom(
            canvas = canvas,
            paint = paint,
            centerX = width * 0.40f,
            centerY = height * 0.92f,
            radius = longSide * 0.38f,
            color = Color.WHITE,
            alpha = 28,
        )

        paint.shader = null
        paint.color = Color.argb(20, 255, 255, 255)
        canvas.drawCircle(width * 0.76f, height * 0.18f, width * 0.16f, paint)
        paint.color = Color.argb(12, 255, 255, 255)
        canvas.drawCircle(width * 0.16f, height * 0.72f, width * 0.24f, paint)

        return bitmap
    }

    private fun drawBloom(
        canvas: Canvas,
        paint: Paint,
        centerX: Float,
        centerY: Float,
        radius: Float,
        color: Int,
        alpha: Int,
    ) {
        paint.shader = RadialGradient(
            centerX,
            centerY,
            radius,
            intArrayOf(
                Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)),
                Color.argb(0, Color.red(color), Color.green(color), Color.blue(color)),
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(centerX, centerY, radius, paint)
    }
}
