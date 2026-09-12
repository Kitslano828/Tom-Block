package org.tomdang.hud.text;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MinecraftDefaultTextWidthServiceTest {

	private MinecraftDefaultTextWidthService service;

	@BeforeEach
	void setUp() {
		service = new MinecraftDefaultTextWidthService();
	}

	@Test
	void nullTextIsRejected() {
		assertThrows(
				IllegalArgumentException.class,
				() -> service.measure(null)
		);
	}

	@Test
	void emptyTextMeasuresZero() {
		assertEquals(0, service.measure(""));
	}

	@Test
	void spaceHasMeasurableWidth() {
		int spaceWidth = service.measure(" ");
		assertEquals(4, spaceWidth);
	}

	@Test
	void narrowLettersAreNarrowerThanWideLetters() {
		int narrowWidth = service.measure("i");
		int wideWidth = service.measure("W");

		assertTrue(narrowWidth < wideWidth);
		assertEquals(2, narrowWidth);
		assertEquals(6, wideWidth);
	}

	@Test
	void multipleCharactersEqualSumOfIndividualMeasurements() {
		int iWidth = service.measure("i"); // 2
		int aWidth = service.measure("a"); // 6
		int combined = service.measure("ia");

		assertEquals(iWidth + aWidth, combined);
		assertEquals(8, combined);
	}

	@Test
	void unknownCharacterReceivesFallbackWidth() {
		// '§' and 'ñ' are not explicitly defined in the map, fallback is 6
		assertEquals(6, service.measure("§"));
		assertEquals(6, service.measure("ñ"));
	}

	@Test
	@DisplayName("'Test dialogue' produces the expected total")
	void testDialogueProducesExpectedTotal() {
		// T (6) + e (6) + s (6) + t (4) = 22
		// ' ' (4)
		// d (6) + i (2) + a (6) + l (2) + o (6) + g (6) + u (6) + e (6) = 40
		// Total = 22 + 4 + 41 = 66
		assertEquals(66, service.measure("Test dialogue"));
	}

}
