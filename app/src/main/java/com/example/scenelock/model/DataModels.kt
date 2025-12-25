package com.example.scenelock.model

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.scenelock.R

enum class FocusScene(
    val title: String,
    val iconResId: Int,
    val bgImageResId: Int,
    val audioResId: Int
) {
    LIBRARY("图书馆", R.drawable.scene_library, R.drawable.bg, R.raw.library_sound),
    CAFE("咖啡厅", R.drawable.scene_cafe, R.drawable.bg, R.raw.cafe_sound),
    AIRPORT("飞机场", R.drawable.scene_airport, R.drawable.bg, R.raw.airport_sound)
}

enum class AppScreen {
    LOGIN, HOME, SHOP, PROFILE, ACHIEVEMENT, STATS, WHITELIST
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?, // 图标是 Drawable 类型
    var isWhitelisted: Boolean = false // 是否在白名单
)
data class User(
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val isLoggedIn: Boolean = false
)

data class Achievement(
    val id: Int,
    val title: String,
    val description: String,
    val target: Int,
    var progress: Int = 0,
    var isUnlocked: Boolean = false,
    val iconRes: Int = 0
)

data class FocusRecord(
    val timestamp: Long,
    val durationMinutes: Int,
    val scene: FocusScene
)

data class ShopItem(
    val id: Int,
    val name: String,
    val price: Int,
    val icon: ImageVector,
    val color: Color,
    var isOwned: Boolean = false
)