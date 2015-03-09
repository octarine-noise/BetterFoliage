package mods.betterfoliage.loader.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.VarInsnNode;

import mods.betterfoliage.loader.AbstractClassTransformer;
import mods.betterfoliage.loader.AbstractMethodTransformer;


public class BetterFoliageTransformer extends AbstractClassTransformer {

    public BetterFoliageTransformer() {
        final Logger log = LogManager.getLogger(BetterFoliageTransformer.class.getSimpleName());
        
        // where: RenderBlocks.renderBlockByRenderType()
        // what: invoke code to overrule the return value of Block.getRenderType()
        // why: allows us to use custom block renderers for any block, without touching block code
        methodTransformers.put(CodeRefs.mRenderBlockByRenderType, new AbstractMethodTransformer() {
            @Override
            public void transform() {
                log.info("Applying block render type override");
                insertAfter(matchVarInsn(Opcodes.ISTORE, 5), 
                            new VarInsnNode(Opcodes.ALOAD, 0),
                            createGetField(CodeRefs.fBlockAccess),
                            new VarInsnNode(Opcodes.ILOAD, 2),
                            new VarInsnNode(Opcodes.ILOAD, 3),
                            new VarInsnNode(Opcodes.ILOAD, 4),
                            new VarInsnNode(Opcodes.ALOAD, 1),
                            new VarInsnNode(Opcodes.ILOAD, 5),
                            createInvokeStatic(CodeRefs.mGetRenderTypeOverride),
                            new VarInsnNode(Opcodes.ISTORE, 5));
            }
        });
        
        // where: WorldClient.doVoidFogParticles(), right before the end of the loop
        // what: invoke code for every random display tick
        // why: allows us to catch random display ticks, without touching block code
        methodTransformers.put(CodeRefs.mDoVoidFogParticles, new AbstractMethodTransformer() {
            @Override
            public void transform() {
                log.info("Applying random display tick call hook");
                insertBefore(matchOpcode(Opcodes.IINC),
                             new VarInsnNode(Opcodes.ALOAD, 10),
                             new VarInsnNode(Opcodes.ALOAD, 0),
                             new VarInsnNode(Opcodes.ILOAD, 7),
                             new VarInsnNode(Opcodes.ILOAD, 8),
                             new VarInsnNode(Opcodes.ILOAD, 9),
                             createInvokeStatic(CodeRefs.mOnRandomDisplayTick));
            }
        });
        
        // where: shadersmodcore.client.Shaders.pushEntity()
        // what: invoke code to overrule block data
        // why: allows us to change the block ID seen by shader programs
        methodTransformers.put(CodeRefs.mPushEntity, new AbstractMethodTransformer() {
            @Override
            public void transform() {
                log.info("Applying Shaders.pushEntity() block id override");
                insertBefore(matchOpcode(Opcodes.IASTORE),
                             new VarInsnNode(Opcodes.ALOAD, 1),
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
                             new VarInsnNode(Opcodes.ILOAD, 2),
                             new VarInsnNode(Opcodes.ILOAD, 3),
                             new VarInsnNode(Opcodes.ILOAD, 4),
                             new VarInsnNode(Opcodes.ILOAD, 5),
                             createInvokeStatic(CodeRefs.mShouldRenderBlockSideOverride));
            }
        });
        
        
    }
}
