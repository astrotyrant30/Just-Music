package com.justmusic.app.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justmusic.app.data.model.SeekBarStyle
import com.justmusic.app.ui.components.seekbars.SeekbarSelectorDialog
import com.justmusic.app.ui.theme.LocalAppThemeColors
import com.justmusic.app.ui.theme.PinkAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton

import com.justmusic.app.ui.components.ProfileSetupDialog
import com.justmusic.app.ui.components.UserAvatarView

@Composable
fun SettingsScreen(
    currentThemeMode: String,
    skipSilenceEnabled: Boolean,
    seekBarStyle: SeekBarStyle,
    userName: String = "Music Lover",
    userAvatar: String = "preset:headphones",
    onThemeSelected: (String) -> Unit,
    onToggleSkipSilence: () -> Unit,
    onStyleSelected: (SeekBarStyle) -> Unit,
    onUserNameChange: (String) -> Unit = {},
    onProfileChange: (String, String) -> Unit = { _, _ -> }
) {
    val theme = LocalAppThemeColors.current
    val context = LocalContext.current
    var showSeekbarDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var localSeekStyle by remember(seekBarStyle) { mutableStateOf(seekBarStyle) }
    var updateStatus by remember { mutableStateOf("Check for Updates") }
    var releaseDownloadUrl by remember { mutableStateOf<String?>(null) }
    val currentVersion = "v1.2.0"
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(top = 18.dp, start = 20.dp, end = 20.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 0. Profile & Greeting Display
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(theme.cardBg)
                        .clickable {
                            showProfileDialog = true
                        }
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatarView(
                        avatarKey = userAvatar,
                        size = 46.dp
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Profile & Greeting",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.textPrimary
                        )
                        Text(
                            text = if (userName.isNotBlank() && userName != "Music Lover") userName else "Time-based Greeting (Tap to set)",
                            fontSize = 13.sp,
                            color = theme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Edit",
                        tint = theme.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 1. App Theme Setting (Light, Dark, System)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(theme.cardBg)
                        .padding(18.dp)
                ) {
                    Text(
                        text = "App Appearance",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                    Text(
                        text = "Customize dark and light theme styles",
                        fontSize = 12.sp,
                        color = theme.textSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("LIGHT", "Light", Icons.Default.LightMode),
                            Triple("DARK", "Dark", Icons.Default.DarkMode),
                            Triple("SYSTEM", "System", Icons.Default.SettingsBrightness)
                        ).forEach { (mode, label, icon) ->
                            val isSelected = currentThemeMode.equals(mode, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) theme.primary
                                        else if (theme.isDark) Color(0x22FFFFFF)
                                        else Color(0xFFEEEAF8)
                                    )
                                    .clickable { onThemeSelected(mode) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) Color.White else theme.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else theme.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Audio Seek Bar Style
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(theme.cardBg)
                        .clickable { showSeekbarDialog = true }
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(theme.cardSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Seek Bar",
                            tint = theme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Audio Seek Bar Style",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.textPrimary
                        )
                        Text(
                            text = localSeekStyle.displayName,
                            fontSize = 13.sp,
                            color = theme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Select",
                        tint = theme.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 3. Skip Silence Automatically
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(theme.cardBg)
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(theme.cardSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Skip Silence",
                            tint = theme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Skip Silence Automatically",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.textPrimary
                        )
                        Text(
                            text = "Skips silent audio frames seamlessly",
                            fontSize = 12.sp,
                            color = theme.textSecondary
                        )
                    }

                    Switch(
                        checked = skipSilenceEnabled,
                        onCheckedChange = { onToggleSkipSilence() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = theme.primary
                        )
                    )
                }
            }

            // 4. GitHub Releases & Update Checker (Fixed endpoint & HTTP 404 handling)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(theme.cardBg)
                        .clickable {
                            if (releaseDownloadUrl != null) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(releaseDownloadUrl))
                                context.startActivity(intent)
                                return@clickable
                            }

                            if (updateStatus == "Checking...") return@clickable
                            updateStatus = "Checking..."
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    // Official GitHub Repo for Just Music
                                    val url = URL("https://api.github.com/repos/astrotyrant30/Just-Music/releases/latest")
                                    val connection = (url.openConnection() as HttpURLConnection).apply {
                                        requestMethod = "GET"
                                        setRequestProperty("Accept", "application/vnd.github.v3+json")
                                        setRequestProperty("User-Agent", "JustMusicApp")
                                        connectTimeout = 6000
                                        readTimeout = 6000
                                    }

                                    val responseCode = connection.responseCode
                                    if (responseCode == HttpURLConnection.HTTP_OK) {
                                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                                        val jsonObject = JSONObject(response)
                                        val latestTag = jsonObject.optString("tag_name", "")
                                        val htmlUrl = jsonObject.optString("html_url", "https://github.com/astrotyrant30/Just-Music/releases")

                                        withContext(Dispatchers.Main) {
                                            if (latestTag.isNotEmpty() && !latestTag.equals(currentVersion, ignoreCase = true)) {
                                                updateStatus = "Update Available: $latestTag"
                                                releaseDownloadUrl = htmlUrl
                                            } else {
                                                updateStatus = "Up to date ($currentVersion)"
                                                releaseDownloadUrl = null
                                            }
                                        }
                                    } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                                        // 404 means no published release yet on GitHub: perfectly up to date!
                                        withContext(Dispatchers.Main) {
                                            updateStatus = "Up to date ($currentVersion)"
                                            releaseDownloadUrl = null
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            updateStatus = "Up to date ($currentVersion)"
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        updateStatus = "Up to date ($currentVersion)"
                                    }
                                }
                            }
                        }
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(theme.cardSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = "Update",
                            tint = theme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Just Music Version",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.textPrimary
                        )
                        Text(
                            text = "Version $currentVersion • Offline Music Player",
                            fontSize = 12.sp,
                            color = theme.textSecondary
                        )
                    }

                    Text(
                        text = updateStatus,
                        fontSize = 12.sp,
                        color = if (releaseDownloadUrl != null) PinkAccent else theme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    if (showSeekbarDialog) {
        SeekbarSelectorDialog(
            currentStyle = localSeekStyle,
            onStyleSelected = { style ->
                localSeekStyle = style
                onStyleSelected(style)
            },
            onDismiss = { showSeekbarDialog = false }
        )
    }

    if (showProfileDialog) {
        ProfileSetupDialog(
            currentName = userName,
            currentAvatar = userAvatar,
            onDismiss = { showProfileDialog = false },
            onSave = { name, avatar ->
                onProfileChange(name, avatar)
            }
        )
    }
}
