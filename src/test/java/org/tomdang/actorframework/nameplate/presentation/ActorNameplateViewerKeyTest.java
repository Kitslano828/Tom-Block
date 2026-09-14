package org.tomdang.actorframework.nameplate.presentation;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ActorNameplateViewerKeyTest {

	@Test
	void storesActorAndViewerIdentity() {
		UUID instanceUUID = UUID.randomUUID();
		UUID viewerUUID = UUID.randomUUID();

		ActorNameplateViewerKey key = new ActorNameplateViewerKey(instanceUUID, viewerUUID);

		assertEquals(instanceUUID, key.instanceUUID());
		assertEquals(viewerUUID, key.viewerUUID());
	}

	@Test
	void equalIdentitiesProduceEqualKeys() {
		UUID instanceUUID = UUID.randomUUID();
		UUID viewerUUID = UUID.randomUUID();

		ActorNameplateViewerKey first = new ActorNameplateViewerKey(instanceUUID, viewerUUID);
		ActorNameplateViewerKey second = new ActorNameplateViewerKey(instanceUUID, viewerUUID);

		assertEquals(first, second);
		assertEquals(first.hashCode(), second.hashCode());
	}

	@Test
	void differentViewersProduceDifferentKeys() {
		UUID instanceUUID = UUID.randomUUID();

		ActorNameplateViewerKey first = new ActorNameplateViewerKey(instanceUUID, UUID.randomUUID());
		ActorNameplateViewerKey second = new ActorNameplateViewerKey(instanceUUID, UUID.randomUUID());

		assertNotEquals(first, second);
	}

	@Test
	void rejectsNullInstanceUUID() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateViewerKey(null, UUID.randomUUID())
		);
	}

	@Test
	void rejectsNullViewerUUID() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateViewerKey(UUID.randomUUID(), null)
		);
	}
}
