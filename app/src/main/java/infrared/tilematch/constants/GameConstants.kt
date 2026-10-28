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

package infrared.tilematch.constants

object GameConstants {
    const val PREFS_NAME = "TileMatchSaves"
    const val PROGRESS_PREFS_NAME = "GameProgress"

    fun mapKey(difficulty: Int) = "map_difficulty_$difficulty"
    fun shuffleUsesKey(difficulty: Int) = "shuffle_uses_difficulty_$difficulty"
    fun revealMapUsedKey(difficulty: Int) = "reveal_map_used_difficulty_$difficulty"
    fun winsKey(difficulty: Int) = "wins_difficulty_$difficulty"
    const val PREFS_FIRST_TIME_LAUNCH = "first_time_launch"
}