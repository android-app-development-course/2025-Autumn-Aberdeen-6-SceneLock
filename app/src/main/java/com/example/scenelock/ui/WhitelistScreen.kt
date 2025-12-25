package com.example.scenelock.ui

import android.content.Intent
import android.provider.Settings
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.scenelock.model.AppInfo
import com.example.scenelock.utils.AppLockManager

private val BlueThemeGradientStart = Color(0xFFE3F2FD)
private val BlueThemeGradientEnd = Color.White
private val TextDarkBlue = Color(0xFF1565C0)
private val TextNormal = Color(0xFF424242)
private val CheckboxChecked = Color(0xFF1976D2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhitelistScreen(
    allApps: List<AppInfo>,
    whitelist: Set<String>,
    onToggleApp: (String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val hasPermission = remember { AppLockManager.hasUsageStatsPermission(context) }

    // 1. 背景渐变
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(BlueThemeGradientStart, BlueThemeGradientEnd)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "白名单设置",
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
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // 2. 权限警告区域 (美化版)
                if (!hasPermission) {
                    Card(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "锁机功能受限！点击开启「使用情况访问权限」",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    // 说明文字
                    Surface(
                        color = TextDarkBlue.copy(alpha = 0.05f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "专注期间，只有被勾选的应用允许打开",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            color = TextDarkBlue.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                }

                // 3. 应用列表 (卡片式)
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allApps) { app ->
                        val isSelected = whitelist.contains(app.packageName)
                        AppItemCard(
                            app = app,
                            isSelected = isSelected,
                            onToggle = { checked -> onToggleApp(app.packageName, checked) }
                        )
                    }
                }
            }
        }
    }
}

// 独立的 App 卡片组件
@Composable
fun AppItemCard(
    app: AppInfo,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle(!isSelected) } // 点击整个卡片也能切换
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App 图标
            AndroidView(
                factory = { ctx ->
                    ImageView(ctx).apply {
                        setImageDrawable(app.icon)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                    }
                },
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 文字信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    fontWeight = FontWeight.Bold,
                    color = TextNormal,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 复选框
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = CheckboxChecked,
                    checkmarkColor = Color.White,
                    uncheckedColor = Color.LightGray
                )
            )
        }
    }
}