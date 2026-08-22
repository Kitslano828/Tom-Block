package org.tomdang.custommobframework;

import lombok.Getter;
import org.bukkit.entity.EntityType;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.custommobframework.custommobdrops.CustomMobDrop;

import java.util.ArrayList;
import java.util.List;

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

	public CustomMob(String id, EntityType entityType, String name, double maxHealth, double damage, MobType mobType, int xpAmount) {
		customMobDrops = new ArrayList<>();
		this.id = id;
		this.entityType = entityType;
		this.name = name;
		this.maxHealth = maxHealth;
		this.damage = damage;
		this.mobType = mobType;
		this.xpAmount = xpAmount;
	}

	public void addMobDrops(CustomItem customItem, int amount, double chance) {
		customMobDrops.add(new CustomMobDrop(customItem, amount, chance));
	}
}
