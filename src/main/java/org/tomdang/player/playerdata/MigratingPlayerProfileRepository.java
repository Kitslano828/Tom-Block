package org.tomdang.player.playerdata;

import java.util.UUID;
import java.util.function.Consumer;
import org.tomdang.player.PlayerProfile;

/** Reads PostgreSQL first and imports a legacy YAML profile on its first access. */
public final class MigratingPlayerProfileRepository implements PlayerProfileRepository {
    private final PlayerProfileRepository primary;
    private final PlayerProfileRepository legacy;
    private final Consumer<String> migrationLog;

    public MigratingPlayerProfileRepository(PlayerProfileRepository primary,
                                            PlayerProfileRepository legacy,
                                            Consumer<String> migrationLog) {
        this.primary = primary;
        this.legacy = legacy;
        this.migrationLog = migrationLog;
    }

    @Override
    public void createPlayerProfile(PlayerProfile player) {
        primary.createPlayerProfile(player);
    }

    @Override
    public void savePlayerProfile(PlayerProfile player) {
        primary.savePlayerProfile(player);
    }

    @Override
    public void loadPlayerProfile(PlayerProfile player) {
        if (primary.containsPlayer(player.getUuid())) {
            primary.loadPlayerProfile(player);
            return;
        }
        if (legacy.containsPlayer(player.getUuid())) {
            legacy.loadPlayerProfile(player);
            primary.savePlayerProfile(player);
            migrationLog.accept("Imported legacy YAML profile " + player.getUuid() + " into PostgreSQL");
        }
    }

    @Override
    public boolean containsPlayer(UUID playerId) {
        return primary.containsPlayer(playerId) || legacy.containsPlayer(playerId);
    }

    @Override
    public void close() {
        primary.close();
    }
}
