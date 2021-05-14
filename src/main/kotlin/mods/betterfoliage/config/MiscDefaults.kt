package mods.betterfoliage.config

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.material.Material
import net.minecraft.world.biome.Biome

val CACTUS_BLOCKS = listOf(Blocks.CACTUS)
val DIRT_BLOCKS = listOf(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL)
val SAND_BLOCKS = listOf(Blocks.SAND, Blocks.RED_SAND)
val NETHERRACK_BLOCKS = listOf(Blocks.NETHERRACK)
val LILYPAD_BLOCKS = listOf(Blocks.LILY_PAD)
val MYCELIUM_BLOCKS = listOf(Blocks.MYCELIUM)

val SALTWATER_BIOMES = listOf(Biome.Category.BEACH, Biome.Category.OCEAN)

val SNOW_MATERIALS = listOf(Material.SNOW_BLOCK, Material.SNOW)
val BlockState.isSnow: Boolean get() = material in SNOW_MATERIALS