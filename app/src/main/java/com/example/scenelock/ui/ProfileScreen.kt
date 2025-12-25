package com.example.scenelock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scenelock.model.User

private val BlueThemeGradientStart = Color(0xFFE3F2FD)
private val BlueThemeGradientEnd = Color.White
private val TextDarkBlue = Color(0xFF1565C0)
private val IconBlue = Color(0xFF2196F3)
private val AvatarGradientStart = Color(0xFF64B5F6)
private val AvatarGradientEnd = Color(0xFF1976D2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    // 1. 背景渐变画笔
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(BlueThemeGradientStart, BlueThemeGradientEnd)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush) // 应用背景
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "个人中心",
                            fontWeight = FontWeight.Bold,
                            color = TextDarkBlue
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "Back",
                                tint = TextDarkBlue
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent // 让 Scaffold 透明
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 2. 顶部大头像区域
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(8.dp, CircleShape) // 添加阴影
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(AvatarGradientStart, AvatarGradientEnd)
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(100.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 用户名
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextDarkBlue
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 3. 信息卡片区域
                InfoCard(
                    icon = Icons.Default.Person,
                    label = "昵称 / Nickname",
                    value = user.name
                )

                Spacer(modifier = Modifier.height(16.dp))

                InfoCard(
                    icon = Icons.Default.Email,
                    label = "账号 / Email",
                    value = user.email
                )

                Spacer(modifier = Modifier.weight(1f)) // 把退出按钮顶到底部

                // 4. 退出登录按钮
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer, // 淡红色背景
                        contentColor = MaterialTheme.colorScheme.onErrorContainer  // 深红色文字
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        "退出登录",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// 辅助组件：信息卡片
@Composable
fun InfoCard(icon: ImageVector, label: String, value: String) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标底色圆圈
            Surface(
                color = BlueThemeGradientStart, // 浅蓝底
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = IconBlue, // 亮蓝图标
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.8f)
                )
            }
        }
    }
}