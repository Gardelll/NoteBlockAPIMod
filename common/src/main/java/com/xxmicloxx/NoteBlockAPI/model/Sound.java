package com.xxmicloxx.NoteBlockAPI.model;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * SoundEvents for note block instruments (1.20.1).
 */
public enum Sound {
	NOTE_PIANO(SoundEvents.NOTE_BLOCK_HARP.value()),
	NOTE_BASS(SoundEvents.NOTE_BLOCK_BASS.value()),
	NOTE_BASS_DRUM(SoundEvents.NOTE_BLOCK_BASEDRUM.value()),
	NOTE_SNARE_DRUM(SoundEvents.NOTE_BLOCK_SNARE.value()),
	NOTE_STICKS(SoundEvents.NOTE_BLOCK_HAT.value()),
	NOTE_BASS_GUITAR(SoundEvents.NOTE_BLOCK_GUITAR.value()),
	NOTE_FLUTE(SoundEvents.NOTE_BLOCK_FLUTE.value()),
	NOTE_BELL(SoundEvents.NOTE_BLOCK_BELL.value()),
	NOTE_CHIME(SoundEvents.NOTE_BLOCK_CHIME.value()),
	NOTE_XYLOPHONE(SoundEvents.NOTE_BLOCK_XYLOPHONE.value()),
	NOTE_PLING(SoundEvents.NOTE_BLOCK_PLING.value()),
	NOTE_IRON_XYLOPHONE(SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value()),
	NOTE_COW_BELL(SoundEvents.NOTE_BLOCK_COW_BELL.value()),
	NOTE_DIDGERIDOO(SoundEvents.NOTE_BLOCK_DIDGERIDOO.value()),
	NOTE_BIT(SoundEvents.NOTE_BLOCK_BIT.value()),
	NOTE_BANJO(SoundEvents.NOTE_BLOCK_BANJO.value());

	private final SoundEvent soundEvent;

	Sound(SoundEvent soundEvent) {
		this.soundEvent = soundEvent;
	}

	/**
	 * Get the sound event for this instrument
	 * @return corresponding {@link SoundEvent}
	 */
	public SoundEvent soundEvent() {
		return soundEvent;
	}
}