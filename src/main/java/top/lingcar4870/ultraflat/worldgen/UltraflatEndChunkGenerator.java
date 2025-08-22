package top.lingcar4870.ultraflat.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.noise.NoiseConfig;
import top.lingcar4870.ultraflat.mixin.ChunkNoiseSamplerInvoker;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class UltraflatEndChunkGenerator extends UltraflatChunkGenerator {
    private static final BlockState AIR = Blocks.AIR.getDefaultState();

    public static final MapCodec<UltraflatEndChunkGenerator> CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter((generator) -> generator.biomeSource),
                            ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings").forGetter((generator) -> generator.settings),
                            RegistryCodecs.entryList(RegistryKeys.STRUCTURE_SET).lenientOptionalFieldOf("structures").forGetter((generator) -> generator.structures))
                    .apply(instance, instance.stable(UltraflatEndChunkGenerator::new)));

    private final RegistryEntry<ChunkGeneratorSettings> settings;
    private final Optional<RegistryEntryList<StructureSet>> structures;

    public UltraflatEndChunkGenerator(BiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> settings, Optional<RegistryEntryList<StructureSet>> structures) {
        super(biomeSource, settings, structures);
        this.settings = settings;
        this.structures = structures;
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        GenerationShapeConfig generationShapeConfig = this.settings.value().generationShapeConfig().trimHeight(chunk.getHeightLimitView());
        int i = generationShapeConfig.minimumY();
        int j = MathHelper.floorDiv(i, generationShapeConfig.verticalCellBlockCount());
        int k = MathHelper.floorDiv(generationShapeConfig.height(), generationShapeConfig.verticalCellBlockCount());
        return k <= 0 ? CompletableFuture.completedFuture(chunk) : CompletableFuture.supplyAsync(() -> {
            ChunkSection section1 = chunk.getSection(3);
            section1.lock();

            Chunk var20;
            try {
                var20 = this.populateNoise(blender, structureAccessor, noiseConfig, chunk, j, k);
            } finally {
                section1.unlock();
            }

            return var20;
        }, Util.getMainWorkerExecutor().named("wgen_fill_noise"));
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private Chunk populateNoise(Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk, int minimumCellY, int cellHeight) {
        ChunkNoiseSampler chunkNoiseSampler = chunk.getOrCreateChunkNoiseSampler(
                chunkx -> this.createChunkNoiseSampler(chunkx, structureAccessor, blender, noiseConfig)
        );
        Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        ChunkPos chunkPos = chunk.getPos();
        int i = chunkPos.getStartX();
        int j = chunkPos.getStartZ();
        chunkNoiseSampler.sampleStartDensity();
        int k = ((ChunkNoiseSamplerInvoker) chunkNoiseSampler).invokeGetHorizontalCellBlockCount();
        int l = ((ChunkNoiseSamplerInvoker) chunkNoiseSampler).invokeGetVerticalCellBlockCount();
        int m = 16 / k;
        int n = 16 / k;

        for (int o = 0; o < m; o++) {
            chunkNoiseSampler.sampleEndDensity(o);

            for (int p = 0; p < n; p++) {
                ChunkSection chunkSection = chunk.getSection(3);

                for (int r = cellHeight - 1; r >= 0; r--) {
                    chunkNoiseSampler.onSampledCellCorners(r, p);

                    for (int s = l - 1; s >= 0; s--) {
                        int t = (minimumCellY + r) * l + s;
                        int u = t & 15;

                        double d = (double)s / l;
                        chunkNoiseSampler.interpolateY(t, d);

                        for (int w = 0; w < k; w++) {
                            int x = i + o * k + w;
                            int y = x & 15;
                            double e = (double)w / k;
                            chunkNoiseSampler.interpolateX(x, e);

                            for (int z = 0; z < k; z++) {
                                int aa = j + p * k + z;
                                int ab = aa & 15;
                                double f = (double)z / k;
                                chunkNoiseSampler.interpolateZ(aa, f);
                                BlockState blockState = ((ChunkNoiseSamplerInvoker) chunkNoiseSampler).invokeSampleBlockState();
                                if (blockState == null) {
                                    blockState = this.settings.value().defaultBlock();
                                }

                                if (blockState != AIR && !SharedConstants.isOutsideGenerationArea(chunk.getPos())) {
                                    chunkSection.setBlockState(y, u, ab, blockState, false);
                                    heightmap.trackUpdate(y, t, ab, blockState);
                                    heightmap2.trackUpdate(y, t, ab, blockState);
                                }
                            }
                        }
                    }
                }
            }

            chunkNoiseSampler.swapBuffers();
        }

        chunkNoiseSampler.stopInterpolation();
        return chunk;
    }
}
