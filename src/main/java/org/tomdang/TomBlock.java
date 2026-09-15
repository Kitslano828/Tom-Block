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

import java.io.File;

public class TomBlock extends JavaPlugin {



	private PlayerBootStrap playerBootStrap;
	private NmsPlayerNpcInteractionInterceptor nmsPlayerNpcInteractionInterceptor;
	private ActorBootStrap actorBootStrap;


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

		CombatBootStrap combatBootStrap = new CombatBootStrap(
				this,
				weaponCreator,
				customItemRegistry,
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
				playerStatPresentationBootStrap.getOverviewConfiguration()
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
				combatBootStrap.getPlayerAttackCooldownService(),
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
				playerStatPresentationBootStrap.getBreakdownMenuConfiguration()
				);

		playerBootStrap.start();
		mobBootStrap.reconcileSpawnPoints();

		actorReconciliationService.reconcileSpawnPoints();
	}

	@Override
	public void onDisable() {
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
