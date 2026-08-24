package org.tomdang.actorframework.movement;

import org.bukkit.Location;
import org.bukkit.util.Vector;


public class LinearMovementStepCalculator {

	public Location calculateNextLocation(Location currentLocation, Location destinationLocation, double maxDistance) {
		if (currentLocation == null) throw new IllegalArgumentException("current location cannot be null");
		if (currentLocation.getWorld() == null) throw new IllegalArgumentException("current location world cannot be null");
		if (destinationLocation == null) throw new IllegalArgumentException("destinationLocation cannot be null");
		if (destinationLocation.getWorld() == null) throw new IllegalArgumentException("destinationLocation world cannot be null");
		if (!currentLocation.getWorld().equals(destinationLocation.getWorld())) throw new IllegalArgumentException("Worlds arent matching");
		if (!Double.isFinite(maxDistance)) throw new IllegalArgumentException("Max distance cannot be nan or infinite");
		if (maxDistance <= 0) throw new IllegalArgumentException("Max distance must be greater than zero");

		double distance = currentLocation.distance(destinationLocation);

		if (distance <= maxDistance) return destinationLocation.clone();

		// Get the displacement vector by subtracting components manually to avoid altering caller vectors
		Vector displacement = new Vector(
				destinationLocation.getX() - currentLocation.getX(),
				destinationLocation.getY() - currentLocation.getY(),
				destinationLocation.getZ() - currentLocation.getZ()
		);

		Vector stepVector = displacement.normalize().multiply(maxDistance);

		Location nextLocation = currentLocation.clone();
		nextLocation.add(stepVector);

		return nextLocation;
	}

	public boolean hasReachedDestination(Location currentLocation, Location destinationLocation) {
		if (currentLocation == null) throw new IllegalArgumentException("currentLocation cannot be null");
		if (currentLocation.getWorld() == null) throw new IllegalArgumentException("currentLocation world cannot be null");
		if (destinationLocation == null) throw new IllegalArgumentException("destinationLocation cannot be null");
		if (destinationLocation.getWorld() == null) throw new IllegalArgumentException("destinationLocation world cannot be null");

		// 3. Reject different worlds
		if (!currentLocation.getWorld().equals(destinationLocation.getWorld())) {
			throw new IllegalArgumentException("Worlds arent matching");
		}

		double toleranceSquared = 0.000001;

		return currentLocation.distanceSquared(destinationLocation) <= toleranceSquared;


	}

}
