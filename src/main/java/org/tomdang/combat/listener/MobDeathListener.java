package org.tomdang.combat.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.tomdang.custommobframework.custommobdrops.MobRewardService;
import org.tomdang.custommobframework.custommobspawn.CustomMobRespawnService;

public class MobDeathListener implements Listener {

	MobRewardService mobRewardService;
	CustomMobRespawnService customMobRespawnService;

	public MobDeathListener(MobRewardService mobRewardService, CustomMobRespawnService customMobRespawnService) {
		this.mobRewardService = mobRewardService;
		this.customMobRespawnService = customMobRespawnService;
	}

	@EventHandler
	public void onMobDeath(EntityDeathEvent event) {
		mobRewardService.givePlayerMobDrops(event);
		customMobRespawnService.handleMobDeath(event);
	}
}
