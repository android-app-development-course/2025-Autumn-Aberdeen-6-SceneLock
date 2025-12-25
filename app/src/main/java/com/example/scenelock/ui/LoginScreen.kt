package com.example.scenelock.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.scenelock.R
import com.example.scenelock.model.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

private val BlueThemeGradientStart = Color(0xFFE3F2FD)
private val BlueThemeGradientEnd = Color.White
private val TextDarkBlue = Color(0xFF1565C0)
private val TextGreyBlue = Color(0xFF546E7A)

@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    onGuestLogin: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val brush = Brush.verticalGradient(
        colors = listOf(BlueThemeGradientStart, BlueThemeGradientEnd)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush), // 应用背景
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .shadow(10.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 3. 标题区域
        Text(
            text = "欢迎来到 SceneLock",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextDarkBlue
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "属于你自己的专注研究所",
            style = MaterialTheme.typography.bodyLarge,
            color = TextGreyBlue,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(64.dp))

        // 4. Google 登录按钮
        Button(
            onClick = {
                scope.launch {
                    signInWithGoogle(context, onLoginSuccess)
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp), // 圆角加大
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp
            )
        ) {
            Text(
                text = "使用 Google 账号登录",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. 游客模式按钮
        TextButton(
            onClick = onGuestLogin
        ) {
            Text(
                text = "游客模式进入 >",
                color = TextGreyBlue,
                fontSize = 14.sp
            )
        }
    }
}

// 登录核心逻辑
private suspend fun signInWithGoogle(
    context: Context,
    onLoginSuccess: (User) -> Unit
) {
    val credentialManager = CredentialManager.create(context)

    // Web Client ID
    val webClientId = "423960260522-7cnt5stiep5t7udt9inmh3jmct97hfml.apps.googleusercontent.com"

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val result = credentialManager.getCredential(
            request = request,
            context = context
        )

        val credential = result.credential

        // 处理各种凭证类型
        when (credential) {
            is GoogleIdTokenCredential -> {
                val email = credential.id
                val name = credential.displayName ?: "Google 用户"
                val profilePictureUri = credential.profilePictureUri

                Toast.makeText(context, "欢迎回来, $name!", Toast.LENGTH_SHORT).show()
                onLoginSuccess(User(name, email, profilePictureUri.toString(), true))
            }

            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val email = googleCredential.id
                        val name = googleCredential.displayName ?: "Google 用户"
                        val profilePictureUri = googleCredential.profilePictureUri

                        Toast.makeText(context, "欢迎回来, $name!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess(User(name, email, profilePictureUri.toString(), true))
                    } catch (e: Exception) {
                        Log.e("Login", "解析 CustomCredential 失败: ${e.message}")
                        Toast.makeText(context, "登录数据解析错误", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("Login", "未知的 CustomCredential 类型: ${credential.type}")
                }
            }

            else -> {
                Log.e("Login", "Unexpected credential type: ${credential.javaClass.name}")
            }
        }

    } catch (e: GetCredentialException) {
        Log.e("Login", "登录失败: ${e.message}")
        // 很多时候是用户手动取消，不需要弹Toast打扰
    }
}