package org.tomdang.actorframework.nameplate.nms.runtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NmsActorNameplateLineRegistryTest {

	private NmsActorNameplateLineRegistry registry;

	@BeforeEach
	void setUp() {
		registry = new NmsActorNameplateLineRegistry();
	}

	@Test
	void registersAndFindsRuntimeLine() {
		UUID presentationUUID = UUID.randomUUID();
		NmsActorNameplateLine line = runtimeLine(presentationUUID, 1);

		registry.register(line);

		assertTrue(registry.contains(presentationUUID));
		assertSame(line, registry.get(presentationUUID));
	}

	@Test
	void rejectsNullRegistration() {
		assertThrows(IllegalArgumentException.class, () -> registry.register(null));
	}

	@Test
	void rejectsDuplicateUUIDWithoutReplacingExistingLine() {
		UUID presentationUUID = UUID.randomUUID();
		NmsActorNameplateLine first = runtimeLine(presentationUUID, 1);
		NmsActorNameplateLine duplicate = runtimeLine(presentationUUID, 2);
		registry.register(first);

		assertThrows(IllegalStateException.class, () -> registry.register(duplicate));
		assertSame(first, registry.get(presentationUUID));
	}

	@Test
	void getRejectsNullUUID() {
		assertThrows(IllegalArgumentException.class, () -> registry.get(null));
	}

	@Test
	void getReturnsNullForUnknownUUID() {
		assertNull(registry.get(UUID.randomUUID()));
	}

	@Test
	void containsRejectsNullUUID() {
		assertThrows(IllegalArgumentException.class, () -> registry.contains(null));
	}

	@Test
	void unknownUUIDIsNotContained() {
		assertFalse(registry.contains(UUID.randomUUID()));
	}

	@Test
	void removesAndReturnsRuntimeLine() {
		UUID presentationUUID = UUID.randomUUID();
		NmsActorNameplateLine line = runtimeLine(presentationUUID, 1);
		registry.register(line);

		assertSame(line, registry.remove(presentationUUID));
		assertFalse(registry.contains(presentationUUID));
	}

	@Test
	void removeRejectsNullUUID() {
		assertThrows(IllegalArgumentException.class, () -> registry.remove(null));
	}

	@Test
	void removeReturnsNullForUnknownUUID() {
		assertNull(registry.remove(UUID.randomUUID()));
	}

	@Test
	void allLinesReturnsImmutableSnapshot() {
		NmsActorNameplateLine first = runtimeLine(UUID.randomUUID(), 1);
		NmsActorNameplateLine second = runtimeLine(UUID.randomUUID(), 2);
		registry.register(first);

		List<NmsActorNameplateLine> snapshot = registry.getAllLines();
		registry.register(second);

		assertEquals(1, snapshot.size());
		assertTrue(snapshot.contains(first));
		assertFalse(snapshot.contains(second));
		assertThrows(UnsupportedOperationException.class, snapshot::clear);
	}

	private static NmsActorNameplateLine runtimeLine(UUID presentationUUID, int entityID) {
		NmsActorNameplateLine line = mock(NmsActorNameplateLine.class);
		when(line.getPresentationUUID()).thenReturn(presentationUUID);
		when(line.getEntityID()).thenReturn(entityID);
		return line;
	}
}
