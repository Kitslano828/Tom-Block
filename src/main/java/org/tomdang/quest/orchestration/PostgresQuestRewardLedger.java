package org.tomdang.quest.orchestration;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.UUID;
public final class PostgresQuestRewardLedger implements QuestRewardLedger {
	private final DataSource dataSource;
	public PostgresQuestRewardLedger(DataSource dataSource) { this.dataSource = dataSource; }
	@Override public boolean claim(UUID playerId, String deliveryId) {
		try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement(
				"INSERT INTO tomblock.quest_reward_deliveries (player_id, delivery_id) VALUES (?, ?) ON CONFLICT DO NOTHING")) {
			statement.setObject(1, playerId); statement.setString(2, deliveryId); return statement.executeUpdate() == 1;
		} catch (SQLException exception) { throw new IllegalStateException("Could not claim quest reward " + deliveryId, exception); }
	}
	@Override public void release(UUID playerId, String deliveryId) {
		try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement(
				"DELETE FROM tomblock.quest_reward_deliveries WHERE player_id = ? AND delivery_id = ?")) {
			statement.setObject(1, playerId); statement.setString(2, deliveryId); statement.executeUpdate();
		} catch (SQLException exception) { throw new IllegalStateException("Could not release quest reward " + deliveryId, exception); }
	}
}
