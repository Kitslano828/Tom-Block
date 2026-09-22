package org.tomdang.player.playerdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.tomdang.player.PlayerProfile;

class MigratingPlayerProfileRepositoryTest {
    @Test
    void importsLegacyProfileOnlyOnce() {
        UUID id = UUID.randomUUID();
        MemoryRepository postgres = new MemoryRepository();
        MemoryRepository yaml = new MemoryRepository();
        PlayerProfile old = new PlayerProfile(id);
        old.setMiningXP(1234);
        yaml.savePlayerProfile(old);
        var messages = new java.util.ArrayList<String>();
        var repository = new MigratingPlayerProfileRepository(postgres, yaml, messages::add);

        PlayerProfile firstLoad = new PlayerProfile(id);
        repository.loadPlayerProfile(firstLoad);
        assertEquals(1234, firstLoad.getMiningXP());
        assertTrue(postgres.containsPlayer(id));
        assertEquals(1, messages.size());

        old.setMiningXP(9999);
        yaml.savePlayerProfile(old);
        PlayerProfile secondLoad = new PlayerProfile(id);
        repository.loadPlayerProfile(secondLoad);
        assertEquals(1234, secondLoad.getMiningXP());
        assertEquals(1, messages.size());
    }

    private static final class MemoryRepository implements PlayerProfileRepository {
        private final Map<UUID, Long> miningXp = new HashMap<>();

        @Override public void createPlayerProfile(PlayerProfile player) { savePlayerProfile(player); }
        @Override public void savePlayerProfile(PlayerProfile player) { miningXp.put(player.getUuid(), player.getMiningXP()); }
        @Override public void loadPlayerProfile(PlayerProfile player) {
            Long value = miningXp.get(player.getUuid());
            if (value != null) player.restoreSkillXp(value, 0);
        }
        @Override public boolean containsPlayer(UUID playerId) { return miningXp.containsKey(playerId); }
    }
}
