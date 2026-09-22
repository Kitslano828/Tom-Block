package org.tomdang.island;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public final class PostgresPrivateIslandRepository implements PrivateIslandRepository {
	private final DataSource dataSource;
	public PostgresPrivateIslandRepository(DataSource dataSource) { this.dataSource = dataSource; }

	@Override public Optional<PrivateIsland> findByOwner(UUID ownerId) {
		String sql = "SELECT island_id, owner_id, world_name, preset_key FROM tomblock.private_islands WHERE owner_id = ?";
		try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setObject(1, ownerId);
			try (ResultSet result = statement.executeQuery()) { return result.next() ? Optional.of(read(result)) : Optional.empty(); }
		} catch (SQLException exception) { throw new IllegalStateException("Could not load private island", exception); }
	}

	@Override public PrivateIsland createForOwner(UUID ownerId) {
		Optional<PrivateIsland> existing = findByOwner(ownerId);
		if (existing.isPresent()) return existing.get();
		PrivateIsland island = PrivateIsland.starter(ownerId);
		String islandSql = "INSERT INTO tomblock.private_islands (island_id, owner_id, world_name, preset_key) VALUES (?, ?, ?, ?) ON CONFLICT (owner_id) DO NOTHING";
		String memberSql = "INSERT INTO tomblock.private_island_members (island_id, player_id, role) VALUES (?, ?, 'OWNER') ON CONFLICT DO NOTHING";
		try (Connection connection = dataSource.getConnection()) {
			connection.setAutoCommit(false);
			try (PreparedStatement statement = connection.prepareStatement(islandSql)) {
				statement.setObject(1, island.islandId()); statement.setObject(2, ownerId);
				statement.setString(3, island.worldName()); statement.setString(4, island.presetKey()); statement.executeUpdate();
			}
			PrivateIsland stored = findByOwner(connection, ownerId).orElseThrow();
			try (PreparedStatement statement = connection.prepareStatement(memberSql)) {
				statement.setObject(1, stored.islandId()); statement.setObject(2, ownerId); statement.executeUpdate();
			}
			connection.commit();
			return stored;
		} catch (SQLException exception) { throw new IllegalStateException("Could not create private island", exception); }
	}

	private Optional<PrivateIsland> findByOwner(Connection connection, UUID ownerId) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement("SELECT island_id, owner_id, world_name, preset_key FROM tomblock.private_islands WHERE owner_id = ?")) {
			statement.setObject(1, ownerId);
			try (ResultSet result = statement.executeQuery()) { return result.next() ? Optional.of(read(result)) : Optional.empty(); }
		}
	}
	private PrivateIsland read(ResultSet result) throws SQLException {
		return new PrivateIsland(result.getObject("island_id", UUID.class), result.getObject("owner_id", UUID.class),
				result.getString("world_name"), result.getString("preset_key"));
	}
}
