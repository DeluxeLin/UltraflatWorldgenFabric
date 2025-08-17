package top.lingcar4870.ultraflat.worldgen;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.StructureWeightSampler;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import top.lingcar4870.ultraflat.mixin.ChunkNoiseSamplerInvoker;

import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class UltraflatChunkGenerator extends ChunkGenerator {
    public static final MapCodec<UltraflatChunkGenerator> CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter((generator) -> generator.biomeSource),
                    ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings").forGetter((generator) -> generator.settings))
                    .apply(instance, instance.stable(UltraflatChunkGenerator::new)));
    private final RegistryEntry<ChunkGeneratorSettings> settings;
    private final Supplier<AquiferSampler.FluidLevelSampler> fluidLevelSampler;

    public UltraflatChunkGenerator(BiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> settings) {
        super(biomeSource);
        this.settings = settings;
        this.fluidLevelSampler = Suppliers.memoize(() -> createFluidLevelSampler(settings.value()));
    }

    // TODO
    private static AquiferSampler.FluidLevelSampler createFluidLevelSampler(ChunkGeneratorSettings settings) {
        AquiferSampler.FluidLevel fluidLevel = new AquiferSampler.FluidLevel(-54, Blocks.LAVA.getDefaultState());
        int i = settings.seaLevel();
        AquiferSampler.FluidLevel fluidLevel2 = new AquiferSampler.FluidLevel(i, settings.defaultFluid());
        return (x, y, z) -> y < Math.min(-54, i) ? fluidLevel : fluidLevel2;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk) {
        // NO-OP
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
    }

    @Override
    public void populateEntities(ChunkRegion region) {
    }

    @Override
    public int getWorldHeight() {
        return 384;
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int i = 0; i <= 15; ++i) {
            for (int j = 0; j <= 15; ++j) {
                chunk.setBlockState(pos.set(i, 64, j), Blocks.BEDROCK.getDefaultState());
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinimumY() {
        return -64;
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return this.sampleHeightmap(world, noiseConfig, x, z, null, heightmap.getBlockPredicate()).orElse(world.getBottomY());
    }

    private OptionalInt sampleHeightmap(HeightLimitView world, NoiseConfig noiseConfig, int x, int z, @Nullable MutableObject<VerticalBlockSample> columnSample, @Nullable Predicate<BlockState> stopPredicate) {
        GenerationShapeConfig generationShapeConfig = this.settings.value().generationShapeConfig().trimHeight(world);
        int i = generationShapeConfig.verticalCellBlockCount();
        int j = generationShapeConfig.minimumY();
        int k = MathHelper.floorDiv(j, i);
        int l = MathHelper.floorDiv(generationShapeConfig.height(), i);
        if (l > 0) {
            BlockState[] blockStates;
            if (columnSample == null) {
                blockStates = null;
            } else {
                blockStates = new BlockState[generationShapeConfig.height()];
                columnSample.setValue(new VerticalBlockSample(j, blockStates));
            }

            int m = generationShapeConfig.horizontalCellBlockCount();
            int n = Math.floorDiv(x, m);
            int o = Math.floorDiv(z, m);
            int p = Math.floorMod(x, m);
            int q = Math.floorMod(z, m);
            int r = n * m;
            int s = o * m;
            double d = (double) p / (double) m;
            double e = (double) q / (double) m;
            ChunkNoiseSampler chunkNoiseSampler = new ChunkNoiseSampler(1, noiseConfig, r, s, generationShapeConfig, DensityFunctionTypes.Beardifier.INSTANCE, this.settings.value(), this.fluidLevelSampler.get(), Blender.getNoBlending());
            chunkNoiseSampler.sampleStartDensity();
            chunkNoiseSampler.sampleEndDensity(0);

            for (int t = l - 1; t >= 0; --t) {
                chunkNoiseSampler.onSampledCellCorners(t, 0);

                for (int u = i - 1; u >= 0; --u) {
                    int v = (k + t) * i + u;
                    double f = (double) u / (double) i;
                    chunkNoiseSampler.interpolateY(v, f);
                    chunkNoiseSampler.interpolateX(x, d);
                    chunkNoiseSampler.interpolateZ(z, e);
                    BlockState blockState = ((ChunkNoiseSamplerInvoker) chunkNoiseSampler).invokeSampleBlockState();
                    BlockState blockState2 = blockState == null ? this.settings.value().defaultBlock() : blockState;
                    if (blockStates != null) {
                        int w = t * i + u;
                        blockStates[w] = blockState2;
                    }

                    if (stopPredicate != null && stopPredicate.test(blockState2)) {
                        chunkNoiseSampler.stopInterpolation();
                        return OptionalInt.of(v + 1);
                    }
                }
            }

            chunkNoiseSampler.stopInterpolation();
        }
        return OptionalInt.empty();
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        MutableObject<VerticalBlockSample> mutableObject = new MutableObject<>();
        this.sampleHeightmap(world, noiseConfig, x, z, mutableObject, null);
        return mutableObject.getValue();
    }

    @Override
    public void appendDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
    }

    @Override
    public CompletableFuture<Chunk> populateBiomes(NoiseConfig noiseConfig, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
        return CompletableFuture.supplyAsync(() -> {
            this.populateBiomes(blender, noiseConfig, structureAccessor, chunk);
            return chunk;
        }, Util.getMainWorkerExecutor().named("init_biomes"));
    }

    private void populateBiomes(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        ChunkNoiseSampler chunkNoiseSampler = chunk.getOrCreateChunkNoiseSampler((chunkx) -> this.createChunkNoiseSampler(chunkx, structureAccessor, blender, noiseConfig));
        BiomeSupplier biomeSupplier = BelowZeroRetrogen.getBiomeSupplier(blender.getBiomeSupplier(this.biomeSource), chunk);
        chunk.populateBiomes(biomeSupplier, ((ChunkNoiseSamplerInvoker) chunkNoiseSampler).invokeCreateMultiNoiseSampler(noiseConfig.getNoiseRouter(), this.settings.value().spawnTarget()));
    }

    private ChunkNoiseSampler createChunkNoiseSampler(Chunk chunk, StructureAccessor world, Blender blender, NoiseConfig noiseConfig) {
        return ChunkNoiseSampler.create(chunk, noiseConfig, StructureWeightSampler.createStructureWeightSampler(world, chunk.getPos()), this.settings.value(), this.fluidLevelSampler.get(), blender);
    }
}
