package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun YallaMeshwarLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val goldColor = Color(0xFFFFD700)
    val bgGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2C2C2C),
            Color(0xFF121212)
        )
    )

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 18.dp,
                shape = CircleShape,
                ambientColor = goldColor.copy(alpha = 0.35f),
                spotColor = goldColor.copy(alpha = 0.5f)
            )
            .clip(CircleShape)
            .background(bgGradient)
            .border(width = 2.5.dp, color = goldColor, shape = CircleShape)
            .testTag("yalla_meshwar_logo"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Navigation,
            contentDescription = "شعار يلا مشوار",
            tint = goldColor,
            modifier = Modifier.size(size * 0.55f)
        )
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(size * 0.32f)
                .offset(y = -(size * 0.08f))
        )
    }
}
