package org.tomdang.region.visualization;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegionVisualizationConfigurationLoaderTest {
	private final RegionVisualizationConfigurationLoader loader = new RegionVisualizationConfigurationLoader();

	@Test
	void loadsAllVisualizationLimits() {
		RegionVisualizationSettings settings = loader.load(new StringReader(validYaml()));

		assertEquals(5, settings.intervalTicks());
		assertEquals(48, settings.radius());
		assertEquals(1, settings.boundarySpacing());
		assertEquals(1600, settings.maximumMarkers());
		assertEquals(20, settings.targetDistance());
	}

	@Test
	void rejectsMissingNonNumericAndNonPositiveValues() {
		assertThrows(IllegalArgumentException.class, () -> loader.load(null));
		assertThrows(IllegalArgumentException.class,
				() -> loader.load(new StringReader(validYaml().replace("  radius: 48\n", ""))));
		assertThrows(IllegalArgumentException.class,
				() -> loader.load(new StringReader(validYaml().replace("boundary-spacing: 1", "boundary-spacing: dense"))));
		assertThrows(IllegalArgumentException.class,
				() -> loader.load(new StringReader(validYaml().replace("maximum-markers: 1600", "maximum-markers: 0"))));
	}

	private String validYaml() {
		return """
				visualization:
				  interval-ticks: 5
				  radius: 48
				  boundary-spacing: 1
				  maximum-markers: 1600
				  target-distance: 20
				""";
	}
}
