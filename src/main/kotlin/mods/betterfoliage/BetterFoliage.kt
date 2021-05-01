package mods.betterfoliage

import mods.betterfoliage.util.textComponent
import mods.octarinecore.client.resource.AsnycSpriteProviderManager
import mods.betterfoliage.resource.generated.GeneratedBlockTexturePack
import mods.betterfoliage.util.Atlas
import net.minecraft.block.BlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.ParticleManager
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.registries.ForgeRegistries
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.simple.SimpleLogger
import org.apache.logging.log4j.util.PropertiesUtil
import java.io.File
import java.io.PrintStream
import java.util.*

object BetterFoliage {
    var log = LogManager.getLogger("BetterFoliage")
    var logDetail = SimpleLogger(
        "BetterFoliage",
        Level.DEBUG,
        false, false, true, false,
        "yyyy-MM-dd HH:mm:ss",
        null,
        PropertiesUtil(Properties()),
        PrintStream(File("logs/betterfoliage.log").apply {
            parentFile.mkdirs()
            if (!exists()) createNewFile()
        })
    )

    val blockSprites = AsnycSpriteProviderManager<ModelBakery>("bf-blocks-extra")
    val particleSprites = AsnycSpriteProviderManager<ParticleManager>("bf-particles-extra")
    val asyncPack = GeneratedBlockTexturePack("bf_gen", "Better Foliage generated assets", logDetail)

    fun getSpriteManager(atlas: Atlas) = when(atlas) {
        Atlas.BLOCKS -> blockSprites
        Atlas.PARTICLES -> particleSprites
    } as AsnycSpriteProviderManager<Any>

    init {
        blockSprites.providers.add(asyncPack)
    }

    fun log(level: Level, msg: String) {
        log.log(level, "[BetterFoliage] $msg")
        logDetail.log(level, msg)
    }

    fun logDetail(msg: String) {
        logDetail.log(Level.DEBUG, msg)
    }

    fun logRenderError(state: BlockState, location: BlockPos) {
        if (state in Client.suppressRenderErrors) return
        Client.suppressRenderErrors.add(state)

        val blockName = ForgeRegistries.BLOCKS.getKey(state.block).toString()
        val blockLoc = "${location.x},${location.y},${location.z}"
        Minecraft.getInstance().ingameGUI.chatGUI.printChatMessage(TranslationTextComponent(
            "betterfoliage.rendererror",
            textComponent(blockName, TextFormatting.GOLD),
            textComponent(blockLoc, TextFormatting.GOLD)
        ))
        logDetail("Error rendering block $state at $blockLoc")
    }
}