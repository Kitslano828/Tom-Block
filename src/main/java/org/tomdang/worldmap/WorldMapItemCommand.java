package org.tomdang.worldmap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class WorldMapItemCommand implements CommandExecutor {
	private final WorldMapItemService service;

	public WorldMapItemCommand(WorldMapItemService service) {
		this.service = service;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("Only players can receive a map item.");
			return true;
		}
		if (args.length != 1 || !(args[0].equalsIgnoreCase("full") || args[0].equalsIgnoreCase("local")))
			return false;
		boolean local = args[0].equalsIgnoreCase("local");
		if (local && !service.isCalibrated()) {
			player.sendMessage("The source image needs its world X/Z bounds in world-map.yml before local zoom can work.");
			return true;
		}
		try {
			player.getInventory().addItem(service.create(player, local));
			player.sendMessage(local ? "Local map added. Hold it to view the player-centered crop."
					: "World map added. Hold it to view the exported image.");
		} catch (IllegalStateException exception) {
			player.sendMessage(exception.getMessage());
		}
		return true;
	}
}
