package top.lingcar4870.ultraflat.worldgen;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseParametersKeys;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;

public class UltraflatSurfaceBuilder extends SurfaceBuilder {
    public UltraflatSurfaceBuilder(NoiseConfig noiseConfig, BlockState defaultState, int seaLevel, RandomSplitter randomDeriver) {
        super(noiseConfig, defaultState, seaLevel, randomDeriver);
    }

    @Override
    public void buildSurface(NoiseConfig noiseConfig, BiomeAccess biomeAccess, Registry<Biome> biomeRegistry, boolean useLegacyRandom, HeightContext heightContext, Chunk chunk, ChunkNoiseSampler chunkNoiseSampler, MaterialRules.MaterialRule materialRule) {
        super.buildSurface(noiseConfig, biomeAccess, biomeRegistry, useLegacyRandom, heightContext, chunk, chunkNoiseSampler, getCustomMaterialRules(materialRule));
    }

    private static MaterialRules.MaterialRule getCustomMaterialRules(MaterialRules.MaterialRule originRules) {
        MaterialRules.MaterialRule ultraflat = MaterialRules.sequence(
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.BADLANDS, BiomeKeys.ERODED_BADLANDS, BiomeKeys.WOODED_BADLANDS),
                        MaterialRules.condition(
                                MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, 0.15d),
                                MaterialRules.terracottaBands()
                        )
                ),
                MaterialRules.condition(
                        MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, Double.MIN_VALUE, 0.1d),
                        MaterialRules.block(Blocks.DEEPSLATE.getDefaultState())
                )
        );
        return MaterialRules.sequence(originRules, ultraflat);
    }

    @Override
    protected BlockState getTerracottaBlock(int x, int y, int z) {
        return super.getTerracottaBlock(x, y, z);
    }
}
