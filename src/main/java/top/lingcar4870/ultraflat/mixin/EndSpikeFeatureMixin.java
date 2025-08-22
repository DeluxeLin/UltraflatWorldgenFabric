package top.lingcar4870.ultraflat.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import net.minecraft.world.gen.feature.EndSpikeFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.lingcar4870.ultraflat.worldgen.UltraflatChunkGenerator;

@Mixin(EndSpikeFeature.class)
public abstract class EndSpikeFeatureMixin extends Feature<EndSpikeFeatureConfig> {
    @Shadow protected abstract void generateSpike(ServerWorldAccess world, Random random, EndSpikeFeatureConfig config, EndSpikeFeature.Spike spike);

    @Unique
    private static final int ULTRAFLAT$SPIKE_TOP = 63;

    public EndSpikeFeatureMixin(Codec<EndSpikeFeatureConfig> configCodec) {
        super(configCodec);
        throw new AssertionError("Should not happen");
    }

    @Redirect(method = "generate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/feature/EndSpikeFeature;generateSpike(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/world/gen/feature/EndSpikeFeatureConfig;Lnet/minecraft/world/gen/feature/EndSpikeFeature$Spike;)V"))
    private void injectGenerate(EndSpikeFeature instance, ServerWorldAccess world, Random random, EndSpikeFeatureConfig config, EndSpikeFeature.Spike spike, @Local(argsOnly = true) FeatureContext<?> context) {
        if (!(context.getGenerator() instanceof UltraflatChunkGenerator)) {
            this.generateSpike(world, random, config, spike);
            return;
        }

        int i = spike.getRadius();

        for (BlockPos blockPos : BlockPos.iterate(
                new BlockPos(spike.getCenterX() - i, world.getBottomY(), spike.getCenterZ() - i),
                new BlockPos(spike.getCenterX() + i, ULTRAFLAT$SPIKE_TOP, spike.getCenterZ() + i)
        )) {
            if (blockPos.getSquaredDistance(spike.getCenterX(), blockPos.getY(), spike.getCenterZ()) <= i * i + 1 && blockPos.getY() <= ULTRAFLAT$SPIKE_TOP) {
                this.setBlockState(world, blockPos, Blocks.OBSIDIAN.getDefaultState());
            } else if (blockPos.getY() > 65) {
                this.setBlockState(world, blockPos, Blocks.AIR.getDefaultState());
            }
        }

        this.ultraflat$$spikePostProcess(world, random, config, spike);
    }

    @Unique
    private void ultraflat$$spikePostProcess(ServerWorldAccess world, Random random, EndSpikeFeatureConfig config, EndSpikeFeature.Spike spike) {
        if (spike.isGuarded()) {
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            for (int m = -2; m <= 2; m++) {
                for (int n = -2; n <= 2; n++) {
                    for (int o = 0; o <= 3; o++) {
                        boolean bl = MathHelper.abs(m) == 2;
                        boolean bl2 = MathHelper.abs(n) == 2;
                        boolean bl3 = o == 3;
                        if (bl || bl2 || bl3) {
                            boolean bl4 = m == -2 || m == 2 || bl3;
                            boolean bl5 = n == -2 || n == 2 || bl3;
                            BlockState blockState = Blocks.IRON_BARS
                                    .getDefaultState()
                                    .with(PaneBlock.NORTH, bl4 && n != -2)
                                    .with(PaneBlock.SOUTH, bl4 && n != 2)
                                    .with(PaneBlock.WEST, bl5 && m != -2)
                                    .with(PaneBlock.EAST, bl5 && m != 2);
                            this.setBlockState(world, mutable.set(spike.getCenterX() + m, ULTRAFLAT$SPIKE_TOP + 1 + o, spike.getCenterZ() + n), blockState);
                        }
                    }
                }
            }
        }

        EndCrystalEntity endCrystalEntity = EntityType.END_CRYSTAL.create(world.toServerWorld(), SpawnReason.STRUCTURE);
        if (endCrystalEntity != null) {
            endCrystalEntity.setBeamTarget(config.getPos());
            endCrystalEntity.setInvulnerable(config.isCrystalInvulnerable());
            endCrystalEntity.refreshPositionAndAngles(spike.getCenterX() + 0.5, ULTRAFLAT$SPIKE_TOP + 1, spike.getCenterZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
            world.spawnEntity(endCrystalEntity);
            BlockPos blockPosx = endCrystalEntity.getBlockPos();
            this.setBlockState(world, blockPosx.down(), Blocks.BEDROCK.getDefaultState());
            this.setBlockState(world, blockPosx, FireBlock.getState(world, blockPosx));
        }
    }
}
