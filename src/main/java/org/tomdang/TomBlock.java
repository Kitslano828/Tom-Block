package org.tomdang;

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
import org.tomdang.bootstrap.*;
import org.tomdang.combat.weapons.WeaponCreator;
import org.tomdang.content.blacksmith.BlacksmithContent;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.advance.DialogueAdvanceService;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkinRegistry;
import org.tomdang.dialogueframework.session.DialogueSessionService;
import org.tomdang.dialogueframework.theme.DialogueThemeRegistry;
import org.tomdang.mining.miningtool.MiningToolCreator;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playeractionbar.ActionBarSuppressionService;
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
import org.tomdang.worldmap.MapTestCommand;
import org.tomdang.worldmap.MapHudCommand;
import org.tomdang.worldmap.MapGiveCommand;
import org.tomdang.worldmap.MapTestService;
import org.tomdang.worldmap.VillageMapGrid;
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
import org.tomdang.foraging.encounter.ForagingEncounterConfigurationLoader;
import org.tomdang.foraging.encounter.ForagingEncounterListener;
import org.tomdang.foraging.encounter.ForagingEncounterService;
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

import java.io.File;
import java.io.IOException;

public class TomBlock extends JavaPlugin {



	private PlayerBootStrap playerBootStrap;
	private CombatBootStrap combatBootStrap;
	private MobBootStrap mobBootStrap;
	private NmsPlayerNpcInteractionInterceptor nmsPlayerNpcInteractionInterceptor;
	private ActorBootStrap actorBootStrap;
	private RegionBrushVisualizationTask regionBrushVisualizationTask;
	private PlayerMovementSpeedBootStrap playerMovementSpeedBootStrap;
	private MapTestService mapTestService;
	private BlockOriginStore blockOriginStore;


	@Override
	public void onEnable() {
		getLogger().info("Plugin enabled.");



		// NameSpaced Keys
		NamespacedKey customItemIdKey = new NamespacedKey(this, "item_id");
		NamespacedKey customMobKey = new NamespacedKey(this, "mob_id");
		NamespacedKey customMobHealthKey = new NamespacedKey(this, "mob_current_health");
		NamespacedKey spawnPointIDKey = new NamespacedKey(this, "spawnpoint_id");
		NamespacedKey populationRuleKey = new NamespacedKey(this, "population_rule_id");
		NamespacedKey actorInstanceIDKey = new NamespacedKey(this, "actor_instance_id");
		NamespacedKey actorDefinitionKey = new NamespacedKey(this, "actor_definition_id");
		NamespacedKey actorAudienceScopeKey = new NamespacedKey(this,"actor_audience_scope");
		NamespacedKey actorAudienceIDKey = new NamespacedKey(this,"actor_audience_id");
		NamespacedKey actorSpawnPointIDKey = new NamespacedKey(this, "actor_spawn_point_id");
		NamespacedKey regionBrushKey = new NamespacedKey(this, "region_brush");

		PlayerStatPresentationBootStrap playerStatPresentationBootStrap = new PlayerStatPresentationBootStrap(this);
		ItemBootStrap itemBootStrap = new ItemBootStrap(
				this,
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
		RegionBootStrap regionBootStrap = new RegionBootStrap(this);

		mobBootStrap = new MobBootStrap(
				this,
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

		final File file = new File(getDataFolder(), "playerprofiles.yml");
		PlayerStatRuleBootStrap playerStatRuleBootStrap = new PlayerStatRuleBootStrap(this);
		playerBootStrap = new PlayerBootStrap(
				this,
				file,
				customArmorResolver,
				customItemResolver,
				playerStatRuleBootStrap.getRegistry()
		);

		PlayerProfileService playerProfileService = playerBootStrap.getPlayerProfileService();
		PlayerProfileRepository playerProfileStorage = playerBootStrap.getPlayerProfileStorage();
		PlayerStatsService playerStatsService = playerBootStrap.getPlayerStatsService();
		PlayerResourceService playerResourceService = playerBootStrap.getPlayerResourceService();
		PlayerActionBarService playerActionBarService = playerBootStrap.getPlayerActionBarService();
		var skillPresenter = new org.tomdang.player.skill.SkillProgressPresenter(playerActionBarService);
		CollectionService collectionService = new CollectionService(
				new CollectionConfigurationLoader().load(getResource("collections.yml")),
				playerBootStrap.getPlayerCounterService(), playerProfileService, skillPresenter);
		getCommand("collections").setExecutor(new CollectionsCommand(collectionService));
		getServer().getPluginManager().registerEvents(new CollectionMenuListener(), this);
		ForagingTreeRegistry foragingTrees = new ForagingTreeRegistry();
		ForagingTreeStore foragingTreeStore = new ForagingTreeStore(new File(getDataFolder(), "foraging-trees.yml"));
		TreeModelRegistry treeModels = new TreeModelConfigurationLoader().load(getResource("foraging/trees.yml"));
		ForagingToolRegistry foragingTools = new ForagingToolConfigurationLoader().load(getResource("foraging/tools.yml"));
		ForagingService foragingService = new ForagingService(
				this, foragingTrees, collectionService, playerProfileService, skillPresenter, foragingTreeStore,
				customItemResolver, playerStatsService, foragingTools);
		foragingTreeStore.load(treeModels).forEach(foragingService::registerExisting);
		TreeAuditVisualizationService treeAuditVisualization;
		try {
			treeAuditVisualization = new TreeAuditVisualizationService(this,
					TreeAuditRegistry.load(getResource("foraging/southwest-tree-audit.csv")));
		} catch (IOException exception) {
			throw new IllegalStateException("Could not load the Southwest Island tree audit", exception);
		}
		var foragingEncounters = new ForagingEncounterConfigurationLoader().load(getResource("foraging/encounters.yml"));
		ForagingEncounterService foragingEncounterService = new ForagingEncounterService(this, foragingEncounters,
				collectionService, playerProfileService, skillPresenter, customItemResolver, playerStatsService, foragingTools);
		getServer().getPluginManager().registerEvents(new ForagingEncounterListener(foragingEncounterService), this);
		getCommand("foragingtree").setExecutor(new ForagingTreeCommand(
				foragingService, foragingTrees, treeModels, treeAuditVisualization, foragingEncounterService));
		IslandPresetRegistry islandPresets = new IslandPresetConfigurationLoader().load(getResource("island-presets.yml"));
		IslandContextService islandContexts = new IslandContextService(islandPresets);
		PrivateIslandWorldService privateIslandWorlds = new PrivateIslandWorldService(this, islandPresets, islandContexts);
		getServer().getPluginManager().registerEvents(
				new PrivateIslandWorldListener(this, privateIslandWorlds, islandContexts), this);
		getCommand("island").setExecutor(new PrivateIslandCommand(
				this, playerBootStrap.getPrivateIslandService(), privateIslandWorlds));
		RegisteredResourceRegistry islandResources = new RegisteredResourceRegistry();
		islandResources.register(block -> foragingTrees.atLog(block.getLocation()).isPresent());
		islandResources.register(block -> foragingEncounters.at(block).isPresent());
		blockOriginStore = playerBootStrap.getDataSource() == null ? new InMemoryBlockOriginStore()
				: new PostgresBlockOriginStore(playerBootStrap.getDataSource(), getLogger()::severe);
		getServer().getPluginManager().registerEvents(new ForagingListener(foragingService,
				block -> !blockOriginStore.isPlayerPlaced(org.tomdang.island.block.ManagedBlockPosition.from(block))), this);
		playerMovementSpeedBootStrap = new PlayerMovementSpeedBootStrap(this, playerStatsService);
		CustomArmorService customArmorService = playerBootStrap.getCustomArmorService();
		ActionBarSuppressionService actionBarSuppressionService = playerBootStrap.getActionBarSuppressionService();
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
		new GameplayAbilityBootStrap(this, customAbilityRegistry);

		actorBootStrap = new ActorBootStrap(
				this,
				actorInstanceIDKey,
				actorDefinitionKey,
				actorAudienceScopeKey,
				actorAudienceIDKey,
				actorSpawnPointIDKey
		);
		mapTestService = new MapTestService(this,
				new VillageMapGrid(regionBootStrap.getRegionRegistry().require("STARTER_VILLAGE")),
				actorBootStrap.getBukkitActorCollisionService());
		getCommand("maptest").setExecutor(new MapTestCommand(mapTestService));
		getCommand("maphud").setExecutor(new MapHudCommand(mapTestService));
		WorldMapItemService worldMapItems = new WorldMapItemService(this);
		getCommand("mapimage").setExecutor(new WorldMapItemCommand(worldMapItems));
		getCommand("map").setExecutor(new MapGiveCommand(worldMapItems, false));
		getCommand("minimap").setExecutor(new MapGiveCommand(worldMapItems, true));
		ActorResolver actorResolver = actorBootStrap.getActorResolver();
		ActorInteractionService actorInteractionService = actorBootStrap.getActorInteractionService();
		ActorDamageService actorDamageService = actorBootStrap.getActorDamageService();
		ActorReconciliationService actorReconciliationService = actorBootStrap.getActorReconciliationService();
		ActorSpawnPointRegistry actorSpawnPointRegistry = actorBootStrap.getActorSpawnPointRegistry();
		ActorRegistry actorRegistry = actorBootStrap.getActorRegistry();
		ActorInteractionRegistry actorInteractionRegistry = actorBootStrap.getActorInteractionRegistry();

		DialogueBootStrap dialogueBootStrap = new DialogueBootStrap(this, actionBarSuppressionService);
		DialogueSessionService dialogueSessionService = dialogueBootStrap.getDialogueSessionService();
		DialogueAdvanceService dialogueAdvanceService = dialogueBootStrap.getDialogueAdvanceService();
		DialogueController dialogueController = dialogueBootStrap.getDialogueController();
		DialogueThemeRegistry dialogueThemeRegistry = dialogueBootStrap.getDialogueThemeRegistry();
		DialogueHudSkinRegistry dialogueHudSkinRegistry = dialogueBootStrap.getDialogueHudSkinRegistry();


		BlacksmithContent blacksmithContent = new BlacksmithContent(dialogueThemeRegistry,
				dialogueHudSkinRegistry,
				dialogueController,
				dialogueSessionService,
				dialogueAdvanceService,
				actorInteractionRegistry,
				actorBootStrap.getActorLookService(),
				dialogueBootStrap.getDialogueChoiceActionRegistry()
		);
		blacksmithContent.register();

		combatBootStrap = new CombatBootStrap(
				this,
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
				this,
				customItemIdKey,
				miningToolCreator,
				customItemRegistry,
				customItemStackFactory,
				playerActionBarService,
				playerProfileService,
				playerStatsService,
				activeAbilityService,
				customAbilityRegistry
		);
		islandResources.register(block -> miningBootstrap.getMiningBlockRegistry().blockInRegistry(block.getType()));
		getServer().getPluginManager().registerEvents(new IslandBlockInteractionListener(
				islandContexts, blockOriginStore, islandResources, new IslandBlockPolicyService()), this);
		new ArmorConfigurationBootStrap(
				this,
				customArmorRegistry,
				customItemRegistry,
				customAbilityRegistry
		);
		CraftingBootStrap craftingBootStrap = new CraftingBootStrap(this,
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
				new org.tomdang.player.skill.SkillProgressPresenter(playerActionBarService)
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
				getServer()
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
				this,
				actorRegistry,
				actorBootStrap.getActorPresentationTypeRegistry(),
				actorInteractionRegistry
		);
		new ActorSpawnPointConfigurationBootStrap(
				this,
				actorRegistry,
				actorSpawnPointRegistry
		);

		PlayerNpcActorResolver playerNpcActorResolver = new PlayerNpcActorResolver(playerNpcRegistry, actorBootStrap.getActiveActorPresentationRegistry(), actorBootStrap.getActorInstanceRegistry());
		PlayerNpcActorInteractionService playerNpcActorInteractionService = new PlayerNpcActorInteractionService(playerNpcRegistry, playerNpcVisibilityRegistry, playerNpcActorResolver, actorInteractionService);
		nmsPlayerNpcInteractionInterceptor = new NmsPlayerNpcInteractionInterceptor(this, playerNpcActorInteractionService::interact);
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
						regionDebug.onTransition(player, transition);
						Bukkit.getPluginManager().callEvent(new PlayerRegionTransitionEvent(player, transition));
					}
				});
		getServer().getPluginManager().registerEvents(new PlayerRegionTrackingListener(regionTracking, regionPositions, regionDebug), this);
		getServer().getPluginManager().registerEvents(new MobRegionConfinementListener(
				customMobKey, populationRuleKey, mobBootStrap.getMobRegionConfinementPolicy()), this);
		getServer().getPluginManager().registerEvents(
				new RegionSpeedRefreshListener(playerMovementSpeedBootStrap.getRefreshScheduler()), this);
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
				regionPositions
		);
		regionBrushVisualizationTask = new RegionBrushVisualizationTask(
				this,
				regionBootStrap.getRegionEditingService(),
				regionBrushItemService,
				regionBootStrap.getRegionVisualizationService(),
				new org.tomdang.region.bukkit.BukkitBlockPositionAdapter(),
				regionBootStrap.getRegionVisualizationSettings()
		);
		SkillMenuBootstrap skillMenuBootstrap = new SkillMenuBootstrap(this);
		new CommandRegistrar(
				this,
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
				this,
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
		for (Player player : Bukkit.getOnlinePlayers()) {
			regionTracking.update(player.getUniqueId(), regionPositions.fromLocation(player.getLocation()));
		}

		playerBootStrap.start();
		mobBootStrap.reconcileSpawnPoints();
		mobBootStrap.startPresentations();
		mobBootStrap.startPopulations();

		actorReconciliationService.reconcileSpawnPoints();
		regionBrushVisualizationTask.start();
		getLogger().info("TomBlock startup complete.");
	}

	@Override
	public void onDisable() {
		if (mapTestService != null) mapTestService.stop();
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
