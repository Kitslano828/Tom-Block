package org.tomdang.player;



import lombok.Getter;
import lombok.Setter;
import org.tomdang.player.playerresource.PlayerResource;

import java.util.UUID;

public class PlayerProfile {

	@Getter
	private final UUID uuid;

	@Getter @Setter
	private int miningXP = 0;
	@Getter @Setter
	private int miningLVL = 1;
	@Getter @Setter
	private int combatXP = 0;
	@Getter @Setter
	private int combatLvl = 1;

	@Getter @Setter
	private double miningFortune = 0;
	@Getter @Setter
	private double strength = 0.0;

	@Getter @Setter
	private double prosperity = 0;
	@Getter @Setter
	private double defense = 0;

	@Getter
	private final PlayerResource health = new PlayerResource();
	@Getter
	private final PlayerResource energy = new PlayerResource();

	public PlayerProfile (UUID uuid) {
		this.uuid = uuid;
	}

	public void increaseMiningXP(int amount) {
		this.miningXP += amount;
	}

	public void increaseCombatXP(int amount) {
		this.combatXP += amount;
	}

	public void increaseMiningFortune(double amount) {
		this.miningFortune += amount;
	}

	public void increaseMiningLevel(int amount) {
		this.miningLVL += amount;
	}

	public boolean isDead() {
		return health.getCurrent() <= 0.0;
	}

	public void reduceDefense(double amount) {
		if (this.defense - amount < 0) {
			this.defense = 0;
		} else {
			this.defense -= amount;
		}
	}
}

