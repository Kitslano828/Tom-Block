package org.tomdang.combat.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tomdang.combat.weapons.Weapon;
import org.tomdang.combat.weapons.WeaponRegistry;

import java.util.ArrayList;
import java.util.List;

public class GetWeapon implements CommandExecutor, TabCompleter {

	WeaponRegistry weaponRegistry;
	public GetWeapon(WeaponRegistry weaponRegistry) {
		this.weaponRegistry = weaponRegistry;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("YOU ARE NOT A PLAYER");
			return true;
		}

		if (args.length != 1) {
			sender.sendMessage("§f§lUSAGE: /getweapon <name>");
			return true;
		}

		String weaponID = args[0];
		Weapon weapon = weaponRegistry.getWeapon(weaponID);
		ItemStack weaponItem = weaponRegistry.getWeaponAsItem(weapon);

		((Player) sender).getInventory().addItem(weaponItem);

		sender.sendMessage("Successfully added " + weapon.getId() + " to you inventory!");

		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		List<String> weaponList = weaponRegistry.getWeaponsAsList();
		if (args.length == 1) {
			List<String> completions = new ArrayList<>();
			for (String id : weaponList) {
				completions.add(id);
			}
			return completions;
		}

		return List.of();
	}
}
