package com.solidgame.app.data.remote

import android.util.Log
import com.solidgame.app.data.model.PriceTick
import com.solidgame.app.data.model.Round
import com.solidgame.app.data.model.Wallet
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

object WebSocketManager {
    private const val TAG = "WebSocketManager"
    private var socket: Socket? = null
    private val gson = Gson()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _priceUpdates = MutableSharedFlow<PriceTick>(extraBufferCapacity = 64)
    val priceUpdates = _priceUpdates.asSharedFlow()

    private val _roundUpdates = MutableSharedFlow<Round>(extraBufferCapacity = 64)
    val roundUpdates = _roundUpdates.asSharedFlow()

    private val _walletUpdates = MutableSharedFlow<Wallet>(extraBufferCapacity = 16)
    val walletUpdates = _walletUpdates.asSharedFlow()

    private val _betResults = MutableSharedFlow<JSONObject>(extraBufferCapacity = 16)
    val betResults = _betResults.asSharedFlow()

    fun connect(userId: String? = null) {
        if (socket?.connected() == true) return

        try {
            val opts = IO.Options().apply {
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 1000
                timeout = 10000
            }

            socket = IO.socket(NetworkClient.socketUrl, opts).apply {
                on(Socket.EVENT_CONNECT) {
                    Log.d(TAG, "Connected to WebSocket Server")
                    _isConnected.value = true
                    if (!userId.isNullOrEmpty()) {
                        authenticate(userId)
                    }
                }

                on(Socket.EVENT_DISCONNECT) {
                    Log.d(TAG, "Disconnected from WebSocket Server")
                    _isConnected.value = false
                }

                on(Socket.EVENT_CONNECT_ERROR) { args ->
                    Log.w(TAG, "WebSocket Connection Error: ${args.firstOrNull()}")
                    _isConnected.value = false
                }

                on("price_update") { args ->
                    val data = args.firstOrNull() ?: return@on
                    try {
                        val jsonStr = data.toString()
                        val tick = gson.fromJson(jsonStr, PriceTick::class.java)
                        _priceUpdates.tryEmit(tick)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing price_update", e)
                    }
                }

                on("round_update") { args ->
                    val data = args.firstOrNull() ?: return@on
                    try {
                        val jsonStr = data.toString()
                        val round = gson.fromJson(jsonStr, Round::class.java)
                        _roundUpdates.tryEmit(round)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing round_update", e)
                    }
                }

                on("wallet_update") { args ->
                    val data = args.firstOrNull() ?: return@on
                    try {
                        val jsonStr = data.toString()
                        val wallet = gson.fromJson(jsonStr, Wallet::class.java)
                        _walletUpdates.tryEmit(wallet)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing wallet_update", e)
                    }
                }

                on("bet_result") { args ->
                    val data = args.firstOrNull() ?: return@on
                    try {
                        val json = if (data is JSONObject) data else JSONObject(data.toString())
                        _betResults.tryEmit(json)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing bet_result", e)
                    }
                }

                connect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize socket", e)
        }
    }

    fun authenticate(userId: String) {
        val payload = JSONObject().apply {
            put("userId", userId)
        }
        socket?.emit("authenticate", payload)
    }

    fun subscribeRound(symbol: String, timeframe: String) {
        val payload = JSONObject().apply {
            put("symbol", symbol)
            put("timeframe", timeframe)
        }
        socket?.emit("subscribe_round", payload)
    }

    fun unsubscribeRound(symbol: String, timeframe: String) {
        val payload = JSONObject().apply {
            put("symbol", symbol)
            put("timeframe", timeframe)
        }
        socket?.emit("unsubscribe_round", payload)
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        _isConnected.value = false
    }
}
