package org.tomdang.customabilityframework.abilitylore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.tomdang.customabilityframework.abilitycooldown.AbilityCooldownCalculator;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customitemframework.lore.ItemLoreContext;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.PlayerStatValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class AbilityLoreRenderer {

	private static final double TICKS_PER_SECOND = 20.0;

	private final AbilityCooldownCalculator cooldownCalculator;

	public AbilityLoreRenderer() {
		this(new AbilityCooldownCalculator());
	}

	public AbilityLoreRenderer(AbilityCooldownCalculator cooldownCalculator) {
		if (cooldownCalculator == null) throw new IllegalArgumentException("cooldownCalculator cannot be null");
		this.cooldownCalculator = cooldownCalculator;
	}

	public List<Component> convertCustomAbilityToLore(CustomAbility ability) {
		return convertCustomAbilityToLore(ability, ItemLoreContext.defaults());
	}

	public List<Component> convertCustomAbilityToLore(CustomAbility ability, ItemLoreContext context) {
		if (ability == null) throw new IllegalArgumentException("ability cannot be null");
		if (context == null) throw new IllegalArgumentException("context cannot be null");

		List<Component> abilityLore = new ArrayList<>();

		Component AbilityName = Component.text(ability.getAbilityName() + " ", NamedTextColor.GOLD).append(ability.getAbilityTrigger().getText()).decoration(TextDecoration.ITALIC,false);
		abilityLore.add(AbilityName);

		Component description = ability.getAbilityDescription().decoration(TextDecoration.ITALIC, false);
		abilityLore.add(description);

		abilityLore.add(Component.empty());

		abilityLore.addAll(ability.getAbilityStatLore());

		Component abilityCost = Component.text("Energy Cost: ", NamedTextColor.GRAY).append(Component.text(ability.getEnergyCost(), NamedTextColor.DARK_GREEN)).decoration(TextDecoration.ITALIC,false);;
		abilityLore.add(abilityCost);

		long effectiveCooldownTicks = cooldownCalculator.calculate(
				ability.getCooldownInTicks(),
				context.getEffectiveStat(PlayerStatType.ABILITY_HASTE)
		);
		String formattedSeconds = PlayerStatValueFormatter.format(effectiveCooldownTicks / TICKS_PER_SECOND);

		// Combine into your Adventure component
		Component cooldown = Component.text("Cooldown: ", NamedTextColor.GRAY)
				.append(Component.text(formattedSeconds + "s", NamedTextColor.DARK_AQUA)).decoration(TextDecoration.ITALIC,false);;
		abilityLore.add(cooldown);

		return abilityLore;
	}

}
