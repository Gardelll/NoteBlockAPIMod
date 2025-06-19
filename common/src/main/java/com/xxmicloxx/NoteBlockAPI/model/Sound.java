package com.xxmicloxx.NoteBlockAPI.model;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * Version independent SoundEvents sounds.
 *
 * Enum mapping to note names for different
 * Minecraft versions.
 */
public enum Sound {
	NOTE_PIANO("NOTE_BLOCK_HARP"),
	NOTE_BASS("NOTE_BLOCK_BASS"),
	NOTE_BASS_DRUM("NOTE_BLOCK_BASEDRUM"),
	NOTE_SNARE_DRUM("NOTE_BLOCK_SNARE"),
	NOTE_STICKS("NOTE_BLOCK_HAT"),
	NOTE_BASS_GUITAR("NOTE_BLOCK_GUITAR"),
	NOTE_FLUTE("NOTE_BLOCK_FLUTE"),
	NOTE_BELL("NOTE_BLOCK_BELL"),
	NOTE_CHIME("NOTE_BLOCK_CHIME"),
	NOTE_XYLOPHONE("NOTE_BLOCK_XYLOPHONE"),
	NOTE_PLING("NOTE_BLOCK_PLING"),
	NOTE_IRON_XYLOPHONE("NOTE_BLOCK_IRON_XYLOPHONE"),
	NOTE_COW_BELL("NOTE_BLOCK_COW_BELL"),
	NOTE_DIDGERIDOO("NOTE_BLOCK_DIDGERIDOO"),
	NOTE_BIT("NOTE_BLOCK_BIT"),
	NOTE_BANJO("NOTE_BLOCK_BANJO");

	private String[] versionDependentNames;
	private SoundEvent cached = null;
	private static Map<String, SoundEvent> cachedSoundMap = new HashMap<>();

	Sound(String... versionDependentNames) {
		this.versionDependentNames = versionDependentNames;
	}

	/**
	 * Attempts to retrieve the org.bukkit.Sound equivalent of a version dependent enum name
	 * @param eventName
	 * @return SoundEvent
	 */
	public static SoundEvent getFromEventName(String eventName) {
		SoundEvent sound = cachedSoundMap.get(eventName.toUpperCase());
		if (sound != null)
			return sound;

        try {
            Field field = SoundEvents.class.getField(eventName);
			Object value = field.get(null);
			if (value instanceof SoundEvent) {
				return (SoundEvent) value;
			} else if (value instanceof Holder.Reference<?> ref) {
				return (SoundEvent) ref.value();
			}
			throw new IllegalArgumentException("Unknown sound event name: " + eventName);
		} catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

	private SoundEvent getSound() {
		if (cached != null) return cached;
		for (String name : versionDependentNames) {
			try {
				return cached = getFromEventName(name);
			} catch (IllegalArgumentException ignore2) {
				// try next
			}
		}
		return null;
	}

	/**
	 * Get the bukkit sound for current server version
	 *
	 * Caches sound on first call
	 * @return corresponding {@link SoundEvent}
	 */
	public SoundEvent soundEvent() {
		if (getSound() != null) {
			return getSound();
		}
		throw new IllegalArgumentException("Found no valid sound name for " + this.name());
	}

	static {
		// Cache sound access.
		for (Sound sound : values())
			for (String soundName : sound.versionDependentNames)
				cachedSoundMap.put(soundName.toUpperCase(), sound.getSound());
	}
}