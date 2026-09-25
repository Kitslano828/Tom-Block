package org.tomdang.platform;

import org.tomdang.platform.lifecycle.ServiceKey;
import org.tomdang.platform.session.PlayerSessionCoordinator;
import org.tomdang.platform.threading.MainThreadGuard;
import org.tomdang.gameplay.event.GameplayEventBus;
import org.tomdang.quest.orchestration.*;
import org.tomdang.encounter.runtime.*;
import org.tomdang.encounter.definition.EncounterRegistry;
import org.tomdang.hud.composition.HudRuntime;

public final class PlatformServices {
	public static final ServiceKey<PlayerSessionCoordinator> PLAYER_SESSIONS =
			ServiceKey.of("player-sessions", PlayerSessionCoordinator.class);
	public static final ServiceKey<HudRuntime> HUD =
			ServiceKey.of("hud", HudRuntime.class);
	public static final ServiceKey<MainThreadGuard> MAIN_THREAD =
			ServiceKey.of("main-thread", MainThreadGuard.class);
	public static final ServiceKey<GameplayEventBus> GAMEPLAY_EVENTS =
			ServiceKey.of("gameplay-events", GameplayEventBus.class);
	@SuppressWarnings("rawtypes") public static final ServiceKey<QuestHandlerRegistry> QUEST_ACTIONS =
			ServiceKey.of("quest-actions", QuestHandlerRegistry.class);
	@SuppressWarnings("rawtypes") public static final ServiceKey<QuestHandlerRegistry> QUEST_CONDITIONS =
			ServiceKey.of("quest-conditions", QuestHandlerRegistry.class);
	@SuppressWarnings("rawtypes") public static final ServiceKey<QuestHandlerRegistry> QUEST_REWARDS =
			ServiceKey.of("quest-rewards", QuestHandlerRegistry.class);
	public static final ServiceKey<QuestLifecycleBus> QUEST_LIFECYCLE =
			ServiceKey.of("quest-lifecycle", QuestLifecycleBus.class);
	public static final ServiceKey<EncounterRuntimeService> ENCOUNTERS =
			ServiceKey.of("encounters", EncounterRuntimeService.class);
	public static final ServiceKey<EncounterRegistry> ENCOUNTER_DEFINITIONS =
			ServiceKey.of("encounter-definitions", EncounterRegistry.class);
	public static final ServiceKey<EncounterBehaviorRegistry> ENCOUNTER_BEHAVIORS =
			ServiceKey.of("encounter-behaviors", EncounterBehaviorRegistry.class);

	private PlatformServices() {}
}
