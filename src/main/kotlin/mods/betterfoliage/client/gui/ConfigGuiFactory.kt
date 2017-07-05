package mods.betterfoliage.client.gui

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.fml.client.IModGuiFactory
import net.minecraftforge.fml.client.config.GuiConfig

class ConfigGuiFactory : IModGuiFactory {

    override fun initialize(minecraftInstance: Minecraft?) { }
    override fun hasConfigGui() = true
    override fun runtimeGuiCategories() = hashSetOf<IModGuiFactory.RuntimeOptionCategoryElement>()
    override fun createConfigGui(parentScreen: GuiScreen?) = createBFConfigGui(parentScreen)

    companion object {
        @JvmStatic
        fun createBFConfigGui(parentScreen: GuiScreen?) = GuiConfig(
            parentScreen,
            Config.rootGuiElements,
            BetterFoliageMod.MOD_ID,
            null,
            false,
            false,
            BetterFoliageMod.MOD_NAME
        )
    }
}
