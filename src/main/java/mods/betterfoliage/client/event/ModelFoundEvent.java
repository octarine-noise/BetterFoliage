package mods.betterfoliage.client.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelFoundEvent extends Event {

    public ModelBakery modelBakery;
    
    public IBlockState blockState;
    public ModelResourceLocation modelLocation;
    public Iterable<IModel> modelWithParents;
    
    public ModelFoundEvent(ModelBakery modelBakery, IBlockState blockState, ModelResourceLocation modelLocation, Iterable<IModel> modelWithParents) {
        super();
        this.modelBakery = modelBakery;
        this.blockState = blockState;
        this.modelLocation = modelLocation;
        this.modelWithParents = modelWithParents;
    }
    
    
}
