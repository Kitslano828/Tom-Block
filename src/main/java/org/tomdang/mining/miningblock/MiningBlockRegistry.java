package org.tomdang.mining.miningblock;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MiningBlockRegistry {
	Map<Material, MiningBlock> miningBlocks = new HashMap<>();

	public MiningBlockRegistry() {

		// Registering the block itself
		createNewMiningBlock(Material.STONE, 0, 0, 1,5);
		createNewMiningBlock(Material.COAL_ORE, 0,1,3,5);
		createNewMiningBlock(Material.IRON_ORE, 0,1,5,7);
		createNewMiningBlock(Material.DIAMOND_ORE, 0,2,8,10);
		createNewMiningBlock(Material.IRON_BLOCK,0, 3, 12,15);

		// Adding Drops

		addDropToMiningBlock(miningBlocks.get(Material.STONE), ItemStack.of(Material.COBBLESTONE), 1, 100,true);
		addDropToMiningBlock(miningBlocks.get(Material.STONE), ItemStack.of(Material.GRAY_DYE), 1, 0.01,false);

		addDropToMiningBlock(miningBlocks.get(Material.COAL_ORE), ItemStack.of(Material.COAL), 1, 100, true);
		addDropToMiningBlock(miningBlocks.get(Material.COAL_ORE), ItemStack.of(Material.BLACK_DYE), 1, 0.01,false);

		addDropToMiningBlock(miningBlocks.get(Material.IRON_ORE), ItemStack.of(Material.RAW_IRON), 1, 100,true);
		addDropToMiningBlock(miningBlocks.get(Material.IRON_ORE), ItemStack.of(Material.LIGHT_GRAY_DYE), 1, 0.01, false);

		addDropToMiningBlock(miningBlocks.get(Material.DIAMOND_ORE), ItemStack.of(Material.DIAMOND), 1, 100,true);
		addDropToMiningBlock(miningBlocks.get(Material.DIAMOND_ORE), ItemStack.of(Material.LIGHT_BLUE_DYE), 1, 0.01,false);

		addDropToMiningBlock(miningBlocks.get(Material.IRON_BLOCK), ItemStack.of(Material.RAW_IRON), 9, 100,true);
		addDropToMiningBlock(miningBlocks.get(Material.IRON_BLOCK), ItemStack.of(Material.IRON_BLOCK), 1, 5, false);
		addDropToMiningBlock(miningBlocks.get(Material.IRON_BLOCK), ItemStack.of(Material.WHITE_DYE), 1, 0.01, false);


	}

	public void createNewMiningBlock(Material material, int blockStrength, int breakingPower, int xp, long regenerationTime) {
		MiningBlock miningBlock = new MiningBlock(blockStrength,breakingPower,material, xp, regenerationTime);
		addBlockToRegistry(material, miningBlock);
	}

	public void addDropToMiningBlock(MiningBlock miningBlock, ItemStack item, int amount, double chance, boolean isAffectedByFortune) {
		miningBlock.addBlockDrops(item, amount, chance, isAffectedByFortune);
	}

	private void addBlockToRegistry(Material material, MiningBlock block) {
		miningBlocks.put(material, block);
	}

	public boolean blockInRegistry(Material block) {
		return miningBlocks.containsKey(block);
	}

	public MiningBlock getMiningBlock(Material block) {
		return miningBlocks.get(block);
	}
}
