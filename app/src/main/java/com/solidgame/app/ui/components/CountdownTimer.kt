package com.solidgame.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solidgame.app.ui.theme.*

@Composable
fun CountdownTimer(
    startTime: Long,
    bettingEndTime: Long,
    endTime: Long,
    status: String,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            kotlinx.coroutines.delay(200)
        }
    }

    val totalDuration = (endTime - startTime).coerceAtLeast(1000)
    val remainingMillis = (endTime - currentTime).coerceAtLeast(0)
    val remainingSec = (remainingMillis / 1000).toInt()

    val bettingWindowMillis = (bettingEndTime - startTime).coerceAtLeast(1000)
    val bettingRemainingMillis = (bettingEndTime - currentTime).coerceAtLeast(0)
    val bettingRemainingSec = (bettingRemainingMillis / 1000).toInt()

    val isBettingOpen = status == "betting" && bettingRemainingMillis > 0
    val progress = (remainingMillis.toFloat() / totalDuration).coerceIn(0f, 1f)

    val badgeBg by animateColorAsState(
        targetValue = when {
            isBettingOpen -> UpGreen.copy(alpha = 0.15f)
            status == "locked" -> GoldYellow.copy(alpha = 0.15f)
            else -> PrimaryIndigo.copy(alpha = 0.15f)
        },
        label = "BadgeBg"
    )

    val badgeColor by animateColorAsState(
        targetValue = when {
            isBettingOpen -> UpGreen
            status == "locked" -> GoldYellow
            else -> PrimaryIndigo
        },
        label = "BadgeColor"
    )

    val badgeText = when {
        isBettingOpen -> "BETTING OPEN ($bettingRemainingSec" + "s left)"
        status == "locked" -> "LOCKED • CALCULATING IN $remainingSec" + "s"
        else -> "COMPLETED"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }

            Text(
                text = String.format("%02d:%02d", remainingSec / 60, remainingSec % 60),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = badgeColor,
            trackColor = DarkSurfaceVariant
        )
    }
}
