package de.jrpie.android.launcher.apps

import android.app.ActivityManager
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import android.util.Log
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.createBitmap
import de.jrpie.android.launcher.R

object IconCache{
    private var ICON_SIZE: Int = 0
    private const val CACHE_PERCENT = 10
    private lateinit var cache: LruCache<String, Bitmap>

    fun initialize(context: Context){
        ICON_SIZE = context.resources.getDimensionPixelSize(R.dimen.app_icon_side)
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val memoryClassBytes =
            activityManager.memoryClass * 1024L * 1024L

        val cacheBytes =
            memoryClassBytes * CACHE_PERCENT / 100

        cache = object : LruCache<String, Bitmap>(cacheBytes.toInt()) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                return value.allocationByteCount
            }
        }

        Log.d(
            "IconCache",
            "Initialized: max=${cache.maxSize() / 1024f / 1024f} MB"
        )
    }

    fun getIcon(context: Context, appInfo: AppInfo): Drawable {
        val key = "${appInfo.packageName}:${appInfo.user}"

        cache.get(key)?.let {
            return it.toDrawable(context.resources)
        }

        val icon = fetchIcon(context, appInfo)
        if (icon != null) {
            val processedIcon = processIconSize(icon)
            cache.put(key, processedIcon)
            return processedIcon.toDrawable(context.resources)
        }

        return Color.TRANSPARENT.toDrawable()
    }

    private fun fetchIcon(
        context: Context,
        appInfo: AppInfo
    ): Drawable? {
        val activityInfo = appInfo.getLauncherActivityInfo(context)
        return activityInfo?.getBadgedIcon(0)
    }

    private fun processIconSize(
        icon: Drawable
    ): Bitmap {
        val bitmap = if (icon is BitmapDrawable) {
            icon.bitmap
        } else {
            val bmp = createBitmap(
                icon.intrinsicWidth.coerceAtLeast(1),
                icon.intrinsicHeight.coerceAtLeast(1)
            )

            val canvas = Canvas(bmp)
            icon.setBounds(0, 0, canvas.width, canvas.height)
            icon.draw(canvas)

            bmp
        }

        return Bitmap.createScaledBitmap(
            bitmap,
            ICON_SIZE,
            ICON_SIZE,
            true
        )
    }

}