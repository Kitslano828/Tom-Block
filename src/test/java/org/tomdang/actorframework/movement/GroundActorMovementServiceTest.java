package org.tomdang.actorframework.movement;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.presentation.ActorPresentationService;
import org.tomdang.region.position.BlockPosition;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GroundActorMovementServiceTest {
	private final World world = mock(World.class);
	private final ActorInstance actor = mock(ActorInstance.class);
	private final ActorPresentationService presentation = mock(ActorPresentationService.class);
	private final ActorMovementTaskService tasks = mock(ActorMovementTaskService.class);
	private final GroundPathPlanner planner = mock(GroundPathPlanner.class);
	private final GroundTraversalPolicy traversal = mock(GroundTraversalPolicy.class);
	private final GroundActorMovementService service = new GroundActorMovementService(
			presentation, tasks, planner, traversal, new LinearMovementStepCalculator());

	@Test
	void followsWaypointsAndFinishesAtRequestedLocation() {
		setup();
		AtomicReference<Location> current = new AtomicReference<>(new Location(world, 0.5, 64, 0.5));
		when(presentation.getPresentationLocation(actor)).thenAnswer(call -> current.get().clone());
		doAnswer(call -> { current.set(call.getArgument(1)); return null; })
				.when(presentation).movePresentation(eq(actor), any(Location.class), anyBoolean());
		when(planner.findPath(eq(world), any(), any(), anyInt())).thenReturn(found());
		when(traversal.canTransition(eq(world), any(), any())).thenReturn(true);
		when(traversal.canStandAt(eq(world), any())).thenReturn(true);
		Location destination = new Location(world, 2.2, 64, 0.7);
		try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
			assertEquals(GroundPathResult.Status.FOUND, service.moveTo(actor, destination, 0.25).status());
		}
		var tick = org.mockito.ArgumentCaptor.forClass(ActorMovementTick.class);
		verify(tasks).start(eq(actor.getInstanceID()), tick.capture());
		boolean done = false;
		for (int i = 0; i < 30 && !done; i++) done = tick.getValue().tick();
		assertTrue(done);
		assertEquals(destination, current.get());
		verify(presentation).movePresentation(actor, destination, false);
	}

	@Test
	void failedPlanDoesNotReplaceExistingMovement() {
		setup();
		when(presentation.getPresentationLocation(actor)).thenReturn(new Location(world, 0.5, 64, 0.5));
		when(traversal.canStandAt(eq(world), any())).thenReturn(true);
		when(planner.findPath(eq(world), any(), any(), anyInt())).thenReturn(
				new GroundPathResult(GroundPathResult.Status.BLOCKED_GOAL, List.of(), 0));
		try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
			assertEquals(GroundPathResult.Status.BLOCKED_GOAL,
					service.moveTo(actor, new Location(world, 2.5, 64, 0.5), 0.2).status());
		}
		verifyNoInteractions(tasks);
	}

	@Test
	void stopsAtCurrentLocationWhenWaypointBecomesBlocked() {
		setup();
		Location start = new Location(world, 0.5, 64, 0.5);
		when(presentation.getPresentationLocation(actor)).thenReturn(start);
		when(planner.findPath(eq(world), any(), any(), anyInt())).thenReturn(found(),
				new GroundPathResult(GroundPathResult.Status.NO_ROUTE, List.of(), 2));
		when(traversal.canStandAt(eq(world), any())).thenReturn(true, false, true);
		try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
			assertEquals(GroundPathResult.Status.FOUND,
					service.moveTo(actor, new Location(world, 2.2, 64, 0.7), 0.25).status());
		}
		var tick = org.mockito.ArgumentCaptor.forClass(ActorMovementTick.class);
		verify(tasks).start(any(), tick.capture());
		assertTrue(tick.getValue().tick());
		verify(presentation).movePresentation(actor, start, false);
		verify(planner, times(2)).findPath(eq(world), any(), any(), anyInt());
	}

	@Test
	void recoversAnInterruptedUpStepWithoutTreatingVisualYAsGround() {
		setup();
		Location stranded = new Location(world, 1.2, 64.4, 0.5);
		when(presentation.getPresentationLocation(actor)).thenReturn(stranded);
		when(traversal.canStandAt(eq(world), any())).thenAnswer(call ->
				((BlockPosition) call.getArgument(1)).y() == 65);
		when(planner.findPath(eq(world), any(), any(), anyInt())).thenReturn(
				new GroundPathResult(GroundPathResult.Status.FOUND,
						List.of(new BlockPosition("world", 1, 65, 0)), 0));
		try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
			assertEquals(GroundPathResult.Status.FOUND,
					service.moveTo(actor, new Location(world, 1.5, 65, 0.5), 0.2).status());
		}
		verify(planner).findPath(eq(world), eq(new BlockPosition("world", 1, 65, 0)), any(), anyInt());
		verify(presentation).movePresentation(eq(actor), argThat(location -> location.getY() == 65), eq(false));
	}

	@Test
	void risingBetweenBlocksDoesNotReplanFromFractionalVisualPosition() {
		setup();
		AtomicReference<Location> current = new AtomicReference<>(new Location(world, 0.5, 64, 0.5));
		when(presentation.getPresentationLocation(actor)).thenAnswer(call -> current.get().clone());
		doAnswer(call -> { current.set(call.getArgument(1)); return null; })
				.when(presentation).movePresentation(eq(actor), any(Location.class), anyBoolean());
		when(traversal.canStandAt(eq(world), any())).thenReturn(true);
		when(traversal.canTransition(eq(world), any(), any())).thenReturn(true);
		when(planner.findPath(eq(world), any(), any(), anyInt())).thenReturn(
				new GroundPathResult(GroundPathResult.Status.FOUND, List.of(
						new BlockPosition("world", 0, 64, 0), new BlockPosition("world", 1, 65, 0)), 2));
		try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
			service.moveTo(actor, new Location(world, 1.5, 65, 0.5), 0.2);
		}
		var tick = org.mockito.ArgumentCaptor.forClass(ActorMovementTick.class);
		verify(tasks).start(any(), tick.capture());
		for (int i = 0; i < 4; i++) assertFalse(tick.getValue().tick());
		assertEquals(1, current.get().getBlockX());
		assertEquals(64, current.get().getBlockY());
		verify(planner, times(1)).findPath(eq(world), any(), any(), anyInt());
	}

	private void setup() {
		when(world.getName()).thenReturn("world");
		when(actor.getInstanceID()).thenReturn(UUID.randomUUID());
	}

	private GroundPathResult found() {
		return new GroundPathResult(GroundPathResult.Status.FOUND, List.of(
				new BlockPosition("world", 0, 64, 0),
				new BlockPosition("world", 0, 64, 1),
				new BlockPosition("world", 1, 64, 1),
				new BlockPosition("world", 2, 64, 0)), 4);
	}
}
