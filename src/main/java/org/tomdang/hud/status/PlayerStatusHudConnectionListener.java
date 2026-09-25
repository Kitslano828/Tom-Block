package org.tomdang.hud.status;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/** Releases status scheduling state when a player session ends. */
public final class PlayerStatusHudConnectionListener implements Listener {
	private final PlayerStatusHudService status;
	public PlayerStatusHudConnectionListener(PlayerStatusHudService status) {
		this.status = java.util.Objects.requireNonNull(status);
	}
	@EventHandler public void onQuit(PlayerQuitEvent event) { status.forget(event.getPlayer().getUniqueId()); }
}
