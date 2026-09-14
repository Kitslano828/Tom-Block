package org.tomdang.actorframework.nameplate.presentation;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ActorNameplateLinePresentationHandleTest {

	@Test
	void storesPresentationIdentity() {
		UUID presentationUUID = UUID.randomUUID();

		ActorNameplateLinePresentationHandle handle =
				new ActorNameplateLinePresentationHandle(presentationUUID, 42);

		assertEquals(presentationUUID, handle.presentationUUID());
		assertEquals(42, handle.entityID());
	}

	@Test
	void rejectsNullPresentationUUID() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateLinePresentationHandle(null, 42)
		);
	}

	@Test
	void rejectsZeroEntityID() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateLinePresentationHandle(UUID.randomUUID(), 0)
		);
	}

	@Test
	void rejectsNegativeEntityID() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateLinePresentationHandle(UUID.randomUUID(), -1)
		);
	}
}
