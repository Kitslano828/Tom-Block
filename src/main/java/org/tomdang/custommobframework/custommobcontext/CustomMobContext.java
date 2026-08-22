package org.tomdang.custommobframework.custommobcontext;

import lombok.Getter;
import lombok.Setter;
import org.tomdang.custommobframework.CustomMob;

import java.util.UUID;

public class CustomMobContext {

	@Getter
	private final UUID mobUUID;
	@Getter @Setter
	private double currentHealth;
	@Getter
	private final CustomMob customMob;

	public CustomMobContext(UUID mobUUID, double currentHealth, CustomMob customMob) {
		this.mobUUID = mobUUID;
		this.currentHealth = currentHealth;
		this.customMob = customMob;
	}

	public boolean isDead() {
		return this.currentHealth <= 0;
	}

	public void reduceCurrentHealth(double amount) {
		if (this.currentHealth - amount < 0) {
			this.currentHealth = 0;
		} else {
			this.currentHealth -= amount;
		}
	}

}
