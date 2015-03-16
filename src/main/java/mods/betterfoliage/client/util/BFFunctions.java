package mods.betterfoliage.client.util;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Function;

@SideOnly(Side.CLIENT)
public class BFFunctions {

    private BFFunctions() {}
    
    public static Function<Block, Map<IBlockState, ModelResourceLocation>> getBlockStateMappings(final Map<Block, IStateMapper> stateMappers) {
        return new Function<Block, Map<IBlockState,ModelResourceLocation>>() {
            @SuppressWarnings("unchecked")
            @Override
            @Nullable
            public Map<IBlockState, ModelResourceLocation> apply(@Nullable Block input) {
                IStateMapper blockStateMapper = stateMappers.get(input);
                if (blockStateMapper != null) return blockStateMapper.putStateModelLocations(input);
                return new DefaultStateMapper().putStateModelLocations(input);
            }
        };
    }
    
    public static <K, V> Function<Map<K, V>, Iterable<Map.Entry<K, V>>> asEntries() {
        return new Function<Map<K,V>, Iterable<Entry<K,V>>>() {
            @Override
            @Nullable
            public Iterable<Entry<K, V>> apply(@Nullable Map<K, V> input) {
                return input.entrySet();
            }
        };
    }
    
}
