package top.lingcar4870.ultraflat.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.BlockColumn;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.lingcar4870.ultraflat.worldgen.UltraflatSurfaceBuilder;

@Mixin(SurfaceBuilder.class)
public class SurfaceBuilderMixin {
    @Shadow @Final private DoublePerlinNoiseSampler icebergSurfaceNoise;
    @Shadow @Final private DoublePerlinNoiseSampler icebergPillarNoise;
    @Shadow @Final private DoublePerlinNoiseSampler icebergPillarRoofNoise;
    @Shadow @Final private int seaLevel;
    @Shadow @Final private RandomSplitter randomDeriver;
    @Shadow @Final private static BlockState SNOW_BLOCK;
    @Shadow @Final private static BlockState PACKED_ICE;

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
                    if (n + 4 <= l && o > m) {
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
}
