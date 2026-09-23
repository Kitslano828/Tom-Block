package org.tomdang.foraging;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tomdang.foraging.audit.TreeAuditClassification;
import org.tomdang.foraging.audit.TreeAuditVisualizationService;

public final class ForagingTreeCommand implements CommandExecutor {
	private final ForagingService service;
	private final ForagingTreeRegistry registry;
	private final TreeModelRegistry models;
	private final TreeAuditVisualizationService auditVisualization;
	public ForagingTreeCommand(ForagingService service, ForagingTreeRegistry registry, TreeModelRegistry models,
			TreeAuditVisualizationService auditVisualization) {
		this.service = service;
		this.registry = registry;
		this.models = models;
		this.auditVisualization = auditVisualization;
	}

	@Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
			@NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) { sender.sendMessage("Players only."); return true; }
		if (args.length >= 1 && args[0].equalsIgnoreCase("audit")) {
			String mode = args.length == 1 ? "all" : args[1].toLowerCase();
			switch (mode) {
				case "on", "all" -> {
					auditVisualization.showAll(player);
					player.sendMessage("§aTree audit enabled: §2green §ais confident, §6orange §ais ambiguous, §dmagenta §ais landmark.");
				}
				case "confident" -> {
					auditVisualization.show(player, TreeAuditClassification.CONFIDENT_TREE);
					player.sendMessage("§aShowing confident tree candidates in green.");
				}
				case "ambiguous" -> {
					auditVisualization.show(player, TreeAuditClassification.AMBIGUOUS);
					player.sendMessage("§6Showing ambiguous tree candidates in orange.");
				}
				case "landmark" -> {
					auditVisualization.show(player, TreeAuditClassification.LANDMARK_TREE);
					player.sendMessage("§dShowing reserved landmark trees in magenta.");
				}
				case "off" -> {
					auditVisualization.hide(player);
					player.sendMessage("§7Tree audit visualization disabled.");
				}
				default -> player.sendMessage("§cUse /foragingtree audit <all|confident|ambiguous|landmark|off>.");
			}
			return true;
		}
		if (args.length >= 1 && args[0].equalsIgnoreCase("grove")) {
			Location origin = player.getLocation().getBlock().getLocation().add(4, 0, 0);
			int offset = 0;
			for (TreeModel model : models.all()) {
				String id = "GROVE_" + model.id();
				if (registry.find(id).isEmpty()) service.place(new ForagingTree(id, origin.clone().add(offset, 0, 0), model));
				offset += 9;
			}
			player.sendMessage("§aPlaced the configured foraging test grove.");
			return true;
		}
		if (args.length == 3 && args[0].equalsIgnoreCase("register")) {
			var target = player.getTargetBlockExact(12);
			if (target == null) { player.sendMessage("§cLook at the root log to register."); return true; }
			String id = args[1].toUpperCase();
			try {
				TreeModel model = models.require(args[2].toUpperCase());
				if (target.getType() != model.logMaterial()) { player.sendMessage("§cThe target is not " + model.logMaterial() + "."); return true; }
				service.registerNatural(new ForagingTree(id, target.getLocation(), model));
				player.sendMessage("§aRegistered existing tree §f" + id + "§a. No blocks were changed.");
			} catch (IllegalArgumentException exception) { player.sendMessage("§c" + exception.getMessage()); }
			return true;
		}
		if (args.length < 2 || args.length > 3 || !args[0].equalsIgnoreCase("spawn")) return false;
		String id = args[1].toUpperCase();
		TreeModel model;
		try { model = models.require(args.length == 3 ? args[2].toUpperCase() : "MODEL_OAK"); }
		catch (IllegalArgumentException exception) { player.sendMessage("§c" + exception.getMessage()); return true; }
		if (registry.find(id).isPresent()) { player.sendMessage("§cA registered tree already uses that id."); return true; }
		Location root = player.getTargetBlockExact(12) == null
				? player.getLocation().add(player.getLocation().getDirection().setY(0).normalize().multiply(4)).getBlock().getLocation()
				: player.getTargetBlockExact(12).getLocation().add(0, 1, 0);
		try {
			service.place(new ForagingTree(id, root, model));
			player.sendMessage("§aPlaced registered tree §f" + id + "§a using §f" + model.id() + "§a.");
		} catch (IllegalArgumentException exception) {
			player.sendMessage("§c" + exception.getMessage());
		}
		return true;
	}
}
