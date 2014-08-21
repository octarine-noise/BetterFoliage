package mods.betterfoliage.client.gui;

import java.util.List;

import mods.betterfoliage.common.config.Config;
import mods.betterfoliage.common.util.BiomeUtils;
import net.minecraft.world.biome.BiomeGenBase;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;


public class BiomeListConfigEntry extends SelectListConfigEntry<BiomeGenBase> {

    public BiomeListConfigEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement<?> configElement) {
        super(owningScreen, owningEntryList, configElement);
    }

    @Override
    protected List<BiomeGenBase> getBaseSet(String name) {
        return BiomeUtils.getAllBiomes();
    }

    @Override
    protected List<BiomeGenBase> getDefaultSelected(String name) {
        if (name.equals("reedBiomeList")) return Lists.newArrayList(Collections2.filter(getBaseSet(name), BiomeUtils.biomeIdFilter(Config.reedBiomeList)));
        if (name.equals("algaeBiomeList")) return Lists.newArrayList(Collections2.filter(getBaseSet(name), BiomeUtils.biomeIdFilter(Config.algaeBiomeList)));
        if (name.equals("coralBiomeList")) return Lists.newArrayList(Collections2.filter(getBaseSet(name), BiomeUtils.biomeIdFilter(Config.coralBiomeList)));
        return ImmutableList.<BiomeGenBase>of();
    }

    @Override
    protected int getItemId(BiomeGenBase item) {
        return item.biomeID;
    }

    @Override
    protected String getItemName(BiomeGenBase item) {
        return item.biomeName;
    }

    @Override
    protected String getTooltipLangKey(String name) {
        if (name.equals("reedBiomeList")) return "betterfoliage.reeds.biomeSelectTooltip";
        if (name.equals("algaeBiomeList")) return "betterfoliage.algae.biomeSelectTooltip";
        if (name.equals("coralBiomeList")) return "betterfoliage.coral.biomeSelectTooltip";
        return "";
    }

}
