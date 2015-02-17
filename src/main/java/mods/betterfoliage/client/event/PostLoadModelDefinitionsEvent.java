package mods.betterfoliage.client.event;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PostLoadModelDefinitionsEvent extends Event {

    public ModelLoader loader;
    
    public PostLoadModelDefinitionsEvent(ModelLoader loader) {
        this.loader = loader;
    }
}