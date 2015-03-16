package mods.betterfoliage.client.texture.models;

import java.lang.reflect.Field;
import java.util.Collection;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;

/** Support for Forge/vanilla {@link IModel}s (i.e. the {@link ModelBlock} wrappers in {@link ModelLoader}).<br/>
 * Resolves to the texture using a given alias in the model definitions (for example "#all").
 * @author octarine-noise
 *
 */
@SideOnly(Side.CLIENT)
public class VanillaMapping implements IModelTextureMapping {

    public String modelName;
    public String textureName;
    
    public VanillaMapping(String modelName, String textureName) {
        this.modelName = modelName;
        this.textureName = textureName;
    }

    @Override
    @Nullable
    public String apply(@Nullable IModel input) {
        for (ModelBlock vanillaModel : getVanillaModelsDFS(input)) {
            ModelBlock current = vanillaModel;
            boolean modelMatch = false;
            while(current != null) {
                if (current.name.equals(modelName)) modelMatch = true;
                current = current.parent;
            }
            if (modelMatch) {
                String texture = vanillaModel.resolveTextureName(textureName);
                if (texture != null) return texture;
            }
        }
        return null;
    }

    /** Return an {@link Iterable} of all {@link ModelBlock}s in the model hierarchy, going from child to parent. Depth-first, will exhaust all children before moving to new variant. 
     * @param model
     * @return
     */
    protected static Iterable<ModelBlock> getVanillaModelsDFS(IModel model) {
        Collection<ModelBlock> result = Lists.newLinkedList();
        addAllChildren(result, model);
        return result;
    }
    
    //
    // Needed reflection stuff. Gotta love private inner classes
    //
    private static Class<?> classVanillaModelWrapper;
    private static Field fieldVanillaModelBlock;
    private static Class<?> classWeightedPartWrapper;
    private static Field fieldWeightedPartModel;
    private static Class<?> classWeightedRandomModel;
    private static Field fieldWeightedRandomModels;
    
    static {
        try {
            classVanillaModelWrapper = Class.forName("net.minecraftforge.client.model.ModelLoader$VanillaModelWrapper");
            fieldVanillaModelBlock = classVanillaModelWrapper.getDeclaredField("model");
            fieldVanillaModelBlock.setAccessible(true);
            
            classWeightedPartWrapper = Class.forName("net.minecraftforge.client.model.ModelLoader$WeightedPartWrapper");
            fieldWeightedPartModel = classWeightedPartWrapper.getDeclaredField("model");
            fieldWeightedPartModel.setAccessible(true);
            
            classWeightedRandomModel = Class.forName("net.minecraftforge.client.model.ModelLoader$WeightedRandomModel");
            fieldWeightedRandomModels = classWeightedRandomModel.getDeclaredField("models");
            fieldWeightedRandomModels.setAccessible(true);
        } catch (NoSuchFieldException e) {
        } catch (SecurityException e) {
        } catch (ClassNotFoundException e) {
        }
    }
    
    @SuppressWarnings("unchecked")
    protected static void addAllChildren(Collection<ModelBlock> list, IModel model) {
        try {
            if (classVanillaModelWrapper.isInstance(model)) {
                ModelBlock block = (ModelBlock) fieldVanillaModelBlock.get(model);
                list.add(block);
            }
            if (classWeightedPartWrapper.isInstance(model)) {
                IModel wrappedModel = (IModel) fieldWeightedPartModel.get(model);
                addAllChildren(list, wrappedModel);
            }
            if (classWeightedRandomModel.isInstance(model)) {
                Iterable<IModel> models = (Iterable<IModel>) fieldWeightedRandomModels.get(model);
                for (IModel wrappedModel : models) {
                    addAllChildren(list, wrappedModel);
                }
            }
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }
    }
}
