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

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import infrared.tilematch.constants.GameConstants
import infrared.tilematch.data.DecodedMap
import infrared.tilematch.data.GameState
import infrared.tilematch.data.Tile
import infrared.tilematch.gamelogic.decodeStringToGameState
import infrared.tilematch.gamelogic.encodeGameStateToString
import infrared.tilematch.gamelogic.extractLayoutFromMapString
import infrared.tilematch.gamelogic.generateRandomTileMap
import infrared.tilematch.managers.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import infrared.tilematch.R

@Composable
fun GameScreen(
    difficulty: Int,
    mapString: String?,
    isNightMode: Boolean,
    areSymbolsEnabled: Boolean,
    onGameStateCreated: (GameState) -> Unit,
    onGoToMenu: () -> Unit,
    initialShuffleUses: Int,
    initialRevealMapUsed: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var gameTrigger by remember { mutableStateOf(0) }

    val levelSeed = rememberSaveable { mutableStateOf(mapString) }
    val originalLayoutSeed = remember { mutableStateOf<String?>(null) }

    var tilesAndData by remember { mutableStateOf<Pair<List<List<Tile?>>, DecodedMap>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showContinueMessage by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var gameState by remember { mutableStateOf<GameState?>(null) }

    val isContinuingGame = remember(mapString) { mapString != null && difficulty != -1 }

    LaunchedEffect(key1 = gameTrigger) {
        isLoading = true
        tilesAndData = null
        delay(300)

        if (gameTrigger == 0 && isContinuingGame) {
            showContinueMessage = true
        }

        val mapToLoad = levelSeed.value
        val decodedData: DecodedMap
        val finalTiles: List<List<Tile?>>

        if (mapToLoad != null && mapToLoad.isNotBlank()) {
            decodedData = decodeStringToGameState(mapToLoad)
            finalTiles = decodedData.tiles
            // If the original layout hasn't been saved yet, extract and save it.
            if (originalLayoutSeed.value == null) {
                originalLayoutSeed.value = extractLayoutFromMapString(mapToLoad)
            }
        } else {
            // generate new map
            val (w, h) = when (difficulty) {
                0 -> 7 to 7; 1 -> 9 to 9; 2 -> 11 to 11; 3 -> 13 to 13; else -> 15 to 15
            }
            finalTiles = generateRandomTileMap(h, w, difficulty)
            val newMapString = encodeGameStateToString(finalTiles, emptyList(), 7)

            // Update both states: the seed to load and the original layout
            levelSeed.value = newMapString
            originalLayoutSeed.value = newMapString
            decodedData = DecodedMap(finalTiles, emptyList(), 7)
        }

        tilesAndData = Pair(finalTiles, decodedData)
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
        val backgroundRes = if (isNightMode) R.drawable.game_bg_night else R.drawable.game_bg
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (isLoading || tilesAndData == null) {
            LoadingScreen(difficulty = difficulty, isContinuing = isContinuingGame)
        } else {
            val (finalTiles, decodedData) = tilesAndData!!
            val gameHeight = finalTiles.size
            val gameWidth = finalTiles.firstOrNull()?.size ?: 0

            if (gameWidth > 0 && gameHeight > 0) {
                val layout = rememberGameLayout(gameWidth = gameWidth, gameHeight = gameHeight)

                gameState = remember(levelSeed) {
                    val isRestartingOrNew = levelSeed.value == originalLayoutSeed.value
                    GameState(
                        difficulty,
                        finalTiles,
                        context,
                        restoredMatchAreaChars = decodedData.matchAreaChars,
                        initialMaxMatchAreaSize = decodedData.maxMatchAreaSize,
                        initialShuffleUses = if (isRestartingOrNew) 1 else initialShuffleUses,
                        initialRevealMapUsed = if (isRestartingOrNew) false else initialRevealMapUsed
                    )
                }

                LaunchedEffect(gameState) {
                    gameState?.let { onGameStateCreated(it) }
                }

                // Restart action: loads the clean original layout.
                val handleRestart = {
                    val prefs = context.getSharedPreferences(GameConstants.PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit()
                        .remove(GameConstants.shuffleUsesKey(difficulty))
                        .remove(GameConstants.revealMapUsedKey(difficulty))
                        .apply()
                    levelSeed.value = originalLayoutSeed.value
                    gameTrigger += 1
                }

                // Generate new action: clears the seed to force generation of a new map.
                val handleGenerateNew = {
                    val prefs = context.getSharedPreferences(GameConstants.PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit()
                        .remove(GameConstants.mapKey(difficulty))
                        .remove(GameConstants.shuffleUsesKey(difficulty))
                        .remove(GameConstants.revealMapUsedKey(difficulty))
                        .apply()
                    levelSeed.value = null // clean state
                    originalLayoutSeed.value = null // clean memory
                    gameTrigger += 1
                }

                GameScreenContent(
                    difficulty = difficulty,
                    layout = layout,
                    gameState = gameState!!,
                    onRestart = handleRestart,
                    onGenerateNew = handleGenerateNew,
                    onGoToMenu = onGoToMenu,
                    showContinueMessage = showContinueMessage,
                    onContinueMessageShown = { showContinueMessage = false },
                    isPaused = isPaused,
                    areSymbolsEnabled = areSymbolsEnabled,
                    onPauseChanged = { isPaused = it },
                    initialMapString = levelSeed.value ?: "",
                    isNightMode = isNightMode
                )
            } else {
                LoadingScreen(difficulty = difficulty, isContinuing = false)
            }
        }
    }
}


@Composable
fun GameScreenContent(
    difficulty: Int,
    layout: GameLayout,
    gameState: GameState,
    onRestart: () -> Unit,
    onGenerateNew: () -> Unit,
    onGoToMenu: () -> Unit,
    showContinueMessage: Boolean,
    onContinueMessageShown: () -> Unit,
    isPaused: Boolean,
    onPauseChanged: (Boolean) -> Unit,
    initialMapString: String,
    isNightMode: Boolean,
    areSymbolsEnabled: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showHelpMenu by remember { mutableStateOf(false) }
    val isAdFreeMode = (difficulty == 0) // para as crianças
    val isAnyMenuOpen = isPaused || gameState.isGameWon || gameState.isGameOver || showHelpMenu
    val isPlusOneSlotAvailable = !gameState.hasUsedPlusOneSlot
    val isShuffleAvailable = gameState.shuffleUsesRemaining > 0
    val isRevealMapAvailable = !gameState.hasUsedRevealMap
    val areHelpsAvailable = !isAdFreeMode && (isPlusOneSlotAvailable || isShuffleAvailable || isRevealMapAvailable)
    val newSlotAlpha = remember { Animatable(1f) }
    var hasAnimated8thSlot by remember { mutableStateOf(gameState.hasUsedPlusOneSlot) }
    var showEndGameMenu by remember { mutableStateOf(false) }
    var isInputBlocked by remember { mutableStateOf(false) }
    var showTutorialDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (difficulty == 1) {
            val prefs = context.getSharedPreferences(GameConstants.PREFS_NAME, Context.MODE_PRIVATE)
            val isFirstTime = prefs.getBoolean(GameConstants.PREFS_FIRST_TIME_LAUNCH, true)

            if (isFirstTime) {
                showTutorialDialog = true
                prefs.edit().putBoolean(GameConstants.PREFS_FIRST_TIME_LAUNCH, false).apply()
            }
        }
    }

    LaunchedEffect(gameState.hasUsedPlusOneSlot) {
        if (gameState.hasUsedPlusOneSlot && !hasAnimated8thSlot) {
            hasAnimated8thSlot = true

            repeat(3) {
                newSlotAlpha.animateTo(0.3f, animationSpec = tween(250))
                newSlotAlpha.animateTo(1.0f, animationSpec = tween(250))
            }
        }
    }

    LaunchedEffect(gameState.isGameOver, gameState.isGameWon) {
        if ((gameState.isGameOver || gameState.isGameWon) && gameState.difficulty != -1) {
            isInputBlocked = true
            if (gameState.isGameWon) SoundManager.playWinSound(context) else SoundManager.playLoseSound(context)
            showEndGameMenu = true


            val prefs = context.getSharedPreferences(GameConstants.PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .remove(GameConstants.mapKey(gameState.difficulty))
                .remove(GameConstants.shuffleUsesKey(gameState.difficulty))
                .remove(GameConstants.revealMapUsedKey(gameState.difficulty))
                .apply()

            delay(500)
            isInputBlocked = false
        }
    }

    BackHandler(enabled = !isAnyMenuOpen) { onPauseChanged(true) }
    BackHandler(enabled = showHelpMenu) { showHelpMenu = false }
    BackHandler(enabled = isPaused) { onPauseChanged(false) }
    BackHandler(enabled = gameState.isGameWon || gameState.isGameOver) { onGoToMenu() }

    Box(modifier = Modifier.fillMaxSize()) {
        val borderPainters = listOf(
            R.drawable.frame_a, R.drawable.frame_b, R.drawable.frame_c, R.drawable.frame_d,
            R.drawable.frame_e, R.drawable.frame_f, R.drawable.frame_g, R.drawable.frame_h
        ).map { painterResource(id = it) }

        DrawGameTiles(layout, gameState, areSymbolsEnabled, isNightMode = isNightMode, scope = coroutineScope)
        DrawBorderAroundGameGrid(layout, borderPainters, isNightMode = isNightMode)


        // --- draw flying tile ---
        gameState.flyingTiles.forEach { flying ->
            Image(
                painter = painterResource(id = tileImageRes(flying.imageChar, areSymbolsEnabled)),
                contentDescription = null,
                modifier = Modifier
                    .absoluteOffset(flying.animOffset.value.x.dp, flying.animOffset.value.y.dp)
                    .size(layout.blockSize)
            )
        }


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = layout.startY + layout.blockSize * layout.gameHeightBlocks)
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(layout.blockSize / 2))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                repeat(7) { DrawTileInBar(R.drawable.area, layout.blockSize, isNightMode = isNightMode) }
                if (gameState.maxMatchAreaSize > 7) {
                    DrawTileInBar(
                        resourceId = R.drawable.area,
                        blockSize = layout.blockSize,
                        modifier = Modifier.alpha(newSlotAlpha.value),
                        isNightMode = isNightMode
                    )
                }
            }
        }

        val barWidth = layout.blockSize * gameState.maxMatchAreaSize
        val barStartX = (LocalConfiguration.current.screenWidthDp.dp - barWidth) / 2
        val groupedTiles = gameState.matchArea.groupBy { it.imageChar }.entries.sortedBy { entry -> gameState.matchArea.indexOfFirst { it.imageChar == entry.key } }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = layout.startY + layout.blockSize * layout.gameHeightBlocks + layout.blockSize / 2,
                    start = barStartX
                ),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            groupedTiles.forEach { (_, group) ->
                group.forEach { tile ->
                    DrawTileInBar(
                        resourceId = tileImageRes(tile.imageChar, areSymbolsEnabled),
                        blockSize = layout.blockSize,
                        isNightMode = isNightMode
                    )
                }
            }
        }

        if (isAnyMenuOpen) {
            Box(modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = false) {})
        }

        if (gameState.isMapRevealed) { // countdown
            RevealCountdownOverlay(timeRemaining = gameState.revealTimeRemaining)
        }

        TopMenuBar(
            onHelpClicked = { showHelpMenu = true },
            isEnabled = !isAnyMenuOpen,
            areHelpsAvailable = areHelpsAvailable,
            modifier = Modifier.align(Alignment.TopEnd),
            isNightMode = isNightMode
        )

        ContinueGameMessage(visible = showContinueMessage, onDismiss = onContinueMessageShown)

        val handleShare = {
            val shareableCode = extractLayoutFromMapString(initialMapString)
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareableCode)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            context.startActivity(shareIntent)
        }

        val showAdAndExecute: (() -> Unit) -> Unit = { action ->
            action()
        }

        AnimatedVisibility(
            visible = isPaused || showEndGameMenu,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            when {
                isPaused -> StyledPauseMenu(
                    onContinue = { onPauseChanged(false) },
                    onExit = { showAdAndExecute(onGoToMenu) },
                    onShare = handleShare,
                    isNightMode = isNightMode
                )
                gameState.isGameWon -> StyledGameWonMenu(
                    onGoToMenu = { showAdAndExecute(onGoToMenu) },
                    onGenerateNew = { showAdAndExecute(onGenerateNew) },
                    onShare = handleShare,
                    showNewLevelButton = difficulty != -1,
                    isBabyMode = (difficulty == 0),
                    isNightMode = isNightMode
                )
                gameState.isGameOver -> StyledGameOverMenu(
                    onRestart = { showAdAndExecute(onRestart) },
                    onGenerateNew = { showAdAndExecute(onGenerateNew) },
                    onGoToMenu = { showAdAndExecute(onGoToMenu) },
                    showNewLevelButton = difficulty != -1,
                    isBabyMode = (difficulty == 0),
                    isNightMode = isNightMode
                )
            }
        }

        AnimatedVisibility(visible = showHelpMenu, enter = fadeIn(), exit = fadeOut()) {
            val showRewardedAdAndExecute: (() -> Unit) -> Unit = { action ->
                action()
                showHelpMenu = false
            }

            HelpMenu(
                onClose = { showHelpMenu = false },
                onPlusOneSlotClicked = { showRewardedAdAndExecute { gameState.grantPlusOneSlotReward() } },
                isPlusOneSlotEnabled = isPlusOneSlotAvailable,
                onShuffleClicked = { showRewardedAdAndExecute { coroutineScope.launch { gameState.shuffleWithAnimation() } } },
                isShuffleEnabled = isShuffleAvailable,
                onRevealMapClicked = { showRewardedAdAndExecute { coroutineScope.launch { gameState.revealMapTemporarily() } } },
                isRevealMapEnabled = isRevealMapAvailable,
                isNightMode = isNightMode
            )
        }

        if (isInputBlocked) { // hack to block accidental clicks from menus for half a second
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            )
        }

        if (showTutorialDialog) {
            TutorialDialog(
                onDismiss = { showTutorialDialog = false },
                isNightMode = isNightMode
            )
        }
    }
}

@Composable
fun TutorialDialog(onDismiss: () -> Unit, isNightMode: Boolean) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = stringResource(R.string.game_tutorialdialog_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TutorialSection(
                    titleRes = R.string.game_tutorialdialog_point1,
                    images = listOf(R.drawable.a, R.drawable.a, R.drawable.a)
                )
                TutorialSection(
                    titleRes = R.string.game_tutorialdialog_point3
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                TutorialSection(
                    titleRes = R.string.game_tutorialdialog_point2,
                    images = listOf(
                        R.drawable.area, R.drawable.area, R.drawable.area,
                        R.drawable.area, R.drawable.area, R.drawable.area, R.drawable.area
                    )
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                TutorialSection(
                    titleRes = R.string.game_tutorialdialog_point6,
                    images = listOf(R.drawable.tile_grey)
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                TutorialSection(
                    titleRes = R.string.game_tutorialdialog_point7,
                    images = listOf(R.drawable.no_a)
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                TutorialSection(
                    titleRes = R.string.game_tutorialdialog_point4,
                    boldItems = listOf(
                        R.string.game_tutorialdialog_point4a,
                        R.string.game_tutorialdialog_point4b,
                        R.string.game_tutorialdialog_point4c
                    )
                )
                Spacer(Modifier.height(16.dp))
                TutorialSection(
                    titleRes = R.string.game_tutorialdialog_point5
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.game_tutorialdialog_close_button))
            }
        },
        containerColor = if (isNightMode) Color.DarkGray else Color.White,
        titleContentColor = if (isNightMode) Color.White else Color.Black,
        textContentColor = if (isNightMode) Color.White else Color.Black
    )
}

@Composable
fun TutorialSection(
    titleRes: Int,
    descriptionRes: Int? = null,
    images: List<Int>? = null,
    boldItems: List<Int>? = null
) {
    Text(text = stringResource(titleRes))

    if (images != null) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            images.forEach { imageRes ->
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (descriptionRes != null) {
        Text(text = stringResource(descriptionRes))
    }

    if (boldItems != null) {
        boldItems.forEach { textRes ->
            Text(text = stringResource(textRes), fontWeight = FontWeight.Bold)
        }
    }
}