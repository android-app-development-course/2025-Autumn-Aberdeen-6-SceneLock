package com.example.scenelock.utils

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.example.scenelock.model.AppInfo

object AppLockManager {

    // 1. 检查“使用情况访问权限”
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // ★★★ 新增：检查“悬浮窗/显示在其他应用上层”权限 ★★★
    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    // 2. 获取已安装应用
    fun getInstalledApps(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val apps = packageManager.getInstalledPackages(0)
        val appList = mutableListOf<AppInfo>()

        for (app in apps) {
            // 排除自己
            if (app.packageName == context.packageName) continue
            // 简单的过滤逻辑：只要有图标的都算
            val appName = app.applicationInfo?.loadLabel(packageManager).toString()
            val icon = app.applicationInfo?.loadIcon(packageManager)
            appList.add(AppInfo(app.packageName, appName, icon))
        }
        return appList.sortedBy { it.appName }
    }

    // ★★★ 优化：获取当前前台应用 (更稳健的写法) ★★★
    fun getForegroundApp(context: Context): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 1000 * 60 * 60 // 查过去1小时

        // 获取一段时间内的使用统计
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            beginTime,
            endTime
        )

        if (stats != null && stats.isNotEmpty()) {
            // 找到“最后一次使用时间 (lastTimeUsed)” 最大的那个应用，就是当前前台应用
            val topApp = stats.maxByOrNull { it.lastTimeUsed }
            return topApp?.packageName
        }
        return null
    }
}