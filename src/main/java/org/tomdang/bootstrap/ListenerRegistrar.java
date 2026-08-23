package org.tomdang.bootstrap;

import org.tomdang.TomBlock;
import org.tomdang.actorframework.combat.ActorDamageService;
import org.tomdang.actorframework.interaction.ActorInteractionService;
import org.tomdang.actorframework.listener.ActorDamageListener;
import org.tomdang.actorframework.listener.ActorInteractListener;
import org.tomdang.actorframework.resolver.ActorResolver;
import org.tomdang.combat.CombatService;
import org.tomdang.combat.listener.MobDeathListener;
import org.tomdang.combat.listener.MobHitListener;
import org.tomdang.combat.listener.PlayerRespawnListener;
import org.tomdang.crafting.CraftingService;
import org.tomdang.crafting.gui.listener.ForgeCloseListener;
import org.tomdang.crafting.gui.listener.ForgeDragListener;
import org.tomdang.crafting.gui.listener.ForgeListener;
import org.tomdang.customabilityframework.CustomAbilityService;
import org.tomdang.customabilityframework.listener.PlayerInteractListener;
import org.tomdang.customarmorframework.listener.PlayerEquipArmorListener;
import org.tomdang.custommobframework.custommobdrops.MobRewardService;
import org.tomdang.custommobframework.custommobspawn.CustomMobRespawnService;
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
import org.tomdang.player.playerdata.PlayerProfileStorage;
import org.tomdang.player.playerresource.PlayerResourceService;
import org.tomdang.playernpc.integration.actor.PlayerNpcActorVisibilityService;
import org.tomdang.playernpc.lifecycle.PlayerNpcLifecycleService;
import org.tomdang.playernpc.listener.PlayerNpcConnectionListener;

public class ListenerRegistrar {

	public ListenerRegistrar(TomBlock instance, PlayerProfileService playerProfileService,
	                         PlayerProfileStorage playerProfileStorage, PlayerResourceService playerResourceService,
	                         CustomAbilityService customAbilityService, MobRewardService mobRewardService, MiningService miningService,
	                         CombatService combatService, CustomMobRespawnService customMobRespawnService, CraftingService craftingService,
							 ActorResolver actorResolver, ActorInteractionService actorInteractionService, ActorDamageService actorDamageService,
							 DialogueSessionService dialogueSessionService, DialogueAdvanceService dialogueAdvanceService, DialogueController dialogueController,
							 PlayerNpcLifecycleService playerNpcLifecycleService, PlayerNpcActorVisibilityService playerNpcActorVisibilityService
	){
		PlayerConnectionListener playerConnectionListener = new PlayerConnectionListener(
				playerProfileService,
				playerProfileStorage,
				playerResourceService,
				dialogueSessionService,
				dialogueController
		);
		MiningListener miningListener = new MiningListener(miningService);
		MobHitListener mobHitListener = new MobHitListener(combatService);
		PlayerRespawnListener playerRespawnListener = new PlayerRespawnListener(combatService);
		MobDeathListener mobDeathListener = new MobDeathListener(mobRewardService, customMobRespawnService);
		PlayerMenuListener playerMenuListener = new PlayerMenuListener();
		PlayerEquipArmorListener playerEquipArmorListener = new PlayerEquipArmorListener(playerResourceService);
		PlayerInteractListener playerInteractListener = new PlayerInteractListener(customAbilityService);
		PlayerRegainHealthListener playerRegainHealthListener = new PlayerRegainHealthListener();
		ForgeCloseListener forgeCloseListener = new ForgeCloseListener();
		ForgeDragListener forgeDragListener = new ForgeDragListener(instance, craftingService);
		ForgeListener forgeListener = new ForgeListener(instance, craftingService);
		ActorInteractListener actorInteractListener = new ActorInteractListener(actorResolver, actorInteractionService);
		ActorDamageListener actorDamageListener = new ActorDamageListener(actorDamageService);
		DialogueSneakListener dialogueSneakListener = new DialogueSneakListener(dialogueSessionService, dialogueAdvanceService);
		PlayerNpcConnectionListener playerNpcConnectionListener = new PlayerNpcConnectionListener(playerNpcLifecycleService, playerNpcActorVisibilityService);


		instance.getServer().getPluginManager().registerEvents(playerInteractListener, instance);
		instance.getServer().getPluginManager().registerEvents(playerConnectionListener, instance);
		instance.getServer().getPluginManager().registerEvents(miningListener,instance);
		instance.getServer().getPluginManager().registerEvents(mobHitListener, instance);
		instance.getServer().getPluginManager().registerEvents(playerRespawnListener, instance);
		instance.getServer().getPluginManager().registerEvents(mobDeathListener, instance);
		instance.getServer().getPluginManager().registerEvents(playerMenuListener, instance);
		instance.getServer().getPluginManager().registerEvents(playerEquipArmorListener, instance);
		instance.getServer().getPluginManager().registerEvents(playerRegainHealthListener, instance);
		instance.getServer().getPluginManager().registerEvents(forgeCloseListener, instance);
		instance.getServer().getPluginManager().registerEvents(forgeDragListener, instance);
		instance.getServer().getPluginManager().registerEvents(forgeListener, instance);
		instance.getServer().getPluginManager().registerEvents(actorInteractListener, instance);
		instance.getServer().getPluginManager().registerEvents(actorDamageListener, instance);
		instance.getServer().getPluginManager().registerEvents(dialogueSneakListener, instance);
		instance.getServer().getPluginManager().registerEvents(playerNpcConnectionListener, instance);
	}

}
