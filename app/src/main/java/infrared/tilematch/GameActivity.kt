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

package infrared.tilematch

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import infrared.tilematch.constants.GameConstants
import infrared.tilematch.data.GameState
import infrared.tilematch.gamelogic.encodeGameStateToString
import infrared.tilematch.managers.SettingsManager
import infrared.tilematch.ui.GameScreen
import infrared.tilematch.ui.theme.TileMatchTheme

class GameActivity : ComponentActivity() {
    private var gameState: GameState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val difficulty = intent.getIntExtra("difficulty", -1)

        val mapStringFromIntent = intent.getStringExtra("mapString")

        val sharedPrefs = getSharedPreferences(GameConstants.PREFS_NAME, MODE_PRIVATE)

        val savedMap = if (difficulty != -1) sharedPrefs.getString(GameConstants.mapKey(difficulty), null) else null
        val savedShuffleUses = if (difficulty != -1) sharedPrefs.getInt(GameConstants.shuffleUsesKey(difficulty), 1) else 1
        val savedRevealMapUsed = if (difficulty != -1) sharedPrefs.getBoolean(GameConstants.revealMapUsedKey(difficulty), false) else false

        val isNightMode = SettingsManager.isNightModeEnabled(this)
        val areSymbolsEnabled = SettingsManager.isSymbolsModeEnabled(this)

        setContent {
            TileMatchTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameScreen(
                        difficulty = difficulty,
                        mapString = mapStringFromIntent ?: savedMap,
                        isNightMode = isNightMode,
                        areSymbolsEnabled = areSymbolsEnabled,
                        onGameStateCreated = { newGameState ->
                            this.gameState = newGameState
                        },
                        onGoToMenu = { this.finish() },
                        initialShuffleUses = savedShuffleUses,
                        initialRevealMapUsed = savedRevealMapUsed,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        gameState?.let { state ->
            if (state.difficulty == -1) return

            val editor = getSharedPreferences(GameConstants.PREFS_NAME, MODE_PRIVATE).edit()

            if (state.isGameWon || state.isGameOver) {
                Log.d("GameSave", "Game finished. Clearing data for difficulty ${state.difficulty}.")
                editor.remove(GameConstants.mapKey(state.difficulty))
                editor.remove(GameConstants.shuffleUsesKey(state.difficulty))
                editor.remove(GameConstants.revealMapUsedKey(state.difficulty))
            } else {
                Log.d("GameSave", "Game paused. Saving state for difficulty ${state.difficulty}.")
                val mapToSave =
                    encodeGameStateToString(state.tiles, state.matchArea, state.maxMatchAreaSize)
                editor.putString(GameConstants.mapKey(state.difficulty), mapToSave)
                editor.putInt(GameConstants.shuffleUsesKey(state.difficulty), state.shuffleUsesRemaining)
                editor.putBoolean(GameConstants.revealMapUsedKey(state.difficulty), state.hasUsedRevealMap && !state.isMapRevealed)
            }
            editor.apply()
        }
    }
}
