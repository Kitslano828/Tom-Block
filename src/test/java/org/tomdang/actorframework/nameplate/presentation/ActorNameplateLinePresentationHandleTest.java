package org.tomdang.actorframework.nameplate.presentation;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ActorNameplateLinePresentationHandleTest {

	@Test
	void storesPresentationIdentity() {
		UUID presentationUUID = UUID.randomUUID();

		ActorNameplateLinePresentationHandle handle =
				new ActorNameplateLinePresentationHandle(presentationUUID, 42, 2.3);

		assertEquals(presentationUUID, handle.presentationUUID());
		assertEquals(42, handle.entityID());
		assertEquals(2.3, handle.verticalOffset());
	}

	@Test
	void rejectsNullPresentationUUID() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateLinePresentationHandle(null, 42, 2.3)
		);
	}

	@Test
	void rejectsZeroEntityID() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateLinePresentationHandle(UUID.randomUUID(), 0, 2.3)
		);
	}

	@Test
	void rejectsNegativeEntityID() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateLinePresentationHandle(UUID.randomUUID(), -1, 2.3)
		);
	}

	@Test
	void allowsZeroVerticalOffset() {
		ActorNameplateLinePresentationHandle handle =
				new ActorNameplateLinePresentationHandle(UUID.randomUUID(), 42, 0.0);

		assertEquals(0.0, handle.verticalOffset());
	}

	@Test
	void rejectsNonFiniteVerticalOffset() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLinePresentationHandle(UUID.randomUUID(), 42, Double.NaN)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLinePresentationHandle(UUID.randomUUID(), 42, Double.POSITIVE_INFINITY)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						new ActorNameplateLinePresentationHandle(UUID.randomUUID(), 42, Double.NEGATIVE_INFINITY))
		);
	}

	@Test
	void rejectsNegativeVerticalOffset() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateLinePresentationHandle(UUID.randomUUID(), 42, -0.1)
		);
	}
}
