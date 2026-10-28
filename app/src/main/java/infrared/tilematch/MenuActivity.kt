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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import infrared.tilematch.managers.SettingsManager
import infrared.tilematch.managers.SoundManager
import infrared.tilematch.ui.BackgroundImage
import infrared.tilematch.ui.MenuScreen
import infrared.tilematch.ui.theme.TileMatchTheme

class MenuActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundManager.initialize(this)

        enableEdgeToEdge()
        setContent {

            TileMatchTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var refreshTrigger by remember { mutableStateOf(0) }
                    val context = LocalContext.current
                    val isNightMode by remember(refreshTrigger) {
                        mutableStateOf(SettingsManager.isNightModeEnabled(context))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        BackgroundImage(isNightMode = isNightMode)
                        MenuScreen(
                            onRefresh = { refreshTrigger++ },
                            isNightMode = isNightMode,
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.release()
    }
}
