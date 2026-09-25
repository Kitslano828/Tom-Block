package org.tomdang.hud.status;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerResourceValuesHudConnectionListener implements Listener {
	private final PlayerResourceValuesHudService values;
	public PlayerResourceValuesHudConnectionListener(PlayerResourceValuesHudService values) {
		this.values = java.util.Objects.requireNonNull(values);
	}
	@EventHandler public void onJoin(PlayerJoinEvent event) { values.refresh(event.getPlayer()); }
	@EventHandler public void onQuit(PlayerQuitEvent event) { values.forget(event.getPlayer().getUniqueId()); }
}
