package org.tomdang.worldmap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MapTestCommand implements CommandExecutor {
	private final MapTestService service;

	public MapTestCommand(MapTestService service) {
		this.service = service;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("Only players can use this command.");
			return true;
		}
		if (args.length != 0) return false;
		if (service.toggle(player)) {
			player.sendMessage("Village map test enabled. Gold diamond = you. Run /maptest again to hide it.");
			player.sendMessage("Concept colors: red buildings, yellow paths, dark-green trees, cyan pond.");
		} else {
			player.sendMessage("Village map test hidden.");
		}
		return true;
	}
}
