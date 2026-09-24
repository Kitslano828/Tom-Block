package org.tomdang.bootstrap;

import org.tomdang.TomBlock;
import org.tomdang.actorframework.combat.ActorDamageService;
import org.tomdang.actorframework.interaction.ActorInteractionService;
import org.tomdang.actorframework.listener.ActorDamageListener;
import org.tomdang.actorframework.listener.ActorInteractListener;
import org.tomdang.actorframework.nameplate.presentation.ActorNameplatePresentation;
import org.tomdang.actorframework.resolver.ActorResolver;
import org.tomdang.combat.CombatService;
import org.tomdang.combat.listener.MobDeathListener;
import org.tomdang.combat.listener.MobHitListener;
import org.tomdang.combat.listener.PlayerRespawnListener;
import org.tomdang.combat.listener.PlayerAttackReadinessListener;
import org.tomdang.combat.listener.PlayerCombatComboListener;
import org.tomdang.combat.attackspeed.PlayerAttackReadinessService;
import org.tomdang.combat.attackspeed.PlayerAttackIndicatorService;
import org.tomdang.combat.combo.ConsecutiveChargedHitTracker;
import org.tomdang.crafting.CraftingService;
import org.tomdang.crafting.gui.listener.ForgeCloseListener;
import org.tomdang.crafting.gui.listener.ForgeDragListener;
import org.tomdang.crafting.gui.listener.ForgeListener;
import org.tomdang.customabilityframework.CustomAbilityService;
import org.tomdang.customabilityframework.listener.PlayerInteractListener;
import org.tomdang.customabilityframework.trigger.AbilityTriggerResolver;
import org.tomdang.customarmorframework.listener.PlayerEquipArmorListener;
import org.tomdang.custommobframework.custommobdrops.MobRewardService;
import org.tomdang.custommobframework.custommobspawn.CustomMobRespawnService;
import org.tomdang.customitemframework.refresh.PlayerInventoryItemRefreshService;
import org.tomdang.customitemframework.refresh.PlayerItemRefreshScheduler;
import org.tomdang.customitemframework.refresh.listener.PlayerHeldItemRefreshListener;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.advance.DialogueAdvanceService;
import org.tomdang.dialogueframework.listener.DialogueSneakListener;
import org.tomdang.dialogueframework.session.DialogueSessionService;
import org.tomdang.mining.MiningService;
import org.tomdang.mining.listener.MiningListener;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.command.PlayerMenuListener;
import org.tomdang.player.listener.PlayerConnectionListener;
import org.tomdang.player.listener.PlayerRegainHealthListener;
import org.tomdang.player.listener.PlayerVanillaDamageListener;
import org.tomdang.player.playerdata.PlayerProfileRepository;
import org.tomdang.player.playerresource.PlayerResourceService;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;
import org.tomdang.player.stats.presentation.PlayerStatsCategoryMenuConfiguration;
import org.tomdang.player.stats.presentation.PlayerStatsOverviewConfiguration;
import org.tomdang.player.stats.presentation.PlayerStatsBreakdownMenuConfiguration;
import org.tomdang.player.skill.menu.SkillMenuConfiguration;
import org.tomdang.player.skill.menu.SkillsMenuListener;
import org.tomdang.playernpc.integration.actor.PlayerNpcActorVisibilityService;
import org.tomdang.playernpc.lifecycle.PlayerNpcLifecycleService;
import org.tomdang.playernpc.listener.PlayerNpcConnectionListener;
import org.tomdang.playernpc.nms.NmsPlayerNpcInteractionInterceptor;
import org.tomdang.region.edit.RegionBrushListener;
import org.tomdang.player.movement.PlayerMovementSpeedListener;
import org.bukkit.block.Block;
import java.util.function.Predicate;

public class ListenerRegistrar {

	public ListenerRegistrar(TomBlock instance, PlayerProfileService playerProfileService,
	                         PlayerProfileRepository playerProfileStorage, PlayerResourceService playerResourceService,
	                         CustomAbilityService customAbilityService, MobRewardService mobRewardService, MiningService miningService, org.tomdang.mining.MiningProgressService miningProgressService,
	                         Predicate<Block> miningRewardEligible,
	                         CombatService combatService, CustomMobRespawnService customMobRespawnService, CraftingService craftingService,
	                         PlayerAttackReadinessService playerAttackReadinessService,
	                         PlayerAttackIndicatorService playerAttackIndicatorService,
	                         ConsecutiveChargedHitTracker consecutiveChargedHitTracker,
							 ActorResolver actorResolver, ActorInteractionService actorInteractionService, ActorDamageService actorDamageService,
							 DialogueSessionService dialogueSessionService, DialogueAdvanceService dialogueAdvanceService, DialogueController dialogueController,
							 PlayerNpcLifecycleService playerNpcLifecycleService, PlayerNpcActorVisibilityService playerNpcActorVisibilityService, NmsPlayerNpcInteractionInterceptor nmsPlayerNpcInteractionInterceptor,
							 ActorNameplatePresentation actorNameplatePresentation,
							 PlayerInventoryItemRefreshService playerInventoryItemRefreshService,
							 PlayerStatsService playerStatsService,
							 PlayerStatPresentationRegistry playerStatPresentationRegistry,
							 PlayerStatsOverviewConfiguration playerStatsOverviewConfiguration,
							 PlayerStatsCategoryMenuConfiguration playerStatsCategoryMenuConfiguration,
							 PlayerStatsBreakdownMenuConfiguration playerStatsBreakdownMenuConfiguration,
							 SkillMenuConfiguration skillMenuConfiguration,
							 RegionBrushListener regionBrushListener,
							 PlayerMovementSpeedListener movementSpeedListener
	){
		PlayerItemRefreshScheduler itemRefreshScheduler = new PlayerItemRefreshScheduler(
				instance,
				playerInventoryItemRefreshService
		);
		PlayerConnectionListener playerConnectionListener = new PlayerConnectionListener(
				playerProfileService,
				playerProfileStorage,
				playerResourceService,
				dialogueSessionService,
				dialogueController
		);
		MiningListener miningListener = new MiningListener(miningService, miningProgressService, miningRewardEligible);
		MobHitListener mobHitListener = new MobHitListener(combatService);
		PlayerRespawnListener playerRespawnListener = new PlayerRespawnListener(combatService);
		PlayerAttackReadinessListener playerAttackReadinessListener =
				new PlayerAttackReadinessListener(playerAttackReadinessService, playerAttackIndicatorService);
		PlayerCombatComboListener playerCombatComboListener =
				new PlayerCombatComboListener(consecutiveChargedHitTracker);
		MobDeathListener mobDeathListener = new MobDeathListener(mobRewardService, customMobRespawnService);
		PlayerMenuListener playerMenuListener = new PlayerMenuListener(playerStatsService, playerStatPresentationRegistry,
				playerStatsOverviewConfiguration, playerStatsCategoryMenuConfiguration, playerStatsBreakdownMenuConfiguration);
		PlayerEquipArmorListener playerEquipArmorListener = new PlayerEquipArmorListener(
				playerResourceService,
				itemRefreshScheduler
		);
		PlayerHeldItemRefreshListener playerHeldItemRefreshListener =
				new PlayerHeldItemRefreshListener(itemRefreshScheduler);
		PlayerInteractListener playerInteractListener = new PlayerInteractListener(
				customAbilityService,
				new AbilityTriggerResolver()
		);
		PlayerRegainHealthListener playerRegainHealthListener = new PlayerRegainHealthListener();
		ForgeCloseListener forgeCloseListener = new ForgeCloseListener();
		ForgeDragListener forgeDragListener = new ForgeDragListener(instance, craftingService);
		ForgeListener forgeListener = new ForgeListener(instance, craftingService);
		ActorInteractListener actorInteractListener = new ActorInteractListener(actorResolver, actorInteractionService);
		ActorDamageListener actorDamageListener = new ActorDamageListener(actorDamageService);
		DialogueSneakListener dialogueSneakListener = new DialogueSneakListener(dialogueSessionService, dialogueAdvanceService);
		PlayerNpcConnectionListener playerNpcConnectionListener = new PlayerNpcConnectionListener(playerNpcLifecycleService, playerNpcActorVisibilityService, nmsPlayerNpcInteractionInterceptor, actorNameplatePresentation);


		instance.getServer().getPluginManager().registerEvents(playerInteractListener, instance);
		instance.getServer().getPluginManager().registerEvents(playerConnectionListener, instance);
		instance.getServer().getPluginManager().registerEvents(miningListener,instance);
		instance.getServer().getPluginManager().registerEvents(mobHitListener, instance);
		instance.getServer().getPluginManager().registerEvents(playerRespawnListener, instance);
		instance.getServer().getPluginManager().registerEvents(playerAttackReadinessListener, instance);
		instance.getServer().getPluginManager().registerEvents(playerCombatComboListener, instance);
		instance.getServer().getPluginManager().registerEvents(mobDeathListener, instance);
		instance.getServer().getPluginManager().registerEvents(playerMenuListener, instance);
		instance.getServer().getPluginManager().registerEvents(new SkillsMenuListener(playerProfileService, skillMenuConfiguration, playerStatPresentationRegistry), instance);
		instance.getServer().getPluginManager().registerEvents(playerEquipArmorListener, instance);
		instance.getServer().getPluginManager().registerEvents(playerHeldItemRefreshListener, instance);
		instance.getServer().getPluginManager().registerEvents(playerRegainHealthListener, instance);
		instance.getServer().getPluginManager().registerEvents(new PlayerVanillaDamageListener(), instance);
		instance.getServer().getPluginManager().registerEvents(forgeCloseListener, instance);
		instance.getServer().getPluginManager().registerEvents(forgeDragListener, instance);
		instance.getServer().getPluginManager().registerEvents(forgeListener, instance);
		instance.getServer().getPluginManager().registerEvents(actorInteractListener, instance);
		instance.getServer().getPluginManager().registerEvents(actorDamageListener, instance);
		instance.getServer().getPluginManager().registerEvents(dialogueSneakListener, instance);
		instance.getServer().getPluginManager().registerEvents(playerNpcConnectionListener, instance);
		instance.getServer().getPluginManager().registerEvents(regionBrushListener, instance);
		instance.getServer().getPluginManager().registerEvents(movementSpeedListener, instance);
	}

}
