package org.tomdang.custommobframework.custommobhealth;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/** Presentation hook; health and eligibility rules remain independent of display entities. */
public interface CustomMobHitFeedback {
	void onAcceptedHit(LivingEntity target, double currentHealth, double maxHealth);
	void onRejectedHit(Player attacker, LivingEntity target);
}
