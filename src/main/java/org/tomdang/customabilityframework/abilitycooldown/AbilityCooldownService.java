package org.tomdang.customabilityframework.abilitycooldown;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.tomdang.TomBlock;
import org.tomdang.customabilityframework.customability.CustomAbility;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityCooldownService {

	Map<UUID, Map<String, Long>> abilityCooldownMap = new HashMap<>();

	public void startAbilityCooldown(Player player, CustomAbility ability) {
		startAbilityCooldown(player, ability.getAbilityID(), ability.getCooldownInTicks());
	}

	public void startAbilityCooldown(Player player, String sourceId, long cooldownInTicks) {
		long cooldownExpirationTick = Bukkit.getCurrentTick() + cooldownInTicks;

		abilityCooldownMap.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(sourceId, cooldownExpirationTick);
	}

	public boolean isAbilityOnCooldown(Player player, CustomAbility ability) {
		return isAbilityOnCooldown(player, ability.getAbilityID());
	}

	public boolean isAbilityOnCooldown(Player player, String sourceId) {
		Map<String,Long> playerAbilityCooldownMap = abilityCooldownMap.get(player.getUniqueId());
		if (playerAbilityCooldownMap == null) return false;

		Long abilityExpirationTick = playerAbilityCooldownMap.get(sourceId);
		if (abilityExpirationTick == null) return false;

		if (Bukkit.getCurrentTick() >= abilityExpirationTick) {
			playerAbilityCooldownMap.remove(sourceId);
			if (playerAbilityCooldownMap.isEmpty()) abilityCooldownMap.remove(player.getUniqueId());
			return false;
		} else {
			return true;
		}
	}

}
