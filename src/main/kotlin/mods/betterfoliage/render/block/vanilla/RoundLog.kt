package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.render.column.ColumnBlockKey
import mods.betterfoliage.resource.discovery.ModelBakeKey
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation

data class RoundLogKey(
    override val axis: Direction.Axis?,
    val barkSprite: ResourceLocation,
    val endSprite: ResourceLocation
) : ColumnBlockKey, ModelBakeKey {
}