package com.justmusic.app

import android.app.Application
import android.graphics.Bitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

class JustMusicApp : Application(), ImageLoaderFactory {

    private var imageLoaderInstance: ImageLoader? = null

    override fun newImageLoader(): ImageLoader {
        return imageLoaderInstance ?: ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    // Cap at 15% of heap memory to prevent memory pressure on low-end devices
                    .maxSizePercent(0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(40L * 1024 * 1024) // 40MB disk cache limit
                    .build()
            }
            // RGB_565 uses 2 bytes per pixel (50% less RAM than ARGB_8888)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .allowRgb565(true)
            .crossfade(150)
            .build().also {
                imageLoaderInstance = it
            }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Clear bitmap cache under memory pressure to avoid low-memory kills
        if (level >= TRIM_MEMORY_RUNNING_LOW || level >= TRIM_MEMORY_BACKGROUND) {
            imageLoaderInstance?.memoryCache?.clear()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        imageLoaderInstance?.memoryCache?.clear()
    }
}
