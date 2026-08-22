package org.tomdang.mining.miningtool;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.tomdang.combat.customcombatability.CustomCombatAbility;
import org.tomdang.customabilityframework.abilitylore.AbilityLoreRenderer;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customitemframework.CustomItemCreator;
import org.tomdang.mining.customminingability.CustomMiningAbility;

import java.util.ArrayList;
import java.util.List;

public class MiningToolCreator extends CustomItemCreator {

	public MiningToolCreator(NamespacedKey miningToolIdKey) {
		super(miningToolIdKey);
	}

	public ItemStack createItemStack(MiningTool miningTool) {
		ItemStack itemTool = super.createItemStack(miningTool);
		List<Component> lore = new ArrayList<>();

		ItemMeta meta = itemTool.getItemMeta();
		if (meta == null) return itemTool; // Quick defensive check for safety

		// Initialize MiniMessage instance
		MiniMessage mm = MiniMessage.miniMessage();

		// Parse the tag strings using deserialize() and explicitly strip out Minecraft's default italics
		lore.add(mm.deserialize("<dark_gray>Breaking Power: " + miningTool.getBreakingPower()).decoration(TextDecoration.ITALIC, false));

		// Empty strings don't hold style attributes, but forcing no-italics prevents spacing bugs
		lore.add(Component.empty());

		lore.add(mm.deserialize("<gray>Mining Speed: <gold>" + (int)miningTool.getMiningSpeed()).decoration(TextDecoration.ITALIC, false));
		lore.add(mm.deserialize("<gray>Mining Fortune: <gold>" + (int)miningTool.getFortune()).decoration(TextDecoration.ITALIC, false));

		lore.add(Component.empty());

		AbilityLoreRenderer abilityLoreRenderer = new AbilityLoreRenderer();

		for (CustomAbility ability : miningTool.getCustomAbilities()) {
			List<Component> abilityLore = abilityLoreRenderer.convertCustomAbilityToLore(ability);
			lore.addAll(abilityLore);
		}

		lore.add(Component.empty());

		// Fixed: Added a space string " " between the Rarity name and Item Category so they don't mash together
		lore.add(Component.text(miningTool.getRarity() + " " + miningTool.getItemCategory())
				.color(miningTool.getRarity().getColor())
				.decoration(TextDecoration.ITALIC, false));

		meta.lore(lore);
		itemTool.setItemMeta(meta);

		return itemTool;
	}
}
