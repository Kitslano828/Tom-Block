package org.tomdang.foraging;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Collection;
import java.util.List;

public final class ForagingTreeRegistry {
	private final Map<String, ForagingTree> trees = new HashMap<>();
	private final Map<String, ForagingTree> logs = new HashMap<>();

	public void register(ForagingTree tree) {
		if (trees.containsKey(tree.id())) throw new IllegalArgumentException("Tree id already exists: " + tree.id());
		for (BlockOffset offset : tree.model().logs()) {
			String key = key(tree.location(offset));
			if (logs.containsKey(key)) throw new IllegalArgumentException("Tree overlaps an existing registered tree");
		}
		trees.put(tree.id(), tree);
		for (BlockOffset offset : tree.model().logs()) logs.put(key(tree.location(offset)), tree);
	}

	public Optional<ForagingTree> atLog(Location location) { return Optional.ofNullable(logs.get(key(location))); }
	public Optional<ForagingTree> find(String id) { return Optional.ofNullable(trees.get(id)); }
	public Collection<ForagingTree> all() { return List.copyOf(trees.values()); }
	public boolean remove(String id) {
		ForagingTree removed = trees.remove(id);
		if (removed == null) return false;
		removed.model().logs().forEach(offset -> logs.remove(key(removed.location(offset))));
		return true;
	}

	private static String key(Location location) {
		return location.getWorld().getUID() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
	}
}
