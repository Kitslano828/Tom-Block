package org.tomdang.foraging;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ForagingTreeCommand implements CommandExecutor {
	private final ForagingService service;
	private final ForagingTreeRegistry registry;
	public ForagingTreeCommand(ForagingService service, ForagingTreeRegistry registry) {
		this.service = service;
		this.registry = registry;
	}

	@Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
			@NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) { sender.sendMessage("Players only."); return true; }
		if (args.length != 2 || !args[0].equalsIgnoreCase("spawn")) return false;
		String id = args[1].toUpperCase();
		if (registry.find(id).isPresent()) { player.sendMessage("§cA registered tree already uses that id."); return true; }
		Location root = player.getTargetBlockExact(12) == null
				? player.getLocation().add(player.getLocation().getDirection().setY(0).normalize().multiply(4)).getBlock().getLocation()
				: player.getTargetBlockExact(12).getLocation().add(0, 1, 0);
		try {
			service.place(new ForagingTree(id, root, TreeModel.modelOak()));
			player.sendMessage("§aPlaced registered model oak §f" + id + "§a. Only its recorded logs are harvestable.");
		} catch (IllegalArgumentException exception) {
			player.sendMessage("§c" + exception.getMessage());
		}
		return true;
	}
}
