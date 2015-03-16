package mods.betterfoliage.client.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

@SideOnly(Side.CLIENT)
public class BiomeUtils {

    /** Hide constructor */
    private BiomeUtils() {}

    public static List<BiomeGenBase> getAllBiomes() {
        List<BiomeGenBase> biomes = Lists.newArrayList(Collections2.filter(Arrays.asList(BiomeGenBase.getBiomeGenArray()), Predicates.notNull()));
        Collections.sort(biomes, new Comparator<BiomeGenBase>() {
            @Override
            public int compare(BiomeGenBase o1, BiomeGenBase o2) {
                return o1.biomeName.compareTo(o2.biomeName);
            }
        });
        return biomes;
    }

    public static Predicate<BiomeGenBase> biomeIdFilter(final List<Integer> biomeIdList) {
        return new Predicate<BiomeGenBase>() {
            public boolean apply(BiomeGenBase biome) {
                return biomeIdList.contains(biome.biomeID);
            }
        };
    }

    public static Function<BiomeGenBase, Integer> biomeIdTransform() {
        return new Function<BiomeGenBase, Integer>() {
            public Integer apply(BiomeGenBase input) {
                return input.biomeID;
            }
        };
    }

    public static Predicate<BiomeGenBase> biomeTempRainFilter(final Float minTemp, final Float maxTemp, final Float minRain, final Float maxRain) {
        return new Predicate<BiomeGenBase>() {
            public boolean apply(BiomeGenBase biome) {
                if (minTemp != null && biome.temperature < minTemp) return false;
                if (maxTemp != null && biome.temperature > maxTemp) return false;
                if (minRain != null && biome.rainfall < minRain) return false;
                if (maxRain != null && biome.rainfall > maxRain) return false;
                return true;
            }
        };
    }

    public static Predicate<BiomeGenBase> biomeClassFilter(final Class<?>... classList) {
        return new Predicate<BiomeGenBase>() {
            public boolean apply(BiomeGenBase biome) {
                for (Class<?> clazz : classList)
                    if (clazz.isAssignableFrom(biome.getClass()) || clazz.equals(biome.getClass()))
                        return true;
                return false;
            }
        };
    }

    public static Predicate<BiomeGenBase> biomeClassNameFilter(final String... names) {
        return new Predicate<BiomeGenBase>() {
            public boolean apply(BiomeGenBase biome) {
                for (String name : names) if (biome.getClass().getName().toLowerCase().contains(name.toLowerCase())) return true;
                return false;
            }
        };
    }

    public static List<Integer> getFilteredBiomeIds(Collection<BiomeGenBase> biomes, Predicate<BiomeGenBase> filter) {
        return Lists.newArrayList(Collections2.transform(Collections2.filter(biomes, filter), biomeIdTransform()));
    }
    
    
}
