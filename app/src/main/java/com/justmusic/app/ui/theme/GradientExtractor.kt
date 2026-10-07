package com.justmusic.app.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GradientExtractor {

    suspend fun extractGradientColors(context: Context, albumArtUri: Uri?): Pair<Color, Color> = withContext(Dispatchers.IO) {
        if (albumArtUri == null) {
            return@withContext Pair(DeepIndigoBg, WarmPurpleBg)
        }

        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, albumArtUri)
                android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.setTargetSize(200, 200)
                }
            } else {
                @Suppress("DEPRECATION")
                val orig = MediaStore.Images.Media.getBitmap(context.contentResolver, albumArtUri)
                Bitmap.createScaledBitmap(orig, 200, 200, true)
            }

            val palette = Palette.from(bitmap).generate()
            val topColorInt = palette.getVibrantColor(palette.getMutedColor(0xFF1E2243.toInt()))
            val bottomColorInt = palette.getDarkVibrantColor(palette.getDarkMutedColor(0xFF4A345B.toInt()))

            Pair(Color(topColorInt), Color(bottomColorInt))
        } catch (e: Exception) {
            Pair(DeepIndigoBg, WarmPurpleBg)
        }
    }
}
