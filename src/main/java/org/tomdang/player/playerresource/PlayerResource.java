package org.tomdang.player.playerresource;

import lombok.Getter;

public class PlayerResource {
	@Getter
	private double current;
	@Getter
	private double maximum = 100;

	public void addCurrent(double amount, double suppliedMaximum) {
		if (this.current + amount > suppliedMaximum) {
			this.current = suppliedMaximum;
		} else {
			this.current += amount;
		}
	}

	public void removeCurrent(double amount) {
		if (this.current - amount < 0) {
			this.current = 0;
		} else {
			this.current -= amount;
		}
	}

	public void restoreFull(double suppliedMaximum) {
		this.current = suppliedMaximum;
	}

	public void setMaximum(double amount) {
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
