package mods.betterfoliage.client.gui;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import mods.betterfoliage.client.util.MiscUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SideOnly(Side.CLIENT)
public abstract class SelectListConfigEntry<T> extends CategoryEntry {

    List<ItemWrapperElement> children;
    List<Integer> notFoundIdList;
    
    @SuppressWarnings("unchecked")
    public SelectListConfigEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement);
        MiscUtils.stripTooltipDefaultText(toolTip);
    }

    @Override
    protected GuiScreen buildChildScreen()
    {
        return new GuiConfig(this.owningScreen, createChildElements(), this.owningScreen.modID, 
                owningScreen.allRequireWorldRestart || this.configElement.requiresWorldRestart(), 
                owningScreen.allRequireMcRestart || this.configElement.requiresMcRestart(), this.owningScreen.title, 
                ((this.owningScreen.titleLine2 == null ? "" : this.owningScreen.titleLine2) + " > " + this.name));
    }
    
    protected abstract List<T> getBaseSet(String qualifiedName);
    protected abstract List<T> getDefaultSelected(String qualifiedName);
    protected abstract int getItemId(T item);
    protected abstract String getItemName(T item);
    protected abstract String getTooltipLangKey(String qualifiedName);
    
    protected List<IConfigElement> createChildElements() {
        children = Lists.newArrayList();
        
        List<Integer> idList = Lists.newArrayList();
        for (Object id : configElement.getList()) idList.add((Integer) id);
        
        List<T> defaults = getDefaultSelected(configElement.getName());
        for(T item : getBaseSet(configElement.getQualifiedName())) {
            children.add(new ItemWrapperElement(item, defaults.contains(item), idList.contains(getItemId(item))));
            idList.remove(new Integer(getItemId(item)));
        }
        
        notFoundIdList = idList;
        List<IConfigElement> result = Lists.newArrayList();
        result.addAll(children);
        return result;
    }
    
    @Override
    public boolean saveConfigElement() {
        boolean requiresRestart = ((GuiConfig) childScreen).entryList.saveConfigElements();
        
        Set<Integer> idSet = Sets.newHashSet();
        for (ItemWrapperElement child : children) if (child.isSelected()) idSet.add(getItemId(child.item));
        
        idSet.addAll(notFoundIdList);
        List<Integer> result = Lists.newArrayList(idSet);
        Collections.sort(result);
        configElement.set(result.toArray());
        
        return requiresRestart;
    }

    public class ItemWrapperElement extends DummyConfigElement implements IConfigElement {

        public T item;
        
        public ItemWrapperElement(T item, boolean defaultValue, boolean currentValue) {
            super(getItemName(item), defaultValue, ConfigGuiType.BOOLEAN, getItemName(item));
            set(currentValue);
            this.item = item;
        }

        @Override
        public String getComment() {
            return I18n.format(getTooltipLangKey(configElement.getQualifiedName()), EnumChatFormatting.GOLD + getItemName(item) + EnumChatFormatting.YELLOW);
        }
        
        public Boolean isSelected() {
            return (Boolean) value;
        }

        @Override
        public void set(Object value) {
            this.value = value;
        }
        
    }
}
