package org.tomdang.player.skill.menu;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.player.skill.SkillType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class SkillMenuConfigurationTest {
	@Test
	void bundledConfigurationPlacesTwoSkillsInsideFiftyFourSlotBorder() {
		try (InputStream stream = getClass().getClassLoader().getResourceAsStream("skills/skills-menu.yml")) {
			assertNotNull(stream);
			SkillMenuConfiguration config = SkillMenuConfiguration.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
			assertEquals(49, config.closeSlot());
			assertEquals(45, config.backSlot());
			assertEquals(22, config.detailSlot());
			assertEquals(21, config.entries().get(SkillType.MINING).slot());
			assertEquals(23, config.entries().get(SkillType.COMBAT).slot());
			assertEquals(Material.GRAY_STAINED_GLASS_PANE, config.backgroundMaterial());
			assertEquals(Material.LIGHT_BLUE_STAINED_GLASS_PANE, config.entries().get(SkillType.MINING).accent());
			assertEquals(4, config.accentSlots().size());
			var background = new SkillMenuBackgroundRenderer().layout(config,
					config.entries().get(SkillType.MINING).accent());
			assertEquals(54, background.size());
			assertEquals(Material.LIGHT_GRAY_STAINED_GLASS_PANE, background.get(10));
			assertEquals(Material.LIGHT_BLUE_STAINED_GLASS_PANE, background.get(8));
			assertEquals(Material.GRAY_STAINED_GLASS_PANE, background.get(1));
			assertEquals(Material.LIGHT_BLUE_STAINED_GLASS_PANE, background.get(45));
			assertEquals(Material.BLACK_STAINED_GLASS_PANE, config.progressEmpty());
			assertTrue(background.stream().allMatch(material -> material.name().endsWith("STAINED_GLASS_PANE")));
		} catch (java.io.IOException exception) {
			fail(exception);
		}
	}

	@Test
	void rejectsOverlappingOrInvalidSlots() {
		String yaml;
		try (InputStream stream = getClass().getClassLoader().getResourceAsStream("skills/skills-menu.yml")) {
			assertNotNull(stream);
			yaml = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (java.io.IOException exception) {
			throw new AssertionError(exception);
		}
		assertThrows(IllegalArgumentException.class,
				() -> SkillMenuConfiguration.load(new StringReader(yaml.replace("slot: 23", "slot: 21"))));
		assertThrows(IllegalArgumentException.class,
				() -> SkillMenuConfiguration.load(new StringReader(yaml.replace("detail-slot: 22", "detail-slot: 30"))));
		assertThrows(IllegalArgumentException.class,
				() -> SkillMenuConfiguration.load(new StringReader(yaml.replace("#FF5555", "red"))));
		assertThrows(IllegalArgumentException.class,
				() -> SkillMenuConfiguration.load(new StringReader(yaml.replace("accent-slots: [0, 8, 45, 53]", "accent-slots: [22]"))));
	}
}
