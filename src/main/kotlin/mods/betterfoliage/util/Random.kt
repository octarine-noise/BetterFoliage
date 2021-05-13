package mods.betterfoliage.util

import net.minecraft.util.math.BlockPos
import java.util.Random

val random = Random(System.nanoTime())

fun randomB() = random.nextBoolean()
fun randomI(min: Int = 0, max: Int = Int.MAX_VALUE) = min + random.nextInt(max - min)
fun randomF(min: Float = 0.0f, max: Float = 1.0f) = random.randomF(min, max)
fun randomD(min: Double = 0.0, max: Double = 1.0) = random.randomD(min, max)

fun Random.randomF(min: Float = 0.0f, max: Float = 1.0f) = nextFloat() * (max - min) + min
fun Random.randomF(min: Double = 0.0, max: Double = 1.0) = randomF(min.toFloat(), max.toFloat())
fun Random.randomD(min: Double = 0.0, max: Double = 1.0) = nextDouble() * (max - min) + min

fun semiRandom(x: Int, y: Int, z: Int, seed: Int): Int {
    var value = (x * x + y * y + z * z + x * y + y * z + z * x + (seed * seed))
    value = (3 * x * value + 5 * y * value + 7 * z * value + (11 * seed))
    return value shr 4
}

//fun BlockPos.semiRandom(seed: Int) = semiRandom(x, y, z, seed)