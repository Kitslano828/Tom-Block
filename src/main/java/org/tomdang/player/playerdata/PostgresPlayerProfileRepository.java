package org.tomdang.player.playerdata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.stats.PlayerStatType;

public final class PostgresPlayerProfileRepository implements PlayerProfileRepository {
    private final DataSource dataSource;
    private final AutoCloseable owner;

    public PostgresPlayerProfileRepository(PostgresConfiguration configuration) {
        this(new PostgresDatabase(configuration), true);
    }

    public PostgresPlayerProfileRepository(PostgresDatabase database) {
        this(database, false);
    }

    private PostgresPlayerProfileRepository(PostgresDatabase database, boolean ownsDatabase) {
        this.dataSource = database.dataSource();
        this.owner = ownsDatabase ? database : null;
    }

    @Override
    public boolean containsPlayer(UUID playerId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM tomblock.player_profiles WHERE player_id = ?")) {
            statement.setObject(1, playerId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException exception) {
            throw failure("check player profile", exception);
        }
    }

    @Override
    public void createPlayerProfile(PlayerProfile player) {
        savePlayerProfile(player);
    }

    @Override
    public void savePlayerProfile(PlayerProfile player) {
        if (player == null) return;
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                upsertProfile(connection, player);
                upsertStats(connection, player);
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw failure("save player profile " + player.getUuid(), exception);
        }
    }

    private void upsertProfile(Connection connection, PlayerProfile player) throws SQLException {
        String sql = """
                INSERT INTO tomblock.player_profiles
                    (player_id, mining_xp, combat_xp, foraging_xp, skill_reward_version, prosperity)
                VALUES (?, ?, ?, ?, 2, ?)
                ON CONFLICT (player_id) DO UPDATE SET
                    mining_xp = EXCLUDED.mining_xp,
                    combat_xp = EXCLUDED.combat_xp,
                    foraging_xp = EXCLUDED.foraging_xp,
                    skill_reward_version = 2,
                    prosperity = EXCLUDED.prosperity,
                    updated_at = CURRENT_TIMESTAMP
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, player.getUuid());
            statement.setLong(2, player.getMiningXP());
            statement.setLong(3, player.getCombatXP());
            statement.setLong(4, player.getForagingXP());
            statement.setDouble(5, player.getProsperity());
            statement.executeUpdate();
        }
    }

    private void upsertStats(Connection connection, PlayerProfile player) throws SQLException {
        String sql = """
                INSERT INTO tomblock.player_base_stats (player_id, stat_key, stat_value)
                VALUES (?, ?, ?)
                ON CONFLICT (player_id, stat_key) DO UPDATE SET stat_value = EXCLUDED.stat_value
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (PlayerStatType stat : PlayerStatType.values()) {
                statement.setObject(1, player.getUuid());
                statement.setString(2, stat.getStorageKey());
                statement.setDouble(3, player.getStats().get(stat));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    @Override
    public void loadPlayerProfile(PlayerProfile player) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT mining_xp, combat_xp, foraging_xp, prosperity FROM tomblock.player_profiles WHERE player_id = ?")) {
                statement.setObject(1, player.getUuid());
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) return;
                    player.restoreSkillXp(result.getLong("mining_xp"), result.getLong("combat_xp"), result.getLong("foraging_xp"));
                    player.setProsperity(result.getDouble("prosperity"));
                }
            }
            for (PlayerStatType stat : PlayerStatType.values()) player.getStats().reset(stat);
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT stat_key, stat_value FROM tomblock.player_base_stats WHERE player_id = ?")) {
                statement.setObject(1, player.getUuid());
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        PlayerStatType stat = findStat(result.getString("stat_key"));
                        if (stat != null) player.setStat(stat, result.getDouble("stat_value"));
                    }
                }
            }
        } catch (SQLException exception) {
            throw failure("load player profile " + player.getUuid(), exception);
        }
    }

    private PlayerStatType findStat(String storageKey) {
        for (PlayerStatType stat : PlayerStatType.values()) {
            if (stat.getStorageKey().equals(storageKey)) return stat;
        }
        return null;
    }

    private IllegalStateException failure(String action, SQLException exception) {
        return new IllegalStateException("Failed to " + action, exception);
    }

    @Override
    public void close() {
        if (owner == null) return;
        try {
            owner.close();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to close PostgreSQL database", exception);
        }
    }
}
