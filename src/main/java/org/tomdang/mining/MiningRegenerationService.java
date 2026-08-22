package org.tomdang.mining;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.tomdang.TomBlock;
import org.tomdang.mining.regeneratingblock.RegeneratingBlock;

public class MiningRegenerationService {

	private final TomBlock instance;

	public MiningRegenerationService(TomBlock instance) {
		this.instance = instance;
	}

	public void scheduleRegeneration(RegeneratingBlock regeneratingBlock, Block block) {

		block.setType(Material.BEDROCK);

		Bukkit.getScheduler().runTaskLater(instance, () -> {
			block.setType(regeneratingBlock.getOriginalMaterial());
		}, regeneratingBlock.getRespawnDelayInTicks());
	}
}
