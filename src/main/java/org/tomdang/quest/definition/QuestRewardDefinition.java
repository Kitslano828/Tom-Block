package org.tomdang.quest.definition;
import java.util.Map;
public record QuestRewardDefinition(String id, String type, Map<String, String> parameters) {
	public QuestRewardDefinition {
		if (id == null || id.isBlank() || type == null || type.isBlank()) throw new IllegalArgumentException("Reward id and type are required");
		parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
	}
}
