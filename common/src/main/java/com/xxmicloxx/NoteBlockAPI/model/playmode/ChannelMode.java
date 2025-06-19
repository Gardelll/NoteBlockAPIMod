package com.xxmicloxx.NoteBlockAPI.model.playmode;

import com.xxmicloxx.NoteBlockAPI.model.Layer;
import com.xxmicloxx.NoteBlockAPI.model.Note;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Decides how is {@link Note} played to {@link Player}
 */
public abstract class ChannelMode {

    @Deprecated
    public abstract void play(Player player, Vec3 position, float yRot, Song song, Layer layer, Note note,
                              SoundCategory soundCategory, float volume, float pitch);

    public abstract void play(Player player, Vec3 position, float yRot, Song song, Layer layer, Note note,
                              SoundCategory soundCategory, float volume, boolean doTranspose);
}