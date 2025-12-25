package com.example.scenelock.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scenelock.model.Achievement

// 定义配色常量
private val BlueThemeGradientStart = Color(0xFFE3F2FD)
private val BlueThemeGradientEnd = Color.White
private val TextDarkBlue = Color(0xFF1565C0)
private val TextNormalBlue = Color(0xFF424242)
private val AccentBlue = Color(0xFF2196F3)
private val GoldTrophy = Color(0xFFFFEFA1)
private val LockedGray = Color(0xFF90A4AE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    achievements: List<Achievement>,
    onBack: () -> Unit
) {
    // 将成就分为两组
    val unlockedList = achievements.filter { it.isUnlocked }
    val lockedList = achievements.filter { !it.isUnlocked }

    // 背景渐变画笔
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
                        Column {
                            Text(
                                "成就里程碑",
                                fontWeight = FontWeight.Bold,
                                color = TextDarkBlue
                            )
                            Text(
                                "当前进度: ${unlockedList.size} / ${achievements.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDarkBlue.copy(alpha = 0.7f)
                            )
                        }
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. 已解锁区域
                if (unlockedList.isNotEmpty()) {
                    item {
                        SectionHeader("已解锁荣誉", AccentBlue)
                    }
                    items(unlockedList) { item ->
                        AchievementCard(item, isLocked = false)
                    }
                }

                // 2. 未解锁区域
                if (lockedList.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader("待探索", LockedGray)
                    }
                    items(lockedList) { item ->
                        AchievementCard(item, isLocked = true)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun AchievementCard(item: Achievement, isLocked: Boolean) {
    // 进度条动画
    val progressAnimated by animateFloatAsState(
        targetValue = if (item.target > 0) item.progress.toFloat() / item.target.toFloat() else 0f,
        animationSpec = tween(1000),
        label = "ProgressAnimation"
    )

    Card(
        colors = CardDefaults.cardColors(
            // 已解锁：纯白底；未解锁：半透明白底
            containerColor = if (isLocked) Color.White.copy(alpha = 0.6f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            // 已解锁：稍微浮起；未解锁：扁平
            defaultElevation = if (isLocked) 0.dp else 4.dp
        ),
        shape = RoundedCornerShape(16.dp), // 更圆润的角
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标区域
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)) // 图标也是圆角
                    .background(
                        if (isLocked) Color(0xFFECEFF1) // 锁定时：淡灰
                        else GoldTrophy.copy(alpha = 0.2f) // 解锁时：淡金背景
                    )
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = if (isLocked) LockedGray else GoldTrophy, // 锁定时灰图标，解锁时金图标
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 中间文字区域
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isLocked) LockedGray else TextDarkBlue // 标题颜色区分
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLocked) LockedGray.copy(alpha = 0.8f) else TextNormalBlue.copy(alpha = 0.8f),
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 进度条
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { progressAnimated },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isLocked) LockedGray else AccentBlue,
                        trackColor = Color(0xFFF5F5F5)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (item.isUnlocked) "DONE" else "${(progressAnimated * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isLocked) LockedGray else AccentBlue
                    )
                }
            }
        }
    }
}