package top.lingcar4870.ultraflat.worldgen.feature;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import top.lingcar4870.ultraflat.Ultraflat;

public class UltraflatOrePlacedFeatures {
    public static final RegistryKey<PlacedFeature> ORE_ANCIENT_DEBRIS_LARGE = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Ultraflat.MODID,"ore_ancient_debris_large"));
    public static final RegistryKey<PlacedFeature> ORE_BLACKSTONE = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Ultraflat.MODID,"ore_blackstone"));
    public static final RegistryKey<PlacedFeature> ORE_DIAMOND = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Ultraflat.MODID,"ore_diamond"));
    public static final RegistryKey<PlacedFeature> ORE_DIAMOND_BURIED = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Ultraflat.MODID,"ore_diamond_buried"));
    public static final RegistryKey<PlacedFeature> ORE_DIAMOND_LARGE = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Ultraflat.MODID,"ore_diamond_large"));
    public static final RegistryKey<PlacedFeature> ORE_DIAMOND_MEDIUM = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Ultraflat.MODID,"ore_diamond_medium"));
    public static final RegistryKey<PlacedFeature> ORE_LAPIS = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Ultraflat.MODID,"ore_lapis"));
    public static final RegistryKey<PlacedFeature> ORE_LAPIS_BURIED = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Ultraflat.MODID,"ore_lapis_buried"));
    public static final RegistryKey<PlacedFeature> ORE_MAGMA = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Ultraflat.MODID,"ore_magma"));
    public static final RegistryKey<PlacedFeature> ORE_REDSTONE = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Ultraflat.MODID,"ore_redstone"));
    public static final RegistryKey<PlacedFeature> ORE_REDSTONE_LOWER = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Ultraflat.MODID,"ore_redstone_lower"));
    public static final RegistryKey<PlacedFeature> ORE_TUFF = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Ultraflat.MODID,"ore_tuff"));

    public static void registerOres() {
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, ORE_DIAMOND);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, ORE_DIAMOND_LARGE);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, ORE_DIAMOND_MEDIUM);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, ORE_DIAMOND_BURIED);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, ORE_REDSTONE);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, ORE_REDSTONE_LOWER);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, ORE_LAPIS);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, ORE_LAPIS_BURIED);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, ORE_TUFF);

        BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(), GenerationStep.Feature.UNDERGROUND_ORES, ORE_MAGMA);
        BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(), GenerationStep.Feature.UNDERGROUND_ORES, ORE_BLACKSTONE);
        BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(), GenerationStep.Feature.UNDERGROUND_ORES, ORE_ANCIENT_DEBRIS_LARGE);
    }
}
