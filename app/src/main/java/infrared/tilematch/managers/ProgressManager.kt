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
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import infrared.tilematch.R
import infrared.tilematch.constants.GameConstants

object ProgressManager {
    fun getWins(context: Context, difficulty: Int): Int {
        val prefs = context.getSharedPreferences(GameConstants.PROGRESS_PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(GameConstants.winsKey(difficulty), 0)
    }

    fun incrementWins(context: Context, difficulty: Int) {
        val prefs = context.getSharedPreferences(GameConstants.PROGRESS_PREFS_NAME, Context.MODE_PRIVATE)
        val currentWins = getWins(context, difficulty)
        val newWins = currentWins + 1

        with(prefs.edit()) {
            putInt(GameConstants.winsKey(difficulty), newWins)
            apply()
        }
        Log.d("ProgressManager", "Difficulty $difficulty wins incremented to $newWins")
    }

    fun resetProgress(context: Context) {
        val prefs = context.getSharedPreferences(GameConstants.PROGRESS_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun isDifficultyUnlocked(context: Context, difficulty: Int): Boolean {
        return when (difficulty) {
            0, 1 -> true // Kids Mode e Easy always unlocked
            2 -> getWins(context, 1) >= 15 // Normal requires 15 wins in Easy
            3 -> getWins(context, 2) >= 30 // Hard requires 30 wins in Normal
            4 -> getWins(context, 3) >= 50 // Insane requires 50 wins in Hard
            else -> true // By default, unlocked
        }
    }

    @Composable
    fun getUnlockRequirementText(difficulty: Int): String {
        return when (difficulty) {
            2 -> stringResource(R.string.menu_buttonlevels_requirement_for_normal)
            3 -> stringResource(R.string.menu_buttonlevels_requirement_for_hard)
            4 -> stringResource(R.string.menu_buttonlevels_requirement_for_insane)
            else -> ""
        }
    }
}