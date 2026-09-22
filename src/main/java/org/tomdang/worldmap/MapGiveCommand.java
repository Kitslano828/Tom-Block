package org.tomdang.worldmap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Gives one of the two map-item views without exposing the old test subcommand. */
public final class MapGiveCommand implements CommandExecutor {
	private final WorldMapItemService maps;
	private final boolean local;

	public MapGiveCommand(WorldMapItemService maps, boolean local) {
		if (maps == null) throw new IllegalArgumentException("maps cannot be null");
		this.maps = maps;
		this.local = local;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 0) return false;
		if (!(sender instanceof Player player)) {
			sender.sendMessage("Only players can receive a map item.");
			return true;
		}
		if (local && !maps.isCalibrated()) {
			player.sendMessage("The map needs calibrated world X/Z bounds first.");
			return true;
		}
		try {
			player.getInventory().addItem(maps.create(player, local));
			player.sendMessage(local ? "Minimap item added. Hold it to view terrain around you."
					: "World map item added. Hold it to view the whole world.");
		} catch (IllegalStateException exception) {
			player.sendMessage(exception.getMessage());
		}
		return true;
	}
}
