package mods.betterfoliage

import it.unimi.dsi.fastutil.ints.IntList
import mods.betterfoliage.util.YarnHelper
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.util.WeightedPicker
import net.minecraft.world.World

val WeightedBakedModel_totalWeight = YarnHelper.requiredField<Int>("net.minecraft.class_1097", "field_5433", "I")
val WeightedBakedModel_models = YarnHelper.requiredField<List<WeightedPicker.Entry>>("net.minecraft.class_1097", "field_5434", "Ljava/util/List;")
val WeightedBakedModelEntry_model = YarnHelper.requiredField<BakedModel>("net.minecraft.class_1097\$class_1099", "field_5437", "Lnet/minecraft/class_1087;")
val WeightedPickerEntry_weight = YarnHelper.requiredField<Int>("net.minecraft.class_3549\$class_3550", "field_15774", "I")
val VertexFormat_offsets = YarnHelper.requiredField<IntList>("net.minecraft.class_293", "field_1597", "Lit/unimi/dsi/fastutil/ints/IntList;")
val BakedQuad_sprite = YarnHelper.requiredField<Sprite>("net.minecraft.class_777", "field_4176", "Lnet/minecraft/class_1058;")
val WorldChunk_world = YarnHelper.requiredField<World>("net.minecraft.class_2818", "field_12858", "Lnet/minecraft/class_1937;")
val ChunkRendererRegion_world = YarnHelper.requiredField<World>("net.minecraft.class_853", "field_4490", "Lnet/minecraft/class_1937;")