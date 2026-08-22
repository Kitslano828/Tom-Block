package org.tomdang.combat.customcombatability.abilities;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.Color;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.tomdang.TomBlock;
import org.tomdang.combat.customcombatability.AbilityDamageHandler;
import org.tomdang.combat.customcombatability.CustomCombatAbility;
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.customability.AbilityExecutionContext;

public class MagicBoltAbility extends CustomCombatAbility {

	private final AbilityDamageHandler abilityDamageHandler;

	public MagicBoltAbility(TomBlock instance, String abilityID, String abilityName, double energyCost,
	                        AbilityTrigger abilityTrigger, double abilityDamage, long cooldownInTicks,
	                        Component abilityDescription, AbilityDamageHandler abilityDamageHandler) {
		super(abilityID, abilityName, energyCost, instance, abilityTrigger, cooldownInTicks, abilityDescription, abilityDamage);
		this.abilityDamageHandler = abilityDamageHandler;
	}

	@Override
	public void execute(AbilityExecutionContext abilityExecutionContext) {
		Player player = abilityExecutionContext.getPlayer();
		Location startLoc = player.getEyeLocation();
		Vector direction = startLoc.getDirection().normalize();

		// 1. Spawn the projectile head (Floating visual Glowstone block)
		BlockDisplay projectileHead = player.getWorld().spawn(startLoc, BlockDisplay.class);
		projectileHead.setBlock(Bukkit.createBlockData(Material.GLOWSTONE));

		// Optional: Scale down the block slightly (0.5x) so it looks like a concentrated bullet
		Transformation transformation = projectileHead.getTransformation();
		transformation.getScale().set(0.25f, 0.25f, 0.25f);
		projectileHead.setTransformation(transformation);

		// Play a neat casting sound effect
		player.getWorld().playSound(startLoc, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.3f);

		// 2. Start the tick-by-tick tracking loop
		new BukkitRunnable() {
			int ticksPassed = 0;
			final int maxTicks = 40;          // Max lifetime (2 seconds)
			final double blocksPerTick = 1.2; // Velocity (Speed multiplier)
			final Location currentLoc = startLoc.clone();

			@Override
			public void run() {
				// Safety check: terminate if entity is destroyed or expires
				if (ticksPassed >= maxTicks || !projectileHead.isValid()) {
					cleanup();
					return;
				}

				// Move the tracking location forward
				currentLoc.add(direction.clone().multiply(blocksPerTick));
				projectileHead.teleport(currentLoc);

				// 3. Create the yellow particle trail
				// DustOptions configures the color (RGB) and size of the particle
				Particle.DustOptions yellowDust = new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.0f);
				currentLoc.getWorld().spawnParticle(Particle.DUST, currentLoc, 4, 0.1, 0.1, 0.1, 0.0, yellowDust);

				// 4. Hit Detection: Check solid blocks
				if (currentLoc.getBlock().getType().isSolid()) {
					explode(currentLoc);
					cleanup();
					return;
				}

				// 5. Hit Detection: Check entities (Exclude the casting player)
				for (Entity entity : currentLoc.getWorld().getNearbyEntities(currentLoc, 0.7, 0.7, 0.7)) {
					if (entity instanceof LivingEntity target && entity != player) {

						abilityDamageHandler.applyAbilityDamage(target, abilityExecutionContext, MagicBoltAbility.this);

						explode(currentLoc);
						cleanup();
						return;
					}
				}

				ticksPassed++;
			}

			// Clean up task helper
			private void cleanup() {
				projectileHead.remove();
				cancel();
			}
		}.runTaskTimer(super.getInstance(), 0L, 1L); // Runs once every single server tick
	}

	// Visual effect for when the project strikes a target or wall
	private void explode(Location loc) {
		loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 1);
		loc.getWorld().playSound(loc, Sound.ENTITY_ITEM_BREAK, 1.0f, 0.8f);
	}
}
