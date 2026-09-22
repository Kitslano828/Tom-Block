package org.tomdang.island.block;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class PostgresBlockOriginStore implements BlockOriginStore {
	private final DataSource dataSource;
	private final Consumer<String> errorLog;
	private final Set<ManagedBlockPosition> positions = ConcurrentHashMap.newKeySet();
	private final ExecutorService writes = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = new Thread(runnable, "tomblock-block-origins");
		thread.setDaemon(true);
		return thread;
	});

	public PostgresBlockOriginStore(DataSource dataSource, Consumer<String> errorLog) {
		this.dataSource = dataSource;
		this.errorLog = errorLog;
		load();
	}

	private void load() {
		String sql = "SELECT world_name, block_x, block_y, block_z FROM tomblock.managed_block_origins";
		try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement(sql);
			 var rows = statement.executeQuery()) {
			while (rows.next()) positions.add(new ManagedBlockPosition(rows.getString(1), rows.getInt(2), rows.getInt(3), rows.getInt(4)));
		} catch (SQLException exception) {
			throw new IllegalStateException("Could not load managed block origins", exception);
		}
	}

	@Override public boolean isPlayerPlaced(ManagedBlockPosition position) { return positions.contains(position); }

	@Override public void recordPlacement(ManagedBlockPosition position, UUID playerId) {
		positions.add(position);
		write(() -> {
			String sql = "INSERT INTO tomblock.managed_block_origins (world_name, block_x, block_y, block_z, placed_by) " +
					"VALUES (?, ?, ?, ?, ?) ON CONFLICT (world_name, block_x, block_y, block_z) DO UPDATE SET placed_by = EXCLUDED.placed_by";
			try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement(sql)) {
				bind(statement, position); statement.setObject(5, playerId); statement.executeUpdate();
			}
		});
	}

	@Override public void remove(ManagedBlockPosition position) {
		positions.remove(position);
		write(() -> {
			String sql = "DELETE FROM tomblock.managed_block_origins WHERE world_name = ? AND block_x = ? AND block_y = ? AND block_z = ?";
			try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement(sql)) {
				bind(statement, position); statement.executeUpdate();
			}
		});
	}

	private void bind(java.sql.PreparedStatement statement, ManagedBlockPosition position) throws SQLException {
		statement.setString(1, position.worldName()); statement.setInt(2, position.x());
		statement.setInt(3, position.y()); statement.setInt(4, position.z());
	}
	private void write(SqlWrite write) {
		writes.execute(() -> { try { write.run(); } catch (SQLException exception) { errorLog.accept("Managed block origin write failed: " + exception.getMessage()); } });
	}
	@Override public void close() {
		writes.shutdown();
		try { if (!writes.awaitTermination(10, TimeUnit.SECONDS)) writes.shutdownNow(); }
		catch (InterruptedException exception) { Thread.currentThread().interrupt(); writes.shutdownNow(); }
	}
	@FunctionalInterface private interface SqlWrite { void run() throws SQLException; }
}
