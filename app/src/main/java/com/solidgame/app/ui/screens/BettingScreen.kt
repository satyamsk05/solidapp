package com.solidgame.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solidgame.app.ui.components.CountdownTimer
import com.solidgame.app.ui.components.RealtimeChart
import com.solidgame.app.ui.theme.*
import com.solidgame.app.ui.viewmodel.MainViewModel

@Composable
fun BettingScreen(
    viewModel: MainViewModel
) {
    val assets by viewModel.assets.collectAsState()
    val selectedAsset by viewModel.selectedAsset.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    val activeRound by viewModel.activeRound.collectAsState()
    val livePrice by viewModel.livePrice.collectAsState()
    val priceHistory by viewModel.priceHistory.collectAsState()
    val wallet by viewModel.wallet.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedAmount by remember { mutableStateOf(100.0) }
    val timeframes = listOf("1m", "3m", "5m", "15m")
    val quickAmounts = listOf(10.0, 50.0, 100.0, 500.0, 1000.0)

    val currentPrice = livePrice?.price ?: activeRound?.openPrice ?: 68000.0
    val openPrice = activeRound?.openPrice ?: currentPrice
    val isLocked = activeRound?.status != "betting"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Asset selector pills
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(assets) { asset ->
                val isSelected = asset.symbol == selectedAsset?.symbol
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) PrimaryIndigo else DarkSurface)
                        .clickable { viewModel.selectAsset(asset) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = asset.symbol.replace("USDT", ""),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) TextPrimary else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Timeframe selector pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            timeframes.forEach { tf ->
                val isSelected = tf == selectedTimeframe
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) DarkSurfaceVariant else DarkSurface)
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) PrimaryIndigo else DarkSurface,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.selectTimeframe(tf) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tf,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) PrimaryIndigo else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Real-Time Canvas Chart
        RealtimeChart(
            pricePoints = if (priceHistory.isEmpty()) listOf(openPrice, currentPrice) else priceHistory,
            openPrice = openPrice,
            currentPrice = currentPrice
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Round Timer HUD
        if (activeRound != null) {
            CountdownTimer(
                startTime = activeRound!!.startTime,
                bettingEndTime = activeRound!!.bettingEndTime,
                endTime = activeRound!!.endTime,
                status = activeRound!!.status
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bet Amount Selector
        Text(
            text = "SELECT BET AMOUNT (INR)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickAmounts.forEach { amount ->
                val isSelected = selectedAmount == amount
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) PrimaryIndigo.copy(alpha = 0.2f) else DarkSurface)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) PrimaryIndigo else BorderSubtle,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedAmount = amount }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₹${amount.toInt()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) PrimaryIndigo else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Potential Payout Calculation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Potential Payout (1.90x):", fontSize = 12.sp, color = TextMuted)
            Text(
                text = "₹${String.format("%.2f", selectedAmount * 1.90)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = UpGreen
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Massive UP and DOWN Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // UP Button
            Button(
                onClick = { viewModel.placeBet("up", selectedAmount) },
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UpGreen),
                enabled = !isLocked && !isLoading
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "UP",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "UP", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Text(text = "1.90x Payout", fontSize = 10.sp, color = TextPrimary.copy(alpha = 0.85f))
                    }
                }
            }

            // DOWN Button
            Button(
                onClick = { viewModel.placeBet("down", selectedAmount) },
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DownRed),
                enabled = !isLocked && !isLoading
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "DOWN",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "DOWN", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Text(text = "1.90x Payout", fontSize = 10.sp, color = TextPrimary.copy(alpha = 0.85f))
                    }
                }
            }
        }

        if (isLocked) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "⚠️ Betting window closed for this round. Next round starting soon.",
                fontSize = 11.sp,
                color = GoldYellow,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
