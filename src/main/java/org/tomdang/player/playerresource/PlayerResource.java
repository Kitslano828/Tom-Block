package org.tomdang.player.playerresource;

import lombok.Getter;

public class PlayerResource {
	@Getter
	private double current;
	@Getter
	private double maximum = 100;

	public void addCurrent(double amount, double effectiveMaximum) {
		if (amount < 0) throw new IllegalArgumentException("Amount needs to be a positive number");
		if (effectiveMaximum < 0) throw new IllegalArgumentException("EffectiveMaximum needs to be a positive number");
		if (this.current + amount > effectiveMaximum) {
			this.current = effectiveMaximum;
		} else {
			this.current += amount;
		}
	}

	public void removeCurrent(double amount) {
		if (amount < 0) throw new IllegalArgumentException("Amount needs to be a positive number");
		if (this.current - amount < 0) {
			this.current = 0;
		} else {
			this.current -= amount;
		}
	}

	public void restoreFull(double effectiveMaximum) {
		if (effectiveMaximum < 0 ) throw new IllegalArgumentException("Effective Maximum needs to be positive");
		this.current = effectiveMaximum;
	}

	public void setMaximum(double amount) {
		if (amount < 0) throw new IllegalArgumentException("amount cannot be negative");
		this.maximum = amount;
		if (this.current > amount) {
			this.current = amount;
		}
	}

	public void setCurrent(double amount) {
		if (amount < 0) {
			this.current = 0;
		} else {
			this.current = amount;
		}
	}
}
