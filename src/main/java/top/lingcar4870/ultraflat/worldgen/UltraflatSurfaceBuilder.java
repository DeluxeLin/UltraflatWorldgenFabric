package top.lingcar4870.ultraflat.worldgen;

import net.minecraft.block.BlockState;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;

public class UltraflatSurfaceBuilder extends SurfaceBuilder {
    public UltraflatSurfaceBuilder(NoiseConfig noiseConfig, BlockState defaultState, int seaLevel, RandomSplitter randomDeriver) {
        super(noiseConfig, defaultState, seaLevel, randomDeriver);
    }

    @Override
    public void buildSurface(NoiseConfig noiseConfig, BiomeAccess biomeAccess, Registry<Biome> biomeRegistry, boolean useLegacyRandom, HeightContext heightContext, Chunk chunk, ChunkNoiseSampler chunkNoiseSampler, MaterialRules.MaterialRule materialRule) {
        super.buildSurface(noiseConfig, biomeAccess, biomeRegistry, useLegacyRandom, heightContext, chunk, chunkNoiseSampler, materialRule);
    }
}
