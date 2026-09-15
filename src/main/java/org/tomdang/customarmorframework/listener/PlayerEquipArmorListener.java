package org.tomdang.customarmorframework.listener;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.tomdang.player.playerresource.PlayerResourceService;
import org.tomdang.customitemframework.refresh.PlayerItemRefreshScheduler;

public class PlayerEquipArmorListener implements Listener {

	private final PlayerResourceService playerResourceService;
	private final PlayerItemRefreshScheduler itemRefreshScheduler;

	public PlayerEquipArmorListener(PlayerResourceService playerResourceService,
	                                PlayerItemRefreshScheduler itemRefreshScheduler) {
		if (playerResourceService == null) throw new IllegalArgumentException("playerResourceService cannot be null");
		if (itemRefreshScheduler == null) throw new IllegalArgumentException("itemRefreshScheduler cannot be null");
		this.playerResourceService = playerResourceService;
		this.itemRefreshScheduler = itemRefreshScheduler;
	}

	@EventHandler
	public void onArmorChange(PlayerArmorChangeEvent event) {
		playerResourceService.reconcilePlayerHealth(event.getPlayer());
		itemRefreshScheduler.requestRefresh(event.getPlayer());
	}


}
