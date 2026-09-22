package org.tomdang.actorframework.movement;

import org.tomdang.region.position.BlockPosition;

import java.util.List;

/** An exact route from starting feet block to destination feet block, or why none was returned. */
public record GroundPathResult(Status status, List<BlockPosition> path, int exploredNodes) {
	public enum Status {
		FOUND,
		BLOCKED_START,
		BLOCKED_GOAL,
		NO_ROUTE,
		SEARCH_LIMIT_REACHED
	}

	public GroundPathResult {
		if (status == null || path == null || exploredNodes < 0)
			throw new IllegalArgumentException("Path result fields are invalid");
		if ((status == Status.FOUND) != !path.isEmpty())
			throw new IllegalArgumentException("Only a found route may contain positions");
		path = List.copyOf(path);
	}
}
