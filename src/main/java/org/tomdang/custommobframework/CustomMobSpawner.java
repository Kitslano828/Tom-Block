package org.tomdang.custommobframework;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Zombie;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.tomdang.custommobframework.custommobcontext.CustomMobContext;
import org.tomdang.custommobframework.custommobcontext.CustomMobContextRegistry;
import org.tomdang.custommobframework.custommobspawn.CustomMobSpawnPoint;
import org.tomdang.custommobframework.custommobspawn.MobSpawnRegionPolicy;
import org.tomdang.region.bukkit.BukkitBlockPositionAdapter;

public class CustomMobSpawner {

	private final NamespacedKey customMobKey;
	private final NamespacedKey spawnPointIDKey;
	private final NamespacedKey currentHealthKey;
	CustomMobContextRegistry customMobContextRegistry;
	private final MobSpawnRegionPolicy spawnRegionPolicy;
	private final BukkitBlockPositionAdapter blockPositions = new BukkitBlockPositionAdapter();

	public CustomMobSpawner(NamespacedKey customMobKey, NamespacedKey spawnPointIDKey,
	                        NamespacedKey currentHealthKey,
	                        CustomMobContextRegistry customMobContextRegistry, MobSpawnRegionPolicy spawnRegionPolicy) {
		this.customMobKey = customMobKey;
		this.spawnPointIDKey = spawnPointIDKey;
		this.currentHealthKey = currentHealthKey;
		this.customMobContextRegistry = customMobContextRegistry;
		this.spawnRegionPolicy = spawnRegionPolicy;
	}

	public Entity createCustomMob(CustomMob customMob, Location location, CustomMobSpawnPoint customMobSpawnPoint) {
		if (customMob == null || location == null) throw new IllegalArgumentException("mob and location are required");
		if (!spawnRegionPolicy.allows(customMob.getId(), blockPositions.fromLocation(location))) return null;
		Entity entity = location.getWorld().spawnEntity(location, customMob.getEntityType());

		applyEntityBehavior(customMob, entity);

		CustomMobContext customMobContext = new CustomMobContext(entity.getUniqueId(), customMob.getMaxHealth(), customMob);
		customMobContextRegistry.addCustomMobContextToRegistry(customMobContext);
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		pdc.set(currentHealthKey, PersistentDataType.DOUBLE, customMob.getMaxHealth());
		if (customMobSpawnPoint != null) {
			String customSpawnID = customMobSpawnPoint.getSpawnPointID();
			pdc.set(spawnPointIDKey, PersistentDataType.STRING, customSpawnID);
		}

		entity.customName(Component.text(customMob.getName() + " " ).append(Component.text("❤"+ (int)customMobContext.getCurrentHealth()).color(NamedTextColor.RED)));
		entity.setCustomNameVisible(true);

		if (entity instanceof LivingEntity livingEntity) {
			var attribute = livingEntity.getAttribute(Attribute.MAX_HEALTH);
			if (attribute != null) {
				attribute.setBaseValue(customMob.getMaxHealth());
				livingEntity.setHealth(customMob.getMaxHealth());
			}
		}

		pdc.set(customMobKey, PersistentDataType.STRING, customMob.getId());


		return entity;
	}

	public void applyEntityBehavior(CustomMob customMob, Entity entity) {
		if (customMob == null) throw new IllegalArgumentException("customMob cannot be null");
		if (entity == null) throw new IllegalArgumentException("entity cannot be null");

		if (entity instanceof Mob) {
			((Mob) entity).setRemoveWhenFarAway(false);
		}
		if (entity instanceof Zombie zombie) {
			zombie.setShouldBurnInDay(customMob.isBurnsInDaylight());
		}
	}
}
