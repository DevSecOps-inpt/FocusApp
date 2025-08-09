package com.example.focuslock.core.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppInfo(
    val packageName: String,
    val appLabel: String,
    val isSystemApp: Boolean,
    val isEnabled: Boolean
)

suspend fun PackageManager.getInstalledUserApps(): List<AppInfo> = withContext(Dispatchers.IO) {
    try {
        getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { appInfo ->
                appInfo.enabled && (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0)
            }
            .map { appInfo ->
                AppInfo(
                    packageName = appInfo.packageName,
                    appLabel = getApplicationLabel(appInfo).toString(),
                    isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    isEnabled = appInfo.enabled
                )
            }
            .sortedBy { it.appLabel }
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun PackageManager.getAppLabel(packageName: String): String = withContext(Dispatchers.IO) {
    try {
        val appInfo = getApplicationInfo(packageName, 0)
        getApplicationLabel(appInfo).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        packageName
    }
} 