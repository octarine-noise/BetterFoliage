package mods.betterfoliage.loader

import cpw.mods.fml.relauncher.FMLLaunchHandler
import cpw.mods.fml.relauncher.IFMLLoadingPlugin
import mods.octarinecore.metaprog.*
import org.objectweb.asm.Opcodes.*

@IFMLLoadingPlugin.TransformerExclusions(
    "mods.betterfoliage.loader",
    "mods.octarinecore.metaprog",
    "kotlin",
    "mods.betterfoliage.kotlin"
)
class BetterFoliageLoader : ASMPlugin(BetterFoliageTransformer::class.java)

class BetterFoliageTransformer : Transformer() {

    init {
        if (FMLLaunchHandler.side().isClient) setupClient()
    }

    fun setupClient() {
        // where: RenderBlocks.renderBlockByRenderType()
        // what: invoke BF code to overrule the return value of Block.getRenderType()
        // why: allows us to use custom block renderers for any block, without touching block code
        transformMethod(Refs.renderBlockByRenderType) {
            find(varinsn(ISTORE, 5))?.insertAfter {
                log.info("Applying block render type override")
                varinsn(ALOAD, 0)
                getField(Refs.blockAccess)
                varinsn(ILOAD, 2)
                varinsn(ILOAD, 3)
                varinsn(ILOAD, 4)
                varinsn(ALOAD, 1)
                varinsn(ILOAD, 5)
                invokeStatic(Refs.getRenderTypeOverride)
                varinsn(ISTORE, 5)
            } ?: log.warn("Failed to apply block render type override!")
        }

        // where: WorldClient.doVoidFogParticles(), right before the end of the loop
        // what: invoke BF code for every random display tick
        // why: allows us to catch random display ticks, without touching block code
        transformMethod(Refs.doVoidFogParticles) {
            find(IINC)?.insertBefore {
                log.info("Applying random display tick call hook")
                varinsn(ALOAD, 10)
                varinsn(ALOAD, 0)
                varinsn(ILOAD, 7)
                varinsn(ILOAD, 8)
                varinsn(ILOAD, 9)
                invokeStatic(Refs.onRandomDisplayTick)
            } ?: log.warn("Failed to apply random display tick call hook!")
        }

        // where: shadersmodcore.client.Shaders.pushEntity()
        // what: invoke BF code to overrule block data
        // why: allows us to change the block ID seen by shader programs
        transformMethod(Refs.pushEntity) {
            find(IASTORE)?.insertBefore {
                log.info("Applying Shaders.pushEntity() block id override")
                varinsn(ALOAD, 1)
                invokeStatic(Refs.getBlockIdOverride)
            } ?: log.warn("Failed to apply Shaders.pushEntity() block id override!")
        }

        // where: Block.getAmbientOcclusionLightValue()
        // what: invoke BF code to overrule AO transparency value
        // why: allows us to have light behave properly on non-solid log blocks without
        //      messing with isOpaqueBlock(), which could have gameplay effects
        transformMethod(Refs.getAmbientOcclusionLightValue) {
            find(FRETURN)?.insertBefore {
                log.info("Applying Block.getAmbientOcclusionLightValue() override")
                varinsn(ALOAD, 0)
                invokeStatic(Refs.getAmbientOcclusionLightValueOverride)
            } ?: log.warn("Failed to apply Block.getAmbientOcclusionLightValue() override!")
        }

        // where: Block.getUseNeighborBrightness()
        // what: invoke BF code to overrule _useNeighborBrightness_
        // why: allows us to have light behave properly on non-solid log blocks
        transformMethod(Refs.getUseNeighborBrightness) {
            find(IRETURN)?.insertBefore {
                log.info("Applying Block.getUseNeighborBrightness() override")
                varinsn(ALOAD, 0)
                invokeStatic(Refs.getUseNeighborBrightnessOverride)
            } ?: log.warn("Failed to apply Block.getUseNeighborBrightness() override!")
        }

        // where: Block.shouldSideBeRendered()
        // what: invoke BF code to overrule condition
        // why: allows us to make log blocks non-solid without
        //      messing with isOpaqueBlock(), which could have gameplay effects
        transformMethod(Refs.shouldSideBeRendered) {
            find(IRETURN)?.insertBefore {
                log.info("Applying Block.shouldSideBeRendered() override")
                varinsn(ALOAD, 1)
                varinsn(ILOAD, 2)
                varinsn(ILOAD, 3)
                varinsn(ILOAD, 4)
                varinsn(ILOAD, 5)
                invokeStatic(Refs.shouldRenderBlockSideOverride)
            } ?: log.warn("Failed to apply Block.shouldSideBeRendered() override!")
        }
    }
}