package org.tomdang.critter.configuration;

import org.junit.jupiter.api.Test;
import org.tomdang.critter.definition.MovementType;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BundledSouthwestIslandCrittersTest {
	@Test void loadsTheCompleteIslandRosterWithDistinctFieldNotes() throws Exception {
		var expected = Set.of("BRAMBLEHOG", "BURROWTAIL", "CANOPY_SCREECHER", "DEWHOPPER", "SPORELING");
		var definitions = List.of("bramblehog", "burrowtail", "canopy-screecher", "dewhopper", "sporeling")
				.stream().map(this::load).toList();
		assertEquals(expected, definitions.stream().map(value -> value.id()).collect(java.util.stream.Collectors.toSet()));
		assertTrue(definitions.stream().allMatch(value -> !value.journalDescription().isBlank()));
		assertEquals(5, definitions.stream().map(value -> value.family()).distinct().count());
		assertTrue(definitions.stream().allMatch(value -> !value.rewards().materials().isEmpty()));
	}

	@Test void sporelingIsRestrictedToRedAndBrownMushroomPatches() {
		var sporeling = load("sporeling");
		assertEquals(Set.of("RED_MUSHROOM_PATCH", "BROWN_MUSHROOM_PATCH"), sporeling.habitats());
		assertTrue(sporeling.movement().modes().contains(MovementType.BURROWING));
	}

	@Test void canopyScreecherUsesFlightWhileGroundSpeciesUseNativeNavigation() {
		assertEquals("DIRECT_FLIGHT", load("canopy-screecher").ai().navigator());
		for (String id : List.of("bramblehog", "burrowtail", "dewhopper", "sporeling"))
			assertEquals("NATIVE_GROUND", load(id).ai().navigator());
	}

	private org.tomdang.critter.definition.CritterDefinition load(String name) {
		try (var input = getClass().getClassLoader()
				.getResourceAsStream("critters/southwest-island/" + name + ".yml")) {
			assertNotNull(input, "Missing bundled critter " + name);
			return new CritterConfigurationLoader().load(input);
		} catch (java.io.IOException exception) {
			throw new java.io.UncheckedIOException(exception);
		}
	}
}
