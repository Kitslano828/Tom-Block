package org.tomdang.actorframework.nameplate.presentation;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ActorNameplateViewerHandleTest {

	@Test
	void storesKeyOrderedLinesAndMovementState() {
		ActorNameplateViewerKey key = viewerKey();
		ActorNameplateLinePresentationHandle first = lineHandle(1);
		ActorNameplateLinePresentationHandle second = lineHandle(2);

		ActorNameplateViewerHandle handle =
				new ActorNameplateViewerHandle(key, List.of(first, second), true);

		assertEquals(key, handle.actorNameplateViewerKey());
		assertEquals(List.of(first, second), handle.linePresentationHandles());
		assertTrue(handle.inMovingState());
	}

	@Test
	void rejectsNullViewerKey() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateViewerHandle(null, List.of(lineHandle(1)), false)
		);
	}

	@Test
	void rejectsNullLineHandles() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateViewerHandle(viewerKey(), null, false)
		);
	}

	@Test
	void rejectsEmptyLineHandles() {
		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateViewerHandle(viewerKey(), List.of(), false)
		);
	}

	@Test
	void rejectsNullLineHandleElement() {
		List<ActorNameplateLinePresentationHandle> handles = new ArrayList<>();
		handles.add(lineHandle(1));
		handles.add(null);

		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateViewerHandle(viewerKey(), handles, false)
		);
	}

	@Test
	void rejectsDuplicatePresentationUUID() {
		UUID duplicatedUUID = UUID.randomUUID();
		ActorNameplateLinePresentationHandle first =
				new ActorNameplateLinePresentationHandle(duplicatedUUID, 1, 2.3);
		ActorNameplateLinePresentationHandle second =
				new ActorNameplateLinePresentationHandle(duplicatedUUID, 2, 2.3);

		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateViewerHandle(viewerKey(), List.of(first, second), false)
		);
	}

	@Test
	void rejectsDuplicateEntityID() {
		ActorNameplateLinePresentationHandle first = lineHandle(1);
		ActorNameplateLinePresentationHandle second = lineHandle(1);

		assertThrows(IllegalArgumentException.class, () ->
				new ActorNameplateViewerHandle(viewerKey(), List.of(first, second), false)
		);
	}

	@Test
	void defensivelyCopiesSuppliedLineHandles() {
		ActorNameplateLinePresentationHandle first = lineHandle(1);
		List<ActorNameplateLinePresentationHandle> suppliedHandles = new ArrayList<>();
		suppliedHandles.add(first);

		ActorNameplateViewerHandle handle =
				new ActorNameplateViewerHandle(viewerKey(), suppliedHandles, false);
		suppliedHandles.add(lineHandle(2));

		assertEquals(List.of(first), handle.linePresentationHandles());
	}

	@Test
	void storedLineHandlesCannotBeModified() {
		ActorNameplateViewerHandle handle =
				new ActorNameplateViewerHandle(viewerKey(), List.of(lineHandle(1)), false);

		assertThrows(UnsupportedOperationException.class, () ->
				handle.linePresentationHandles().add(lineHandle(2))
		);
	}

	private static ActorNameplateViewerKey viewerKey() {
		return new ActorNameplateViewerKey(UUID.randomUUID(), UUID.randomUUID());
	}

	private static ActorNameplateLinePresentationHandle lineHandle(int entityID) {
		return new ActorNameplateLinePresentationHandle(UUID.randomUUID(), entityID, 2.3);
	}
}
