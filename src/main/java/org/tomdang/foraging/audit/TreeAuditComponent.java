package org.tomdang.foraging.audit;

import org.bukkit.Location;

public record TreeAuditComponent(int id, TreeAuditClassification classification,
		int minimumX, int minimumY, int minimumZ, int maximumX, int maximumY, int maximumZ) {

	public boolean isWithin(Location location, int radius) {
		double nearestX = Math.max(minimumX, Math.min(maximumX, location.getX()));
		double nearestY = Math.max(minimumY, Math.min(maximumY, location.getY()));
		double nearestZ = Math.max(minimumZ, Math.min(maximumZ, location.getZ()));
		double dx = nearestX - location.getX();
		double dy = nearestY - location.getY();
		double dz = nearestZ - location.getZ();
		return dx * dx + dy * dy + dz * dz <= radius * radius;
	}
}
