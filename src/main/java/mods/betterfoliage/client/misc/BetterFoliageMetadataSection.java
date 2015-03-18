package mods.betterfoliage.client.misc;

import java.lang.reflect.Type;

import mods.betterfoliage.BetterFoliage;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.JsonUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class BetterFoliageMetadataSection implements IMetadataSection {

	public boolean rotation;
	
	public static class BetterFoliageMetadataSerializer implements IMetadataSectionSerializer {

		@Override
		public String getSectionName() {
			return BetterFoliage.METADATA_SECTION;
		}

		@Override
		public BetterFoliageMetadataSection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			BetterFoliageMetadataSection result = new BetterFoliageMetadataSection();
			result.rotation = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(json.getAsJsonObject(), "rotation", true);
			return result;
		}

	}
}
