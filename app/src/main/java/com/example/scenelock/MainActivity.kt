package com.example.scenelock

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.example.scenelock.model.Achievement
import kotlinx.coroutines.launch
import com.example.scenelock.model.AppScreen
import com.example.scenelock.model.ShopItem
import com.example.scenelock.model.User // 引入User
import com.example.scenelock.ui.AchievementScreen
import com.example.scenelock.ui.AppDrawer
import com.example.scenelock.ui.FocusScreen
import com.example.scenelock.ui.ShopScreen
import com.example.scenelock.ui.LoginScreen // 引入LoginScreen
import com.example.scenelock.ui.ProfileScreen
import com.example.scenelock.model.FocusRecord
import com.example.scenelock.model.FocusScene
import com.example.scenelock.ui.StatsScreen
import java.time.Instant
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.scenelock.model.AppInfo
import com.example.scenelock.ui.WhitelistScreen
import com.example.scenelock.utils.AppLockManager
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SceneLockApp()
        }
    }
}

@Composable
fun SceneLockApp() {
    val context = LocalContext.current
    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    LaunchedEffect(Unit) {
        // 在后台线程加载应用列表，防止卡顿
        installedApps = AppLockManager.getInstalledApps(context)
    }

    // 2. 白名单集合 (存储选中的包名)
    val whitelist = remember { mutableStateListOf<String>() }

    // 3. 专注锁机逻辑 ★★★★★
    // 这是一个全局监控，只要 currentScreen 是 HOME 且 focus 正在进行（这里我们通过 FocusScreen 的状态判断不太容易，
    // 最好的办法是把 isFocusing 状态提升到 MainActivity，或者利用一个回调）

    // 为了简单实现，我们在 Main 里加一个 "全局专注状态" 标志位
    var isGlobalFocusing by remember { mutableStateOf(false) }

    LaunchedEffect(isGlobalFocusing) {
        if (isGlobalFocusing) {
            // 开始监控循环
            while (isGlobalFocusing) {
                delay(500) // 每 0.5 秒检查一次

                // 只有获得了权限才检查
                if (AppLockManager.hasUsageStatsPermission(context)) {
                    val currentPkg = AppLockManager.getForegroundApp(context)

                    // 如果前台应用存在，且不是我自己，且不在白名单里
                    if (currentPkg != null
                        && currentPkg != context.packageName // 不是 SceneLock
                        && !whitelist.contains(currentPkg) // 不在白名单
                    ) {
                        // ★★★ 发现违规应用！强制拉回 SceneLock ★★★
                        val intent = Intent(context, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        context.startActivity(intent)

                        // 可选：显示个 Toast 警告
                        // Toast.makeText(context, "专注中！禁止打开该应用", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // 默认起始页改为 LOGIN
    var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }

    // 保存当前用户状态
    var currentUser by remember {
        mutableStateOf(User("游客", "", null, false))
    }

    var totalCoins by remember { mutableIntStateOf(100) }
    val shopItems = remember {
        mutableStateListOf(
            // --- 基础道具 (10-100 金币) ---
            ShopItem(1, "幸运四叶草", 30, Icons.Default.Spa, Color(0xFFA5D6A7)), // 浅绿
            ShopItem(2, "飞机模型", 50, Icons.Default.Flight, Color(0xFF90CAF9)), // 浅蓝
            ShopItem(3, "学士帽", 60, Icons.Default.School, Color(0xFFCE93D8)), // 浅紫
            ShopItem(4, "生日蛋糕", 66, Icons.Default.Cake, Color(0xFFFFAB91)), // 浅红
            ShopItem(5, "复古相机", 80, Icons.Default.CameraAlt, Color(0xFFB0BEC5)), // 灰蓝
            ShopItem(6, "向日葵", 40, Icons.Default.LocalFlorist, Color(0xFFFFE082)), // 金黄
            ShopItem(7, "绘画套装", 75, Icons.Default.Brush, Color(0xFFF48FB1)), // 粉色

            // --- 进阶生活 (100-300 金币) ---
            ShopItem(8, "头戴耳机", 110, Icons.Default.Headphones, Color(0xFF9FA8DA)), // 靛青
            ShopItem(9, "豪华跑车", 120, Icons.Default.DirectionsCar, Color(0xFFEF9A9A)), // 红色
            ShopItem(10, "智能手表", 130, Icons.Default.Watch, Color(0xFF4DB6AC)), // 青色
            ShopItem(11, "手冲咖啡机", 150, Icons.Default.LocalCafe, Color(0xFFD7CCC8)), // 褐色
            ShopItem(12, "电吉他", 180, Icons.Default.MusicNote, Color(0xFFFFCC80)), // 橙色
            ShopItem(13, "宠物小狗", 200, Icons.Default.Pets, Color(0xFFFFD54F)), // 黄色
            ShopItem(14, "山地自行车", 220, Icons.Default.PedalBike, Color(0xFF80CBC4)), // 蓝绿
            ShopItem(15, "游戏主机", 250, Icons.Default.Gamepad, Color(0xFF9575CD)), // 深紫

            // --- 奢华大奖 (300-1000+ 金币) ---
            ShopItem(16, "神秘城堡", 300, Icons.Default.Castle, Color(0xFFB39DDB)), // 紫色
            ShopItem(17, "高性能电脑", 350, Icons.Default.Computer, Color(0xFF90A4AE)), // 蓝灰
            ShopItem(18, "豪华游艇", 450, Icons.Default.DirectionsBoat, Color(0xFF29B6F6)), // 蓝色
            ShopItem(19, "海边别墅", 500, Icons.Default.BeachAccess, Color(0xFF4FC3F7)), // 天蓝
            ShopItem(20, "舒适沙发", 600, Icons.Default.Weekend, Color(0xFFA1887F)), // 棕色
            ShopItem(21, "冠军奖杯", 800, Icons.Default.EmojiEvents, Color(0xFFFFD700)), // 金色
            ShopItem(22, "稀世钻石", 999, Icons.Default.Diamond, Color(0xFFE0F7FA)), // 钻石青
            ShopItem(23, "火星火箭", 2000, Icons.Default.RocketLaunch, Color(0xFFFF7043)) // 深橙
        )
    }

    val achievementList = remember {
        mutableStateListOf(
            Achievement(1, "第一次封印", "完成首次强制专注模式，不中途退出。", 1),
            Achievement(2, "逃生按钮？不存在的", "连续 5 次专注未触发紧急退出。", 5),
            Achievement(3, "心流入门者", "累计专注时长达到 5 小时 (300分钟)。", 300),
            Achievement(4, "我不需要手机", "连续 7 天每天至少完成一次专注。", 7),
            Achievement(5, "专注比命重要", "在 60 分钟以上的专注中坚持到结束。", 1),
            Achievement(6, "自律不是选择", "连续 3 次失败后仍再次开启专注并成功。", 1),
            Achievement(7, "白噪音信徒", "单一场景累计使用时长超过 10 小时。", 600),
            Achievement(8, "境所原住民", "累计专注总时长达到 50 小时。", 3000),
            Achievement(9, "你居然真的没退", "在 90 分钟专注中一次紧急退出都没用。", 1),
            Achievement(10, "系统都拦不住你", "连续 10 次专注全部成功，无失败记录。", 10),
            Achievement(11, "深夜境所居民", "凌晨 1:00–5:00 完成一次完整专注。", 1),
            Achievement(12, "场景偏执狂", "连续 20 次只使用同一个场景进行专注。", 20),
            Achievement(13, "失控后的自控", "当天有失败记录，但仍累计完成 3 小时专注。", 180),
            Achievement(14, "你已经习惯被管着了", "连续 14 天每天都开启专注。", 14)
        )
    }

    val focusRecords = remember {
        mutableStateListOf(
            // 假数据：今天专注了 25 分钟
            FocusRecord(System.currentTimeMillis(), 25, FocusScene.LIBRARY),
            // 假数据：昨天专注了 50 分钟 (86400000 毫秒 = 1天)
            FocusRecord(System.currentTimeMillis() - 86400000, 50, FocusScene.CAFE),
            // 假数据：前天专注了 120 分钟
            FocusRecord(System.currentTimeMillis() - 86400000 * 2, 120, FocusScene.AIRPORT),
            // 假数据：大前天偷懒了，0分钟
            FocusRecord(System.currentTimeMillis() - 86400000 * 4, 15, FocusScene.LIBRARY),
        )
    }

    // 如果是登录页，不显示侧边栏结构，直接显示登录全屏
    if (currentScreen == AppScreen.LOGIN) {
        LoginScreen(
            onLoginSuccess = { user ->
                currentUser = user
                currentScreen = AppScreen.HOME // 登录成功跳主页
            },
            onGuestLogin = {
                currentUser = User("游客", "", null, false)
                currentScreen = AppScreen.HOME // 游客也跳主页
            }
        )
    } else {
        // 主程序结构 (包含侧边栏)
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    AppDrawer(
                        currentUser = currentUser, // 传进去显示头像名字
                        onNavigate = { screen ->
                            currentScreen = screen
                        },
                        onCloseDrawer = {
                            scope.launch { drawerState.close() }
                        },
                        onLogout = { // 处理登出
                            currentScreen = AppScreen.LOGIN
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            },
        ) {
            when (currentScreen) {
                AppScreen.HOME -> {
                    FocusScreen(
                        whitelist = whitelist.toSet(),
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        // ★★★ 修改点：接收 minutes 和 scene 两个参数 ★★★
                        onFocusFinished = { minutes, scene ->
                            totalCoins += minutes

                            // 更新成就
                            achievementList.forEachIndexed { index, achievement ->
                                if (!achievement.isUnlocked) {
                                    val newProgress = achievement.progress + minutes
                                    achievementList[index] = achievement.copy(
                                        progress = newProgress.coerceAtMost(achievement.target),
                                        isUnlocked = newProgress >= achievement.target
                                    )
                                }
                            }

                            // ★★★ 核心修复：现在这里的 scene 就是从 FocusScreen 传出来的了 ★★★
                            focusRecords.add(0, FocusRecord(
                                timestamp = System.currentTimeMillis(),
                                durationMinutes = minutes,
                                scene = scene // 这里不会再报错了
                            ))
                        }
                    )
                }
                AppScreen.SHOP -> {
                    ShopScreen(
                        userCoins = totalCoins,
                        items = shopItems,
                        onBack = { currentScreen = AppScreen.HOME },
                        onBuy = { item ->
                            if (totalCoins >= item.price) {
                                totalCoins -= item.price
                                val index = shopItems.indexOf(item)
                                if (index != -1) {
                                    shopItems[index] = item.copy(isOwned = true)
                                }
                                true
                            } else {
                                false
                            }
                        }
                    )
                }
                AppScreen.PROFILE -> {
                    ProfileScreen(
                        user = currentUser,
                        onBack = { currentScreen = AppScreen.HOME }, // 点击返回回主页
                        onLogout = {
                            // 处理登出逻辑
                            currentUser = User("游客", "", null, false)
                            currentScreen = AppScreen.LOGIN
                        }
                    )
                }

                AppScreen.ACHIEVEMENT -> {
                    AchievementScreen(
                        achievements = achievementList,
                        onBack = { currentScreen = AppScreen.HOME }
                    )
                }

                AppScreen.STATS -> {
                    StatsScreen(
                        records = focusRecords,
                        onBack = { currentScreen = AppScreen.HOME }
                    )
                }

                AppScreen.WHITELIST -> {
                    WhitelistScreen(
                        allApps = installedApps,
                        whitelist = whitelist.toSet(),
                        onToggleApp = { pkg, isSelected ->
                            if (isSelected) whitelist.add(pkg) else whitelist.remove(pkg)
                        },
                        onBack = { currentScreen = AppScreen.HOME }
                    )
                }

                else -> {}
            }
        }
    }
}