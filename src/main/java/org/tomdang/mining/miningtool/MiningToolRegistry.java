package org.tomdang.mining.miningtool;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.tomdang.customitemframework.CustomItemRegistry;

import java.util.*;

public class MiningToolRegistry {

	// Linking ID to the mining Tool
	private final MiningToolCreator miningToolCreator;
	private final CustomItemRegistry customItemRegistry;
	@Getter
	private final Map<String, MiningTool> miningTools = new HashMap<>();

	public MiningToolRegistry(MiningToolCreator miningToolCreator, CustomItemRegistry customItemRegistry) {
		this.miningToolCreator = miningToolCreator;
		this.customItemRegistry = customItemRegistry;
	}

	public ItemStack getMiningToolAsItem(MiningTool miningTool) {
		return miningToolCreator.createItemStack(miningTool);
	}

	public void addToolToRegistry(String id, MiningTool miningTool) {
		if (containsMiningTool(id)) throw new IllegalStateException(id + " already exists in registry");
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

	public boolean containsMiningTool(String id) {
		if (id == null || id.isBlank()) throw new IllegalArgumentException("id cannot be null or blank");
		return miningTools.containsKey(id);
	}
}
