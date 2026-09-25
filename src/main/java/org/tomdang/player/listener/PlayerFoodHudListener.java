package org.tomdang.player.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.entity.Player;

public final class PlayerFoodHudListener implements Listener {

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
		if (event.getEntity() instanceof Player player) stabilize(player);
    }

	@EventHandler
	public void onExhaustion(EntityExhaustionEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		event.setCancelled(true);
		stabilize(player);
	}

	private void stabilize(Player player) {
		player.setFoodLevel(20);
		player.setSaturation(20.0f);
		player.setExhaustion(0.0f);
	}
}
