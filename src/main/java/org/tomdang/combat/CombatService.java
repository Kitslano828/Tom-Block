package org.tomdang.combat;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import org.tomdang.combat.damage.PlayerDamageCalculator;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.CustomMobResolver;
import org.tomdang.custommobframework.custommobhealth.CustomMobHealthService;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerresource.PlayerResourceService;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.PlayerStatValueFormatter;

public class CombatService {

	private final CustomMobResolver customMobResolver;
	private final PlayerProfileService playerProfileService;
	private final PlayerStatsService playerStatsService;
	private final PlayerResourceService playerResourceService;
	private final CustomMobHealthService customMobHealthService;
	private final PlayerDamageCalculator playerDamageCalculator;

	public CombatService(PlayerProfileService playerProfileService, CustomMobResolver customMobResolver,
	                     PlayerStatsService playerStatsService, PlayerResourceService playerResourceService,
	                     CustomMobHealthService customMobHealthService, PlayerDamageCalculator playerDamageCalculator
	) {
		if (playerDamageCalculator == null) throw new IllegalArgumentException("playerDamageCalculator cannot be null");
		this.playerProfileService = playerProfileService;
		this.customMobResolver = customMobResolver;
		this.playerStatsService = playerStatsService;
		this.playerResourceService = playerResourceService;
		this.customMobHealthService = customMobHealthService;
		this.playerDamageCalculator = playerDamageCalculator;
	}

	public void onMobHit(EntityDamageByEntityEvent event) {
		Entity entity = event.getDamager();
		Entity entityAttacked = event.getEntity();

		if (entity instanceof Player) {
			if (event.getEntity() instanceof Player) { // Player hitting Player
				event.getDamager().sendMessage("You cannot Damage other players");
				event.setCancelled(true);
			} else {
				damageMob(event);
			}
		} else if (entityAttacked instanceof Player) { // mob hitting player
			mobHitPlayer(event);
		}
	}

	public void damageMob(EntityDamageByEntityEvent event) {
		Player player = (Player) event.getDamager();
		CustomMob customMob = customMobResolver.getCustomMob(event.getEntity());
		if (customMob == null) {
			return;
		}

		event.setCancelled(true);

		double finalDamage = finalDamage(player);
		customMobHealthService.damageMob(player, (LivingEntity) event.getEntity(),finalDamage);
		event.getDamager().sendMessage("YOU DEALT " + finalDamage + " DAMAGE!");
	}

	public double finalDamage(Player player) {
		double totalDamage = playerStatsService.getTotalDamage(player);
		double totalStrength = playerStatsService.getTotalStrength(player);
		return playerDamageCalculator.calculateBasicAttack(totalDamage, totalStrength);
	}

	public void mobHitPlayer(EntityDamageByEntityEvent event) {
		event.setCancelled(true);
		Player player = (Player) event.getEntity();
		CustomMob customMob = customMobResolver.getCustomMob(event.getDamager());

		Sound hurtSound = Sound.sound(
				Key.key("entity.player.hurt"),
				Sound.Source.PLAYER,
				1.0f, // Volume
				1.0f  // Pitch
		);
		player.playSound(hurtSound);
		player.playHurtAnimation(180);

		Vector knockbackDirection = player.getLocation().toVector().subtract(event.getDamager().getLocation().toVector());

		if (knockbackDirection.lengthSquared() > 0) {
			knockbackDirection.normalize();
		} else {
			knockbackDirection = new Vector(0, 0, 1);
		}

		// 3. Scale and Apply the Knockback
		double horizontalStrength = 0.5; // Controls distance away (default is ~0.4 - 0.5)
		double verticalStrength = 0.35;   // Controls the pop-up height into the air

		knockbackDirection.multiply(horizontalStrength);
		knockbackDirection.setY(verticalStrength); // Give a slight upward lift

		player.setVelocity(knockbackDirection);

		if (customMob == null) {
			event.getEntity().sendMessage("A non-custom mob damaged you!");
		} else {
			calculateDamage(player, customMob);
		}
	}

	public void calculateDamage(Player player, CustomMob customMob) {
		double mobDamage = customMob.getDamage();
		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());

		double totalDefense = playerStatsService.getTotalDefense(player);

		double finalDamage = (mobDamage * 100) / (100+ totalDefense);

		playerResourceService.damagePlayer(player, finalDamage);
		player.sendMessage("You took " + finalDamage + " damage!" + " Reduced from " + mobDamage + " by your defense!");
		player.sendMessage("Health: "
				+ PlayerStatValueFormatter.format(playerProfile.getHealth().getCurrent())
				+ " / "
				+ PlayerStatValueFormatter.format(playerStatsService.getTotalHealthStat(player)));
		if (playerProfile.isDead()) {
			player.sendMessage("YOU HAVE BEEN KILLED BY " + customMob.getName());
			player.setHealth(0.0);
		}
	}

	public void respawnPlayer(Player player) {
		playerResourceService.restoreHealthToMaximum(player);
		Location spawnPoint = player.getRespawnLocation();
		if (spawnPoint == null) {
			spawnPoint = player.getWorld().getSpawnLocation();
		}
		player.teleport(spawnPoint);
	}

}
