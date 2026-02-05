/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ultreon.qvoxel.server;

import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * The WorldSaveInfo class encapsulates information about the state of a saved game world,
 * including its seed, generator version, game mode, last played game mode, name, and the last save time.
 */
public final class WorldSaveInfo {
    private long seed;
    private final int generatorVersion;
    private final @NotNull dev.ultreon.qvoxel.server.GameMode gamemode;
    private @Nullable GameMode lastPlayedGamemode;
    private @NotNull String name;
    private LocalDateTime lastSave;
    private int colorArgb;
    private boolean favorite;
    private boolean deleted;
    private LocalDateTime deletionDate;

    /**
     * Constructs a new WorldSaveInfo instance with the specified parameters.
     *
     * @param seed             The seed value for the world generation.
     * @param generatorVersion The version of the world generator used.
     * @param gamemode         The current game mode.
     * @param lastPlayedGamemode The game mode used the last time the world was played.
     * @param name             The name of the world.
     * @param colorArgb            The color to display in the selection screen.
     * @param lastSave         The date and time when the world was last saved.
     */
    public WorldSaveInfo(long seed, int generatorVersion, @NotNull GameMode gamemode, @Nullable GameMode lastPlayedGamemode, @NotNull String name,
                         int colorArgb, @NotNull LocalDateTime lastSave) {
        this.seed = seed;
        this.generatorVersion = generatorVersion;
        this.gamemode = gamemode;
        this.lastPlayedGamemode = lastPlayedGamemode;
        this.name = name;
        this.colorArgb = colorArgb;
        this.lastSave = lastSave;
    }

    public static WorldSaveInfo fromMap(MapType infoData) {
        WorldSaveInfo worldSaveInfo = new WorldSaveInfo(
                infoData.getLong("seed", 0),
                infoData.getInt("generatorVersion", 0),
                Objects.requireNonNull(GameMode.byOrdinal(infoData.getInt("gamemode", GameMode.SURVIVAL.ordinal()))),
                GameMode.byOrdinal(infoData.getInt("lastPlayedIn", GameMode.SURVIVAL.ordinal())),
                infoData.getString("name", "unnamed world"),
                infoData.getInt("color", 0xFF000000),
                LocalDateTime.ofEpochSecond(infoData.getLong("lastSave"), 0, ZoneOffset.UTC)
        );
        worldSaveInfo.favorite = infoData.getBoolean("favorite", false);
        if (infoData.getBoolean("deleted", false)) {
            long deletionDate1 = infoData.getLong("deletionDate", -1L);
            if (deletionDate1 < 0) {
                worldSaveInfo.deleted = false;
            } else {
                worldSaveInfo.deleted = true;
                worldSaveInfo.deletionDate = LocalDateTime.ofEpochSecond(deletionDate1, 0, ZoneOffset.UTC);
            }
        }
        return worldSaveInfo;
    }

    public long getSeed() {
        return seed;
    }

    public int getGeneratorVersion() {
        return generatorVersion;
    }

    public GameMode getGamemode() {
        return gamemode;
    }

    public GameMode getLastPlayedGamemode() {
        return lastPlayedGamemode;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getLastSave() {
        return lastSave;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WorldSaveInfo that = (WorldSaveInfo) o;
        return seed == that.seed && generatorVersion == that.generatorVersion && colorArgb == that.colorArgb && favorite == that.favorite && deleted == that.deleted && gamemode == that.gamemode && lastPlayedGamemode == that.lastPlayedGamemode && Objects.equals(name, that.name) && Objects.equals(lastSave, that.lastSave) && Objects.equals(deletionDate, that.deletionDate);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Long.hashCode(seed);
        hash = 31 * hash + generatorVersion;
        hash = 31 * hash + gamemode.hashCode();
        hash = 31 * hash + (lastPlayedGamemode != null ? lastPlayedGamemode.hashCode() : 0);
        hash = 31 * hash + name.hashCode();
        hash = 31 * hash + (lastSave != null ? lastSave.hashCode() : 0);
        hash = 31 * hash + colorArgb;
        hash = 31 * hash + (favorite ? 1 : 0);
        hash = 31 * hash + (deleted ? 1 : 0);
        hash = 31 * hash + (deletionDate != null ? deletionDate.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "WorldSaveInfo[" +
                "seed=" + seed + ", " +
                "generatorVersion=" + generatorVersion + ", " +
                "gamemode=" + gamemode + ", " +
                "lastPlayedGamemode=" + lastPlayedGamemode + ", " +
                "name=" + name + ", " +
                "lastSave=" + lastSave + ']';
    }

    public void setName(String name) {
        this.name = name;
        lastSave = LocalDateTime.now();
    }

    public void save(WorldStorage storage) throws IOException {
        MapType save = new MapType();
        save.putLong("seed", seed);
        save.putInt("generatorVersion", generatorVersion);
        save.putInt("gamemode", gamemode.ordinal());
        save.putInt("lastPlayedGamemode", lastPlayedGamemode == null ? gamemode.ordinal() : lastPlayedGamemode.ordinal());
        save.putString("name", name);
        save.putInt("color", colorArgb);
        save.putLong("lastSave", lastSave.toEpochSecond(ZoneOffset.UTC));
        save.putBoolean("favorite", favorite);
        save.putBoolean("deleted", deleted);
        if (deleted) {
            if (deletionDate == null) {
                deletionDate = LocalDateTime.now();
            }
            save.putLong("deletionDate", deletionDate.toEpochSecond(ZoneOffset.UTC));
        }
        storage.write(save, "info.ubo");
    }

    public void setLastPlayedGamemode(@Nullable GameMode lastPlayedGamemode) {
        this.lastPlayedGamemode = lastPlayedGamemode;
    }

    public int getColorArgb() {
        return colorArgb;
    }

    public void setColorArgb(int colorArgb) {
        this.colorArgb = colorArgb;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void delete() {
        deleted = true;
        deletionDate = LocalDateTime.now();
    }

    public void restore() {
        deleted = false;
        deletionDate = null;
    }

    public LocalDateTime getDeletionDate() {
        return deletionDate;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public void setLastSave(LocalDateTime lastSave) {
        this.lastSave = lastSave;
    }
}
