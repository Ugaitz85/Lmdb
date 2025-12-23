package com.example.dodgeblocks.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import kotlin.math.max

enum class Difficulty { EASY, NORMAL, HARD }
enum class Skin { CLASSIC, NEON, PASTEL }

private val Context.dataStore by preferencesDataStore(name = "dodgeblocks_prefs")

class Prefs(private val context: Context) {

    private object Keys {
        val soundEnabled = booleanPreferencesKey("soundEnabled")
        val vibrationEnabled = booleanPreferencesKey("vibrationEnabled")
        val difficulty = stringPreferencesKey("difficulty")
        val skin = stringPreferencesKey("skin")
        val highScore = intPreferencesKey("highScore")
        val top5 = stringPreferencesKey("top5_json")
    }

    val soundEnabledFlow: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.soundEnabled] ?: true }

    val vibrationEnabledFlow: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.vibrationEnabled] ?: true }

    val difficultyFlow: Flow<Difficulty> =
        context.dataStore.data.map { prefs ->
            prefs[Keys.difficulty]?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() }
                ?: Difficulty.NORMAL
        }

    val skinFlow: Flow<Skin> =
        context.dataStore.data.map { prefs ->
            prefs[Keys.skin]?.let { runCatching { Skin.valueOf(it) }.getOrNull() } ?: Skin.CLASSIC
        }

    val highScoreFlow: Flow<Int> =
        context.dataStore.data.map { it[Keys.highScore] ?: 0 }

    val top5Flow: Flow<List<Int>> =
        context.dataStore.data.map { prefs ->
            decodeTop5(prefs[Keys.top5])
        }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.soundEnabled] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.vibrationEnabled] = enabled }
    }

    suspend fun setDifficulty(difficulty: Difficulty) {
        context.dataStore.edit { it[Keys.difficulty] = difficulty.name }
    }

    suspend fun setSkin(skin: Skin) {
        context.dataStore.edit { it[Keys.skin] = skin.name }
    }

    /**
     * Actualiza highScore y top-5 (orden desc, máx 5).
     * Intención: llamada en Game Over si el score entra en ranking.
     */
    suspend fun updateLeaderboardIfNeeded(newScore: Int) {
        if (newScore <= 0) return

        context.dataStore.edit { prefs: Preferences ->
            val currentTop = decodeTop5(prefs[Keys.top5]).toMutableList()
            val currentHigh = prefs[Keys.highScore] ?: 0

            // Insertar score y mantener top-5
            currentTop.add(newScore)
            currentTop.sortDescending()
            val nextTop = currentTop.take(5)

            val nextHigh = max(currentHigh, newScore)

            // Guardar siempre top y high (simple y robusto)
            prefs[Keys.top5] = encodeTop5(nextTop)
            prefs[Keys.highScore] = nextHigh
        }
    }

    private fun encodeTop5(list: List<Int>): String {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }

    private fun decodeTop5(json: String?): List<Int> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val v = arr.optInt(i, 0)
                    if (v > 0) add(v)
                }
            }.sortedDescending().take(5)
        }.getOrElse { emptyList() }
    }
}
