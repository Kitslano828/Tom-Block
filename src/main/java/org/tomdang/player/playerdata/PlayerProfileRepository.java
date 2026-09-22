package org.tomdang.player.playerdata;

import java.util.UUID;
import org.tomdang.player.PlayerProfile;

/** Persistence boundary for player profiles. */
public interface PlayerProfileRepository extends AutoCloseable {
    void createPlayerProfile(PlayerProfile player);
    void savePlayerProfile(PlayerProfile player);
    void loadPlayerProfile(PlayerProfile player);
    boolean containsPlayer(UUID playerId);

    default void flush() {
    }

    @Override
    default void close() {
    }
}
