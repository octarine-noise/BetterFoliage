package mods.betterfoliage.client.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;


public class BiomeListConfigEntry extends SelectListConfigEntry<BiomeGenBase> {

    public static List<BiomeGenBase> reedBiomeList = Lists.newArrayList();
    public static List<BiomeGenBase> algaeBiomeList = Lists.newArrayList();
    public static List<BiomeGenBase> coralBiomeList = Lists.newArrayList();
    
    public BiomeListConfigEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement<?> configElement) {
        super(owningScreen, owningEntryList, configElement);
    }

    @Override
    protected List<BiomeGenBase> getBaseSet(String qualifiedName) {
        List<BiomeGenBase> biomes = Lists.newArrayList(Collections2.filter(Arrays.asList(BiomeGenBase.getBiomeGenArray()), Predicates.notNull()));
        Collections.sort(biomes, new Comparator<BiomeGenBase>() {
            @Override
            public int compare(BiomeGenBase o1, BiomeGenBase o2) {
                return o1.biomeName.compareTo(o2.biomeName);
            }
        });
        return biomes;
    }

    @Override
    protected List<BiomeGenBase> getDefaultSelected(String name) {
        if (name.equals("reedBiomeList")) return reedBiomeList;
        if (name.equals("algaeBiomeList")) return algaeBiomeList;
        if (name.equals("coralBiomeList")) return coralBiomeList;
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
