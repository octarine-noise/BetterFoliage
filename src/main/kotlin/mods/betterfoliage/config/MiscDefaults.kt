package mods.betterfoliage.config

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.material.Material
import net.minecraft.world.biome.Biome

val SALTWATER_BIOMES = listOf(Biome.Category.BEACH, Biome.Category.OCEAN)

val SNOW_MATERIALS = listOf(Material.TOP_SNOW, Material.SNOW)
val BlockState.isSnow: Boolean get() = material in SNOW_MATERIALS

val ACCEPTED_ROUND_LOG_MATERIALS = listOf(Material.WOOD, Material.GRASS)
