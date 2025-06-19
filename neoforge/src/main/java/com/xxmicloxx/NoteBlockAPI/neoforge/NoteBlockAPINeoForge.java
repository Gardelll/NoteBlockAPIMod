package com.xxmicloxx.NoteBlockAPI.neoforge;

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = NoteBlockAPI.MOD_ID, dist = Dist.DEDICATED_SERVER)
public final class NoteBlockAPINeoForge {
    public NoteBlockAPINeoForge() {
        // Run our common setup.
        NoteBlockAPI.init();
    }
}
