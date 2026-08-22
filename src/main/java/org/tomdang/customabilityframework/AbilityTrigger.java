package org.tomdang.customabilityframework;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

@Getter
public enum AbilityTrigger {
	RIGHT_CLICK(Component.text("RIGHT CLICK", NamedTextColor.YELLOW).decoration(TextDecoration.BOLD,true).decoration(TextDecoration.ITALIC,false)),
	SNEAK_RIGHT_CLICK(Component.text("SNEAK RIGHT CLICK", NamedTextColor.YELLOW).decoration(TextDecoration.BOLD,true).decoration(TextDecoration.ITALIC,false)),
	SNEAK_LEFT_CLICK(Component.text("SNEAK LEFT CLICK", NamedTextColor.YELLOW).decoration(TextDecoration.BOLD,true).decoration(TextDecoration.ITALIC,false));

	private final Component text;

	AbilityTrigger(Component component) {
		this.text = component;
	}
}
