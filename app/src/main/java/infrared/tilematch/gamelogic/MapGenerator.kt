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

package infrared.tilematch.gamelogic

import android.util.Log
import infrared.tilematch.data.Tile

fun availableCharsForDifficulty(difficulty: Int): List<Char> {
    return when (difficulty) {
        0 -> ('a'..'d').toList()
        1 -> ('a'..'f').toList()
        2 -> ('a'..'i').toList()
        3 -> ('a'..'o').toList()
        else -> ('a'..'r').toList()
    }
}

fun ensurePlayabilityAndCorrectGrid(originalGrid: Array<Array<Tile?>>): Array<Array<Tile?>>? {
    val grid = originalGrid.map { it.clone() }.toTypedArray() // copy
    val TAG = "TileCheck"
    if (grid.isEmpty() || grid[0].isEmpty()) return grid
    val rows = grid.size
    val cols = grid[0].size

    fun findWallPath(startRow: Int, startCol: Int): List<Tile> {
        val visited = Array(rows) { BooleanArray(cols) }
        val queue = ArrayDeque<Triple<Int, Int, List<Tile>>>()
        queue.add(Triple(startRow, startCol, emptyList()))
        visited[startRow][startCol] = true
        val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        while (queue.isNotEmpty()) {
            val (r, c, wallsBroken) = queue.removeFirst()
            if (r == rows - 1) return wallsBroken
            for ((dr, dc) in directions) {
                val nr = r + dr
                val nc = c + dc
                if (nr in 0 until rows && nc in 0 until cols && !visited[nr][nc]) {
                    val tile = grid[nr][nc]
                    visited[nr][nc] = true
                    when {
                        tile == null || tile.isRemoved -> queue.addFirst(Triple(nr, nc, wallsBroken))
                        tile.isWall -> queue.add(Triple(nr, nc, wallsBroken + tile))
                        else -> queue.add(Triple(nr, nc, wallsBroken))
                    }
                }
            }
        }
        return emptyList()
    }

    fun hasPathToBottom(startRow: Int, startCol: Int): Boolean {
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
                    val neighbor = grid[nr][nc]
                    if (neighbor == null || !neighbor.isWall) {
                        visited[nr][nc] = true
                        queue.add(nr to nc)
                    }
                }
            }
        }
        return false
    }

    for (row in 0 until rows) {
        for (col in 0 until cols) {
            val tile = grid[row][col]
            if (tile != null && !tile.isRemoved && !tile.isWall) {
                while (true) {
                    if (hasPathToBottom(row, col)) break
                    val wallsOnPath = findWallPath(row, col)
                    val wallToRemove = wallsOnPath.firstOrNull { it.isWall }
                    if (wallToRemove != null) {
                        grid[wallToRemove.row][wallToRemove.col] = null // Modificar a cópia
                    } else {
                        Log.e(TAG, "Unplayable level: Piece at [$row, $col] is permanently blocked.")
                        return null // fail
                    }
                }
            }
        }
    }

    val counts = mutableMapOf<Char, Int>()
    grid.flatten().forEach { tile ->
        if (tile != null && !tile.isRemoved && !tile.isWall) {
            counts[tile.imageChar] = counts.getOrDefault(tile.imageChar, 0) + 1
        }
    }
    if (counts.values.any { it % 3 != 0 }) {
        Log.e(TAG, "Unplayable level: Unbalanced piece count. Counts: $counts")
        return null
    }

    return grid
}

fun generateRandomTileMap(rows: Int, cols: Int, difficulty: Int): List<List<Tile?>> {
    val total = rows * cols
    val emptyPercent = when (difficulty) { 0 -> 0.40f; 1 -> 0.30f; 2 -> 0.20f; 3 -> 0.10f; else -> 0.05f }
    val wallPercent = when (difficulty) { 0 -> 0.10f; 1 -> 0.20f; 2 -> 0.30f; 3 -> 0.35f; else -> 0.40f }
    val allIndices = (0 until total).shuffled().toMutableList()
    val wallCount = (total * wallPercent).toInt()
    val wallIndices = allIndices.take(wallCount).toMutableSet()
    val remainingIndices = allIndices.drop(wallCount).toMutableList()
    val emptyCount = (total * emptyPercent).toInt()
    remainingIndices.take(emptyCount).toSet()
    val availableForTiles = remainingIndices.drop(emptyCount).toMutableList()
    val tileCount = (availableForTiles.size / 3) * 3
    val tileIndices = availableForTiles.take(tileCount).toMutableList()
    val chars = availableCharsForDifficulty(difficulty)
    val charPool = mutableListOf<Char>()
    val availableChars = chars.toMutableList()
    while (charPool.size < tileCount) {
        if (availableChars.isEmpty()) availableChars.addAll(chars)
        val ch = availableChars.removeAt(0)
        repeat(3) { charPool.add(ch) }
    }
    charPool.shuffle()

    val grid = Array(rows) { arrayOfNulls<Tile?>(cols) }

    val positionPairs = tileIndices.map { index -> index / cols to index % cols }.toMutableList()
    val charTrios = charPool.chunked(3)
    for (trio in charTrios) {
        val char = trio.first()
        if (positionPairs.size >= 3) {
            val (a, b, c) = positionPairs.take(3)
            grid[a.first][a.second] = Tile(a.first, a.second, initialImageChar = char)
            grid[b.first][b.second] = Tile(b.first, b.second, initialImageChar = char)
            grid[c.first][c.second] = Tile(c.first, c.second, initialImageChar = char)
            positionPairs.removeAll(listOf(a, b, c))
        }
    }
    for (index in wallIndices) {
        val row = index / cols
        val col = index % cols
        if (grid[row][col] == null) grid[row][col] =
            Tile(row, col, initialImageChar = 'x', isWall = true)
    }

    val correctedGrid = ensurePlayabilityAndCorrectGrid(grid)
    if (correctedGrid == null) {
        Log.e("Generator", "❌ Unplayable level after correction attempt. Regenerating.")
        return generateRandomTileMap(rows, cols, difficulty) // Try again
    }

    return correctedGrid.map { it.toList() }
}