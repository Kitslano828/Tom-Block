package org.tomdang.quest.definition;
import java.util.Map;
public record QuestConditionDefinition(String id, String type, Map<String, String> parameters) {
	public QuestConditionDefinition {
		if (id == null || id.isBlank() || type == null || type.isBlank()) throw new IllegalArgumentException("Condition id and type are required");
		parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
	}
}
