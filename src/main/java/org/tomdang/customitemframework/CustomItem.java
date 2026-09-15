package org.tomdang.customitemframework;

import lombok.Getter;
import org.bukkit.Material;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.customitemframework.stats.CustomItemStatCapModifiers;

import java.util.ArrayList;
import java.util.List;

public class CustomItem {

	@Getter
	private final String id;
	@Getter
	private final Material material;
	@Getter
	private final String displayName;
	@Getter
	private final Rarity rarity;
	@Getter
	private final ItemCategory itemCategory;
	@Getter
	private final List<CustomAbility> customAbilities;
	@Getter
	private final CustomItemStatModifiers statModifiers;
	@Getter
	private final CustomItemStatCapModifiers statCapModifiers;

	public CustomItem(String id, Material material, String displayName, Rarity rarity,
					  ItemCategory itemCategory) {
		this(id, material, displayName, rarity, itemCategory, CustomItemStatModifiers.empty(), CustomItemStatCapModifiers.empty());
	}

	public CustomItem(String id, Material material, String displayName, Rarity rarity,
					  ItemCategory itemCategory, CustomItemStatModifiers statModifiers) {
		this(id, material, displayName, rarity, itemCategory, statModifiers, CustomItemStatCapModifiers.empty());
	}

	public CustomItem(String id, Material material, String displayName, Rarity rarity,
	                  ItemCategory itemCategory, CustomItemStatModifiers statModifiers,
	                  CustomItemStatCapModifiers statCapModifiers) {
		if (statModifiers == null) throw new IllegalArgumentException("statModifiers cannot be null");
		if (statCapModifiers == null) throw new IllegalArgumentException("statCapModifiers cannot be null");
		customAbilities = new ArrayList<>();

		this.id = id;
		this.material = material;
		this.displayName = displayName;
		this.rarity = rarity;
		this.itemCategory = itemCategory;
		this.statModifiers = statModifiers;
		this.statCapModifiers = statCapModifiers;
	}

	public void addAbility(CustomAbility customAbility) {
		customAbilities.add(customAbility);
	}

	public boolean hasAbility(CustomAbility customAbility) {
		return customAbilities.contains(customAbility);
	}
}
