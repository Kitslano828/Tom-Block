package org.tomdang.customitemframework;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

@Getter
public enum Rarity {
	COMMON(NamedTextColor.WHITE),
	UNCOMMON(NamedTextColor.GREEN),
	RARE(NamedTextColor.BLUE),
	EPIC(NamedTextColor.DARK_PURPLE),
	LEGENDARY(NamedTextColor.GOLD),
	MYTHIC(NamedTextColor.LIGHT_PURPLE),
	ULTRA_RARE(NamedTextColor.AQUA),
	ONE_OF_ONE(NamedTextColor.DARK_BLUE);


	private final TextColor color;

	Rarity(TextColor color) {
		this.color = color;
	}

}

