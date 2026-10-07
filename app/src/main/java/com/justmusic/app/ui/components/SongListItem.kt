package com.justmusic.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.justmusic.app.R
import com.justmusic.app.data.model.Song
import com.justmusic.app.ui.theme.LocalAppThemeColors
import com.justmusic.app.ui.theme.PinkAccent
import java.util.concurrent.TimeUnit

@Composable
fun SongListItem(
    song: Song,
    isPlaying: Boolean,
    isLiked: Boolean,
    onClick: () -> Unit,
    onLikeToggle: () -> Unit,
    onOptionsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeColors.current
    val context = LocalContext.current

    val imageRequest = remember(song.albumArtUri) {
        ImageRequest.Builder(context)
            .data(song.albumArtUri)
            .size(150, 150)
            .build()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageRequest,
            placeholder = painterResource(id = R.drawable.ic_default_album_art),
            error = painterResource(id = R.drawable.ic_default_album_art),
            contentDescription = "Album Art",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                fontSize = 15.sp,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isPlaying) theme.primary else theme.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = song.artist,
                fontSize = 13.sp,
                color = theme.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Duration text matching Screenshot 3 right side
        Text(
            text = formatDuration(song.durationMs),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = theme.textSecondary,
            modifier = Modifier.padding(end = 6.dp)
        )

        IconButton(
            onClick = onLikeToggle,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) PinkAccent else theme.textSecondary.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }

        if (onOptionsClick != null) {
            IconButton(
                onClick = onOptionsClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = theme.textSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
    return String.format("%d:%02d", minutes, seconds)
}
