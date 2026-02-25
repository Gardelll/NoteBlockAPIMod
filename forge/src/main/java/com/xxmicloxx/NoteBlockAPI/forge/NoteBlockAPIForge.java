package com.xxmicloxx.NoteBlockAPI.forge;

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

@Mod(NoteBlockAPI.MOD_ID)
public final class NoteBlockAPIForge {
    public NoteBlockAPIForge(IEventBus modBus) {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(NoteBlockAPI.MOD_ID, modBus);

        // Run our common setup.
        NoteBlockAPI.init();
    }
}
