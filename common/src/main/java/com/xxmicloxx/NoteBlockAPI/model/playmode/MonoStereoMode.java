package com.xxmicloxx.NoteBlockAPI.model.playmode;

import com.xxmicloxx.NoteBlockAPI.model.CustomInstrument;
import com.xxmicloxx.NoteBlockAPI.model.Layer;
import com.xxmicloxx.NoteBlockAPI.model.Note;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory;
import com.xxmicloxx.NoteBlockAPI.utils.CompatibilityUtils;
import com.xxmicloxx.NoteBlockAPI.utils.InstrumentUtils;
import com.xxmicloxx.NoteBlockAPI.utils.NoteUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Ignores panning of {@link Note} and {@link Layer} from nbs format and plays mono {@link Note} as fake stereo at fixed offset from {@link Player} head.
 */
public class MonoStereoMode extends ChannelMode{

    private float distance = 2;

    @Override
    public void play(Player player, Vec3 position, float yRot, Song song, Layer layer, Note note, SoundCategory soundCategory, float volume, float pitch) {
        if (InstrumentUtils.isCustomInstrument(note.getInstrument())) {
            CustomInstrument instrument = song.getCustomInstruments()[note.getInstrument() - InstrumentUtils.getCustomInstrumentFirstIndex()];

            if (instrument.getSound() != null) {
                CompatibilityUtils.playSound(player, position, yRot, instrument.getSound(), soundCategory, volume, pitch, distance);
                CompatibilityUtils.playSound(player, position, yRot, instrument.getSound(), soundCategory, volume, pitch, -distance);
            } else {
                CompatibilityUtils.playSound(player, position, yRot, instrument.getSoundFileName(), soundCategory, volume, pitch, distance);
                CompatibilityUtils.playSound(player, position, yRot, instrument.getSoundFileName(), soundCategory, volume, pitch, -distance);
            }
        } else {
            CompatibilityUtils.playSound(player, position, yRot, InstrumentUtils.getInstrument(note.getInstrument()), soundCategory, volume, pitch, distance);
            CompatibilityUtils.playSound(player, position, yRot, InstrumentUtils.getInstrument(note.getInstrument()), soundCategory, volume, pitch, -distance);
        }
    }

    @Override
    public void play(Player player, Vec3 position, float yRot, Song song, Layer layer, Note note, SoundCategory soundCategory, float volume, boolean doTranspose) {
        float pitch;
        if(doTranspose)
            pitch = NoteUtils.getPitchTransposed(note);
        else
            pitch = NoteUtils.getPitchInOctave(note);

        if (InstrumentUtils.isCustomInstrument(note.getInstrument())) {
            CustomInstrument instrument = song.getCustomInstruments()[note.getInstrument() - InstrumentUtils.getCustomInstrumentFirstIndex()];

            if (!doTranspose){
                CompatibilityUtils.playSound(player, position, yRot, InstrumentUtils.warpNameOutOfRange(instrument.getSoundFileName(), note.getKey(), note.getPitch()), soundCategory, volume, pitch, distance);
                CompatibilityUtils.playSound(player, position, yRot, InstrumentUtils.warpNameOutOfRange(instrument.getSoundFileName(), note.getKey(), note.getPitch()), soundCategory, volume, pitch, -distance);
            } else {
                if (instrument.getSound() != null) {
                    CompatibilityUtils.playSound(player, position, yRot, instrument.getSound(), soundCategory, volume, pitch, distance);
                    CompatibilityUtils.playSound(player, position, yRot, instrument.getSound(), soundCategory, volume, pitch, -distance);
                } else {
                    CompatibilityUtils.playSound(player, position, yRot, instrument.getSoundFileName(), soundCategory, volume, pitch, distance);
                    CompatibilityUtils.playSound(player, position, yRot, instrument.getSoundFileName(), soundCategory, volume, pitch, -distance);
                }
            }
        } else {
            if (NoteUtils.isOutOfRange(note.getKey(), note.getPitch()) && !doTranspose) {
                CompatibilityUtils.playSound(player, position, yRot, InstrumentUtils.warpNameOutOfRange(note.getInstrument(), note.getKey(), note.getPitch()), soundCategory, volume, pitch, distance);
                CompatibilityUtils.playSound(player, position, yRot, InstrumentUtils.warpNameOutOfRange(note.getInstrument(), note.getKey(), note.getPitch()), soundCategory, volume, pitch, -distance);
            } else {
                CompatibilityUtils.playSound(player, position, yRot, InstrumentUtils.getInstrument(note.getInstrument()), soundCategory, volume, pitch, distance);
                CompatibilityUtils.playSound(player, position, yRot, InstrumentUtils.getInstrument(note.getInstrument()), soundCategory, volume, pitch, -distance);
            }
        }
    }

    /**
     * Returns distance of {@link Note} from {@link Player}'s head.
     * @return
     */
    public float getDistance() {
        return distance;
    }

    /**
     * Sets distance of {@link Note} from {@link Player}'s head.
     * @param distance
     */
    public void setDistance(float distance) {
        this.distance = distance;
    }
}
