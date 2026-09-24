package org.tomdang.quest.progress;

import java.util.Collection;
import java.util.UUID;

public interface QuestProgressRepository {
	Collection<QuestProgress> load(UUID playerId);
	void save(QuestProgress progress);
	void delete(UUID playerId, String questId);
}
