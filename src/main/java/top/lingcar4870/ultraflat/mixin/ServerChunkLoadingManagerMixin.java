package top.lingcar4870.ultraflat.mixin;

import com.mojang.datafixers.DataFixer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import top.lingcar4870.ultraflat.worldgen.UltraflatChunkGenerator;

import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerChunkLoadingManager.class)
public class ServerChunkLoadingManagerMixin {

    @Mutable
    @Shadow @Final private NoiseConfig noiseConfig;

    //fk u moj: if the generator is not NoiseChunkGenerator, it will use all zero noise
    @Inject(method = "<init>", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
    @SuppressWarnings("rawtypes")
    private void injectNoiseConfig(ServerWorld world, LevelStorage.Session session, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor executor, ThreadExecutor mainThreadExecutor, ChunkProvider chunkProvider, ChunkGenerator chunkGenerator, WorldGenerationProgressListener worldGenerationProgressListener, ChunkStatusChangeListener chunkStatusChangeListener, Supplier persistentStateManagerFactory, ChunkTicketManager ticketManager, int viewDistance, boolean dsync, CallbackInfo ci, Path path, DynamicRegistryManager dynamicRegistryManager, long l) {
        if (chunkGenerator instanceof UltraflatChunkGenerator ultraflatChunkGenerator) {
            this.noiseConfig = NoiseConfig.create(ultraflatChunkGenerator.getSettings().value(), dynamicRegistryManager.getOrThrow(RegistryKeys.NOISE_PARAMETERS), l);
        }
    }
}
