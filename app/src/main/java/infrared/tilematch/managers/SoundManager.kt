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
import android.media.AudioAttributes
import android.media.SoundPool
import infrared.tilematch.R

object SoundManager {
    private var soundPool: SoundPool? = null
    private var isInitialized = false
    private var clickSoundId: Int = 0
    private var winSoundId: Int = 0
    private var loseSoundId: Int = 0
    private var tickSoundId: Int = 0
    private var shuffleSoundId: Int = 0
    private var extraSlotSoundId: Int = 0
    private var revealSoundId: Int = 0
    private var popupSound: Int = 0

    fun initialize(context: Context) {
        if (isInitialized) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()

        clickSoundId = soundPool?.load(context, R.raw.click, 1) ?: 0
        winSoundId = soundPool?.load(context, R.raw.won, 1) ?: 0
        loseSoundId = soundPool?.load(context, R.raw.lose, 1) ?: 0
        tickSoundId = soundPool?.load(context, R.raw.tick, 1) ?: 0
        shuffleSoundId = soundPool?.load(context, R.raw.shuffle, 1) ?: 0
        extraSlotSoundId = soundPool?.load(context, R.raw.extra_slot, 1) ?: 0
        revealSoundId = soundPool?.load(context, R.raw.reveal, 1) ?: 0
        popupSound = soundPool?.load(context, R.raw.click_tile1, 1) ?: 0
        isInitialized = true
    }

    fun playClickSound(context: Context) {
        if (!isInitialized || !SettingsManager.isSoundEnabled(context)) return

        soundPool?.play(clickSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }
    fun playWinSound(context: Context) {
        if (!isInitialized || !SettingsManager.isSoundEnabled(context)) return
        soundPool?.play(winSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }
    fun playLoseSound(context: Context) {
        if (!isInitialized || !SettingsManager.isSoundEnabled(context)) return
        soundPool?.play(loseSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }
    fun playTickSound(context: Context) {
        if (!isInitialized || !SettingsManager.isSoundEnabled(context)) return
        soundPool?.play(tickSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }
    fun playShuffleSound(context: Context) {
        if (!isInitialized || !SettingsManager.isSoundEnabled(context)) return
        soundPool?.play(shuffleSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }
    fun playExtraSlotSound(context: Context) {
        if (!isInitialized || !SettingsManager.isSoundEnabled(context)) return
        soundPool?.play(extraSlotSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }
    fun playRevealSound(context: Context) {
        if (!isInitialized || !SettingsManager.isSoundEnabled(context)) return
        soundPool?.play(revealSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }
    fun playTilePopup(context: Context) {
        soundPool?.play(popupSound, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        isInitialized = false
    }
}