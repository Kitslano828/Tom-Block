package org.tomdang.custommobframework.custommobhealth;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.CustomMobResolver;
import org.tomdang.custommobframework.custommobcontext.CustomMobContext;
import org.tomdang.custommobframework.custommobcontext.CustomMobContextRegistry;


public class CustomMobHealthService {

	private final CustomMobContextRegistry customMobContextRegistry;
	private final CustomMobResolver customMobResolver;
	private final NamespacedKey currentHealthKey;

	public CustomMobHealthService(CustomMobContextRegistry customMobContextRegistry,
	                              CustomMobResolver customMobResolver, NamespacedKey currentHealthKey) {
		this.customMobContextRegistry = customMobContextRegistry;
		this.customMobResolver = customMobResolver;
		this.currentHealthKey = currentHealthKey;
	}

	public void damageMob(Player damageDealer, LivingEntity entity, double amount) {
		CustomMobContext customMobContext = customMobContextRegistry.getCustomMobContext(entity.getUniqueId());
		if (customMobContext == null) {
			CustomMob customMob = customMobResolver.getCustomMob(entity);
			if (customMob == null) return;
			Double savedHealth = entity.getPersistentDataContainer().get(currentHealthKey, PersistentDataType.DOUBLE);
			// Older mobs have no saved custom health; their Bukkit health is the best available fallback.
			double restored = savedHealth == null ? entity.getHealth() : savedHealth;
			if (!Double.isFinite(restored)) restored = customMob.getMaxHealth();
			customMobContext = new CustomMobContext(entity.getUniqueId(),
					Math.max(0, Math.min(restored, customMob.getMaxHealth())), customMob);
			customMobContextRegistry.addCustomMobContextToRegistry(customMobContext);
		}

		if (customMobContext != null) {
			customMobContext.reduceCurrentHealth(amount);
			entity.getPersistentDataContainer().set(currentHealthKey, PersistentDataType.DOUBLE,
					customMobContext.getCurrentHealth());
			entity.playHurtAnimation(0f);
			entity.customName(updateHealthDisplay(customMobContext));
			if (customMobContext.isDead()) {
				if (damageDealer != null) {
					entity.setKiller(damageDealer);
				}
				entity.setHealth(0);
			}
		}


	}

	public Component updateHealthDisplay(CustomMobContext context) {
		return Component.text(context.getCustomMob().getName() + " " ).append(Component.text("❤"+ (int)context.getCurrentHealth()).color(NamedTextColor.RED));
	}

}
