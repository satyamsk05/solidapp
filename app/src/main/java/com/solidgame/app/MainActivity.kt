package com.solidgame.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.solidgame.app.ui.components.CustomBottomNavBar
import com.solidgame.app.ui.components.NavTab
import com.solidgame.app.ui.screens.*
import com.solidgame.app.ui.theme.SolidgameTheme
import com.solidgame.app.ui.theme.DarkBackground
import com.solidgame.app.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SolidgameTheme {
                val user by viewModel.user.collectAsState()
                val toastMsg by viewModel.toastMessage.collectAsState()
                var currentTab by remember { mutableStateOf(NavTab.HOME) }

                // Display live toast alerts
                LaunchedEffect(toastMsg) {
                    toastMsg?.let { msg ->
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                if (user == null) {
                    // Show Auth Flow
                    AuthScreen(
                        viewModel = viewModel,
                        onAuthSuccess = {
                            currentTab = NavTab.HOME
                        }
                    )
                } else {
                    // Main App with Bottom Navigation
                    Scaffold(
                        bottomBar = {
                            CustomBottomNavBar(
                                selectedTab = currentTab,
                                onTabSelected = { currentTab = it }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DarkBackground)
                                .padding(innerPadding)
                        ) {
                            when (currentTab) {
                                NavTab.HOME -> HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToTrade = { currentTab = NavTab.TRADE },
                                    onNavigateToWallet = { currentTab = NavTab.WALLET }
                                )
                                NavTab.TRADE -> BettingScreen(
                                    viewModel = viewModel
                                )
                                NavTab.WALLET -> WalletScreen(
                                    viewModel = viewModel
                                )
                                NavTab.HISTORY -> HistoryScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
