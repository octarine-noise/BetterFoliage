package mods.betterfoliage.client.gui;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import mods.betterfoliage.client.util.RenderUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cpw.mods.fml.client.config.ConfigGuiType;
import cpw.mods.fml.client.config.DummyConfigElement;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.GuiConfigEntries.CategoryEntry;
import cpw.mods.fml.client.config.IConfigElement;


public abstract class SelectListConfigEntry<T> extends CategoryEntry {

    List<ItemWrapperElement> children;
    List<Integer> notFoundIdList;
    
    @SuppressWarnings("unchecked")
    public SelectListConfigEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement<?> configElement) {
        super(owningScreen, owningEntryList, configElement);
        RenderUtils.stripTooltipDefaultText(toolTip);
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
    
    @SuppressWarnings("rawtypes")
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
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean saveConfigElement() {
        boolean requiresRestart = ((GuiConfig) childScreen).entryList.saveConfigElements();
        
        Set<Integer> idSet = Sets.newHashSet();
        for (ItemWrapperElement child : children)
            if (Boolean.TRUE.equals(child.getCurrentValue()))
                idSet.add(getItemId(child.item));
        
        idSet.addAll(notFoundIdList);
        List<Integer> result = Lists.newArrayList(idSet);
        Collections.sort(result);
        configElement.set(result.toArray());
        
        return requiresRestart;
    }

    public class ItemWrapperElement extends DummyConfigElement<Boolean> implements IConfigElement<Boolean> {

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
        
        public Boolean getCurrentValue() {
            return (Boolean) value;
        }

        @Override
        public void set(Boolean value) {
            this.value = value;
        }
        
        public void setDefault(Boolean value) {
            this.defaultValue = value;
        }
    }
}
