package com.solidgame.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solidgame.app.ui.theme.*
import com.solidgame.app.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Loggin.dev WhatsApp Auth state
    var activeLogginToken by remember { mutableStateOf<String?>(null) }
    var activeLogginLink by remember { mutableStateOf<String?>(null) }
    var isWaitingWhatsApp by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()

    // Polling effect when waiting for WhatsApp verification from Loggin.dev
    LaunchedEffect(isWaitingWhatsApp, activeLogginToken) {
        val currentToken = activeLogginToken
        if (isWaitingWhatsApp && currentToken != null) {
            while (isActive && isWaitingWhatsApp) {
                delay(2000)
                viewModel.verifyLogginAuth(currentToken) { success, _ ->
                    if (success) {
                        isWaitingWhatsApp = false
                        onAuthSuccess()
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Solidgame",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryIndigo
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Real-Time 1.90x Payout Prediction Market",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(24.dp))

                // WhatsApp 1-Tap Login (Loggin.dev Otpless flow)
                if (isWaitingWhatsApp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F2618), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFF25D366).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = Color(0xFF25D366),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Waiting for WhatsApp Verification",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Please tap 'Send' in WhatsApp. You will be automatically logged in.",
                                fontSize = 12.sp,
                                color = Color(0xFFA5D6A7),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        activeLogginLink?.let { link ->
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                            context.startActivity(intent)
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                                ) {
                                    Text("Reopen WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                                OutlinedButton(
                                    onClick = {
                                        isWaitingWhatsApp = false
                                        activeLogginToken = null
                                    },
                                    modifier = Modifier.weight(0.8f).height(40.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Cancel", fontSize = 12.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            errorMessage = null
                            viewModel.initLogginAuth { success, token, link, err ->
                                if (success && token != null && link != null) {
                                    activeLogginToken = token
                                    activeLogginLink = link
                                    isWaitingWhatsApp = true
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        errorMessage = "Could not open WhatsApp app: ${e.message}"
                                    }
                                } else {
                                    errorMessage = err ?: "Failed to start WhatsApp login"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366)
                        ),
                        enabled = !isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "WhatsApp",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Login with WhatsApp (1-Tap)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                        }
                    }

                    if (!errorMessage.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = errorMessage!!,
                            color = DownRed,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "100% Encrypted • 1-Tap OTP-less WhatsApp Auth",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}
