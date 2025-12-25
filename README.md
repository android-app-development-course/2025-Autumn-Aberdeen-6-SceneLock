# 境所 SceneLock 

> **重新掌握时间，回归深度专注。**
>
> *Reclaim your time, return to deep focus.*

![Version](https://img.shields.io/badge/version-1.0.1-blue.svg) ![Language](https://img.shields.io/badge/language-Kotlin-purple.svg) ![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-green.svg) ![Platform](https://img.shields.io/badge/platform-Android-orange.svg)

## 项目简介

在这个信息碎片化的时代，**境所 (SceneLock)** 是一款专为解决手机成瘾和注意力涣散而设计的 Android 应用程序。

不同于普通的番茄钟，SceneLock 采用“强制性的温柔”理念。它通过技术手段构建一个数字禁区，结合**沉浸式白噪音场景**、**严苛的应用锁机制**以及**游戏化的激励系统**，帮助学生、考研党和远程办公者快速进入心流状态。

##  核心功能

*   **强制专注模式**
    *   基于 `UsageStatsManager` 和悬浮窗权限，实时监控前台应用。
    *   一旦检测到非白名单应用，立即强制拉回专注页面，确保无干扰。
*   **沉浸式场景**
    *   提供图书馆、咖啡厅、飞机场等多种预设场景。
    *   动态切换全屏背景壁纸与高保真环境白噪音（雨声、翻书声、广播声）。
*   **白名单机制**
    *   允许用户自定义专注期间可使用的 App，平衡刚需与自律。
*   **游戏化激励**
    *   **货币系统**：专注时长自动转化为金币。
    *   **道具商城**：使用金币购买精美的虚拟数字收藏品。
    *   **成就系统**：Steam 风格的成就列表，记录每一次突破。
*   **数据统计**
    *   手绘折线图展示近 7 天专注趋势。
    *   概览卡片显示总时长、今日专注及日均数据。

## 🛠️ 技术栈

*   **语言**: Kotlin
*   **UI 框架**: Jetpack Compose (Material3)
*   **架构**: Single Activity Architecture
*   **关键技术**:
    *   **后台监控**: Kotlin Coroutines + `UsageStatsManager`
    *   **界面覆盖**: `SYSTEM_ALERT_WINDOW` (悬浮窗权限)
    *   **音频处理**: `MediaPlayer` (生命周期管理 & 动态切换)
    *   **认证**: Firebase Authentication & Credential Manager API
    *   **绘图**: Compose Canvas (自定义图表)

## 📂 项目结构

```text
com.example.scenelock
├── MainActivity.kt          // App 入口，负责页面导航与全局状态管理
├── model                    // 数据模型层
│   └── DataModels.kt        // 定义 User, FocusScene, ShopItem, Achievement 等数据类
├── ui                       // 界面展示层
│   ├── theme                // 主题配置
│   ├── FocusScreen.kt       // 核心专注页面（含锁机逻辑、计时器、音频控制）
│   ├── LoginScreen.kt       // Google 登录与游客入口
│   ├── ShopScreen.kt        // 道具商城
│   ├── AchievementScreen.kt // 成就列表
│   ├── StatsScreen.kt       // 数据统计图表
│   ├── WhitelistScreen.kt   // 应用白名单设置
│   ├── ProfileScreen.kt     // 个人中心
│   └── AppDrawer.kt         // 侧边栏导航组件
└── utils                    // 工具类
    └── AppLockManager.kt    // 封装权限检查与前台应用检测逻辑
```

## 📄 许可证 (License)

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件。

---

**境所 SceneLock** - *如果你管不住自己，就让境所来管你。*
