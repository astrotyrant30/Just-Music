package com.justmusic.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justmusic.app.data.model.SeekBarStyle
import com.justmusic.app.ui.components.seekbars.SeekbarSelectorDialog
import com.justmusic.app.ui.theme.PinkAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun SettingsScreen(
    skipSilenceEnabled: Boolean,
    seekBarStyle: SeekBarStyle,
    onToggleSkipSilence: () -> Unit,
    onStyleSelected: (SeekBarStyle) -> Unit
) {
    var showSeekbarDialog by remember { mutableStateOf(false) }
    var updateStatus by remember { mutableStateOf("Check for Updates") }
    val currentVersion = "v1.0.0"
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B172E))
            .padding(top = 16.dp, start = 20.dp, end = 20.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn {
            // Seek Bar Style Selector Option
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x22FFFFFF))
                        .clickable { showSeekbarDialog = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Audio Seek Bar Style",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = seekBarStyle.displayName,
                            fontSize = 13.sp,
                            color = PinkAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Skip Silence Option
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x22FFFFFF))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Skip Silence Automatically",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Automatically skips silent audio segments during track playback",
                            fontSize = 12.sp,
                            color = Color(0xB3FFFFFF)
                        )
                    }

                    Switch(
                        checked = skipSilenceEnabled,
                        onCheckedChange = { onToggleSkipSilence() },
                        colors = SwitchDefaults.colors(checkedThumbColor = PinkAccent)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // App Version Info & GitHub Update Tracker
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x11FFFFFF))
                        .clickable {
                            if (updateStatus == "Checking...") return@clickable
                            updateStatus = "Checking..."
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    // Change 'abhinav/JustMusic' to your actual GitHub repo!
                                    val url = URL("https://api.github.com/repos/abhinav/JustMusic/releases/latest")
                                    val connection = url.openConnection() as HttpURLConnection
                                    connection.requestMethod = "GET"
                                    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                                    
                                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                                        val jsonObject = JSONObject(response)
                                        val latestVersion = jsonObject.getString("tag_name")
                                        
                                        withContext(Dispatchers.Main) {
                                            if (latestVersion != currentVersion) {
                                                updateStatus = "Update Available: $latestVersion"
                                            } else {
                                                updateStatus = "Up to date ($currentVersion)"
                                            }
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            updateStatus = "Failed to check (HTTP ${connection.responseCode})"
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        updateStatus = "Error checking updates"
                                    }
                                }
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Just Music",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Version $currentVersion • Offline Audio Engine",
                            fontSize = 12.sp,
                            color = Color(0x88FFFFFF)
                        )
                    }
                    Text(
                        text = updateStatus,
                        fontSize = 12.sp,
                        color = PinkAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    if (showSeekbarDialog) {
        SeekbarSelectorDialog(
            currentStyle = seekBarStyle,
            onStyleSelected = onStyleSelected,
            onDismiss = { showSeekbarDialog = false }
        )
    }
}
