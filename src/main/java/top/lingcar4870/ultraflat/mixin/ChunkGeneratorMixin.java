package top.lingcar4870.ultraflat.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.lingcar4870.ultraflat.Ultraflat;
import top.lingcar4870.ultraflat.worldgen.UltraflatChunkGenerator;

import java.util.List;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
    @ModifyVariable(method = "generateFeatures", name = "placedFeature", at = @At("STORE"))
    private PlacedFeature modifyOreFeature(PlacedFeature value, @Local(name = "registry2") Registry<PlacedFeature> registry2) {
        if ((ChunkGenerator)(Object) this instanceof UltraflatChunkGenerator || !registry2.getKey(value).orElse(RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of("null"))).getValue().getNamespace().equals(Ultraflat.MODID)) {
            return value;
        }

        // SHIT
        return new PlacedFeature(RegistryEntry.of(new ConfiguredFeature<>(Feature.NO_OP, new DefaultFeatureConfig())), List.of());
    }
}
