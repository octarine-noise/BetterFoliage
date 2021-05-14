@file:Suppress("NOTHING_TO_INLINE")
package mods.betterfoliage.util

import mods.betterfoliage.BetterFoliage
import net.minecraft.text.LiteralText
import net.minecraft.text.Style
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.lang.Math.*
import kotlin.reflect.KProperty

const val PI2 = 2.0 * PI

/** Strip the given prefix off the start of the string, if present */
inline fun String.stripStart(str: String, ignoreCase: Boolean = true) = if (startsWith(str, ignoreCase)) substring(str.length) else this
inline fun String.stripEnd(str: String, ignoreCase: Boolean = true) = if (endsWith(str, ignoreCase)) substring(0, length - str.length) else this

/** Strip the given prefix off the start of the resource path, if present */
inline fun Identifier.stripStart(str: String) = Identifier(namespace, path.stripStart(str))
inline fun Identifier.stripEnd(str: String) = Identifier(namespace, path.stripEnd(str))

/**
 * Property-level delegate backed by a [ThreadLocal].
 *
 * @param[init] Lambda to get initial value
 */
class ThreadLocalDelegate<T>(init: () -> T) {
    var tlVal = ThreadLocal.withInitial(init)
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = tlVal.get()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) { tlVal.set(value) }
}

/** Call the supplied lambda and return its result, or the given default value if an exception is thrown. */
fun <T> tryDefault(default: T, work: ()->T) = try { work() } catch (e: Throwable) { default }

/**
 * Return this [Double] value if it lies between the two limits. If outside, return the
 * minimum/maximum value correspondingly.
 */
fun Double.minmax(minVal: Double, maxVal: Double) = min(max(this, minVal), maxVal)

/**
 * Return this [Int] value if it lies between the two limits. If outside, return the
 * minimum/maximum value correspondingly.
 */
fun Int.minmax(minVal: Int, maxVal: Int) = min(max(this, minVal), maxVal)

fun nextPowerOf2(x: Int): Int {
    return 1 shl (if (x == 0) 0 else 32 - Integer.numberOfLeadingZeros(x - 1))
}

/**
 * Check if the Chunk containing the given [BlockPos] is loaded.
 * Works for both [World] and [ChunkCache] (vanilla and OptiFine) instances.
 */
//fun IWorldReader.isBlockLoaded(pos: BlockPos) = when {
//    this is World -> isBlockLoaded(pos, false)
//    this is RenderChunkCache -> isworld.isBlockLoaded(pos, false)
//    Refs.OptifineChunkCache.isInstance(this) -> (Refs.CCOFChunkCache.get(this) as ChunkCache).world.isBlockLoaded(pos, false)
//    else -> false
//}

@Suppress("LeakingThis")
abstract class HasLogger {
    val logger = BetterFoliage.logger(this)
    val detailLogger = BetterFoliage.detailLogger(this)
}

fun textComponent(msg: String, color: Formatting = Formatting.GRAY): LiteralText {
    val style = Style.EMPTY.withColor(color)
    return LiteralText(msg).apply { this.style = style }
}