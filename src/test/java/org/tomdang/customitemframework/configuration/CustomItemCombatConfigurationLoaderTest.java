package org.tomdang.customitemframework.configuration;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.tomdang.combat.eligibility.AttackCapability;
import org.tomdang.customitemframework.combat.CombatDamageType;
import org.tomdang.customitemframework.combat.CombatWeightClass;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomItemCombatConfigurationLoaderTest {
	private final CustomItemCombatConfigurationLoader loader = new CustomItemCombatConfigurationLoader();

	@Test
	void absentPropertiesProduceAnEmptyProfile() {
		var profile = loader.load(section("material: STONE\n"), "TEST_ITEM");
		assertTrue(profile.weightClass().isEmpty());
		assertTrue(profile.damageType().isEmpty());
		assertTrue(profile.baseRecoveryTicks().isEmpty());
		assertTrue(profile.attackCapabilities().isEmpty());
	}

	@Test
	void loadsAttackCapabilitiesForAnyCustomItem() {
		var profile = loader.load(section("""
				attack-capabilities:
				  - JELLYFISH_HUNTING
				"""), "JELLYFISH_NET");

		assertEquals(java.util.Set.of(AttackCapability.JELLYFISH_HUNTING), profile.attackCapabilities());
		assertTrue(profile.weightClass().isEmpty());
	}

	@Test
	void rejectsUnknownOrMalformedAttackCapabilities() {
		assertThrows(IllegalArgumentException.class,
				() -> loader.load(section("attack-capabilities: [UNKNOWN]\n"), "TEST_ITEM"));
		assertThrows(IllegalArgumentException.class,
				() -> loader.load(section("attack-capabilities: JELLYFISH_HUNTING\n"), "TEST_ITEM"));
		assertThrows(IllegalArgumentException.class,
				() -> loader.load(section("attack-capabilities: [JELLYFISH_HUNTING, JELLYFISH_HUNTING]\n"), "TEST_ITEM"));
	}

	@Test
	void loadsCombatTraitsAndRecoveryForAnyCustomItem() {
		var profile = loader.load(section("""
				weapon-class: HEAVY
				damage-type: BLUNT
				base-recovery-ticks: 40
				"""), "DIVANS_DRILL");
		assertEquals(CombatWeightClass.HEAVY, profile.weightClass().orElseThrow());
		assertEquals(CombatDamageType.BLUNT, profile.damageType().orElseThrow());
		assertEquals(40, profile.baseRecoveryTicks().orElseThrow());
	}

	@Test
	void incompleteTraitsAndInvalidRecoveryAreRejected() {
		assertThrows(IllegalArgumentException.class,
				() -> loader.load(section("weapon-class: HEAVY\n"), "TEST_ITEM"));
		assertThrows(IllegalArgumentException.class,
				() -> loader.load(section("base-recovery-ticks: 0\n"), "TEST_ITEM"));
		assertThrows(IllegalArgumentException.class,
				() -> loader.load(section("base-recovery-ticks: slow\n"), "TEST_ITEM"));
	}

	private org.bukkit.configuration.ConfigurationSection section(String yaml) {
		return YamlConfiguration.loadConfiguration(new StringReader(yaml));
	}
}
