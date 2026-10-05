package com.solidgame.app.data.model

import com.google.gson.annotations.SerializedName

data class Asset(
    val symbol: String,
    val name: String,
    val minBet: Double,
    val maxBet: Double
)

data class AssetListResponse(
    val assets: List<Asset>,
    val timeframes: List<String>
)

data class PriceTick(
    val symbol: String,
    val price: Double,
    val timestamp: Long,
    val change24h: Double? = null,
    val high24h: Double? = null,
    val low24h: Double? = null
)

data class Round(
    val id: String,
    val symbol: String,
    val timeframe: String,
    val startTime: Long,
    val bettingEndTime: Long,
    val endTime: Long,
    val openPrice: Double,
    val closePrice: Double?,
    val status: String, // "betting", "locked", "completed"
    val result: String?, // "up", "down", "draw"
    val totalUpAmount: Double,
    val totalDownAmount: Double
)

data class Bet(
    val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("roundId") val roundId: String,
    val direction: String, // "up", "down"
    val amount: Double,
    val potentialPayout: Double,
    val status: String, // "pending", "won", "lost", "refunded"
    val payoutAmount: Double,
    val createdAt: Long
)

data class Wallet(
    val userId: String,
    val balance: Double,
    val lockedBalance: Double
)

data class Transaction(
    val id: String,
    val userId: String,
    val type: String,
    val amount: Double,
    val balanceBefore: Double,
    val balanceAfter: Double,
    val description: String?,
    val createdAt: Long
)

data class LogginInitResponse(
    val success: Boolean,
    val token: String,
    val link: String,
    val appKey: String
)

data class VerifyLogginRequest(
    val token: String
)

data class UserProfile(
    val id: String,
    val phone: String,
    val name: String,
    val balance: Double,
    val lockedBalance: Double
)

data class VerifyOtpResponse(
    val token: String,
    val user: UserProfile
)

data class PlaceBetRequest(
    @SerializedName("round_id") val roundId: String,
    val direction: String,
    val amount: Double
)

data class AmountRequest(
    val amount: Double
)

data class DepositRequestPayload(
    val amount: Double,
    val utrNumber: String
)

data class WithdrawalRequestPayload(
    val amount: Double,
    val upiId: String
)

data class UpiDetailsResponse(
    val upiId: String,
    val upiName: String
)

