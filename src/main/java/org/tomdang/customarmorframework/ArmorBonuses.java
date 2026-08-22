package org.tomdang.customarmorframework;

import lombok.Getter;
import lombok.Setter;

public class ArmorBonuses {

	@Setter @Getter
	private double healthBonus;
	@Setter @Getter
	private double defenseBonus;

	public void addToHealthBonus(double amount) {
		this.healthBonus += amount;
	}

	public void addToDefenseBonus(double amount) {
		this.defenseBonus += amount;
	}
}
