package com.example.dodgeblocks.game

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class GameStatus { RUNNING, PAUSED, GAME_OVER }

data class Player(
    val x: Float,
    val y: Float,
    val r: Float
)

data class Block(
    val x: Float,
    val y: Float,
    val size: Float,
    val vy: Float
)

data class DifficultyParams(
    val baseSpawnIntervalSec: Float,
    val blockSizePxMin: Float,
    val blockSizePxMax: Float,
    val speedPxPerSecMin: Float,
    val speedPxPerSecMax: Float
)

data class Rng(val state: Long) {
    // LCG simple: determinista, sin dependencias externas.
    fun next(): Rng = Rng(state * 6364136223846793005L + 1442695040888963407L)

    fun nextFloat01(): Pair<Float, Rng> {
        val n = next()
        // Tomar 24 bits altos para float [0,1)
        val bits = ((n.state ushr 40) and 0xFFFFFF).toInt()
        val f = bits / 16777216f
        return f to n
    }

    fun nextFloat(min: Float, max: Float): Pair<Float, Rng> {
        val (u, n) = nextFloat01()
        return (min + (max - min) * u) to n
    }

    fun nextInt(min: Int, maxInclusive: Int): Pair<Int, Rng> {
        val (u, n) = nextFloat01()
        val v = min + (u * (maxInclusive - min + 1)).toInt().coerceIn(0, (maxInclusive - min))
        return v to n
    }
}

data class GameWorld(
    val status: GameStatus,
    val widthPx: Float,
    val heightPx: Float,
    val elapsedSec: Float,
    val score: Int,
    val player: Player,
    val blocks: List<Block>,
    val spawnAccumulatorSec: Float,
    val rng: Rng
) {
    companion object {
        fun initial(
            widthPx: Float,
            heightPx: Float,
            playerRadiusPx: Float,
            seed: Long
        ): GameWorld {
            val p = Player(
                x = widthPx / 2f,
                y = heightPx * 0.82f,
                r = playerRadiusPx
            )
            return GameWorld(
                status = GameStatus.RUNNING,
                widthPx = widthPx,
                heightPx = heightPx,
                elapsedSec = 0f,
                score = 0,
                player = p,
                blocks = emptyList(),
                spawnAccumulatorSec = 0f,
                rng = Rng(seed)
            )
        }
    }
}

data class StepResult(
    val world: GameWorld,
    val collidedNow: Boolean
)

/**
 * Paso de simulación:
 * - dtSec viene limitado fuera a <= 0.033
 * - dificultad progresiva: cada ~30s sube multiplicador de velocidad y ligeramente el spawn rate.
 * - colisión círculo vs rectángulo (closest point) correcta.
 */
fun stepWorld(world: GameWorld, dtSec: Float, params: DifficultyParams): StepResult {
    if (world.status != GameStatus.RUNNING) return StepResult(world, collidedNow = false)

    val nextElapsed = world.elapsedSec + dtSec

    // Progresión por "tiers" cada 30s
    val tier = floor(nextElapsed / 30f).toInt().coerceAtLeast(0)
    val speedMul = 1f + tier * 0.12f
    val spawnMul = 1f + tier * 0.05f

    val effectiveSpawnInterval = params.baseSpawnIntervalSec / spawnMul

    var rng = world.rng
    var acc = world.spawnAccumulatorSec + dtSec
    val spawned = mutableListOf<Block>()

    while (acc >= effectiveSpawnInterval) {
        acc -= effectiveSpawnInterval

        val (size, rng1) = rng.nextFloat(params.blockSizePxMin, params.blockSizePxMax)
        rng = rng1

        val maxX = max(0f, world.widthPx - size)
        val (x, rng2) = rng.nextFloat(0f, maxX)
        rng = rng2

        val (vyBase, rng3) = rng.nextFloat(params.speedPxPerSecMin, params.speedPxPerSecMax)
        rng = rng3

        spawned += Block(
            x = x,
            y = -size,
            size = size,
            vy = vyBase * speedMul
        )
    }

    // Mover bloques
    val moved = (world.blocks + spawned).map { b ->
        b.copy(y = b.y + b.vy * dtSec)
    }.filter { b ->
        // Mantener mientras pueda colisionar (un poco más abajo del borde)
        b.y <= world.heightPx + b.size * 1.2f
    }

    // Actualizar score: segundos redondeados
    val nextScore = nextElapsed.roundToInt().coerceAtLeast(0)

    // Colisión
    val collided = moved.any { b -> circleRectCollides(world.player, b) }

    val nextWorld = if (collided) {
        world.copy(
            status = GameStatus.GAME_OVER,
            elapsedSec = nextElapsed,
            score = nextScore,
            blocks = moved,
            spawnAccumulatorSec = acc,
            rng = rng
        )
    } else {
        world.copy(
            elapsedSec = nextElapsed,
            score = nextScore,
            blocks = moved,
            spawnAccumulatorSec = acc,
            rng = rng
        )
    }

    return StepResult(nextWorld, collidedNow = collided)
}

fun movePlayerBy(world: GameWorld, dx: Float, dy: Float): GameWorld {
    val p = world.player
    val nx = (p.x + dx).coerceIn(p.r, world.widthPx - p.r)
    val ny = (p.y + dy).coerceIn(p.r, world.heightPx - p.r)
    return world.copy(player = p.copy(x = nx, y = ny))
}

fun setPaused(world: GameWorld, paused: Boolean): GameWorld {
    val next = when {
        world.status == GameStatus.GAME_OVER -> world.status
        paused -> GameStatus.PAUSED
        else -> GameStatus.RUNNING
    }
    return world.copy(status = next)
}

private fun circleRectCollides(c: Player, r: Block): Boolean {
    // Rect: [x, x+size] x [y, y+size]
    val closestX = clamp(c.x, r.x, r.x + r.size)
    val closestY = clamp(c.y, r.y, r.y + r.size)
    val dx = c.x - closestX
    val dy = c.y - closestY
    return (dx * dx + dy * dy) <= (c.r * c.r)
}

private fun clamp(v: Float, lo: Float, hi: Float): Float = min(hi, max(lo, v))

/**
 * Heurística para suavizar un "pico" de dt (por ejemplo al reanudar):
 * limitar dt a 0.033 se hace fuera; aquí solo exponemos un helper si hiciera falta.
 */
fun safeDt(dtSec: Float): Float {
    val absDt = abs(dtSec)
    return absDt.coerceIn(0f, 0.033f)
}
