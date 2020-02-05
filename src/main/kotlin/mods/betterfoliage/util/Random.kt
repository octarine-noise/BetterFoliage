package mods.betterfoliage.util

import net.minecraft.util.math.BlockPos
import kotlin.random.Random

val random = Random(System.nanoTime())

fun randomB() = random.nextBoolean()
fun randomI(min: Int = 0, max: Int = Int.MAX_VALUE) = random.nextInt(min, max)
fun randomL(min: Long = 0, max: Long = Long.MAX_VALUE) = random.nextLong(min, max)
fun randomF(min: Double = 0.0, max: Double = 1.0) = random.nextDouble(min, max).toFloat()
fun randomD(min: Double = 0.0, max: Double = 1.0) = if (min == max) min else random.nextDouble(min, max)

fun semiRandom(x: Int, y: Int, z: Int, seed: Int): Int {
    var value = (x * x + y * y + z * z + x * y + y * z + z * x + (seed * seed))
    value = (3 * x * value + 5 * y * value + 7 * z * value + (11 * seed))
    return value shr 4
}

//fun BlockPos.semiRandom(seed: Int) = semiRandom(x, y, z, seed)