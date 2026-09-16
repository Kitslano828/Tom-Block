package org.tomdang.region.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tomdang.region.bukkit.BukkitBlockPositionAdapter;
import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.edit.RegionBrushItemService;
import org.tomdang.region.edit.RegionEditAction;
import org.tomdang.region.edit.RegionEditingService;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.resolution.RegionResolver;
import org.tomdang.region.bukkit.RegionTrackingDebugService;
import org.tomdang.region.tracking.PlayerRegionTrackingService;
import org.tomdang.region.tracking.RegionMembershipSnapshot;
import org.tomdang.region.tracking.RegionMembershipTransition;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class RegionCommand implements CommandExecutor, TabCompleter {
	private final RegionResolver resolver;
	private final BukkitBlockPositionAdapter positionAdapter;
	private final RegionRegistry registry;
	private final RegionEditingService editingService;
	private final RegionBrushItemService brushItemService;
	private final PlayerRegionTrackingService tracking;
	private final RegionTrackingDebugService debug;

	public RegionCommand(RegionResolver resolver, BukkitBlockPositionAdapter positionAdapter,
			RegionRegistry registry, RegionEditingService editingService, RegionBrushItemService brushItemService) {
		this(resolver, positionAdapter, registry, editingService, brushItemService, null, null);
	}

	public RegionCommand(RegionResolver resolver, BukkitBlockPositionAdapter positionAdapter,
			RegionRegistry registry, RegionEditingService editingService, RegionBrushItemService brushItemService,
			PlayerRegionTrackingService tracking, RegionTrackingDebugService debug) {
		if (resolver == null) throw new IllegalArgumentException("resolver cannot be null");
		if (positionAdapter == null) throw new IllegalArgumentException("positionAdapter cannot be null");
		if (registry == null) throw new IllegalArgumentException("registry cannot be null");
		if (editingService == null) throw new IllegalArgumentException("editingService cannot be null");
		if (brushItemService == null) throw new IllegalArgumentException("brushItemService cannot be null");
		this.resolver = resolver;
		this.positionAdapter = positionAdapter;
		this.registry = registry;
		this.editingService = editingService;
		this.brushItemService = brushItemService;
		this.tracking = tracking;
		this.debug = debug;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
			@NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("This command can only inspect a player's current block."));
			return true;
		}
		if (args.length == 0) {
			sendUsage(sender);
			return true;
		}
		return switch (args[0].toLowerCase(Locale.ROOT)) {
			case "inspect" -> inspect(player, args);
			case "track" -> track(player, args);
			case "edit" -> edit(player, args);
			case "undo" -> undo(player, args);
			case "save" -> save(player, args);
			case "cancel" -> cancel(player, args);
			default -> {
				sendUsage(sender);
				yield true;
			}
		};
	}

	private boolean track(Player player, String[] args) {
		if (!player.hasPermission("tomblock.admin.region.edit")) {
			player.sendMessage(Component.text("You do not have permission to track regions.", NamedTextColor.RED));
			return true;
		}
		if (tracking == null || debug == null) {
			player.sendMessage(Component.text("Region tracking is unavailable.", NamedTextColor.RED));
			return true;
		}
		if (args.length > 2 || (args.length == 2 && !args[1].equalsIgnoreCase("status"))) {
			player.sendMessage(Component.text("Usage: /region track [status]", NamedTextColor.RED));
			return true;
		}
		if (args.length == 1) {
			boolean enabled = debug.toggle(player.getUniqueId());
			player.sendMessage(Component.text("Region transition chat messages " + (enabled ? "enabled" : "disabled") + ".",
					enabled ? NamedTextColor.GREEN : NamedTextColor.YELLOW));
		}
		RegionMembershipSnapshot snapshot = tracking.snapshot(player.getUniqueId()).orElse(null);
		if (snapshot == null) {
			player.sendMessage(Component.text("No cached region snapshot yet.", NamedTextColor.GRAY));
			return true;
		}
		player.sendMessage(Component.text("Cached primary: " + snapshot.primary().orElse("none"), NamedTextColor.AQUA));
		player.sendMessage(Component.text("Direct: " + names(snapshot.direct()), NamedTextColor.GRAY));
		player.sendMessage(Component.text("Resolved: " + names(snapshot.resolved()), NamedTextColor.GRAY));
		RegionMembershipTransition last = debug.last(player.getUniqueId()).orElse(null);
		if (last != null) player.sendMessage(Component.text("Last transition: entered=" + names(last.entered())
				+ "; left=" + names(last.left()), NamedTextColor.YELLOW));
		return true;
	}

	private String names(List<String> ids) {
		return ids.isEmpty() ? "none" : String.join(", ", ids);
	}

	private boolean inspect(Player player, String[] args) {
		if (args.length != 1) {
			sendUsage(player);
			return true;
		}

		BlockPosition position = positionAdapter.fromLocation(player.getLocation());
		List<RegionDefinition> direct = resolver.directRegionsAt(position);
		List<RegionDefinition> resolved = resolver.regionsAt(position);
		Set<String> directIds = new HashSet<>(direct.stream().map(RegionDefinition::id).toList());
		List<RegionDefinition> inherited = resolved.stream()
				.filter(region -> !directIds.contains(region.id()))
				.toList();

		player.sendMessage(Component.text("Region inspection at ", NamedTextColor.GOLD)
				.append(Component.text(position.worldId() + " " + position.x() + ", " + position.y() + ", " + position.z(),
						NamedTextColor.YELLOW)));
		if (resolved.isEmpty()) {
			player.sendMessage(Component.text("No configured region contains this block.", NamedTextColor.GRAY));
			return true;
		}

		player.sendMessage(Component.text("Primary: ", NamedTextColor.GRAY).append(formatRegion(resolved.getFirst())));
		player.sendMessage(Component.text("Direct: ", NamedTextColor.GRAY).append(formatRegions(direct, position, true)));
		player.sendMessage(Component.text("Inherited: ", NamedTextColor.GRAY).append(formatRegions(inherited, position, false)));
		return true;
	}

	private boolean edit(Player player, String[] args) {
		if (!requireEditPermission(player) || args.length != 2) {
			if (args.length != 2) player.sendMessage(Component.text("Usage: /region edit <region-id>", NamedTextColor.RED));
			return true;
		}
		RegionDefinition region = registry.find(args[1]).orElse(null);
		if (region == null) {
			player.sendMessage(Component.text("Unknown region: " + args[1], NamedTextColor.RED));
			return true;
		}
		editingService.begin(player.getUniqueId(), region.id());
		player.getInventory().addItem(brushItemService.create()).values()
				.forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
		player.sendMessage(Component.text("Editing region ", NamedTextColor.GRAY)
				.append(Component.text(region.id(), NamedTextColor.AQUA))
				.append(Component.text(". A Region Brush was added to your inventory.", NamedTextColor.GRAY)));
		return true;
	}

	private boolean undo(Player player, String[] args) {
		if (!requireEditPermission(player) || args.length != 1) return true;
		if (editingService.session(player.getUniqueId()).isEmpty()) {
			player.sendMessage(Component.text("You do not have an active region edit session.", NamedTextColor.RED));
			return true;
		}
		RegionEditAction action = editingService.undo(player.getUniqueId()).orElse(null);
		if (action == null) {
			player.sendMessage(Component.text("There are no region edits to undo.", NamedTextColor.GRAY));
			return true;
		}
		player.sendMessage(Component.text("Undid " + action.appliedState() + " at ", NamedTextColor.YELLOW)
				.append(Component.text(action.position().x() + ", " + action.position().y() + ", " + action.position().z(),
						NamedTextColor.GRAY)));
		return true;
	}

	private boolean save(Player player, String[] args) {
		if (!requireEditPermission(player) || args.length != 1) return true;
		try {
			editingService.save();
			player.sendMessage(Component.text("Region overrides saved.", NamedTextColor.GREEN));
		} catch (IOException exception) {
			player.sendMessage(Component.text("Could not save region overrides. Changes remain in memory.", NamedTextColor.RED));
		}
		return true;
	}

	private boolean cancel(Player player, String[] args) {
		if (!requireEditPermission(player) || args.length != 1) return true;
		boolean existed = editingService.session(player.getUniqueId()).isPresent();
		editingService.end(player.getUniqueId());
		player.sendMessage(Component.text(existed
				? "Region edit session closed. Live unsaved changes were not discarded."
				: "You did not have an active region edit session.", existed ? NamedTextColor.YELLOW : NamedTextColor.GRAY));
		return true;
	}

	private boolean requireEditPermission(Player player) {
		if (player.hasPermission("tomblock.admin.region.edit")) return true;
		player.sendMessage(Component.text("You do not have permission to edit regions.", NamedTextColor.RED));
		return false;
	}

	private void sendUsage(CommandSender sender) {
		sender.sendMessage(Component.text("Usage: /region <inspect|track|edit|undo|save|cancel>", NamedTextColor.RED));
	}

	private Component formatRegions(List<RegionDefinition> regions, BlockPosition position, boolean showSource) {
		if (regions.isEmpty()) return Component.text("none", NamedTextColor.DARK_GRAY);
		Component output = Component.empty();
		for (int index = 0; index < regions.size(); index++) {
			if (index > 0) output = output.append(Component.text(", ", NamedTextColor.GRAY));
			output = output.append(formatRegion(regions.get(index)));
			if (showSource) {
				output = output.append(Component.text(
						" source=" + resolver.evaluateDirect(regions.get(index), position).source(),
						NamedTextColor.DARK_GRAY));
			}
		}
		return output;
	}

	private Component formatRegion(RegionDefinition region) {
		String tags = region.tags().isEmpty() ? "" : " tags=" + String.join("|", region.tags());
		return Component.text(region.id(), NamedTextColor.AQUA)
				.append(Component.text(" [priority=" + region.priority() + tags + "]", NamedTextColor.DARK_GRAY));
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
			@NotNull String alias, @NotNull String[] args) {
		if (args.length == 1) {
			String prefix = args[0].toLowerCase(Locale.ROOT);
			return List.of("inspect", "track", "edit", "undo", "save", "cancel").stream()
					.filter(value -> value.startsWith(prefix))
					.toList();
		}
		if (args.length == 2 && args[0].equalsIgnoreCase("track")) {
			return "status".startsWith(args[1].toLowerCase(Locale.ROOT)) ? List.of("status") : List.of();
		}
		if (args.length == 2 && args[0].equalsIgnoreCase("edit")) {
			String prefix = args[1].toLowerCase(Locale.ROOT);
			return registry.all().stream().map(RegionDefinition::id)
					.filter(id -> id.toLowerCase(Locale.ROOT).startsWith(prefix))
					.sorted()
					.toList();
		}
		return List.of();
	}
}
