package org.tomdang.bootstrap;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.tomdang.actorframework.audience.ActorAudienceResolver;
import org.tomdang.actorframework.combat.ActorDamageService;
import org.tomdang.actorframework.instance.ActorInstanceRegistry;
import org.tomdang.actorframework.instance.ActorInstanceService;
import org.tomdang.actorframework.interaction.ActorInteractionRegistry;
import org.tomdang.actorframework.interaction.ActorInteractionService;
import org.tomdang.actorframework.lifecycle.ActorLifecycleService;
import org.tomdang.actorframework.movement.*;
import org.tomdang.actorframework.presentation.ActiveActorPresentationRegistry;
import org.tomdang.actorframework.presentation.ActorPresentationService;
import org.tomdang.actorframework.presentation.ActorPresentationTypeRegistry;
import org.tomdang.actorframework.presentation.bukkit.BukkitActorCollisionService;
import org.tomdang.actorframework.presentation.bukkit.BukkitVillagerPresentation;
import org.tomdang.actorframework.reconciliation.ActorReconciliationService;
import org.tomdang.actorframework.registry.ActorRegistry;
import org.tomdang.actorframework.resolver.ActorResolver;
import org.tomdang.actorframework.spawn.ActorSpawnPointRegistry;
import org.tomdang.actorframework.spawn.ActorSpawnPointService;

public class ActorBootStrap {

	@Getter
	private final ActorRegistry actorRegistry;
	@Getter
	private final ActorInstanceRegistry actorInstanceRegistry;
	@Getter
	private final ActorInstanceService actorInstanceService;
	@Getter
	private final ActorPresentationService actorPresentationService;
	@Getter
	private final ActorLifecycleService actorLifecycleService;
	@Getter
	private final ActorSpawnPointRegistry actorSpawnPointRegistry;
	@Getter
	private final ActorSpawnPointService actorSpawnPointService;
	@Getter
	private final ActorResolver actorResolver;
	@Getter
	private final ActorInteractionRegistry actorInteractionRegistry;
	@Getter
	private final ActorInteractionService actorInteractionService;
	@Getter
	private final ActorDamageService actorDamageService;
	@Getter
	private final ActorReconciliationService actorReconciliationService;
	@Getter
	private final ActiveActorPresentationRegistry activeActorPresentationRegistry;
	@Getter
	private final ActorPresentationTypeRegistry actorPresentationTypeRegistry;
	@Getter
	private final ActorAudienceResolver actorAudienceResolver;
	@Getter
	private final BukkitActorCollisionService bukkitActorCollisionService;
	@Getter
	private final LinearMovementStepCalculator linearMovementStepCalculator;
	private final ActorMovementTaskRegistry actorMovementTaskRegistry;
	@Getter
	private final LinearActorMovementService linearActorMovementService;
	@Getter
	private final ActorMovementTaskService actorMovementTaskService;
	@Getter
	private final ActorFollowService actorFollowService;

	public ActorBootStrap(Plugin plugin, NamespacedKey actorInstanceIDKey, NamespacedKey actorDefinitionIDKey,
	                      NamespacedKey actorAudienceScopeKey, NamespacedKey actorAudienceIDKey,
	                      NamespacedKey actorSpawnPointIDKey
	) {
		if (plugin == null) throw new IllegalArgumentException("plugin cannot be null");
		if (actorInstanceIDKey == null) throw new IllegalArgumentException("Actor instance ID Key cannot be null");
		if (actorDefinitionIDKey == null) throw new IllegalArgumentException("Actor definition ID Key cannot be null");
		if (actorAudienceIDKey == null) throw new IllegalArgumentException("Actor audience ID Key cannot be null");
		if (actorAudienceScopeKey == null) throw new IllegalArgumentException("Actor audience scope ID Key cannot be null");
		if (actorSpawnPointIDKey == null) throw new IllegalArgumentException("Actor spawn point ID Key cannot be null");

		actorRegistry = new ActorRegistry();

		actorAudienceResolver = new ActorAudienceResolver(Bukkit.getServer());

		ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
		if (scoreboardManager == null) throw new IllegalStateException("ScoreBoard Manager cannot be null");
		Scoreboard mainScoreboard = scoreboardManager.getMainScoreboard();
		bukkitActorCollisionService = new BukkitActorCollisionService(mainScoreboard);

		activeActorPresentationRegistry = new ActiveActorPresentationRegistry();
		actorPresentationTypeRegistry = new ActorPresentationTypeRegistry();

		BukkitVillagerPresentation bukkitVillagerPresentation = new BukkitVillagerPresentation(
				actorInstanceIDKey,
				actorDefinitionIDKey,
				actorAudienceScopeKey,
				actorAudienceIDKey,
				bukkitActorCollisionService,
				actorSpawnPointIDKey
		);

		actorPresentationTypeRegistry.registerPresentation("VILLAGER", bukkitVillagerPresentation);

		actorPresentationService = new ActorPresentationService(activeActorPresentationRegistry, actorPresentationTypeRegistry);

		linearMovementStepCalculator = new LinearMovementStepCalculator();
		actorMovementTaskRegistry = new ActorMovementTaskRegistry();
		actorMovementTaskService = new ActorMovementTaskService(plugin, actorMovementTaskRegistry);
		HorizontalFacingCalculator horizontalFacingCalculator = new HorizontalFacingCalculator();

		actorFollowService = new ActorFollowService(actorPresentationService, linearMovementStepCalculator, actorMovementTaskService, horizontalFacingCalculator);

		linearActorMovementService = new LinearActorMovementService(actorPresentationService, linearMovementStepCalculator, actorMovementTaskService);

		actorInteractionRegistry = new ActorInteractionRegistry();

		actorInteractionService = new ActorInteractionService(actorInteractionRegistry);


		actorInstanceRegistry = new ActorInstanceRegistry();
		actorInstanceService = new ActorInstanceService(actorRegistry, actorInstanceRegistry);

		actorResolver = new ActorResolver(
				actorInstanceIDKey,
				actorDefinitionIDKey, actorAudienceScopeKey,
				actorAudienceIDKey,
				actorSpawnPointIDKey,
				actorRegistry,
				actorInstanceRegistry
		);

		actorDamageService = new ActorDamageService(actorResolver);

		actorLifecycleService = new ActorLifecycleService(actorInstanceService, actorPresentationService, actorMovementTaskService);

		actorSpawnPointRegistry = new ActorSpawnPointRegistry();



		actorSpawnPointService = new ActorSpawnPointService(actorSpawnPointRegistry, actorLifecycleService);

		actorReconciliationService = new ActorReconciliationService(
				actorResolver,
				actorLifecycleService,
				actorSpawnPointRegistry,
				actorSpawnPointService
		);
	}

	public void shutDown() {
		actorMovementTaskService.cancelAll();
	}

}
