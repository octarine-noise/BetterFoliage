package mods.betterfoliage.loader

import mods.octarinecore.metaprog.ASMPlugin
import mods.octarinecore.metaprog.Transformer
import mods.octarinecore.metaprog.allAvailable
import net.minecraftforge.fml.relauncher.FMLLaunchHandler
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import org.objectweb.asm.Opcodes.*

@IFMLLoadingPlugin.TransformerExclusions(
    "mods.betterfoliage.loader",
    "mods.octarinecore.metaprog",
    "kotlin",
    "mods.octarinecore.kotlin"
)
class BetterFoliageLoader : ASMPlugin(BetterFoliageTransformer::class.java)

class BetterFoliageTransformer : Transformer() {

    val isOptifinePresent = allAvailable(Refs.OptifineClassTransformer)

    init {
        if (FMLLaunchHandler.side().isClient) setupClient()
    }

    fun setupClient() {
        // where: WorldClient.doVoidFogParticles(), right before the end of the loop
        // what: invoke BF code for every random display tick
        // why: allows us to catch random display ticks, without touching block code
        transformMethod(Refs.doVoidFogParticles) {
            find(IINC)?.insertBefore {
                log.info("Applying random display tick call hook")
                varinsn(ALOAD, 0)
                varinsn(ALOAD, 13)
                varinsn(ALOAD, if (isOptifinePresent) 8 else 12)
                invokeStatic(Refs.onRandomDisplayTick)
            } ?: log.warn("Failed to apply random display tick call hook!")
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
                varinsn(ALOAD, 2)
                varinsn(ALOAD, 3)
                invokeStatic(Refs.shouldRenderBlockSideOverride)
            } ?: log.warn("Failed to apply Block.shouldSideBeRendered() override!")
        }

        // where: ModelLoader.setupModelRegistry(), right before the textures are loaded
        // what: invoke handler code with ModelLoader instance
        // why: allows us to iterate the unbaked models in ModelLoader in time to register textures
        transformMethod(Refs.setupModelRegistry) {
            find(invokeName("addAll"))?.insertAfter {
                log.info("Applying ModelLoader lifecycle callback")
                varinsn(ALOAD, 0)
                invokeStatic(Refs.onAfterLoadModelDefinitions)
            }
        }

        // where: RenderChunk.rebuildChunk()
        // what: replace call to BlockRendererDispatcher.renderBlock()
        // why: allows us to perform additional rendering for each block
        // what: invoke code to overrule result of Block.canRenderInLayer()
        // why: allows us to render transparent quads for blocks which are only on the SOLID layer
        transformMethod(Refs.rebuildChunk) {
            find(invokeRef(Refs.renderBlock))?.replace {
                log.info("Applying RenderChunk block render override")
                varinsn(ALOAD, if (isOptifinePresent) 20 else 18)
                invokeStatic(Refs.renderWorldBlock)
            }
            if (isOptifinePresent) {
                find(varinsn(ISTORE, 21))?.insertBefore {
                    log.info("Applying RenderChunk block layer override")
                    insn(POP)
                    varinsn(ALOAD, 17)
                    varinsn(ALOAD, 20)
                    invokeStatic(Refs.canRenderBlockInLayer)
                }
            } else {
                find(invokeRef(Refs.canRenderInLayer))?.replace {
                    log.info("Applying RenderChunk block layer override")
                    invokeStatic(Refs.canRenderBlockInLayer)
                }
            }
        }

        // where: net.minecraft.client.renderer.BlockModelRenderer$AmbientOcclusionFace
        // what: make constructor public
        // why: use vanilla AO calculation at will without duplicating code
        transformMethod(Refs.AOF_constructor) {
            log.info("Setting AmbientOcclusionFace constructor public")
            makePublic()
        }

        // where: shadersmod.client.SVertexBuilder.pushEntity()
        // what: invoke code to overrule block data
        // why: allows us to change the block ID seen by shader programs
        transformMethod(Refs.pushEntity_state) {
            find(invokeRef(Refs.pushEntity_num))?.insertBefore {
                log.info("Applying SVertexBuilder.pushEntity() block ID override")
                varinsn(ALOAD, 0)
                invokeStatic(Refs.getBlockIdOverride)
            } ?: log.warn("Failed to apply SVertexBuilder.pushEntity() block ID override!")
        }
    }
}