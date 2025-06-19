package com.xxmicloxx.NoteBlockAPI.fabric;

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import net.fabricmc.api.DedicatedServerModInitializer;

public final class NotBlockAPIFabric implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        NoteBlockAPI.init();
    }
}
