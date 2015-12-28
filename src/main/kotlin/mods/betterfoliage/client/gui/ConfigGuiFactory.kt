package mods.betterfoliage.client.gui

import cpw.mods.fml.client.IModGuiFactory
import cpw.mods.fml.client.IModGuiFactory.RuntimeOptionCategoryElement
import cpw.mods.fml.client.config.GuiConfig
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen

class ConfigGuiFactory : IModGuiFactory {

    override fun mainConfigGuiClass() = ConfigGuiBetterFoliage::class.java
    override fun runtimeGuiCategories() = hashSetOf<RuntimeOptionCategoryElement>()
    override fun getHandlerFor(element: RuntimeOptionCategoryElement?) = null
    override fun initialize(minecraftInstance: Minecraft?) { }

    class ConfigGuiBetterFoliage(parentScreen: GuiScreen?) : GuiConfig(
            parentScreen,
            Config.rootGuiElements,
            BetterFoliageMod.MOD_ID,
            null,
            false,
            false,
            BetterFoliageMod.MOD_NAME
    )
}
