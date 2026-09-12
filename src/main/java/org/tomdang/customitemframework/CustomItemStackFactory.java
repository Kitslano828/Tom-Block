package org.tomdang.customitemframework;

import org.bukkit.inventory.ItemStack;
import org.tomdang.combat.weapons.Weapon;
import org.tomdang.combat.weapons.WeaponCreator;
import org.tomdang.customarmorframework.CustomArmor;
import org.tomdang.customarmorframework.CustomArmorCreator;
import org.tomdang.mining.miningtool.MiningTool;
import org.tomdang.mining.miningtool.MiningToolCreator;

public class CustomItemStackFactory {

	private final CustomItemCreator customItemCreator;
	private final WeaponCreator weaponCreator;
	private final MiningToolCreator miningToolCreator;
	private final CustomArmorCreator customArmorCreator;

	public CustomItemStackFactory(CustomItemCreator customItemCreator, WeaponCreator weaponCreator, MiningToolCreator miningToolCreator,
								  CustomArmorCreator customArmorCreator
	) {
		this.customItemCreator = customItemCreator;
		this.weaponCreator = weaponCreator;
		this.miningToolCreator = miningToolCreator;
		this.customArmorCreator = customArmorCreator;
	}

	public ItemStack createCustomItemStack(CustomItem item) {
		if (item == null) throw new IllegalArgumentException("Item cannot be null");
		return switch (item) {
			case MiningTool miningTool -> miningToolCreator.createItemStack(miningTool);
			case Weapon weapon -> weaponCreator.createItemStack(weapon);
			case CustomArmor customArmor -> customArmorCreator.createItemStack(customArmor);
			default -> customItemCreator.createItemStack(item);
		};
	}

	public ItemStack createCustomItemStack(CustomItem item, int amount) {
		if (item == null) throw new IllegalArgumentException("Item cannot be null");
		if (amount < 1) throw new IllegalArgumentException("Amount needs to be greater than 0");
		ItemStack stack = createCustomItemStack(item);
		stack.setAmount(amount);
		return stack;
	}

}
