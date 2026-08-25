package org.tomdang.player.playerresource;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlayerResourceTest {

	@Test
	void newResourceStartsEmptyWithDefaultMaximum() {
		PlayerResource resource = new PlayerResource();

		assertEquals(0.0, resource.getCurrent(), 0.000001);
		assertEquals(100, resource.getMaximum(), 0.000001);
	}

	@Test
	void addingPastMaximumCapsAtMaximum() {
		PlayerResource resource = new PlayerResource();
		resource.addCurrent(200.0, 100.0);

		assertEquals(100, resource.getCurrent(), 0.000001);
	}

	@Test
	void removingPastZeroStopsAtZero() {
		PlayerResource resource = new PlayerResource();
		resource.addCurrent(50.0, 100.0);
		resource.removeCurrent(75.0);

		assertEquals(0.0, resource.getCurrent(), 0.000001);
	}

	@Test
	void settingCurrentAboveBaseMaximumIsAllowed() {
		PlayerResource resource = new PlayerResource();
		resource.setCurrent(150.0);

		assertEquals(150.0, resource.getCurrent(), 0.000001);
	}

	@Test
	void addingNegativeAmountIsRejected() {
		PlayerResource resource = new PlayerResource();
		assertThrows(
				IllegalArgumentException.class,
				() -> resource.addCurrent(-10.0, 100.0)
		);
	}

	@Test
	void removingNegativeAmountIsRejected() {
		PlayerResource resource = new PlayerResource();
		assertThrows(
				IllegalArgumentException.class,
				() -> resource.removeCurrent(-10.0)
		);
	}

	@Test
	void settingNegativeMaximumIsRejected() {
		PlayerResource resource = new PlayerResource();
		assertThrows(
				IllegalArgumentException.class,
				() -> resource.setMaximum(-15.0));
	}

	@Test
	void restoringToNegativeEffectiveMaximumIsRejected() {
		PlayerResource resource = new PlayerResource();
		assertThrows(
				IllegalArgumentException.class,
				() -> resource.restoreFull(-10));
	}

	@Test
	void addingWithNegativeEffectiveMaximumIsRejected() {
		PlayerResource resource = new PlayerResource();
		assertThrows(
				IllegalArgumentException.class,
				() -> resource.addCurrent(10.0, -5.0));
	}

}