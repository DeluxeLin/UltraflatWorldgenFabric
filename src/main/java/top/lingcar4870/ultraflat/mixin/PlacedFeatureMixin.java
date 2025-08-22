package top.lingcar4870.ultraflat.mixin;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.lingcar4870.ultraflat.Ultraflat;
import top.lingcar4870.ultraflat.worldgen.UltraflatChunkGenerator;

@Mixin(PlacedFeature.class)
public abstract class PlacedFeatureMixin {

    @Inject(method = "generate(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private void injectGenerate(StructureWorldAccess world, ChunkGenerator generator, Random random, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        //noinspection ConstantValue
        if (!(generator instanceof UltraflatChunkGenerator) &&
                world.getRegistryManager().getOrThrow(RegistryKeys.PLACED_FEATURE).getKey((PlacedFeature) (Object) this).get().getValue().getNamespace().equals(Ultraflat.MODID)) {
            cir.setReturnValue(true);
        }
    }
}
