package org.tomdang.bootstrap;

import org.tomdang.TomBlock;
import org.tomdang.actorframework.command.FollowTestCommand;
import org.tomdang.actorframework.command.MoveActorTestCommand;
import org.tomdang.actorframework.instance.ActorInstanceRegistry;
import org.tomdang.actorframework.lifecycle.ActorLifecycleService;
import org.tomdang.actorframework.movement.ActorFollowService;
import org.tomdang.actorframework.movement.LinearActorMovementService;
import org.tomdang.combat.command.GetWeapon;
import org.tomdang.combat.weapons.WeaponRegistry;
import org.tomdang.crafting.command.ForgeCommand;
import org.tomdang.customarmorframework.CustomArmorRegistry;
import org.tomdang.customarmorframework.CustomArmorService;
import org.tomdang.customarmorframework.command.GiveCustomArmor;
import org.tomdang.customitemframework.command.RefreshItemsCommand;
import org.tomdang.customitemframework.command.GiveCustomItemCommand;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.CustomItemStackFactory;
import org.tomdang.customitemframework.refresh.PlayerInventoryItemRefreshService;
import org.tomdang.custommobframework.CustomMobRegistry;
import org.tomdang.custommobframework.command.SpawnCustomMob;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.command.DialogueChoiceCommand;
import org.tomdang.dialogueframework.session.DialogueSessionService;
import org.tomdang.mining.command.GetMiningTool;
import org.tomdang.mining.command.GiveMiningLevel;
import org.tomdang.mining.command.SetMiningLevel;
import org.tomdang.mining.command.SetMiningXP;
import org.tomdang.mining.miningtool.MiningToolRegistry;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.command.HealCommand;
import org.tomdang.player.command.PlayerStatsCommand;
import org.tomdang.player.command.SetStatCommand;
import org.tomdang.player.command.energy.RecoverEnergy;
import org.tomdang.player.command.energy.UseEnergy;
import org.tomdang.player.playerresource.PlayerResourceService;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;
import org.tomdang.player.stats.presentation.PlayerStatsOverviewConfiguration;
import org.tomdang.playernpc.command.NmsPlayerNpcTestCommand;
import org.tomdang.playernpc.lifecycle.PlayerNpcLifecycleService;
import org.tomdang.region.bukkit.BukkitBlockPositionAdapter;
import org.tomdang.region.command.RegionCommand;
import org.tomdang.region.resolution.RegionResolver;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.edit.RegionEditingService;
import org.tomdang.region.edit.RegionBrushItemService;
import org.tomdang.region.bukkit.RegionTrackingDebugService;
import org.tomdang.region.tracking.PlayerRegionTrackingService;
import org.tomdang.player.movement.PlayerMovementSpeedRefreshScheduler;

public class CommandRegistrar {
	
	public CommandRegistrar(TomBlock instance, PlayerProfileService playerProfileService, WeaponRegistry weaponRegistry, CustomMobRegistry customMobRegistry,
	                        CustomArmorService customArmorService, PlayerStatsService playerStatsService, CustomArmorRegistry customArmorRegistry,
	                        PlayerResourceService playerResourceService, MiningToolRegistry miningToolRegistry, DialogueSessionService dialogueSessionService,
	                        DialogueController dialogueController, PlayerNpcLifecycleService playerNpcLifecycleService,
	                        ActorInstanceRegistry actorInstanceRegistry, LinearActorMovementService linearActorMovementService, ActorLifecycleService actorLifecycleService,
	                        ActorFollowService actorFollowService,
	                        PlayerInventoryItemRefreshService playerInventoryItemRefreshService,
	                        PlayerStatPresentationRegistry playerStatPresentationRegistry,
	                        PlayerStatsOverviewConfiguration playerStatsOverviewConfiguration,
	                        RegionResolver regionResolver,
	                        RegionRegistry regionRegistry,
	                        RegionEditingService regionEditingService,
	                        RegionBrushItemService regionBrushItemService,
	                        PlayerRegionTrackingService regionTracking,
	                        RegionTrackingDebugService regionDebug,
	                        PlayerMovementSpeedRefreshScheduler movementSpeedRefreshScheduler,
	                        CustomItemRegistry customItemRegistry,
	                        CustomItemStackFactory customItemStackFactory
	) {
		// COMMANDS
		SetMiningLevel setMiningLevel = new SetMiningLevel(playerProfileService);
		instance.getCommand("setmininglvl").setExecutor(setMiningLevel);
		instance.getCommand("setmininglvl").setTabCompleter(setMiningLevel);

		GiveMiningLevel giveMiningLevel = new GiveMiningLevel(playerProfileService);
		instance.getCommand("getmininglvl").setExecutor(giveMiningLevel);
		instance.getCommand("getmininglvl").setTabCompleter(giveMiningLevel);

		SetMiningXP setMiningXP = new SetMiningXP(playerProfileService);
		instance.getCommand("setminingxp").setExecutor(setMiningXP);
		instance.getCommand("setminingxp").setTabCompleter(setMiningXP);

		GetMiningTool getMiningTool = new GetMiningTool(miningToolRegistry);
		instance.getCommand("giveminingtool").setExecutor(getMiningTool);
		instance.getCommand("giveminingtool").setTabCompleter(getMiningTool);

		GetWeapon getWeapon = new GetWeapon(weaponRegistry);
		instance.getCommand("giveweapon").setExecutor(getWeapon);
		instance.getCommand("giveweapon").setTabCompleter(getWeapon);

		GiveCustomItemCommand giveCustomItem = new GiveCustomItemCommand(customItemRegistry, customItemStackFactory);
		instance.getCommand("givecustomitem").setExecutor(giveCustomItem);
		instance.getCommand("givecustomitem").setTabCompleter(giveCustomItem);

		SpawnCustomMob spawnCustomMob = new SpawnCustomMob(customMobRegistry);
		instance.getCommand("spawncustommob").setExecutor(spawnCustomMob);
		instance.getCommand("spawncustommob").setTabCompleter(spawnCustomMob);

		PlayerStatsCommand playerStatsCommand = new PlayerStatsCommand(playerStatsService, playerStatPresentationRegistry, playerStatsOverviewConfiguration);
		instance.getCommand("stats").setExecutor(playerStatsCommand);

		SetStatCommand setStatCommand = new SetStatCommand(
				playerProfileService, playerInventoryItemRefreshService, movementSpeedRefreshScheduler);
		instance.getCommand("setstat").setExecutor(setStatCommand);
		instance.getCommand("setstat").setTabCompleter(setStatCommand);

		RefreshItemsCommand refreshItemsCommand = new RefreshItemsCommand(playerInventoryItemRefreshService);
		instance.getCommand("refreshitems").setExecutor(refreshItemsCommand);

		GiveCustomArmor giveCustomArmor = new GiveCustomArmor(customArmorRegistry);
		instance.getCommand("givecustomarmor").setExecutor(giveCustomArmor);

		HealCommand healCommand = new HealCommand(playerResourceService);
		instance.getCommand("heal").setExecutor(healCommand);

		RecoverEnergy recoverEnergy = new RecoverEnergy(playerResourceService);
		instance.getCommand("recoverenergy").setExecutor(recoverEnergy);

		UseEnergy useEnergy = new UseEnergy(playerResourceService);
		instance.getCommand("useenergy").setExecutor(useEnergy);

		ForgeCommand forgeCommand = new ForgeCommand();
		instance.getCommand("forge").setExecutor(forgeCommand);

		DialogueChoiceCommand dialogueChoiceCommand = new DialogueChoiceCommand(dialogueSessionService, dialogueController);
		instance.getCommand("dialoguechoice").setExecutor(dialogueChoiceCommand);

		NmsPlayerNpcTestCommand nmsPlayerNpcTestCommand = new NmsPlayerNpcTestCommand(playerNpcLifecycleService);
		instance.getCommand("spawnnmsnpc").setExecutor(nmsPlayerNpcTestCommand);

		MoveActorTestCommand moveActorTestCommand = new MoveActorTestCommand(actorInstanceRegistry, linearActorMovementService, actorLifecycleService);
		instance.getCommand("moveactortest").setExecutor(moveActorTestCommand);

		FollowTestCommand followTestCommand = new FollowTestCommand(actorFollowService, actorInstanceRegistry);
		instance.getCommand("followtest").setExecutor(followTestCommand);

		RegionCommand regionCommand = new RegionCommand(
				regionResolver,
				new BukkitBlockPositionAdapter(),
				regionRegistry,
				regionEditingService,
				regionBrushItemService,
				regionTracking,
				regionDebug
		);
		instance.getCommand("region").setExecutor(regionCommand);
		instance.getCommand("region").setTabCompleter(regionCommand);
	}
	
}
