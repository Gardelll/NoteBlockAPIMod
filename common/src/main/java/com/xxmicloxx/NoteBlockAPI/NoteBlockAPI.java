package com.xxmicloxx.NoteBlockAPI;

import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.utils.GameInstance;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.thread.NamedThreadFactory;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class; contains methods for playing and adjusting songs for players
 */
public class NoteBlockAPI {
	public static final Logger LOGGER = LoggerFactory.getLogger("NoteBlockAPI");

	public static final String MOD_ID = "noteblockapi";

	private static NoteBlockAPI apiInstance;

	private final Map<UUID, ArrayList<SongPlayer>> playingSongs = new ConcurrentHashMap<>();
	private final Map<UUID, Byte> playerVolume = new ConcurrentHashMap<>();
	private final ExecutorService songExecutor = Executors.newCachedThreadPool(new NamedThreadFactory(MOD_ID));

	/**
	 * Returns true if a Player is currently receiving a song
	 * @param player
	 * @return is receiving a song
	 */
	public static boolean isReceivingSong(Player player) {
		return isReceivingSong(player.getUUID());
	}

	/**
	 * Returns true if a Player with specified UUID is currently receiving a song
	 * @param uuid
	 * @return is receiving a song
	 */
	public static boolean isReceivingSong(UUID uuid) {
		ArrayList<SongPlayer> songs = apiInstance.playingSongs.get(uuid);
		return (songs != null && !songs.isEmpty());
	}

	/**
	 * Stops the song for a Player
	 * @param player
	 */
	public static void stopPlaying(Player player) {
		stopPlaying(player.getUUID());
	}

	/**
	 * Stops the song for a Player
	 * @param uuid
	 */
	public static void stopPlaying(UUID uuid) {
		ArrayList<SongPlayer> songs = apiInstance.playingSongs.get(uuid);
		if (songs == null) {
			return;
		}
		for (SongPlayer songPlayer : songs) {
			songPlayer.removePlayer(uuid);
		}
	}

	/**
	 * Sets the volume for a given Player
	 * @param player
	 * @param volume
	 */
	public static void setPlayerVolume(Player player, byte volume) {
		setPlayerVolume(player.getUUID(), volume);
	}

	/**
	 * Sets the volume for a given Player
	 * @param uuid
	 * @param volume
	 */
	public static void setPlayerVolume(UUID uuid, byte volume) {
		apiInstance.playerVolume.put(uuid, volume);
	}

	/**
	 * Gets the volume for a given Player
	 * @param player
	 * @return volume (byte)
	 */
	public static byte getPlayerVolume(Player player) {
		return getPlayerVolume(player.getUUID());
	}

	/**
	 * Gets the volume for a given Player
	 * @param uuid
	 * @return volume (byte)
	 */
	public static byte getPlayerVolume(UUID uuid) {
		Byte byteObj = apiInstance.playerVolume.get(uuid);
		if (byteObj == null) {
			byteObj = 100;
			apiInstance.playerVolume.put(uuid, byteObj);
		}
		return byteObj;
	}

	public static ArrayList<SongPlayer> getSongPlayersByPlayer(Player player){
		return getSongPlayersByPlayer(player.getUUID());
	}

	public static ArrayList<SongPlayer> getSongPlayersByPlayer(UUID player){
		return apiInstance.playingSongs.get(player);
	}

	public static void setSongPlayersByPlayer(Player player, ArrayList<SongPlayer> songs){
		setSongPlayersByPlayer(player.getUUID(), songs);
	}

	public static void setSongPlayersByPlayer(UUID player, ArrayList<SongPlayer> songs){
		apiInstance.playingSongs.put(player, songs);
	}

	public static void init() {
		apiInstance = new NoteBlockAPI();
		LifecycleEvent.SERVER_STOPPED.register(minecraftServer -> {
            apiInstance.songExecutor.shutdownNow();
        });
	}

	public void doSync(Runnable runnable) {
		GameInstance.getServer().execute(runnable);
	}

	public void doAsync(Runnable runnable) {
		songExecutor.submit(runnable);
	}

	public boolean isDisabling() {
		MinecraftServer server = GameInstance.getServer();
		if  (server == null) {
			return true;
		}
		return !server.isRunning();
	}

	public static NoteBlockAPI getAPI(){
		return apiInstance;
	}
}
