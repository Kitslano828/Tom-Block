package org.tomdang.bootstrap;

import org.tomdang.TomBlock;

import org.tomdang.custommobframework.custommobspawn.MobRegionConfinementListener;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.tomdang.actorframework.combat.ActorDamageService;
import org.tomdang.actorframework.interaction.ActorInteractionRegistry;
import org.tomdang.actorframework.interaction.ActorInteractionService;
import org.tomdang.actorframework.nameplate.layout.ActorNameplateLayout;
import org.tomdang.actorframework.nameplate.layout.ActorNameplateLayoutCalculator;
import org.tomdang.actorframework.nameplate.nms.NmsActorNameplateLineFactory;
import org.tomdang.actorframework.nameplate.nms.NmsActorNameplatePresentation;
import org.tomdang.actorframework.nameplate.nms.NmsActorNameplateViewer;
import org.tomdang.actorframework.nameplate.nms.runtime.NmsActorNameplateLineRegistry;
import org.tomdang.actorframework.nameplate.presentation.ActorNameplatePresentation;
import org.tomdang.actorframework.nameplate.presentation.ActorNameplatePresentationRegistry;
import org.tomdang.actorframework.reconciliation.ActorReconciliationService;
import org.tomdang.actorframework.registry.ActorRegistry;
import org.tomdang.actorframework.resolver.ActorResolver;
import org.tomdang.actorframework.spawn.ActorSpawnPointRegistry;
import org.tomdang.combat.weapons.WeaponCreator;
import org.tomdang.content.blacksmith.BlacksmithContent;
import org.tomdang.content.hunting.CritterHunterWillContent;
import org.tomdang.crafting.CraftingService;
import org.tomdang.customabilityframework.CustomAbilityRegistry;
import org.tomdang.customabilityframework.CustomAbilityService;
import org.tomdang.customarmorframework.CustomArmorCreator;
import org.tomdang.customarmorframework.CustomArmorRegistry;
import org.tomdang.customarmorframework.CustomArmorResolver;
import org.tomdang.customarmorframework.CustomArmorService;
import org.tomdang.customitemframework.CustomItemCreator;
import org.tomdang.customabilityframework.activeability.ActiveAbilityService;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.CustomItemStackFactory;
import org.tomdang.customitemframework.refresh.PlayerInventoryItemRefreshService;
import org.tomdang.custommobframework.CustomMobResolver;
import org.tomdang.custommobframework.custommobdrops.MobRewardService;
import org.tomdang.custommobframework.custommobhealth.CustomMobHealthService;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.advance.DialogueAdvanceService;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkinRegistry;
import org.tomdang.dialogueframework.session.DialogueSessionService;
import org.tomdang.dialogueframework.theme.DialogueThemeRegistry;
import org.tomdang.mining.miningtool.MiningToolCreator;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playeractionbar.PlayerActionBarService;
import org.tomdang.player.playerdata.PlayerProfileRepository;
import org.tomdang.player.playerresource.PlayerResourceService;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.playernpc.integration.actor.PlayerNpcActorInteractionService;
import org.tomdang.playernpc.integration.actor.PlayerNpcActorPresentation;
import org.tomdang.playernpc.integration.actor.PlayerNpcActorResolver;
import org.tomdang.playernpc.integration.actor.PlayerNpcActorVisibilityService;
import org.tomdang.playernpc.integration.actor.PlayerNpcProfileNameFactory;
import org.tomdang.playernpc.lifecycle.PlayerNpcLifecycleService;
import org.tomdang.playernpc.nms.NmsPlayerNpcFactory;
import org.tomdang.playernpc.nms.NmsPlayerNpcInteractionInterceptor;
import org.tomdang.playernpc.nms.NmsPlayerNpcViewer;
import org.tomdang.playernpc.runtime.PlayerNpcRegistry;
import org.tomdang.playernpc.runtime.PlayerNpcVisibilityRegistry;
import org.tomdang.region.edit.RegionBrushItemService;
import org.tomdang.region.edit.RegionBrushListener;
import org.tomdang.region.visualization.RegionBrushVisualizationTask;
import org.tomdang.region.bukkit.BukkitBlockPositionAdapter;
import org.tomdang.region.bukkit.PlayerRegionTrackingListener;
import org.tomdang.region.bukkit.PlayerRegionTransitionEvent;
import org.tomdang.region.bukkit.RegionTrackingDebugService;
import org.tomdang.region.bukkit.RegionSpeedRefreshListener;
import org.tomdang.region.tracking.PlayerRegionTrackingService;
import org.tomdang.worldmap.MapGiveCommand;
import org.tomdang.worldmap.WorldMapItemCommand;
import org.tomdang.worldmap.WorldMapItemService;
import org.tomdang.foraging.ForagingListener;
import org.tomdang.foraging.ForagingService;
import org.tomdang.foraging.ForagingTreeCommand;
import org.tomdang.foraging.ForagingTreeRegistry;
import org.tomdang.foraging.ForagingTreeStore;
import org.tomdang.foraging.TreeModelConfigurationLoader;
import org.tomdang.foraging.TreeModelRegistry;
import org.tomdang.foraging.ForagingToolConfigurationLoader;
import org.tomdang.foraging.ForagingToolRegistry;
import org.tomdang.foraging.audit.TreeAuditRegistry;
import org.tomdang.foraging.audit.TreeAuditVisualizationService;
import org.tomdang.island.PrivateIslandCommand;
import org.tomdang.island.PrivateIslandWorldListener;
import org.tomdang.island.PrivateIslandWorldService;
import org.tomdang.island.preset.IslandPresetConfigurationLoader;
import org.tomdang.island.preset.IslandPresetRegistry;
import org.tomdang.island.runtime.IslandContextService;
import org.tomdang.island.block.BlockOriginStore;
import org.tomdang.island.block.InMemoryBlockOriginStore;
import org.tomdang.island.block.IslandBlockInteractionListener;
import org.tomdang.island.block.IslandBlockPolicyService;
import org.tomdang.island.block.PostgresBlockOriginStore;
import org.tomdang.island.block.RegisteredResourceRegistry;
import org.tomdang.collection.CollectionConfigurationLoader;
import org.tomdang.collection.CollectionMenuListener;
import org.tomdang.collection.CollectionService;
import org.tomdang.collection.CollectionsCommand;
import org.tomdang.quest.bukkit.QuestCommand;
import org.tomdang.quest.bukkit.QuestGateListener;
import org.tomdang.quest.bukkit.QuestPlayerConnectionListener;
import org.tomdang.platform.PlatformServices;
import org.tomdang.platform.lifecycle.ModuleRuntime;
import org.tomdang.platform.session.PlayerSessionCoordinator;
import org.tomdang.platform.session.bukkit.PlayerSessionListener;
import org.tomdang.platform.threading.MainThreadGuard;
import org.tomdang.gameplay.event.GameplayEventBus;
import org.tomdang.gameplay.event.type.RegionEntered;

import java.io.File;
import java.io.IOException;

public final class TomBlockApplication implements AutoCloseable {



	private PlayerBootStrap playerBootStrap;
	private CombatBootStrap combatBootStrap;
	private MobBootStrap mobBootStrap;
	private NmsPlayerNpcInteractionInterceptor nmsPlayerNpcInteractionInterceptor;
	private ActorBootStrap actorBootStrap;
	private RegionBrushVisualizationTask regionBrushVisualizationTask;
	private PlayerMovementSpeedBootStrap playerMovementSpeedBootStrap;
	private BlockOriginStore blockOriginStore;
	private QuestBootStrap questBootStrap;
	private ModuleRuntime contentModules;
	private PlayerSessionCoordinator playerSessions;
	private HudBootstrap hudBootstrap;
	private GameplayEventBus gameplayEvents;
	private EncounterBootstrap encounterBootstrap;
	private CritterBootstrap critterBootstrap;
	private org.tomdang.quest.presentation.QuestActorMarkerRefreshService questMarkerRefresh;
	private org.tomdang.quest.presentation.QuestTrackerHudService questTracker;
	private GuiBootstrap guiBootstrap;
	private WorldCalendarBootstrap worldCalendarBootstrap;


	private final TomBlock plugin;

	public TomBlockApplication(TomBlock plugin) {
		this.plugin = java.util.Objects.requireNonNull(plugin, "plugin");
	}

	public void start() {
		plugin.getLogger().info("Plugin enabled.");
		playerSessions = new PlayerSessionCoordinator();
		MainThreadGuard mainThread = new MainThreadGuard(Bukkit::isPrimaryThread);
		gameplayEvents = new GameplayEventBus(mainThread);
		guiBootstrap = new GuiBootstrap(plugin);
		plugin.getServer().getPluginManager().registerEvents(new PlayerSessionListener(playerSessions), plugin);
		for (Player player : Bukkit.getOnlinePlayers()) {
			playerSessions.open(player.getUniqueId()).activate();
		}



		// NameSpaced Keys
		NamespacedKey customItemIdKey = new NamespacedKey(plugin, "item_id");
		NamespacedKey customMobKey = new NamespacedKey(plugin, "mob_id");
		NamespacedKey customMobHealthKey = new NamespacedKey(plugin, "mob_current_health");
		NamespacedKey spawnPointIDKey = new NamespacedKey(plugin, "spawnpoint_id");
		NamespacedKey populationRuleKey = new NamespacedKey(plugin, "population_rule_id");
		NamespacedKey actorInstanceIDKey = new NamespacedKey(plugin, "actor_instance_id");
		NamespacedKey actorDefinitionKey = new NamespacedKey(plugin, "actor_definition_id");
		NamespacedKey actorAudienceScopeKey = new NamespacedKey(plugin,"actor_audience_scope");
		NamespacedKey actorAudienceIDKey = new NamespacedKey(plugin,"actor_audience_id");
		NamespacedKey actorSpawnPointIDKey = new NamespacedKey(plugin, "actor_spawn_point_id");
		NamespacedKey regionBrushKey = new NamespacedKey(plugin, "region_brush");

		PlayerStatPresentationBootStrap playerStatPresentationBootStrap = new PlayerStatPresentationBootStrap(plugin);
		ItemBootStrap itemBootStrap = new ItemBootStrap(
				plugin,
				customItemIdKey,
				playerStatPresentationBootStrap.getRegistry()
		);

		CustomItemRegistry customItemRegistry = itemBootStrap.getCustomItemRegistry();
		CustomItemCreator customItemCreator = itemBootStrap.getCustomItemCreator();
		CustomItemResolver customItemResolver = itemBootStrap.getCustomItemResolver();
		CustomArmorRegistry customArmorRegistry = itemBootStrap.getCustomArmorRegistry();
		CustomArmorResolver customArmorResolver = itemBootStrap.getCustomArmorResolver();
		CustomArmorCreator customArmorCreator = itemBootStrap.getCustomArmorCreator();
		WeaponCreator weaponCreator = new WeaponCreator(customItemIdKey, playerStatPresentationBootStrap.getRegistry());
		MiningToolCreator miningToolCreator = new MiningToolCreator(customItemIdKey, playerStatPresentationBootStrap.getRegistry());
		CustomItemStackFactory customItemStackFactory = new CustomItemStackFactory(
				customItemCreator,
				weaponCreator,
				miningToolCreator,
				customArmorCreator
		);
		RegionBootStrap regionBootStrap = new RegionBootStrap(plugin);

		mobBootStrap = new MobBootStrap(
				plugin,
				customMobKey,
				spawnPointIDKey,
				customMobHealthKey,
				populationRuleKey,
				customItemRegistry,
				regionBootStrap.getRegionRegistry(),
				regionBootStrap.getRegionResolver()
		);
		CustomMobResolver customMobResolver = mobBootStrap.getCustomMobResolver();
		CustomMobHealthService customMobHealthService = mobBootStrap.getCustomMobHealthService();

		final File file = new File(plugin.getDataFolder(), "playerprofiles.yml");
		PlayerStatRuleBootStrap playerStatRuleBootStrap = new PlayerStatRuleBootStrap(plugin);
		playerBootStrap = new PlayerBootStrap(
				plugin,
				file,
				customArmorResolver,
				customItemResolver,
				playerStatRuleBootStrap.getRegistry()
		);
		questBootStrap = new QuestBootStrap(plugin, playerBootStrap.getDataSource(), gameplayEvents);
		encounterBootstrap = new EncounterBootstrap(plugin, playerBootStrap.getDataSource(), gameplayEvents, questBootStrap);
		plugin.getCommand("quest").setExecutor(new QuestCommand(questBootStrap.progressService()));

		PlayerProfileService playerProfileService = playerBootStrap.getPlayerProfileService();
		PlayerProfileRepository playerProfileStorage = playerBootStrap.getPlayerProfileStorage();
		PlayerStatsService playerStatsService = playerBootStrap.getPlayerStatsService();
		PlayerResourceService playerResourceService = playerBootStrap.getPlayerResourceService();
		PlayerActionBarService playerActionBarService = playerBootStrap.getPlayerActionBarService();
		hudBootstrap = new HudBootstrap(plugin, playerResourceService, playerActionBarService);
		var hudRuntime = hudBootstrap.runtime();
		var productionHud = hudBootstrap.production();
		var progressionNotifications = hudBootstrap.progressionNotifications();
		var huntingHud = hudBootstrap.hunting();
		var skillPresenter = new org.tomdang.player.skill.SkillProgressPresenter(progressionNotifications);
		critterBootstrap = new CritterBootstrap(plugin, playerBootStrap.getDataSource(), gameplayEvents,
				encounterBootstrap, playerProfileService, playerProfileStorage, progressionNotifications,
				playerActionBarService, huntingHud,
				customItemRegistry, customItemStackFactory, customItemResolver,
				guiBootstrap.registry(), guiBootstrap.service());
		CollectionService collectionService = new CollectionService(
				new CollectionConfigurationLoader().load(plugin.getResource("collections.yml")),
				playerBootStrap.getPlayerCounterService(), playerProfileService, skillPresenter, gameplayEvents);
		plugin.getCommand("collections").setExecutor(new CollectionsCommand(collectionService));
		plugin.getServer().getPluginManager().registerEvents(new CollectionMenuListener(), plugin);
		ForagingTreeRegistry foragingTrees = new ForagingTreeRegistry();
		ForagingTreeStore foragingTreeStore = new ForagingTreeStore(new File(plugin.getDataFolder(), "foraging-trees.yml"));
		TreeModelRegistry treeModels = new TreeModelConfigurationLoader().load(plugin.getResource("foraging/trees.yml"));
		ForagingToolRegistry foragingTools = new ForagingToolConfigurationLoader().load(plugin.getResource("foraging/tools.yml"));
		ForagingService foragingService = new ForagingService(
				plugin, foragingTrees, collectionService, playerProfileService, skillPresenter, foragingTreeStore,
				customItemResolver, playerStatsService, foragingTools, playerActionBarService);
		foragingTreeStore.load(treeModels).forEach(foragingService::registerExisting);
		TreeAuditVisualizationService treeAuditVisualization;
		try {
			treeAuditVisualization = new TreeAuditVisualizationService(plugin,
					TreeAuditRegistry.load(plugin.getResource("foraging/southwest-tree-audit.csv")));
		} catch (IOException exception) {
			throw new IllegalStateException("Could not load the Southwest Island tree audit", exception);
		}
		plugin.getCommand("foragingtree").setExecutor(new ForagingTreeCommand(
				foragingService, foragingTrees, treeModels, treeAuditVisualization));
		IslandPresetRegistry islandPresets = new IslandPresetConfigurationLoader().load(plugin.getResource("island-presets.yml"));
		IslandContextService islandContexts = new IslandContextService(islandPresets);
		worldCalendarBootstrap = new WorldCalendarBootstrap(plugin, islandContexts, productionHud);
		PrivateIslandWorldService privateIslandWorlds = new PrivateIslandWorldService(plugin, islandPresets, islandContexts);
		plugin.getServer().getPluginManager().registerEvents(
				new PrivateIslandWorldListener(plugin, privateIslandWorlds, islandContexts, playerActionBarService), plugin);
		plugin.getCommand("island").setExecutor(new PrivateIslandCommand(
				plugin, playerBootStrap.getPrivateIslandService(), privateIslandWorlds));
		RegisteredResourceRegistry islandResources = new RegisteredResourceRegistry();
		islandResources.register(block -> foragingTrees.atLog(block.getLocation()).isPresent());
		blockOriginStore = playerBootStrap.getDataSource() == null ? new InMemoryBlockOriginStore()
				: new PostgresBlockOriginStore(playerBootStrap.getDataSource(), plugin.getLogger()::severe);
		plugin.getServer().getPluginManager().registerEvents(new ForagingListener(foragingService,
				block -> !blockOriginStore.isPlayerPlaced(org.tomdang.island.block.ManagedBlockPosition.from(block))), plugin);
		playerMovementSpeedBootStrap = new PlayerMovementSpeedBootStrap(plugin, playerStatsService);
		CustomArmorService customArmorService = playerBootStrap.getCustomArmorService();
		ItemRefreshBootStrap itemRefreshBootStrap = new ItemRefreshBootStrap(
				customItemResolver,
				customItemStackFactory,
				playerStatsService
		);
		PlayerInventoryItemRefreshService playerInventoryItemRefreshService =
				itemRefreshBootStrap.getPlayerInventoryItemRefreshService();

		AbilityBootStrap abilityBootStrap = new AbilityBootStrap(
				customItemResolver,
				customArmorService,
				playerResourceService,
				playerStatsService
		);

		CustomAbilityRegistry customAbilityRegistry = abilityBootStrap.getCustomAbilityRegistry();
		ActiveAbilityService activeAbilityService = abilityBootStrap.getActiveAbilityService();
		CustomAbilityService customAbilityService = abilityBootStrap.getCustomAbilityService();
		new GameplayAbilityBootStrap(plugin, customAbilityRegistry);

		actorBootStrap = new ActorBootStrap(
				plugin,
				actorInstanceIDKey,
				actorDefinitionKey,
				actorAudienceScopeKey,
				actorAudienceIDKey,
				actorSpawnPointIDKey,
				gameplayEvents
		);
		WorldMapItemService worldMapItems = new WorldMapItemService(plugin);
		plugin.getCommand("mapimage").setExecutor(new WorldMapItemCommand(worldMapItems));
		plugin.getCommand("map").setExecutor(new MapGiveCommand(worldMapItems, false));
		plugin.getCommand("minimap").setExecutor(new MapGiveCommand(worldMapItems, true));
		ActorResolver actorResolver = actorBootStrap.getActorResolver();
		ActorInteractionService actorInteractionService = actorBootStrap.getActorInteractionService();
		ActorDamageService actorDamageService = actorBootStrap.getActorDamageService();
		ActorReconciliationService actorReconciliationService = actorBootStrap.getActorReconciliationService();
		ActorSpawnPointRegistry actorSpawnPointRegistry = actorBootStrap.getActorSpawnPointRegistry();
		ActorRegistry actorRegistry = actorBootStrap.getActorRegistry();
		ActorInteractionRegistry actorInteractionRegistry = actorBootStrap.getActorInteractionRegistry();
		DialogueBootStrap dialogueBootStrap = new DialogueBootStrap(plugin, gameplayEvents,
				new org.tomdang.hud.dialogue.HudEngineDialogueRenderer(productionHud));
		DialogueSessionService dialogueSessionService = dialogueBootStrap.getDialogueSessionService();
		DialogueAdvanceService dialogueAdvanceService = dialogueBootStrap.getDialogueAdvanceService();
		DialogueController dialogueController = dialogueBootStrap.getDialogueController();
		DialogueThemeRegistry dialogueThemeRegistry = dialogueBootStrap.getDialogueThemeRegistry();
		DialogueHudSkinRegistry dialogueHudSkinRegistry = dialogueBootStrap.getDialogueHudSkinRegistry();
		dialogueBootStrap.getDialogueChoiceActionRegistry().registerAction("START_QUEST",
				new org.tomdang.quest.integration.StartQuestDialogueAction(questBootStrap.progressService()));
		var questOffers = new org.tomdang.quest.presentation.QuestOfferConfigurationLoader()
				.load(plugin.getResource("quest-offers.yml"));
		for (var offer : questOffers.all()) questBootStrap.registry().require(offer.questId());
		var questMarkerLines = new org.tomdang.quest.presentation.QuestActorNameplateLineProvider(
				new org.tomdang.quest.presentation.QuestActorMarkerResolver(
						questOffers, questBootStrap.registry(), questBootStrap.progressService()));


		BlacksmithContent blacksmithContent = new BlacksmithContent(dialogueThemeRegistry,
				dialogueHudSkinRegistry,
				dialogueController,
				dialogueSessionService,
				dialogueAdvanceService,
				actorInteractionRegistry,
				actorBootStrap.getActorLookService(),
				dialogueBootStrap.getDialogueChoiceActionRegistry()
		);
		contentModules = new ModuleRuntime(java.util.List.of(
				blacksmithContent,
				new CritterHunterWillContent(dialogueThemeRegistry, actorInteractionRegistry,
						actorBootStrap.getActorLookService(), dialogueController,
						dialogueSessionService, dialogueAdvanceService, questBootStrap.progressService())
		));
		contentModules.context().provide(PlatformServices.PLAYER_SESSIONS, playerSessions);
		contentModules.context().provide(PlatformServices.HUD, hudRuntime);
		contentModules.context().provide(PlatformServices.MAIN_THREAD, mainThread);
		contentModules.context().provide(PlatformServices.GAMEPLAY_EVENTS, gameplayEvents);
		contentModules.context().provide(PlatformServices.QUEST_ACTIONS, questBootStrap.actions());
		contentModules.context().provide(PlatformServices.QUEST_CONDITIONS, questBootStrap.conditions());
		contentModules.context().provide(PlatformServices.QUEST_REWARDS, questBootStrap.rewards());
		contentModules.context().provide(PlatformServices.QUEST_LIFECYCLE, questBootStrap.lifecycle());
		contentModules.context().provide(PlatformServices.ENCOUNTERS, encounterBootstrap.runtime());
		contentModules.context().provide(PlatformServices.ENCOUNTER_DEFINITIONS, encounterBootstrap.definitions());
		contentModules.context().provide(PlatformServices.ENCOUNTER_BEHAVIORS, encounterBootstrap.behaviors());
		questBootStrap.rewards().register("GIVE_CUSTOM_ITEM", (context, parameters) -> {
			String itemId = java.util.Objects.requireNonNull(parameters.get("item"), "Missing item reward");
			int amount = Integer.parseInt(parameters.getOrDefault("amount", "1"));
			if (amount < 1) throw new IllegalArgumentException("Quest item reward amount must be positive");
			var item = customItemRegistry.getCustomItem(itemId);
			if (item == null) throw new IllegalArgumentException("Unknown quest reward item " + itemId);
			var player = Bukkit.getPlayer(context.playerId());
			if (player == null) throw new IllegalStateException("Quest reward player is offline");
			var overflow = player.getInventory().addItem(customItemStackFactory.createCustomItemStack(item, amount));
			for (var stack : overflow.values()) player.getWorld().dropItemNaturally(player.getLocation(), stack);
		});
		contentModules.start();
		encounterBootstrap.sealFramework();
		questBootStrap.sealOrchestration();

		combatBootStrap = new CombatBootStrap(
				plugin,
				weaponCreator,
				customItemRegistry,
				customItemResolver,
				customAbilityRegistry,
				playerProfileService,
				playerStatsService,
				playerResourceService,
				customMobResolver,
				customMobHealthService
		);
		MiningBootstrap miningBootstrap = new MiningBootstrap(
				plugin,
				customItemIdKey,
				miningToolCreator,
				customItemRegistry,
				customItemStackFactory,
				progressionNotifications,
				playerProfileService,
				playerStatsService,
				activeAbilityService,
				customAbilityRegistry
		);
		islandResources.register(block -> miningBootstrap.getMiningBlockRegistry().blockInRegistry(block.getType()));
		plugin.getServer().getPluginManager().registerEvents(new IslandBlockInteractionListener(
				islandContexts, blockOriginStore, islandResources, new IslandBlockPolicyService(), playerActionBarService), plugin);
		new ArmorConfigurationBootStrap(
				plugin,
				customArmorRegistry,
				customItemRegistry,
				customAbilityRegistry
		);
		CraftingBootStrap craftingBootStrap = new CraftingBootStrap(plugin,
				customItemResolver,
				customItemRegistry,
				customItemStackFactory
		);
		CraftingService craftingService = craftingBootStrap.getCraftingService();

		MobRewardService mobRewardService = new MobRewardService(
				playerProfileService,
				customItemStackFactory,
				mobBootStrap.getCustomMobResolver(),
				new org.tomdang.player.skill.SkillProgressionService(),
				new org.tomdang.player.skill.SkillProgressPresenter(progressionNotifications),
				gameplayEvents
		);



		NmsPlayerNpcFactory nmsPlayerNpcFactory = new NmsPlayerNpcFactory();
		NmsPlayerNpcViewer nmsPlayerNpcViewer = new NmsPlayerNpcViewer();
		PlayerNpcRegistry playerNpcRegistry = new PlayerNpcRegistry();
		PlayerNpcVisibilityRegistry playerNpcVisibilityRegistry = new PlayerNpcVisibilityRegistry();
		PlayerNpcLifecycleService playerNpcLifecycleService = new PlayerNpcLifecycleService(
				playerNpcRegistry,
				playerNpcVisibilityRegistry,
				nmsPlayerNpcViewer,
				nmsPlayerNpcFactory
		);
		ActorNameplatePresentation actorNameplatePresentation = new NmsActorNameplatePresentation(
				new ActorNameplatePresentationRegistry(),
				new NmsActorNameplateLineRegistry(),
				new ActorNameplateLayoutCalculator(),
				new NmsActorNameplateLineFactory(),
				new NmsActorNameplateViewer(),
				new ActorNameplateLayout(2.05, 0.3),
				plugin.getServer(),
				questMarkerLines
		);
		PlayerNpcActorPresentation playerNpcActorPresentation = new PlayerNpcActorPresentation(
				playerNpcLifecycleService,
				actorBootStrap.getActorAudienceResolver(),
				actorBootStrap.getBukkitActorCollisionService(),
				actorNameplatePresentation,
				new PlayerNpcProfileNameFactory()
		);
		actorBootStrap.getActorPresentationTypeRegistry().registerPresentation("PLAYER_NPC", playerNpcActorPresentation);
		new ActorConfigurationBootStrap(
				plugin,
				actorRegistry,
				actorBootStrap.getActorPresentationTypeRegistry(),
				actorInteractionRegistry
		);
		new ActorSpawnPointConfigurationBootStrap(
				plugin,
				actorRegistry,
				actorSpawnPointRegistry
		);

		PlayerNpcActorResolver playerNpcActorResolver = new PlayerNpcActorResolver(playerNpcRegistry, actorBootStrap.getActiveActorPresentationRegistry(), actorBootStrap.getActorInstanceRegistry());
		questMarkerRefresh = new org.tomdang.quest.presentation.QuestActorMarkerRefreshService(
				questBootStrap.lifecycle(), playerNpcRegistry, playerNpcActorResolver,
				playerNpcLifecycleService, actorNameplatePresentation);
		questTracker = new org.tomdang.quest.presentation.QuestTrackerHudService(
				plugin, questBootStrap.registry(), questBootStrap.progressService(), questBootStrap.lifecycle(), hudRuntime);
		PlayerNpcActorInteractionService playerNpcActorInteractionService = new PlayerNpcActorInteractionService(playerNpcRegistry, playerNpcVisibilityRegistry, playerNpcActorResolver, actorInteractionService);
		nmsPlayerNpcInteractionInterceptor = new NmsPlayerNpcInteractionInterceptor(plugin, playerNpcActorInteractionService::interact);
		PlayerNpcActorVisibilityService playerNpcActorVisibilityService = new PlayerNpcActorVisibilityService(playerNpcRegistry, playerNpcActorResolver, actorBootStrap.getActorAudienceResolver(), playerNpcLifecycleService, actorNameplatePresentation);
		BukkitBlockPositionAdapter regionPositions = new BukkitBlockPositionAdapter();
		playerStatsService.setLocationCapProvider((player, statType) -> regionBootStrap.getRegionStatCapResolver()
				.resolve(regionPositions.fromLocation(player.getLocation()), statType));
		RegionTrackingDebugService regionDebug = new RegionTrackingDebugService();
		PlayerRegionTrackingService regionTracking = new PlayerRegionTrackingService(
				regionBootStrap.getRegionResolver(),
				(playerId, transition) -> {
					Player player = Bukkit.getPlayer(playerId);
					if (player != null) {
						for (String regionId : transition.entered()) gameplayEvents.publish(
								new RegionEntered(playerId, regionId));
						regionDebug.onTransition(player, transition);
						Bukkit.getPluginManager().callEvent(new PlayerRegionTransitionEvent(player, transition));
					}
				});
		plugin.getServer().getPluginManager().registerEvents(new PlayerRegionTrackingListener(regionTracking, regionPositions, regionDebug), plugin);
		plugin.getServer().getPluginManager().registerEvents(new MobRegionConfinementListener(
				customMobKey, populationRuleKey, mobBootStrap.getMobRegionConfinementPolicy()), plugin);
		plugin.getServer().getPluginManager().registerEvents(
				new RegionSpeedRefreshListener(playerMovementSpeedBootStrap.getRefreshScheduler()), plugin);
		regionBootStrap.getRegionEditingService().onMembershipChanged(position -> {
			mobBootStrap.reconcileSpawnPointsAt(position);
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (regionPositions.fromLocation(player.getLocation()).equals(position)) {
					regionTracking.refresh(player.getUniqueId(), position);
				}
			}
		});
		RegionBrushItemService regionBrushItemService = new RegionBrushItemService(regionBrushKey);
		RegionBrushListener regionBrushListener = new RegionBrushListener(
				regionBrushItemService,
				regionBootStrap.getRegionEditingService(),
				regionPositions,
				playerActionBarService
		);
		regionBrushVisualizationTask = new RegionBrushVisualizationTask(
				plugin,
				regionBootStrap.getRegionEditingService(),
				regionBrushItemService,
				regionBootStrap.getRegionVisualizationService(),
				new org.tomdang.region.bukkit.BukkitBlockPositionAdapter(),
				regionBootStrap.getRegionVisualizationSettings()
		);
		SkillMenuBootstrap skillMenuBootstrap = new SkillMenuBootstrap(plugin);
		new CommandRegistrar(
				plugin,
				playerProfileService,
				combatBootStrap.getWeaponRegistry(),
				mobBootStrap.getCustomMobRegistry(),
				customArmorService,
				playerStatsService,
				customArmorRegistry,
				playerResourceService,
				miningBootstrap.getMiningToolRegistry(),
				dialogueSessionService,
				dialogueController,
				playerNpcLifecycleService,
				actorBootStrap.getActorInstanceRegistry(),
				actorBootStrap.getLinearActorMovementService(),
				actorBootStrap.getGroundActorMovementService(),
				actorBootStrap.getActorLifecycleService(),
				actorBootStrap.getActorFollowService(),
				playerInventoryItemRefreshService,
				playerStatPresentationBootStrap.getRegistry(),
				playerStatPresentationBootStrap.getOverviewConfiguration(),
				skillMenuBootstrap.configuration(),
				regionBootStrap.getRegionResolver(),
				regionBootStrap.getRegionRegistry(),
				regionBootStrap.getRegionEditingService(),
				regionBrushItemService,
				regionTracking,
				regionDebug,
				playerMovementSpeedBootStrap.getRefreshScheduler(),
				customItemRegistry,
				customItemStackFactory
		);

		new ListenerRegistrar(
				plugin,
				playerProfileService,
				playerProfileStorage,
				playerResourceService,
				customAbilityService,
				mobRewardService,
				miningBootstrap.getMiningService(),
				miningBootstrap.getMiningProgressService(),
				block -> !blockOriginStore.isPlayerPlaced(org.tomdang.island.block.ManagedBlockPosition.from(block)),
				combatBootStrap.getCombatService(),
				mobBootStrap.getCustomMobRespawnService(),
				craftingService,
				combatBootStrap.getPlayerAttackReadinessService(),
				combatBootStrap.getPlayerAttackIndicatorService(),
				combatBootStrap.getConsecutiveChargedHitTracker(),
				actorResolver,
				actorInteractionService,
				actorDamageService,
				dialogueSessionService,
				dialogueAdvanceService,
				dialogueController,
				playerNpcLifecycleService,
				playerNpcActorVisibilityService,
				nmsPlayerNpcInteractionInterceptor,
				actorNameplatePresentation,
				playerInventoryItemRefreshService,
				playerStatsService,
				playerStatPresentationBootStrap.getRegistry(),
				playerStatPresentationBootStrap.getOverviewConfiguration(),
				playerStatPresentationBootStrap.getCategoryMenuConfiguration(),
				playerStatPresentationBootStrap.getBreakdownMenuConfiguration(),
				skillMenuBootstrap.configuration(),
				regionBrushListener,
				playerMovementSpeedBootStrap.getListener()
				);
		plugin.getServer().getPluginManager().registerEvents(
				new QuestPlayerConnectionListener(questBootStrap.progressService(), questBootStrap.registry(), plugin.getLogger(),
						playerId -> { questMarkerRefresh.refresh(playerId); questTracker.refresh(playerId); }), plugin);
		plugin.getServer().getPluginManager().registerEvents(
				new QuestGateListener(questBootStrap.progressService(), questBootStrap.gates(), dialogueController,
						dialogueSessionService, gameplayEvents, playerActionBarService), plugin);
		for (Player player : Bukkit.getOnlinePlayers()) {
			regionTracking.update(player.getUniqueId(), regionPositions.fromLocation(player.getLocation()));
		}

		playerBootStrap.start();
		mobBootStrap.reconcileSpawnPoints();
		mobBootStrap.startPresentations();
		mobBootStrap.startPopulations();

		actorReconciliationService.reconcileSpawnPoints();
		regionBrushVisualizationTask.start();
		plugin.getLogger().info("TomBlock startup complete.");
	}

	@Override
	public void close() {
		if (worldCalendarBootstrap != null) worldCalendarBootstrap.close();
		if (guiBootstrap != null) guiBootstrap.close();
		if (contentModules != null) contentModules.close();
		if (hudBootstrap != null) hudBootstrap.close();
		if (questTracker != null) questTracker.close();
		if (questMarkerRefresh != null) questMarkerRefresh.close();
		if (critterBootstrap != null) critterBootstrap.close();
		if (encounterBootstrap != null) encounterBootstrap.close();
		if (questBootStrap != null) questBootStrap.close();
		if (playerSessions != null) playerSessions.close();
		if (mobBootStrap != null) mobBootStrap.stopPopulations();
		if (mobBootStrap != null) mobBootStrap.stopPresentations();
		if (regionBrushVisualizationTask != null) {
			regionBrushVisualizationTask.stop();
		}
		if (playerMovementSpeedBootStrap != null) {
			playerMovementSpeedBootStrap.shutDown();
		}
		if (combatBootStrap != null) {
			combatBootStrap.getPlayerAttackIndicatorService().stop();
		}
		if (nmsPlayerNpcInteractionInterceptor != null) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				nmsPlayerNpcInteractionInterceptor.remove(player);
			}
		}

		if (actorBootStrap != null) {
			actorBootStrap.shutDown();
		}

		if (blockOriginStore != null) blockOriginStore.close();
		if (playerBootStrap != null) {
			playerBootStrap.shutDown();
		}


	}
}
