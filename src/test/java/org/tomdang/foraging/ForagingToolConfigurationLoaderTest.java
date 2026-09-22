package org.tomdang.foraging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForagingToolConfigurationLoaderTest {
	@Test void loadsAxeLevelRequirements() {
		ForagingToolRegistry tools = new ForagingToolConfigurationLoader().load(
				getClass().getClassLoader().getResourceAsStream("foraging/tools.yml"));
		assertEquals(0, tools.find("ROOKIE_AXE").requiredForagingLevel());
		assertEquals(5, tools.find("FORESTER_AXE").requiredForagingLevel());
		assertNull(tools.find("NOT_AN_AXE"));
	}
}
