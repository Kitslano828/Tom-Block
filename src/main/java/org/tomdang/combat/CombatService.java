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
import org.tomdang.combat.damage.CriticalHitRoller;
import org.tomdang.combat.damage.RandomCriticalHitRoller;
import org.tomdang.combat.damage.PlayerAttackResult;
import org.tomdang.combat.attackspeed.AttackReadinessCalculation;
import org.tomdang.combat.attackspeed.AttackReadinessDamageScaler;
import org.tomdang.combat.attackspeed.PlayerAttackReadinessService;
import org.tomdang.combat.attackspeed.HeldItemCombatResolver;
import org.tomdang.combat.hit.PlayerCombatHitPublisher;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.CustomMobResolver;
import org.tomdang.custommobframework.custommobhealth.CustomMobHealthService;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.combat.eligibility.AttackDelivery;
import org.tomdang.combat.eligibility.AttackSource;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerresource.PlayerResourceService;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.PlayerStatValueFormatter;
import org.tomdang.player.stats.PlayerStatType;

import java.util.Optional;
import java.util.Set;

public class CombatService {

	private final CustomMobResolver customMobResolver;
	private final PlayerProfileService playerProfileService;
	private final PlayerStatsService playerStatsService;
	private final PlayerResourceService playerResourceService;
	private final CustomMobHealthService customMobHealthService;
	private final PlayerDamageCalculator playerDamageCalculator;
	private final CriticalHitRoller criticalHitRoller;
	private final PlayerAttackReadinessService attackReadinessService;
	private final AttackReadinessDamageScaler readinessDamageScaler;
	private final HeldItemCombatResolver heldItemCombatResolver;
	private final PlayerCombatHitPublisher hitPublisher;

	public CombatService(PlayerProfileService playerProfileService, CustomMobResolver customMobResolver,
	                     PlayerStatsService playerStatsService, PlayerResourceService playerResourceService,
	                     CustomMobHealthService customMobHealthService, PlayerDamageCalculator playerDamageCalculator,
	                     PlayerAttackReadinessService attackReadinessService,
	                     AttackReadinessDamageScaler readinessDamageScaler,
	                     HeldItemCombatResolver heldItemCombatResolver,
	                     PlayerCombatHitPublisher hitPublisher
	) {
		this(playerProfileService, customMobResolver, playerStatsService, playerResourceService,
				customMobHealthService, playerDamageCalculator, new RandomCriticalHitRoller(),
				attackReadinessService, readinessDamageScaler, heldItemCombatResolver, hitPublisher);
	}

	public CombatService(PlayerProfileService playerProfileService, CustomMobResolver customMobResolver,
	                     PlayerStatsService playerStatsService, PlayerResourceService playerResourceService,
	                     CustomMobHealthService customMobHealthService, PlayerDamageCalculator playerDamageCalculator,
	                     CriticalHitRoller criticalHitRoller, PlayerAttackReadinessService attackReadinessService,
	                     AttackReadinessDamageScaler readinessDamageScaler,
	                     HeldItemCombatResolver heldItemCombatResolver,
	                     PlayerCombatHitPublisher hitPublisher) {
		if (playerDamageCalculator == null) throw new IllegalArgumentException("playerDamageCalculator cannot be null");
		if (criticalHitRoller == null) throw new IllegalArgumentException("criticalHitRoller cannot be null");
		if (attackReadinessService == null) throw new IllegalArgumentException("attackReadinessService cannot be null");
		if (readinessDamageScaler == null) throw new IllegalArgumentException("readinessDamageScaler cannot be null");
		if (heldItemCombatResolver == null) throw new IllegalArgumentException("heldItemCombatResolver cannot be null");
		if (hitPublisher == null) throw new IllegalArgumentException("hitPublisher cannot be null");
		this.playerProfileService = playerProfileService;
		this.customMobResolver = customMobResolver;
		this.playerStatsService = playerStatsService;
		this.playerResourceService = playerResourceService;
		this.customMobHealthService = customMobHealthService;
		this.playerDamageCalculator = playerDamageCalculator;
		this.criticalHitRoller = criticalHitRoller;
		this.attackReadinessService = attackReadinessService;
		this.readinessDamageScaler = readinessDamageScaler;
		this.heldItemCombatResolver = heldItemCombatResolver;
		this.hitPublisher = hitPublisher;
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
		CustomItem attackingItem = heldItemCombatResolver.resolve(player);
		AttackSource source = new AttackSource(AttackDelivery.BASIC_ATTACK,
				attackingItem == null ? Set.of() : attackingItem.getCombatProfile().attackCapabilities());
		if (!customMobHealthService.canDamage((LivingEntity) event.getEntity(), source)) {
			customMobHealthService.rejectedAttack(player, (LivingEntity) event.getEntity());
			return;
		}
		PlayerCombatHitContext context = createHitContext(player, (LivingEntity) event.getEntity());
		if (!customMobHealthService.damageMob(player, context.target(), context.damage(), source)) return;
		hitPublisher.publish(context);
		String prefix = context.critical() ? "CRITICAL HIT! " : "";
		event.getDamager().sendMessage(prefix + "YOU DEALT "
				+ PlayerStatValueFormatter.format(context.damage()) + " DAMAGE!");
	}

	public PlayerCombatHitContext createHitContext(Player player, LivingEntity target) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		if (target == null) throw new IllegalArgumentException("target cannot be null");
		CustomItem heldItem = heldItemCombatResolver.resolve(player);
		long baseRecoveryTicks = heldItemCombatResolver.resolveBaseRecoveryTicks(heldItem);
		AttackReadinessCalculation readiness = attackReadinessService.consume(
				player.getUniqueId(), baseRecoveryTicks,
				playerStatsService.getTotalStat(player, PlayerStatType.ATTACK_SPEED));
		PlayerAttackResult fullAttack = attackResult(player);
		double scaledDamage = readinessDamageScaler.scale(fullAttack.damage(), readiness.readiness());
		return new PlayerCombatHitContext(
				player,
				target,
				Optional.ofNullable(heldItem),
				heldItem == null ? Optional.empty() : heldItem.getCombatProfile().weightClass(),
				heldItem == null ? Optional.empty() : heldItem.getCombatProfile().damageType(),
				readiness,
				fullAttack.damage(),
				scaledDamage,
				fullAttack.critical()
		);
	}

	public double finalDamage(Player player) {
		return attackResult(player).damage();
	}

	public PlayerAttackResult attackResult(Player player) {
		double totalDamage = playerStatsService.getTotalDamage(player);
		double totalStrength = playerStatsService.getTotalStrength(player);
		double criticalChance = playerStatsService.getTotalCritChance(player);
		double criticalDamage = playerStatsService.getTotalCritDamage(player);
		boolean critical = criticalHitRoller.isCritical(criticalChance);
		return playerDamageCalculator.calculateBasicAttack(totalDamage, totalStrength, criticalDamage, critical);
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
