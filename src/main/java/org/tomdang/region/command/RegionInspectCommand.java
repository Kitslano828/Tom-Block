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
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.resolution.RegionResolver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RegionInspectCommand implements CommandExecutor, TabCompleter {
	private final RegionResolver resolver;
	private final BukkitBlockPositionAdapter positionAdapter;

	public RegionInspectCommand(RegionResolver resolver, BukkitBlockPositionAdapter positionAdapter) {
		if (resolver == null) throw new IllegalArgumentException("resolver cannot be null");
		if (positionAdapter == null) throw new IllegalArgumentException("positionAdapter cannot be null");
		this.resolver = resolver;
		this.positionAdapter = positionAdapter;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
			@NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("This command can only inspect a player's current block."));
			return true;
		}
		if (args.length != 1 || !args[0].equalsIgnoreCase("inspect")) {
			sender.sendMessage(Component.text("Usage: /region inspect", NamedTextColor.RED));
			return true;
		}

		BlockPosition position = positionAdapter.fromLocation(player.getLocation());
		List<RegionDefinition> direct = resolver.directRegionsAt(position);
		List<RegionDefinition> resolved = resolver.regionsAt(position);
		Set<String> directIds = new HashSet<>(direct.stream().map(RegionDefinition::id).toList());
		List<RegionDefinition> inherited = resolved.stream()
				.filter(region -> !directIds.contains(region.id()))
				.toList();

		sender.sendMessage(Component.text("Region inspection at ", NamedTextColor.GOLD)
				.append(Component.text(position.worldId() + " " + position.x() + ", " + position.y() + ", " + position.z(),
						NamedTextColor.YELLOW)));
		if (resolved.isEmpty()) {
			sender.sendMessage(Component.text("No configured region contains this block.", NamedTextColor.GRAY));
			return true;
		}

		sender.sendMessage(Component.text("Primary: ", NamedTextColor.GRAY).append(formatRegion(resolved.getFirst())));
		sender.sendMessage(Component.text("Direct: ", NamedTextColor.GRAY).append(formatRegions(direct, position, true)));
		sender.sendMessage(Component.text("Inherited: ", NamedTextColor.GRAY).append(formatRegions(inherited, position, false)));
		return true;
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
		if (args.length == 1 && "inspect".startsWith(args[0].toLowerCase())) return List.of("inspect");
		return List.of();
	}
}
