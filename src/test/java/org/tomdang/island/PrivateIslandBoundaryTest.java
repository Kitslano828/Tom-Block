package org.tomdang.island;

import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrivateIslandBoundaryTest {
	@Test void circularBoundaryIncludesRadiusAndRejectsBeyondIt() {
		assertTrue(PrivateIslandWorldListener.contains(new Location(null, 0, 64, 0), 192));
		assertTrue(PrivateIslandWorldListener.contains(new Location(null, 192, 64, 0), 192));
		assertFalse(PrivateIslandWorldListener.contains(new Location(null, 193, 64, 0), 192));
		assertFalse(PrivateIslandWorldListener.contains(new Location(null, 140, 64, 140), 192));
	}
}
