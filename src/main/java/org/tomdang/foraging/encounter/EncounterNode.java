package org.tomdang.foraging.encounter;

import org.bukkit.Location;
import org.bukkit.World;

public record EncounterNode(int x, int y, int z) {
	public Location location(World world) { return new Location(world, x, y, z); }
	public String key(String world) { return world + ":" + x + ":" + y + ":" + z; }
}
