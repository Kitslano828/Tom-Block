package org.tomdang.quest.definition;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuestRegistryTest {
	@Test
	void validatesPrerequisitesAfterAllQuestsAreRegistered() {
		QuestRegistry registry = new QuestRegistry();
		registry.register(quest("SECOND", Set.of("FIRST")));
		assertThrows(IllegalArgumentException.class, registry::validatePrerequisites);
		registry.register(quest("FIRST", Set.of()));
		registry.validatePrerequisites();
		assertEquals(2, registry.all().size());
	}

	private QuestDefinition quest(String id, Set<String> prerequisites) {
		QuestObjectiveDefinition objective = new QuestObjectiveDefinition(
				"DONE", QuestObjectiveType.CUSTOM, "DONE", 1, false, Map.of());
		QuestStageDefinition stage = new QuestStageDefinition(
				"END", "End", List.of(objective), null, Map.of());
		return new QuestDefinition(id, id, "", "TEST", QuestRepeatability.ONCE,
				prerequisites, "END", Map.of("END", stage));
	}
}
