package org.tomdang.actorframework.nameplate.presentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ActorNameplatePresentationRegistryTest {

	private ActorNameplatePresentationRegistry registry;

	@BeforeEach
	void setUp() {
		registry = new ActorNameplatePresentationRegistry();
	}

	@Test
	void registersAndLooksUpViewerHandle() {
		ActorNameplateViewerHandle handle = viewerHandle(UUID.randomUUID(), UUID.randomUUID(), 1);

		registry.register(handle);

		assertTrue(registry.doesPresentationHandleExist(handle.actorNameplateViewerKey()));
		assertSame(handle, registry.lookup(handle.actorNameplateViewerKey()));
	}

	@Test
	void rejectsNullRegistration() {
		assertThrows(IllegalArgumentException.class, () -> registry.register(null));
	}

	@Test
	void rejectsDuplicateViewerKeyWithoutReplacingExistingHandle() {
		UUID instanceUUID = UUID.randomUUID();
		UUID viewerUUID = UUID.randomUUID();
		ActorNameplateViewerHandle first = viewerHandle(instanceUUID, viewerUUID, 1);
		ActorNameplateViewerHandle duplicate = viewerHandle(instanceUUID, viewerUUID, 2);
		registry.register(first);

		assertThrows(IllegalStateException.class, () -> registry.register(duplicate));
		assertSame(first, registry.lookup(first.actorNameplateViewerKey()));
	}

	@Test
	void existenceCheckRejectsNullKey() {
		assertThrows(IllegalArgumentException.class, () ->
				registry.doesPresentationHandleExist(null)
		);
	}

	@Test
	void lookupRejectsNullKey() {
		assertThrows(IllegalArgumentException.class, () -> registry.lookup(null));
	}

	@Test
	void lookupReturnsNullForUnknownKey() {
		assertNull(registry.lookup(new ActorNameplateViewerKey(UUID.randomUUID(), UUID.randomUUID())));
	}

	@Test
	void removesAndReturnsViewerHandle() {
		ActorNameplateViewerHandle handle = viewerHandle(UUID.randomUUID(), UUID.randomUUID(), 1);
		registry.register(handle);

		assertSame(handle, registry.removeHandle(handle.actorNameplateViewerKey()));
		assertFalse(registry.doesPresentationHandleExist(handle.actorNameplateViewerKey()));
	}

	@Test
	void removalRejectsNullKey() {
		assertThrows(IllegalArgumentException.class, () -> registry.removeHandle(null));
	}

	@Test
	void removalReturnsNullForUnknownKey() {
		assertNull(registry.removeHandle(
				new ActorNameplateViewerKey(UUID.randomUUID(), UUID.randomUUID())
		));
	}

	@Test
	void findsOnlyHandlesForRequestedActor() {
		UUID requestedActor = UUID.randomUUID();
		ActorNameplateViewerHandle firstViewer = viewerHandle(requestedActor, UUID.randomUUID(), 1);
		ActorNameplateViewerHandle secondViewer = viewerHandle(requestedActor, UUID.randomUUID(), 2);
		ActorNameplateViewerHandle anotherActor =
				viewerHandle(UUID.randomUUID(), UUID.randomUUID(), 3);
		registry.register(firstViewer);
		registry.register(secondViewer);
		registry.register(anotherActor);

		List<ActorNameplateViewerHandle> result = registry.findActorHandles(requestedActor);

		assertEquals(2, result.size());
		assertTrue(result.contains(firstViewer));
		assertTrue(result.contains(secondViewer));
		assertFalse(result.contains(anotherActor));
	}

	@Test
	void actorLookupRejectsNullUUID() {
		assertThrows(IllegalArgumentException.class, () -> registry.findActorHandles(null));
	}

	@Test
	void findsOnlyHandlesForRequestedViewer() {
		UUID requestedViewer = UUID.randomUUID();
		ActorNameplateViewerHandle firstActor = viewerHandle(UUID.randomUUID(), requestedViewer, 1);
		ActorNameplateViewerHandle secondActor = viewerHandle(UUID.randomUUID(), requestedViewer, 2);
		ActorNameplateViewerHandle anotherViewer =
				viewerHandle(UUID.randomUUID(), UUID.randomUUID(), 3);
		registry.register(firstActor);
		registry.register(secondActor);
		registry.register(anotherViewer);

		List<ActorNameplateViewerHandle> result = registry.findViewerHandles(requestedViewer);

		assertEquals(2, result.size());
		assertTrue(result.contains(firstActor));
		assertTrue(result.contains(secondActor));
		assertFalse(result.contains(anotherViewer));
	}

	@Test
	void viewerLookupRejectsNullUUID() {
		assertThrows(IllegalArgumentException.class, () -> registry.findViewerHandles(null));
	}

	@Test
	void bulkLookupResultsCannotModifyRegistryState() {
		UUID instanceUUID = UUID.randomUUID();
		ActorNameplateViewerHandle handle = viewerHandle(instanceUUID, UUID.randomUUID(), 1);
		registry.register(handle);
		List<ActorNameplateViewerHandle> result = registry.findActorHandles(instanceUUID);

		assertThrows(UnsupportedOperationException.class, () -> result.clear());
		assertSame(handle, registry.lookup(handle.actorNameplateViewerKey()));
	}

	private static ActorNameplateViewerHandle viewerHandle(
			UUID instanceUUID,
			UUID viewerUUID,
			int entityID
	) {
		ActorNameplateViewerKey key = new ActorNameplateViewerKey(instanceUUID, viewerUUID);
		ActorNameplateLinePresentationHandle lineHandle =
				new ActorNameplateLinePresentationHandle(UUID.randomUUID(), entityID);
		return new ActorNameplateViewerHandle(key, List.of(lineHandle), false);
	}
}
