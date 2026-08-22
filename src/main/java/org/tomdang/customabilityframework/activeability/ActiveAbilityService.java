package org.tomdang.customabilityframework.activeability;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActiveAbilityService {

	private final Map<UUID, Map<String, Long>> activeAbilityMap = new HashMap<>();

	public void activateAbility(Player player, String abilityID, long durationInTicks) {
		UUID playerUUID = player.getUniqueId();
		if (!activeAbilityMap.containsKey(playerUUID)) {
			Map<String, Long> playerActiveAbilityMap = new HashMap<>();
			activeAbilityMap.put(playerUUID, playerActiveAbilityMap);
		}

		long endingTick = Bukkit.getCurrentTick() + durationInTicks;
	 	Map<String, Long> playerActiveAbilityMap = activeAbilityMap.get(player.getUniqueId());

		playerActiveAbilityMap.put(abilityID, endingTick);
		activeAbilityMap.put(player.getUniqueId(), playerActiveAbilityMap);
	}

	public boolean isAbilityActive(Player player, String abilityID) {
		UUID playerUUID = player.getUniqueId();

		Map<String, Long> playerActiveAbilityMap = activeAbilityMap.get(playerUUID);
		if (activeAbilityMap.get(playerUUID) == null) return false;

		if (!playerActiveAbilityMap.containsKey(abilityID)) return false;

		long expirationTick = playerActiveAbilityMap.get(abilityID);

		if (Bukkit.getCurrentTick() <= expirationTick) return true;

		playerActiveAbilityMap.remove(abilityID);
		if (activeAbilityMap.get(playerUUID).isEmpty()) {
			activeAbilityMap.remove(playerUUID);
		}
		return false;
	}


}
