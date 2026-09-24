package org.tomdang.quest.progress;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class PostgresQuestProgressRepository implements QuestProgressRepository {
	private final DataSource dataSource;

	public PostgresQuestProgressRepository(DataSource dataSource) {
		if (dataSource == null) throw new IllegalArgumentException("Data source cannot be null");
		this.dataSource = dataSource;
	}

	@Override public Collection<QuestProgress> load(UUID playerId) {
		String questSql = "SELECT * FROM tomblock.player_quests WHERE player_id = ?";
		String objectiveSql = "SELECT stage_id, objective_id, amount FROM tomblock.player_quest_objectives WHERE player_id = ? AND quest_id = ?";
		try (Connection connection = dataSource.getConnection();
		     PreparedStatement quests = connection.prepareStatement(questSql)) {
			quests.setObject(1, playerId);
			Collection<QuestProgress> loaded = new ArrayList<>();
			try (ResultSet result = quests.executeQuery()) {
				while (result.next()) {
					String questId = result.getString("quest_id");
					Map<QuestObjectiveProgressKey, Long> objectives = new LinkedHashMap<>();
					try (PreparedStatement objectiveStatement = connection.prepareStatement(objectiveSql)) {
						objectiveStatement.setObject(1, playerId);
						objectiveStatement.setString(2, questId);
						try (ResultSet objectiveResult = objectiveStatement.executeQuery()) {
							while (objectiveResult.next()) objectives.put(
									new QuestObjectiveProgressKey(objectiveResult.getString("stage_id"), objectiveResult.getString("objective_id")),
									objectiveResult.getLong("amount"));
						}
					}
					loaded.add(new QuestProgress(playerId, questId, QuestStatus.valueOf(result.getString("status")),
							result.getString("current_stage_id"), objectives, result.getLong("revision"),
							instant(result, "started_at"), instant(result, "updated_at"), nullableInstant(result, "completed_at")));
				}
			}
			return loaded;
		} catch (SQLException exception) {
			throw new IllegalStateException("Could not load quest progress for " + playerId, exception);
		}
	}

	@Override public void save(QuestProgress progress) {
		String questSql = """
				INSERT INTO tomblock.player_quests
				(player_id, quest_id, status, current_stage_id, revision, started_at, updated_at, completed_at)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?)
				ON CONFLICT (player_id, quest_id) DO UPDATE SET status = EXCLUDED.status,
				current_stage_id = EXCLUDED.current_stage_id, revision = EXCLUDED.revision,
				started_at = EXCLUDED.started_at, updated_at = EXCLUDED.updated_at,
				completed_at = EXCLUDED.completed_at
				""";
		String deleteObjectives = "DELETE FROM tomblock.player_quest_objectives WHERE player_id = ? AND quest_id = ?";
		String objectiveSql = "INSERT INTO tomblock.player_quest_objectives (player_id, quest_id, stage_id, objective_id, amount, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
		try (Connection connection = dataSource.getConnection()) {
			connection.setAutoCommit(false);
			try {
				try (PreparedStatement statement = connection.prepareStatement(questSql)) {
					statement.setObject(1, progress.playerId()); statement.setString(2, progress.questId());
					statement.setString(3, progress.status().name()); statement.setString(4, progress.currentStageId());
					statement.setLong(5, progress.revision()); statement.setObject(6, postgresTime(progress.startedAt()));
					statement.setObject(7, postgresTime(progress.updatedAt())); statement.setObject(8, postgresTime(progress.completedAt()));
					statement.executeUpdate();
				}
				try (PreparedStatement statement = connection.prepareStatement(deleteObjectives)) {
					statement.setObject(1, progress.playerId()); statement.setString(2, progress.questId()); statement.executeUpdate();
				}
				try (PreparedStatement statement = connection.prepareStatement(objectiveSql)) {
					for (Map.Entry<QuestObjectiveProgressKey, Long> entry : progress.objectiveProgress().entrySet()) {
						statement.setObject(1, progress.playerId()); statement.setString(2, progress.questId());
						statement.setString(3, entry.getKey().stageId()); statement.setString(4, entry.getKey().objectiveId());
						statement.setLong(5, entry.getValue()); statement.setObject(6, postgresTime(progress.updatedAt())); statement.addBatch();
					}
					statement.executeBatch();
				}
				connection.commit();
			} catch (SQLException exception) {
				connection.rollback();
				throw exception;
			}
		} catch (SQLException exception) {
			throw new IllegalStateException("Could not save quest progress for " + progress.playerId(), exception);
		}
	}

	@Override public void delete(UUID playerId, String questId) {
		try (Connection connection = dataSource.getConnection();
		     PreparedStatement statement = connection.prepareStatement(
				     "DELETE FROM tomblock.player_quests WHERE player_id = ? AND quest_id = ?")) {
			statement.setObject(1, playerId); statement.setString(2, questId); statement.executeUpdate();
		} catch (SQLException exception) {
			throw new IllegalStateException("Could not delete quest progress", exception);
		}
	}

	private static Instant instant(ResultSet result, String column) throws SQLException {
		return result.getObject(column, java.time.OffsetDateTime.class).toInstant();
	}

	private static Instant nullableInstant(ResultSet result, String column) throws SQLException {
		java.time.OffsetDateTime value = result.getObject(column, java.time.OffsetDateTime.class);
		return value == null ? null : value.toInstant();
	}

	private static OffsetDateTime postgresTime(Instant value) {
		return value == null ? null : OffsetDateTime.ofInstant(value, ZoneOffset.UTC);
	}
}
