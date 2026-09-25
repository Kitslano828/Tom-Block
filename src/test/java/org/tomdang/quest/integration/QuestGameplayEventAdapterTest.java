package org.tomdang.quest.integration;

import org.junit.jupiter.api.Test;
import org.tomdang.gameplay.event.GameplayEventBus;
import org.tomdang.gameplay.event.type.ActorInteracted;
import org.tomdang.gameplay.event.type.CritterCaptured;
import org.tomdang.platform.threading.MainThreadGuard;
import org.tomdang.quest.definition.QuestDefinition;
import org.tomdang.quest.definition.QuestObjectiveDefinition;
import org.tomdang.quest.definition.QuestObjectiveType;
import org.tomdang.quest.definition.QuestRegistry;
import org.tomdang.quest.definition.QuestRepeatability;
import org.tomdang.quest.definition.QuestStageDefinition;
import org.tomdang.quest.progress.InMemoryQuestProgressRepository;
import org.tomdang.quest.progress.QuestProgressService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestGameplayEventAdapterTest {
	@Test void convertsTypedGameplayFactsIntoQuestProgressAndCanDetach() {
		UUID playerId = UUID.randomUUID();
		QuestRegistry registry = new QuestRegistry();
		registry.register(quest());
		registry.validateAndSeal();
		QuestProgressService quests = new QuestProgressService(registry, new InMemoryQuestProgressRepository());
		quests.load(playerId);
		quests.start(playerId, "HUNT");
		GameplayEventBus events = new GameplayEventBus(new MainThreadGuard(() -> true));
		QuestGameplayEventAdapter adapter = new QuestGameplayEventAdapter(events, quests);

		events.publish(new ActorInteracted(playerId, "WILL", UUID.randomUUID()));
		assertEquals("CAPTURE", quests.progress(playerId, "HUNT").orElseThrow().currentStageId());
		events.publish(new CritterCaptured(playerId, "GLIMMERFLY"));
		assertFalse(quests.isCompleted(playerId, "HUNT"));
		events.publish(new CritterCaptured(playerId, "GLIMMERFLY"));
		assertTrue(quests.isCompleted(playerId, "HUNT"));

		adapter.close();
		quests.reset(playerId, "HUNT");
		quests.start(playerId, "HUNT");
		events.publish(new ActorInteracted(playerId, "WILL", null));
		assertEquals("START", quests.progress(playerId, "HUNT").orElseThrow().currentStageId());
	}

	private QuestDefinition quest() {
		QuestStageDefinition start = new QuestStageDefinition("START", "Start", List.of(
				new QuestObjectiveDefinition("TALK", QuestObjectiveType.INTERACT_WITH_ACTOR,
						"WILL", 1, false, Map.of())), "CAPTURE", Map.of());
		QuestStageDefinition capture = new QuestStageDefinition("CAPTURE", "Capture", List.of(
				new QuestObjectiveDefinition("CAPTURE_TWO", QuestObjectiveType.CAPTURE_CRITTER,
						"GLIMMERFLY", 2, false, Map.of())), null, Map.of());
		return new QuestDefinition("HUNT", "Hunt", "", "HUNTING", QuestRepeatability.REPEATABLE,
				Set.of(), "START", Map.of("START", start, "CAPTURE", capture));
	}
}
