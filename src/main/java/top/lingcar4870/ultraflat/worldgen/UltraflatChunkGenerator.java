package top.lingcar4870.ultraflat.worldgen;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.*;
import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.StructureWeightSampler;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.densityfunction.DensityFunctions;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import top.lingcar4870.ultraflat.mixin.ChunkNoiseSamplerInvoker;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class UltraflatChunkGenerator extends ChunkGenerator {
    public static final MapCodec<UltraflatChunkGenerator> CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter((generator) -> generator.biomeSource),
                    ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings").forGetter((generator) -> generator.settings),
                    RegistryCodecs.entryList(RegistryKeys.STRUCTURE_SET).lenientOptionalFieldOf("structures").forGetter((generator) -> generator.structures))
                    .apply(instance, instance.stable(UltraflatChunkGenerator::new)));

    private final RegistryEntry<ChunkGeneratorSettings> settings;
    private final Supplier<AquiferSampler.FluidLevelSampler> fluidLevelSampler;
    private final Optional<RegistryEntryList<StructureSet>> structures;
    private UltraflatSurfaceBuilder surfaceBuilder;

    public UltraflatChunkGenerator(BiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> settings, Optional<RegistryEntryList<StructureSet>> structures) {
        super(biomeSource);
        this.settings = settings;
        this.fluidLevelSampler = Suppliers.memoize(() -> createFluidLevelSampler(settings.value()));
        this.structures = structures;
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
        if (!SharedConstants.isOutsideGenerationArea(chunk.getPos())) {
            HeightContext heightContext = new HeightContext(this, region);
            this.buildSurface(chunk, heightContext, noiseConfig, structures, region.getBiomeAccess(), region.getRegistryManager().getOrThrow(RegistryKeys.BIOME), Blender.getBlender(region));
        }
    }

    private void buildSurface(Chunk chunk, HeightContext heightContext, NoiseConfig noiseConfig, StructureAccessor structureAccessor, BiomeAccess biomeAccess, Registry<Biome> biomeRegistry, Blender blender) {
        ChunkNoiseSampler chunkNoiseSampler = chunk.getOrCreateChunkNoiseSampler((chunkx) -> this.createChunkNoiseSampler(chunkx, structureAccessor, blender, noiseConfig));
        ChunkGeneratorSettings chunkGeneratorSettings = this.settings.value();
        this.getSurfaceBuilder(noiseConfig).buildSurface(noiseConfig, biomeAccess, biomeRegistry, chunkGeneratorSettings.usesLegacyRandom(), heightContext, chunk, chunkNoiseSampler, chunkGeneratorSettings.surfaceRule());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void populateEntities(ChunkRegion region) {
        if (!this.settings.value().mobGenerationDisabled()) {
            ChunkPos chunkPos = region.getCenterPos();
            RegistryEntry<Biome> registryEntry = region.getBiome(chunkPos.getStartPos().withY(region.getTopYInclusive()));
            ChunkRandom chunkRandom = new ChunkRandom(new CheckedRandom(RandomSeed.getSeed()));
            chunkRandom.setPopulationSeed(region.getSeed(), chunkPos.getStartX(), chunkPos.getStartZ());
            SpawnHelper.populateEntities(region, registryEntry, chunkPos, chunkRandom);
        }
    }

    @Override
    public int getWorldHeight() {
        return 384;
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        BlockPos.Mutable pos = new BlockPos.Mutable();
        ChunkPos chunkPos = chunk.getPos();
        BlockState m, n;

        for (int i = 0; i <= 15; ++i) {
            for (int j = 0; j <= 15; ++j) {
                m = this.settings.value().defaultBlock();
                n = this.settings.value().defaultBlock();

                chunk.setBlockState(pos.set(i, 61, j), Blocks.BEDROCK.getDefaultState());
                chunk.setBlockState(pos.set(i, 62, j), this.settings.value().defaultBlock());
                if (noiseConfig.getNoiseRouter().finalDensity().sample(new DensityFunction.UnblendedNoisePos(chunkPos.getStartX() + i, 64, chunkPos.getStartZ() + j)) < 0) {
                    n = this.settings.value().defaultFluid();
                    if (noiseConfig.getNoiseRouter().finalDensity().sample(new DensityFunction.UnblendedNoisePos(chunkPos.getStartX() + i, 63, chunkPos.getStartZ() + j)) < -0.2) {
                        m = this.settings.value().defaultFluid();
                    }
                }

                chunk.setBlockState(pos.set(i, 63, j), m);
                chunk.setBlockState(pos.set(i, 64, j), n);
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 64;
    }

    public RegistryEntry<ChunkGeneratorSettings> getSettings() {
        return settings;
    }

    public SurfaceBuilder getSurfaceBuilder(NoiseConfig noiseConfig) {
        if (this.surfaceBuilder == null) {
            this.surfaceBuilder = new UltraflatSurfaceBuilder(noiseConfig, this.settings.value().defaultBlock(), this.settings.value().seaLevel(), noiseConfig.randomDeriver);
        }

        return surfaceBuilder;
    }

    @Override
    public int getMinimumY() {
        return -64;
    }

    @Override
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
        super.generateFeatures(world, chunk, structureAccessor);
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
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        NoiseRouter noiseRouter = noiseConfig.getNoiseRouter();
        DensityFunction.UnblendedNoisePos unblendedNoisePos = new DensityFunction.UnblendedNoisePos(pos.getX(), pos.getY(), pos.getZ());
        double d = noiseRouter.ridges().sample(unblendedNoisePos);
        text.add(
                "NoiseRouter T: "
                        + decimalFormat.format(noiseRouter.temperature().sample(unblendedNoisePos))
                        + " V: "
                        + decimalFormat.format(noiseRouter.vegetation().sample(unblendedNoisePos))
                        + " C: "
                        + decimalFormat.format(noiseRouter.continents().sample(unblendedNoisePos))
                        + " E: "
                        + decimalFormat.format(noiseRouter.erosion().sample(unblendedNoisePos))
                        + " D: "
                        + decimalFormat.format(noiseRouter.depth().sample(unblendedNoisePos))
                        + " W: "
                        + decimalFormat.format(d)
                        + " PV: "
                        + decimalFormat.format(DensityFunctions.getPeaksValleysNoise((float)d))
                        + " AS: "
                        + decimalFormat.format(noiseRouter.initialDensityWithoutJaggedness().sample(unblendedNoisePos))
                        + " N: "
                        + decimalFormat.format(noiseRouter.finalDensity().sample(unblendedNoisePos))
        );
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

    @Override
    // TODO
    public StructurePlacementCalculator createStructurePlacementCalculator(RegistryWrapper<StructureSet> structureSetRegistry, NoiseConfig noiseConfig, long seed) {
        Stream<RegistryEntry<StructureSet>> stream = this.structures.map(RegistryEntryList::stream).orElseGet(() ->
                structureSetRegistry.streamEntries().map((structureEntry) -> structureEntry));
        return StructurePlacementCalculator.create(noiseConfig, seed, this.biomeSource, stream);
    }
}
