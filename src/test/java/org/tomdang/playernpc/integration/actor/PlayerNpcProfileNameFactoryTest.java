package org.tomdang.playernpc.integration.actor;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerNpcProfileNameFactoryTest {

	private final PlayerNpcProfileNameFactory factory = new PlayerNpcProfileNameFactory();

	@Test
	void createProducesDeterministicSixteenCharacterName() {
		UUID actorInstanceID = UUID.fromString("12345678-90ab-cdef-1234-567890abcdef");

		String first = factory.create(actorInstanceID);
		String second = factory.create(actorInstanceID);

		assertEquals("tb_1234567890abc", first);
		assertEquals(16, first.length());
		assertEquals(first, second);
	}

	@Test
	void createProducesDifferentNamesForDifferentInstancePrefixes() {
		String first = factory.create(UUID.fromString("12345678-90ab-cdef-1234-567890abcdef"));
		String second = factory.create(UUID.fromString("abcdef12-3456-7890-abcd-ef1234567890"));

		assertNotEquals(first, second);
	}

	@Test
	void createRejectsNullInstanceID() {
		assertThrows(IllegalArgumentException.class, () -> factory.create(null));
	}
}
