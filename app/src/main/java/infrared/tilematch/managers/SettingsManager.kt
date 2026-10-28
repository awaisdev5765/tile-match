/*
 * Copyright (C) 2026  InfraRed
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see:
 * https://www.gnu.org/licenses/
 */

package infrared.tilematch.managers

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object SettingsManager {
    private const val PREFS_NAME = "TileMatchSettings"
    private const val KEY_SOUND_ENABLED = "sound_enabled"
    private const val KEY_NIGHT_MODE_ENABLED = "night_mode_enabled"
    private const val KEY_SYMBOLS_MODE_ENABLED = "symbols_mode_enabled"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Sound Settings ---
    fun isSoundEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    // --- Night Mode Settings ---
    fun isNightModeEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_NIGHT_MODE_ENABLED, false)
    }

    fun setNightModeEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_NIGHT_MODE_ENABLED, enabled).apply()
    }

    // --- Symbols Mode Settings ---
    fun isSymbolsModeEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_SYMBOLS_MODE_ENABLED, false)
    }

    fun setSymbolsModeEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_SYMBOLS_MODE_ENABLED, enabled).apply()
    }

    fun resetGame(context: Context) {
        ProgressManager.resetProgress(context)

        val savedGamesPrefs = context.getSharedPreferences("TileMatchSaves", Context.MODE_PRIVATE)
        savedGamesPrefs.edit().clear().apply()
    }
}