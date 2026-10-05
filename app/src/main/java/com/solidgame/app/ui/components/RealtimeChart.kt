package com.solidgame.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solidgame.app.ui.theme.*

@Composable
fun RealtimeChart(
    pricePoints: List<Double>,
    openPrice: Double,
    currentPrice: Double,
    modifier: Modifier = Modifier
) {
    val isUp = currentPrice >= openPrice
    val lineColor = if (isUp) UpGreen else DownRed
    val gradientColor = if (isUp) UpGreenGlow else DownRedGlow

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(DarkSurfaceVariant, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        if (pricePoints.size < 2) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Streaming live chart data from Binance...",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
            return
        }

        val minPrice = (pricePoints.minOrNull() ?: openPrice).coerceAtMost(openPrice) * 0.9998
        val maxPrice = (pricePoints.maxOrNull() ?: openPrice).coerceAtLeast(openPrice) * 1.0002
        val range = (maxPrice - minPrice).coerceAtLeast(0.0001)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            fun getX(index: Int): Float = (index.toFloat() / (pricePoints.size - 1)) * width
            fun getY(price: Double): Float = height - (((price - minPrice) / range).toFloat() * height)

            // Draw Open Price horizontal reference line
            val openY = getY(openPrice)
            drawLine(
                color = BorderSubtle,
                start = Offset(0f, openY),
                end = Offset(width, openY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // Build smooth path for prices
            val path = Path()
            val fillPath = Path()

            path.moveTo(getX(0), getY(pricePoints[0]))
            fillPath.moveTo(getX(0), height)
            fillPath.lineTo(getX(0), getY(pricePoints[0]))

            for (i in 1 until pricePoints.size) {
                val x1 = getX(i - 1)
                val y1 = getY(pricePoints[i - 1])
                val x2 = getX(i)
                val y2 = getY(pricePoints[i])

                val cx = (x1 + x2) / 2
                path.cubicTo(cx, y1, cx, y2, x2, y2)
                fillPath.cubicTo(cx, y1, cx, y2, x2, y2)
            }

            fillPath.lineTo(width, height)
            fillPath.close()

            // Draw gradient area below curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(gradientColor, Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw primary price line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw pulsing dot on current price
            val currentX = width
            val currentY = getY(currentPrice)
            drawCircle(
                color = lineColor.copy(alpha = 0.3f),
                radius = 10.dp.toPx(),
                center = Offset(currentX, currentY)
            )
            drawCircle(
                color = lineColor,
                radius = 5.dp.toPx(),
                center = Offset(currentX, currentY)
            )
        }

        // Price HUD Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "LOCK PRICE", fontSize = 10.sp, color = TextMuted)
                Text(
                    text = "$${String.format("%.2f", openPrice)}",
                    fontSize = 13.sp,
                    color = TextPrimary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "CURRENT PRICE", fontSize = 10.sp, color = TextMuted)
                Text(
                    text = "$${String.format("%.2f", currentPrice)}",
                    fontSize = 15.sp,
                    color = lineColor
                )
            }
        }
    }
}
