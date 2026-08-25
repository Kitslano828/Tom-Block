package org.tomdang.actorframework.movement;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class LinearMovementStepCalculatorTest {

	@Test
	void movesByMaximumDistanceTowardDestination() {
		World world = mock(World.class);
		Location currentLocation = new Location(world, 0, 0, 0);
		Location destination = new Location(world, 10, 0, 0);
		double maxDistance = 2.0;

		LinearMovementStepCalculator linearMovementStepCalculator = new LinearMovementStepCalculator();
		Location next = linearMovementStepCalculator.calculateNextLocation(currentLocation, destination, maxDistance);

		assertEquals(2.0, next.getX(), 0.000001);
		assertEquals(0.0, next.getY(), 0.000001);
		assertEquals(0.0, next.getZ(), 0.000001);

		// Verify currentLocation remains unchanged (0, 0, 0)
		assertEquals(0.0, currentLocation.getX(), 0.000001);
		assertEquals(0.0, currentLocation.getY(), 0.000001);
		assertEquals(0.0, currentLocation.getZ(), 0.000001);

		// Verify destination remains unchanged (10, 0, 0)
		assertEquals(10.0, destination.getX(), 0.000001);
		assertEquals(0.0, destination.getY(), 0.000001);
		assertEquals(0.0, destination.getZ(), 0.000001);
	}

	@Test
	void movesCorrectDistanceAlongDiagonal() {
		World world = mock(World.class);
		Location currentLocation = new Location(world, 0, 0, 0);
		Location destination = new Location(world, 3, 4, 0);
		double maxDistance = 1.0;

		LinearMovementStepCalculator linearMovementStepCalculator = new LinearMovementStepCalculator();
		Location next = linearMovementStepCalculator.calculateNextLocation(currentLocation, destination, maxDistance);

		assertEquals(0.6, next.getX(), 0.000001);
		assertEquals(0.8, next.getY(), 0.000001);
		assertEquals(0.0, next.getZ(), 0.000001);

		assertEquals(
				1.0,
				currentLocation.distance(next),
				0.000001
		);
	}

	@Test
	void doesNotOvershootNearbyDestination() {
		World world = mock(World.class);
		Location currentLocation = new Location(world, 0, 0, 0);
		Location destination = new Location(world, 1, 0, 0);
		double maxDistance = 2.0;

		LinearMovementStepCalculator linearMovementStepCalculator = new LinearMovementStepCalculator();
		Location next = linearMovementStepCalculator.calculateNextLocation(currentLocation, destination, maxDistance);

		assertEquals(1.0, next.getX(), 0.000001);
		assertEquals(0.0, next.getY(), 0.000001);
		assertEquals(0.0, next.getZ(), 0.000001);

		assertNotSame(destination, next);
	}

	@Test
	void locationInsideToleranceIsReached() {
		World world = mock(World.class);
		Location currentLocation = new Location(world, 0, 0, 0);
		Location destination = new Location(world, 0.0005, 0, 0);

		LinearMovementStepCalculator linearMovementStepCalculator = new LinearMovementStepCalculator();

		assertTrue(linearMovementStepCalculator.hasReachedDestination(currentLocation, destination));
	}

	@Test
	void locationOutsideToleranceIsNotReached() {
		World world = mock(World.class);
		Location currentLocation = new Location(world, 0, 0, 0);
		Location destination = new Location(world, 0.002, 0, 0);

		LinearMovementStepCalculator linearMovementStepCalculator = new LinearMovementStepCalculator();

		assertFalse(linearMovementStepCalculator.hasReachedDestination(currentLocation, destination));
	}

}
