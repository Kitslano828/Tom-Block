package org.tomdang.customitemframework.lore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.tomdang.customabilityframework.abilitylore.AbilityLoreRenderer;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.stats.CustomItemStatLoreRenderer;
import org.tomdang.customitemframework.stats.CustomItemStatCapLoreRenderer;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CustomItemLoreRenderer {

	private final CustomItemStatLoreRenderer statLoreRenderer;
	private final AbilityLoreRenderer abilityLoreRenderer;
	private final CustomItemStatCapLoreRenderer capLoreRenderer;

	public CustomItemLoreRenderer() {
		this(new CustomItemStatLoreRenderer(), new AbilityLoreRenderer(), new CustomItemStatCapLoreRenderer());
	}

	public CustomItemLoreRenderer(PlayerStatPresentationRegistry presentations) {
		this(new CustomItemStatLoreRenderer(presentations), new AbilityLoreRenderer(),
				new CustomItemStatCapLoreRenderer(presentations));
	}

	public CustomItemLoreRenderer(CustomItemStatLoreRenderer statLoreRenderer,
	                              AbilityLoreRenderer abilityLoreRenderer) {
		this(statLoreRenderer, abilityLoreRenderer, new CustomItemStatCapLoreRenderer());
	}

	public CustomItemLoreRenderer(CustomItemStatLoreRenderer statLoreRenderer,
	                              AbilityLoreRenderer abilityLoreRenderer,
	                              CustomItemStatCapLoreRenderer capLoreRenderer) {
		if (statLoreRenderer == null) throw new IllegalArgumentException("statLoreRenderer cannot be null");
		if (abilityLoreRenderer == null) throw new IllegalArgumentException("abilityLoreRenderer cannot be null");
		if (capLoreRenderer == null) throw new IllegalArgumentException("capLoreRenderer cannot be null");
		this.statLoreRenderer = statLoreRenderer;
		this.abilityLoreRenderer = abilityLoreRenderer;
		this.capLoreRenderer = capLoreRenderer;
	}

	public List<Component> render(CustomItem customItem) {
		return render(customItem, List.of(), ItemLoreContext.defaults());
	}

	public List<Component> render(CustomItem customItem, Collection<Component> leadingLore) {
		return render(customItem, leadingLore, ItemLoreContext.defaults());
	}

	public List<Component> render(CustomItem customItem, ItemLoreContext context) {
		return render(customItem, List.of(), context);
	}

	public List<Component> render(CustomItem customItem, Collection<Component> leadingLore,
	                              ItemLoreContext context) {
		if (customItem == null) throw new IllegalArgumentException("customItem cannot be null");
		if (leadingLore == null) throw new IllegalArgumentException("leadingLore cannot be null");
		if (context == null) throw new IllegalArgumentException("context cannot be null");
		if (leadingLore.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("leadingLore cannot contain null elements");
		}

		List<Component> lore = new ArrayList<>(leadingLore);
		appendSection(lore, statLoreRenderer.render(customItem.getStatModifiers()));
		appendSection(lore, capLoreRenderer.render(customItem.getStatCapModifiers()));

		List<Component> abilityLore = new ArrayList<>();
		for (CustomAbility ability : customItem.getCustomAbilities()) {
			if (!abilityLore.isEmpty()) abilityLore.add(Component.empty());
			abilityLore.addAll(abilityLoreRenderer.convertCustomAbilityToLore(ability, context));
		}
		appendSection(lore, abilityLore);

		appendSection(lore, List.of(
				Component.text(customItem.getRarity() + " " + customItem.getItemCategory())
						.color(customItem.getRarity().getColor())
						.decoration(TextDecoration.ITALIC, false)
		));

		return List.copyOf(lore);
	}

	private void appendSection(List<Component> lore, Collection<Component> section) {
		if (section.isEmpty()) return;
		if (!lore.isEmpty()) lore.add(Component.empty());
		lore.addAll(section);
	}
}
