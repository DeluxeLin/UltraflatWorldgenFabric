package top.lingcar4870.ultraflat.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.BasaltColumnsFeature;
import net.minecraft.world.gen.feature.BasaltColumnsFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import top.lingcar4870.ultraflat.worldgen.UltraflatChunkGenerator;

@Mixin(BasaltColumnsFeature.class)
public abstract class BasaltColumnsFeatureMixin extends Feature<BasaltColumnsFeatureConfig> {
    private BasaltColumnsFeatureMixin(Codec<BasaltColumnsFeatureConfig> configCodec) {
        super(configCodec);
        throw new AssertionError("Should not happen");
    }

    @ModifyArg(method = "generate", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/gen/feature/BasaltColumnsFeature;placeBasaltColumn(Lnet/minecraft/world/WorldAccess;ILnet/minecraft/util/math/BlockPos;II)Z"), index = 3)
    private int ultraflatGenerate(int height, @Local(argsOnly = true) FeatureContext<?> context) {
        if (context.getGenerator() instanceof UltraflatChunkGenerator) {
            height = 0;
        }

        return height;
    }
}
