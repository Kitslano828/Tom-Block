package org.tomdang.customabilityframework;

import org.tomdang.customabilityframework.customability.CustomAbility;

import java.util.HashMap;
import java.util.Map;

public class CustomAbilityRegistry {

	private final Map<String, CustomAbility> customAbilityMap = new HashMap<>();

		public CustomAbilityRegistry() {

	}

	public void registerAbility(CustomAbility ability) {
		if (containsAbility(ability.getAbilityID())) throw new IllegalStateException("ability " + ability.getAbilityID() + " already exists");
		customAbilityMap.put(ability.getAbilityID(), ability);
	}

	public CustomAbility getCustomAbility(String id) {
		return customAbilityMap.get(id);
	}

	public boolean containsAbility(String id) {
		if (id == null || id.isBlank()) throw new IllegalArgumentException("id cannot be null or blank");
		return customAbilityMap.containsKey(id);
	}

}
