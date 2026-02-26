# NoteBlockAPIMod

[English](README.md)

[NoteBlockAPI](https://github.com/koca2000/NoteBlockAPI) 的 Fabric/NeoForge/Forge 移植版 — 一个用于在游戏内向玩家播放 Note Block Studio (.nbs) 文件的服务端库。

基于 [Architectury](https://github.com/architectury/architectury-api) 实现跨模组加载器兼容。

## 分支

| 分支 | Minecraft 版本 | 平台 |
|---|---|---|
| [master](https://github.com/Gardelll/NoteBlockAPIMod/tree/master) | 1.21 | Fabric, NeoForge |
| [1.20](https://github.com/Gardelll/NoteBlockAPIMod/tree/1.20) | 1.20.1 | Fabric, Forge |

## 功能

- 解析 .nbs 文件（格式 v0-v4+）
- 4 种播放器类型：Radio、Position、NoteBlock、Entity
- 播放列表、循环、随机播放、淡入淡出
- 立体声/单声道模式
- 玩家独立音量控制
- 歌曲生命周期事件系统

## 快速开始

### 添加依赖

在 `build.gradle` 中添加 Maven 仓库：

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

然后添加依赖（版本号见 [Releases](https://github.com/Gardelll/NoteBlockAPIMod/releases)）：

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

> 版本号格式为 `{mod_version}+{minecraft_version}`，例如 `1.0.0+1.21`。

### 基本用法

```java
// 解析 NBS 文件
Song song = NBSDecoder.parse(new File("path/to/song.nbs"));

// 创建 RadioSongPlayer（无论玩家位置，向所有已添加的玩家播放）
RadioSongPlayer rsp = new RadioSongPlayer(song);

// 添加玩家并开始播放
rsp.addPlayer(player);
rsp.setPlaying(true);
```

### 播放器类型

| 类型 | 说明 |
|---|---|
| `RadioSongPlayer` | 向所有已添加的玩家播放，不受位置限制 |
| `PositionSongPlayer` | 从固定位置播放，范围内可听 |
| `NoteBlockSongPlayer` | 从音符盒位置播放 |
| `EntitySongPlayer` | 跟随实体播放，范围内可听 |

```java
// PositionSongPlayer 示例
PositionSongPlayer psp = new PositionSongPlayer(song);
psp.setTargetPosition(new Vec3(x, y, z));
psp.setTargetLevel(level);
psp.setDistance(16);
psp.addPlayer(player);
psp.setPlaying(true);
```

### 播放列表

```java
Song song1 = NBSDecoder.parse(new File("song1.nbs"));
Song song2 = NBSDecoder.parse(new File("song2.nbs"));
Playlist playlist = new Playlist(song1, song2);

RadioSongPlayer rsp = new RadioSongPlayer(playlist);
rsp.addPlayer(player);
rsp.setPlaying(true);
```

### 事件

本 Mod 使用 Architectury 事件系统（而非 Bukkit 事件）：

```java
SongEvent.SONG_END.register(songPlayer -> {
    // 歌曲播放结束
});

SongEvent.SONG_NEXT.register(songPlayer -> {
    // 播放列表中的下一首歌曲开始
});
```

可用事件：`SONG_END`、`SONG_NEXT`、`SONG_STOPPED`、`SONG_LOOP`、`SONG_DESTROYING`、`PLAYER_RANGE_STATE_CHANGE`。

## 文档

循环播放、淡入淡出、声道模式等高级用法请参阅原版 [NoteBlockAPI Wiki](https://github.com/koca2000/NoteBlockAPI/wiki)。

> 注意：Wiki 是为 Bukkit 版本编写的。本 Mod 版本的 API 基本相同，但使用 Architectury 事件代替 Bukkit 事件，使用 `Vec3`/`Level` 代替 `Location`。

## 许可证

[LGPL-3.0](LICENSE.txt)

## 致谢

原版 [NoteBlockAPI](https://github.com/koca2000/NoteBlockAPI) 由 xxmicloxx、michidk、koca2000、Luck 开发。
