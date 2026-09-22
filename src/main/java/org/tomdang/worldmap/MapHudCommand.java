package org.tomdang.worldmap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MapHudCommand implements CommandExecutor {
	private final MapTestService maps;

	public MapHudCommand(MapTestService maps) {
		this.maps = maps;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("Only players can view the map HUD.");
			return true;
		}
		if (args.length > 1) return false;
		if (args.length == 1 && args[0].equalsIgnoreCase("off")) {
			maps.hide(player);
			player.sendMessage("Map HUD hidden.");
			return true;
		}
		String mode = args.length == 0 ? "live" : args[0].toLowerCase(java.util.Locale.ROOT);
		boolean enabled = switch (mode) {
			case "static" -> maps.toggleStaticProbe(player);
			case "marker" -> maps.toggleMarkerProbe(player);
			case "live" -> maps.toggleImage(player);
			case "centered" -> maps.toggleCenteredProbe(player);
			default -> { yield false; }
		};
		if (!(mode.equals("static") || mode.equals("marker") || mode.equals("live")
				|| mode.equals("centered"))) return false;
		player.sendMessage(enabled ? "Map HUD " + mode + " test enabled." : "Map HUD hidden.");
		return true;
	}
}
