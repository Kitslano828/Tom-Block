package org.tomdang.custommobframework;

import lombok.Getter;
import org.bukkit.entity.EntityType;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.custommobframework.custommobdrops.CustomMobDrop;
import org.tomdang.combat.eligibility.AttackEligibilityRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CustomMob {
	@Getter
	private final String id;
	@Getter
	private final EntityType entityType;
	@Getter
	private final String name;
	@Getter
	private final double maxHealth;
	@Getter
	private final double damage;
	@Getter
	private final MobType mobType;
	@Getter
	private final List<CustomMobDrop> customMobDrops;
	@Getter
	private final int xpAmount;
	@Getter
	private final boolean burnsInDaylight;
	@Getter
	private final AttackEligibilityRule attackEligibilityRule;

	public CustomMob(String id, EntityType entityType, String name, double maxHealth, double damage, MobType mobType,
	                 int xpAmount, boolean burnsInDaylight) {
		this(id, entityType, name, maxHealth, damage, mobType, xpAmount, burnsInDaylight,
				new AttackEligibilityRule(Set.of()));
	}

	public CustomMob(String id, EntityType entityType, String name, double maxHealth, double damage, MobType mobType,
	                 int xpAmount, boolean burnsInDaylight, AttackEligibilityRule attackEligibilityRule) {
		if (attackEligibilityRule == null) throw new IllegalArgumentException("attackEligibilityRule cannot be null");
		customMobDrops = new ArrayList<>();
		this.id = id;
		this.entityType = entityType;
		this.name = name;
		this.maxHealth = maxHealth;
		this.damage = damage;
		this.mobType = mobType;
		this.xpAmount = xpAmount;
		this.burnsInDaylight = burnsInDaylight;
		this.attackEligibilityRule = attackEligibilityRule;
	}

	public void addMobDrops(CustomItem customItem, int amount, double chance) {
		customMobDrops.add(new CustomMobDrop(customItem, amount, chance));
	}
}
