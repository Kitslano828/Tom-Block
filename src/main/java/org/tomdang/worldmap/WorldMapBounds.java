package org.tomdang.worldmap;

/** The world coordinates represented by the four edges of the source PNG. */
public record WorldMapBounds(int minimumX, int minimumZ, int maximumX, int maximumZ) {
	public WorldMapBounds {
		if (maximumX <= minimumX || maximumZ <= minimumZ)
			throw new IllegalArgumentException("Map bounds must have positive X and Z spans");
	}

	public double pixelX(double x, int imageWidth) {
		return (x - minimumX) * imageWidth / (maximumX - minimumX + 1.0);
	}

	public double pixelY(double z, int imageHeight) {
		return (z - minimumZ) * imageHeight / (maximumZ - minimumZ + 1.0);
	}
}
