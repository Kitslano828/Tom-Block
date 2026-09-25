package org.tomdang.hud.protocol.lab;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.tomdang.hud.composition.HudRuntime;
import org.tomdang.hud.protocol.HudPackStateRegistry;
import org.tomdang.hud.protocol.bukkit.HudPackStatusListener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class HudLabCommand implements CommandExecutor {
	private final HudRuntime hud;
	private final HudPackStateRegistry packStates;
	private final HudPackStatusListener packStatus;
	private final Set<UUID> visible = new HashSet<>();
	public HudLabCommand(HudRuntime hud, HudPackStateRegistry packStates, HudPackStatusListener packStatus) {
		this.hud = java.util.Objects.requireNonNull(hud);
		this.packStates = java.util.Objects.requireNonNull(packStates);
		this.packStatus = java.util.Objects.requireNonNull(packStatus);
	}
	@Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) { sender.sendMessage("This command is player-only."); return true; }
		UUID playerId = player.getUniqueId();
		packStatus.synchronize(player);
		if (!packStates.canRender(playerId)) {
			player.sendMessage("The TomBlock resource pack is not ready (status: "
					+ packStates.state(playerId).name().toLowerCase(java.util.Locale.ROOT) + ").");
			return true;
		}
		if (visible.remove(playerId)) {
			hud.hide(playerId, HudLabElement.ID, Bukkit.getCurrentTick());
			player.sendMessage("HUD laboratory hidden.");
		} else {
			visible.add(playerId);
			hud.show(playerId, new HudLabElement(), Bukkit.getCurrentTick());
			player.sendMessage("HUD laboratory shown. Use /hudlab again to hide it.");
		}
		return true;
	}
}
