package org.tomdang.mining.miningtool;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.tomdang.customabilityframework.CustomAbilityRegistry;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;

import java.util.*;

public class MiningToolRegistry {

	// Linking ID to the mining Tool
	private MiningToolCreator miningToolCreator;
	private CustomItemRegistry customItemRegistry;
	@Getter
	public Map<String, MiningTool> miningTools = new HashMap<>();
	ItemCategory miningToolCategory = ItemCategory.MINING_TOOL;

	public MiningToolRegistry(MiningToolCreator miningToolCreator, CustomItemRegistry customItemRegistry,
							  CustomAbilityRegistry customAbilityRegistry
	) {
		this.miningToolCreator = miningToolCreator;
		this.customItemRegistry = customItemRegistry;

		createNewMiningTool(Material.WOODEN_PICKAXE, 1, 15,3, "WOODEN_PICKAXE",
							Rarity.COMMON, "Wooden Pickaxe", miningToolCategory);
		createNewMiningTool(Material.STONE_PICKAXE, 1, 30,5, "STONE_PICKAXE",
							Rarity.COMMON, "Stone Pickaxe", miningToolCategory );
		createNewMiningTool(Material.GOLDEN_PICKAXE, 0, 60,6, "GOLDEN_PICKAXE",
							Rarity.COMMON, "Golden Pickaxe", miningToolCategory );
		createNewMiningTool(Material.IRON_PICKAXE, 2, 45,5, "IRON_PICKAXE",
							Rarity.COMMON, "Iron Pickaxe", miningToolCategory );
		createNewMiningTool(Material.DIAMOND_PICKAXE, 3, 60,8, "DIAMOND_PICKAXE",
							Rarity.COMMON, "Diamond Pickaxe", miningToolCategory);
		createNewMiningTool(Material.NETHERITE_PICKAXE, 3, 85,11, "NETHERITE_PICKAXE",
							Rarity.UNCOMMON, "Netherite Pickaxe", miningToolCategory );

		miningTools.get("NETHERITE_PICKAXE").addAbility(customAbilityRegistry.getCustomAbility("MINING_SPREAD_ABILITY"));
	}

	public void createNewMiningTool(Material material, int breakingPower, double miningSpeed,
	                                double fortune, String id, Rarity rarity, String displayName, ItemCategory itemCategory) {
		MiningTool miningTool;
		miningTool = new MiningTool(material,breakingPower,miningSpeed,fortune,id, rarity, displayName, itemCategory);
		addToolToRegistry(id, miningTool);
	}

	public ItemStack getMiningToolAsItem(MiningTool miningTool) {
		return miningToolCreator.createItemStack(miningTool);
	}

	private void addToolToRegistry(String id, MiningTool miningTool) {
		miningTools.put(id, miningTool);
		customItemRegistry.addItemToRegistry(miningTool);
	}

	public boolean toolInRegistry(String id) {
		return miningTools.containsKey(id);
	}

	public List<String> getToolsAsList() {
		return new ArrayList<>(miningTools.keySet());
	}

	public MiningTool getMiningTool(String id) {
		return miningTools.get(id);
	}
}
