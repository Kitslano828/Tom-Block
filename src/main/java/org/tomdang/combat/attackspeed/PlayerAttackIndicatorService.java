package org.tomdang.combat.attackspeed;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.player.playerresource.PlayerStatsService;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;

public final class PlayerAttackIndicatorService {
	private static final double UPDATE_TOLERANCE = 0.000001;

	private final Plugin plugin;
	private final HeldItemCombatResolver heldItemCombatResolver;
	private final PlayerStatsService statsService;
	private final AttackRecoveryCalculator recoveryCalculator;
	private final AttackIndicatorAttributeCalculator attributeCalculator;
	private final AttackRecoveryStatSelector recoveryStatSelector = new AttackRecoveryStatSelector();
	private final Map<UUID, Double> originalBaseValues = new HashMap<>();
	private BukkitTask updateTask;

	public PlayerAttackIndicatorService(Plugin plugin, HeldItemCombatResolver heldItemCombatResolver,
	                                    PlayerStatsService statsService,
	                                    AttackRecoveryCalculator recoveryCalculator) {
		if (plugin == null) throw new IllegalArgumentException("plugin cannot be null");
		if (heldItemCombatResolver == null) throw new IllegalArgumentException("heldItemCombatResolver cannot be null");
		if (statsService == null) throw new IllegalArgumentException("statsService cannot be null");
		if (recoveryCalculator == null) throw new IllegalArgumentException("recoveryCalculator cannot be null");
		this.plugin = plugin;
		this.heldItemCombatResolver = heldItemCombatResolver;
		this.statsService = statsService;
		this.recoveryCalculator = recoveryCalculator;
		this.attributeCalculator = new AttackIndicatorAttributeCalculator();
	}

	public void start() {
		if (updateTask != null) return;
		updateTask = plugin.getServer().getScheduler().runTaskTimer(
				plugin,
				() -> plugin.getServer().getOnlinePlayers().forEach(this::synchronize),
				0,
				1
		);
	}

	public void stop() {
		if (updateTask != null) {
			updateTask.cancel();
			updateTask = null;
		}
		plugin.getServer().getOnlinePlayers().forEach(this::restore);
		originalBaseValues.clear();
	}

	public void synchronize(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		AttributeInstance attackSpeedAttribute = player.getAttribute(Attribute.ATTACK_SPEED);
		if (attackSpeedAttribute == null) return;

		originalBaseValues.putIfAbsent(player.getUniqueId(), attackSpeedAttribute.getBaseValue());
		CustomItem heldItem = heldItemCombatResolver.resolve(player);
		long baseRecoveryTicks = heldItemCombatResolver.resolveBaseRecoveryTicks(heldItem);
		OptionalLong recovery = recoveryCalculator.calculate(
				baseRecoveryTicks,
				recoveryStatSelector.select(player, heldItem, statsService)
		);
		if (recovery.isEmpty()) return;
		double desiredAttributeValue = attributeCalculator.desiredValue(recovery.getAsLong());

		// Compensate for vanilla held-item modifiers while retaining them. This changes only the
		// attribute base required to make the final value match TomBlock's effective recovery.
		double adjustedBase = attributeCalculator.adjustedBase(
				attackSpeedAttribute.getBaseValue(),
				attackSpeedAttribute.getValue(),
				desiredAttributeValue
		);
		if (Double.isFinite(adjustedBase)
				&& Math.abs(attackSpeedAttribute.getBaseValue() - adjustedBase) > UPDATE_TOLERANCE) {
			attackSpeedAttribute.setBaseValue(adjustedBase);
		}
	}

	public void restore(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		Double originalBaseValue = originalBaseValues.remove(player.getUniqueId());
		if (originalBaseValue == null) return;
		AttributeInstance attackSpeedAttribute = player.getAttribute(Attribute.ATTACK_SPEED);
		if (attackSpeedAttribute != null) attackSpeedAttribute.setBaseValue(originalBaseValue);
	}
}
