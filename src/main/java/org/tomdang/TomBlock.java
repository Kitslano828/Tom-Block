package org.tomdang;

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
import org.tomdang.player.playerdata.PlayerProfileStorage;
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

import java.io.File;

public class TomBlock extends JavaPlugin {



	private PlayerBootStrap playerBootStrap;
	private CombatBootStrap combatBootStrap;
	private NmsPlayerNpcInteractionInterceptor nmsPlayerNpcInteractionInterceptor;
	private ActorBootStrap actorBootStrap;
	private RegionBrushVisualizationTask regionBrushVisualizationTask;
	private PlayerMovementSpeedBootStrap playerMovementSpeedBootStrap;


	@Override
	public void onEnable() {
		System.out.println("Plugin Enabled");



		// NameSpaced Keys
		NamespacedKey customItemIdKey = new NamespacedKey(this, "item_id");
		NamespacedKey customMobKey = new NamespacedKey(this, "mob_id");
		NamespacedKey spawnPointIDKey = new NamespacedKey(this, "spawnpoint_id");
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

		MobBootStrap mobBootStrap = new MobBootStrap(
				this,
				customMobKey,
				spawnPointIDKey,
				customItemRegistry
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
		PlayerProfileStorage playerProfileStorage = playerBootStrap.getPlayerProfileStorage();
		PlayerStatsService playerStatsService = playerBootStrap.getPlayerStatsService();
		PlayerResourceService playerResourceService = playerBootStrap.getPlayerResourceService();
		playerMovementSpeedBootStrap = new PlayerMovementSpeedBootStrap(this, playerStatsService);
		PlayerActionBarService playerActionBarService = playerBootStrap.getPlayerActionBarService();
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
				combatBootStrap.getCombatLevel(),
				playerActionBarService
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
		RegionBootStrap regionBootStrap = new RegionBootStrap(this);
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
		getServer().getPluginManager().registerEvents(
				new RegionSpeedRefreshListener(playerMovementSpeedBootStrap.getRefreshScheduler()), this);
		regionBootStrap.getRegionEditingService().onMembershipChanged(position -> {
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
				actorBootStrap.getActorLifecycleService(),
				actorBootStrap.getActorFollowService(),
				playerInventoryItemRefreshService,
				playerStatPresentationBootStrap.getRegistry(),
				playerStatPresentationBootStrap.getOverviewConfiguration(),
				regionBootStrap.getRegionResolver(),
				regionBootStrap.getRegionRegistry(),
				regionBootStrap.getRegionEditingService(),
				regionBrushItemService,
				regionTracking,
				regionDebug,
				playerMovementSpeedBootStrap.getRefreshScheduler()
		);

		new ListenerRegistrar(
				this,
				playerProfileService,
				playerProfileStorage,
				playerResourceService,
				customAbilityService,
				mobRewardService,
				miningBootstrap.getMiningService(),
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
				regionBrushListener,
				playerMovementSpeedBootStrap.getListener()
				);
		for (Player player : Bukkit.getOnlinePlayers()) {
			regionTracking.update(player.getUniqueId(), regionPositions.fromLocation(player.getLocation()));
		}

		playerBootStrap.start();
		mobBootStrap.reconcileSpawnPoints();

		actorReconciliationService.reconcileSpawnPoints();
		regionBrushVisualizationTask.start();
	}

	@Override
	public void onDisable() {
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

		if (playerBootStrap != null) {
			playerBootStrap.shutDown();
		}


	}
}
