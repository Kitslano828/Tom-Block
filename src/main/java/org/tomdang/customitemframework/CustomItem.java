package org.tomdang.customitemframework;

import lombok.Getter;
import org.bukkit.Material;
import org.tomdang.customabilityframework.customability.CustomAbility;

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

	public CustomItem(String id, Material material, String displayName, Rarity rarity,
					  ItemCategory itemCategory) {
		customAbilities = new ArrayList<>();

		this.id = id;
		this.material = material;
		this.displayName = displayName;
		this.rarity = rarity;
		this.itemCategory = itemCategory;
	}

	public void addAbility(CustomAbility customAbility) {
		customAbilities.add(customAbility);
	}

	public boolean hasAbility(CustomAbility customAbility) {
		return customAbilities.contains(customAbility);
	}
}
