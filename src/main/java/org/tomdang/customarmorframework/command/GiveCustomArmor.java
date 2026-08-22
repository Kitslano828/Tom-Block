package org.tomdang.customarmorframework.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tomdang.customarmorframework.CustomArmor;
import org.tomdang.customarmorframework.CustomArmorRegistry;

import java.util.ArrayList;
import java.util.List;

public class GiveCustomArmor implements CommandExecutor, TabCompleter {
	private final CustomArmorRegistry customArmorRegistry;

	public GiveCustomArmor(CustomArmorRegistry customArmorRegistry) {
		this.customArmorRegistry = customArmorRegistry;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("YOU ARE NOT A PLAYER");
			return true;
		}

		if (args.length != 1) {
			sender.sendMessage("§f§lUSAGE: /getcustomarmor <name>");
			return true;
		}

		String armorID = args[0];
		CustomArmor customArmor = customArmorRegistry.getArmor(armorID);
		ItemStack armorItem = customArmorRegistry.getCustomArmorAsItem(customArmor);

		((Player) sender).getInventory().addItem(armorItem);
		sender.sendMessage("Successfully added " + customArmor.getDisplayName() + " to your inventory");
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		List<String> toolList = customArmorRegistry.getCustomArmorAsList();
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
