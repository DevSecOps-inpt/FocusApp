package com.example.focuslock.features.settings

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslock.data.local.dao.BlockedAppDao
import com.example.focuslock.data.local.entity.BlockedApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BlockedAppsViewModel @Inject constructor(
    private val blockedAppDao: BlockedAppDao,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    val blockedApps = blockedAppDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val userApps = flow {
        emit(getUserApps())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private suspend fun getUserApps(): List<ApplicationInfo> = withContext(Dispatchers.IO) {
        context.packageManager.getInstalledApplications(0)
            .filter { app ->
                app.enabled && 
                app.flags and ApplicationInfo.FLAG_SYSTEM == 0 &&
                app.packageName != context.packageName // Exclude our own app
            }
            .sortedBy { app ->
                context.packageManager.getApplicationLabel(app).toString()
            }
    }
    
    fun toggleApp(app: ApplicationInfo) {
        viewModelScope.launch {
            val packageName = app.packageName
            val appLabel = context.packageManager.getApplicationLabel(app).toString()
            
            val existingApp = blockedApps.value.find { it.packageName == packageName }
            
            if (existingApp != null) {
                // Remove from blocked list
                blockedAppDao.delete(existingApp)
            } else {
                // Add to blocked list
                val blockedApp = BlockedApp(
                    packageName = packageName,
                    appLabel = appLabel,
                    addedAt = System.currentTimeMillis()
                )
                blockedAppDao.upsert(blockedApp)
            }
        }
    }
    
    fun isAppBlocked(packageName: String): Boolean {
        return blockedApps.value.any { it.packageName == packageName }
    }
}
