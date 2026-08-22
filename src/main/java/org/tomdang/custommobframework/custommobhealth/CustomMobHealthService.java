package org.tomdang.custommobframework.custommobhealth;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.tomdang.custommobframework.custommobcontext.CustomMobContext;
import org.tomdang.custommobframework.custommobcontext.CustomMobContextRegistry;


public class CustomMobHealthService {

	private final CustomMobContextRegistry customMobContextRegistry;

	public CustomMobHealthService(CustomMobContextRegistry customMobContextRegistry) {
		this.customMobContextRegistry = customMobContextRegistry;
	}

	public void damageMob(Player damageDealer, LivingEntity entity, double amount) {
		CustomMobContext customMobContext = customMobContextRegistry.getCustomMobContext(entity.getUniqueId());

		if (customMobContext != null) {
			customMobContext.reduceCurrentHealth(amount);
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
