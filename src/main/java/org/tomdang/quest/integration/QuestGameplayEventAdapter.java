package org.tomdang.quest.integration;

import org.tomdang.gameplay.event.GameplayEventBus;
import org.tomdang.gameplay.event.GameplayEventSubscription;
import org.tomdang.gameplay.event.type.ActorInteracted;
import org.tomdang.gameplay.event.type.CritterCaptured;
import org.tomdang.gameplay.event.type.CritterObserved;
import org.tomdang.gameplay.event.type.DialogueCompleted;
import org.tomdang.gameplay.event.type.EncounterCompleted;
import org.tomdang.gameplay.event.type.ItemCollected;
import org.tomdang.gameplay.event.type.MobDefeated;
import org.tomdang.gameplay.event.type.RegionEntered;
import org.tomdang.quest.definition.QuestObjectiveType;
import org.tomdang.quest.progress.QuestProgressService;
import org.tomdang.quest.progress.QuestSignal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** The only production bridge from generic gameplay facts into quest progression. */
public final class QuestGameplayEventAdapter implements AutoCloseable {
	private final List<GameplayEventSubscription> subscriptions = new ArrayList<>();

	public QuestGameplayEventAdapter(GameplayEventBus events, QuestProgressService quests) {
		if (events == null || quests == null) throw new IllegalArgumentException("events and quests cannot be null");
		subscriptions.add(events.subscribe(ActorInteracted.class, event -> {
			quests.signal(event.playerId(), one(QuestObjectiveType.INTERACT_WITH_ACTOR, event.actorId(), "actor-interacted"));
			quests.signal(event.playerId(), one(QuestObjectiveType.RETURN_TO_ACTOR, event.actorId(), "actor-interacted"));
		}));
		subscriptions.add(events.subscribe(RegionEntered.class, event ->
				quests.signal(event.playerId(), one(QuestObjectiveType.ENTER_REGION, event.regionId(), "region-entered"))));
		subscriptions.add(events.subscribe(ItemCollected.class, event -> quests.signal(event.playerId(),
				new QuestSignal(QuestObjectiveType.COLLECT_ITEM, event.itemId(), event.amount(), Map.of("event", "item-collected")))));
		subscriptions.add(events.subscribe(MobDefeated.class, event ->
				quests.signal(event.playerId(), one(QuestObjectiveType.DEFEAT_MOB, event.mobId(), "mob-defeated"))));
		subscriptions.add(events.subscribe(CritterObserved.class, event ->
				quests.signal(event.playerId(), one(QuestObjectiveType.OBSERVE_CRITTER, event.critterId(), "critter-observed"))));
		subscriptions.add(events.subscribe(CritterCaptured.class, event ->
				quests.signal(event.playerId(), one(QuestObjectiveType.CAPTURE_CRITTER, event.critterId(), "critter-captured"))));
		subscriptions.add(events.subscribe(DialogueCompleted.class, event -> quests.signal(event.playerId(),
				one(QuestObjectiveType.COMPLETE_DIALOGUE, event.dialogueId(), "dialogue-completed"))));
		subscriptions.add(events.subscribe(EncounterCompleted.class, event -> quests.signal(event.playerId(),
				one(QuestObjectiveType.COMPLETE_ENCOUNTER, event.encounterId(), "encounter-completed"))));
	}

	private static QuestSignal one(QuestObjectiveType type, String target, String eventName) {
		return new QuestSignal(type, target, 1, Map.of("event", eventName));
	}

	@Override public void close() {
		for (int index = subscriptions.size() - 1; index >= 0; index--) subscriptions.get(index).close();
		subscriptions.clear();
	}
}
