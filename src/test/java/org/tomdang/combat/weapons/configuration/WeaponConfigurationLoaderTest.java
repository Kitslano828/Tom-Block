package org.tomdang.combat.weapons.configuration;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.customitemframework.combat.CombatWeightClass;
import org.tomdang.customitemframework.combat.CombatDamageType;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeaponConfigurationLoaderTest {

	@TempDir
	Path temporaryDirectory;

	private final WeaponConfigurationLoader loader = new WeaponConfigurationLoader();

	@Test
	void readerConfigurationLoadsWeapon() {
		List<WeaponDefinition> definitions = loader.loadDefinitions(new StringReader("""
				weapons:
				  TEST_WEAPON:
				    material: IRON_SWORD
				    display-name: "Test Weapon"
				    rarity: COMMON
				    weapon-class: HEAVY
				    damage-type: BLUNT
				    base-recovery-ticks: 30
				    stats:
				      damage: 10.0
				      strength: 2.0
				      mining-fortune: 4.0
				    abilities: []
				"""));

		WeaponDefinition definition = findDefinition(definitions, "TEST_WEAPON");
		assertEquals(Material.IRON_SWORD, definition.material());
		assertEquals("Test Weapon", definition.displayName());
		assertEquals(10, definition.statModifiers().get(PlayerStatType.DAMAGE));
		assertEquals(4, definition.statModifiers().get(PlayerStatType.MINING_FORTUNE));
		assertEquals(CombatWeightClass.HEAVY, definition.weaponClass());
		assertEquals(CombatDamageType.BLUNT, definition.damageType());
		assertEquals(30, definition.baseRecoveryTicks());
	}

	@Test
	void validConfigurationLoadsEveryWeapon() throws IOException {
		File file = writeConfiguration("""
				weapons:
				  ROOKIE_SWORD:
				    material: IRON_SWORD
				    display-name: "Rookie Sword"
				    rarity: COMMON
				    weapon-class: MEDIUM
				    damage-type: SLASHING
				    base-recovery-ticks: 16
				    damage: 17.0
				    strength: 5.0
				    abilities: []
				  PRACTICE_WAND:
				    material: STICK
				    display-name: "Practice Wand"
				    rarity: RARE
				    weapon-class: LIGHT
				    damage-type: MAGIC
				    base-recovery-ticks: 20
				    damage: 5.0
				    strength: 5.0
				    abilities:
				      - MAGIC_BOLT_ABILITY
				  WIND_BLADE:
				    material: DIAMOND_SWORD
				    display-name: "Wind Blade"
				    rarity: EPIC
				    weapon-class: LIGHT
				    damage-type: SLASHING
				    base-recovery-ticks: 12
				    damage: 100.0
				    strength: 100.0
				    abilities:
				      - WIND_DASH_ABILITY
				""");

		List<WeaponDefinition> definitions = loader.loadDefinitions(file);

		assertEquals(3, definitions.size());

		WeaponDefinition rookieSword = findDefinition(definitions, "ROOKIE_SWORD");
		assertEquals(Material.IRON_SWORD, rookieSword.material());
		assertEquals("Rookie Sword", rookieSword.displayName());
		assertEquals(Rarity.COMMON, rookieSword.rarity());
		assertEquals(17.0, rookieSword.damage());
		assertEquals(5.0, rookieSword.strength());
		assertEquals(CombatWeightClass.MEDIUM, rookieSword.weaponClass());
		assertEquals(CombatDamageType.SLASHING, rookieSword.damageType());
		assertEquals(16, rookieSword.baseRecoveryTicks());
		assertTrue(rookieSword.abilityIDs().isEmpty());

		WeaponDefinition practiceWand = findDefinition(definitions, "PRACTICE_WAND");
		assertEquals(List.of("MAGIC_BOLT_ABILITY"), practiceWand.abilityIDs());

		WeaponDefinition windBlade = findDefinition(definitions, "WIND_BLADE");
		assertEquals(List.of("WIND_DASH_ABILITY"), windBlade.abilityIDs());
	}

	@Test
	void missingWeaponsRootIsRejected() throws IOException {
		File file = writeConfiguration("something-else: {}\n");

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> loader.loadDefinitions(file)
		);

		assertTrue(exception.getMessage().contains("weapons"));
	}

	@Test
	void missingMaterialIsRejected() throws IOException {
		assertInvalidWeapon("""
				display-name: "Test Sword"
				rarity: COMMON
				damage: 10.0
				strength: 2.0
				abilities: []
				""", "TEST_WEAPON");
	}

	@Test
	void unknownMaterialIsRejected() throws IOException {
		assertInvalidWeapon(validWeaponBody().replace("IRON_SWORD", "IRON_SWROD"), "IRON_SWROD");
	}

	@Test
	void missingDisplayNameIsRejected() throws IOException {
		assertInvalidWeapon("""
				material: IRON_SWORD
				rarity: COMMON
				damage: 10.0
				strength: 2.0
				abilities: []
				""", "TEST_WEAPON");
	}

	@Test
	void missingRarityIsRejected() throws IOException {
		assertInvalidWeapon("""
				material: IRON_SWORD
				display-name: "Test Sword"
				damage: 10.0
				strength: 2.0
				abilities: []
				""", "TEST_WEAPON");
	}

	@Test
	void unknownRarityIsRejected() throws IOException {
		assertInvalidWeapon(validWeaponBody().replace("COMMON", "MYTHICALISH"), "MYTHICALISH");
	}

	@Test
	void missingDamageIsRejected() throws IOException {
		assertInvalidWeapon("""
				material: IRON_SWORD
				display-name: "Test Sword"
				rarity: COMMON
				strength: 2.0
				abilities: []
				""", "TEST_WEAPON");
	}

	@Test
	void nonNumericDamageIsRejected() throws IOException {
		assertInvalidWeapon(validWeaponBody().replace("damage: 10.0", "damage: lots"), "TEST_WEAPON");
	}

	@Test
	void negativeDamageIsRejected() throws IOException {
		assertInvalidWeapon(validWeaponBody().replace("damage: 10.0", "damage: -1.0"), "TEST_WEAPON");
	}

	@Test
	void missingStrengthIsRejected() throws IOException {
		assertInvalidWeapon("""
				material: IRON_SWORD
				display-name: "Test Sword"
				rarity: COMMON
				damage: 10.0
				abilities: []
				""", "TEST_WEAPON");
	}

	@Test
	void nonNumericStrengthIsRejected() throws IOException {
		assertInvalidWeapon(validWeaponBody().replace("strength: 2.0", "strength: strong"), "TEST_WEAPON");
	}

	@Test
	void negativeStrengthIsRejected() throws IOException {
		assertInvalidWeapon(validWeaponBody().replace("strength: 2.0", "strength: -1.0"), "TEST_WEAPON");
	}

	@Test
	void missingAbilitiesListIsRejected() throws IOException {
		assertInvalidWeapon("""
				material: IRON_SWORD
				display-name: "Test Sword"
				rarity: COMMON
				damage: 10.0
				strength: 2.0
				""", "TEST_WEAPON");
	}

	@Test
	void scalarAbilitiesValueIsRejected() throws IOException {
		assertInvalidWeapon(
				validWeaponBody().replace("abilities: []", "abilities: MAGIC_BOLT_ABILITY"),
				"TEST_WEAPON"
		);
	}

	@Test
	void blankAbilityIDIsRejected() throws IOException {
		assertInvalidWeapon(
				validWeaponBody().replace("abilities: []", "abilities:\n  - '   '"),
				"TEST_WEAPON"
		);
	}

	@Test
	void invalidCombatMetadataIsRejected() throws IOException {
		assertInvalidWeapon(validWeaponBody().replace("MEDIUM", "QUICKISH"), "weapon-class");
		assertInvalidWeapon(validWeaponBody().replace("SLASHING", "LASER"), "damage-type");
		assertInvalidWeapon(validWeaponBody().replace("base-recovery-ticks: 20", "base-recovery-ticks: 0"),
				"base-recovery-ticks");
	}

	private void assertInvalidWeapon(String weaponBody, String expectedMessagePart) throws IOException {
		File file = writeConfiguration("weapons:\n  TEST_WEAPON:\n" + indent(weaponBody, 4));

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> loader.loadDefinitions(file)
		);

		assertTrue(
				exception.getMessage().contains(expectedMessagePart),
				() -> "Expected error message to contain '" + expectedMessagePart
						+ "' but was '" + exception.getMessage() + "'"
		);
	}

	private File writeConfiguration(String contents) throws IOException {
		Path file = temporaryDirectory.resolve("weapons.yml");
		Files.writeString(file, contents);
		return file.toFile();
	}

	private WeaponDefinition findDefinition(List<WeaponDefinition> definitions, String id) {
		return definitions.stream()
				.filter(definition -> definition.id().equals(id))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Missing weapon definition " + id));
	}

	private String validWeaponBody() {
		return """
				material: IRON_SWORD
				display-name: "Test Sword"
				rarity: COMMON
				weapon-class: MEDIUM
				damage-type: SLASHING
				base-recovery-ticks: 20
				damage: 10.0
				strength: 2.0
				abilities: []
				""";
	}

	private String indent(String value, int spaces) {
		String indentation = " ".repeat(spaces);
		return value.lines()
				.map(line -> indentation + line)
				.reduce("", (result, line) -> result + line + "\n");
	}
}
