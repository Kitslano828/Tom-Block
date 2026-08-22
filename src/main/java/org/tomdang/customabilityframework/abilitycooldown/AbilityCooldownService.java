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
		long cooldownExpirationTick = Bukkit.getCurrentTick() + ability.getCooldownInTicks();

		abilityCooldownMap.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(ability.getAbilityID(), cooldownExpirationTick);
	}

	public boolean isAbilityOnCooldown(Player player, CustomAbility ability) {
		Map<String,Long> playerAbilityCooldownMap = abilityCooldownMap.get(player.getUniqueId());
		if (playerAbilityCooldownMap == null) return false;

		Long abilityExpirationTick = playerAbilityCooldownMap.get(ability.getAbilityID());
		if (abilityExpirationTick == null) return false;

		if (Bukkit.getCurrentTick() >= abilityExpirationTick) {
			playerAbilityCooldownMap.remove(ability.getAbilityID());
			if (playerAbilityCooldownMap.isEmpty()) abilityCooldownMap.remove(player.getUniqueId());
			return false;
		} else {
			return true;
		}
	}

}
