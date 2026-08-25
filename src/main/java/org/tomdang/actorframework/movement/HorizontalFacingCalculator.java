package org.tomdang.actorframework.movement;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class HorizontalFacingCalculator {

	public Location rotateLocation(Location rotatingLocation, Location endingLocation) {
		if (rotatingLocation == null) throw new IllegalArgumentException("rotatingLocation should not be null");
		if (endingLocation == null) throw new IllegalArgumentException("endingLocation should not be null");
		if (rotatingLocation.getWorld() == null) throw new IllegalArgumentException("rotatingLocation world should not be null");
		if (endingLocation.getWorld() == null) throw new IllegalArgumentException("endingLocation world should not be null");
		if (!rotatingLocation.getWorld().equals(endingLocation.getWorld())) throw new IllegalStateException("Not Matching worlds");

		double dx = endingLocation.getX() - rotatingLocation.getX();
		double dz = endingLocation.getZ() - rotatingLocation.getZ();

		// Check if X and Z displacement is effectively zero to avoid undefined yaw
		if (Math.abs(dx) < 1e-6 && Math.abs(dz) < 1e-6) {
			return rotatingLocation.clone();
		}

		Vector direction = new Vector(dx, 0.0, dz);

		// Stage 4: Apply the direction to a clone and return
		Location result = rotatingLocation.clone();
		result.setDirection(direction);

		return result;

	}

}
