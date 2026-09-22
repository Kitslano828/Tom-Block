package org.tomdang.actorframework.movement;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.presentation.ActorPresentationService;
import org.tomdang.region.position.BlockPosition;

import java.util.List;

/** Opt-in, ground-bound movement; other actor movement modes remain unchanged. */
public final class GroundActorMovementService {
	private static final int SEARCH_BUDGET = 400;
	private final ActorPresentationService presentation;
	private final ActorMovementTaskService tasks;
	private final GroundPathPlanner planner;
	private final GroundTraversalPolicy traversal;
	private final LinearMovementStepCalculator steps;

	public GroundActorMovementService(ActorPresentationService presentation, ActorMovementTaskService tasks,
	                                  GroundPathPlanner planner, GroundTraversalPolicy traversal,
	                                  LinearMovementStepCalculator steps) {
		if (presentation == null || tasks == null || planner == null || traversal == null || steps == null)
			throw new IllegalArgumentException("Ground movement dependencies are required");
		this.presentation = presentation;
		this.tasks = tasks;
		this.planner = planner;
		this.traversal = traversal;
		this.steps = steps;
	}

	/** Returns the planning outcome. A failed request does not cancel existing movement. */
	public GroundPathResult moveTo(ActorInstance actor, Location destination, double distancePerTick) {
		if (actor == null || destination == null || destination.getWorld() == null)
			throw new IllegalArgumentException("Actor and destination with a world are required");
		if (!Double.isFinite(distancePerTick) || distancePerTick <= 0)
			throw new IllegalArgumentException("distancePerTick must be positive and finite");
		if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Ground movement requires the server thread");
		Location current = presentation.getPresentationLocation(actor);
		World world = current.getWorld();
		if (world == null || !world.equals(destination.getWorld()))
			throw new IllegalArgumentException("Actor and destination must be in the same world");
		Location target = destination.clone();
		BlockPosition start = recoverStart(world, feet(current));
		if (start == null)
			return new GroundPathResult(GroundPathResult.Status.BLOCKED_START, List.of(), 0);
		GroundPathResult route = planner.findPath(world, start, feet(target), SEARCH_BUDGET);
		if (route.status() != GroundPathResult.Status.FOUND) return route;
		if (current.getBlockY() != start.y()) {
			current.setY(start.y());
			presentation.movePresentation(actor, current, false);
		}
		if (steps.hasReachedDestination(current, target)) {
			tasks.cancel(actor.getInstanceID());
			presentation.movePresentation(actor, target, false);
			return route;
		}
		tasks.start(actor.getInstanceID(), new RouteTick(actor, target, distancePerTick, route.path()));
		return route;
	}

	private BlockPosition feet(Location location) {
		return new BlockPosition(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	private BlockPosition recoverStart(World world, BlockPosition observed) {
		if (traversal.canStandAt(world, observed)) return observed;
		// A previous interrupted step can leave a packet NPC in the block immediately
		// below or above a valid feet position. Never search farther or load chunks.
		for (int offset : new int[]{1, -1}) {
			BlockPosition candidate = new BlockPosition(observed.worldId(), observed.x(), observed.y() + offset, observed.z());
			if (traversal.canStandAt(world, candidate)) return candidate;
		}
		return null;
	}

	private final class RouteTick implements ActorMovementTick {
		private final ActorInstance actor;
		private final Location destination;
		private final double distancePerTick;
		private List<BlockPosition> route;
		private int waypointIndex;
		private int replans;

		private RouteTick(ActorInstance actor, Location destination, double distancePerTick, List<BlockPosition> route) {
			this.actor = actor;
			this.destination = destination;
			this.distancePerTick = distancePerTick;
			this.route = route;
			this.waypointIndex = route.size() > 1 ? 1 : route.size();
		}

		@Override
		public boolean tick() {
			Location current = presentation.getPresentationLocation(actor);
			World world = current.getWorld();
			if (world == null || !world.equals(destination.getWorld())) return stop(current);
			if (waypointIndex < route.size()) {
				BlockPosition from = route.get(waypointIndex - 1);
				BlockPosition next = route.get(waypointIndex);
				if (!traversal.canStandAt(world, next)
						|| !traversal.canTransition(world, from, next)) {
					if (++replans > 2) return stop(current);
					BlockPosition restart = recoverStart(world, feet(current));
					if (restart == null) return stop(current);
					GroundPathResult replacement = planner.findPath(world, restart, feet(destination), SEARCH_BUDGET);
					if (replacement.status() != GroundPathResult.Status.FOUND) return stop(current);
					route = replacement.path();
					waypointIndex = route.size() > 1 ? 1 : route.size();
					return false;
				}
				Location waypoint = new Location(world, next.x() + 0.5, next.y(), next.z() + 0.5);
				Location moved = steps.calculateNextLocation(current, waypoint, distancePerTick);
				if (steps.hasReachedDestination(moved, waypoint)) waypointIndex++;
				presentation.movePresentation(actor, moved, true);
				return false;
			}
			// Final positioning stays within the validated destination feet block.
			if (!traversal.canStandAt(world, feet(destination))) return stop(current);
			Location moved = steps.calculateNextLocation(current, destination, distancePerTick);
			boolean arrived = steps.hasReachedDestination(moved, destination);
			presentation.movePresentation(actor, moved, !arrived);
			return arrived;
		}

		private boolean stop(Location current) {
			presentation.movePresentation(actor, current, false);
			return true;
		}
	}
}
