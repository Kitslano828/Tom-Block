package org.tomdang.hud.protocol.bukkit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.tomdang.hud.protocol.*;

public final class HudPackStatusListener implements Listener {
	private final HudPackStateRegistry states;
	private final java.util.logging.Logger logger;
	private final Plugin plugin;
	private final java.util.UUID packId;
	public HudPackStatusListener(HudPackStateRegistry states, java.util.logging.Logger logger,
			Plugin plugin, java.util.UUID packId) {
		this.states = java.util.Objects.requireNonNull(states);
		this.logger = java.util.Objects.requireNonNull(logger);
		this.plugin = java.util.Objects.requireNonNull(plugin);
		this.packId = java.util.Objects.requireNonNull(packId);
	}
	@EventHandler public void onStatus(PlayerResourcePackStatusEvent event) {
		if (!packId.equals(event.getID())) {
			logger.fine("Ignoring resource-pack status for unrelated pack " + event.getID());
			return;
		}
		HudPackState state = translate(event.getStatus());
		states.update(event.getPlayer().getUniqueId(), state);
		logger.info("HUD pack " + event.getID() + " for " + event.getPlayer().getName() + ": "
				+ event.getStatus() + " -> " + states.state(event.getPlayer().getUniqueId()));
		if (state == HudPackState.DECLINED || state == HudPackState.FAILED)
			event.getPlayer().sendMessage(Component.text("TomBlock HUD disabled because its resource pack is unavailable.", NamedTextColor.YELLOW));
	}
	@EventHandler(priority = EventPriority.MONITOR) public void onJoin(PlayerJoinEvent event) {
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> synchronize(event.getPlayer()), 20L);
	}
	public void synchronize(org.bukkit.entity.Player player) {
		var status = player.getResourcePackStatus();
		if (status != null) states.update(player.getUniqueId(), translate(status));
	}
	private HudPackState translate(PlayerResourcePackStatusEvent.Status status) {
		return switch (status) {
			case ACCEPTED, DOWNLOADED -> HudPackState.ACCEPTED;
			case SUCCESSFULLY_LOADED -> HudPackState.LOADED;
			case DECLINED, DISCARDED -> HudPackState.DECLINED;
			case FAILED_DOWNLOAD, FAILED_RELOAD, INVALID_URL -> HudPackState.FAILED;
		};
	}
	@EventHandler public void onQuit(PlayerQuitEvent event) { states.remove(event.getPlayer().getUniqueId()); }
}
