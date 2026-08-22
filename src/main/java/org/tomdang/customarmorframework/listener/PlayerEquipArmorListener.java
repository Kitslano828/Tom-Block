package org.tomdang.customarmorframework.listener;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.tomdang.player.playerresource.PlayerResourceService;

public class PlayerEquipArmorListener implements Listener {

	PlayerResourceService playerResourceService;

	public PlayerEquipArmorListener(PlayerResourceService playerResourceService) {
		this.playerResourceService = playerResourceService;
	}

	@EventHandler
	public void onArmorChange(PlayerArmorChangeEvent event) {
		playerResourceService.reconcilePlayerHealth(event.getPlayer());
		// else does nothing
	}


}
