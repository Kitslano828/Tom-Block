package org.tomdang.foraging;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.tomdang.player.counter.CounterKey;

public record TreeModel(String id, Material logMaterial, Material leafMaterial,
		List<BlockOffset> logs, Set<BlockOffset> leaves, int tier, double durability,
		double requiredPower, long xp, int regenerationSeconds, CounterKey collectionKey) {
	public TreeModel {
		logs = List.copyOf(logs);
		leaves = Set.copyOf(leaves);
		if (tier < 1 || durability <= 0 || requiredPower < 0 || xp < 0 || regenerationSeconds < 0 || collectionKey == null)
			throw new IllegalArgumentException("Invalid tree progression settings");
	}

	public static TreeModel modelOak() {
		List<BlockOffset> logs = List.of(
				new BlockOffset(0, 0, 0), new BlockOffset(0, 1, 0), new BlockOffset(0, 2, 0),
				new BlockOffset(0, 3, 0), new BlockOffset(0, 4, 0), new BlockOffset(0, 5, 0),
				new BlockOffset(1, 4, 0), new BlockOffset(2, 4, 0), new BlockOffset(-1, 4, 0),
				new BlockOffset(-2, 4, 0), new BlockOffset(0, 4, 1), new BlockOffset(0, 4, 2),
				new BlockOffset(0, 4, -1), new BlockOffset(0, 4, -2));
		Set<BlockOffset> leaves = new LinkedHashSet<>();
		for (int y = 3; y <= 5; y++) {
			int radius = y == 5 ? 1 : 2;
			for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
				BlockOffset offset = new BlockOffset(x, y, z);
				if (!logs.contains(offset) && !(radius == 2 && Math.abs(x) == 2 && Math.abs(z) == 2)) leaves.add(offset);
			}
		}
		leaves.add(new BlockOffset(0, 6, 0));
		leaves.add(new BlockOffset(1, 6, 0));
		leaves.add(new BlockOffset(-1, 6, 0));
		leaves.add(new BlockOffset(0, 6, 1));
		leaves.add(new BlockOffset(0, 6, -1));
		return new TreeModel("MODEL_OAK", Material.OAK_LOG, Material.OAK_LEAVES, logs, leaves,
				1, 100.0, 10.0, 70L, 30, CounterKey.of("FORAGING:OAK_LOGS_BROKEN"));
	}
}
