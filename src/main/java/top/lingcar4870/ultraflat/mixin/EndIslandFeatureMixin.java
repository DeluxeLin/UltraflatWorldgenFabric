package top.lingcar4870.ultraflat.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.EndIslandFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.lingcar4870.ultraflat.worldgen.UltraflatChunkGenerator;

@Mixin(EndIslandFeature.class)
public class EndIslandFeatureMixin {

    @ModifyVariable(method = "generate", at = @At("STORE"))
    private BlockPos injectGenerate(BlockPos origin, @Local(argsOnly = true) FeatureContext<?> context) {
        if (!(context.getGenerator() instanceof UltraflatChunkGenerator)) {
            return origin;
        }

        return origin.withY(63);
    }
}
