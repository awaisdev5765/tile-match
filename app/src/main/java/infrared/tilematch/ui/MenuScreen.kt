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

package infrared.tilematch.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import infrared.tilematch.GameActivity
import infrared.tilematch.R
import infrared.tilematch.managers.ProgressManager

@Composable
fun MenuScreen(
    onRefresh: () -> Unit,
    isNightMode: Boolean,
) {
    var showMainMenu by remember { mutableStateOf(true) }
    var showReadMapDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showAboutMenu by remember { mutableStateOf(false) }

    val buttonFontSize = LocalTextStyle.current.fontSize
    val versionFontSize = buttonFontSize * 0.7f

    var refreshTrigger by remember { mutableStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val context = LocalContext.current

    val scope = rememberCoroutineScope()


    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    BackHandler(enabled = !showMainMenu || showReadMapDialog || showSettingsMenu || showAboutMenu) {
        when {
            showReadMapDialog -> showReadMapDialog = false
            showSettingsMenu -> showSettingsMenu = false
            showAboutMenu -> showAboutMenu = false
            else -> showMainMenu = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val isAnyMenuOpen = showSettingsMenu || showAboutMenu || showReadMapDialog
        AnimatedVisibility(visible = !isAnyMenuOpen) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedVisibility(
                    visible = showMainMenu,
                    enter = fadeIn(),
                    exit = slideOutHorizontally { -it } + fadeOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        val logoColorFilter = if (isNightMode) {
                            val brightness = 0.7f
                            val colorMatrix = ColorMatrix(
                                floatArrayOf(
                                    brightness, 0f, 0f, 0f, 0f,
                                    0f, brightness, 0f, 0f, 0f,
                                    0f, 0f, brightness, 0f, 0f,
                                    0f, 0f, 0f, 1f, 0f
                                )
                            )
                            ColorFilter.colorMatrix(colorMatrix)
                        } else {
                            null
                        }

                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Game Logo",
                            modifier = Modifier.size(90.dp),
                            colorFilter = logoColorFilter
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        val titleColor = if (isNightMode) Color.LightGray else Color.Black
                        Column {
                            Text(text = "Tile", fontSize = 44.sp, color = titleColor, fontWeight = FontWeight.Bold)
                            Text(text = "Match", fontSize = 44.sp, color = titleColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showMainMenu,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StyledMenuButtons(
                            onPlayClick = { showMainMenu = false },
                            onSettingsClick = { showSettingsMenu = true },
                            onAboutClick = { showAboutMenu = true },
                            isNightMode = isNightMode
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !showMainMenu,
                    enter = slideInHorizontally { it } + fadeIn(),
                    exit = slideOutHorizontally { it } + fadeOut()
                ) {
                    DifficultySelectionScreen(
                        onBackToMenu = { showMainMenu = true },
                        refreshTrigger = refreshTrigger,
                        onReadMapClick = { showReadMapDialog = true },
                        isNightMode = isNightMode
                    )
                }
            }
        }

        AnimatedVisibility(visible = showReadMapDialog, enter = fadeIn(), exit = fadeOut()) {
            ReadMapDialog(
                onDismiss = { showReadMapDialog = false },
                onPlay = { mapCode ->
                    val intent = Intent(context, GameActivity::class.java).apply {
                        putExtra("mapString", mapCode)
                        putExtra("difficulty", -1)
                    }
                    context.startActivity(intent)
                    showReadMapDialog = false
                },
                isNightMode = isNightMode
            )
        }

        AnimatedVisibility(visible = showSettingsMenu, enter = fadeIn(), exit = fadeOut()) {
            SettingsMenu(
                onDismiss = { showSettingsMenu = false },
                onSettingsChanged = {
                    refreshTrigger++
                    onRefresh()
                }
            )
        }

        AnimatedVisibility(visible = showAboutMenu, enter = fadeIn(), exit = fadeOut()) {
            AboutMenu(onDismiss = { showAboutMenu = false }, isNightMode = isNightMode)
        }




        if (!isAnyMenuOpen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.app_version),
                    fontSize = versionFontSize,
                    color = if (isNightMode) Color.LightGray else Color.Black
                )
            }
        }
    }
}
