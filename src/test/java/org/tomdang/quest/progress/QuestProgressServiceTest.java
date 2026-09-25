package org.tomdang.quest.progress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomdang.quest.definition.QuestDefinition;
import org.tomdang.quest.definition.QuestObjectiveDefinition;
import org.tomdang.quest.definition.QuestObjectiveType;
import org.tomdang.quest.definition.QuestRegistry;
import org.tomdang.quest.definition.QuestRepeatability;
import org.tomdang.quest.definition.QuestStageDefinition;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestProgressServiceTest {
	private final UUID playerId = UUID.randomUUID();
	private QuestProgressService service;

	@BeforeEach void setUp() {
		QuestRegistry registry = new QuestRegistry();
		registry.register(quest());
		service = new QuestProgressService(registry, new InMemoryQuestProgressRepository(),
				Clock.fixed(Instant.parse("2026-09-24T00:00:00Z"), ZoneOffset.UTC));
		service.load(playerId);
	}

	@Test void advancesAutomaticStagesAndCompletesTerminalStage() {
		assertEquals("START", service.start(playerId, "QUEST").currentStageId());
		service.signal(playerId, QuestSignal.one(QuestObjectiveType.INTERACT_WITH_ACTOR, "WILL"));
		assertEquals("CAPTURE", service.progress(playerId, "QUEST").orElseThrow().currentStageId());
		service.signal(playerId, QuestSignal.one(QuestObjectiveType.CAPTURE_CRITTER, "GLIMMERFLY"));
		assertFalse(service.isCompleted(playerId, "QUEST"));
		service.signal(playerId, QuestSignal.one(QuestObjectiveType.CAPTURE_CRITTER, "GLIMMERFLY"));
		assertTrue(service.isCompleted(playerId, "QUEST"));
	}

	@Test void onceOnlyQuestCannotRestartAndResetRemovesIt() {
		service.start(playerId, "QUEST");
		service.complete(playerId, "QUEST");
		assertThrows(IllegalStateException.class, () -> service.start(playerId, "QUEST"));
		service.reset(playerId, "QUEST");
		assertTrue(service.progress(playerId, "QUEST").isEmpty());
	}

	@Test void reportsWhetherPlayerProgressIsLoaded() {
		assertTrue(service.isLoaded(playerId));
		service.unload(playerId);
		assertFalse(service.isLoaded(playerId));
	}

	private QuestDefinition quest() {
		QuestStageDefinition start = new QuestStageDefinition("START", "Start", List.of(
				new QuestObjectiveDefinition("TALK", QuestObjectiveType.INTERACT_WITH_ACTOR, "WILL", 1, false, Map.of())),
				"CAPTURE", Map.of());
		QuestStageDefinition capture = new QuestStageDefinition("CAPTURE", "Capture", List.of(
				new QuestObjectiveDefinition("CAPTURE_TWO", QuestObjectiveType.CAPTURE_CRITTER, "GLIMMERFLY", 2, false, Map.of())),
				null, Map.of());
		return new QuestDefinition("QUEST", "Quest", "", "TEST", QuestRepeatability.ONCE, Set.of(),
				"START", Map.of("START", start, "CAPTURE", capture));
	}
}
