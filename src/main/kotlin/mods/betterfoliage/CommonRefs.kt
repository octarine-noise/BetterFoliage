package mods.betterfoliage

import it.unimi.dsi.fastutil.ints.IntList
import mods.betterfoliage.util.YarnHelper
import net.minecraft.client.texture.Sprite
import net.minecraft.world.World

val VertexFormat_offsets = YarnHelper.requiredField<IntList>("net.minecraft.class_293", "field_1597", "Lit/unimi/dsi/fastutil/ints/IntList;")
val BakedQuad_sprite = YarnHelper.requiredField<Sprite>("net.minecraft.class_777", "field_4176", "Lnet/minecraft/class_1058;")
val WorldChunk_world = YarnHelper.requiredField<World>("net.minecraft.class_2818", "field_12858", "Lnet/minecraft/class_1937;")
val ChunkRendererRegion_world = YarnHelper.requiredField<World>("net.minecraft.class_853", "field_4490", "Lnet/minecraft/class_1937;")