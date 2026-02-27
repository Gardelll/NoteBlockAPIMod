package com.xxmicloxx.NoteBlockAPI.forge;

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NoteBlockAPI.MOD_ID)
public final class NoteBlockAPIForge {
    public NoteBlockAPIForge(FMLJavaModLoadingContext context) {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(NoteBlockAPI.MOD_ID, context.getModEventBus());

        // Run our common setup.
        NoteBlockAPI.init();
    }
}
