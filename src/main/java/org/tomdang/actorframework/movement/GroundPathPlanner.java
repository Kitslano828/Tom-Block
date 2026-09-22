package org.tomdang.actorframework.movement;

import org.bukkit.World;
import org.tomdang.region.position.BlockPosition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/** Bounded A* over loaded, traversable feet blocks. Call only on the server thread. */
public final class GroundPathPlanner {
	private static final double DIAGONAL_COST = Math.sqrt(2);
	private static final double HEIGHT_COST = 0.2;
	private final GroundTraversalPolicy traversal;

	public GroundPathPlanner(GroundTraversalPolicy traversal) {
		if (traversal == null) throw new IllegalArgumentException("traversal cannot be null");
		this.traversal = traversal;
	}

	public GroundPathResult findPath(World world, BlockPosition start, BlockPosition goal, int maxExploredNodes) {
		if (world == null || start == null || goal == null)
			throw new IllegalArgumentException("World, start, and goal are required");
		if (maxExploredNodes < 1) throw new IllegalArgumentException("maxExploredNodes must be positive");
		if (!start.worldId().equals(world.getName()) || !goal.worldId().equals(world.getName()))
			throw new IllegalArgumentException("Start and goal must belong to the supplied world");
		if (!traversal.canStandAt(world, start)) return failure(GroundPathResult.Status.BLOCKED_START, 0);
		if (!traversal.canStandAt(world, goal)) return failure(GroundPathResult.Status.BLOCKED_GOAL, 0);
		if (start.equals(goal)) return new GroundPathResult(GroundPathResult.Status.FOUND, List.of(start), 0);

		PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingDouble(Node::estimatedTotal));
		Map<BlockPosition, Double> bestCost = new HashMap<>();
		Map<BlockPosition, BlockPosition> previous = new HashMap<>();
		Set<BlockPosition> explored = new HashSet<>();
		bestCost.put(start, 0.0);
		frontier.add(new Node(start, 0.0, heuristic(start, goal)));

		while (!frontier.isEmpty()) {
			Node current = frontier.poll();
			if (current.cost() > bestCost.getOrDefault(current.position(), Double.POSITIVE_INFINITY)) continue;
			if (explored.contains(current.position())) continue;
			if (explored.size() >= maxExploredNodes)
				return failure(GroundPathResult.Status.SEARCH_LIMIT_REACHED, explored.size());
			explored.add(current.position());
			if (current.position().equals(goal))
				return new GroundPathResult(GroundPathResult.Status.FOUND,
						reconstruct(goal, previous), explored.size());

			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					if (dx == 0 && dz == 0) continue;
					for (int dy = -1; dy <= 1; dy++) {
						BlockPosition next = new BlockPosition(world.getName(),
								current.position().x() + dx, current.position().y() + dy,
								current.position().z() + dz);
						if (explored.contains(next) || !traversal.canTransition(world, current.position(), next)) continue;
						double stepCost = (dx != 0 && dz != 0 ? DIAGONAL_COST : 1.0)
								+ Math.abs(dy) * HEIGHT_COST;
						double candidateCost = current.cost() + stepCost;
						if (candidateCost >= bestCost.getOrDefault(next, Double.POSITIVE_INFINITY)) continue;
						bestCost.put(next, candidateCost);
						previous.put(next, current.position());
						frontier.add(new Node(next, candidateCost, candidateCost + heuristic(next, goal)));
					}
				}
			}
		}
		return failure(GroundPathResult.Status.NO_ROUTE, explored.size());
	}

	private double heuristic(BlockPosition from, BlockPosition goal) {
		long dx = Math.abs((long) from.x() - goal.x());
		long dz = Math.abs((long) from.z() - goal.z());
		long dy = Math.abs((long) from.y() - goal.y());
		return Math.max(dx, dz) + (DIAGONAL_COST - 1) * Math.min(dx, dz) + HEIGHT_COST * dy;
	}

	private List<BlockPosition> reconstruct(BlockPosition goal, Map<BlockPosition, BlockPosition> previous) {
		List<BlockPosition> reversed = new ArrayList<>();
		for (BlockPosition position = goal; position != null; position = previous.get(position)) reversed.add(position);
		java.util.Collections.reverse(reversed);
		return reversed;
	}

	private GroundPathResult failure(GroundPathResult.Status status, int exploredNodes) {
		return new GroundPathResult(status, List.of(), exploredNodes);
	}

	private record Node(BlockPosition position, double cost, double estimatedTotal) { }
}
