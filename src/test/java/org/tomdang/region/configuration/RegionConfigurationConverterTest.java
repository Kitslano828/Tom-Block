package org.tomdang.region.configuration;

import org.junit.jupiter.api.Test;
import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.shape.CompositeRegionShape;
import org.tomdang.region.shape.CuboidRegionShape;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionConfigurationConverterTest {
	private final RegionConfigurationConverter converter = new RegionConfigurationConverter();

	@Test
	void convertsSingleCuboidWithoutUnnecessaryComposite() {
		RegionDefinition definition = converter.convert(configuration(List.of(cuboid(0, 10))));

		assertInstanceOf(CuboidRegionShape.class, definition.shape());
		assertEquals("VILLAGE", definition.id());
		assertTrue(definition.directlyContains(position(5)));
	}

	@Test
	void convertsMultipleCuboidsIntoCompositeAndPreservesOverrides() {
		RegionDefinition definition = converter.convert(configuration(List.of(cuboid(0, 10), cuboid(20, 30))));

		assertInstanceOf(CompositeRegionShape.class, definition.shape());
		assertTrue(definition.directlyContains(position(5)));
		assertTrue(definition.directlyContains(position(25)));
		assertTrue(definition.directlyContains(position(50)));
	}

	@Test
	void rejectsNullConfiguration() {
		assertThrows(IllegalArgumentException.class, () -> converter.convert(null));
	}

	private RegionConfigurationDefinition configuration(List<RegionCuboidConfigurationDefinition> cuboids) {
		return new RegionConfigurationDefinition("VILLAGE", Optional.empty(), 10, Set.of("SAFE"), cuboids,
				Set.of(position(50)), Set.of(position(6)));
	}

	private RegionCuboidConfigurationDefinition cuboid(int minimum, int maximum) {
		return new RegionCuboidConfigurationDefinition(position(minimum), position(maximum));
	}

	private BlockPosition position(int x) {
		return new BlockPosition("world", x, 64, 0);
	}
}
