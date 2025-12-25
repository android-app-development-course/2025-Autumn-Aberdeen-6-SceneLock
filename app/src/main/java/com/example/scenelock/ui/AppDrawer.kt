package com.example.scenelock.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.scenelock.model.AppScreen
import com.example.scenelock.model.User
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.Brush

@Composable
fun AppDrawer(
    currentUser: User,
    onNavigate: (AppScreen) -> Unit,
    onCloseDrawer: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE3F2FD),
            Color.White
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(16.dp)
    ) {
        // 头像区域
        @OptIn(ExperimentalFoundationApi::class)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .combinedClickable(onClick = {
                    if (currentUser.isLoggedIn) {
                        // 如果已登录，跳转到个人主页
                        onNavigate(AppScreen.PROFILE)
                        onCloseDrawer() // 顺便关闭侧边栏
                    } else {
                        // 如果没登录，去登录页
                        onNavigate(AppScreen.LOGIN)
                        onCloseDrawer()
                    }
                })
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Avatar",
                modifier = Modifier.size(64.dp),
                tint = if (currentUser.isLoggedIn) Color(0xFF1976D2)  else Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = currentUser.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (currentUser.isLoggedIn) currentUser.email else "点击登录",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        HorizontalDivider()

        NavigationDrawerItem(
            label = { Text("专注") },
            selected = false,
            icon = { Icon(Icons.Default.Timer, null) },
            onClick = { onNavigate(AppScreen.HOME); onCloseDrawer() },
            modifier = Modifier.padding(vertical = 4.dp)
        )

        NavigationDrawerItem(
            label = { Text("商城") },
            selected = false,
            icon = { Icon(Icons.Default.ShoppingCart, null) },
            onClick = { onNavigate(AppScreen.SHOP); onCloseDrawer() },
            modifier = Modifier.padding(vertical = 4.dp)
        )

        NavigationDrawerItem(
            label = { Text("成就") },
            selected = false,
            icon = { Icon(Icons.Default.EmojiEvents, null) },
            onClick = { onNavigate(AppScreen.ACHIEVEMENT); onCloseDrawer() },
            modifier = Modifier.padding(vertical = 4.dp)
        )

        NavigationDrawerItem(
            label = { Text("统计") },
            selected = false,
            icon = { Icon(Icons.Default.BarChart, null) },
            onClick = { onNavigate(AppScreen.STATS); onCloseDrawer() },
            modifier = Modifier.padding(vertical = 4.dp)
        )

        NavigationDrawerItem(
            label = { Text("白名单设置") },
            selected = false,
            icon = { Icon(Icons.Default.Settings, null) },
            onClick = { onNavigate(AppScreen.WHITELIST); onCloseDrawer() },
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.weight(1f)) // 把退出登录顶到底部

        // 退出登录按钮
        NavigationDrawerItem(
            label = { Text("退出登录") },
            selected = false,
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) },
            onClick = onLogout,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}