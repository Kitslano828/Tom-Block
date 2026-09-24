package org.tomdang.quest.progress;

import org.tomdang.quest.definition.QuestObjectiveType;

import java.util.Map;

public record QuestSignal(QuestObjectiveType type, String target, long amount, Map<String, String> context) {
	public QuestSignal {
		if (type == null) throw new IllegalArgumentException("Signal type cannot be null");
		if (target == null || target.isBlank()) throw new IllegalArgumentException("Signal target cannot be blank");
		if (amount <= 0) throw new IllegalArgumentException("Signal amount must be positive");
		if (context == null) throw new IllegalArgumentException("Signal context cannot be null");
		context = Map.copyOf(context);
	}

	public static QuestSignal one(QuestObjectiveType type, String target) {
		return new QuestSignal(type, target, 1, Map.of());
	}
}
