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

import infrared.tilematch.data.DecodedMap
import infrared.tilematch.data.Tile


fun extractLayoutFromMapString(mapCode: String): String {
    val parts = mapCode.split('|')
    if (parts.size < 2) return mapCode // original if invalid

    val width = parts[0]
    val gridData = parts[1]

    val originalGridData = buildString {
        gridData.forEach { char ->
            if (char.isLetter() && char != 'W' && char != 'x') {
                append(char.lowercaseChar())
            } else {
                append(char)
            }
        }
    }
    return "$width|$originalGridData||7"
}

fun encodeGameStateToString(tiles: List<List<Tile?>>, matchArea: List<Tile>, maxMatchAreaSize: Int): String {
    val width = tiles.firstOrNull()?.size ?: 0
    if (width == 0) return "0|||7"

    val charList = tiles.flatten().map { tile ->
        when {
            tile == null -> '.'
            tile.isWall -> 'W'
            tile.isRemoved -> tile.imageChar.uppercaseChar()
            else -> tile.imageChar
        }
    }

    val compressedGrid = StringBuilder()
    var i = 0
    while (i < charList.size) {
        val currentChar = charList[i]
        var count = 1
        var j = i + 1
        while (j < charList.size && charList[j] == currentChar) {
            count++
            j++
        }
        if (count > 1) {
            compressedGrid.append(count)
        }
        compressedGrid.append(currentChar)
        i = j
    }

    val matchAreaString = matchArea.joinToString("") { it.imageChar.toString() }
    return "$width|$compressedGrid|$matchAreaString|$maxMatchAreaSize"
}

fun decodeStringToGameState(mapString: String): DecodedMap {
    val parts = mapString.split('|')
    if (parts.size < 4) return DecodedMap(emptyList(), emptyList(), 7)

    val width = parts[0].toIntOrNull() ?: 0
    val compressedGrid = parts[1]
    val matchAreaString = parts[2]
    val maxMatchAreaSize = parts[3].toIntOrNull() ?: 7

    val decompressedGrid = StringBuilder()
    var i = 0
    while (i < compressedGrid.length) {
        var numStr = ""
        while (i < compressedGrid.length && compressedGrid[i].isDigit()) {
            numStr += compressedGrid[i]
            i++
        }
        val count = numStr.toIntOrNull() ?: 1
        val char = compressedGrid.getOrNull(i)
        if (char != null) {
            repeat(count) {
                decompressedGrid.append(char)
            }
        }
        i++
    }

    if (width == 0 || decompressedGrid.isEmpty()) {
        return DecodedMap(emptyList(), matchAreaString.map { it }, maxMatchAreaSize)
    }

    val tiles = decompressedGrid.chunked(width).mapIndexed { rowIndex, rowString ->
        rowString.mapIndexed { colIndex, char ->
            when {
                char == '.' -> null
                char == 'W' -> Tile(
                    row = rowIndex,
                    col = colIndex,
                    initialImageChar = 'x',
                    isWall = true
                )
                char.isUpperCase() -> {
                    val tile = Tile(
                        row = rowIndex,
                        col = colIndex,
                        initialImageChar = char.lowercaseChar()
                    )
                    tile.isRemoved = true
                    tile
                }
                else -> Tile(row = rowIndex, col = colIndex, initialImageChar = char)
            }
        }
    }

    val matchAreaChars = matchAreaString.map { it }

    return DecodedMap(tiles, matchAreaChars, maxMatchAreaSize)
}