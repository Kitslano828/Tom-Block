package org.tomdang.customitemframework.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.tomdang.combat.eligibility.AttackCapability;
import org.tomdang.customitemframework.combat.CombatDamageType;
import org.tomdang.customitemframework.combat.CombatWeightClass;
import org.tomdang.customitemframework.combat.CustomItemCombatProfile;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

public class CustomItemCombatConfigurationLoader {

	public CustomItemCombatProfile load(ConfigurationSection section, String itemId) {
		if (section == null) throw new IllegalArgumentException("section cannot be null");
		if (itemId == null || itemId.isBlank()) throw new IllegalArgumentException("itemId cannot be null or blank");
		Optional<CombatWeightClass> weight = parseOptionalEnum(section, itemId, "weapon-class", CombatWeightClass.class);
		Optional<CombatDamageType> damage = parseOptionalEnum(section, itemId, "damage-type", CombatDamageType.class);
		if (weight.isPresent() != damage.isPresent()) {
			throw new IllegalArgumentException(itemId + " must configure weapon-class and damage-type together");
		}
		OptionalLong recovery = OptionalLong.empty();
		if (section.isSet("base-recovery-ticks")) {
			if (!section.isInt("base-recovery-ticks") && !section.isLong("base-recovery-ticks")) {
				throw new IllegalArgumentException(itemId + " has an invalid base-recovery-ticks");
			}
			long value = section.getLong("base-recovery-ticks");
			if (value <= 0) throw new IllegalArgumentException(itemId + " must have positive base-recovery-ticks");
			recovery = OptionalLong.of(value);
		}

		Set<AttackCapability> capabilities = EnumSet.noneOf(AttackCapability.class);
		if (section.isSet("attack-capabilities")) {
			if (!section.isList("attack-capabilities")) throw new IllegalArgumentException(itemId + " does not contain a valid list of attack capabilities");
			List<?> attackCapabilities = section.getList("attack-capabilities");

			for (Object capability : attackCapabilities) {
				if (!(capability instanceof String)) {
					throw new IllegalArgumentException(itemId + " has a non-text attack capability");
				}

				String name = (String) capability;
				if (name.isBlank()) {
					throw new IllegalArgumentException(itemId + " has a blank attack capability");
				}

				AttackCapability parsed;
				try {
					parsed = AttackCapability.valueOf(name.trim().toUpperCase(Locale.ROOT));
				} catch (IllegalArgumentException exception) {
					throw new IllegalArgumentException(
							itemId + " has an unknown attack capability: " + name,
							exception
					);
				}

				if (!capabilities.add(parsed)) throw new IllegalArgumentException(itemId + " has a duplicate capability " + parsed);
			}
		}
		return new CustomItemCombatProfile(weight, damage, recovery, capabilities);
	}

	private <E extends Enum<E>> Optional<E> parseOptionalEnum(ConfigurationSection section, String itemId,
	                                                         String field, Class<E> enumType) {
		if (!section.isSet(field)) return Optional.empty();
		String value = section.getString(field);
		if (value == null || value.isBlank()) throw new IllegalArgumentException(itemId + " has an invalid " + field);
		try {
			return Optional.of(Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT)));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(itemId + " has an invalid " + field + " " + value);
		}
	}
}
