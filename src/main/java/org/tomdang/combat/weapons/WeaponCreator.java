package org.tomdang.combat.weapons;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.tomdang.combat.customcombatability.CustomCombatAbility;
import org.tomdang.customabilityframework.abilitylore.AbilityLoreRenderer;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customitemframework.CustomItemCreator;

import java.util.ArrayList;
import java.util.List;

public class WeaponCreator extends CustomItemCreator {
	public WeaponCreator(NamespacedKey customItemIdKey) {
		super(customItemIdKey);
	}

	public ItemStack createItemStack(Weapon weapon) {
		ItemStack itemTool = super.createItemStack(weapon);
		List<Component> lore = new ArrayList<>();

		ItemMeta meta = itemTool.getItemMeta();
		meta.setMaxStackSize(1);

		// Get the MiniMessage instance for parsing tags
		MiniMessage mm = MiniMessage.miniMessage();

		// 1. Use mm.deserialize() for tags, and .decoration(TextDecoration.ITALIC, false) to remove default italics
		lore.add(mm.deserialize("<gray>Damage: <gold>" + (int)weapon.getDamage()).decoration(TextDecoration.ITALIC, false));
		lore.add(mm.deserialize("<gray>Strength: <gold>" + (int)weapon.getStrength()).decoration(TextDecoration.ITALIC, false));

		lore.add(Component.empty());

		AbilityLoreRenderer abilityLoreRenderer = new AbilityLoreRenderer();

		for (CustomAbility ability : weapon.getCustomAbilities()) {
			List<Component> abilityLore = abilityLoreRenderer.convertCustomAbilityToLore(ability);
			lore.addAll(abilityLore);
		}

		lore.add(Component.empty());

		// 2. Applying your enum rarity color safely while turning off italics
		lore.add(Component.text(weapon.getRarity() + " " + weapon.getItemCategory())
				.color(weapon.getRarity().getColor())
				.decoration(TextDecoration.ITALIC, false));

		meta.lore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemTool.setItemMeta(meta);

		return itemTool;
	}
}
