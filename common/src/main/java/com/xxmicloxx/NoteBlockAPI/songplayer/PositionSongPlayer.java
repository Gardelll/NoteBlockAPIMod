package com.xxmicloxx.NoteBlockAPI.songplayer;

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import com.xxmicloxx.NoteBlockAPI.event.SongEvent;
import com.xxmicloxx.NoteBlockAPI.model.Layer;
import com.xxmicloxx.NoteBlockAPI.model.Note;
import com.xxmicloxx.NoteBlockAPI.model.Playlist;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * SongPlayer created at a specified Location
 *
 */
public class PositionSongPlayer extends RangeSongPlayer {
	private Level targetLevel;

	private Vec3 targetPosition;

	private float yRot = 0;

	public PositionSongPlayer(Song song) {
		super(song);
	}

	public PositionSongPlayer(Song song, SoundCategory soundCategory) {
		super(song, soundCategory);
	}

	public PositionSongPlayer(Playlist playlist, SoundCategory soundCategory) {
		super(playlist, soundCategory);
	}

	public PositionSongPlayer(Playlist playlist) {
		super(playlist);
	}

	@Override
	void update(String key, Object value) {
		super.update(key, value);

		switch (key) {
			case "targetPosition":
				targetPosition = (Vec3) value;
				break;
			case "targetLevel":
				targetLevel = (Level) value;
				break;
			case "yRot":
				yRot = (Float) value;
				break;
		}
	}

	/**
	 * Gets location on which is the PositionSongPlayer playing
	 * @return {@link Vec3}
	 */
	public Vec3 getTargetPosition() {
		return targetPosition;
	}

	/**
	 * Sets location on which is the PositionSongPlayer playing
	 */
	public void setTargetPosition(Vec3 targetPosition) {
		this.targetPosition = targetPosition;
	}

	public Level getTargetLevel() {
		return targetLevel;
	}

	public void setTargetLevel(Level targetLevel) {
		this.targetLevel = targetLevel;
	}

	public float getyRot() {
		return yRot;
	}

	public void setyRot(float yRot) {
		this.yRot = yRot;
	}

	@Override
	public void playTick(Player player, int tick) {
		if (!player.level().equals(targetLevel)) {
			return; // not in same world
		}

		byte playerVolume = NoteBlockAPI.getPlayerVolume(player);

		for (Layer layer : song.getLayerHashMap().values()) {
			Note note = layer.getNote(tick);
			if (note == null) continue;

			float volume = ((layer.getVolume() * (int) this.volume * (int) playerVolume * note.getVelocity()) / 100_00_00_00F)
					* ((1F / 16F) * getDistance());

			channelMode.play(player, targetPosition, yRot, song, layer, note, soundCategory, volume, !enable10Octave);

			if (isInRange(player)) {
				if (!playerList.get(player.getUUID())) {
					playerList.put(player.getUUID(), true);
					SongEvent.PLAYER_RANGE_STATE_CHANGE.invoker().stateChange(this, player, true);
				}
			} else {
				if (playerList.get(player.getUUID())) {
					playerList.put(player.getUUID(), false);
					SongEvent.PLAYER_RANGE_STATE_CHANGE.invoker().stateChange(this, player, false);
				}
			}
		}
	}

	/**
	 * Returns true if the Player is able to hear the current PositionSongPlayer
	 * @param player in range
	 * @return ability to hear the current PositionSongPlayer
	 */
	@Override
	public boolean isInRange(Player player) {
		return player.position().distanceTo(targetPosition) <= getDistance();
	}
}
