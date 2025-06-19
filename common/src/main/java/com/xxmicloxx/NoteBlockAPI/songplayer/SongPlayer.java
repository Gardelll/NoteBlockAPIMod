package com.xxmicloxx.NoteBlockAPI.songplayer;

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import com.xxmicloxx.NoteBlockAPI.event.SongEvent;
import com.xxmicloxx.NoteBlockAPI.model.FadeType;
import com.xxmicloxx.NoteBlockAPI.model.Playlist;
import com.xxmicloxx.NoteBlockAPI.model.RepeatMode;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory;
import com.xxmicloxx.NoteBlockAPI.model.playmode.ChannelMode;
import com.xxmicloxx.NoteBlockAPI.model.playmode.MonoMode;
import dev.architectury.event.EventResult;
import dev.architectury.utils.GameInstance;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.world.entity.player.Player;


/**
 * Plays a Song for a list of Players
 *
 */
public abstract class SongPlayer {

	protected Song song;
	protected Playlist playlist;
	protected int actualSong = 0;

	protected boolean playing = false;
	protected boolean fading = false;
	protected short tick = -1;
	protected Map<UUID, Boolean> playerList = new ConcurrentHashMap<UUID, Boolean>();

	protected boolean autoDestroy = false;
	protected boolean destroyed = false;

	protected byte volume = 100;
	protected Fade fadeIn;
	protected Fade fadeOut;
	protected Fade fadeTemp = null;
	protected RepeatMode repeat = RepeatMode.NO;
	protected boolean random = false;

	protected Map<Song, Boolean> songQueue = new ConcurrentHashMap<Song, Boolean>(); //True if already played

	private final Lock lock = new ReentrantLock();
	private final Random rng = new Random();

	protected NoteBlockAPI plugin;

	protected SoundCategory soundCategory;
	protected ChannelMode channelMode = new MonoMode();
	protected boolean enable10Octave = false;

	public SongPlayer(Song song) {
		this(new Playlist(song), SoundCategory.MASTER);
	}

	public SongPlayer(Song song, SoundCategory soundCategory) {
		this(new Playlist(song), soundCategory);
	}

	public SongPlayer(Song song, SoundCategory soundCategory, boolean random) {
		this(new Playlist(song), soundCategory, random);
	}

	public SongPlayer(Playlist playlist){
		this(playlist, SoundCategory.MASTER);
	}

	public SongPlayer(Playlist playlist, SoundCategory soundCategory){
		this(playlist, soundCategory, false);
	}

	public SongPlayer(Playlist playlist, SoundCategory soundCategory, boolean random){
		this.playlist = playlist;
		this.random = random;
		this.soundCategory = soundCategory;
		plugin = NoteBlockAPI.getAPI();

		fadeIn = new Fade(FadeType.NONE, 60);
		fadeIn.setFadeStart((byte) 0);
		fadeIn.setFadeTarget(volume);

		fadeOut = new Fade(FadeType.NONE, 60);
		fadeOut.setFadeStart(volume);
		fadeOut.setFadeTarget((byte) 0);

		if (random){
			checkPlaylistQueue();
			actualSong = rng.nextInt(playlist.getCount());
		}
		this.song = playlist.get(actualSong);

		start();
	}

	void update(String key, Object value){
		switch (key){
			case "playing":
				playing = (boolean) value;
				break;
			case "fadeType":
				fadeIn.setType(FadeType.valueOf(((String) value).replace("FADE_", "")));
				break;
			case "fadeTarget":
				fadeIn.setFadeTarget((byte) value);
				break;
			case "fadeStart":
				fadeIn.setFadeStart((byte) value);
				break;
			case "fadeDuration":
				fadeIn.setFadeDuration((int) value);
				break;
			case "fadeDone":
				fadeIn.setFadeDone((int) value);
				break;
			case "tick":
				tick = (short) value;
				break;
			case "addplayer":
				addPlayer(((Player) value).getUUID());
				break;
			case "removeplayer":
				removePlayer(((Player) value).getUUID());
				break;
			case "autoDestroy":
				autoDestroy = (boolean) value;
				break;
			case "volume":
				volume = (byte) value;
				break;
			case "soundCategory":
				soundCategory = SoundCategory.valueOf((String) value);
				break;

		}
	}

	/**
	 * Check if 6 octave range is enabled
	 *
	 * @return true if enabled, false otherwise
	 */
	public boolean isEnable10Octave() {
		return enable10Octave;
	}

	/**
	 * Enable or disable 6 octave range
	 * <p>
	 * If not enabled, notes will be transposed to 2 octave range
	 *
	 * @param enable10Octave true if enabled, false otherwise
	 */
	public void setEnable10Octave(boolean enable10Octave) {
		this.enable10Octave = enable10Octave;
	}

	/**
	 * Starts this SongPlayer
	 */
	private void start() {
		plugin.doAsync(() -> {
			while (!destroyed) {
				long startTime = System.currentTimeMillis();
				lock.lock();
				try {
					if (destroyed || NoteBlockAPI.getAPI().isDisabling()){
						break;
					}

					if (playing || fading) {
						if (fadeTemp != null){
							if (fadeTemp.isDone()) {
								fadeTemp = null;
								fading = false;
								if (!playing) {
									plugin.doSync(() -> SongEvent.SONG_STOPPED.invoker().stop(this));
									volume = fadeIn.getFadeTarget();
									continue;
								}
							}else {
								int fade = fadeTemp.calculateFade();
								if (fade != -1){
									volume = (byte) fade;
								}
							}
						} else if (tick < fadeIn.getFadeDuration()){
							int fade = fadeIn.calculateFade();
							if (fade != -1){
								volume = (byte) fade;
							}
						} else if (tick >= song.getLength() - fadeOut.getFadeDuration()){
							int fade = fadeOut.calculateFade();
							if (fade != -1){
								volume = (byte) fade;
							}
						}

						tick++;
						if (tick > song.getLength()) {
							tick = -1;
							fadeIn.setFadeDone(0);
							fadeOut.setFadeDone(0);
							volume = fadeIn.getFadeTarget();
							if (repeat == RepeatMode.ONE){
								EventResult result = SongEvent.SONG_LOOP.invoker().loop(this);
								if (result.isTrue()) {
									continue;
								}
							} else {
								if (random) {
									songQueue.put(song, true);
									checkPlaylistQueue();
									ArrayList<Song> left = new ArrayList<>();
									for (Song s : songQueue.keySet()) {
										if (!songQueue.get(s)) {
											left.add(s);
										}
									}

									if (left.size() == 0) {
										left.addAll(songQueue.keySet());
										for (Song s : songQueue.keySet()) {
											songQueue.put(s, false);
										}
										song = left.get(rng.nextInt(left.size()));
										actualSong = playlist.getIndex(song);
										if (repeat == RepeatMode.ALL) {
											EventResult result = SongEvent.SONG_LOOP.invoker().loop(this);
											if (result.isTrue()) {
												continue;
											}
										}
									} else {
										song = left.get(rng.nextInt(left.size()));
										actualSong = playlist.getIndex(song);

										plugin.doSync(() -> SongEvent.SONG_NEXT.invoker().next(this));
										continue;
									}
								} else {
									if (playlist.hasNext(actualSong)) {
										actualSong++;
										song = playlist.get(actualSong);
										plugin.doSync(() -> SongEvent.SONG_NEXT.invoker().next(this));
										continue;
									} else {
										actualSong = 0;
										song = playlist.get(actualSong);
										if (repeat == RepeatMode.ALL) {
											EventResult result = SongEvent.SONG_LOOP.invoker().loop(this);
											if (result.isTrue()) {
												continue;
											}
										}
									}
								}
							}
							playing = false;
							plugin.doSync(() -> SongEvent.SONG_END.invoker().end(this));
							if (autoDestroy) {
								destroy();
							}
							continue;
						}

						plugin.doSync(() -> {
							try {
								for (UUID uuid : playerList.keySet()) {
									Player player = GameInstance.getServer().getPlayerList().getPlayer(uuid);
									if (player == null) {
										// offline...
										continue;
									}
									playTick(player, tick);
								}
							} catch (Exception e){
                                NoteBlockAPI.LOGGER.error("An error occurred during the playback of song {}",
									song != null ? song.getPath() + " (" + song.getAuthor() + " - " + song.getTitle() + ")" : "null", e);
							}
						});
					}
				} catch (Exception e) {
					NoteBlockAPI.LOGGER.error("An error occurred during the playback of song {}",
						song != null ? song.getPath() + " (" + song.getAuthor() + " - " + song.getTitle() + ")" : "null", e);
				} finally {
					lock.unlock();
				}

				if (destroyed) {
					break;
				}

				long duration = System.currentTimeMillis() - startTime;
				float delayMillis = song.getDelay() * 50;
				if (duration < delayMillis) {
					try {
						Thread.sleep((long) (delayMillis - duration));
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			}
		});
	}

	private void checkPlaylistQueue(){
		for (Song s : songQueue.keySet()){
			if (!playlist.contains(s)){
				songQueue.remove(s);
			}
		}

		for (Song s : playlist.getSongList()){
			if (!songQueue.containsKey(s)){
				songQueue.put(s, false);
			}
		}
	}

	/**
	 * Returns {@link Fade} for Fade in effect
	 * @return Fade
	 */
	public Fade getFadeIn(){
		return fadeIn;
	}

	/**
	 * Returns {@link Fade} for Fade out effect
	 * @return Fade
	 */
	public Fade getFadeOut(){
		return fadeOut;
	}

	/**
	 * Gets list of current Player UUIDs listening to this SongPlayer
	 * @return list of Player UUIDs
	 */
	public Set<UUID> getPlayerUUIDs() {
        Set<UUID> uuids = new HashSet<>(playerList.keySet());
		return Collections.unmodifiableSet(uuids);
	}

	/**
	 * Adds a Player to the list of Players listening to this SongPlayer
	 * @param player
	 */
	public void addPlayer(Player player) {
		addPlayer(player.getUUID());
	}

	/**
	 * Adds a Player to the list of Players listening to this SongPlayer
	 * @param player's uuid
	 */
	public void addPlayer(UUID player) {
		lock.lock();
		try {
			if (!playerList.containsKey(player)) {
				playerList.put(player, false);
				ArrayList<SongPlayer> songs = NoteBlockAPI.getSongPlayersByPlayer(player);
				if (songs == null) {
					songs = new ArrayList<>();
				}
				songs.add(this);
				NoteBlockAPI.setSongPlayersByPlayer(player, songs);
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns whether the SongPlayer is set to destroy itself when no one is listening
	 * or when the Song ends
	 * @return if autoDestroy is enabled
	 */
	public boolean getAutoDestroy() {
		lock.lock();
		try {
			return autoDestroy;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Sets whether the SongPlayer is going to destroy itself when no one is listening
	 * or when the Song ends
	 * @param autoDestroy if autoDestroy is enabled
	 */
	public void setAutoDestroy(boolean autoDestroy) {
		lock.lock();
		try {
			this.autoDestroy = autoDestroy;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Plays the Song for the specific player
	 * @param player to play this SongPlayer for
	 * @param tick to play at
	 */
	public abstract void playTick(Player player, int tick);

	/**
	 * SongPlayer will destroy itself
	 */
	public void destroy() {
		lock.lock();
		try {
			EventResult result = SongEvent.SONG_DESTROYING.invoker().destroy(this);
			if (result.isFalse()) {
				return;
			}
			destroyed = true;
			playing = false;
			setTick((short) -1);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns whether the SongPlayer is actively playing
	 * @return is playing
	 */
	public boolean isPlaying() {
		return playing;
	}

	/**
	 * Sets whether the SongPlayer is playing
	 * @param playing
	 */
	public void setPlaying(boolean playing) {
		setPlaying(playing, null);
	}

	/**
	 * Sets whether the SongPlayer is playing and whether it should fade if previous value was different
	 * @param playing
	 * @param fade
	 */
	public void setPlaying(boolean playing, boolean fade) {
		setPlaying(playing, fade ? (playing ? fadeIn : fadeOut) : null);
	}

	public void setPlaying(boolean playing, Fade fade) {
		if (this.playing == playing) return;

		this.playing = playing;
		if (fade != null && fade.getType() != FadeType.NONE) {
			fadeTemp = new Fade(fade.getType(), fade.getFadeDuration());
			fadeTemp.setFadeStart(playing ? 0 : volume);
			fadeTemp.setFadeTarget(playing ? volume : 0);
			fading = true;
		} else {
			fading = false;
			fadeTemp = null;
			volume = fadeIn.getFadeTarget();
			if (!playing) {
				plugin.doSync(() -> SongEvent.SONG_STOPPED.invoker().stop(this));
			}
		}
	}

	/**
	 * Gets the current tick of this SongPlayer
	 * @return
	 */
	public short getTick() {
		return tick;
	}

	/**
	 * Sets the current tick of this SongPlayer
	 * @param tick
	 */
	public void setTick(short tick) {
		this.tick = tick;
	}

	/**
	 * Removes a player from this SongPlayer
	 * @param player to remove
	 */
	public void removePlayer(Player player) {
		removePlayer(player.getUUID());
	}

	/**
	 * Removes a player from this SongPlayer
	 * @param uuid of player to remove
	 */
	public void removePlayer(UUID uuid) {
		lock.lock();
		try {
			playerList.remove(uuid);
			if (NoteBlockAPI.getSongPlayersByPlayer(uuid) == null) {
				return;
			}
			ArrayList<SongPlayer> songs = new ArrayList<>(
					NoteBlockAPI.getSongPlayersByPlayer(uuid));
			songs.remove(this);
			NoteBlockAPI.setSongPlayersByPlayer(uuid, songs);
			if (playerList.isEmpty() && autoDestroy) {
				plugin.doSync(() -> SongEvent.SONG_END.invoker().end(this));
				destroy();
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Gets the current volume of this SongPlayer
	 * @return volume (0-100)
	 */
	public byte getVolume() {
		return volume;
	}

	/**
	 * Sets the current volume of this SongPlayer
	 * @param volume (0-100)
	 */
	public void setVolume(byte volume) {
		if (volume > 100){
			volume = 100;
		} else if (volume < 0){
			volume = 0;
		}
		this.volume = volume;

		fadeIn.setFadeTarget(volume);
		fadeOut.setFadeStart(volume);
		if (fadeTemp != null) {
			if (playing) fadeTemp.setFadeTarget(volume);
			else fadeTemp.setFadeStart(volume);
		}
	}

	/**
	 * Gets the Song being played by this SongPlayer
	 * @return
	 */
	public Song getSong() {
		return song;
	}

	/**
	 * Gets the Playlist being played by this SongPlayer
	 * @return
	 */
	public Playlist getPlaylist() {
		return playlist;
	}

	/**
	 * Sets the Playlist being played by this SongPlayer. Will affect next Song
	 */
	public void setPlaylist(Playlist playlist) {
		this.playlist = playlist;
	}

	/**
	 * Get index of actually played {@link Song} in {@link Playlist}
	 * @return
	 */
	public int getPlayedSongIndex(){
		return actualSong;
	}

	/**
	 * Start playing {@link Song} at specified index in {@link Playlist}
	 * If there is no {@link Song} at this index, {@link SongPlayer} will continue playing current song
	 * @param index
	 */
	public void playSong(int index){
		lock.lock();
		try {
			if (playlist.exist(index)){
				song = playlist.get(index);
				actualSong = index;
				tick = -1;
				fadeIn.setFadeDone(0);
				fadeOut.setFadeDone(0);
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Start playing {@link Song} that is next in {@link Playlist} or random {@link Song} from {@link Playlist}
	 */
	public void playNextSong(){
		lock.lock();
		try {
			tick = song.getLength();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Gets the SoundCategory of this SongPlayer
	 * @see SoundCategory
	 * @return SoundCategory of this SongPlayer
	 */
	public SoundCategory getCategory() {
		return soundCategory;
	}

	/**
	 * Sets the SoundCategory for this SongPlayer
	 * @param soundCategory
	 */
	public void setCategory(SoundCategory soundCategory) {
		this.soundCategory = soundCategory;
	}

	/**
	 * Sets whether the SongPlayer will loop
	 * @deprecated
	 * @param loop
	 */
	public void setLoop(boolean loop){
		this.repeat = RepeatMode.ALL;
	}

	/**
	 * Gets whether the SongPlayer will loop
	 * @deprecated
	 * @return is loop
	 */
	public boolean isLoop(){
		return repeat == RepeatMode.ALL;
	}

	/**
	 * Sets SongPlayer's {@link RepeatMode}
	 * @param repeatMode
	 */
	public void setRepeatMode(RepeatMode repeatMode){
		this.repeat = repeatMode;
	}

	/**
	 * Gets SongPlayer's {@link RepeatMode}
	 * @return
	 */
	public RepeatMode getRepeatMode(){
		return repeat;
	}

	/**
	 * Sets whether the SongPlayer will choose next song from player randomly
	 * @param random
	 */
	public void setRandom(boolean random){
		this.random = random;
	}

	/**
	 * Gets whether the SongPlayer will choose next song from player randomly
	 * @return is random
	 */
	public boolean isRandom(){
		return random;
	}

	public ChannelMode getChannelMode(){
		return channelMode;
	}

	void makeNewClone(Class newClass){
		try {
			Constructor c = newClass.getDeclaredConstructor(SongPlayer.class);
			c.setAccessible(true);
		} catch (IllegalArgumentException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}

}
