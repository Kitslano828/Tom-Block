package org.tomdang.quest.definition;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class QuestRegistry {
	private final Map<String, QuestDefinition> quests = new LinkedHashMap<>();

	public void register(QuestDefinition quest) {
		if (quest == null) throw new IllegalArgumentException("Quest cannot be null");
		if (quests.putIfAbsent(quest.id(), quest) != null)
			throw new IllegalArgumentException("Duplicate quest id: " + quest.id());
	}

	public QuestDefinition require(String id) {
		QuestDefinition quest = quests.get(id);
		if (quest == null) throw new IllegalArgumentException("Unknown quest: " + id);
		return quest;
	}

	public Collection<QuestDefinition> all() {
		return java.util.List.copyOf(quests.values());
	}

	public void validatePrerequisites() {
		for (QuestDefinition quest : quests.values()) {
			for (String prerequisite : quest.prerequisites()) {
				if (!quests.containsKey(prerequisite))
					throw new IllegalArgumentException("Quest " + quest.id() + " has unknown prerequisite " + prerequisite);
			}
		}
	}
}
