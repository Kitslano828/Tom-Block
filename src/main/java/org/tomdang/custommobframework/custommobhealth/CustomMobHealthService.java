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
import org.tomdang.combat.eligibility.AttackDelivery;
import org.tomdang.combat.eligibility.AttackEligibilityCalculator;
import org.tomdang.combat.eligibility.AttackSource;

import java.util.Set;


public class CustomMobHealthService {

	private final CustomMobContextRegistry customMobContextRegistry;
	private final CustomMobResolver customMobResolver;
	private final NamespacedKey currentHealthKey;
	private final AttackEligibilityCalculator eligibility = new AttackEligibilityCalculator();
	private final CustomMobHitFeedback hitFeedback;

	public CustomMobHealthService(CustomMobContextRegistry customMobContextRegistry,
	                              CustomMobResolver customMobResolver, NamespacedKey currentHealthKey) {
		this(customMobContextRegistry, customMobResolver, currentHealthKey, null);
	}

	public CustomMobHealthService(CustomMobContextRegistry customMobContextRegistry,
	                              CustomMobResolver customMobResolver, NamespacedKey currentHealthKey,
	                              CustomMobHitFeedback hitFeedback) {
		this.customMobContextRegistry = customMobContextRegistry;
		this.customMobResolver = customMobResolver;
		this.currentHealthKey = currentHealthKey;
		this.hitFeedback = hitFeedback;
	}

	public void damageMob(Player damageDealer, LivingEntity entity, double amount) {
		damageMob(damageDealer, entity, amount, new AttackSource(AttackDelivery.BASIC_ATTACK, Set.of()));
	}

	public boolean canDamage(LivingEntity entity, AttackSource source) {
		if (entity == null || source == null) throw new IllegalArgumentException("entity and source are required");
		CustomMob mob = customMobResolver.getCustomMob(entity);
		return mob != null && eligibility.canDamage(mob.getAttackEligibilityRule(), source);
	}

	public boolean damageMob(Player damageDealer, LivingEntity entity, double amount, AttackSource source) {
		if (!canDamage(entity, source)) {
			rejectedAttack(damageDealer, entity);
			return false;
		}
		CustomMobContext customMobContext = customMobContextRegistry.getCustomMobContext(entity.getUniqueId());
		if (customMobContext == null) {
			CustomMob customMob = customMobResolver.getCustomMob(entity);
			if (customMob == null) return false;
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
			if (hitFeedback != null) hitFeedback.onAcceptedHit(entity,
					customMobContext.getCurrentHealth(), customMobContext.getCustomMob().getMaxHealth());
			if (customMobContext.isDead()) {
				if (damageDealer != null) {
					entity.setKiller(damageDealer);
				}
				entity.setHealth(0);
			}
		}
		return true;
	}

	public void rejectedAttack(Player attacker, LivingEntity target) {
		if (hitFeedback != null) hitFeedback.onRejectedHit(attacker, target);
	}

	public Component updateHealthDisplay(CustomMobContext context) {
		return Component.text(context.getCustomMob().getName() + " " ).append(Component.text("❤"+ (int)context.getCurrentHealth()).color(NamedTextColor.RED));
	}

}
