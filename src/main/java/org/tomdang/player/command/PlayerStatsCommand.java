package org.tomdang.player.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.tomdang.customarmorframework.ArmorBonuses;
import org.tomdang.customarmorframework.CustomArmorService;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerresource.PlayerStatsService;

import java.util.ArrayList;
import java.util.List;

public class PlayerStatsCommand implements CommandExecutor {

	private final PlayerProfileService playerProfileService;
	private final CustomArmorService customArmorService;
	private final PlayerStatsService playerStatsService;

	public PlayerStatsCommand(PlayerProfileService playerProfileService, CustomArmorService customArmorService, PlayerStatsService playerStatsService) {
		this.playerProfileService = playerProfileService;
		this.customArmorService = customArmorService;
		this.playerStatsService = playerStatsService;
	}


	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		if (!(sender instanceof Player player)) {
			System.out.println("YOU ARE NOT A PLAYER");
			return true;
		}

		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());

		TextComponent chestName = Component.text("            YOUR STATS").color(TextColor.color(0x290F0D));
		Inventory playerStatsGUI = Bukkit.createInventory(null, 27, chestName);

		ItemStack miningStat = createMiningStatItem(playerProfile);
		ItemStack combatStat = createCombatStatItem(player);

		playerStatsGUI.setItem(12, miningStat);
		playerStatsGUI.setItem(14, combatStat);

		player.openInventory(playerStatsGUI);

		return true;
	}

	public ItemStack createMiningStatItem(PlayerProfile playerProfile) {
		ItemStack miningStatItem = new ItemStack(Material.IRON_PICKAXE);
		ItemMeta pickaxeMeta = miningStatItem.getItemMeta();
		if (pickaxeMeta != null) {
			pickaxeMeta.itemName(MiniMessage.miniMessage().deserialize("<white>Mining Stats").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
			List<Component> lore = new ArrayList<>();
			lore.add(MiniMessage.miniMessage().deserialize("<gray>Mining Fortune: " + "<gold>" + playerProfile.getMiningFortune()).decoration(TextDecoration.ITALIC, false));
			lore.add(MiniMessage.miniMessage().deserialize(""));
			lore.add(MiniMessage.miniMessage().deserialize(""));
			lore.add(MiniMessage.miniMessage().deserialize("<gray>Mining Level: " + "<gold>" + playerProfile.getMiningLVL()).decoration(TextDecoration.ITALIC, false));
			lore.add(MiniMessage.miniMessage().deserialize("<gray>Mining XP: " + "<gold>" + playerProfile.getMiningXP()).decoration(TextDecoration.ITALIC, false));
			pickaxeMeta.lore(lore);
			pickaxeMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			miningStatItem.setItemMeta(pickaxeMeta);
		}
		return miningStatItem;
	}

	public ItemStack createCombatStatItem(Player player) {
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		ArmorBonuses armorBonuses = customArmorService.calculateBonusStats(player);
		double totalDefense = playerStatsService.getTotalDefense(player);
		double totalEnergy = playerStatsService.getTotalEnergy(player);
		ItemStack combatStatItem = new ItemStack(Material.IRON_SWORD);
		ItemMeta swordMeta = combatStatItem.getItemMeta();
		if (swordMeta != null) {
			swordMeta.itemName(MiniMessage.miniMessage().deserialize("<white>Combat Stats").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
			List<Component> lore = new ArrayList<>();
			lore.add(MiniMessage.miniMessage().deserialize("<red>Strength: " + "<white>" + playerProfile.getStrength()).decoration(TextDecoration.ITALIC, false));
			lore.add(MiniMessage.miniMessage().deserialize("<green>Current Health: " + "<white>" + playerProfile.getHealth().getCurrent()).decoration(TextDecoration.ITALIC, false));
			lore.add(MiniMessage.miniMessage().deserialize("<green>Total Health: " + "<white>" + playerStatsService.getTotalHealthStat(player)).decoration(TextDecoration.ITALIC, false));
			lore.add(MiniMessage.miniMessage().deserialize("<gold>Defense: " + "<white>" + playerProfile.getDefense()).decoration(TextDecoration.ITALIC, false));
			lore.add(MiniMessage.miniMessage().deserialize("<gold>Armor Defense: " + "<white>" + armorBonuses.getDefenseBonus()).decoration(TextDecoration.ITALIC, false));
			lore.add(MiniMessage.miniMessage().deserialize("<gold>Total Defense: " + "<white>" + totalDefense).decoration(TextDecoration.ITALIC, false));
			lore.add(MiniMessage.miniMessage().deserialize("<gold>Current Energy: " + "<white>" + playerProfile.getEnergy().getCurrent()).decoration(TextDecoration.ITALIC, false));
			lore.add(MiniMessage.miniMessage().deserialize("<gold>Total Energy: " + "<white>" + totalEnergy).decoration(TextDecoration.ITALIC, false));
			lore.add(MiniMessage.miniMessage().deserialize(""));
			lore.add(MiniMessage.miniMessage().deserialize(""));
			lore.add(MiniMessage.miniMessage().deserialize("<gray>Combat Level: " + "<gold>" + playerProfile.getCombatLvl()).decoration(TextDecoration.ITALIC, false));
			lore.add(MiniMessage.miniMessage().deserialize("<gray>Combat XP: " + "<gold>" + playerProfile.getCombatXP()).decoration(TextDecoration.ITALIC, false));
			swordMeta.lore(lore);
			swordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			combatStatItem.setItemMeta(swordMeta);
		}
		return combatStatItem;
	}
}
