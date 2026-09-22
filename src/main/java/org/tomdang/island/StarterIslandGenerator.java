package org.tomdang.island;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/** Deterministic starter preset: a compact stranded island surrounded by a deep, continuous ocean. */
public final class StarterIslandGenerator extends ChunkGenerator {
	@Override public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random,
			int chunkX, int chunkZ, @NotNull ChunkData data) {
		for (int localX = 0; localX < 16; localX++) for (int localZ = 0; localZ < 16; localZ++) {
			int x = chunkX * 16 + localX;
			int z = chunkZ * 16 + localZ;
			double distance = Math.sqrt(x * x + z * z);
			int oceanFloor = 45 + floorVariation(x, z);
			int surface;
			if (distance <= 16) {
				surface = 65 - Math.max(0, (int) ((distance - 10) / 3));
			} else if (distance < 34) {
				double blend = (distance - 16) / 18.0;
				surface = (int) Math.round(63 * (1.0 - blend) + oceanFloor * blend);
			} else {
				surface = oceanFloor;
			}
			data.setBlock(localX, data.getMinHeight(), localZ, Material.BEDROCK);
			for (int y = data.getMinHeight() + 1; y <= surface - 4; y++) data.setBlock(localX, y, localZ, Material.STONE);
			Material subsurface = distance < 19 ? Material.DIRT : Material.SANDSTONE;
			for (int y = surface - 3; y < surface; y++) data.setBlock(localX, y, localZ, subsurface);
			Material top = surface >= 64 ? Material.GRASS_BLOCK : (distance < 24 ? Material.SAND : Material.GRAVEL);
			data.setBlock(localX, surface, localZ, top);
			for (int y = surface + 1; y <= 63; y++) data.setBlock(localX, y, localZ, Material.WATER);
		}
	}

	private int floorVariation(int x, int z) {
		long value = x * 341873128712L + z * 132897987541L;
		value ^= value >>> 13;
		return (int) Math.floorMod(value, 3) - 1;
	}
}
