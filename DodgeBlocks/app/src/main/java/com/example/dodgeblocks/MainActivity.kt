package com.example.dodgeblocks

import android.app.Activity
import android.os.Bundle
import android.view.SoundEffectConstants
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.dodgeblocks.data.Difficulty
import com.example.dodgeblocks.data.Prefs
import com.example.dodgeblocks.data.Skin
import com.example.dodgeblocks.data.rememberPrefs
import com.example.dodgeblocks.data.rememberPrefsState
import com.example.dodgeblocks.game.DifficultyParams
import com.example.dodgeblocks.game.GameStatus
import com.example.dodgeblocks.game.GameWorld
import com.example.dodgeblocks.game.movePlayerBy
import com.example.dodgeblocks.game.safeDt
import com.example.dodgeblocks.game.setPaused
import com.example.dodgeblocks.game.stepWorld
import com.example.dodgeblocks.ui.DodgeBlocksTheme
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

private sealed interface Screen {
    data object Menu : Screen
    data object Game : Screen
    data object Settings : Screen
}

data class GamePalette(
    val background: Color,
    val player: Color,
    val block: Color,
    val hud: Color
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableImmersiveEdgeToEdge()

        setContent {
            DodgeBlocksTheme {
                val prefs = rememberPrefs()
                val prefsState = rememberPrefsState(prefs)

                var screen by rememberSaveable { mutableStateOf<Screen>(Screen.Menu) }
                var gameSessionId by rememberSaveable { mutableIntStateOf(1) }
                var showFps by rememberSaveable { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (screen) {
                        Screen.Menu -> MenuScreen(
                            highScore = prefsState.highScore,
                            top5 = prefsState.top5,
                            onPlay = {
                                gameSessionId += 1
                                screen = Screen.Game
                            },
                            onSettings = { screen = Screen.Settings }
                        )

                        Screen.Settings -> SettingsScreen(
                            prefs = prefs,
                            soundEnabled = prefsState.soundEnabled,
                            vibrationEnabled = prefsState.vibrationEnabled,
                            difficulty = prefsState.difficulty,
                            skin = prefsState.skin,
                            showFps = showFps,
                            onShowFpsChange = { showFps = it },
                            onBack = { screen = Screen.Menu }
                        )

                        Screen.Game -> GameScreen(
                            prefs = prefs,
                            difficulty = prefsState.difficulty,
                            skin = prefsState.skin,
                            soundEnabled = prefsState.soundEnabled,
                            vibrationEnabled = prefsState.vibrationEnabled,
                            showFps = showFps,
                            gameSessionId = gameSessionId,
                            onRetry = { gameSessionId += 1 },
                            onMenu = { screen = Screen.Menu }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableImmersiveEdgeToEdge()
    }

    private fun enableImmersiveEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

@Composable
private fun MenuScreen(
    highScore: Int,
    top5: List<Int>,
    onPlay: () -> Unit,
    onSettings: () -> Unit
) {
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Dodge Blocks",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "High Score: $highScore",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(18.dp))
        OutlinedCard(
            modifier = Modifier.width(320.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    text = "Top 5",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                if (top5.isEmpty()) {
                    Text(
                        text = "Aún no hay puntuaciones. ¡Juega una partida!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    top5.take(5).forEachIndexed { i, s ->
                        Row(
                            Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${i + 1}.", fontWeight = FontWeight.SemiBold)
                            Text(text = "$s s")
                        }
                        if (i != minOf(top5.size, 5) - 1) {
                            Divider(Modifier.padding(vertical = 6.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                onPlay()
            },
            modifier = Modifier.width(240.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Jugar")
        }

        Spacer(Modifier.height(10.dp))

        FilledTonalButton(
            onClick = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                onSettings()
            },
            modifier = Modifier.width(240.dp)
        ) {
            Icon(Icons.Filled.Settings, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Ajustes")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    prefs: Prefs,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    difficulty: Difficulty,
    skin: Skin,
    showFps: Boolean,
    onShowFpsChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .padding(18.dp)
    ) {
        Text(
            text = "Ajustes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(14.dp))

        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Sonido (efectos del sistema)",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            scope.launch { prefs.setSoundEnabled(it) }
                        }
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Vibración / Haptics",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            scope.launch { prefs.setVibrationEnabled(it) }
                        }
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Mostrar FPS (overlay)", modifier = Modifier.weight(1f))
                    Switch(checked = showFps, onCheckedChange = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onShowFpsChange(it)
                    })
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        var diffExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = diffExpanded,
            onExpandedChange = { diffExpanded = !diffExpanded }
        ) {
            OutlinedTextField(
                value = difficulty.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Dificultad") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diffExpanded) },
                modifier = Modifier.menuAnchor().fillMaxSize()
            )
            ExposedDropdownMenu(
                expanded = diffExpanded,
                onDismissRequest = { diffExpanded = false }
            ) {
                Difficulty.values().forEach { d ->
                    DropdownMenuItem(
                        text = { Text(d.name) },
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            diffExpanded = false
                            scope.launch { prefs.setDifficulty(d) }
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        var skinExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = skinExpanded,
            onExpandedChange = { skinExpanded = !skinExpanded }
        ) {
            OutlinedTextField(
                value = skin.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Skin") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = skinExpanded) },
                modifier = Modifier.menuAnchor().fillMaxSize()
            )
            ExposedDropdownMenu(
                expanded = skinExpanded,
                onDismissRequest = { skinExpanded = false }
            ) {
                Skin.values().forEach { s ->
                    DropdownMenuItem(
                        text = { Text(s.name) },
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            skinExpanded = false
                            scope.launch { prefs.setSkin(s) }
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = "Notas: sin red, sin recursos binarios. Sonido = efectos del sistema.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxSize()) {
            TextButton(onClick = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                onBack()
            }) {
                Text("Volver al Menú")
            }
        }
    }
}

@Composable
private fun GameScreen(
    prefs: Prefs,
    difficulty: Difficulty,
    skin: Skin,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    showFps: Boolean,
    gameSessionId: Int,
    onRetry: () -> Unit,
    onMenu: () -> Unit
) {
    val context = LocalContext.current
    val activity = (context as? Activity)
    val view = LocalView.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    DisposableEffect(gameSessionId) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val palette = remember(skin) { paletteForSkin(skin) }
    val params = remember(difficulty) {
        paramsForDifficulty(difficulty, densityScale = view.resources.displayMetrics.density)
    }

    var viewport by remember { mutableStateOf(IntSize.Zero) }
    var world by remember(gameSessionId) { mutableStateOf<GameWorld?>(null) }
    var leaderboardSaved by remember(gameSessionId) { mutableStateOf(false) }

    var fps by remember { mutableFloatStateOf(0f) }
    var fpsAcc by remember { mutableFloatStateOf(0f) }
    var fpsFrames by remember { mutableIntStateOf(0) }

    LaunchedEffect(gameSessionId, viewport) {
        if (viewport.width > 0 && viewport.height > 0) {
            val seed = System.nanoTime() xor (gameSessionId.toLong() shl 17)
            world = GameWorld.initial(
                widthPx = viewport.width.toFloat(),
                heightPx = viewport.height.toFloat(),
                playerRadiusPx = 16f * view.resources.displayMetrics.density,
                seed = seed
            )
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, gameSessionId) {
        val obs = LifecycleEventObserver { _, event ->
            val w = world ?: return@LifecycleEventObserver
            if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_PAUSE) {
                if (w.status == GameStatus.RUNNING) {
                    world = setPaused(w, paused = true)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    LaunchedEffect(gameSessionId, viewport, params) {
        var lastNanos = 0L
        while (isActive) {
            val frameNanos = androidx.compose.runtime.withFrameNanos { it }
            if (lastNanos == 0L) {
                lastNanos = frameNanos
                continue
            }
            val w = world
            if (w == null) {
                lastNanos = frameNanos
                continue
            }

            val dt = safeDt((frameNanos - lastNanos) / 1_000_000_000f)
            lastNanos = frameNanos

            if (showFps) {
                fpsAcc += dt
                fpsFrames += 1
                if (fpsAcc >= 1.0f) {
                    fps = (fpsFrames / max(0.001f, fpsAcc))
                    fpsAcc = 0f
                    fpsFrames = 0
                }
            } else {
                fps = 0f
                fpsAcc = 0f
                fpsFrames = 0
            }

            if (w.status == GameStatus.RUNNING) {
                val (next, collidedNow) = stepWorld(w, dt, params)

                if (collidedNow && vibrationEnabled) {
                    haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                }
                if (collidedNow && soundEnabled) {
                    view.playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN)
                }

                world = next
            }
        }
    }

    LaunchedEffect(gameSessionId, world?.status) {
        val w = world ?: return@LaunchedEffect
        if (w.status == GameStatus.GAME_OVER && !leaderboardSaved) {
            leaderboardSaved = true
            prefs.updateLeaderboardIfNeeded(w.score)
        }
    }

    BackHandler(enabled = true) {
        val w = world
        if (w == null) {
            onMenu()
            return@BackHandler
        }
        when (w.status) {
            GameStatus.RUNNING -> world = setPaused(w, paused = true)
            GameStatus.PAUSED, GameStatus.GAME_OVER -> onMenu()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .pointerInput(gameSessionId) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val w = world ?: return@detectDragGestures
                        if (w.status == GameStatus.RUNNING) {
                            world = movePlayerBy(w, dragAmount.x, dragAmount.y)
                        }
                    }
                )
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(8.dp)
                .background(Color.Transparent)
                .align(Alignment.Center)
                .then(
                    Modifier
                        .fillMaxSize()
                        .padding(0.dp)
                ),
            onDraw = {
                val size = IntSize(size.width.toInt(), size.height.toInt())
                if (size != viewport) viewport = size

                val w = world ?: return@Canvas

                w.blocks.forEach { b ->
                    drawRect(
                        color = palette.block,
                        topLeft = Offset(b.x, b.y),
                        size = Size(b.size, b.size)
                    )
                }

                drawCircle(
                    color = palette.player,
                    radius = w.player.r,
                    center = Offset(w.player.x, w.player.y)
                )
            }
        )

        val w = world
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp, start = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = "Score: ${w?.score ?: 0}s",
                        color = palette.hud,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (showFps) {
                        Text(
                            text = "FPS: ${fps.toInt()}",
                            color = palette.hud.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.width(10.dp))

            IconButton(
                onClick = {
                    val ww = world ?: return@IconButton
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    world = setPaused(ww, paused = ww.status != GameStatus.PAUSED)
                },
                modifier = Modifier
                    .size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Pause,
                    contentDescription = "Pausa",
                    tint = palette.hud
                )
            }
        }

        if (w != null && w.status != GameStatus.RUNNING) {
            OverlayCard(
                title = if (w.status == GameStatus.PAUSED) "Pausado" else "Game Over",
                subtitle = if (w.status == GameStatus.GAME_OVER) "Puntuación: ${w.score}s" else "Arrastra para esquivar los bloques",
                primaryText = if (w.status == GameStatus.PAUSED) "Reanudar" else "Reintentar",
                onPrimary = {
                    if (soundEnabled) view.playSoundEffect(SoundEffectConstants.CLICK)
                    if (w.status == GameStatus.PAUSED) {
                        world = setPaused(w, paused = false)
                    } else {
                        onRetry()
                    }
                },
                secondaryText = "Menú",
                onSecondary = {
                    if (soundEnabled) view.playSoundEffect(SoundEffectConstants.CLICK)
                    onMenu()
                },
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun OverlayCard(
    title: String,
    subtitle: String,
    primaryText: String,
    onPrimary: () -> Unit,
    secondaryText: String,
    onSecondary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.42f))
    ) {
        Card(
            modifier = modifier
                .padding(18.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(14.dp))
                Button(onClick = onPrimary, modifier = Modifier.width(220.dp)) {
                    Text(primaryText)
                }
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(onClick = onSecondary, modifier = Modifier.width(220.dp)) {
                    Text(secondaryText)
                }
            }
        }
    }
}

private fun paramsForDifficulty(d: Difficulty, densityScale: Float): DifficultyParams {
    return when (d) {
        Difficulty.EASY -> DifficultyParams(
            baseSpawnIntervalSec = 0.85f,
            blockSizePxMin = 34f * densityScale,
            blockSizePxMax = 72f * densityScale,
            speedPxPerSecMin = 240f * densityScale,
            speedPxPerSecMax = 430f * densityScale
        )

        Difficulty.NORMAL -> DifficultyParams(
            baseSpawnIntervalSec = 0.65f,
            blockSizePxMin = 32f * densityScale,
            blockSizePxMax = 78f * densityScale,
            speedPxPerSecMin = 300f * densityScale,
            speedPxPerSecMax = 520f * densityScale
        )

        Difficulty.HARD -> DifficultyParams(
            baseSpawnIntervalSec = 0.48f,
            blockSizePxMin = 28f * densityScale,
            blockSizePxMax = 82f * densityScale,
            speedPxPerSecMin = 360f * densityScale,
            speedPxPerSecMax = 640f * densityScale
        )
    }
}

private fun paletteForSkin(s: Skin): GamePalette {
    return when (s) {
        Skin.CLASSIC -> GamePalette(
            background = Color(0xFF0B1220),
            player = Color(0xFF60A5FA),
            block = Color(0xFFE2E8F0),
            hud = Color(0xFFE2E8F0)
        )

        Skin.NEON -> GamePalette(
            background = Color(0xFF070A12),
            player = Color(0xFF22C55E),
            block = Color(0xFFF97316),
            hud = Color(0xFFE2E8F0)
        )

        Skin.PASTEL -> GamePalette(
            background = Color(0xFFF8FAFC),
            player = Color(0xFF93C5FD),
            block = Color(0xFFFBCFE8),
            hud = Color(0xFF0F172A)
        )
    }
}
