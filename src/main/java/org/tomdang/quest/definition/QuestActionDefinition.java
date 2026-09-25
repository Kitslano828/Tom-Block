package org.tomdang.quest.definition;
import java.util.Map;
public record QuestActionDefinition(String id, String type, Map<String, String> parameters) {
	public QuestActionDefinition {
		if (id == null || id.isBlank() || type == null || type.isBlank()) throw new IllegalArgumentException("Action id and type are required");
		parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
	}
}
