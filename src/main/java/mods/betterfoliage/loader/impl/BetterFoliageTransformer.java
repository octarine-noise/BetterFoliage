package mods.betterfoliage.loader.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import mods.betterfoliage.loader.AbstractClassTransformer;
import mods.betterfoliage.loader.AbstractMethodTransformer;


public class BetterFoliageTransformer extends AbstractClassTransformer {

    public BetterFoliageTransformer() {
        final Logger log = LogManager.getLogger(BetterFoliageTransformer.class.getSimpleName());
        final boolean optifinePresent = isOptifinePresent();

        if (isServerSide()) return;
        
        // where: WorldClient.doVoidFogParticles(), right before the end of the loop
        // what: invoke code for every random display tick
        // why: allows us to catch random display ticks, without touching block code
        methodTransformers.put(CodeRefs.mDoVoidFogParticles, new AbstractMethodTransformer() {
            @Override
            public void transform() {
                log.info("Applying random display tick call hook");
                insertBefore(matchOpcode(Opcodes.IINC),
                             new VarInsnNode(Opcodes.ALOAD, 0),
                             new VarInsnNode(Opcodes.ALOAD, 13),
                             new VarInsnNode(Opcodes.ALOAD, optifinePresent ? 8 : 12),
                             createInvokeStatic(CodeRefs.mOnRandomDisplayTick));
            }
        });
        
        // where: ModelLoader.setupModelRegistry(), right before the textures are loaded
        // what: invoke handler code with ModelLoader instance
        // why: allows us to iterate the unbaked models in ModelLoader in time to register textures 
        methodTransformers.put(CodeRefs.mSetupModelRegistry, new AbstractMethodTransformer() {
            @Override
            public void transform() {
                log.info("Applying ModelLoader lifecycle callback");
                insertAfter(matchInvokeName("addAll"),
                            new VarInsnNode(Opcodes.ALOAD, 0),
                            createInvokeStatic(CodeRefs.mOnAfterLoadModelDefinitions));
            }
        });
        
        // where: RenderChunk.rebuildChunk()
        // what: replace call to BlockRendererDispatcher.renderBlock()
        // why: allows us to perform additional rendering for each block
        // what: invoke code to overrule result of Block.canRenderInLayer()
        // why: allows us to render transparent quads for blocks which are only on the SOLID layer
        methodTransformers.put(CodeRefs.mRebuildChunk, new AbstractMethodTransformer() {
            @Override
            public void transform() {
                log.info("Applying RenderChunk block layer override");
                replace(matchInvokeMethod(CodeRefs.mRenderBlock),
                        new VarInsnNode(Opcodes.ALOAD, optifinePresent ? 21 : 18),
                        createInvokeStatic(CodeRefs.mRenderWorldBlock)
                );
                if (optifinePresent) {
                    insertBefore(matchVarInsn(Opcodes.ISTORE, 22),
                                 new InsnNode(Opcodes.POP),
                                 new VarInsnNode(Opcodes.ALOAD, 17),
                                 new VarInsnNode(Opcodes.ALOAD, 21),
                                 createInvokeStatic(CodeRefs.mCanRenderBlockInLayer)
                    );
                } else {
                    replace(matchInvokeMethod(CodeRefs.mCanRenderInLayer),
                            createInvokeStatic(CodeRefs.mCanRenderBlockInLayer)
                    );
                }
            }
        });
        
        // where: shadersmod.client.SVertexBuilder.pushEntity()
        // what: invoke code to overrule block data
        // why: allows us to change the block ID seen by shader programs
        methodTransformers.put(CodeRefs.mPushEntity_S, new AbstractMethodTransformer() {
            @Override
            public void transform() {
                log.info("Applying SVertexBuilder.pushEntity() block ID override");
                insertBefore(matchVarInsn(Opcodes.ISTORE, 8),
                             new VarInsnNode(Opcodes.ALOAD, 0),
                             createInvokeStatic(CodeRefs.mGetBlockIdOverride));
            }
        });
        
        // where: Block.getAmbientOcclusionLightValue()
        // what: invoke code to overrule AO transparency value
        // why: allows us to have light behave properly on non-solid log blocks without
        //      messing with isOpaqueBlock(), which could have gameplay effects
        methodTransformers.put(CodeRefs.mGetAmbientOcclusionLightValue, new AbstractMethodTransformer() {
            @Override
            public void transform() {
                log.info("Applying Block.getAmbientOcclusionLightValue() override");
                insertBefore(matchOpcode(Opcodes.FRETURN),
                             new VarInsnNode(Opcodes.ALOAD, 0),
                             createInvokeStatic(CodeRefs.mGetAmbientOcclusionLightValueOverride));
            }
            
        });
        
        // where: Block.getUseNeighborBrightness()
        // what: invoke code to overrule <b>useNeighborBrightness</b>
        // why: allows us to have light behave properly on non-solid log blocks
        methodTransformers.put(CodeRefs.mGetUseNeighborBrightness, new AbstractMethodTransformer() {
            @Override
            public void transform() {
                log.info("Applying Block.getUseNeighborBrightness() override");
                insertBefore(matchOpcode(Opcodes.IRETURN),
                             new VarInsnNode(Opcodes.ALOAD, 0),
                             createInvokeStatic(CodeRefs.mGetUseNeighborBrightnessOverride));
            }
            
        });
        
        // where: Block.shouldSideBeRendered()
        // what: invoke code to overrule condition
        // why: allows us to make log blocks non-solid without
        //      messing with isOpaqueBlock(), which could have gameplay effects
        methodTransformers.put(CodeRefs.mShouldSideBeRendered, new AbstractMethodTransformer() {
            @Override
            public void transform() {
                log.info("Applying Block.shouldSideBeRendered() override");
                insertBefore(matchOpcode(Opcodes.IRETURN),
                             new VarInsnNode(Opcodes.ALOAD, 1),
                             new VarInsnNode(Opcodes.ALOAD, 2),
                             new VarInsnNode(Opcodes.ALOAD, 3),
                             createInvokeStatic(CodeRefs.mShouldRenderBlockSideOverride));
            }
        });
    }
    
    protected boolean isOptifinePresent() {
        try {
            @SuppressWarnings("unused")
            Class<?> optifine = Class.forName("optifine.OptiFineClassTransformer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
}
