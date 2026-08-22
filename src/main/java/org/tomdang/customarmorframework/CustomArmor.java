package org.tomdang.customarmorframework;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;

import java.util.*;
import java.util.function.Supplier;

public class CustomArmor extends CustomItem {
	// Armor should have every stat as there will be armor for each skill and eventually one that is a jack of all trades
	@Getter
	private final ArmorSlot armorSlot;
	@Getter
	private final double health;
	@Getter
	private final double defense;
	@Getter
	private final Color color;

	private final Map<String, Supplier<Double>> statRegistry = new LinkedHashMap<>();
	public CustomArmor(String id, Material material, String displayName, Rarity rarity,
					   ItemCategory itemCategory, ArmorSlot armorSlot, double health, double defense, Color color) {
		super(id, material, displayName, rarity, itemCategory);
		this.armorSlot = armorSlot;
		this.health = health;
		this.defense = defense;
		this.color = color;

		statRegistry.put("Health", this::getHealth);
		statRegistry.put("Defense", this::getDefense);
	}

	// CHANGED: Returns List<Component> instead of List<String>
	public List<Component> getStatsAsComponents() {
		List<Component> statsList = new ArrayList<>();

		statRegistry.forEach((statName, statValueSupplier) -> {
			double value = statValueSupplier.get();
			if (value > 0) {
				// Create the gray text prefix (e.g., "Health: +")
				Component prefix = Component.text(statName + ": ", NamedTextColor.GRAY).
						decoration(TextDecoration.ITALIC, false);

				// Create the gold text suffix for the number (e.g., "50.0")
				Component number = Component.text(value, NamedTextColor.GOLD).
						decoration(TextDecoration.ITALIC, false);;

				// Join them together seamlessly on a single line
				statsList.add(prefix.append(number));
			}
		});

		return statsList;
	}
}
