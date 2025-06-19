package com.xxmicloxx.NoteBlockAPI.songplayer;

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import com.xxmicloxx.NoteBlockAPI.event.SongEvent;
import com.xxmicloxx.NoteBlockAPI.model.Layer;
import com.xxmicloxx.NoteBlockAPI.model.Note;
import com.xxmicloxx.NoteBlockAPI.model.Playlist;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * SongPlayer created at a specified NoteBlock
 *
 */
public class NoteBlockSongPlayer extends RangeSongPlayer {
	private Level level;
	private BlockPos blockPos;
	private BlockState blockState;

	public NoteBlockSongPlayer(Song song) {
		super(song);
	}

	public NoteBlockSongPlayer(Song song, SoundCategory soundCategory) {
		super(song, soundCategory);
	}

	public NoteBlockSongPlayer(Playlist playlist, SoundCategory soundCategory) {
		super(playlist, soundCategory);
	}

	public NoteBlockSongPlayer(Playlist playlist) {
		super(playlist);
	}

	@Override
	void update(String key, Object value) {
		super.update(key, value);
		switch (key) {
			case "level":
				level = (Level) value;
				break;
			case "blockPos":
				blockPos = (BlockPos) value;
				break;
			case "blockState":
				blockState = (BlockState) value;
				break;
		}
	}

	/**
	 * Get the Block this SongPlayer is played at
	 * @return Block representing a NoteBlock
	 */
	public BlockPos getBlockPos() {
		return blockPos;
	}

	/**
	 * Set the Block this SongPlayer is played at
	 */
	public void setBlockPos(BlockPos blockPos) {
		this.blockPos = blockPos;
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public BlockState getBlockState() {
		return blockState;
	}

	public void setBlockState(BlockState blockState) {
		this.blockState = blockState;
	}

	@Override
	public void playTick(Player player, int tick) {
		if (!blockState.is(Blocks.NOTE_BLOCK)) {
			return;
		}
		if (!player.level().equals(level)) {
			// not in same world
			return;
		}
		byte playerVolume = NoteBlockAPI.getPlayerVolume(player);
		Vec3 center = blockPos.getCenter();

		for (Layer layer : song.getLayerHashMap().values()) {
			Note note = layer.getNote(tick);
			if (note == null) {
				continue;
			}

			float volume = ((layer.getVolume() * (int) this.volume * (int) playerVolume * note.getVelocity()) / 100_00_00_00F)
					* ((1F / 16F) * getDistance());

            channelMode.play(player, center, 0, song, layer, note, soundCategory, volume, !enable10Octave);

			if (isInRange(player)) {
				if (!this.playerList.get(player.getUUID())) {
					playerList.put(player.getUUID(), true);
					SongEvent.PLAYER_RANGE_STATE_CHANGE.invoker().stateChange(this, player, true);
				}
			} else {
				if (this.playerList.get(player.getUUID())) {
					playerList.put(player.getUUID(), false);
					SongEvent.PLAYER_RANGE_STATE_CHANGE.invoker().stateChange(this, player, false);
				}
			}
		}
	}

	/**
	 * Returns true if the Player is able to hear the current NoteBlockSongPlayer
	 * @param player in range
	 * @return ability to hear the current NoteBlockSongPlayer
	 */
	@Override
	public boolean isInRange(Player player) {
		Vec3 center = blockPos.getCenter();
        return !(player.position().distanceTo(center) > getDistance());
	}
}