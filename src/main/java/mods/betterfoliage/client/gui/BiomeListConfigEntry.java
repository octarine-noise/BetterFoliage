package mods.betterfoliage.client.gui;

import java.util.List;

import mods.betterfoliage.client.util.BiomeUtils;
import mods.betterfoliage.common.config.Config;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

@SideOnly(Side.CLIENT)
public class BiomeListConfigEntry extends SelectListConfigEntry<BiomeGenBase> {

    public String tooltipKey = "";
    public List<BiomeGenBase> defaultSelected = null;
    
    public BiomeListConfigEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement);
    }

    @Override
    protected List<BiomeGenBase> getBaseSet(String name) {
        return BiomeUtils.getAllBiomes();
    }

    @Override
    protected List<BiomeGenBase> getDefaultSelected(String name) {
        if (defaultSelected == null) {
            Predicate<BiomeGenBase> defaultFilter = Predicates.alwaysFalse();
            if (configElement.getName().equals("reedBiomeList")) {
                defaultFilter = Config.reedBiomeDefaults;
                tooltipKey = "betterfoliage.reeds.biomeSelectTooltip";
            }
            if (configElement.getName().equals("algaeBiomeList")) {
                defaultFilter = Config.algaeBiomeDefaults;
                tooltipKey = "betterfoliage.algae.biomeSelectTooltip";
            }
            if (configElement.getName().equals("coralBiomeList")) {
                defaultFilter = Config.coralBiomeDefaults;
                tooltipKey = "betterfoliage.coral.biomeSelectTooltip";
            }
            defaultSelected = Lists.newArrayList(Collections2.filter(BiomeUtils.getAllBiomes(), defaultFilter));
        }
        return defaultSelected;
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
        return tooltipKey;
    }

}
