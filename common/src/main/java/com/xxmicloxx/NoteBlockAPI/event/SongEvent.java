package com.xxmicloxx.NoteBlockAPI.event;

import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.world.entity.player.Player;

/**
 * SongEvents
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-06-19 02:19
 */
public interface SongEvent {
    Event<SongDestroying> SONG_DESTROYING = EventFactory.createEventResult();

    Event<SongEnd> SONG_END = EventFactory.createEventResult();

    Event<SongLoop> SONG_LOOP = EventFactory.createEventResult();

    Event<SongNext> SONG_NEXT = EventFactory.createEventResult();

    Event<SongStopped> SONG_STOPPED = EventFactory.createEventResult();

    Event<PlayerRangeStateChange> PLAYER_RANGE_STATE_CHANGE = EventFactory.createEventResult();

    /**
     * Called whenever a SongPlayer is destroyed
     */
    interface SongDestroying {
        EventResult destroy(SongPlayer song);
    }

    /**
     * Called when a Song ends
     * or when no players are listening and auto destroy is enabled
     */
    interface SongEnd {
        void end(SongPlayer song);
    }

    /**
     * Called when a Song ends and is going to start again
     */
    interface SongLoop {
        EventResult loop(SongPlayer song);
    }

    /**
     * Called when a Song is going to play next song in playlist
     */
    interface SongNext {
        void next(SongPlayer song);
    }

    /**
     * Called whenever a SongPlayer is stopped
     */
    interface SongStopped {
        void stop(SongPlayer song);
    }

    interface PlayerRangeStateChange {
        void stateChange(SongPlayer song, Player player, boolean state);
    }
}
