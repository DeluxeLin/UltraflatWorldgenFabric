package top.lingcar4870.ultraflat.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.BlockColumn;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.lingcar4870.ultraflat.worldgen.UltraflatSurfaceBuilder;

@Mixin(SurfaceBuilder.class)
public abstract class SurfaceBuilderMixin {
    @Shadow @Final private DoublePerlinNoiseSampler icebergSurfaceNoise;
    @Shadow @Final private DoublePerlinNoiseSampler icebergPillarNoise;
    @Shadow @Final private DoublePerlinNoiseSampler icebergPillarRoofNoise;
    @Shadow @Final private int seaLevel;
    @Shadow @Final private RandomSplitter randomDeriver;
    @Shadow @Final private static BlockState SNOW_BLOCK;
    @Shadow @Final private static BlockState PACKED_ICE;
    @Shadow @Final private DoublePerlinNoiseSampler badlandsSurfaceNoise;
    @Shadow @Final private DoublePerlinNoiseSampler badlandsPillarNoise;
    @Shadow @Final private DoublePerlinNoiseSampler badlandsPillarRoofNoise;
    @Shadow @Final private BlockState[] terracottaBands;
    @Shadow @Final private DoublePerlinNoiseSampler terracottaBandsOffsetNoise;
    @Shadow @Final private DoublePerlinNoiseSampler surfaceNoise;

    @Shadow protected abstract BlockState getTerracottaBlock(int x, int y, int z);

    @Inject(method = "placeIceberg", at = @At("HEAD"), cancellable = true)
    private void injectPlaceIcebergs(int minY, Biome biome, BlockColumn column, BlockPos.Mutable mutablePos, int x, int z, int surfaceY, CallbackInfo ci) {
        if (!((SurfaceBuilder)(Object) this instanceof UltraflatSurfaceBuilder)) {
            return;
        }

        double e = Math.min(Math.abs(this.icebergSurfaceNoise.sample(x, 0.0, z) * 8.25), this.icebergPillarNoise.sample(x * 1.28, 0.0, z * 1.28) * 15.0);
        if (!(e <= 1.8)) {
            double h = Math.abs(this.icebergPillarRoofNoise.sample(x * 1.17, 0.0, z * 1.17) * 1.5);
            double i = Math.min(e * e * 1.2, Math.ceil(h * 40.0) + 14.0);
            if (biome.shouldGenerateLowerFrozenOceanSurface(mutablePos.set(x, this.seaLevel, z), this.seaLevel)) {
                i -= 2.0;
            }

            double j;
            if (i > 2.0) {
                j = this.seaLevel - i - 7.0;
                i += this.seaLevel;
            } else {
                i = 0.0;
                j = 0.0;
            }

            double k = i;
            Random random = this.randomDeriver.split(x, 0, z);
            int l = 2 + random.nextInt(4);
            int m = this.seaLevel + random.nextInt(10) + 5;
            int n = 0;
            for (int o = Math.max(surfaceY, (int)i + 1); o >= minY; o--) {
                if (column.getState(64).isAir() && 64 < (int) k && random.nextDouble() > 0.01
                        || column.getState(64).isOf(Blocks.WATER) && 64 > (int) j && j != 0.0 && random.nextDouble() > 0.15) {
                    if (n + 5 <= l && o > m) {
                        column.setState(64, SNOW_BLOCK);
                        n++;
                    } else {
                        column.setState(64, PACKED_ICE);
                    }
                }
            }
        }
        ci.cancel();
    }

    @Inject(method = "placeBadlandsPillar", at = @At("HEAD"), cancellable = true)
    private void injectPlaceBadlandsPillar(BlockColumn column, int x, int z, int surfaceY, HeightLimitView chunk, CallbackInfo ci) {
        if (! ((SurfaceBuilder)(Object) this instanceof UltraflatSurfaceBuilder)) {
            return;
        }

        double d = 0.2;
        double e = Math.min(Math.abs(this.badlandsSurfaceNoise.sample(x, 0.0, z) * 8.25), this.badlandsPillarNoise.sample(x * d, 0.0, z * d) * 15.0);
        if (!(e <= 0.0)) {
            double f = 0.75;
            double g = 1.5;
            double h = Math.abs(this.badlandsPillarRoofNoise.sample(x * f, 0.0, z * f) * g);
            double i = 64.0 + Math.min(e * e * 2.5, Math.ceil(h * 50.0) + 24.0);
            int j = MathHelper.floor(i);
            if (surfaceY <= j) {
                if (!column.getState(64).isOf(Blocks.WATER)) {
                    column.setState(64, this.getTerracottaBlock(x, j, z));
                }
            }
        }
        ci.cancel();
    }

    @Inject(method = "getTerracottaBlock", at = @At("HEAD"), cancellable = true)
    private void injectGetTerracottaBlock(int x, int y, int z, CallbackInfoReturnable<BlockState> cir) {
        if (!((SurfaceBuilder)(Object) this instanceof UltraflatSurfaceBuilder)) {
            return;
        }

        int i = (int) Math.round(this.terracottaBandsOffsetNoise.sample(x, 0.0, z) * 4.0);
        int j = (int) Math.round(this.surfaceNoise.sample(x, 64, z) * 15.0) % 10;

        BlockState ret = this.terracottaBands[(y + i + j) % this.terracottaBands.length];
        cir.setReturnValue(ret);
    }
}
