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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.platform.LocalContext
import infrared.tilematch.data.GameState
import infrared.tilematch.utils.BaseMenu
import infrared.tilematch.utils.playMenuClickSound
import kotlinx.coroutines.delay
import kotlin.math.ceil
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import infrared.tilematch.enums.ButtonType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import infrared.tilematch.R


@Composable
private fun getButtonColorsForType(
    type: ButtonType,
    isNightMode: Boolean
): ButtonColors {
    return when (type) {
        ButtonType.Normal -> if (isNightMode) {
            ButtonDefaults.buttonColors(
                containerColor = Color.DarkGray,
                contentColor = Color.White
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        }
        ButtonType.Destructive -> if (isNightMode) {
            ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9E2B25), // darker red
                contentColor = Color.White
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF44336), // red
                contentColor = Color.White
            )
        }
    }
}

@Composable
fun StyledGameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isNightMode: Boolean = false,
    buttonType: ButtonType = ButtonType.Normal
) {
    val context = LocalContext.current
    val colors = getButtonColorsForType(type = buttonType, isNightMode = isNightMode)

    Button(
        onClick = {
            playMenuClickSound(context)
            onClick()
        },
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = colors,
        enabled = enabled
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = text)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HelpButton(
    onClick: () -> Unit,
    isEnabled: Boolean,
    helpCountText: String? = null,
    isNightMode: Boolean
) {
    val context = LocalContext.current

    val buttonColors = if (isNightMode) {
        ButtonDefaults.buttonColors(
            // darker green for night mode
            containerColor = if (isEnabled) Color(0xFF1B5E20) else Color.DarkGray,
            contentColor = Color.White
        )
    } else {
        ButtonDefaults.buttonColors(
            // green
            containerColor = if (isEnabled) Color(0xFF3D9F40) else Color.Gray,
            contentColor = Color.White
        )
    }

    Button(
        onClick = {
            playMenuClickSound(context)
            onClick()
        },
        enabled = isEnabled,
        modifier = Modifier
            .wrapContentSize()
            .height(40.dp),
        shape = RoundedCornerShape(12.dp),
        colors = buttonColors,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.HelpOutline,
            contentDescription = "Help",
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.game_help_button),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RevealCountdownOverlay(timeRemaining: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = false) {}
            .windowInsetsPadding(WindowInsets.statusBars),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 24.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timeRemaining.toString(),
                fontSize = 32.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun HelpMenu(
    onClose: () -> Unit,
    onPlusOneSlotClicked: () -> Unit,
    isPlusOneSlotEnabled: Boolean,
    onShuffleClicked: () -> Unit,
    isShuffleEnabled: Boolean,
    onRevealMapClicked: () -> Unit,
    isRevealMapEnabled: Boolean,
    isNightMode: Boolean
) {
    BaseMenu(title = stringResource(R.string.game_helpmenu_title), isNightMode = isNightMode) {
        if (isPlusOneSlotEnabled) {
            StyledGameButton(
                text = stringResource(R.string.game_helpmenu_extraspace_button),
                onClick = onPlusOneSlotClicked,
                isNightMode = isNightMode,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Add,
                enabled = isPlusOneSlotEnabled
            )
            Spacer(Modifier.height(16.dp))
        }
        if (isShuffleEnabled) {
            StyledGameButton(
                text = stringResource(R.string.game_helpmenu_shuffle_button),
                onClick = onShuffleClicked,
                isNightMode = isNightMode,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Refresh,
                enabled = isShuffleEnabled
            )
            Spacer(Modifier.height(16.dp))
        }
        if (isRevealMapEnabled) {
            StyledGameButton(
                text = stringResource(R.string.game_helpmenu_reveal_button),
                onClick = onRevealMapClicked,
                isNightMode = isNightMode,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Visibility,
                enabled = isRevealMapEnabled
            )
        }
        Spacer(Modifier.height(24.dp))
        StyledGameButton(
            text = stringResource(R.string.game_helpmenu_close_buton),
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            isNightMode = isNightMode,
            buttonType = ButtonType.Destructive
        )
    }
}

@Composable
fun StyledPauseMenu(onContinue: () -> Unit, onExit: () -> Unit, onShare: () -> Unit, isNightMode: Boolean) {
    BaseMenu(title = stringResource(R.string.game_pausedialog_title), isNightMode = isNightMode) {
        StyledGameButton(
            text = stringResource(R.string.game_pausedialog_continue_button),
            onClick = onContinue,
            isNightMode = isNightMode,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.PlayArrow
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StyledGameButton(
                text = stringResource(R.string.game_pausedialog_share_button),
                onClick = onShare,
                isNightMode = isNightMode,
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Share
            )
            StyledGameButton(
                text = stringResource(R.string.game_pausedialog_menu_button),
                onClick = onExit,
                isNightMode = isNightMode,
                modifier = Modifier.weight(1f),
                icon = Icons.Default.ExitToApp,
                buttonType = ButtonType.Destructive,
            )
        }
    }
}


@Composable
fun StyledGameOverMenu(
    onRestart: () -> Unit,
    onGenerateNew: () -> Unit,
    onGoToMenu: () -> Unit,
    showNewLevelButton: Boolean,
    isBabyMode: Boolean,
    isNightMode: Boolean
) {
    val context = LocalContext.current
    if (isBabyMode) { // kids have simple menu
        var countdown by remember { mutableStateOf(5) }

        LaunchedEffect(Unit) {
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            onRestart()
        }

        BaseMenu(
            title = stringResource(R.string.game_losedialog_kids_title),
            isNightMode = isNightMode
        ) {
            Button(
                onClick = {
                    playMenuClickSound(context)
                    onRestart()
                },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                )
            ) {
                Text(
                    stringResource(id = R.string.game_losedialog_kids_restart_button, countdown),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            TextButton(onClick = {
                playMenuClickSound(context)
                onGoToMenu()
            }) {
                Text(stringResource(R.string.game_losedialog_kids_menu_button), color = Color.White)
            }
        }
    } else { // modo normal
        BaseMenu(
            title = stringResource(R.string.game_losedialog_title),
            isNightMode = isNightMode
        ) {
            Text(
                stringResource(R.string.game_losedialog_description),
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            StyledGameButton(
                text = stringResource(R.string.game_losedialog_restart_button),
                onClick = onRestart,
                isNightMode = isNightMode,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Refresh
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showNewLevelButton) {
                    StyledGameButton(
                        text = stringResource(R.string.game_losedialog_new_button),
                        onClick = onGenerateNew,
                        isNightMode = isNightMode,
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.ArrowForward
                    )
                }
                StyledGameButton(
                    text = stringResource(R.string.game_losedialog_menu_button),
                    onClick = onGoToMenu,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.ExitToApp,
                    isNightMode = isNightMode,
                    buttonType = ButtonType.Destructive
                )
            }
        }
    }
}

@Composable
fun StyledGameWonMenu(
    onGoToMenu: () -> Unit,
    onGenerateNew: () -> Unit,
    onShare: () -> Unit,
    showNewLevelButton: Boolean,
    isBabyMode: Boolean,
    isNightMode: Boolean
) {
    val context = LocalContext.current
    if (isBabyMode) { // kids. simple menu
        var countdown by remember { mutableStateOf(5) }

        LaunchedEffect(Unit) {
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            onGenerateNew()
        }

        BaseMenu(
            title = stringResource(R.string.game_wondialog_kids_title),
            isNightMode = isNightMode
        ) {
            Button(
                onClick = {
                    playMenuClickSound(context)
                    onGenerateNew()
                },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                )
            ) {
                Text(
                    stringResource(id = R.string.game_wondialog_kids_restart_button, countdown),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            TextButton(onClick = {
                playMenuClickSound(context)
                onGoToMenu()
            }) {
                Text(stringResource(R.string.game_wondialog_kids_menu_button), color = Color.White)
            }
        }
    } else {  // normal game modes
        BaseMenu(title = stringResource(R.string.game_wondialog_title), isNightMode = isNightMode) {
            Text(
                stringResource(R.string.game_wondialog_description),
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            if (showNewLevelButton) {
                StyledGameButton(
                    text = stringResource(R.string.game_wondialog_new_button),
                    onClick = onGenerateNew,
                    isNightMode = isNightMode,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.ArrowForward
                )
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StyledGameButton(
                    text = stringResource(R.string.game_wondialog_share_button),
                    onClick = onShare,
                    isNightMode = isNightMode,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Share
                )

                StyledGameButton(
                    text = stringResource(R.string.game_wondialog_menu_button),
                    onClick = onGoToMenu,
                    isNightMode = isNightMode,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.ExitToApp,
                    buttonType = ButtonType.Destructive
                )
            }
        }
    }
}

@Composable
fun LoadingScreen(difficulty: Int, isContinuing: Boolean) {
    val difficultyLabel = when (difficulty) {
        -1 -> stringResource(R.string.game_loadingscreen_customlevel)
        0 -> stringResource(R.string.menu_kids_button)
        1 -> stringResource(R.string.menu_easy_button)
        2 -> stringResource(R.string.menu_normal_button)
        3 -> stringResource(R.string.menu_hard_button)
        else -> stringResource(R.string.menu_insane_button)
    }
    val loadingText = if (isContinuing) {
        stringResource(id = R.string.game_loadingscreen_load, difficultyLabel)
    } else {
        stringResource(id = R.string.game_loadingscreen_generate, difficultyLabel)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(loadingText, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun TopMenuBar(
    onHelpClicked: () -> Unit,
    isEnabled: Boolean,
    areHelpsAvailable: Boolean,
    modifier: Modifier = Modifier,
    isNightMode: Boolean
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(56.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = areHelpsAvailable,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            HelpButton(
                onClick = onHelpClicked,
                isEnabled = isEnabled,
                isNightMode = isNightMode
            )
        }
    }
}

@Composable
fun ContinueGameMessage(visible: Boolean, onDismiss: () -> Unit) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(3000)
            onDismiss()
        }
    }

    Box(modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(top = 16.dp), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = stringResource(R.string.game_gamecontinued_message),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

data class GameLayout(
    val blockSize: Dp,
    val gameWidthBlocks: Int,
    val gameHeightBlocks: Int,
    val startX: Dp,
    val startY: Dp,
    val tilePositions: List<List<Offset>>,
    val extraBottomHeight: Dp
)

@Composable
fun rememberGameLayout(gameWidth: Int, gameHeight: Int): GameLayout {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp

    val totalWidthBlocks = gameWidth + 1f
    val blockSize = minOf(screenWidthDp / totalWidthBlocks, screenHeightDp / (gameHeight + 2.5f))
    val extraBottomHeight = blockSize * 1.5f
    // val totalContentHeight = blockSize * gameHeight + blockSize + extraBottomHeight  // TODO: fix this permanently. Instead of using measurements, let Android calculate the center. This way it's a measurement that works on tablets and phones in emulators. But it's not 100% centered. I think because the tablet is in landscape mode, the calculation of the bottom Android bar is messing things up?
    val totalContentHeight = blockSize * gameHeight + blockSize
    val startX = blockSize * 0.5f
    val startY = (screenHeightDp - totalContentHeight) / 2
    val tilePositions = List(gameHeight) { row ->
        List(gameWidth) { col ->
            Offset(
                x = startX.value + col * blockSize.value,
                y = startY.value + row * blockSize.value
            )
        }
    }
    return GameLayout(blockSize, gameWidth, gameHeight, startX, startY, tilePositions, extraBottomHeight)
}

@Composable
fun TileImage(
    painter: Painter,
    size: Dp,
    offset: Offset,
    onClick: (() -> Unit)? = null,
    isNightMode: Boolean = false
) {
    val colorFilter = if (isNightMode) {
        ColorFilter.tint(Color.Black.copy(alpha = 0.4f), blendMode = BlendMode.SrcAtop)
    } else {
        null
    }

    val baseModifier = Modifier
        .absoluteOffset(offset.x.dp, offset.y.dp)
        .size(size)


    val scale = remember { Animatable(1f) }
    LaunchedEffect(key1 = onClick) {
        if (onClick != null) {
            scale.animateTo(1.1f, animationSpec = tween(durationMillis = 100))
            scale.animateTo(1f, animationSpec = tween(durationMillis = 100))
        }
    }
    val finalModifier = if (onClick != null) {
        baseModifier
            .scale(scale.value)
            .clickable { onClick() }
    } else {
        baseModifier
    }

    Image(
        painter = painter,
        contentDescription = null,
        modifier = finalModifier,
        contentScale = ContentScale.FillBounds,
        colorFilter = colorFilter
    )
}

@Composable
fun DrawGameTiles(layout: GameLayout, gameState: GameState, areSymbolsEnabled: Boolean, isNightMode: Boolean, scope: CoroutineScope) {
    gameState.tiles.forEach { rowTiles ->
        rowTiles.forEach { tile ->
            tile?.let {
                val offset = layout.tilePositions[it.row][it.col]
                val painter = when {
                    it.isWall -> painterResource(R.drawable.tile_grey)
                    it.isRemoved -> null
                    gameState.isMapRevealed || it.isVisible -> painterResource(id = tileImageRes(it.imageChar, areSymbolsEnabled))
                    else -> painterResource(id = altTileImageRes(it.imageChar))
                }

                painter?.let { p ->
                    val isTilePlayable =
                        !gameState.isMapRevealed &&
                                it.isVisible &&
                                !it.isWall

                    TileImage(
                        painter = p,
                        size = layout.blockSize,
                        offset = offset,
                        onClick = if (isTilePlayable) {
                            { scope.launch { gameState.onTileClick(it.row, it.col, layout) } }
                        } else null,
                        isNightMode = isNightMode
                    )
                }
            }
        }
    }
}

@Composable
fun DrawBorderAroundGameGrid(layout: GameLayout, borderTiles: List<Painter>, isNightMode: Boolean) {
    val blockSize = layout.blockSize
    val half = blockSize / 2
    val startX = layout.startX - half
    val startY = layout.startY - half
    val innerWidth = blockSize * layout.gameWidthBlocks
    val innerHeight = blockSize * layout.gameHeightBlocks + layout.extraBottomHeight
    val tl = borderTiles[0]
    val t  = borderTiles[1]
    val tr = borderTiles[2]
    val l  = borderTiles[3]
    val r  = borderTiles[4]
    val bl = borderTiles[5]
    val b  = borderTiles[6]
    val br = borderTiles[7]
    val horizontalRepeats = layout.gameWidthBlocks * 2
    val verticalRepeats = with(LocalDensity.current) { ceil(innerHeight.toPx() / half.toPx()).toInt() }

    TileImage(painter = tl, size = half, offset = Offset(startX.value, startY.value), isNightMode = isNightMode)
    TileImage(painter = tr, size = half, offset = Offset(startX.value + half.value + innerWidth.value, startY.value), isNightMode = isNightMode)
    TileImage(painter = bl, size = half, offset = Offset(startX.value, startY.value + half.value + innerHeight.value),isNightMode = isNightMode)
    TileImage(painter = br, size = half, offset = Offset(startX.value + half.value + innerWidth.value, startY.value + half.value + innerHeight.value), isNightMode = isNightMode)

    DrawRepeatedBorderSide(painter = t, count = horizontalRepeats, start = Offset(startX.value + half.value, startY.value), isHorizontal = true, tileSize = half, isNightMode = isNightMode)
    DrawRepeatedBorderSide(painter = b, count = horizontalRepeats, start = Offset(startX.value + half.value, startY.value + half.value + innerHeight.value), isHorizontal = true, tileSize = half, isNightMode = isNightMode)
    DrawRepeatedBorderSide(painter = l, count = verticalRepeats, start = Offset(startX.value, startY.value + half.value), isHorizontal = false, tileSize = half, isNightMode = isNightMode)
    DrawRepeatedBorderSide(painter = r, count = verticalRepeats, start = Offset(startX.value + half.value + innerWidth.value, startY.value + half.value), isHorizontal = false, tileSize = half, isNightMode = isNightMode)
}

@Composable
fun DrawRepeatedBorderSide(painter: Painter, count: Int, start: Offset, isHorizontal: Boolean, tileSize: Dp, isNightMode: Boolean) {
    repeat(count) { i ->
        val offset = if (isHorizontal) Offset(start.x + tileSize.value * i, start.y) else Offset(start.x, start.y + tileSize.value * i)
        TileImage(painter = painter, size = tileSize, offset = offset, isNightMode = isNightMode)
    }
}

@Composable
fun DrawTileInBar(
    resourceId: Int,
    blockSize: Dp,
    modifier: Modifier = Modifier,
    isNightMode: Boolean
) {
    val colorFilter = if (isNightMode) {
        ColorFilter.tint(Color.Black.copy(alpha = 0.4f), blendMode = BlendMode.SrcAtop)
    } else {
        null
    }

    Image(
        painter = painterResource(id = resourceId),
        contentDescription = null,
        modifier = modifier.requiredSize(blockSize),
        contentScale = ContentScale.FillBounds,
        colorFilter = colorFilter
    )
}


fun tileImageRes(char: Char, areSymbolsEnabled: Boolean): Int {
    return if (areSymbolsEnabled) {
        when (char) {
            'a' -> R.drawable.symbol_a; 'b' -> R.drawable.symbol_b; 'c' -> R.drawable.symbol_c; 'd' -> R.drawable.symbol_d
            'e' -> R.drawable.symbol_e; 'f' -> R.drawable.symbol_f; 'g' -> R.drawable.symbol_g; 'h' -> R.drawable.symbol_h
            'i' -> R.drawable.symbol_i; 'j' -> R.drawable.symbol_j; 'k' -> R.drawable.symbol_k; 'l' -> R.drawable.symbol_l
            'm' -> R.drawable.symbol_m; 'n' -> R.drawable.symbol_n; 'o' -> R.drawable.symbol_o; 'p' -> R.drawable.symbol_p
            'q' -> R.drawable.symbol_q; 'r' -> R.drawable.symbol_r; else -> R.drawable.area
        }
    } else {
        when (char) {
            'a' -> R.drawable.a; 'b' -> R.drawable.b; 'c' -> R.drawable.c; 'd' -> R.drawable.d
            'e' -> R.drawable.e; 'f' -> R.drawable.f; 'g' -> R.drawable.g; 'h' -> R.drawable.h
            'i' -> R.drawable.i; 'j' -> R.drawable.j; 'k' -> R.drawable.k; 'l' -> R.drawable.l
            'm' -> R.drawable.m; 'n' -> R.drawable.n; 'o' -> R.drawable.o; 'p' -> R.drawable.p
            'q' -> R.drawable.q; 'r' -> R.drawable.r; else -> R.drawable.area
        }
    }
}

fun altTileImageRes(char: Char): Int {
    return when (char) {
        in 'a'..'f' -> R.drawable.no_a
        in 'g'..'l' -> R.drawable.no_g
        in 'm'..'r' -> R.drawable.no_m
        else -> R.drawable.area
    }
}