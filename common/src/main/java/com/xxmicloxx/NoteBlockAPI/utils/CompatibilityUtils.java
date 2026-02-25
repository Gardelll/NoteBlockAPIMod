package com.xxmicloxx.NoteBlockAPI.utils;

import com.google.common.base.Preconditions;
import com.xxmicloxx.NoteBlockAPI.model.CustomInstrument;
import com.xxmicloxx.NoteBlockAPI.model.Sound;
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory;
import java.util.ArrayList;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Fields/methods for reflection &amp; version checking
 */
public class CompatibilityUtils {
    private static float serverVersion = -1;

    /**
     * Returns whether the version of Bukkit is or is after 1.12
     *
     * @return version is after 1.12
     * @deprecated Compare {@link #getServerVersion()} with 0.0112f
     */
    public static boolean isPost1_12() {
        return getServerVersion() >= 0.0112f;
    }

    /**
     * Plays a sound using NMS &amp; reflection
     *
     * @param player
     * @param position
     * @param sound
     * @param category
     * @param volume
     * @param pitch
     * @param distance
     */
    public static void playSound(Player player, Vec3 position, float yRot, String sound,
                                 SoundCategory category, float volume, float pitch, float distance) {
        playSound0(player, MathUtils.stereoPan(position, yRot, distance), sound, category, volume, pitch, player.getRandom().nextLong());
    }

    /**
     * Plays a sound using NMS &amp; reflection
     *
     * @param player
     * @param position
     * @param sound
     * @param category
     * @param volume
     * @param pitch
     * @param distance
     */
    public static void playSound(Player player, Vec3 position, float yRot, Sound sound,
                                 SoundCategory category, float volume, float pitch, float distance) {
        playSound0(player, MathUtils.stereoPan(position, yRot, distance), sound, category, volume, pitch, player.getRandom().nextLong());
    }

    /**
     * Return list of instuments which were added in specified version
     *
     * @param serverVersion 1.12 = 0.0112f, 1.14 = 0.0114f,...
     * @return list of custom instruments, if no instuments were added in specified version returns empty list
     */
    public static ArrayList<CustomInstrument> getVersionCustomInstruments(float serverVersion) {
        ArrayList<CustomInstrument> instruments = new ArrayList<>();
        if (serverVersion == 0.0112f) {
            instruments.add(new CustomInstrument((byte) 0, "Guitar", "block.note_block.guitar.ogg"));
            instruments.add(new CustomInstrument((byte) 0, "Flute", "block.note_block.flute.ogg"));
            instruments.add(new CustomInstrument((byte) 0, "Bell", "block.note_block.bell.ogg"));
            instruments.add(new CustomInstrument((byte) 0, "Chime", "block.note_block.icechime.ogg"));
            instruments.add(new CustomInstrument((byte) 0, "Xylophone", "block.note_block.xylobone.ogg"));
            return instruments;
        }

        if (serverVersion == 0.0114f) {
            instruments.add(new CustomInstrument((byte) 0, "Iron Xylophone", "block.note_block.iron_xylophone.ogg"));
            instruments.add(new CustomInstrument((byte) 0, "Cow Bell", "block.note_block.cow_bell.ogg"));
            instruments.add(new CustomInstrument((byte) 0, "Didgeridoo", "block.note_block.didgeridoo.ogg"));
            instruments.add(new CustomInstrument((byte) 0, "Bit", "block.note_block.bit.ogg"));
            instruments.add(new CustomInstrument((byte) 0, "Banjo", "block.note_block.banjo.ogg"));
            instruments.add(new CustomInstrument((byte) 0, "Pling", "block.note_block.pling.ogg"));
            return instruments;
        }
        return instruments;
    }

    /**
     * Return list of custom instruments based on song first custom instrument index and server version
     *
     * @param firstCustomInstrumentIndex
     * @return
     */
    public static ArrayList<CustomInstrument> getVersionCustomInstrumentsForSong(int firstCustomInstrumentIndex) {
        ArrayList<CustomInstrument> instruments = new ArrayList<>();

        if (getServerVersion() < 0.0112f) {
            if (firstCustomInstrumentIndex == 10) {
                instruments.addAll(getVersionCustomInstruments(0.0112f));
            } else if (firstCustomInstrumentIndex == 16) {
                instruments.addAll(getVersionCustomInstruments(0.0112f));
                instruments.addAll(getVersionCustomInstruments(0.0114f));
            }
        } else if (getServerVersion() < 0.0114f) {
            if (firstCustomInstrumentIndex == 16) {
                instruments.addAll(getVersionCustomInstruments(0.0114f));
            }
        }

        return instruments;
    }

    /**
     * Returns server version as float less than 1 with two digits for each version part
     *
     * @return e.g. 0.011401f for 1.14.1
     */
    public static float getServerVersion() {
        if (serverVersion != -1) {
            return serverVersion;
        }

        String versionInfo = SharedConstants.getCurrentVersion().getName();

        String[] versionParts = versionInfo.split("\\.");

        StringBuilder versionString = new StringBuilder("0.");
        for (String part : versionParts) {
            if (part.length() == 1) {
                versionString.append("0");
            }

            versionString.append(part);
        }
        serverVersion = Float.parseFloat(versionString.toString());
        return serverVersion;
    }

    private static void playSound0(Player player, Vec3 pos, String sound, SoundCategory category, float volume, float pitch, long seed) {
        if (player instanceof ServerPlayer sp && pos != null && sound != null && category != null) {
            playSound0(sp, pos, Holder.direct(SoundEvent.createVariableRangeEvent(ResourceLocation.tryParse(sound))), SoundSource.valueOf(category.name()), volume, pitch, seed);
        }
    }

    private static void playSound0(Player player, Vec3 pos, Sound sound, SoundCategory category, float volume, float pitch, long seed) {
        if (player instanceof ServerPlayer sp && pos != null && sound != null && category != null) {
            playSound0(sp, pos, Holder.direct(sound.soundEvent()), SoundSource.valueOf(category.name()), volume, pitch, seed);
        }
    }

    private static void playSound0(ServerPlayer player, Vec3 pos, Holder<SoundEvent> soundEffectHolder, SoundSource categoryNMS, float volume, float pitch, long seed) {
        Preconditions.checkArgument(pos != null, "pos cannot be null");
        ClientboundSoundPacket packet = new ClientboundSoundPacket(soundEffectHolder, categoryNMS, pos.x, pos.y, pos.z, volume, pitch, seed);
        player.connection.send(packet);
    }
}
