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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.tomdang.custommobframework.custommobcontext.CustomMobContext;
import org.tomdang.custommobframework.custommobcontext.CustomMobContextRegistry;
import org.tomdang.custommobframework.custommobspawn.CustomMobSpawnPoint;

import java.awt.*;

public class CustomMobSpawner {

	private final NamespacedKey customMobKey;
	private final NamespacedKey spawnPointIDKey;
	CustomMobContextRegistry customMobContextRegistry;

	public CustomMobSpawner(NamespacedKey customMobKey, NamespacedKey spawnPointIDKey, CustomMobContextRegistry customMobContextRegistry) {
		this.customMobKey = customMobKey;
		this.spawnPointIDKey = spawnPointIDKey;
		this.customMobContextRegistry = customMobContextRegistry;
	}

	public Entity createCustomMob(CustomMob customMob, Location location, CustomMobSpawnPoint customMobSpawnPoint) {
		// if CustomMobSpawnPoint Exist
		Entity entity = location.getWorld().spawnEntity(location, customMob.getEntityType());

		if (entity instanceof Mob) {
			((Mob) entity).setRemoveWhenFarAway(false);
		}

		CustomMobContext customMobContext = new CustomMobContext(entity.getUniqueId(), customMob.getMaxHealth(), customMob);
		customMobContextRegistry.addCustomMobContextToRegistry(customMobContext);
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
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
}
