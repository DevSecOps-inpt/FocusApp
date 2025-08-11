// app/src/main/java/com/devsecopsinpt/focusapp/data/BlockedAppsStore.kt
package com.devsecopsinpt.focusapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("focus_prefs")
private val BLOCKED = stringSetPreferencesKey("blocked_packages")

object BlockedAppsStore {
    fun blockedFlow(ctx: Context): Flow<Set<String>> =
        ctx.dataStore.data.map { it[BLOCKED] ?: emptySet() }

    suspend fun toggle(ctx: Context, pkg: String) {
        ctx.dataStore.edit { p ->
            val cur = p[BLOCKED] ?: emptySet()
            p[BLOCKED] = if (pkg in cur) cur - pkg else cur + pkg
        }
    }

    suspend fun setAll(ctx: Context, pkgs: Set<String>) {
        ctx.dataStore.edit { it[BLOCKED] = pkgs }
    }
}
