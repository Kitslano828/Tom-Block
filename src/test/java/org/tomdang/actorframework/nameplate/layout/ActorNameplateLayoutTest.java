package org.tomdang.actorframework.nameplate.layout;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActorNameplateLayoutTest {

	@Test
	void storesValidLayoutMeasurements() {
		ActorNameplateLayout layout = new ActorNameplateLayout(2.3, 0.25);

		assertEquals(2.3, layout.bottomLineYOffset());
		assertEquals(0.25, layout.lineSpacing());
	}

	@Test
	void allowsZeroBottomOffset() {
		assertDoesNotThrow(() -> new ActorNameplateLayout(0.0, 0.25));
	}

	@Test
	void rejectsNegativeBottomOffset() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateLayout(-0.1, 0.25)
		);
	}

	@Test
	void rejectsNonFiniteBottomOffsets() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLayout(Double.NaN, 0.25)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLayout(Double.POSITIVE_INFINITY, 0.25)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLayout(Double.NEGATIVE_INFINITY, 0.25))
		);
	}

	@Test
	void rejectsZeroOrNegativeLineSpacing() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLayout(2.3, 0.0)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLayout(2.3, -0.1))
		);
	}

	@Test
	void rejectsNonFiniteLineSpacing() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLayout(2.3, Double.NaN)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLayout(2.3, Double.POSITIVE_INFINITY)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLayout(2.3, Double.NEGATIVE_INFINITY))
		);
	}
}
