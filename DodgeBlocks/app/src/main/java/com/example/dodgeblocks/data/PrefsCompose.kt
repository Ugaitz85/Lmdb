package com.example.dodgeblocks.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext

data class PrefsState(
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val difficulty: Difficulty,
    val skin: Skin,
    val highScore: Int,
    val top5: List<Int>
)

@Composable
fun rememberPrefs(context: Context = LocalContext.current): Prefs {
    // Intención: instancia única ligada al Contexto actual.
    return remember(context) { Prefs(context.applicationContext) }
}

@Composable
fun rememberPrefsState(prefs: Prefs): PrefsState {
    val sound by prefs.soundEnabledFlow.collectAsStateWithLifecycle(initialValue = true)
    val vib by prefs.vibrationEnabledFlow.collectAsStateWithLifecycle(initialValue = true)
    val diff by prefs.difficultyFlow.collectAsStateWithLifecycle(initialValue = Difficulty.NORMAL)
    val skin by prefs.skinFlow.collectAsStateWithLifecycle(initialValue = Skin.CLASSIC)
    val high by prefs.highScoreFlow.collectAsStateWithLifecycle(initialValue = 0)
    val top5 by prefs.top5Flow.collectAsStateWithLifecycle(initialValue = emptyList())

    return PrefsState(
        soundEnabled = sound,
        vibrationEnabled = vib,
        difficulty = diff,
        skin = skin,
        highScore = high,
        top5 = top5
    )
}
