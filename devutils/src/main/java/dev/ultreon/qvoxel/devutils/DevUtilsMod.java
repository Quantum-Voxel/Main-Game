package dev.ultreon.qvoxel.devutils;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevUtilsMod implements ModInitializer {
    public static final String MOD_ID = "qvoxel-devutils";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Quantum Voxel DevUtils mod initialized!");
    }
}