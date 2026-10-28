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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import infrared.tilematch.GameActivity
import infrared.tilematch.managers.ProgressManager
import infrared.tilematch.managers.SettingsManager
import infrared.tilematch.managers.SoundManager
import infrared.tilematch.utils.BaseMenu
import infrared.tilematch.utils.playMenuClickSound
import infrared.tilematch.R

@Composable
private fun getMainMenuButtonColors(isNightMode: Boolean): ButtonColors {
    return if (isNightMode) {
        ButtonDefaults.buttonColors(
            containerColor = Color.DarkGray,
            contentColor = Color(0xFFE0E0E0)
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    }
}

@Composable
private fun getDestructiveButtonColors(isNightMode: Boolean): ButtonColors {
    return if (isNightMode) {
        ButtonDefaults.buttonColors(
            containerColor = Color(0xFF9E2B25),
            contentColor = Color.White
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF44336),
            contentColor = Color.White
        )
    }
}

@Composable
private fun getCheckboxColors(isNightMode: Boolean): CheckboxColors {
    return if (isNightMode) {
        CheckboxDefaults.colors(
            checkedColor = Color(0xFF00897B),
            uncheckedColor = Color.Gray,
            checkmarkColor = Color.White
        )
    } else {
        CheckboxDefaults.colors(
            checkedColor = Color(0xFF50E3C2),
            uncheckedColor = Color.Gray,
            checkmarkColor = Color.Black
        )
    }
}

@Composable
fun BackgroundImage(isNightMode: Boolean) {
    val painter = painterResource(
        id = if (isNightMode) R.drawable.game_bg_night else R.drawable.game_bg
    )
    Image(
        painter = painter,
        contentDescription = "Menu screen background",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun StyledMenuButtons(onPlayClick: () -> Unit, onSettingsClick: () -> Unit, onAboutClick: () -> Unit, isNightMode: Boolean) {
    val context = LocalContext.current
    val roundedCornerShape = RoundedCornerShape(8.dp)
    val spacing = 16.dp
    val desiredButtonHeight = 60.dp
    val totalHeightForRightColumn = (desiredButtonHeight * 2) + spacing
    val buttonColors = getMainMenuButtonColors(isNightMode = isNightMode)
    val buttonGroupWidth = 360.dp

    Row(
        modifier = Modifier
            .width(buttonGroupWidth)
            .height(totalHeightForRightColumn)
            .padding(horizontal = spacing),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play btn
        Button(
            onClick = { playMenuClickSound(context); onPlayClick() },
            colors = buttonColors,
            modifier = Modifier.fillMaxHeight().aspectRatio(1f).weight(1f, fill = false),
            border = BorderStroke(1.dp, Color.DarkGray),
            shape = roundedCornerShape
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", modifier = Modifier.size(48.dp))
                Text(stringResource(R.string.menu_play_button), fontSize = 28.sp)
            }
        }

        Spacer(modifier = Modifier.width(spacing))

        // btns Settings and About
        Column(
            modifier = Modifier.fillMaxHeight().weight(1f, fill = false),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { playMenuClickSound(context); onSettingsClick() },
                colors = buttonColors,
                modifier = Modifier.fillMaxWidth().height(desiredButtonHeight),
                border = BorderStroke(1.dp, Color.DarkGray),
                shape = roundedCornerShape
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.menu_settings_button))
            }

            Spacer(modifier = Modifier.height(spacing))

            Button(
                onClick = { playMenuClickSound(context); onAboutClick() },
                colors = buttonColors,
                modifier = Modifier.fillMaxWidth().height(desiredButtonHeight),
                border = BorderStroke(1.dp, Color.DarkGray),
                shape = roundedCornerShape
            ) {
                Icon(Icons.Filled.Info, contentDescription = "About")
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.menu_about_button))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMenu(onDismiss: () -> Unit, onSettingsChanged: () -> Unit) {
    val context = LocalContext.current
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(SettingsManager.isSoundEnabled(context)) }
    var nightModeEnabled by remember { mutableStateOf(SettingsManager.isNightModeEnabled(context)) }
    var symbolsModeEnabled by remember { mutableStateOf(SettingsManager.isSymbolsModeEnabled(context)) }

    val destructiveButtonColors = getDestructiveButtonColors(isNightMode = nightModeEnabled)
    val normalButtonColors = getMainMenuButtonColors(isNightMode = nightModeEnabled)
    val checkboxColors = getCheckboxColors(isNightMode = nightModeEnabled)
    val textColor = if (nightModeEnabled) Color(0xFFE0E0E0) else Color.White

    BaseMenu(title = stringResource(R.string.settings_title), isNightMode = nightModeEnabled) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val onSoundToggle = {
                val newState = !soundEnabled
                SettingsManager.setSoundEnabled(context, newState)
                if (newState) {
                    SoundManager.playTickSound(context)
                }
                soundEnabled = newState
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onSoundToggle() }
            ) {
                Checkbox(
                    checked = soundEnabled,
                    onCheckedChange = { onSoundToggle() },
                    colors = checkboxColors
                )
                Text(stringResource(R.string.settings_sound), color = textColor)
            }

            val onNightModeToggle = {
                SoundManager.playTickSound(context)
                nightModeEnabled = !nightModeEnabled
                SettingsManager.setNightModeEnabled(context, nightModeEnabled)
                onSettingsChanged()
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onNightModeToggle() }
            ) {
                Checkbox(
                    checked = nightModeEnabled,
                    onCheckedChange = { onNightModeToggle() },
                    colors = checkboxColors
                )
                Text(stringResource(R.string.settings_night_mode), color = textColor)
            }

            val onSymbolsToggle = {
                SoundManager.playTickSound(context)
                symbolsModeEnabled = !symbolsModeEnabled
                SettingsManager.setSymbolsModeEnabled(context, symbolsModeEnabled)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onSymbolsToggle() }
            ) {
                Checkbox(
                    checked = symbolsModeEnabled,
                    onCheckedChange = { onSymbolsToggle() },
                    colors = checkboxColors
                )
                Text(stringResource(R.string.settings_accessibility), color = textColor)
            }
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { playMenuClickSound(context); showResetConfirmDialog = true },
                colors = destructiveButtonColors,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.settings_reset_button))
            }
            Button(
                onClick = { playMenuClickSound(context); onDismiss() },
                colors = normalButtonColors,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.settings_close_button))
            }
        }
    }


    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text(stringResource(R.string.settings_alertdialog_title)) },
            text = { Text(stringResource(R.string.settings_alertdialog_text)) },
            confirmButton = {
                Button(
                    onClick = {
                        playMenuClickSound(context)
                        SettingsManager.resetGame(context)
                        onSettingsChanged()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)) // Tom de erro
                ) {
                    Text(stringResource(R.string.settings_alertdialog_reset))
                }
            },
            dismissButton = {
                Button(onClick = { playMenuClickSound(context); showResetConfirmDialog = false }) {
                    Text(stringResource(R.string.settings_alertdialog_cancel))
                }
            }
        )
    }

}

@Composable
private fun HowToPlaySection(isNightMode: Boolean) {
    val textColor = if (isNightMode) Color(0xFFE0E0E0) else Color.White
    val secondaryTextColor = Color.LightGray
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.about_tutorial_title), fontSize = 20.sp, color = textColor, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(stringResource(R.string.about_tutorial_descrition), color = secondaryTextColor)
        Text(stringResource(R.string.about_tutorial_point1), color = textColor)
        Text(stringResource(R.string.about_tutorial_point2), color = textColor)
        Text(stringResource(R.string.about_tutorial_point3), color = textColor)
        Text(stringResource(R.string.about_tutorial_point4), color = textColor)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.about_tutorial_help_title), fontSize = 20.sp, color = textColor, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(stringResource(R.string.about_tutorial_help_descrition), color = secondaryTextColor)
        Text(stringResource(R.string.about_tutorial_help_point1), color = textColor)
        Text(stringResource(R.string.about_tutorial_help_point2), color = textColor)
        Text(stringResource(R.string.about_tutorial_help_point3), color = textColor)
    }
}




@Composable
fun AboutMenu(onDismiss: () -> Unit, isNightMode: Boolean) {
    val uriHandler = LocalUriHandler.current
    val buttonColors = getMainMenuButtonColors(isNightMode = isNightMode)
    val textColor = if (isNightMode) Color(0xFFE0E0E0) else Color.White

    BaseMenu(title = stringResource(R.string.about_title), isNightMode = isNightMode) {
        Column(
            modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.about_author),
                    fontSize = 14.sp,
                    color = Color.LightGray
                )
            }
            Text(
                stringResource(R.string.about_dedicatory),
                fontSize = 16.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.about_descrition),
                fontSize = 16.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.about_credits),
                    fontSize = 14.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Kenney",
                    fontSize = 16.sp,
                    color = Color(0xFF50E3C2),
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { uriHandler.openUri("https://kenney.nl") }
                        .padding(vertical = 4.dp)
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = Color.Gray
            )
            HowToPlaySection(isNightMode = isNightMode)
        }
        Spacer(Modifier.height(24.dp))
        val context = LocalContext.current
        Button(
            onClick = { playMenuClickSound(context); onDismiss() },
            colors = buttonColors,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.about_close_button))
        }
    }
}

@Composable
fun DifficultySelectionScreen(
    onBackToMenu: () -> Unit,
    refreshTrigger: Int,
    onReadMapClick: () -> Unit,
    isNightMode: Boolean
) {
    val roundedCornerShape = RoundedCornerShape(8.dp)
    val context = LocalContext.current
    val spacing = 16.dp
    val buttonWidth = 120.dp
    val buttonHeight = 120.dp

    val easyWins = remember(refreshTrigger) { ProgressManager.getWins(context, 1) }
    val normalWins = remember(refreshTrigger) { ProgressManager.getWins(context, 2) }
    val hardWins = remember(refreshTrigger) { ProgressManager.getWins(context, 3) }
    val insaneWins = remember(refreshTrigger) { ProgressManager.getWins(context, 4) }
    val isNormalUnlocked = remember(refreshTrigger) { ProgressManager.isDifficultyUnlocked(context, 2) }
    val isHardUnlocked = remember(refreshTrigger) { ProgressManager.isDifficultyUnlocked(context, 3) }
    val isInsaneUnlocked = remember(refreshTrigger) { ProgressManager.isDifficultyUnlocked(context, 4) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight()
    ) {
        Text(
            text = stringResource(R.string.menu_difficulty_title),
            fontSize = 32.sp,
            color = if (isNightMode) Color(0xFFE0E0E0) else Color.Black,
            modifier = Modifier.padding(bottom = spacing)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                DifficultyButton(
                    text = stringResource(R.string.menu_easy_button),
                    isNightMode = isNightMode,
                    currentProgress = easyWins,
                    onClick = {
                        val intent = Intent(context, GameActivity::class.java)
                        intent.putExtra("difficulty", 1)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(buttonWidth, buttonHeight),
                    roundedCornerShape = roundedCornerShape
                )
                DifficultyButton(
                    text = stringResource(R.string.menu_hard_button),
                    isNightMode = isNightMode,
                    currentProgress = hardWins,
                    onClick = {
                        val intent = Intent(context, GameActivity::class.java)
                        intent.putExtra("difficulty", 3)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(buttonWidth, buttonHeight),
                    roundedCornerShape = roundedCornerShape,
                    isLocked = !isHardUnlocked,
                    unlockText = ProgressManager.getUnlockRequirementText(3)
                )
            }
            Spacer(modifier = Modifier.width(spacing))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                DifficultyButton(
                    text = stringResource(R.string.menu_normal_button),
                    isNightMode = isNightMode,
                    currentProgress = normalWins,
                    onClick = {
                        val intent = Intent(context, GameActivity::class.java)
                        intent.putExtra("difficulty", 2)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(buttonWidth, buttonHeight),
                    roundedCornerShape = roundedCornerShape,
                    isLocked = !isNormalUnlocked,
                    unlockText = ProgressManager.getUnlockRequirementText(2)
                )
                DifficultyButton(
                    text = stringResource(R.string.menu_insane_button),
                    isNightMode = isNightMode,
                    currentProgress = insaneWins,
                    onClick = {
                        val intent = Intent(context, GameActivity::class.java)
                        intent.putExtra("difficulty", 4)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(buttonWidth, buttonHeight),
                    roundedCornerShape = roundedCornerShape,
                    isLocked = !isInsaneUnlocked,
                    unlockText = ProgressManager.getUnlockRequirementText(4)
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing))

        DifficultyButton(
            text = stringResource(R.string.menu_kids_button),
            isNightMode = isNightMode,
            onClick = {
                val intent = Intent(context, GameActivity::class.java)
                intent.putExtra("difficulty", 0)
                context.startActivity(intent)
            },
            modifier = Modifier
                .width(buttonWidth * 2 + spacing)
                .height(buttonHeight),
            roundedCornerShape = roundedCornerShape,
            subtitle = stringResource(R.string.menu_kids_button_description)
        )
        Spacer(modifier = Modifier.height(spacing))

        Button(
            onClick = { playMenuClickSound(context); onReadMapClick() },
            modifier = Modifier.width(buttonWidth * 2 + spacing).height(60.dp),
            border = BorderStroke(1.dp, Color.DarkGray),
            colors = getMainMenuButtonColors(isNightMode = isNightMode),
            shape = roundedCornerShape
        ) {
            Text(stringResource(R.string.menu_load_button), fontSize = 16.sp)
        }
    }
}

@Composable
fun ReadMapDialog(onDismiss: () -> Unit, onPlay: (String) -> Unit, isNightMode: Boolean) {
    var mapCode by remember { mutableStateOf("") }
    val isCodeValid = remember(mapCode) { isMapCodeValid(mapCode) }
    val normalButtonColors = getMainMenuButtonColors(isNightMode = isNightMode)

    BaseMenu(title = stringResource(R.string.menu_loadlevel_title), isNightMode = isNightMode) {
        TextField(
            value = mapCode,
            onValueChange = { mapCode = it },
            label = { Text(stringResource(R.string.menu_loadlevel_description)) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
            maxLines = 5,
            isError = mapCode.isNotBlank() && !isCodeValid,
            supportingText = {
                if (mapCode.isNotBlank() && !isCodeValid) {
                    Text(
                        stringResource(R.string.menu_loadlevel_invalid_code),
                        color = Color(0xFFD32F2F)
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = if (isNightMode) Color(0xFFE0E0E0) else Color.Black,
                unfocusedTextColor = if (isNightMode) Color.LightGray else Color.DarkGray,
                focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                cursorColor = Color(0xFF50E3C2),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedLabelColor = Color(0xFF50E3C2),
                unfocusedLabelColor = Color.Gray
            )
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val context = LocalContext.current
            Button(
                onClick = { playMenuClickSound(context); onDismiss() },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = normalButtonColors
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancel")
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.menu_loadlevel_cancel_button))
            }
            Button(
                onClick = { playMenuClickSound(context); onPlay(mapCode) },
                enabled = isCodeValid,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF50E3C2),
                    contentColor = Color.Black,
                    disabledContainerColor = Color.Gray
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Load Level")
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.menu_loadlevel_load_button))
            }
        }
    }
}

fun isMapCodeValid(code: String): Boolean {
    val cleanCode = code.trim()
    if (cleanCode.isEmpty()) return false
    try {
        val parts = cleanCode.split('|')
        if (parts.size < 3) return false
        val width = parts[0].toIntOrNull()
        if (width == null || width <= 0) return false
        val compressedGrid = parts[1]
        val validChars = "W.Xx" + ('a'..'r').joinToString("") + ('A'..'R').joinToString("")
        var decompressedLength = 0
        var i = 0
        while (i < compressedGrid.length) {
            var numStr = ""
            while (i < compressedGrid.length && compressedGrid[i].isDigit()) {
                numStr += compressedGrid[i]
                i++
            }
            val count = numStr.toIntOrNull() ?: 1
            if (i >= compressedGrid.length) return false
            val char = compressedGrid[i]
            if (char.isDigit()) return false
            if (char !in validChars) return false
            decompressedLength += count
            i++
        }
        if (decompressedLength == 0) return false
        if (decompressedLength % width != 0) return false
    } catch (_: Exception) {
        return false
    }
    return true
}

@Composable
fun DifficultyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    roundedCornerShape: RoundedCornerShape,
    currentProgress: Int? = null,
    subtitle: String? = null,
    isLocked: Boolean = false,
    unlockText: String? = null,
    isNightMode: Boolean
) {
    val context = LocalContext.current
    val buttonColors = if (isNightMode) {
        ButtonDefaults.buttonColors(
            containerColor = Color.DarkGray,
            contentColor = Color(0xFFE0E0E0),
            disabledContainerColor = Color(0xFF333333),
            disabledContentColor = Color.Gray
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = Color(0xFFE0E0E0),
            disabledContentColor = Color.DarkGray
        )
    }

    Button(
        onClick = { playMenuClickSound(context); onClick() },
        colors = buttonColors,
        modifier = modifier,
        enabled = !isLocked,
        border = BorderStroke(1.dp, Color.DarkGray),
        shape = roundedCornerShape,
        contentPadding = PaddingValues(top = 8.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLocked) {
                Icon(Icons.Default.Lock, contentDescription = "Locked")
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(text = text, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            val subtitleText = when {
                isLocked -> unlockText
                subtitle != null -> subtitle
                currentProgress != null -> stringResource(R.string.menu_buttonlevels_wins_text, currentProgress)
                else -> ""
            }
            if (!subtitleText.isNullOrEmpty()) {
                Text(text = subtitleText, fontSize = 12.sp, color = LocalContentColor.current.copy(alpha = 0.7f), textAlign = TextAlign.Center)
            }
        }
    }
}