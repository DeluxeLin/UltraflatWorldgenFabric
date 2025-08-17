package top.lingcar4870.ultraflat;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import top.lingcar4870.ultraflat.worldgen.UltraflatChunkGenerator;

public class Ultraflat implements ModInitializer {
    public static final String MODID = "ultraflat";
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing...");
        Registry.register(Registries.CHUNK_GENERATOR, Identifier.of(MODID, "ultra_flat"), UltraflatChunkGenerator.CODEC);
    }
}
