package org.tomdang.bootstrap;

import org.tomdang.TomBlock;
import org.tomdang.actorframework.command.FollowTestCommand;
import org.tomdang.actorframework.command.MoveActorTestCommand;
import org.tomdang.actorframework.instance.ActorInstanceRegistry;
import org.tomdang.actorframework.lifecycle.ActorLifecycleService;
import org.tomdang.actorframework.movement.ActorFollowService;
import org.tomdang.actorframework.movement.LinearActorMovementService;
import org.tomdang.combat.command.GetWeapon;
import org.tomdang.combat.command.SetDefense;
import org.tomdang.combat.weapons.WeaponRegistry;
import org.tomdang.crafting.command.ForgeCommand;
import org.tomdang.customarmorframework.CustomArmorRegistry;
import org.tomdang.customarmorframework.CustomArmorService;
import org.tomdang.customarmorframework.command.GiveCustomArmor;
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
import org.tomdang.player.command.energy.RecoverEnergy;
import org.tomdang.player.command.energy.UseEnergy;
import org.tomdang.player.playerresource.PlayerResourceService;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.playernpc.command.NmsPlayerNpcTestCommand;
import org.tomdang.playernpc.lifecycle.PlayerNpcLifecycleService;

public class CommandRegistrar {
	
	public CommandRegistrar(TomBlock instance, PlayerProfileService playerProfileService, WeaponRegistry weaponRegistry, CustomMobRegistry customMobRegistry,
	                        CustomArmorService customArmorService, PlayerStatsService playerStatsService, CustomArmorRegistry customArmorRegistry,
	                        PlayerResourceService playerResourceService, MiningToolRegistry miningToolRegistry, DialogueSessionService dialogueSessionService,
	                        DialogueController dialogueController, PlayerNpcLifecycleService playerNpcLifecycleService,
	                        ActorInstanceRegistry actorInstanceRegistry, LinearActorMovementService linearActorMovementService, ActorLifecycleService actorLifecycleService,
	                        ActorFollowService actorFollowService
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

		SpawnCustomMob spawnCustomMob = new SpawnCustomMob(customMobRegistry);
		instance.getCommand("spawncustommob").setExecutor(spawnCustomMob);
		instance.getCommand("spawncustommob").setTabCompleter(spawnCustomMob);

		PlayerStatsCommand playerStatsCommand = new PlayerStatsCommand(playerProfileService, customArmorService, playerStatsService);
		instance.getCommand("stats").setExecutor(playerStatsCommand);

		SetDefense setDefense = new SetDefense(playerProfileService);
		instance.getCommand("setdefense").setExecutor(setDefense);

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
	}
	
}
