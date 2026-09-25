package org.tomdang.quest.orchestration;

import org.junit.jupiter.api.Test;
import org.tomdang.quest.definition.*;
import org.tomdang.quest.progress.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class QuestOrchestrationServiceTest {
	@Test void runsConditionsActionsLifecycleAndIdempotentRewards() {
		List<String> actionsRun = new ArrayList<>();
		List<QuestLifecycleType> lifecycleEvents = new ArrayList<>();
		AtomicInteger rewardsGranted = new AtomicInteger();
		QuestHandlerRegistry<QuestAction> actions = new QuestHandlerRegistry<>("action");
		QuestHandlerRegistry<QuestCondition> conditions = new QuestHandlerRegistry<>("condition");
		QuestHandlerRegistry<QuestReward> rewards = new QuestHandlerRegistry<>("reward");
		actions.register("LOG", (context, parameters) -> actionsRun.add(parameters.get("value")));
		conditions.register("ALLOW", (context, parameters) -> true);
		rewards.register("COUNT", (context, parameters) -> rewardsGranted.incrementAndGet());
		QuestLifecycleBus lifecycle = new QuestLifecycleBus();
		lifecycle.subscribe(event -> lifecycleEvents.add(event.type()));
		QuestOrchestrationService orchestration = new QuestOrchestrationService(actions, conditions, rewards,
				new InMemoryQuestRewardLedger(), lifecycle);
		QuestRegistry registry = new QuestRegistry();
		QuestDefinition definition = quest();
		registry.register(definition);
		registry.validateAndSeal();
		orchestration.validate(registry);
		QuestProgressService progress = new QuestProgressService(registry, new InMemoryQuestProgressRepository(), orchestration);
		UUID player = UUID.randomUUID();
		progress.load(player);

		progress.start(player, "ORCHESTRATED");
		progress.signal(player, QuestSignal.one(QuestObjectiveType.CUSTOM, "DONE"));

		assertTrue(progress.isCompleted(player, "ORCHESTRATED"));
		assertEquals(List.of("enter", "exit"), actionsRun);
		assertEquals(List.of(QuestLifecycleType.STARTED, QuestLifecycleType.STAGE_ENTERED,
				QuestLifecycleType.STAGE_EXITED, QuestLifecycleType.COMPLETED), lifecycleEvents);
		assertEquals(1, rewardsGranted.get());
		orchestration.completed(definition, progress.progress(player, "ORCHESTRATED").orElseThrow());
		assertEquals(1, rewardsGranted.get());
	}

	@Test void rejectsUnknownHandlersBeforeRuntime() {
		QuestOrchestrationService orchestration = new QuestOrchestrationService(
				new QuestHandlerRegistry<>("action"), new QuestHandlerRegistry<>("condition"),
				new QuestHandlerRegistry<>("reward"), new InMemoryQuestRewardLedger(), new QuestLifecycleBus());
		QuestRegistry registry = new QuestRegistry();
		registry.register(quest()); registry.validateAndSeal();
		assertThrows(IllegalArgumentException.class, () -> orchestration.validate(registry));
	}

	private QuestDefinition quest() {
		QuestObjectiveDefinition objective = new QuestObjectiveDefinition("DONE", QuestObjectiveType.CUSTOM,
				"DONE", 1, false, Map.of());
		QuestStageDefinition stage = new QuestStageDefinition("ONLY", "Only", List.of(objective), null, Map.of(),
				List.of(new QuestActionDefinition("ENTER", "LOG", Map.of("value", "enter"))),
				List.of(new QuestActionDefinition("EXIT", "LOG", Map.of("value", "exit"))),
				List.of(new QuestConditionDefinition("CAN_FINISH", "ALLOW", Map.of())));
		return new QuestDefinition("ORCHESTRATED", "Orchestrated", "", "TEST", QuestRepeatability.ONCE,
				QuestStartPolicy.MANUAL, Set.of(), "ONLY", Map.of("ONLY", stage),
				List.of(new QuestConditionDefinition("CAN_START", "ALLOW", Map.of())),
				List.of(new QuestRewardDefinition("REWARD", "COUNT", Map.of())));
	}
}
