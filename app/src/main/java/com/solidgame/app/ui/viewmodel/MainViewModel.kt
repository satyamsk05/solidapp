package com.solidgame.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solidgame.app.data.model.*
import com.solidgame.app.data.remote.NetworkClient
import com.solidgame.app.data.remote.WebSocketManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val api = NetworkClient.apiService

    private val _user = MutableStateFlow<UserProfile?>(null)
    val user = _user.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token = _token.asStateFlow()

    private val _assets = MutableStateFlow<List<Asset>>(emptyList())
    val assets = _assets.asStateFlow()

    private val _selectedAsset = MutableStateFlow<Asset?>(null)
    val selectedAsset = _selectedAsset.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow("1m")
    val selectedTimeframe = _selectedTimeframe.asStateFlow()

    private val _activeRound = MutableStateFlow<Round?>(null)
    val activeRound = _activeRound.asStateFlow()

    private val _livePrice = MutableStateFlow<PriceTick?>(null)
    val livePrice = _livePrice.asStateFlow()

    private val _priceHistory = MutableStateFlow<List<Double>>(emptyList())
    val priceHistory = _priceHistory.asStateFlow()

    private val _wallet = MutableStateFlow<Wallet?>(null)
    val wallet = _wallet.asStateFlow()

    private val _upiDetails = MutableStateFlow<UpiDetailsResponse?>(null)
    val upiDetails = _upiDetails.asStateFlow()

    private val _bets = MutableStateFlow<List<Bet>>(emptyList())
    val bets = _bets.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    private var priceCollectionJob: Job? = null
    private var roundCollectionJob: Job? = null
    private var walletCollectionJob: Job? = null
    private var betResultJob: Job? = null

    init {
        loadAssets()
        listenToWebSocket()
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private fun loadAssets() {
        viewModelScope.launch {
            try {
                val res = api.getAssets()
                if (res.isSuccessful && res.body() != null) {
                    val list = res.body()!!.assets
                    _assets.value = list
                    if (_selectedAsset.value == null && list.isNotEmpty()) {
                        selectAsset(list[0])
                    }
                } else {
                    // Fallback default assets
                    val defaults = listOf(
                        Asset("BTCUSDT", "Bitcoin", 10.0, 50000.0),
                        Asset("ETHUSDT", "Ethereum", 10.0, 50000.0),
                        Asset("SOLUSDT", "Solana", 10.0, 25000.0),
                        Asset("BNBUSDT", "BNB", 10.0, 25000.0)
                    )
                    _assets.value = defaults
                    if (_selectedAsset.value == null) {
                        selectAsset(defaults[0])
                    }
                }
            } catch (e: Exception) {
                // Network fallback
                val defaults = listOf(
                    Asset("BTCUSDT", "Bitcoin", 10.0, 50000.0),
                    Asset("ETHUSDT", "Ethereum", 10.0, 50000.0),
                    Asset("SOLUSDT", "Solana", 10.0, 25000.0),
                    Asset("BNBUSDT", "BNB", 10.0, 25000.0)
                )
                _assets.value = defaults
                selectAsset(defaults[0])
            }
        }
    }

    fun selectAsset(asset: Asset) {
        val oldAsset = _selectedAsset.value
        _selectedAsset.value = asset
        _priceHistory.value = emptyList()

        if (oldAsset != null) {
            WebSocketManager.unsubscribeRound(oldAsset.symbol, _selectedTimeframe.value)
        }
        WebSocketManager.subscribeRound(asset.symbol, _selectedTimeframe.value)
        fetchActiveRound(asset.symbol, _selectedTimeframe.value)
    }

    fun selectTimeframe(timeframe: String) {
        val asset = _selectedAsset.value ?: return
        val oldTf = _selectedTimeframe.value
        _selectedTimeframe.value = timeframe

        WebSocketManager.unsubscribeRound(asset.symbol, oldTf)
        WebSocketManager.subscribeRound(asset.symbol, timeframe)
        fetchActiveRound(asset.symbol, timeframe)
    }

    private fun fetchActiveRound(symbol: String, timeframe: String) {
        viewModelScope.launch {
            try {
                val res = api.getActiveRound(symbol, timeframe)
                if (res.isSuccessful && res.body() != null) {
                    _activeRound.value = res.body()
                }
            } catch (e: Exception) {
                // Handled via WebSocket round_update
            }
        }
    }

    private fun listenToWebSocket() {
        WebSocketManager.connect(_user.value?.id)

        priceCollectionJob = viewModelScope.launch {
            WebSocketManager.priceUpdates.collect { tick ->
                if (_selectedAsset.value?.symbol?.equals(tick.symbol, ignoreCase = true) == true) {
                    _livePrice.value = tick
                    val currentList = _priceHistory.value.toMutableList()
                    currentList.add(tick.price)
                    if (currentList.size > 60) {
                        currentList.removeAt(0)
                    }
                    _priceHistory.value = currentList
                }
            }
        }

        roundCollectionJob = viewModelScope.launch {
            WebSocketManager.roundUpdates.collect { round ->
                if (_selectedAsset.value?.symbol?.equals(round.symbol, ignoreCase = true) == true &&
                    _selectedTimeframe.value == round.timeframe
                ) {
                    _activeRound.value = round
                }
            }
        }

        walletCollectionJob = viewModelScope.launch {
            WebSocketManager.walletUpdates.collect { w ->
                _wallet.value = w
            }
        }

        betResultJob = viewModelScope.launch {
            WebSocketManager.betResults.collect { json ->
                val result = json.optString("result")
                val payout = json.optDouble("payoutAmount", 0.0)
                if (result == "won") {
                    _toastMessage.value = "🎉 Bet WON! ₹${String.format("%.2f", payout)} credited to wallet!"
                } else if (result == "lost") {
                    _toastMessage.value = "❌ Bet lost on round completion."
                } else if (result == "refunded") {
                    _toastMessage.value = "⚖️ Round Draw: ₹${String.format("%.2f", payout)} refunded."
                }
                refreshUserData()
            }
        }
    }

    fun initLogginAuth(onComplete: (Boolean, String?, String?, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = api.initLoggin()
                _isLoading.value = false
                if (res.isSuccessful && res.body()?.success == true) {
                    val body = res.body()!!
                    onComplete(true, body.token, body.link, null)
                } else {
                    onComplete(false, null, null, "Failed to initialize WhatsApp auth")
                }
            } catch (e: Exception) {
                _isLoading.value = false
                onComplete(false, null, null, e.message)
            }
        }
    }

    fun verifyLogginAuth(logginToken: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val res = api.verifyLoggin(VerifyLogginRequest(logginToken))
                if (res.isSuccessful && res.body() != null) {
                    val body = res.body()!!
                    _token.value = body.token
                    _user.value = body.user
                    _wallet.value = Wallet(body.user.id, body.user.balance, body.user.lockedBalance)
                    WebSocketManager.authenticate(body.user.id, body.token)
                    refreshUserData()
                    onComplete(true, null)
                } else {
                    val err = res.errorBody()?.string() ?: "WhatsApp verification pending"
                    onComplete(false, err)
                }
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }

    fun placeBet(direction: String, amount: Double) {
        val t = _token.value
        val round = _activeRound.value
        if (t == null) {
            _toastMessage.value = "Please login first to place bets"
            return
        }
        if (round == null) {
            _toastMessage.value = "No active round available"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = api.placeBet("Bearer $t", PlaceBetRequest(round.id, direction, amount))
                _isLoading.value = false
                if (res.isSuccessful && res.body() != null) {
                    _toastMessage.value = "✅ Bet of ₹$amount placed on ${direction.uppercase()}!"
                    refreshUserData()
                } else {
                    _toastMessage.value = "Failed: ${res.errorBody()?.string() ?: "Insufficient balance or round locked"}"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _toastMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun deposit(amount: Double) {
        val t = _token.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = api.deposit("Bearer $t", AmountRequest(amount))
                _isLoading.value = false
                if (res.isSuccessful) {
                    _toastMessage.value = "✅ Added ₹$amount to wallet successfully!"
                    refreshUserData()
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _toastMessage.value = e.message
            }
        }
    }

    fun fetchUpiDetails() {
        viewModelScope.launch {
            try {
                val res = api.getUpiDetails()
                if (res.isSuccessful) _upiDetails.value = res.body()
            } catch (e: Exception) {}
        }
    }

    fun submitDepositRequest(amount: Double, utr: String) {
        val t = _token.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = api.submitDepositRequest("Bearer $t", DepositRequestPayload(amount, utr))
                _isLoading.value = false
                if (res.isSuccessful) {
                    _toastMessage.value = "✅ UTR submitted! Admin will verify and credit funds."
                    refreshUserData()
                } else {
                    _toastMessage.value = "Failed: UTR already used or invalid"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _toastMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun submitWithdrawRequest(amount: Double, upiId: String) {
        val t = _token.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = api.submitWithdrawRequest("Bearer $t", WithdrawalRequestPayload(amount, upiId))
                _isLoading.value = false
                if (res.isSuccessful) {
                    _toastMessage.value = "✅ Withdrawal request submitted! Funds will be sent to your UPI."
                    refreshUserData()
                } else {
                    _toastMessage.value = "Insufficient available balance"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _toastMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun withdraw(amount: Double) {
        val t = _token.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = api.withdraw("Bearer $t", AmountRequest(amount))
                _isLoading.value = false
                if (res.isSuccessful) {
                    _toastMessage.value = "✅ Withdrew ₹$amount successfully!"
                    refreshUserData()
                } else {
                    _toastMessage.value = "Insufficient available balance"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _toastMessage.value = e.message
            }
        }
    }

    fun refreshUserData() {
        val t = _token.value ?: return
        viewModelScope.launch {
            try {
                val wRes = api.getWallet("Bearer $t")
                if (wRes.isSuccessful) _wallet.value = wRes.body()

                val bRes = api.getBets("Bearer $t")
                if (bRes.isSuccessful) _bets.value = bRes.body() ?: emptyList()

                val txRes = api.getTransactions("Bearer $t")
                if (txRes.isSuccessful) _transactions.value = txRes.body() ?: emptyList()
            } catch (e: Exception) {
                // silent
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        priceCollectionJob?.cancel()
        roundCollectionJob?.cancel()
        walletCollectionJob?.cancel()
        betResultJob?.cancel()
        WebSocketManager.disconnect()
    }
}
