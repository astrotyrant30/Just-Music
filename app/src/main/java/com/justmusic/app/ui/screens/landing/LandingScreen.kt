package com.justmusic.app.ui.screens.landing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.justmusic.app.ui.components.ProfileSetupDialog

@Composable
fun LandingScreen(
    onGetStarted: (userName: String, avatarKey: String) -> Unit
) {
    var showProfileSetup by remember { mutableStateOf(false) }

    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A),
            Color(0xFF0B1120),
            Color(0xFF070B14)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 20.dp)
        ) {
            val availableHeight = maxHeight
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // 3D Headphones Hero Illustration with Orbiting Spheres (Responsive)
                val illustrationHeight = min(260.dp, availableHeight * 0.38f)
                val headphoneSize = min(150.dp, availableHeight * 0.22f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(illustrationHeight),
                    contentAlignment = Alignment.Center
                ) {
                    // Orbital ring background graphics
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        drawCircle(
                            color = Color(0x223B82F6),
                            radius = size.width * 0.32f,
                            center = center
                        )
                        drawCircle(
                            color = Color(0x3306B6D4),
                            radius = size.width * 0.42f,
                            center = center
                        )
                        // Floating Spheres
                        drawCircle(
                            color = Color(0xFFF43F5E),
                            radius = 12.dp.toPx(),
                            center = Offset(center.x - size.width * 0.30f, center.y - 45.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFF06B6D4),
                            radius = 8.dp.toPx(),
                            center = Offset(center.x + size.width * 0.34f, center.y + 55.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFFFBBF24),
                            radius = 10.dp.toPx(),
                            center = Offset(center.x + size.width * 0.25f, center.y - 65.dp.toPx())
                        )
                    }

                    // Center Headphones Card
                    Box(
                        modifier = Modifier
                            .size(headphoneSize)
                            .shadow(28.dp, CircleShape, spotColor = Color(0x663B82F6))
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF3B82F6), Color(0xFF06B6D4))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "Headphones",
                            tint = Color.White,
                            modifier = Modifier.size(headphoneSize * 0.58f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Text Content matching "enjoy your music, enjoy your life"
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    val fontSize = if (availableHeight < 680.dp) 28.sp else 34.sp

                    Text(
                        text = "enjoy your",
                        fontSize = fontSize,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xD9FFFFFF)
                    )

                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                                append("music")
                            }
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal, color = Color(0xD9FFFFFF))) {
                                append(", enjoy")
                            }
                        },
                        fontSize = fontSize
                    )

                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal, color = Color(0xD9FFFFFF))) {
                                append("your ")
                            }
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                                append("life")
                            }
                        },
                        fontSize = fontSize
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "listen to your favorite music for free, anywhere.",
                        fontSize = 14.sp,
                        color = Color(0x99FFFFFF),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Capsule Pill Get Started Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .clip(RoundedCornerShape(29.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF2563EB), Color(0xFF3B82F6))
                                )
                            )
                            .clickable { showProfileSetup = true }
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(20.dp))

                            Text(
                                text = "GET STARTED",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Go",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showProfileSetup) {
            ProfileSetupDialog(
                currentName = "",
                currentAvatar = "preset:headphones",
                onDismiss = {
                    showProfileSetup = false
                    // If dismissed without saving, still allow continuing with defaults
                    onGetStarted("Music Lover", "preset:headphones")
                },
                onSave = { name, avatar ->
                    showProfileSetup = false
                    onGetStarted(name, avatar)
                }
            )
        }
    }
}
