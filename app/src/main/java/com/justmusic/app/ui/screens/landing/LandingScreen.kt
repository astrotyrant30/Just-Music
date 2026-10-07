package com.justmusic.app.ui.screens.landing

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LandingScreen(
    onGetStarted: () -> Unit
) {
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF261A42),
            Color(0xFF19112E),
            Color(0xFF120B22)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .padding(horizontal = 28.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 3D Headphones Hero Illustration with Orbiting Spheres
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                // Orbital ring background graphics
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(
                        color = Color(0x22A862FF),
                        radius = 110.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = Color(0x3300E5FF),
                        radius = 140.dp.toPx(),
                        center = center
                    )
                    // Floating Spheres
                    drawCircle(
                        color = Color(0xFFFF5277),
                        radius = 12.dp.toPx(),
                        center = Offset(center.x - 110.dp.toPx(), center.y - 60.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = 8.dp.toPx(),
                        center = Offset(center.x + 120.dp.toPx(), center.y + 70.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFFFFD166),
                        radius = 10.dp.toPx(),
                        center = Offset(center.x + 90.dp.toPx(), center.y - 80.dp.toPx())
                    )
                }

                // Center Headphones Card
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .shadow(28.dp, CircleShape, spotColor = Color(0xFFA862FF))
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFA862FF), Color(0xFF00E5FF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "Headphones",
                        tint = Color.White,
                        modifier = Modifier.size(85.dp)
                    )
                }
            }

            // Text Content matching "enjoy your music, enjoy your life"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = "enjoy your",
                    fontSize = 34.sp,
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
                    fontSize = 34.sp
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
                    fontSize = 34.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "listen to your favorite music for free, anywhere.",
                    fontSize = 14.sp,
                    color = Color(0x99FFFFFF),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Capsule Pill Get Started Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(29.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFA862FF), Color(0xFFC282FF))
                            )
                        )
                        .clickable(onClick = onGetStarted)
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
}
