package org.tomdang.mining.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tomdang.mining.miningtool.MiningTool;
import org.tomdang.mining.miningtool.MiningToolCreator;
import org.tomdang.mining.miningtool.MiningToolRegistry;

import java.util.ArrayList;
import java.util.List;

public class GetMiningTool implements CommandExecutor, TabCompleter {

	private final MiningToolRegistry miningToolRegistry;
	public GetMiningTool(MiningToolRegistry miningToolRegistry) {
		this.miningToolRegistry = miningToolRegistry;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage("YOU ARE NOT A PLAYER");
			return true;
		}

		if (args.length != 1) {
			sender.sendMessage("§f§lUSAGE: /getminingtool <name>");
			return true;
		}

		String toolID = args[0];
		MiningTool miningTool = miningToolRegistry.getMiningTool(toolID);
		ItemStack toolItem = miningToolRegistry.getMiningToolAsItem(miningTool);

		((Player) sender).getInventory().addItem(toolItem);

		sender.sendMessage("Successfully added " + miningTool.getId() + " to you inventory!");

		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		List<String> toolList = miningToolRegistry.getToolsAsList();
		if (args.length == 1) {
			List<String> completions = new ArrayList<>();
			for (String id : toolList) {
				completions.add(id);
			}
			return completions;
		}

		return List.of();
	}
}
