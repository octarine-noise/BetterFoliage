package mods.betterfoliage.common.config;

import net.minecraft.client.resources.I18n;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;


public class AlternateTextBooleanEntry extends GuiConfigEntries.ButtonEntry {

    protected final boolean beforeValue;
    protected boolean       currentValue;
    
    public AlternateTextBooleanEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement<Boolean> configElement)
    {
        super(owningScreen, owningEntryList, configElement);
        this.beforeValue = Boolean.valueOf(configElement.get().toString());
        this.currentValue = beforeValue;
        this.btnValue.enabled = enabled();
        updateValueButtonText();
    }
    
    @Override
    public void updateValueButtonText()
    {
        this.btnValue.displayString = I18n.format(configElement.getLanguageKey() + "." + String.valueOf(currentValue));
//        btnValue.packedFGColour = currentValue ? GuiUtils.getColorCode('2', true) : GuiUtils.getColorCode('4', true);
    }

    @Override
    public void valueButtonPressed(int slotIndex)
    {
        if (enabled())
            currentValue = !currentValue;
    }

    @Override
    public boolean isDefault()
    {
        return currentValue == Boolean.valueOf(configElement.getDefault().toString());
    }

    @Override
    public void setToDefault()
    {
        if (enabled())
        {
            currentValue = Boolean.valueOf(configElement.getDefault().toString());
            updateValueButtonText();
        }
    }

    @Override
    public boolean isChanged()
    {
        return currentValue != beforeValue;
    }

    @Override
    public void undoChanges()
    {
        if (enabled())
        {
            currentValue = beforeValue;
            updateValueButtonText();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean saveConfigElement()
    {
        if (enabled() && isChanged())
        {
            configElement.set(currentValue);
            return configElement.requiresMcRestart();
        }
        return false;
    }

    @Override
    public Boolean getCurrentValue()
    {
        return currentValue;
    }

    @Override
    public Boolean[] getCurrentValues()
    {
        return new Boolean[] { getCurrentValue() };
    }
}
