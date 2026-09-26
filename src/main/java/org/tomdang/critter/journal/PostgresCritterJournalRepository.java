package org.tomdang.critter.journal;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PostgresCritterJournalRepository implements CritterJournalRepository {
    private static final String COLUMNS = "critter_id,knowledge,observations,successful_hunts,first_seen_at,last_seen_at";
    private final DataSource dataSource;

    public PostgresCritterJournalRepository(DataSource dataSource) { this.dataSource = Objects.requireNonNull(dataSource); }

    public Optional<CritterJournalEntry> find(UUID playerId, String critterId) {
        String sql = "SELECT " + COLUMNS + " FROM tomblock.critter_journal WHERE player_id=? AND critter_id=?";
        try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, playerId);
            statement.setString(2, critterId.toUpperCase(Locale.ROOT));
            try (var result = statement.executeQuery()) {
                return result.next() ? Optional.of(read(playerId, result)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load critter journal", exception);
        }
    }

    public List<CritterJournalEntry> findAll(UUID playerId) {
        String sql = "SELECT " + COLUMNS + " FROM tomblock.critter_journal WHERE player_id=? ORDER BY critter_id";
        try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, playerId);
            try (var result = statement.executeQuery()) {
                List<CritterJournalEntry> entries = new ArrayList<>();
                while (result.next()) entries.add(read(playerId, result));
                return List.copyOf(entries);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load critter collection", exception);
        }
    }

    public void save(CritterJournalEntry entry) {
        String sql = "INSERT INTO tomblock.critter_journal(player_id,critter_id,knowledge,observations,successful_hunts,first_seen_at,last_seen_at) VALUES(?,?,?,?,?,?,?) ON CONFLICT(player_id,critter_id) DO UPDATE SET knowledge=EXCLUDED.knowledge,observations=EXCLUDED.observations,successful_hunts=EXCLUDED.successful_hunts,last_seen_at=EXCLUDED.last_seen_at";
        try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, entry.playerId()); statement.setString(2, entry.critterId().toUpperCase(Locale.ROOT));
            statement.setString(3, entry.knowledge().name()); statement.setInt(4, entry.observations());
            statement.setInt(5, entry.successfulHunts()); statement.setTimestamp(6, Timestamp.from(entry.firstSeenAt()));
            statement.setTimestamp(7, Timestamp.from(entry.lastSeenAt())); statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not save critter journal", exception);
        }
    }

    private CritterJournalEntry read(UUID playerId, ResultSet result) throws SQLException {
        return new CritterJournalEntry(playerId, result.getString("critter_id"),
                CritterKnowledge.valueOf(result.getString("knowledge")), result.getInt("observations"),
                result.getInt("successful_hunts"), result.getTimestamp("first_seen_at").toInstant(),
                result.getTimestamp("last_seen_at").toInstant());
    }
}
