package com.solidgame.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solidgame.app.data.model.Asset
import com.solidgame.app.ui.theme.*
import com.solidgame.app.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToTrade: () -> Unit,
    onNavigateToWallet: () -> Unit
) {
    val assets by viewModel.assets.collectAsState()
    val wallet by viewModel.wallet.collectAsState()
    val user by viewModel.user.collectAsState()
    val livePrice by viewModel.livePrice.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back,",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Text(
                    text = user?.name ?: "Crypto Trader",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Wallet Chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceVariant)
                    .clickable { onNavigateToWallet() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Wallet",
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "₹${String.format("%.2f", wallet?.balance ?: 0.0)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hero Promo Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(PrimaryIndigo.copy(alpha = 0.25f), DarkCard)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(UpGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "1.90X FIXED PAYOUT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = UpGreen
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "• Live Binance Stream", fontSize = 11.sp, color = TextMuted)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Predict Crypto Movements\nUp or Down in 1 Minute",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onNavigateToTrade,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = "Start Trading Now", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SUPPORTED MARKETS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(assets) { asset ->
                MarketCoinItem(
                    asset = asset,
                    livePrice = if (livePrice?.symbol == asset.symbol) livePrice?.price else null,
                    onClick = {
                        viewModel.selectAsset(asset)
                        onNavigateToTrade()
                    }
                )
            }
        }
    }
}

@Composable
fun MarketCoinItem(
    asset: Asset,
    livePrice: Double?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = asset.symbol.take(3),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PrimaryIndigo
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = asset.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = asset.symbol,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    val displayPrice = livePrice ?: when (asset.symbol) {
                        "BTCUSDT" -> 68420.50
                        "ETHUSDT" -> 3540.20
                        "SOLUSDT" -> 182.75
                        else -> 590.10
                    }
                    Text(
                        text = "$${String.format("%.2f", displayPrice)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = UpGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "+2.45%",
                            fontSize = 11.sp,
                            color = UpGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
