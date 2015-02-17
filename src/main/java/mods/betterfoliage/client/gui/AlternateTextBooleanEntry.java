package mods.betterfoliage.client.gui;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AlternateTextBooleanEntry extends GuiConfigEntries.ButtonEntry {

    protected final boolean beforeValue;
    protected boolean       currentValue;
    
    public AlternateTextBooleanEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement)
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
