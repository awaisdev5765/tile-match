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

package infrared.tilematch.data

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import infrared.tilematch.managers.ProgressManager
import infrared.tilematch.managers.SoundManager
import infrared.tilematch.utils.playTileSound
import kotlinx.coroutines.delay
import androidx.compose.ui.geometry.Offset
import infrared.tilematch.ui.GameLayout

data class FlyingTile(
    val imageChar: Char,
    val startOffset: Offset,
    val endOffset: Offset,
    val animOffset: Animatable<Offset, AnimationVector2D>
)


data class Tile(
    val row: Int,
    val col: Int,
    var initialImageChar: Char,
    var isWall: Boolean = false
) {
    var imageChar by mutableStateOf(initialImageChar)

    var isVisible by mutableStateOf(false)
    var isRemoved by mutableStateOf(false)
}

data class DecodedMap(
    val tiles: List<List<Tile?>>,
    val matchAreaChars: List<Char>,
    val maxMatchAreaSize: Int
)

class GameState(
    val difficulty: Int,
    val tiles: List<List<Tile?>>,
    val context: Context,
    restoredMatchAreaChars: List<Char> = emptyList(),
    initialMaxMatchAreaSize: Int = 7,
    initialShuffleUses: Int = 1,
    initialRevealMapUsed: Boolean = false
) {
    val rows: Int = tiles.size
    val cols: Int = tiles.firstOrNull()?.size ?: 0
    val matchArea = mutableStateListOf<Tile>()
    var isGameOver by mutableStateOf(false)
    var isGameWon by mutableStateOf(false)

    var maxMatchAreaSize by mutableStateOf(initialMaxMatchAreaSize)
    var hasUsedPlusOneSlot by mutableStateOf(initialMaxMatchAreaSize > 7)
    var shuffleUsesRemaining by mutableStateOf(initialShuffleUses)
    var hasUsedRevealMap by mutableStateOf(initialRevealMapUsed)
    var isMapRevealed by mutableStateOf(false)

    var revealTimeRemaining by mutableStateOf(0)

    val flyingTiles = mutableStateListOf<FlyingTile>()

    init {
        if (restoredMatchAreaChars.isNotEmpty()) {
            val availableTiles = tiles.flatten().filterNotNull().filter { it.isRemoved }.toMutableList()
            restoredMatchAreaChars.forEach { charToFind ->
                val tile = availableTiles.find { it.imageChar == charToFind }
                if (tile != null) {
                    matchArea.add(tile)
                    availableTiles.remove(tile)
                }
            }
        }
        updateVisibilityFromBottom()
        checkWinCondition()
        if (matchArea.size >= maxMatchAreaSize) {
            isGameOver = true
        }
    }

    fun calculateTargetIndexForChar(char: Char): Int {
        val grouped = matchArea.groupBy { it.imageChar }
            .entries
            .sortedBy { entry -> matchArea.indexOfFirst { it.imageChar == entry.key } }

        var index = 0
        grouped.forEach { (c, group) ->
            if (c == char) {
                return index + group.size
            }
            index += group.size
        }
        return matchArea.size
    }

    // GameState.kt
    fun willCauseGameOverWithCharIncludingFlights(imageChar: Char): Boolean {
        val futureSize = matchArea.size + flyingTiles.size + 1
        return futureSize > maxMatchAreaSize
    }

    // GameState.kt
    suspend fun onTileClick(row: Int, col: Int, layout: GameLayout) {
        val tile = tiles[row][col] ?: return
        if (isGameOver || isGameWon || !tile.isVisible || tile in matchArea) return

        val pending = flyingTiles.toList()
        for (f in pending) {
            f.animOffset.snapTo(f.endOffset)
            onFlyingTileArrived(f)
        }

        if (willCauseGameOverWithCharIncludingFlights(tile.imageChar)) {
            return
        }

        tile.isRemoved = true

        val targetIndex = calculateTargetIndexForChar(tile.imageChar)
        val start = layout.tilePositions[row][col]
        val end = Offset(
            x = layout.startX.value + layout.blockSize.value * targetIndex,
            y = layout.startY.value + layout.blockSize.value * rows
        )

        val anim = Animatable(start, Offset.VectorConverter)
        val flying = FlyingTile(tile.imageChar, start, end, anim)
        flyingTiles.add(flying)

        anim.animateTo(end, tween(250))
        onFlyingTileArrived(flying)
    }



    fun onFlyingTileArrived(flying: FlyingTile) {
        matchArea.add(
            calculateTargetIndexForChar(flying.imageChar),
            Tile(row = -1, col = -1, initialImageChar = flying.imageChar).apply {
                isRemoved = true
            }
        )

        val sameKind = matchArea.filter { it.imageChar == flying.imageChar }
        playTileSound(context, sameKind.size)   // 👈 toca sempre

        if (sameKind.size == 3) {
            matchArea.removeAll(sameKind)
        }

        if (matchArea.size >= maxMatchAreaSize) {
            isGameOver = true
        }

        flyingTiles.remove(flying)
        updateVisibilityFromBottom()
        checkWinCondition()
    }


    suspend fun shuffleWithAnimation() {
        if (shuffleUsesRemaining <= 0) return

        SoundManager.playShuffleSound(context)

        delay(100) // Pause

        // shuffle animation
        val playableTiles = tiles.flatten().filterNotNull().filter { !it.isRemoved && !it.isWall }
        if (playableTiles.isEmpty()) return

        val originalChars = playableTiles.map { it.imageChar }

        val onBoardChars = originalChars.distinct()
        if (onBoardChars.isEmpty()) {
            shuffleUsesRemaining--
            updateVisibilityFromBottom()
            return
        }

        repeat(5) { // blink 5 times
            playableTiles.forEach { tile ->
                tile.imageChar = onBoardChars.random()
            }
            delay(80)
        }

        val shuffledChars = originalChars.shuffled()
        playableTiles.forEachIndexed { index, tile ->
            tile.imageChar = shuffledChars[index]
        }

        shuffleUsesRemaining--
        updateVisibilityFromBottom()
    }

    suspend fun revealMapTemporarily() {
        if (hasUsedRevealMap || isMapRevealed) return

        SoundManager.playRevealSound(context)

        hasUsedRevealMap = true
        isMapRevealed = true
        updateVisibilityBasedOnReveal()

        for (i in 10 downTo 1) {
            revealTimeRemaining = i
            delay(1000)
        }

        isMapRevealed = false
        revealTimeRemaining = 0
        updateVisibilityFromBottom()
    }

    fun grantPlusOneSlotReward() {
        if (hasUsedPlusOneSlot) return
        SoundManager.playExtraSlotSound(context)
        maxMatchAreaSize = 8
        hasUsedPlusOneSlot = true
    }

    private fun checkWinCondition() {
        if (isGameWon || isGameOver) return
        val nonWallTiles = tiles.flatten().filterNotNull().filter { !it.isWall }
        if (nonWallTiles.isNotEmpty() && nonWallTiles.all { it.isRemoved }) {
            isGameWon = true
            if (difficulty != -1 && difficulty != 0) {
                ProgressManager.incrementWins(context, difficulty)
            }
        }
    }

    private fun updateVisibilityFromBottom() {
        if (isMapRevealed) {
            updateVisibilityBasedOnReveal()
            return
        }

        tiles.flatten().filterNotNull().forEach { it.isVisible = false }
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val tile = tiles[row][col]
                if (tile != null && !tile.isRemoved) {
                    tile.isVisible = hasPathThroughEmptyToBottom(row, col)
                }
            }
        }
    }

    private fun updateVisibilityBasedOnReveal() {
        tiles.flatten().filterNotNull().forEach {
            if (!it.isRemoved && !it.isWall) {
                it.isVisible = true
            }
        }
    }

    private fun hasPathThroughEmptyToBottom(startRow: Int, startCol: Int): Boolean {
        val visited = Array(rows) { BooleanArray(cols) }
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(startRow to startCol)
        visited[startRow][startCol] = true
        val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        while (queue.isNotEmpty()) {
            val (r, c) = queue.removeFirst()
            if (r == rows - 1) return true
            for ((dr, dc) in directions) {
                val nr = r + dr
                val nc = c + dc
                if (nr in 0 until rows && nc in 0 until cols && !visited[nr][nc]) {
                    val neighbor = tiles.getOrNull(nr)?.getOrNull(nc)
                    if ((nr == startRow && nc == startCol) || neighbor == null || neighbor.isRemoved) {
                        visited[nr][nc] = true
                        queue.add(nr to nc)
                    }
                }
            }
        }
        return false
    }
}