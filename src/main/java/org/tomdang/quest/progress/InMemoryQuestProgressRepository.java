package org.tomdang.quest.progress;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class InMemoryQuestProgressRepository implements QuestProgressRepository {
	private final Map<PlayerQuestKey, QuestProgress> entries = new LinkedHashMap<>();

	@Override public synchronized Collection<QuestProgress> load(UUID playerId) {
		return entries.entrySet().stream().filter(entry -> entry.getKey().playerId.equals(playerId))
				.map(Map.Entry::getValue).toList();
	}

	@Override public synchronized void save(QuestProgress progress) {
		entries.put(new PlayerQuestKey(progress.playerId(), progress.questId()), progress);
	}

	@Override public synchronized void delete(UUID playerId, String questId) {
		entries.remove(new PlayerQuestKey(playerId, questId));
	}

	private record PlayerQuestKey(UUID playerId, String questId) { }
}
