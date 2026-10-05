package com.solidgame.app.data.remote

import com.solidgame.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/loggin-init")
    suspend fun initLoggin(): Response<LogginInitResponse>

    @POST("auth/loggin-verify")
    suspend fun verifyLoggin(@Body request: VerifyLogginRequest): Response<VerifyOtpResponse>

    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<SendOtpResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

    @GET("auth/me")
    suspend fun getProfile(@Header("Authorization") token: String): Response<UserProfile>

    // Market
    @GET("market/assets")
    suspend fun getAssets(): Response<AssetListResponse>

    @GET("market/price/{symbol}")
    suspend fun getPrice(@Path("symbol") symbol: String): Response<PriceTick>

    @GET("market/prices")
    suspend fun getAllPrices(): Response<List<PriceTick>>

    // Rounds
    @GET("rounds/active")
    suspend fun getActiveRound(
        @Query("symbol") symbol: String,
        @Query("timeframe") timeframe: String
    ): Response<Round>

    @GET("rounds/all-active")
    suspend fun getAllActiveRounds(): Response<List<Round>>

    @GET("rounds/history")
    suspend fun getRoundHistory(
        @Query("symbol") symbol: String?,
        @Query("timeframe") timeframe: String?
    ): Response<List<Round>>

    @GET("rounds/{id}")
    suspend fun getRoundById(@Path("id") id: String): Response<Round>

    // Bets
    @POST("bets")
    suspend fun placeBet(
        @Header("Authorization") token: String,
        @Body request: PlaceBetRequest
    ): Response<Bet>

    @GET("bets")
    suspend fun getBets(@Header("Authorization") token: String): Response<List<Bet>>

    @GET("bets/active")
    suspend fun getActiveBets(@Header("Authorization") token: String): Response<List<Bet>>

    // Wallet
    @GET("wallet")
    suspend fun getWallet(@Header("Authorization") token: String): Response<Wallet>

    @GET("wallet/transactions")
    suspend fun getTransactions(@Header("Authorization") token: String): Response<List<Transaction>>

    @POST("wallet/deposit")
    suspend fun deposit(
        @Header("Authorization") token: String,
        @Body request: AmountRequest
    ): Response<Map<String, Any>>

    @POST("wallet/withdraw")
    suspend fun withdraw(
        @Header("Authorization") token: String,
        @Body request: AmountRequest
    ): Response<Map<String, Any>>

    @GET("wallet/upi-details")
    suspend fun getUpiDetails(): Response<UpiDetailsResponse>

    @POST("wallet/deposit-request")
    suspend fun submitDepositRequest(
        @Header("Authorization") token: String,
        @Body request: DepositRequestPayload
    ): Response<Map<String, Any>>

    @POST("wallet/withdraw-request")
    suspend fun submitWithdrawRequest(
        @Header("Authorization") token: String,
        @Body request: WithdrawalRequestPayload
    ): Response<Map<String, Any>>
}
