package com.justmusic.app.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.LruCache
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

data class DynamicMusicColors(
    val primary: Color,
    val darkBackground: Color,
    val darkCard: Color,
    val lightBackground: Color,
    val gradientTop: Color,
    val gradientBottom: Color
)

object GradientExtractor {

    // In-memory LRU cache to avoid re-decoding images and re-running Palette calculations
    private val paletteCache = LruCache<String, DynamicMusicColors>(24)

    private val FallbackColorPalettes = listOf(
        Pair(Color(0xFF3B82F6), Color(0xFF1E3A8A)), // Expressive Azure
        Pair(Color(0xFF0D9488), Color(0xFF134E4A)), // Expressive Teal
        Pair(Color(0xFF6366F1), Color(0xFF312E81)), // Expressive Indigo
        Pair(Color(0xFFE11D48), Color(0xFF881337)), // Expressive Rose
        Pair(Color(0xFFF59E0B), Color(0xFF78350F)), // Expressive Amber
        Pair(Color(0xFF10B981), Color(0xFF064E3B)), // Expressive Emerald
        Pair(Color(0xFF8B5CF6), Color(0xFF4C1D95)), // Expressive Violet
        Pair(Color(0xFF06B6D4), Color(0xFF164E63)), // Expressive Cyan
        Pair(Color(0xFFF97316), Color(0xFF7C2D12))  // Expressive Coral
    )

    fun createPaletteFromColors(primaryColor: Color, darkColor: Color): DynamicMusicColors {
        // Deep obsidian background subtly tinted by the music color for high contrast & elegance
        val tintedDarkBg = Color(
            red = (0.05f * 0.85f + darkColor.red * 0.15f).coerceIn(0.04f, 0.12f),
            green = (0.06f * 0.85f + darkColor.green * 0.15f).coerceIn(0.04f, 0.12f),
            blue = (0.08f * 0.85f + darkColor.blue * 0.15f).coerceIn(0.05f, 0.14f),
            alpha = 1f
        )

        val tintedDarkCard = Color(
            red = (0.09f * 0.80f + darkColor.red * 0.20f).coerceIn(0.08f, 0.18f),
            green = (0.11f * 0.80f + darkColor.green * 0.20f).coerceIn(0.08f, 0.18f),
            blue = (0.14f * 0.80f + darkColor.blue * 0.20f).coerceIn(0.10f, 0.20f),
            alpha = 1f
        )

        // Light background subtly tinted with warmth of primary
        val tintedLightBg = Color(
            red = (0.97f * 0.96f + primaryColor.red * 0.04f).coerceIn(0.94f, 0.99f),
            green = (0.98f * 0.96f + primaryColor.green * 0.04f).coerceIn(0.94f, 0.99f),
            blue = (0.99f * 0.96f + primaryColor.blue * 0.04f).coerceIn(0.94f, 0.99f),
            alpha = 1f
        )

        return DynamicMusicColors(
            primary = primaryColor,
            darkBackground = tintedDarkBg,
            darkCard = tintedDarkCard,
            lightBackground = tintedLightBg,
            gradientTop = darkColor,
            gradientBottom = primaryColor
        )
    }

    private fun decodeMicroBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    // Decode tiny 48x48 bitmap: uses <10KB RAM and gives Palette all needed data
                    decoder.setTargetSize(48, 48)
                    decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                // Pre-Android P: downsample safely to avoid allocating massive full-res bitmap
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val boundsOptions = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeStream(stream, null, boundsOptions)

                    val sampleSize = calculateInSampleSize(boundsOptions.outWidth, boundsOptions.outHeight, 48, 48)

                    context.contentResolver.openInputStream(uri)?.use { sampleStream ->
                        val decodeOptions = BitmapFactory.Options().apply {
                            inSampleSize = sampleSize
                            inPreferredConfig = Bitmap.Config.RGB_565
                        }
                        BitmapFactory.decodeStream(sampleStream, null, decodeOptions)
                    }
                }
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun calculateInSampleSize(rawWidth: Int, rawHeight: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (rawHeight > reqHeight || rawWidth > reqWidth) {
            val halfHeight = rawHeight / 2
            val halfWidth = rawWidth / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    suspend fun extractDynamicPalette(
        context: Context,
        albumArtUri: Uri?,
        fallbackSeed: String = ""
    ): DynamicMusicColors? = withContext(Dispatchers.IO) {
        val cacheKey = albumArtUri?.toString() ?: fallbackSeed
        if (cacheKey.isNotBlank()) {
            paletteCache.get(cacheKey)?.let { return@withContext it }
        }

        if (albumArtUri != null) {
            var microBitmap: Bitmap? = null
            try {
                microBitmap = decodeMicroBitmap(context, albumArtUri)
                if (microBitmap != null) {
                    val palette = Palette.from(microBitmap).maximumColorCount(12).generate()

                    val vibrant = palette.getVibrantColor(0)
                    val lightVibrant = palette.getLightVibrantColor(0)
                    val darkVibrant = palette.getDarkVibrantColor(0)
                    val dominant = palette.getDominantColor(0)
                    val muted = palette.getMutedColor(0)
                    val darkMuted = palette.getDarkMutedColor(0)

                    val primaryInt = when {
                        vibrant != 0 -> vibrant
                        dominant != 0 -> dominant
                        lightVibrant != 0 -> lightVibrant
                        muted != 0 -> muted
                        else -> 0
                    }

                    val darkInt = when {
                        darkVibrant != 0 -> darkVibrant
                        darkMuted != 0 -> darkMuted
                        else -> primaryInt
                    }

                    if (primaryInt != 0) {
                        val result = createPaletteFromColors(Color(primaryInt), Color(darkInt))
                        if (cacheKey.isNotBlank()) {
                            paletteCache.put(cacheKey, result)
                        }
                        return@withContext result
                    }
                }
            } catch (_: Throwable) {
                // Fall through to seed-based palette
            } finally {
                // Immediately recycle to free native bitmap memory on low-end devices
                try {
                    microBitmap?.recycle()
                } catch (_: Throwable) {}
            }
        }

        // If no album art or palette extraction yielded default, create from seed if provided
        if (fallbackSeed.isNotBlank()) {
            val hash = abs(fallbackSeed.hashCode())
            val pair = FallbackColorPalettes[hash % FallbackColorPalettes.size]
            val fallbackResult = createPaletteFromColors(pair.first, pair.second)
            paletteCache.put(cacheKey, fallbackResult)
            return@withContext fallbackResult
        }

        null
    }

    suspend fun extractGradientColors(context: Context, albumArtUri: Uri?): Pair<Color, Color> = withContext(Dispatchers.IO) {
        val extracted = extractDynamicPalette(context, albumArtUri)
        if (extracted != null) {
            Pair(extracted.gradientTop, extracted.gradientBottom)
        } else {
            Pair(Color(0xFF1E293B), Color(0xFF3B82F6))
        }
    }
}
