package com.example.scenelock.ui

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.scenelock.R
import com.example.scenelock.MainActivity // 确保引入你的 MainActivity
import com.example.scenelock.model.FocusScene
import com.example.scenelock.utils.AppLockManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FocusScreen(
    whitelist: Set<String>,
    onOpenDrawer: () -> Unit,
    onFocusFinished: (Int, FocusScene) -> Unit
) {
    var isFocusing by remember { mutableStateOf(false) }
    var currentScene by remember { mutableStateOf(FocusScene.LIBRARY) }
    var focusDurationMinutes by remember { mutableIntStateOf(25) }
    var timeLeftSeconds by remember { mutableLongStateOf(25 * 60L) }
    var isMuted by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showGiveUpDialog by remember { mutableStateOf(false) }

    // 权限提示框状态
    var showPermissionDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // 音频播放器逻辑
    val mediaPlayer = remember(currentScene) {
        MediaPlayer.create(context, currentScene.audioResId).apply { isLooping = true }
    }
    DisposableEffect(mediaPlayer) { onDispose { mediaPlayer.release() } }
    LaunchedEffect(isFocusing) {
        if (isFocusing) { if (!mediaPlayer.isPlaying) mediaPlayer.start() }
        else { if (mediaPlayer.isPlaying) mediaPlayer.pause(); try{mediaPlayer.seekTo(0)}catch(e:Exception){} }
    }
    LaunchedEffect(isMuted, mediaPlayer) {
        if (isMuted) mediaPlayer.setVolume(0f, 0f) else mediaPlayer.setVolume(1f, 1f)
    }

    // 核心应用锁监控协程
    LaunchedEffect(isFocusing) {
        if (isFocusing) {
            while (isFocusing) {
                // 提高检测频率到 200ms
                delay(500)

                // 只有当两个权限都有的时候才工作
                if (AppLockManager.hasUsageStatsPermission(context) && AppLockManager.hasOverlayPermission(context)) {
                    val currentPkg = AppLockManager.getForegroundApp(context)

                    if (currentPkg != null
                        && currentPkg != context.packageName // 不是自己
                        && !whitelist.contains(currentPkg) // 不在白名单
                        && !currentPkg.contains("launcher") // 允许桌面(避免切不出后台)
                        && !currentPkg.contains("systemui") // 允许下拉栏
                    ) {
                        // 强力拉回逻辑
                        val intent = Intent(context, MainActivity::class.java)
                        // 关键 Flags：清除栈顶，重新把 Activity 带到前台
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }

    // 倒计时逻辑
    LaunchedEffect(isFocusing, timeLeftSeconds) {
        if (isFocusing && timeLeftSeconds > 0) {
            delay(1000L)
            timeLeftSeconds--
        } else if (isFocusing && timeLeftSeconds == 0L) {
            isFocusing = false
            onFocusFinished(focusDurationMinutes, currentScene)
            Toast.makeText(context, "专注完成！", Toast.LENGTH_LONG).show()
        }
    }

    val mainContentColor = Color(0xff855f45)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(id = currentScene.bgImageResId), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())

        Scaffold(
            topBar = {
                AnimatedVisibility(visible = !isFocusing, enter = fadeIn(), exit = fadeOut()) {
                    TopAppBar(
                        title = { Text("") },
                        navigationIcon = {
                            IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "Menu", tint = Color.Gray) }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                // 静音按钮
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopEnd) {
                    AnimatedVisibility(visible = isFocusing) {
                        IconButton(onClick = { isMuted = !isMuted }, modifier = Modifier.background(Color.White.copy(alpha = 0.5f), CircleShape)) {
                            Icon(if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.Default.VolumeUp, "Mute", tint = Color.Black)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    AnimatedVisibility(visible = isFocusing) {
                        Text(formatTime(timeLeftSeconds), style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold, color = mainContentColor), modifier = Modifier.padding(bottom = 24.dp))
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(280.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .combinedClickable(
                                onClick = {
                                    if (!isFocusing) {
                                        // ★★★ 点击开始专注前，先检查权限 ★★★
                                        if (!AppLockManager.hasUsageStatsPermission(context) || !AppLockManager.hasOverlayPermission(context)) {
                                            showPermissionDialog = true
                                        } else {
                                            // 权限齐全，开始专注
                                            timeLeftSeconds = focusDurationMinutes * 60L
                                            isFocusing = true
                                        }
                                    } else {
                                        showGiveUpDialog = true
                                    }
                                },
                                onLongClick = { if (!isFocusing) showSettingsDialog = true }
                            )
                    ) {
                        Image(painter = painterResource(id = currentScene.iconResId), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }

    // 权限引导弹窗
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("需要权限") },
            text = { Text("为了实现锁机功能，请授权以下权限：\n1. 使用情况访问权限\n2. 显示在其他应用上层 (悬浮窗)") },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    // 1. 如果没使用权限，跳去使用权限设置
                    if (!AppLockManager.hasUsageStatsPermission(context)) {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    }
                    // 2. 如果没悬浮窗权限，跳去悬浮窗设置
                    if (!AppLockManager.hasOverlayPermission(context)) {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                        context.startActivity(intent)
                    }
                    Toast.makeText(context, "请依次开启权限后返回", Toast.LENGTH_LONG).show()
                }) {
                    Text("去开启")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("取消") }
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(initialTime = focusDurationMinutes, initialScene = currentScene, onDismiss = { showSettingsDialog = false }, onConfirm = { time, scene -> focusDurationMinutes = time; currentScene = scene; showSettingsDialog = false })
    }
    if (showGiveUpDialog) {
        GiveUpDialog(onDismiss = { showGiveUpDialog = false }) { isFocusing = false; showGiveUpDialog = false; Toast.makeText(context, "放弃专注", Toast.LENGTH_SHORT).show() }
    }
}

// 辅助函数
private fun formatTime(seconds: Long): String {
    val h = seconds / 3600; val m = (seconds % 3600) / 60; val s = seconds % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
private fun formatDurationUi(minutes: Int): String {
    val h = minutes / 60; val m = minutes % 60
    return if (h > 0) "${h}小时 ${m}分钟" else "${m}分钟"
}
@Composable private fun SettingsDialog(initialTime: Int, initialScene: FocusScene, onDismiss: () -> Unit, onConfirm: (Int, FocusScene) -> Unit) {
    var time by remember { mutableIntStateOf(initialTime) }; var scene by remember { mutableStateOf(initialScene) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("专注设置") }, text = { Column { Text("时长: ${formatDurationUi(time)}"); Slider(value = time.toFloat(), onValueChange = { time = it.toInt() }, valueRange = 1f..480f); Spacer(Modifier.height(16.dp)); Text("场景:"); Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) { FocusScene.values().forEach { s -> FilterChip(selected = (scene == s), onClick = { scene = s }, label = { Text(s.title) }) } } } }, confirmButton = { TextButton(onClick = { onConfirm(time, scene) }) { Text("确定") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } })
}
@Composable
private fun GiveUpDialog(onDismiss: () -> Unit, onConfirmGiveUp: () -> Unit) {
    var inputText by remember { mutableStateOf("") }
    val confirmText = "我确认结束专注"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("放弃专注？") },
        text = {
            Column {
                Text("输入以下文字确认放弃：")
                Text(confirmText, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                OutlinedTextField(value = inputText, onValueChange = { inputText = it }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = onConfirmGiveUp, enabled = inputText == confirmText, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("放弃") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("继续") } }
    )
}