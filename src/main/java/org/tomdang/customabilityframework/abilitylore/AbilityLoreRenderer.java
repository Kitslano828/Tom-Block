package org.tomdang.customabilityframework.abilitylore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.tomdang.customabilityframework.customability.CustomAbility;

import java.util.ArrayList;
import java.util.List;

public class AbilityLoreRenderer {

	public List<Component> convertCustomAbilityToLore(CustomAbility ability) {

		List<Component> abilityLore = new ArrayList<>();

		Component AbilityName = Component.text(ability.getAbilityName() + " ", NamedTextColor.GOLD).append(ability.getAbilityTrigger().getText()).decoration(TextDecoration.ITALIC,false);
		abilityLore.add(AbilityName);

		Component description = ability.getAbilityDescription().decoration(TextDecoration.ITALIC, false);
		abilityLore.add(description);

		abilityLore.add(Component.empty());

		abilityLore.addAll(ability.getAbilityStatLore());

		Component abilityCost = Component.text("Energy Cost: ", NamedTextColor.GRAY).append(Component.text(ability.getEnergyCost(), NamedTextColor.DARK_GREEN)).decoration(TextDecoration.ITALIC,false);;
		abilityLore.add(abilityCost);

		// Convert ticks to seconds as a double
		double seconds = ability.getCooldownInTicks() / 20.0;

		// Determine the formatted string
		String formattedSeconds;
		if (seconds % 1 == 0) {
			formattedSeconds = String.format("%.0f", seconds); // No decimal if whole number
		} else {
			formattedSeconds = String.format("%.1f", Math.floor(seconds) + 0.5); // Forces .5 for any fractional value
		}

		// Combine into your Adventure component
		Component cooldown = Component.text("Cooldown: ", NamedTextColor.GRAY)
				.append(Component.text(formattedSeconds + "s", NamedTextColor.DARK_AQUA)).decoration(TextDecoration.ITALIC,false);;
		abilityLore.add(cooldown);

		return abilityLore;
	}

}
