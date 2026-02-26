# NoteBlockAPIMod

[中文](README-zh_CN.md)

A Fabric/NeoForge/Forge port of [NoteBlockAPI](https://github.com/koca2000/NoteBlockAPI) — a server-side library for playing Note Block Studio (.nbs) files to players in-game.

Built with [Architectury](https://github.com/architectury/architectury-api) for cross-loader compatibility.

## Branches

| Branch | Minecraft | Platforms |
|---|---|---|
| [master](https://github.com/Gardelll/NoteBlockAPIMod/tree/master) | 1.21 | Fabric, NeoForge |
| [1.20](https://github.com/Gardelll/NoteBlockAPIMod/tree/1.20) | 1.20.1 | Fabric, Forge |

## Features

- Parse .nbs files (format v0-v4+)
- 4 playback types: Radio, Position, NoteBlock, Entity
- Playlist, repeat, shuffle, fade in/out
- Stereo/Mono channel modes
- Per-player volume control
- Event system for song lifecycle

## Getting Started

### Add Dependency

Add the Maven repository to your `build.gradle`:

```groovy
repositories {
    maven {
        name = "GardelNexus"
        url = "https://nexus.gardel.top/repository/maven-releases/"
        content {
            includeGroup "com.xxmicloxx.NoteBlockAPI"
        }
    }
}
```

Then add the dependency (version see [Releases](https://github.com/Gardelll/NoteBlockAPIMod/releases)):

**Fabric:**

```groovy
dependencies {
    modCompileOnly "com.xxmicloxx.NoteBlockAPI:noteblockapi-fabric:${noteblockapi_version}"
}
```

**Forge (1.20.1):**

```groovy
dependencies {
    compileOnly fg.deobf("com.xxmicloxx.NoteBlockAPI:noteblockapi-forge:${noteblockapi_version}")
}
```

**NeoForge (1.21+):**

```groovy
dependencies {
    compileOnly "com.xxmicloxx.NoteBlockAPI:noteblockapi-neoforge:${noteblockapi_version}"
}
```

> The version format is `{mod_version}+{minecraft_version}`, e.g. `1.0.0+1.21`.

### Basic Usage

```java
// Parse an NBS file
Song song = NBSDecoder.parse(new File("path/to/song.nbs"));

// Create a RadioSongPlayer (plays to all added players regardless of location)
RadioSongPlayer rsp = new RadioSongPlayer(song);

// Add a player and start playback
rsp.addPlayer(player);
rsp.setPlaying(true);
```

### SongPlayer Types

| Type | Description |
|---|---|
| `RadioSongPlayer` | Plays to all added players regardless of location |
| `PositionSongPlayer` | Plays from a fixed position, audible within a distance |
| `NoteBlockSongPlayer` | Plays from a note block position |
| `EntitySongPlayer` | Follows an entity, audible within a distance |

```java
// PositionSongPlayer example
PositionSongPlayer psp = new PositionSongPlayer(song);
psp.setTargetPosition(new Vec3(x, y, z));
psp.setTargetLevel(level);
psp.setDistance(16);
psp.addPlayer(player);
psp.setPlaying(true);
```

### Playlist

```java
Song song1 = NBSDecoder.parse(new File("song1.nbs"));
Song song2 = NBSDecoder.parse(new File("song2.nbs"));
Playlist playlist = new Playlist(song1, song2);

RadioSongPlayer rsp = new RadioSongPlayer(playlist);
rsp.addPlayer(player);
rsp.setPlaying(true);
```

### Events

This mod uses Architectury's event system instead of Bukkit events:

```java
SongEvent.SONG_END.register(songPlayer -> {
    // Song finished playing
});

SongEvent.SONG_NEXT.register(songPlayer -> {
    // Next song in playlist started
});
```

Available events: `SONG_END`, `SONG_NEXT`, `SONG_STOPPED`, `SONG_LOOP`, `SONG_DESTROYING`, `PLAYER_RANGE_STATE_CHANGE`.

## Documentation

For advanced usage (looping, fade effects, channel modes, etc.), see the original [NoteBlockAPI Wiki](https://github.com/koca2000/NoteBlockAPI/wiki).

> Note: The wiki is written for the Bukkit version. The API is largely the same, but this Mod version uses Architectury events instead of Bukkit events, and `Vec3`/`Level` instead of `Location`.

## License

[LGPL-3.0](LICENSE.txt)

## Credits

Original [NoteBlockAPI](https://github.com/koca2000/NoteBlockAPI) by xxmicloxx, michidk, koca2000, Luck.
