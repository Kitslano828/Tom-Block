package org.tomdang.quest.presentation;

import org.junit.jupiter.api.Test;
import org.tomdang.quest.definition.QuestDefinition;
import org.tomdang.quest.definition.QuestObjectiveDefinition;
import org.tomdang.quest.definition.QuestObjectiveType;
import org.tomdang.quest.definition.QuestRegistry;
import org.tomdang.quest.definition.QuestRepeatability;
import org.tomdang.quest.definition.QuestStageDefinition;
import org.tomdang.quest.progress.InMemoryQuestProgressRepository;
import org.tomdang.quest.progress.QuestProgressService;
import org.tomdang.quest.progress.QuestSignal;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestActorMarkerResolverTest {
	private static final String ACTOR = "WILL";

	@Test void followsOfferActiveReturnAndCompletedStates() {
		QuestRegistry definitions = new QuestRegistry();
		definitions.register(quest());
		QuestProgressService progress = new QuestProgressService(definitions, new InMemoryQuestProgressRepository());
		QuestOfferRegistry offers = new QuestOfferRegistry();
		offers.register(new QuestOfferDefinition(ACTOR, "HUNT"));
		QuestActorMarkerResolver resolver = new QuestActorMarkerResolver(offers, definitions, progress);
		UUID player = UUID.randomUUID();

		assertTrue(resolver.resolve(player, ACTOR).isEmpty());
		progress.load(player);
		assertEquals(QuestActorMarker.QUEST, resolver.resolve(player, ACTOR).orElseThrow());

		progress.start(player, "HUNT");
		assertTrue(resolver.resolve(player, ACTOR).isEmpty());
		progress.signal(player, QuestSignal.one(QuestObjectiveType.OBSERVE_CRITTER, "GLIMMERFLY"));
		assertEquals(QuestActorMarker.COMPLETE, resolver.resolve(player, ACTOR).orElseThrow());

		progress.signal(player, QuestSignal.one(QuestObjectiveType.COMPLETE_DIALOGUE, "WILL_RETURN"));
		assertTrue(resolver.resolve(player, ACTOR).isEmpty());
	}

	private QuestDefinition quest() {
		QuestStageDefinition observe = new QuestStageDefinition("OBSERVE", "Observe", List.of(
				new QuestObjectiveDefinition("WATCH", QuestObjectiveType.OBSERVE_CRITTER, "GLIMMERFLY", 1, false, Map.of())),
				"RETURN", Map.of());
		QuestStageDefinition handIn = new QuestStageDefinition("RETURN", "Return", List.of(
				new QuestObjectiveDefinition("REPORT", QuestObjectiveType.COMPLETE_DIALOGUE, "WILL_RETURN", 1, false,
						Map.of("actor", ACTOR))), null, Map.of());
		return new QuestDefinition("HUNT", "First Hunt", "", "HUNTING", QuestRepeatability.ONCE,
				Set.of(), "OBSERVE", Map.of("OBSERVE", observe, "RETURN", handIn));
	}
}
