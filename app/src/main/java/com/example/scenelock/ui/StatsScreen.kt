package com.example.scenelock.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scenelock.model.FocusRecord
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    records: List<FocusRecord>,
    onBack: () -> Unit
) {
    // 1. 计算统计数据逻辑
    val totalCount = records.size
    val totalMinutes = records.sumOf { it.durationMinutes }
    val averageMinutes = if (totalCount > 0) totalMinutes / totalCount else 0

    // 计算今日时长
    val todayStr = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        .withZone(ZoneId.systemDefault())
        .format(Instant.now())

    val todayMinutes = records.filter {
        val dateStr = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(it.timestamp))
        dateStr == todayStr
    }.sumOf { it.durationMinutes }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE3F2FD),
            Color.White
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("数据统计") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // --- 概览卡片区域 ---
                item {
                    Text("概览", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "总专注时长",
                            value = "${totalMinutes / 60}h ${totalMinutes % 60}m",
                            icon = Icons.Default.AccessTime,
                            color = Color(0xFF42A5F5)
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "专注次数",
                            value = "$totalCount 次",
                            icon = Icons.Default.History,
                            color = Color(0xFF66BB6A)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "日均时长",
                            value = "${averageMinutes}m",
                            icon = Icons.Default.CalendarToday,
                            color = Color(0xFFFFA726)
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "今日专注",
                            value = "${todayMinutes}m",
                            icon = Icons.Default.Timeline,
                            color = Color(0xFFAB47BC)
                        )
                    }
                }

                // --- 折线图区域 ---
                item {
                    Text("近7天趋势 (分钟)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    WeeklyChart(records)
                }
            }
        }
    }
}

// 单个统计卡片组件
@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeeklyChart(records: List<FocusRecord>) {
    // 准备最近 7 天的数据
    val last7DaysData = remember(records) {
        val data = FloatArray(7)
        val today = java.time.LocalDate.now()
        for (i in 0..6) {
            val date = today.minusDays((6 - i).toLong()) // 从6天前到今天
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

            val minutes = records.filter {
                val rDate = Instant.ofEpochMilli(it.timestamp)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                rDate.toString() == dateStr
            }.sumOf { it.durationMinutes }

            data[i] = minutes.toFloat()
        }
        data
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().height(220.dp)
    ) {
        Box(modifier = Modifier.padding(24.dp).fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val maxVal = (last7DaysData.maxOrNull() ?: 60f).coerceAtLeast(60f)

                val path = Path()
                val stepX = width / 6

                last7DaysData.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = height - (value / maxVal * height)

                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)

                    // 绘制数据点
                    drawCircle(
                        color = Color(0xFF2167F3),
                        radius = 8f,
                        center = Offset(x, y)
                    )
                }

                // 绘制线条
                drawPath(
                    path = path,
                    color = Color(0xFF2196F3),
                    style = Stroke(width = 5f)
                )

                // 绘制填充渐变
                val fillPath = Path()
                fillPath.addPath(path)
                fillPath.lineTo(width, height)
                fillPath.lineTo(0f, height)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2196F3).copy(alpha = 0.3f), Color.Transparent)
                    )
                )
            }
        }
    }
}