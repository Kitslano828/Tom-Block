package org.tomdang.foraging;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Produces a connected, near-to-far break wave beginning at the struck log. */
public final class TreeBreakPlanner {
	public List<BlockOffset> plan(List<BlockOffset> logs, BlockOffset struck) {
		if (!logs.contains(struck)) throw new IllegalArgumentException("struck block is not part of the tree");
		Set<BlockOffset> remaining = new HashSet<>(logs);
		ArrayDeque<BlockOffset> queue = new ArrayDeque<>();
		List<BlockOffset> result = new ArrayList<>(logs.size());
		queue.add(struck);
		remaining.remove(struck);
		while (!queue.isEmpty()) {
			BlockOffset current = queue.remove();
			result.add(current);
			remaining.stream().filter(candidate -> candidate.distance(current) == 1)
					.sorted((a, b) -> Integer.compare(a.y(), b.y())).toList().forEach(next -> {
						if (remaining.remove(next)) queue.add(next);
					});
		}
		// Decorative diagonal branches may not be face-connected; finish them nearest-first.
		remaining.stream().sorted((a, b) -> {
			int distance = Integer.compare(a.distance(struck), b.distance(struck));
			return distance != 0 ? distance : Integer.compare(a.y(), b.y());
		}).forEach(result::add);
		return result;
	}
}
