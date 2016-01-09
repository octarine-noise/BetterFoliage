package mods.betterfoliage.client.gui

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.fml.client.IModGuiFactory
import net.minecraftforge.fml.client.config.GuiConfig

class ConfigGuiFactory : IModGuiFactory {

    override fun mainConfigGuiClass() = ConfigGuiBetterFoliage::class.java
    override fun runtimeGuiCategories() = hashSetOf<IModGuiFactory.RuntimeOptionCategoryElement>()
    override fun getHandlerFor(element: IModGuiFactory.RuntimeOptionCategoryElement?) = null
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
